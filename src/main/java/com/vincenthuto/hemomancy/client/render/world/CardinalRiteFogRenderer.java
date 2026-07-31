package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CardinalRiteFogRenderer {
	private static final CardinalRiteFogState STATE = new CardinalRiteFogState();

	private CardinalRiteFogRenderer() {
	}

	public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			List<ActiveRiteClientData.RiteEntry> activeRites, float time, Camera camera) {
		List<CardinalRiteFogState.Sample> samples = STATE.update(activeRites, time);
		if (!enabled() || ShaderInit.CARDINAL_RITE_FOG.getInstance().get() == null) {
			if (!enabled()) {
				STATE.clear();
			}
			CardinalRiteFogLightning.clear();
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		long gameTick = (long) Math.floor(time);
		CardinalRiteFogLightning.tick(activeRites, gameTick, time - gameTick);
		int renderDistance = minecraft.options.renderDistance().get();
		Vec3 cameraPosition = camera.getPosition();
		for (CardinalRiteFogState.Sample sample : samples) {
			ActiveRiteClientData.RiteEntry rite = sample.rite();
			boolean legacy = "LEGACY".equals(rite.getPhase());
			float radius = CardinalRiteFogGeometry.perimeterRadius(
					rite.getFootprintRadius(), rite.getRiteSize(), rite.getCompletedRings(),
					rite.getTotalRings(), legacy);
			if (sample.opacity() <= 0.001F || !CardinalRiteFogGeometry.isWithinRenderDistance(
					rite.getCenter(), cameraPosition, radius, renderDistance)) {
				continue;
			}
			drawRiteFog(poseStack, buffer, rite.getCenter(), radius,
					time, sample.opacity(), camera);
		}
	}

	public static void clear() {
		STATE.clear();
		CardinalRiteFogLightning.clear();
	}

	private static boolean enabled() {
		return HemoClientConfig.RENDER_CARDINAL_RITE_FOG == null
				|| HemoClientConfig.RENDER_CARDINAL_RITE_FOG.get();
	}

	private static void drawRiteFog(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			BlockPos center, float radius, float time, float opacity, Camera camera) {
		float seed = fogSeed(center);
		double centerX = center.getX() + 0.5D;
		double centerY = CardinalRiteBoundaryGeometry.boundaryPlaneY(center.getY());
		double centerZ = center.getZ() + 0.5D;
		Vec3 cameraPosition = camera.getPosition();

		List<RenderedPuff> renderedPuffs = new ArrayList<>();
		for (CardinalRiteFogGeometry.FogPuff puff : CardinalRiteFogGeometry.puffs(seed, radius)) {
			CardinalRiteFogGeometry.PuffPosition position =
					CardinalRiteFogGeometry.position(puff, radius, time);
			double worldX = centerX + position.x();
			double worldY = centerY + fogVerticalOffset() + position.y();
			double worldZ = centerZ + position.z();
			double distanceSquared = cameraPosition.distanceToSqr(worldX, worldY, worldZ);
			renderedPuffs.add(new RenderedPuff(puff, position, distanceSquared));
		}
		renderedPuffs.sort(Comparator.comparingDouble(RenderedPuff::distanceSquared).reversed());

		Vec3 cameraRight = new Vec3(camera.getLeftVector()).scale(-1.0D);
		Vec3 cameraUp = new Vec3(camera.getUpVector());
		RenderType renderType = HemoRenderTypes.cardinalRiteFog(time, seed);
		VertexConsumer consumer = buffer.getBuffer(renderType);

		poseStack.pushPose();
		poseStack.translate(centerX - cameraPosition.x, centerY - cameraPosition.y,
				centerZ - cameraPosition.z);
		Matrix4f matrix = poseStack.last().pose();
		float verticalOffset = fogVerticalOffset();
		for (RenderedPuff rendered : renderedPuffs) {

			emitPuff(consumer, matrix, rendered.puff(), rendered.position(),
					cameraRight, cameraUp, verticalOffset, time, opacity);
		}
		poseStack.popPose();
		buffer.endBatch(renderType);
	}

	private static void emitPuff(VertexConsumer consumer, Matrix4f matrix,
			CardinalRiteFogGeometry.FogPuff puff,
			CardinalRiteFogGeometry.PuffPosition center,
			Vec3 cameraRight, Vec3 cameraUp, float verticalOffset,
			float time, float riteOpacity) {
		float roll = puff.rollRadians()
				+ (float) Math.sin(time * puff.driftSpeed() * 10.37F + puff.phase()) * 0.10F;
		float cosine = (float) Math.cos(roll);
		float sine = (float) Math.sin(roll);
		Vec3 horizontalAxis = cameraRight.scale(cosine).add(cameraUp.scale(sine))
				.scale(puff.halfWidth());
		Vec3 verticalAxis = cameraUp.scale(cosine).subtract(cameraRight.scale(sine))
				.scale(puff.halfHeight());
		Vec3 puffCenter = new Vec3(center.x(), center.y() + verticalOffset, center.z());

		// RGB carries stable per-puff shader metadata rather than a literal tint:
		// variant seed, crimson weighting, and brightness. Alpha is true opacity.
		float alpha = riteOpacity * puff.opacity()
				* CardinalRiteFogGeometry.opacityMultiplier(puff, time);
		emit(consumer, matrix, puffCenter.subtract(horizontalAxis).subtract(verticalAxis),
				0.0F, 0.0F, puff, alpha);
		emit(consumer, matrix, puffCenter.subtract(horizontalAxis).add(verticalAxis),
				0.0F, 1.0F, puff, alpha);
		emit(consumer, matrix, puffCenter.add(horizontalAxis).add(verticalAxis),
				1.0F, 1.0F, puff, alpha);
		emit(consumer, matrix, puffCenter.add(horizontalAxis).subtract(verticalAxis),
				1.0F, 0.0F, puff, alpha);
	}

	private static void emit(VertexConsumer consumer, Matrix4f matrix, Vec3 position,
			float u, float v, CardinalRiteFogGeometry.FogPuff puff, float alpha) {
		consumer.addVertex(matrix, (float) position.x, (float) position.y, (float) position.z)
				.setUv(u, v)
				.setColor(puff.variantSeed(), puff.crimsonWeight(), puff.brightness(), alpha);
	}

	private static float fogSeed(BlockPos center) {
		return center.getX() * 0.137F + center.getY() * 0.071F + center.getZ() * 0.193F;
	}

	private static float fogVerticalOffset() {
		return HemoClientConfig.CARDINAL_RITE_FOG_VERTICAL_OFFSET == null
				? 0.0F
				: HemoClientConfig.CARDINAL_RITE_FOG_VERTICAL_OFFSET.get().floatValue();
	}

	private record RenderedPuff(
			CardinalRiteFogGeometry.FogPuff puff,
			CardinalRiteFogGeometry.PuffPosition position,
			double distanceSquared) {
	}
}
