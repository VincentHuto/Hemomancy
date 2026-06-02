package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.MemoryWeavingRecipeSerializer;
import com.vincenthuto.hemomancy.common.tile.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Random;

/**
 * Renders the Somatic Loom's visual effects: fractal tendency star, enzyme
 * indicator rings, blood volume ring, and crafting progress ring.
 * <p>
 * Uses flat POSITION_COLOR quads with proper depth testing (matching
 * {@link com.vincenthuto.hemomancy.client.render.world.CardinalRiteBoundaryRenderer}
 * and {@link com.vincenthuto.hemomancy.client.render.world.BloodCraftRingRenderer}),
 * fixing the z-sorting issues of the old textured billboard approach.
 */
public class SomaticLoomRenderer implements BlockEntityRenderer<SomaticLoomBlockEntity> {

	/** Fractal random â€” reseeded each tick for stable-per-tick crackle. */
	private static final Random FRAC_RAND = new Random();

	// â”€â”€ Star geometry (block-local coordinates) â”€â”€
	private static final float CX = 0.5f;
	private static final float CZ = 0.5f;
	private static final float STAR_Y = 1.5f;
	private static final float BASE_RADIUS = 0.5f;
	private static final float SPIKE_BASE_HALF_ANGLE = 22.75f;
	/** How far each unit of affinity extends a spoke beyond BASE_RADIUS. */
	private static final float AFFINITY_RADIUS_SCALE = 0.5f;

	// â”€â”€ Fractal spoke widths & alphas â”€â”€
	private static final float SPOKE_CORE_WIDTH = 0.004f;
	private static final float SPOKE_GLOW_WIDTH = 0.012f;
	private static final float SPOKE_CORE_ALPHA = 0.85f;
	private static final float SPOKE_GLOW_ALPHA = 0.25f;
	/** Fractal recursion terminates when displacement drops below this. */
	private static final double FRACTAL_DETAIL = 0.02;

	// â”€â”€ Ring segment count â”€â”€
	private static final int RING_SEGMENTS = 64;
	/** Segment count for the thinner enzyme indicator rings. */
	private static final int ENZYME_RING_SEGMENTS = 48;
	private static final int ORB_LAT_SEGMENTS = 8;
	private static final int ORB_LON_SEGMENTS = 16;
	private static final double ORB_RENDER_BOUNDS = 10.0D;
	private static final float ORB_THREAD_CENTER_Y = 0.58f;
	private static final int ORB_UNRAVEL_STRANDS = 9;
	private static final float ORB_CENTER_STRAND_WIDTH = 0.012f;
	private static final float ORB_UNRAVEL_STRAND_WIDTH = 0.01f;
	private static final float ORB_UNRAVEL_MIN_LENGTH = 0.32f;
	private static final float ORB_UNRAVEL_MAX_LENGTH = 0.78f;

	// â”€â”€ Ring widths (used by enzyme, blood, crafting rings) â”€â”€
	private static final float RING_CORE_WIDTH = 0.06f;
	private static final float RING_GLOW_WIDTH = 0.15f;

	// â”€â”€ Undulation for rings â”€â”€
	private static final double UNDULATE_FREQ = 5.0;
	private static final float UNDULATE_AMP = 0.04f;
	private static final double UNDULATE_SPEED = 0.06;
	private static final double UNDULATE_FREQ2 = 11.0;
	private static final float UNDULATE_AMP2 = 0.015f;
	private static final double UNDULATE_SPEED2 = 0.1;

	public SomaticLoomRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public boolean shouldRenderOffScreen(SomaticLoomBlockEntity te) {
		return true;
	}

	@Override
	public AABB getRenderBoundingBox(SomaticLoomBlockEntity te) {
		return new AABB(te.getBlockPos()).inflate(ORB_RENDER_BOUNDS);
	}


