package com.vincenthuto.hemomancy.client.render.tile.functional;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.SanguineMonolithModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineMonolithBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Renders the Sanguine Monolith as a black slab (1 wide × 2 tall × 0.5 deep)
 * with an animated red vein pattern crawling over the bottom third.
 * The veins use the same organic sine-wave tendril approach as the
 * {@code BloodlinePoolScreen} and {@code LedgerScreen} backgrounds.
 */
public class SanguineMonolithRenderer implements BlockEntityRenderer<SanguineMonolithBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_sanguine_monolith.png");

	private final SanguineMonolithModel model;

	// ── Vein animation parameters ──
	private static final int VEIN_COUNT = 12;
	private static final float VEIN_ZONE_HEIGHT = 0.667f; // bottom third of 2-block height

	// Pre-computed vein tendril parameters (seed-based for consistency)
	private final float[][] veinParams;

	public SanguineMonolithRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new SanguineMonolithModel(context.bakeLayer(SanguineMonolithModel.LAYER_LOCATION));

		java.util.Random rand = new java.util.Random(42L);
		veinParams = new float[VEIN_COUNT][8];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();          // start X position (0–1 across face)
			veinParams[i][1] = rand.nextFloat() * 0.33f;  // start Y position (bottom third only)
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // base angle
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.5f; // speed
			veinParams[i][4] = 0.02f + rand.nextFloat() * 0.04f; // amplitude (world-space)
			veinParams[i][5] = 3f + rand.nextFloat() * 5f;  // frequency
			veinParams[i][6] = 15 + rand.nextInt(25);        // length (steps)
			veinParams[i][7] = 0.3f + rand.nextFloat() * 0.7f; // brightness
		}
	}

	@Override
	public boolean shouldRenderOffScreen(SanguineMonolithBlockEntity te) {
		return true;
	}

	@Override
	public void render(SanguineMonolithBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST  -> 270f;
			case SOUTH -> 0f;
			case WEST  -> 90f;
			default    -> 0f;
		};

		// ── Pass 1: Render the black monolith body ──
		ms.pushPose();
		ms.translate(0.5D, 1.5D, 0.5D);
		ms.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
		ms.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		// Render entirely black (RGB = 0.05, 0.02, 0.02 for a very dark slab)
		model.renderToBuffer(ms, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				0.05F, 0.02F, 0.02F, 1.0F);
		ms.popPose();

		// ── Pass 2: Animated red vein overlay on the bottom third ──
		float time = (te.getLevel() != null)
				? te.getLevel().getGameTime() + partialTicks
				: te.getTickCount() + partialTicks;

		ms.pushPose();
		ms.translate(0.5D, 0.0D, 0.5D);
		ms.mulPose(Vector3.YP.rotationDegrees(-yRot).toMoj());

		renderVeinOverlay(ms, bufferIn, time, facing);
		ms.popPose();
	}

	/**
	 * Draws animated red vein tendrils on the front and back faces of the
	 * monolith's bottom third. Each tendril is a sine-wave curve rendered
	 * as small POSITION_COLOR quads on the surface.
	 */
	private void renderVeinOverlay(PoseStack ms, MultiBufferSource bufferIn, float time, Direction facing) {
		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);

		// Render veins on front face (Z = -0.251 so it sits just above the model surface)
		renderFaceVeins(ms, vc, time, 0.0f, 0.0f, -0.251f, 1.0f, false);

		// Render veins on back face
		renderFaceVeins(ms, vc, time, 0.0f, 0.0f, 0.251f, 1.0f, true);

		// Render veins on left side (X = -0.501)
		renderSideVeins(ms, vc, time, -0.501f, 0.0f, 0.0f, false);

		// Render veins on right side (X = 0.501)
		renderSideVeins(ms, vc, time, 0.501f, 0.0f, 0.0f, true);
	}

	private void renderFaceVeins(PoseStack ms, VertexConsumer vc, float time,
			float faceX, float faceY, float faceZ, float faceWidth, boolean flipNormal) {

		Matrix4f mat = ms.last().pose();

		for (int i = 0; i < VEIN_COUNT; i++) {
			float[] p = veinParams[i];
			float startX = (p[0] - 0.5f) * faceWidth;
			float startY = p[1] * VEIN_ZONE_HEIGHT;
			float baseAngle = p[2];
			float speed = p[3];
			float amplitude = p[4];
			float frequency = p[5];
			int length = (int) p[6];
			float brightness = p[7];

			float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.02f + i);
			float cosA = Mth.cos(angleDrift);
			float sinA = Mth.sin(angleDrift);
			float timeOffset = time * speed * 0.1f;

			int baseRed = (int) (100 + 80 * brightness);
			int baseGreen = (int) (5 + 15 * brightness);
			int baseBlue = (int) (5 + 10 * brightness);

			for (int step = 0; step < length; step++) {
				float t = step * 0.015f;
				float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
				float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + i);
				float displacement = squiggle + microSquiggle;

				float px = faceX + startX + t * cosA - displacement * sinA;
				float py = faceY + startY + t * sinA + displacement * cosA;

				// Stay within bottom third
				if (py > VEIN_ZONE_HEIGHT || py < 0) continue;
				// Stay within face width
				if (Math.abs(px) > faceWidth * 0.5f) continue;

				float tipFade = 1f;
				if (step < 5) tipFade = step / 5f;
				else if (step > length - 5) tipFade = (length - step) / 5f;

				float pulse = 0.6f + 0.4f * Mth.sin(time * 0.08f + i * 0.5f + step * 0.05f);
				int alpha = (int) (Mth.clamp(tipFade * pulse * 200, 20, 220));
				int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
				int g = (int) Mth.clamp(baseGreen * pulse * 0.4f, 0, 255);
				int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

				float size = 0.015f;
				float z = faceZ;

				// Render a small quad for each vein step
				vc.vertex(mat, px - size, py - size, z).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, px - size, py + size, z).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, px + size, py + size, z).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, px + size, py - size, z).color(r, g, b, alpha).endVertex();
			}
		}

		// Add a subtle pulsing glow wash across the bottom third
		float glowPulse = 0.5f + 0.5f * Mth.sin(time * 0.04f);
		int glowAlpha = (int) (20 * glowPulse);
		float hw = faceWidth * 0.5f;
		float z = faceZ + (flipNormal ? -0.001f : 0.001f);

		vc.vertex(mat, -hw, 0f, z).color(80, 5, 5, glowAlpha).endVertex();
		vc.vertex(mat, -hw, VEIN_ZONE_HEIGHT * 0.8f, z).color(40, 2, 2, 0).endVertex();
		vc.vertex(mat,  hw, VEIN_ZONE_HEIGHT * 0.8f, z).color(40, 2, 2, 0).endVertex();
		vc.vertex(mat,  hw, 0f, z).color(80, 5, 5, glowAlpha).endVertex();
	}

	private void renderSideVeins(PoseStack ms, VertexConsumer vc, float time,
			float sideX, float sideY, float sideZ, boolean rightSide) {

		Matrix4f mat = ms.last().pose();
		float halfDepth = 0.25f; // 0.5 blocks / 2

		for (int i = 0; i < VEIN_COUNT / 2; i++) {
			float[] p = veinParams[i + VEIN_COUNT / 2];
			float startZ = (p[0] - 0.5f) * halfDepth * 2;
			float startY = p[1] * VEIN_ZONE_HEIGHT;
			float baseAngle = p[2];
			float speed = p[3];
			float amplitude = p[4] * 0.5f; // narrower on sides
			float frequency = p[5];
			int length = (int) (p[6] * 0.7f);
			float brightness = p[7];

			float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.02f + i + 7);
			float cosA = Mth.cos(angleDrift);
			float sinA = Mth.sin(angleDrift);
			float timeOffset = time * speed * 0.1f;

			int baseRed = (int) (90 + 70 * brightness);
			int baseGreen = (int) (5 + 12 * brightness);
			int baseBlue = (int) (5 + 8 * brightness);

			for (int step = 0; step < length; step++) {
				float t = step * 0.015f;
				float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
				float displacement = squiggle;

				float pz = sideZ + startZ + t * cosA - displacement * sinA;
				float py = sideY + startY + t * sinA + displacement * cosA;

				if (py > VEIN_ZONE_HEIGHT || py < 0) continue;
				if (Math.abs(pz) > halfDepth) continue;

				float tipFade = 1f;
				if (step < 4) tipFade = step / 4f;
				else if (step > length - 4) tipFade = (length - step) / 4f;

				float pulse = 0.6f + 0.4f * Mth.sin(time * 0.08f + i * 0.7f + step * 0.06f);
				int alpha = (int) (Mth.clamp(tipFade * pulse * 180, 15, 200));
				int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
				int g = (int) Mth.clamp(baseGreen * pulse * 0.4f, 0, 255);
				int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

				float size = 0.012f;
				float x = sideX;

				vc.vertex(mat, x, py - size, pz - size).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, x, py + size, pz - size).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, x, py + size, pz + size).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, x, py - size, pz + size).color(r, g, b, alpha).endVertex();
			}
		}
	}
}
