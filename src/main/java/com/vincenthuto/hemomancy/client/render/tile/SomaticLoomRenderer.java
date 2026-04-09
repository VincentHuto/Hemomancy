package com.vincenthuto.hemomancy.client.render.tile;

import java.util.Map;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.MemoryWeavingRecipeSerializer;
import com.vincenthuto.hemomancy.common.tile.SomaticLoomBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SomaticLoomRenderer implements BlockEntityRenderer<SomaticLoomBlockEntity> {

	/** Seeded random reused across frames — seed is set per-frame to keep fractal stable for a few ticks. */
	private static final Random FRAC_RAND = new Random();

	public SomaticLoomRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public boolean shouldRenderOffScreen(SomaticLoomBlockEntity te) {
		return true;
	}

	@Override
	public void render(SomaticLoomBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		float currentTime = te.getLevel().getGameTime() + partialTicks;
		Vector3 startVec = Vector3.fromBlockEntityCenter(te);

		// Only show effects when a hematic memory has been inserted
		boolean showEffects = !te.contents.get(0).isEmpty();

		// === Render items FIRST so they write to the depth buffer ===
		matrixStackIn.pushPose();
		renderItems(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		matrixStackIn.popPose();

		// === Render effects AFTER items ===
		if (showEffects) {
			double diameter = 4;
			matrixStackIn.pushPose();
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			matrixStackIn.translate(0.5f, 0.5f, 0.5f);

			Vec3 centerPos = startVec.add(0.5).toVec3();

			// Seed the fractal random per-tick so the lightning crackles actively
			FRAC_RAND.setSeed(te.getLevel().getGameTime() * 31L + te.getBlockPos().hashCode());

			drawCenter(matrixStackIn, bufferIn, te, centerPos, diameter, currentTime, combinedLightIn, combinedOverlayIn);

			// Enzyme indicator rings — one concentric black ring per enzyme dose level reached
			drawEnzymeIndicatorRings(matrixStackIn, bufferIn, te, centerPos, diameter, currentTime);

			double bloodVolume = te.getBloodVolume();
			double maxBloodVolume = te.getMaxBloodVolume();
			if (maxBloodVolume > 0) {
				double fillRatio = Mth.clamp(bloodVolume / maxBloodVolume, 0, 1);
				drawBloodVolumeRing(matrixStackIn, bufferIn, te, centerPos, diameter, fillRatio, currentTime,
						combinedLightIn, combinedOverlayIn);
			}

			// === Crafting progress ring ===
			if (te.isCrafting() && te.getCraftingTotalTime() > 0) {
				double progressRatio = 1.0 - ((double) te.getCraftingProgress() / te.getCraftingTotalTime());
				boolean pulsing = te.getCraftingPhase() == 2; // completing
				drawCraftingProgressRing(matrixStackIn, bufferIn, te, centerPos, diameter,
						progressRatio, pulsing, currentTime, combinedLightIn, combinedOverlayIn);
			}

			matrixStackIn.popPose();
		}
	}

	// ========================== ITEM RENDERING ==========================

	public void renderItems(SomaticLoomBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		float gameTime = te.getLevel().getGameTime() + partialTicks;
		Minecraft mc = Minecraft.getInstance();

		// Slot 0 — first item, floating above the center of the block
		ItemStack stack0 = te.contents.get(0);
		if (!stack0.isEmpty()) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0.5F, 1.25F, 0.5F);
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(gameTime).toMoj());
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			mc.getItemRenderer().renderStatic(null, stack0, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn,
					null, combinedLightIn, combinedOverlayIn, 0);
			matrixStackIn.popPose();
		}

		// Slot 1 — second item, floating above the first
		ItemStack stack1 = te.contents.get(1);
		if (!stack1.isEmpty()) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0.5F, 1.75F, 0.5F);
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(-gameTime * 0.75f).toMoj());
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			mc.getItemRenderer().renderStatic(null, stack1, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn,
					null, combinedLightIn, combinedOverlayIn, 0);
			matrixStackIn.popPose();
		}

		// Recipe result preview — floating above both items
		MemoryWeavingRecipe currRecipe = MemoryWeavingRecipeSerializer
				.getRecipe(te.getRecipePath());
		if (currRecipe != null) {
			matrixStackIn.pushPose();
			float bob = Mth.sin(gameTime * 0.1f) * 0.05f;
			matrixStackIn.translate(0.5F, 2.25F + bob, 0.5F);
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(gameTime * 0.5f).toMoj());
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			mc.getItemRenderer().renderStatic(null, currRecipe.getResultItem(te.getLevel().registryAccess()),
					ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn, null, combinedLightIn, combinedOverlayIn,
					0);
			matrixStackIn.popPose();
		}
	}

	// ========================== FRACTAL STAR ==========================

    /**
	 * Returns the funnel Y offset for a given radial distance from center.
	 * Points at baseRadius stay at baseY; points further out rise upward.
	 */
	private static double funnelY(double baseY, double radius, double baseRadius) {
        /** How much Y rises per unit of radial distance beyond the base — controls funnel steepness. */
        double FUNNEL_SLOPE = 0.98;
        return baseY + Math.max(0, radius - baseRadius) * FUNNEL_SLOPE;
	}

	private void drawCenter(PoseStack stack, MultiBufferSource bufferIn, SomaticLoomBlockEntity te, Vec3 from,
			double diameter, float currentTime, int combinedLightIn, int combinedOverlayIn) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();
		double cx = from.x;
		double cy = from.z;
		float rotAngle = -90f;
		double spikeBaseWidth = 22.75;
		double baseY = from.y + 2;

		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float affinity = affs.getOrDefault(tend, 0f);

			double cx1 = cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter;
			double cx2 = cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter;
			double cy1 = cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter;
			double cy2 = cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter;

			double depthDist = ((65 - diameter) * affinity * 0.0625 / 4 + diameter);
			double lx = cx + Math.cos(Math.toRadians(rotAngle)) * depthDist;
			double ly = cy + Math.sin(Math.toRadians(rotAngle)) * depthDist;

			double displace = (Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2)) / 2.0;

			// Funnel: Y rises with distance from center
			Vec3 tip = new Vec3(lx, funnelY(baseY, depthDist, diameter), ly);
			Vec3 base1 = new Vec3(cx1, funnelY(baseY, diameter, diameter), cy1);
			Vec3 base2 = new Vec3(cx2, funnelY(baseY, diameter, diameter), cy2);

			ParticleColor color = tend.getColor();

			// Core beams — fine detail fractal
			fracLine(stack, bufferIn, tip, base1, color, displace, 0.08, 0.05f);
			fracLine(stack, bufferIn, tip, base2, color, displace, 0.08, 0.05f);
			fracLine(stack, bufferIn, base1, tip, color, displace, 0.05, 0.04f);
			fracLine(stack, bufferIn, base2, tip, color, displace, 0.05, 0.04f);

			// Glow layer — thicker, more transparent, additive blend
			fracLineGlow(stack, bufferIn, tip, base1, color, displace, 0.15, 0.12f, 0.45f);
			fracLineGlow(stack, bufferIn, tip, base2, color, displace, 0.15, 0.12f, 0.45f);

			rotAngle += 45;
		}
	}

	// ========================== ENZYME INDICATOR RINGS ==========================

	/**
	 * Draws concentric black rings at the fractal star's Y-level. One ring appears
	 * for each 0.2f enzyme dose that has been reached by ANY tendency. The rings sit
	 * at radii matching where the spike tips would be at that affinity level, so they
	 * visually "line up" with the spikes.
	 *
	 * Max 5 rings (affinity steps: 0.2, 0.4, 0.6, 0.8, 1.0).
	 */
	private void drawEnzymeIndicatorRings(PoseStack stack, MultiBufferSource bufferIn,
			SomaticLoomBlockEntity te, Vec3 center, double diameter, float currentTime) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();

		// Find the highest enzyme step reached by any tendency
		float maxAffinity = 0f;
		for (Float val : affs.values()) {
			if (val != null && val > maxAffinity) {
				maxAffinity = val;
			}
		}
		// How many complete 0.2 steps have been reached
		int maxSteps = (int) Math.ceil(maxAffinity / 0.2f);
		maxSteps = Math.min(maxSteps, 5);

		if (maxSteps <= 0) return;

		double cx = center.x;
		double cz = center.z;
		double baseY = center.y + 2;
		int segments = 48;

		for (int step = 1; step <= maxSteps; step++) {
			float stepAffinity = step * 0.2f;
			// Match the spike radius formula from drawCenter:
			// depthDist = ((65 - diameter) * affinity * 0.0625 / 4 + diameter)
			double spikeRadius = ((65 - diameter) * stepAffinity * 0.0625 / 4 + diameter);
			// Inset slightly so rings sit on the interior of the spikes
			double ringRadius = diameter + (spikeRadius - diameter) * 0.85;
			double ringY = funnelY(baseY, ringRadius, diameter);

			for (int i = 0; i < segments; i++) {
				float angle1 = (360f / segments) * i;
				float angle2 = (360f / segments) * (i + 1);

				// Subtle wave undulation on the ring height — less pronounced than blood ring
				double wave1 = Math.sin(Math.toRadians(angle1 * 3) + currentTime * 0.1) * 0.03;
				double wave2 = Math.sin(Math.toRadians(angle2 * 3) + currentTime * 0.1) * 0.03;

				double x1 = cx + Math.cos(Math.toRadians(angle1)) * ringRadius;
				double z1 = cz + Math.sin(Math.toRadians(angle1)) * ringRadius;
				double x2 = cx + Math.cos(Math.toRadians(angle2)) * ringRadius;
				double z2 = cz + Math.sin(Math.toRadians(angle2)) * ringRadius;

				Vec3 from = new Vec3(x1, ringY + wave1, z1);
				Vec3 to = new Vec3(x2, ringY + wave2, z2);

				// Black ring — core only, thin, semi-transparent
				drawBeamSegment(stack, bufferIn, from, to, 0.0f, 0.0f, 0.0f, 0.2f, 0.025f);
			}
		}
	}

	// ========================== BLOOD VOLUME RING ==========================

	private void drawBloodVolumeRing(PoseStack stack, MultiBufferSource bufferIn, SomaticLoomBlockEntity te,
			Vec3 center, double diameter, double fillRatio, float currentTime, int combinedLightIn,
			int combinedOverlayIn) {
		double ringRadius = diameter * 0.6;
		double baseY = center.y + 2;
		double ringY = funnelY(baseY, ringRadius, diameter) + 0.75;
		double cx = center.x;
		double cz = center.z;
		float rotOffset = currentTime * 0.5f;

		int segments = 64;
		int filledSegments = (int) (segments * fillRatio);

		for (int i = 0; i < segments; i++) {
			float angle1 = rotOffset + (360f / segments) * i;
			float angle2 = rotOffset + (360f / segments) * (i + 1);

			// Gentle wave undulation on the ring height
			double wave1 = Math.sin(Math.toRadians(angle1 * 3) + currentTime * 0.1) * 0.06;
			double wave2 = Math.sin(Math.toRadians(angle2 * 3) + currentTime * 0.1) * 0.06;

			double x1 = cx + Math.cos(Math.toRadians(angle1)) * ringRadius;
			double z1 = cz + Math.sin(Math.toRadians(angle1)) * ringRadius;
			double x2 = cx + Math.cos(Math.toRadians(angle2)) * ringRadius;
			double z2 = cz + Math.sin(Math.toRadians(angle2)) * ringRadius;

			Vec3 from = new Vec3(x1, ringY + wave1, z1);
			Vec3 to = new Vec3(x2, ringY + wave2, z2);

			if (i < filledSegments) {
				// Filled — bright core + glow
				drawBeamSegment(stack, bufferIn, from, to, 0.8f, 0.08f, 0.08f, 1.0f, 0.04f);
				drawBeamSegmentGlow(stack, bufferIn, from, to, 0.9f, 0.15f, 0.1f, 0.5f, 0.09f);
			} else {
				// Empty — dim
				drawBeamSegment(stack, bufferIn, from, to, 0.12f, 0.02f, 0.02f, 0.6f, 0.025f);
			}
		}
	}

	// ========================== CRAFTING PROGRESS RING ==========================

	/**
	 * Draws a ring that fills as ritual crafting progresses. Positioned above the
	 * blood volume ring. When {@code pulsing} is true (awaiting attunement), the
	 * ring oscillates in alpha to prompt the player to interact.
	 */
	private void drawCraftingProgressRing(PoseStack stack, MultiBufferSource bufferIn,
			SomaticLoomBlockEntity te, Vec3 center, double diameter, double progressRatio,
			boolean pulsing, float currentTime, int combinedLightIn, int combinedOverlayIn) {
		double ringRadius = diameter * 0.45;
		double baseY = center.y + 2;
		double ringY = funnelY(baseY, ringRadius, diameter) + 1.1;
		double cx = center.x;
		double cz = center.z;
		float rotOffset = -currentTime * 0.8f; // rotate opposite to blood ring

		int segments = 64;
		int filledSegments = (int) (segments * progressRatio);

		// Pulsing alpha modulation for attunement phase
		float pulseAlpha = pulsing
				? 0.5f + 0.5f * Mth.sin(currentTime * 0.3f)
				: 1.0f;

		for (int i = 0; i < segments; i++) {
			float angle1 = rotOffset + (360f / segments) * i;
			float angle2 = rotOffset + (360f / segments) * (i + 1);

			double wave1 = Math.sin(Math.toRadians(angle1 * 4) + currentTime * 0.15) * 0.04;
			double wave2 = Math.sin(Math.toRadians(angle2 * 4) + currentTime * 0.15) * 0.04;

			double x1 = cx + Math.cos(Math.toRadians(angle1)) * ringRadius;
			double z1 = cz + Math.sin(Math.toRadians(angle1)) * ringRadius;
			double x2 = cx + Math.cos(Math.toRadians(angle2)) * ringRadius;
			double z2 = cz + Math.sin(Math.toRadians(angle2)) * ringRadius;

			Vec3 from = new Vec3(x1, ringY + wave1, z1);
			Vec3 to = new Vec3(x2, ringY + wave2, z2);

			if (i < filledSegments) {
				// Filled — purple core + glow
				drawBeamSegment(stack, bufferIn, from, to,
						0.55f, 0.1f, 0.7f, pulseAlpha, 0.04f);
				drawBeamSegmentGlow(stack, bufferIn, from, to,
						0.7f, 0.2f, 0.9f, 0.4f * pulseAlpha, 0.09f);
			} else {
				// Empty — dim
				drawBeamSegment(stack, bufferIn, from, to,
						0.15f, 0.03f, 0.18f, 0.4f, 0.025f);
			}
		}
	}

	// ========================== LOW-LEVEL BEAM DRAWING ==========================

	/**
	 * Fractal lightning line with core render type.
	 */
	private void fracLine(PoseStack matrix, MultiBufferSource buffer, Vec3 from, Vec3 to, ParticleColor color,
			double displace, double detail, float thickness) {
		if (displace < detail) {
			drawBeamSegment(matrix, buffer, from, to,
					color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
					1.0f, thickness);
		} else {
			double offset = 0.25;
			Vec3 mid = new Vec3(
					(to.x + from.x) / 2 + (FRAC_RAND.nextFloat() - offset) * displace * offset,
					(to.y + from.y) / 2 + (FRAC_RAND.nextFloat() - offset) * displace * offset,
					(to.z + from.z) / 2 + (FRAC_RAND.nextFloat() - offset) * displace * offset);
			fracLine(matrix, buffer, from, mid, color, displace / 2, detail, thickness);
			fracLine(matrix, buffer, to, mid, color, displace / 2, detail, thickness);
		}
	}

	/**
	 * Fractal lightning line with additive glow render type.
	 */
	private void fracLineGlow(PoseStack matrix, MultiBufferSource buffer, Vec3 from, Vec3 to, ParticleColor color,
			double displace, double detail, float thickness, float alpha) {
		if (displace < detail) {
			drawBeamSegmentGlow(matrix, buffer, from, to,
					color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
					alpha, thickness);
		} else {
			double offset = 0.25;
			Vec3 mid = new Vec3(
					(to.x + from.x) / 2 + (FRAC_RAND.nextFloat() - offset) * displace * offset,
					(to.y + from.y) / 2 + (FRAC_RAND.nextFloat() - offset) * displace * offset,
					(to.z + from.z) / 2 + (FRAC_RAND.nextFloat() - offset) * displace * offset);
			fracLineGlow(matrix, buffer, from, mid, color, displace / 2, detail, thickness, alpha);
			fracLineGlow(matrix, buffer, to, mid, color, displace / 2, detail, thickness, alpha);
		}
	}

	/**
	 * Draws a single beam segment using the core laser render type.
	 */
	private static void drawBeamSegment(PoseStack matrixStackIn, MultiBufferSource buffer, Vec3 from, Vec3 to,
			float r, float g, float b, float alpha, float thickness) {
		final Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);

		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.LOOM_BEAM_CORE);
		matrixStackIn.pushPose();
		matrixStackIn.translate(to.x, to.y, to.z);

		float diffX = (float) (from.x - to.x);
		float diffY = (float) (from.y - to.y);
		float diffZ = (float) (from.z - to.z);

		Vector3f startLaser = new Vector3f(0, 0, 0);
		Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
		Vector3f sortPos = new Vector3f((float) to.x, (float) to.y, (float) to.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();

		// Dark backing for contrast
		drawLaser(builder, positionMatrix, endLaser, startLaser, 0, 0, 0, alpha * 0.6f, thickness * 1.5f,
				v, v + diffY * -5.5, new Vector3f(sortPos.x, sortPos.y - 0.05f, sortPos.z));
		// Core beam
		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness,
				v, v + diffY * -5.5, sortPos);

		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}

	/**
	 * Draws a single beam segment using the additive glow render type for a bloom effect.
	 */
	private static void drawBeamSegmentGlow(PoseStack matrixStackIn, MultiBufferSource buffer, Vec3 from, Vec3 to,
			float r, float g, float b, float alpha, float thickness) {
		final Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);

		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.LOOM_BEAM_GLOW);
		matrixStackIn.pushPose();
		matrixStackIn.translate(to.x, to.y, to.z);

		float diffX = (float) (from.x - to.x);
		float diffY = (float) (from.y - to.y);
		float diffZ = (float) (from.z - to.z);

		Vector3f startLaser = new Vector3f(0, 0, 0);
		Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
		Vector3f sortPos = new Vector3f((float) to.x, (float) to.y, (float) to.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();

		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness,
				v, v + diffY * -5.5, sortPos);

		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}

	/**
	 * Renders two perpendicular cross-quads between two points so the beam is
	 * visible from every viewing angle (including top-down).
	 */
	private static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to,
			float r, float g, float b, float alpha, float thickness, double v1, double v2, Vector3f sortPos) {
		Player player = Minecraft.getInstance().player;
		if (player == null) return;

		// Beam direction
		Vector3f SE = new Vector3f(to);
		SE.sub(from);
		float seLen = SE.length();
		if (seLen < 1e-6f) return; // zero-length segment

		// First perpendicular: camera-facing billboard
		Vector3f P = new Vector3f((float) player.getX() - sortPos.x(), (float) player.getEyeY() - sortPos.y(),
				(float) player.getZ() - sortPos.z());
		Vector3f PS = new Vector3f(from);
		PS.sub(P);
		Vector3f adjustedVec = new Vector3f(PS);
		adjustedVec.cross(SE);
		float len = adjustedVec.length();
		if (len < 1e-6f) {
			adjustedVec.set(0, 1, 0);
			adjustedVec.cross(SE);
			len = adjustedVec.length();
			if (len < 1e-6f) {
				adjustedVec.set(1, 0, 0);
				adjustedVec.cross(SE);
				len = adjustedVec.length();
				if (len < 1e-6f) return;
			}
		}
		adjustedVec.mul(thickness / len);

		// Second perpendicular: 90° rotated around beam axis from the first
		Vector3f perp2 = new Vector3f(adjustedVec);
		Vector3f seNorm = new Vector3f(SE);
		seNorm.normalize();
		perp2.cross(seNorm);
		float len2 = perp2.length();
		if (len2 > 1e-6f) {
			perp2.mul(thickness / len2);
		} else {
			perp2.set(adjustedVec); // fallback — shouldn't happen
		}

		// Draw first quad (billboard-facing)
		drawQuad(builder, positionMatrix, from, to, adjustedVec, r, g, b, alpha, v1, v2);
		// Draw second quad (perpendicular to the first — ensures top-down visibility)
		drawQuad(builder, positionMatrix, from, to, perp2, r, g, b, alpha, v1, v2);
	}

	/**
	 * Emits a single quad for a beam segment given the offset vector from the beam axis.
	 */
	private static void drawQuad(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to,
			Vector3f offset, float r, float g, float b, float alpha, double v1, double v2) {
		Vector3f p1 = new Vector3f(from).add(offset);
		Vector3f p2 = new Vector3f(from).sub(offset);
		Vector3f p3 = new Vector3f(to).add(offset);
		Vector3f p4 = new Vector3f(to).sub(offset);

		builder.vertex(positionMatrix, p1.x(), p1.y(), p1.z()).color(r, g, b, alpha).uv(1, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p3.x(), p3.y(), p3.z()).color(r, g, b, alpha).uv(1, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p4.x(), p4.y(), p4.z()).color(r, g, b, alpha).uv(0, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p2.x(), p2.y(), p2.z()).color(r, g, b, alpha).uv(0, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
	}

}