	@Override
	public void render(SomaticLoomBlockEntity te, float partialTicks, PoseStack stack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		float currentTime = te.getLevel().getGameTime() + partialTicks;
		boolean showEffects = !te.contents.get(0).isEmpty() || te.isCrafting();

		// === Items (always rendered, depth writes first) ===
		stack.pushPose();
		renderItems(te, partialTicks, stack, buffer, combinedLight, combinedOverlay);
		stack.popPose();

		// === Flat-color effects ===
		if (showEffects) {
			// Seed fractal random per-tick so the lightning crackles each tick
			FRAC_RAND.setSeed(te.getLevel().getGameTime() * 31L + te.getBlockPos().hashCode());

			stack.pushPose();
			Matrix4f mat = stack.last().pose();
			VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);

			// Fractal tendency star
			stack.translate(0F, -0.5F, 0F);

			drawFractalStar(vc, mat, te, currentTime);

			// Enzyme indicator rings
			drawEnzymeRings(vc, mat, te, currentTime);

			if (te.isAwaitingBlood()) {
				drawAwaitingBloodGlow(vc, mat, te, currentTime);
			}

			// Blood volume ring
			double bloodVol = te.getBloodVolume();
			double maxBloodVol = te.getMaxBloodVolume();
			if (maxBloodVol > 0) {
				drawBloodVolumeRing(vc, mat,
						Mth.clamp(bloodVol / maxBloodVol, 0, 1), currentTime);
			}

			// Crafting progress ring
			if (te.isCrafting() && te.getCraftingTotalTime() > 0) {
				double progress = 1.0 - ((double) te.getCraftingProgress() / te.getCraftingTotalTime());
				drawCraftingProgressRing(vc, mat, progress,
						te.getCraftingPhase() == 2, currentTime);
			}

			if (te.isWeavingOrbs()) {
				stack.pushPose();
				stack.translate(0F, 0.5F, 0F);
				drawRitualOrbs(buffer, stack.last().pose(), te, currentTime, partialTicks);
				stack.popPose();
			}

			stack.popPose();
		}
	}

	//  Item rendering (unchanged)

	public void renderItems(SomaticLoomBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		float gameTime = te.getLevel().getGameTime() + partialTicks;
		Minecraft mc = Minecraft.getInstance();

		// Slot 0 â€” first item, floating above the center of the block
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

		// Slot 1 â€” second item, floating above the first
		for (int i = 1; i < te.contents.size(); i++) {
			ItemStack stack1 = te.contents.get(i);
			if (stack1.isEmpty()) continue;
			matrixStackIn.pushPose();
			double angle = gameTime * 0.04D + (i - 1) * Math.PI * 2.0D / Math.max(1, te.contents.size() - 1);
			double radius = 0.32D + Math.min(0.18D, (te.contents.size() - 2) * 0.025D);
			matrixStackIn.translate(0.5F + Math.cos(angle) * radius, 1.72F, 0.5F + Math.sin(angle) * radius);
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(-gameTime * 0.75f + i * 25f).toMoj());
			matrixStackIn.scale(0.42f, 0.42f, 0.42f);
			mc.getItemRenderer().renderStatic(null, stack1, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn,
					null, combinedLightIn, combinedOverlayIn, 0);
			matrixStackIn.popPose();
		}

		// Recipe result preview â€” floating above both items
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

	//  Fractal Tendency Star

	private void drawFractalStar(VertexConsumer vc, Matrix4f mat,
			SomaticLoomBlockEntity te, float currentTime) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();
		float angle = -90f;

		// Global breathing pulse (0..1)
		double pulse = (Math.sin(currentTime * 0.08) + 1.0) * 0.5;

		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float affinity = Math.min(3.0f, affs.getOrDefault(tend, 0f) / 16.0f);
			ParticleColor color = tend.getColor();
			float r = color.getRed() / 255f;
			float g = color.getGreen() / 255f;
			float b = color.getBlue() / 255f;

			// Pulse modulation
			float cAlpha = (float) (SPOKE_CORE_ALPHA * (0.7 + 0.3 * pulse));
			float gAlpha = (float) (SPOKE_GLOW_ALPHA * (0.5 + 0.5 * pulse));

			// Spoke base corners (two points at BASE_RADIUS)
			double a1Rad = Math.toRadians(angle + SPIKE_BASE_HALF_ANGLE);
			double a2Rad = Math.toRadians(angle - SPIKE_BASE_HALF_ANGLE);
			float bx1 = CX + (float) (Math.cos(a1Rad) * BASE_RADIUS);
			float bz1 = CZ + (float) (Math.sin(a1Rad) * BASE_RADIUS);
			float bx2 = CX + (float) (Math.cos(a2Rad) * BASE_RADIUS);
			float bz2 = CZ + (float) (Math.sin(a2Rad) * BASE_RADIUS);

			// Spoke tip
			double tipRad = Math.toRadians(angle);
			float tipDist = BASE_RADIUS + affinity * AFFINITY_RADIUS_SCALE;
			float tx = CX + (float) (Math.cos(tipRad) * tipDist);
			float tz = CZ + (float) (Math.sin(tipRad) * tipDist);

			// Vertical undulation per spoke vertex based on angular position
			float yBase1 = STAR_Y + yUndulation(a1Rad, currentTime, 1f);
			float yBase2 = STAR_Y + yUndulation(a2Rad, currentTime, 1f);
			float yTip   = STAR_Y + yUndulation(tipRad, currentTime, 1f);

			// Fractal displacement magnitude (based on base width)
			float displace = (float) Math.sqrt(
					(bx1 - bx2) * (bx1 - bx2) + (bz1 - bz2) * (bz1 - bz2)) * 0.5f;

			// Primary fractal spokes (tip â†” base corners)
			fracLine(vc, mat, tx, yTip, tz, bx1, yBase1, bz1,
					r, g, b, cAlpha, gAlpha, SPOKE_CORE_WIDTH, SPOKE_GLOW_WIDTH,
					displace, FRACTAL_DETAIL);
			fracLine(vc, mat, tx, yTip, tz, bx2, yBase2, bz2,
					r, g, b, cAlpha, gAlpha, SPOKE_CORE_WIDTH, SPOKE_GLOW_WIDTH,
					displace, FRACTAL_DETAIL);

			// Secondary reverse spokes (dimmer, thinner)
			fracLine(vc, mat, bx1, yBase1, bz1, tx, yTip, tz,
					r, g, b, cAlpha * 0.6f, gAlpha * 0.5f,
					SPOKE_CORE_WIDTH * 0.7f, SPOKE_GLOW_WIDTH * 0.6f,
					displace * 0.8, FRACTAL_DETAIL);
			fracLine(vc, mat, bx2, yBase2, bz2, tx, yTip, tz,
					r, g, b, cAlpha * 0.6f, gAlpha * 0.5f,
					SPOKE_CORE_WIDTH * 0.7f, SPOKE_GLOW_WIDTH * 0.6f,
					displace * 0.8, FRACTAL_DETAIL);

			angle += 45f;
		}
	}

	//  Enzyme Indicator Rings
	/**
	 * Draws concentric dark rings at the fractal star's Y-level. One ring per
	 * 0.2f enzyme dose reached by any tendency. Radii match spike tip positions.
	 */
	private void drawEnzymeRings(VertexConsumer vc, Matrix4f mat,
			SomaticLoomBlockEntity te, float currentTime) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();

		float maxAff = 0f;
		for (Float v : affs.values()) {
			if (v != null && v > maxAff) maxAff = v;
		}
		int steps = Math.min((int) Math.ceil(maxAff / 8.0f), 8);
		if (steps <= 0) return;

		for (int s = 1; s <= steps; s++) {
			float stepAff = s * 0.2f;
			float radius = BASE_RADIUS + stepAff * AFFINITY_RADIUS_SCALE * 0.85f;
			// Offset the direction per step so concentric rings undulate out of phase
			float dir = (s % 2 == 0) ? 1.0f : -1.0f;
			drawUndulatingRing(vc, mat, CX, STAR_Y, CZ, radius,
					0f, 0f, 0f, 0.2f, 0.06f,
					0.025f, 0.06f,
					currentTime, dir, ENZYME_RING_SEGMENTS);
		}
	}

	//  Blood Volume Ring

	private void drawBloodVolumeRing(VertexConsumer vc, Matrix4f mat,
			double fillRatio, float currentTime) {
		float radius = 0.7f;
		float ringY = STAR_Y + 0.3f;
		float rotOff = currentTime * 0.5f;
		int filled = (int) (RING_SEGMENTS * fillRatio);

		// Breathing pulse
		double pulse = (Math.sin(currentTime * 0.12) + 1.0) * 0.5;

		for (int i = 0; i < RING_SEGMENTS; i++) {
			float a1 = rotOff + (360f / RING_SEGMENTS) * i;
			float a2 = rotOff + (360f / RING_SEGMENTS) * (i + 1);

			float wave1 = yUndulation(Math.toRadians(a1), currentTime, 1f);
			float wave2 = yUndulation(Math.toRadians(a2), currentTime, 1f);

			float r1 = radius + undulation(Math.toRadians(a1), currentTime, 1f) * 0.5f;
			float r2 = radius + undulation(Math.toRadians(a2), currentTime, 1f) * 0.5f;

			if (i < filled) {
				float coreA = (float) (0.70 + 0.30 * pulse);
				float glowA = (float) (0.20 + 0.15 * pulse);
				emitRingArc(vc, mat, CX, ringY, CZ,
						a1, a2, r1, r2, wave1, wave2,
						0.8f, 0.08f, 0.08f, coreA, glowA,
						0.04f, 0.10f);
			} else {
				emitRingArc(vc, mat, CX, ringY, CZ,
						a1, a2, r1, r2, wave1, wave2,
						0.12f, 0.02f, 0.02f, 0.5f, 0.12f,
						0.025f, 0.05f);
			}
		}
	}

	//  Crafting Progress Ring

	private void drawCraftingProgressRing(VertexConsumer vc, Matrix4f mat,
			double progressRatio, boolean pulsing, float currentTime) {
		float radius = 0.55f;
		float ringY = STAR_Y + 0.5f;
		float rotOff = -currentTime * 0.8f;

		int filled = (int) (RING_SEGMENTS * progressRatio);
		float pulseAlpha = pulsing
				? 0.5f + 0.5f * Mth.sin(currentTime * 0.3f)
				: 1.0f;

		for (int i = 0; i < RING_SEGMENTS; i++) {
			float a1 = rotOff + (360f / RING_SEGMENTS) * i;
			float a2 = rotOff + (360f / RING_SEGMENTS) * (i + 1);

			float wave1 = yUndulation(Math.toRadians(a1), currentTime, -1f);
			float wave2 = yUndulation(Math.toRadians(a2), currentTime, -1f);

			float r1 = radius + undulation(Math.toRadians(a1), currentTime, -1f) * 0.4f;
			float r2 = radius + undulation(Math.toRadians(a2), currentTime, -1f) * 0.4f;

			if (i < filled) {
				emitRingArc(vc, mat, CX, ringY, CZ,
						a1, a2, r1, r2, wave1, wave2,
						0.55f, 0.1f, 0.7f, pulseAlpha * 0.85f, pulseAlpha * 0.25f,
						0.04f, 0.09f);
			} else {
				emitRingArc(vc, mat, CX, ringY, CZ,
						a1, a2, r1, r2, wave1, wave2,
						0.15f, 0.03f, 0.18f, 0.35f, 0.08f,
						0.025f, 0.05f);
			}
		}
	}

	//  Fractal Line (recursive midpoint displacement)

	/**
	 * Recursively subdivides a line with random midpoint displacement to
	 * produce a jagged fractal lightning effect, then draws each leaf segment
	 * as a flat quad strip (core + glow) in the XZ plane.
	 */
	private void drawAwaitingBloodGlow(VertexConsumer vc, Matrix4f mat,
			SomaticLoomBlockEntity te, float currentTime) {
		double required = te.getRitualBloodRequired();
		double charged = required <= 0.0D ? 0.0D : Mth.clamp(te.getRitualBloodCharged() / required, 0.0D, 1.0D);
		float pulse = 0.55f + 0.35f * Mth.sin(currentTime * 0.18f);
		float radius = 0.9f + (float) charged * 0.22f;
		drawUndulatingRing(vc, mat, CX, STAR_Y + 0.65f, CZ, radius,
				0.36f, 0.0f, 0.0f, 0.42f * pulse, 0.18f * pulse,
				0.05f, 0.16f, currentTime, -1f, RING_SEGMENTS);
	}

	private void drawRitualOrbs(MultiBufferSource buffer, Matrix4f mat,
			SomaticLoomBlockEntity te, float currentTime, float partialTicks) {
		drawRitualOrbEffects(buffer, mat, te, currentTime, partialTicks);
		drawRitualOrbShells(buffer, mat, te, currentTime, partialTicks);
	}

	private void drawRitualOrbEffects(MultiBufferSource buffer, Matrix4f mat,
			SomaticLoomBlockEntity te, float currentTime, float partialTicks) {
		VertexConsumer effectVc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);
		for (SomaticLoomBlockEntity.RitualOrb orb : te.getRitualOrbs()) {
			if (orb.completed()) continue;
			ParticleColor color = orb.tendency().getColor();
			float r = color.getRed() / 255f;
			float g = color.getGreen() / 255f;
			float b = color.getBlue() / 255f;
			Vec3 offset = orb.renderOffset(partialTicks);
			float x = 0.5f + (float) offset.x;
			float y = 0.5f + (float) offset.y;
			float z = 0.5f + (float) offset.z;
			drawOrbCenterStrand(effectVc, mat, x, y, z, currentTime, r, g, b);
			drawOrbTrail(effectVc, mat, orb, offset, partialTicks, r, g, b);
			drawOrbUnraveledStrands(effectVc, mat, orb, x, y, z, currentTime, r, g, b);
		}
	}

	private void drawRitualOrbShells(MultiBufferSource buffer, Matrix4f mat,
			SomaticLoomBlockEntity te, float currentTime, float partialTicks) {
		for (SomaticLoomBlockEntity.RitualOrb orb : te.getRitualOrbs()) {
			if (orb.completed()) continue;
			ParticleColor color = orb.tendency().getColor();
			float r = color.getRed() / 255f;
			float g = color.getGreen() / 255f;
			float b = color.getBlue() / 255f;
			Vec3 offset = orb.renderOffset(partialTicks);
			float x = 0.5f + (float) offset.x;
			float y = 0.5f + (float) offset.y;
			float z = 0.5f + (float) offset.z;
			float pulse = 0.75f + 0.25f * Mth.sin(currentTime * 0.22f + orb.enzymeCost());
			float size = (0.18f + orb.enzymeCost() * 0.025f) * pulse;
			drawShaderWrithedOrbShell(buffer, mat, orb, x, y, z, size, currentTime, r, g, b, false);
			drawShaderWrithedOrbShell(buffer, mat, orb, x, y, z, size, currentTime, r, g, b, true);
		}
	}

	private void drawShaderWrithedOrbShell(MultiBufferSource buffer, Matrix4f mat,
			SomaticLoomBlockEntity.RitualOrb orb, float x, float y, float z, float size,
			float currentTime, float r, float g, float b, boolean glow) {
		float layerRadius = glow ? size * 1.45f : size;
		Vector3f center = mat.transformPosition(x, y, z, new Vector3f());
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.loomOrbShell(currentTime, orbShaderSeed(orb),
				center.x(), center.y(), center.z(), layerRadius,
				glow ? 0.08f : 0.18f, glow ? 7.0f : 11.0f, glow));
		drawOrbSphere(vc, mat, x, y, z, size, r, g, b, 0.82f, glow);
	}

	private static float orbShaderSeed(SomaticLoomBlockEntity.RitualOrb orb) {
		Vec3 start = orb.startOffset();
		return (float) (orb.tendency().ordinal() * 31.0D
				+ orb.enzymeCost() * 7.0D
				+ start.x * 3.17D
				+ start.y * 5.13D
				+ start.z * 11.71D);
	}

	private void drawOrbCenterStrand(VertexConsumer vc, Matrix4f mat,
			float x, float y, float z, float currentTime, float r, float g, float b) {
		float pulse = 0.65f + 0.25f * Mth.sin(currentTime * 0.17f + x * 2.0f + z * 2.0f);
		fracLine(vc, mat, CX, ORB_THREAD_CENTER_Y, CZ, x, y, z,
				r, g, b, 0.16f * pulse, 0.08f * pulse,
				ORB_CENTER_STRAND_WIDTH, 0.045f, 0.12D, FRACTAL_DETAIL);
	}

	private void drawOrbTrail(VertexConsumer vc, Matrix4f mat, SomaticLoomBlockEntity.RitualOrb orb,
			Vec3 renderOffset, float partialTicks, float r, float g, float b) {
		Vec3 previous = orb.previousOffset();
		if (previous.distanceToSqr(renderOffset) < 0.001D) {
			return;
		}
		float fade = Mth.clamp(1.0f - partialTicks * 0.35f, 0.35f, 1.0f);
		drawFlatLine(vc, mat,
				0.5f + (float) previous.x, 0.5f + (float) previous.y, 0.5f + (float) previous.z,
				0.5f + (float) renderOffset.x, 0.5f + (float) renderOffset.y, 0.5f + (float) renderOffset.z,
				r, g, b, 0.12f * fade, 0.06f * fade, 0.035f, 0.18f);
	}

	private void drawOrbUnraveledStrands(VertexConsumer vc, Matrix4f mat,
			SomaticLoomBlockEntity.RitualOrb orb, float x, float y, float z,
			float currentTime, float r, float g, float b) {
		long seed = 97L * orb.tendency().ordinal()
				+ 131L * orb.enzymeCost()
				+ Double.doubleToLongBits(orb.startOffset().x * 3.17D)
				+ Double.doubleToLongBits(orb.startOffset().y * 5.13D)
				+ Double.doubleToLongBits(orb.startOffset().z * 11.71D);
		FRAC_RAND.setSeed(seed);
		for (int i = 0; i < ORB_UNRAVEL_STRANDS; i++) {
			double yaw = FRAC_RAND.nextDouble() * Math.PI * 2.0D;
			float phase = FRAC_RAND.nextFloat() * Mth.TWO_PI;
			float strandWave = Mth.sin(currentTime * 0.055f + phase);
			float strandCurl = Mth.cos(currentTime * 0.041f + phase * 1.31f);
			double vertical = (FRAC_RAND.nextDouble() - 0.42D) * 0.8D + strandCurl * 0.16D;
			double animatedYaw = yaw + strandWave * 0.22D;
			float length = ORB_UNRAVEL_MIN_LENGTH
					+ FRAC_RAND.nextFloat() * (ORB_UNRAVEL_MAX_LENGTH - ORB_UNRAVEL_MIN_LENGTH);
			float rootRadius = 0.12f + FRAC_RAND.nextFloat() * 0.12f + strandWave * 0.018f;
			float sx = x + (float) Math.cos(animatedYaw) * rootRadius;
			float sy = y + (FRAC_RAND.nextFloat() - 0.5f) * 0.22f + strandCurl * 0.018f;
			float sz = z + (float) Math.sin(animatedYaw) * rootRadius;
			float ex = sx + (float) Math.cos(animatedYaw) * length;
			float ey = sy + (float) vertical * length;
			float ez = sz + (float) Math.sin(animatedYaw) * length;
			float alpha = 0.09f + FRAC_RAND.nextFloat() * 0.07f;
			fracLine(vc, mat, sx, sy, sz, ex, ey, ez,
					r, g, b, alpha, alpha * 0.45f,
					ORB_UNRAVEL_STRAND_WIDTH, 0.055f, 0.08D, FRACTAL_DETAIL);
		}
	}

	private static void drawOrbSphere(VertexConsumer vc, Matrix4f mat,
			float x, float y, float z, float radius, float r, float g, float b, float alpha, boolean glow) {
		float layerRadius = glow ? radius * 1.45f : radius;
		float layerAlpha = glow ? alpha * 0.18f : alpha;
		drawSphere(vc, mat, x, y, z, layerRadius, r, g, b, layerAlpha);
	}

	private static void drawSphere(VertexConsumer vc, Matrix4f mat,
			float cx, float cy, float cz, float radius, float r, float g, float b, float alpha) {
		for (int lat = 0; lat < ORB_LAT_SEGMENTS; lat++) {
			double theta1 = Math.PI * lat / ORB_LAT_SEGMENTS;
			double theta2 = Math.PI * (lat + 1) / ORB_LAT_SEGMENTS;
			for (int lon = 0; lon < ORB_LON_SEGMENTS; lon++) {
				double phi1 = Math.PI * 2.0D * lon / ORB_LON_SEGMENTS;
				double phi2 = Math.PI * 2.0D * (lon + 1) / ORB_LON_SEGMENTS;
				emitSphereQuad(vc, mat, cx, cy, cz, radius, theta1, phi1, theta2, phi2, r, g, b, alpha);
			}
		}
	}

	private static void emitSphereQuad(VertexConsumer vc, Matrix4f mat,
			float cx, float cy, float cz, float radius,
			double theta1, double phi1, double theta2, double phi2,
			float r, float g, float b, float alpha) {
		float x1 = sphereX(cx, radius, theta1, phi1);
		float y1 = sphereY(cy, radius, theta1);
		float z1 = sphereZ(cz, radius, theta1, phi1);
		float x2 = sphereX(cx, radius, theta2, phi1);
		float y2 = sphereY(cy, radius, theta2);
		float z2 = sphereZ(cz, radius, theta2, phi1);
		float x3 = sphereX(cx, radius, theta2, phi2);
		float y3 = sphereY(cy, radius, theta2);
		float z3 = sphereZ(cz, radius, theta2, phi2);
		float x4 = sphereX(cx, radius, theta1, phi2);
		float y4 = sphereY(cy, radius, theta1);
		float z4 = sphereZ(cz, radius, theta1, phi2);
		emitQuad(vc, mat,
				x1, y1, z1, r, g, b, alpha,
				x2, y2, z2, r, g, b, alpha,
				x3, y3, z3, r, g, b, alpha,
				x4, y4, z4, r, g, b, alpha);
	}

	private static float sphereX(float cx, float radius, double theta, double phi) {
		return cx + radius * (float) (Math.sin(theta) * Math.cos(phi));
	}

	private static float sphereY(float cy, float radius, double theta) {
		return cy + radius * (float) Math.cos(theta);
	}

	private static float sphereZ(float cz, float radius, double theta, double phi) {
		return cz + radius * (float) (Math.sin(theta) * Math.sin(phi));
	}

	private void fracLine(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1, float x2, float y2, float z2,
			float r, float g, float b, float coreAlpha, float glowAlpha,
			float coreWidth, float glowWidth, double displace, double detail) {
		if (displace < detail) {
			drawFlatLine(vc, mat, x1, y1, z1, x2, y2, z2,
					r, g, b, coreAlpha, glowAlpha, coreWidth, glowWidth);
		} else {
			// Center bias for random displacement (shifts distribution away from midpoint)
			float centerBias = 0.25f;
			// Displace midpoint in XZ only (keep Y stable to stay flat)
			float mx = (x1 + x2) * 0.5f + (FRAC_RAND.nextFloat() - centerBias) * (float) displace * centerBias;
			float my = (y1 + y2) * 0.5f;
			float mz = (z1 + z2) * 0.5f + (FRAC_RAND.nextFloat() - centerBias) * (float) displace * centerBias;
			fracLine(vc, mat, x1, y1, z1, mx, my, mz,
					r, g, b, coreAlpha, glowAlpha, coreWidth, glowWidth, displace / 2, detail);
			fracLine(vc, mat, mx, my, mz, x2, y2, z2,
					r, g, b, coreAlpha, glowAlpha, coreWidth, glowWidth, displace / 2, detail);
		}
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  Low-level drawing helpers
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/**
	 * Draws a single flat line segment as three quads (inner glow + core + outer glow)
	 * perpendicular to the line direction in the XZ plane.
	 * Matches the CardinalRite ring segment approach but for straight lines.
	 */
	private static void drawFlatLine(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1, float x2, float y2, float z2,
			float r, float g, float b, float coreAlpha, float glowAlpha,
			float coreWidth, float glowWidth) {
		float dx = x2 - x1;
		float dz = z2 - z1;
		float len = (float) Math.sqrt(dx * dx + dz * dz);
		if (len < 1e-6f) return;

		// Perpendicular direction in XZ plane
		float px = -dz / len;
		float pz = dx / len;

		float cHalf = coreWidth * 0.5f;
		float gOuter = cHalf + glowWidth;

		// Inner glow (transparent edge â†’ opaque at core boundary)
		emitQuad(vc, mat,
				x1 - px * gOuter, y1, z1 - pz * gOuter, r, g, b, 0f,
				x1 - px * cHalf, y1, z1 - pz * cHalf, r, g, b, glowAlpha,
				x2 - px * cHalf, y2, z2 - pz * cHalf, r, g, b, glowAlpha,
				x2 - px * gOuter, y2, z2 - pz * gOuter, r, g, b, 0f);

		// Core (solid)
		emitQuad(vc, mat,
				x1 - px * cHalf, y1, z1 - pz * cHalf, r, g, b, coreAlpha,
				x1 + px * cHalf, y1, z1 + pz * cHalf, r, g, b, coreAlpha,
				x2 + px * cHalf, y2, z2 + pz * cHalf, r, g, b, coreAlpha,
				x2 - px * cHalf, y2, z2 - pz * cHalf, r, g, b, coreAlpha);

		// Outer glow (opaque at core boundary â†’ transparent edge)
		emitQuad(vc, mat,
				x1 + px * cHalf, y1, z1 + pz * cHalf, r, g, b, glowAlpha,
				x1 + px * gOuter, y1, z1 + pz * gOuter, r, g, b, 0f,
				x2 + px * gOuter, y2, z2 + pz * gOuter, r, g, b, 0f,
				x2 + px * cHalf, y2, z2 + pz * cHalf, r, g, b, glowAlpha);
	}

	/**
	 * Draws a full undulating ring (all segments) with uniform color.
	 * Used for enzyme indicator rings.
	 */
	private void drawUndulatingRing(VertexConsumer vc, Matrix4f mat,
			float cx, float y, float cz, float baseRadius,
			float r, float g, float b, float coreAlpha, float glowAlpha,
			float coreW, float glowW,
			float currentTime, float dir, int segments) {
		for (int i = 0; i < segments; i++) {
			float a1 = (360f / segments) * i;
			float a2 = (360f / segments) * (i + 1);

			float rUnd1 = undulation(Math.toRadians(a1), currentTime, dir);
			float rUnd2 = undulation(Math.toRadians(a2), currentTime, dir);

			float yW1 = yUndulation(Math.toRadians(a1), currentTime, dir);
			float yW2 = yUndulation(Math.toRadians(a2), currentTime, dir);

			emitRingArc(vc, mat, cx, y, cz,
					a1, a2, baseRadius + rUnd1, baseRadius + rUnd2, yW1, yW2,
					r, g, b, coreAlpha, glowAlpha, coreW, glowW);
		}
	}

	/**
	 * Emits one arc segment of a ring: inner glow + core + outer glow.
	 * Radial widths expand outward from center, matching the CardinalRite approach.
	 *
	 * @param r1, r2 undulated radius at each end of the arc
	 * @param yOff1, yOff2 subtle Y wave offsets
	 */
	private static void emitRingArc(VertexConsumer vc, Matrix4f mat,
			float cx, float y, float cz,
			float aDeg1, float aDeg2, float r1, float r2, float yOff1, float yOff2,
			float r, float g, float b, float coreAlpha, float glowAlpha,
			float coreW, float glowW) {

		float cos1 = (float) Math.cos(Math.toRadians(aDeg1));
		float sin1 = (float) Math.sin(Math.toRadians(aDeg1));
		float cos2 = (float) Math.cos(Math.toRadians(aDeg2));
		float sin2 = (float) Math.sin(Math.toRadians(aDeg2));

		// Radial width bands for vertex 1
		float iGlow1 = r1 - glowW - coreW * 0.5f;
		float iCore1 = r1 - coreW * 0.5f;
		float oCore1 = r1 + coreW * 0.5f;
		float oGlow1 = r1 + glowW + coreW * 0.5f;

		// Radial width bands for vertex 2
		float iGlow2 = r2 - glowW - coreW * 0.5f;
		float iCore2 = r2 - coreW * 0.5f;
		float oCore2 = r2 + coreW * 0.5f;
		float oGlow2 = r2 + glowW + coreW * 0.5f;

		float y1 = y + yOff1;
		float y2 = y + yOff2;

		// Inner glow
		emitQuad(vc, mat,
				cx + cos1 * iGlow1, y1, cz + sin1 * iGlow1, r, g, b, 0f,
				cx + cos1 * iCore1, y1, cz + sin1 * iCore1, r, g, b, glowAlpha,
				cx + cos2 * iCore2, y2, cz + sin2 * iCore2, r, g, b, glowAlpha,
				cx + cos2 * iGlow2, y2, cz + sin2 * iGlow2, r, g, b, 0f);

		// Core
		emitQuad(vc, mat,
				cx + cos1 * iCore1, y1, cz + sin1 * iCore1, r, g, b, coreAlpha,
				cx + cos1 * oCore1, y1, cz + sin1 * oCore1, r, g, b, coreAlpha,
				cx + cos2 * oCore2, y2, cz + sin2 * oCore2, r, g, b, coreAlpha,
				cx + cos2 * iCore2, y2, cz + sin2 * iCore2, r, g, b, coreAlpha);

		// Outer glow
		emitQuad(vc, mat,
				cx + cos1 * oCore1, y1, cz + sin1 * oCore1, r, g, b, glowAlpha,
				cx + cos1 * oGlow1, y1, cz + sin1 * oGlow1, r, g, b, 0f,
				cx + cos2 * oGlow2, y2, cz + sin2 * oGlow2, r, g, b, 0f,
				cx + cos2 * oCore2, y2, cz + sin2 * oCore2, r, g, b, glowAlpha);
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  Undulation
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/** Layered sine-wave radial offset for ring segments. */
	private static float undulation(double angleRad, float time, float dir) {
		double w1 = Math.sin(angleRad * UNDULATE_FREQ + time * UNDULATE_SPEED * dir) * UNDULATE_AMP;
		double w2 = Math.sin(angleRad * UNDULATE_FREQ2 - time * UNDULATE_SPEED2 * dir) * UNDULATE_AMP2;
		double throb = Math.sin(time * 0.04) * 0.02;
		return (float) (w1 + w2 + throb);
	}

	/** Layered sine-wave vertical (Y) offset for ring segments and star spokes. */
	private static float yUndulation(double angleRad, float time, float dir) {
		double w1 = Math.sin(angleRad * 3.0 + time * 0.07 * dir) * 0.035;
		double w2 = Math.sin(angleRad * 7.0 - time * 0.05 * dir) * 0.015;
		double throb = Math.sin(time * 0.035) * 0.012;
		return (float) (w1 + w2 + throb);
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  Quad helper (POSITION_COLOR format)
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	private static void emitQuad(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3,
			float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
		vc.addVertex(mat, x1, y1, z1).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r2, g2, b2, a2);
		vc.addVertex(mat, x3, y3, z3).setColor(r3, g3, b3, a3);
		vc.addVertex(mat, x4, y4, z4).setColor(r4, g4, b4, a4);
	}

}
