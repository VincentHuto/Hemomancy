package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.tile.TileEntityChiselStation;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceDirection;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.math.vector.Vector3f;

public class RenderChiselStation extends TileEntityRenderer<TileEntityChiselStation> {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

	public RenderChiselStation(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(TileEntityChiselStation te, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// Add items above block
		GlStateManager.pushMatrix();
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		GlStateManager.translatef(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
		GlStateManager.popMatrix();
		Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		Minecraft mc = Minecraft.getInstance();

		
		
		matrixStackIn.push();
		if (te.getStackInSlot(3) != null) {
			ItemStack stack = te.getStackInSlot(3);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, -1.0, 1.5f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-360));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-90));
				matrixStackIn.translate(1.3, 1.0, -0.5f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, -1.0, -0.5f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, 1f, 1.5f);
			}
			mc.getItemRenderer().renderItem(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn);

		}
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(0));
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180));

		matrixStackIn.translate(-0.5, 0f, -0.4f);
		mc.getItemRenderer().renderItem(te.getStackInSlot(0), TransformType.FIXED, combinedLightIn, combinedOverlayIn,
				matrixStackIn, bufferIn);
		matrixStackIn.translate(0, 0f, 0.05);

		mc.getItemRenderer().renderItem(te.getStackInSlot(1), TransformType.FIXED, combinedLightIn, combinedOverlayIn,
				matrixStackIn, bufferIn);
		mc.getItemRenderer().renderItem(te.getStackInSlot(2), TransformType.FIXED, combinedLightIn, combinedOverlayIn,
				matrixStackIn, bufferIn);
		matrixStackIn.pop();

		/*
		 * for (int i = 0; i < te.getSizeInventory(); i++) {
		 * 
		 * GlStateManager.pushMatrix(); GlStateManager.translatef(0.5F, 1.25F, 0.5F);
		 * 
		 * GlStateManager.translatef(0.025F, -0.5F, 0.025F); GlStateManager.rotatef(90F,
		 * 1F, 0F, 0F); GlStateManager.rotatef(270F, 0F, 0F, 1F);
		 * GlStateManager.translatef(0F, -0.1F, 0.3F);
		 * 
		 * GlStateManager.scalef(0.15f, 0.15f, 0.15f); GlStateManager.popMatrix();
		 * 
		 * ItemStack stack = te.chestContents.get(i); if (!stack.isEmpty()) {
		 * mc.getItemRenderer().renderItem(stack, TransformType.FIXED, combinedLightIn,
		 * combinedOverlayIn, matrixStackIn, bufferIn); } }
		 */
	}

}
