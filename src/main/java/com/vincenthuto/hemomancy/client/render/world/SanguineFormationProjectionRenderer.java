package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveSanguineFormationProjectionClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSanguineFormationProjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SanguineFormationProjectionRenderer {
	private static final int LAT_BANDS = 10;
	private static final int LON_BANDS = 16;
	private static final float BASE_RADIUS = 0.30F;
	private static final float GLOW_EXTRA = 0.10F;

	private SanguineFormationProjectionRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		float time = mc.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

		for (ActiveSanguineFormationProjectionClientData.ProjectionEntry entry
				: ActiveSanguineFormationProjectionClientData.getActiveProjections()) {
			Vec3 renderPos = faceCenter(entry.getPos(), entry.getFace());
			float progress = entry.getProgress();
			float fade = entry.getFadeProgress(partialTick);
			float visibility = entry.getVisibility(partialTick);
			if (visibility <= 0.01F) {
				continue;
			}

			float failureShrink = entry.getState() == PacketSanguineFormationProjection.State.FAILURE
					? 1.0F - 0.85F * fade : 1.0F;
			float coalesced = progress * progress;
			float radius = lerp(BASE_RADIUS, 0.10F, coalesced) * failureShrink;
			float pulse = 1.0F + 0.04F * (float) Math.sin(time * 0.35F + (entry.getSeed() & 15));
			radius = Math.max(0.04F, radius * pulse);

			float darken = entry.getState() == PacketSanguineFormationProjection.State.FAILURE
					? Math.max(progress, fade) : progress;
			float coreR = lerp(0.95F, 0.34F, darken);
			float coreG = lerp(entry.isAttuned() ? 0.09F : 0.04F, 0.0F, darken);
			float coreB = lerp(0.05F, 0.0F, darken);
			float coreA = (0.38F + 0.52F * progress) * visibility;
			float glowA = (0.30F * (1.0F - progress) + 0.03F) * visibility;
			if (entry.getState() == PacketSanguineFormationProjection.State.FAILURE) {
				coreR *= 0.55F;
				coreG *= 0.25F;
				coreB *= 0.25F;
				glowA *= 0.45F;
			}

			poseStack.pushPose();
			poseStack.translate(renderPos.x - camera.x, renderPos.y - camera.y, renderPos.z - camera.z);
			Matrix4f matrix = poseStack.last().pose();

			VertexConsumer core = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
			renderSphere(core, matrix, radius, time, entry.getSeed(), coreR, coreG, coreB, coreA);
			buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);

			VertexConsumer glow = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
			renderSphere(glow, matrix, radius + GLOW_EXTRA * (1.0F - coalesced), time, entry.getSeed() + 11L,
					0.55F, 0.02F, 0.02F, glowA);
			buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
			poseStack.popPose();
		}
	}

	private static Vec3 faceCenter(net.minecraft.core.BlockPos pos, Direction face) {
		return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.58D));
	}

	private static void renderSphere(VertexConsumer consumer, Matrix4f matrix, float baseRadius, float time, long seed,
			float red, float green, float blue, float alpha) {
		for (int lat = 0; lat < LAT_BANDS; lat++) {
			double theta0 = Math.PI * lat / LAT_BANDS;
			double theta1 = Math.PI * (lat + 1) / LAT_BANDS;

			for (int lon = 0; lon < LON_BANDS; lon++) {
				double phi0 = 2.0D * Math.PI * lon / LON_BANDS;
				double phi1 = 2.0D * Math.PI * (lon + 1) / LON_BANDS;

				float r00 = baseRadius + undulation(theta0, phi0, time, seed);
				float r10 = baseRadius + undulation(theta1, phi0, time, seed);
				float r11 = baseRadius + undulation(theta1, phi1, time, seed);
				float r01 = baseRadius + undulation(theta0, phi1, time, seed);

				addVertex(consumer, matrix, theta0, phi0, r00, red, green, blue, alpha);
				addVertex(consumer, matrix, theta1, phi0, r10, red, green, blue, alpha);
				addVertex(consumer, matrix, theta1, phi1, r11, red, green, blue, alpha);
				addVertex(consumer, matrix, theta0, phi1, r01, red, green, blue, alpha);
			}
		}
	}

	private static void addVertex(VertexConsumer consumer, Matrix4f matrix, double theta, double phi, float radius,
			float red, float green, float blue, float alpha) {
		float x = (float) (Math.sin(theta) * Math.cos(phi)) * radius;
		float y = (float) Math.cos(theta) * radius;
		float z = (float) (Math.sin(theta) * Math.sin(phi)) * radius;
		consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
	}

	private static float undulation(double theta, double phi, float time, long seed) {
		double offset = (seed & 255) * 0.013D;
		return (float) (0.018D * Math.sin(theta * 4.0D + time * 0.17D + offset)
				+ 0.012D * Math.cos(phi * 5.0D + time * 0.11D + offset));
	}

	private static float lerp(float from, float to, float amount) {
		return from + (to - from) * Math.max(0.0F, Math.min(1.0F, amount));
	}
}
