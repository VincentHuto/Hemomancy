package com.vincenthuto.hemomancy.client.render.item.tile;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Custom item renderer for RunePattern items.
 * Renders layer0 (background) normally, then renders layer1 (rune overlay)
 * with translucent blending so the background shows through.
 */
public class RunePatternItemRenderer extends BlockEntityWithoutLevelRenderer {

	public RunePatternItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
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
				opaqueBuffer.putBulkData(poseStack.last(), quad, r, g, b, combinedLight, combinedOverlay);
			}
		}

		// Render layer1 (rune overlay, tintIndex 1) with entity translucent render type
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

			consumer.vertex(pos.x(), pos.y(), pos.z())
					.color(255, 255, 255, a)
					.uv(u, v)
					.overlayCoords(overlay)
					.uv2(light)
					.normal(norm.x(), norm.y(), norm.z())
					.endVertex();
		}
	}
}

