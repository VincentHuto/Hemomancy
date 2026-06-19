package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * Custom item renderer for scarPattern items.
 * Renders layer0 (background) normally, then renders layer1 (scar overlay)
 * with translucent blending so the background shows through.
 */
public class ScarPatternItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final float[][] QUADRANT_OFFSETS = {
			{0.16F, 0.54F},
			{0.52F, 0.54F},
			{0.16F, 0.18F},
			{0.52F, 0.18F}
	};
	private static final float ICON_RADIUS_SCALE = 0.62F;
	private static final float GUI_OVERLAY_DEPTH = 1.12F;
	private static final float HELD_OVERLAY_DEPTH = 0.045F;

	public ScarPatternItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		Minecraft mc = Minecraft.getInstance();
		BakedModel model = mc.getItemRenderer().getModel(stack, null, null, 0);
		RandomSource random = RandomSource.create(42L);

		List<BakedQuad> allQuads = model.getQuads(null, null, random);

		// Render layer0 (background, tintIndex 0) normally with opaque sheet
		VertexConsumer opaqueBuffer = buffer.getBuffer(Sheets.cutoutBlockSheet());
		for (BakedQuad quad : allQuads) {
			if (quad.getTintIndex() != 1) {
				int color = mc.getItemColors().getColor(stack, quad.getTintIndex());
				float r = (color >> 16 & 0xFF) / 255.0F;
				float g = (color >> 8 & 0xFF) / 255.0F;
				float b = (color & 0xFF) / 255.0F;
				opaqueBuffer.putBulkData(poseStack.last(), quad, r, g, b, 1.0F, combinedLight, combinedOverlay);
			}
		}

		if (renderPreparedScarIcons(stack, displayContext, poseStack, buffer, combinedLight, combinedOverlay, mc)) {
			return;
		}

		// Render layer1 (scar overlay, tintIndex 1) with entity translucent render type
		RenderType translucentType = RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
		VertexConsumer translucentBuffer = buffer.getBuffer(translucentType);

		float gameTime = (float) (System.currentTimeMillis() % 3000L) / 3000.0F;
		float pulse = (Mth.sin(gameTime * (float) (Math.PI * 2.0)) + 1.0F) / 2.0F;
		float minAlpha = 0.25F;
		float maxAlpha = 0.65F;
		float alpha = minAlpha + pulse * (maxAlpha - minAlpha);

		for (BakedQuad quad : allQuads) {
			if (quad.getTintIndex() == 1) {
				renderQuadWithAlpha(poseStack.last(), translucentBuffer, quad, alpha, combinedLight, combinedOverlay);
			}
		}
	}

	private boolean renderPreparedScarIcons(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay, Minecraft mc) {
		List<ResourceLocation> scarIds = ItemScarPattern.getScarIds(stack);
		if (scarIds.isEmpty()) {
			return false;
		}
		boolean flatOverlayContext = isFlatOverlayContext(displayContext);
		for (int i = 0; i < scarIds.size() && i < QUADRANT_OFFSETS.length; i++) {
			ItemStack patternStack = patternStackFor(scarIds.get(i));
			if (patternStack.isEmpty()) {
				continue;
			}
			float[] offset = QUADRANT_OFFSETS[i];
			float centerX = offset[0] + 0.16F;
			float centerY = offset[1] + 0.16F;
			poseStack.pushPose();
			// rotation value used to spin the icon itself
			double rotation = ((System.currentTimeMillis() / 18.0) + i * 42.0) % 360.0;
			float depth = (flatOverlayContext ? HELD_OVERLAY_DEPTH : GUI_OVERLAY_DEPTH) + i * 0.01F;
			poseStack.translate(0.125F, 0.125F, depth);

			// Orbit across the motif face so the dynamic scar marks circle the pattern itself.
			float baseX = centerX - 0.5F - 0.18F;
			float baseY = centerY - 0.95F - 0.18F; // vertical offset (keep unchanged)
			float originalRadius = (float) Math.hypot(baseX, baseY);
			float orbitRadius = originalRadius * ICON_RADIUS_SCALE;
			double angleRad = Math.toRadians(rotation);
			float orbitX = (float) Math.cos(angleRad) * orbitRadius;
			float orbitY = (float) Math.sin(angleRad) * orbitRadius;

			// translate by the computed orbit vector (relative to the center point)
			poseStack.translate(orbitX, orbitY, 0.0F);

			// Spin the overlay in the item plane.
			poseStack.mulPose(Axis.XP.rotationDegrees((float) 0));
			poseStack.mulPose(Axis.YP.rotationDegrees((float) 0));
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) 0));
			poseStack.scale(0.75F, 0.75f, 0.75f);
			renderPatternOverlay(patternStack, poseStack, buffer, combinedLight, combinedOverlay, mc, i);
			poseStack.popPose();
		}
		return true;
	}

	private void renderPatternOverlay(ItemStack patternStack, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay, Minecraft mc, int seedOffset) {
		BakedModel model = mc.getItemRenderer().getModel(patternStack, mc.level, null, 59 + seedOffset);
		RandomSource random = RandomSource.create(9173L + seedOffset);
		VertexConsumer translucentBuffer = buffer.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
		for (BakedQuad quad : model.getQuads(null, null, random)) {
			if (quad.getTintIndex() == 1) {
				renderQuadWithAlpha(poseStack.last(), translucentBuffer, quad, 0.85F, combinedLight, combinedOverlay);
			}
		}
	}

	private static boolean isFlatOverlayContext(ItemDisplayContext displayContext) {
		return displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.GROUND;
	}

	private static ItemStack patternStackFor(ResourceLocation id) {
		String path = id.getPath();
		if (path.startsWith("scar_")) {
			path = path.substring("scar_".length());
		}
		Item item = BuiltInRegistries.ITEM.get(Hemomancy.rloc("scar_pattern_" + path));
		if (item == Items.AIR) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(item);
	}

	/**
	 * Manually emits a quad's vertices with a custom alpha value.
	 * Vertex data format per vertex: posX, posY, posZ, color(ABGR), texU, texV, lightmapUV, normalPacked
	 * (8 ints per vertex for DefaultVertexFormat.BLOCK)
	 */
	private void renderQuadWithAlpha(PoseStack.Pose pose, VertexConsumer consumer, BakedQuad quad,
			float alpha, int light, int overlay) {
		int[] vertexData = quad.getVertices();
		int vertexCount = vertexData.length / 8; // 8 ints per vertex

		Matrix4f posMatrix = pose.pose();
		Matrix3f normalMatrix = pose.normal();

		for (int i = 0; i < vertexCount; i++) {
			int offset = i * 8;

			float x = Float.intBitsToFloat(vertexData[offset]);
			float y = Float.intBitsToFloat(vertexData[offset + 1]);
			float z = Float.intBitsToFloat(vertexData[offset + 2]);

			float u = Float.intBitsToFloat(vertexData[offset + 4]);
			float v = Float.intBitsToFloat(vertexData[offset + 5]);

			int packedNormal = vertexData[offset + 7];
			float nx = (byte) (packedNormal & 0xFF) / 127.0F;
			float ny = (byte) ((packedNormal >> 8) & 0xFF) / 127.0F;
			float nz = (byte) ((packedNormal >> 16) & 0xFF) / 127.0F;

			// Transform position
			Vector4f pos = new Vector4f(x, y, z, 1.0F);
			pos.mul(posMatrix);

			// Transform normal
			Vector3f norm = new Vector3f(nx, ny, nz);
			norm.mul(normalMatrix);

			int a = (int) (alpha * 255.0F);

			consumer.addVertex(pos.x(), pos.y(), pos.z())
					.setColor(255, 255, 255, a)
					.setUv(u, v)
					.setOverlay(overlay)
					.setLight(light)
					.setNormal(norm.x(), norm.y(), norm.z());
		}
	}
}
