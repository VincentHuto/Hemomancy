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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.util.math.vector.Vector3f;

public class RenderChiselStation extends BlockEntityRenderer<TileEntityChiselStation> {
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

		// Chisel
		matrixStackIn.push();
		if (te.chestContents.get(3) != null) {
			ItemStack stack = te.chestContents.get(3);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, -1.0, 1.85f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-360));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-90));
				matrixStackIn.translate(1.3, 1.0, -0.5f + 0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, -1.0, -0.5f + 0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, 1f, 1.5f + 0.35f);
			}
			mc.getItemRenderer().renderItem(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn);

		}

		matrixStackIn.pop();

		// Output
		matrixStackIn.push();
		if (te.chestContents.get(2) != ItemStack.EMPTY) {
			ItemStack stack = te.chestContents.get(2);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(75));
				matrixStackIn.translate(-1f, 0.25, -1f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(75));
				matrixStackIn.translate(1f, -1.65, -1.5);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(75));
				matrixStackIn.translate(1f, 0.25, -1f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-75));
				matrixStackIn.translate(1f, -1.65, 1.5f);
			}
			mc.getItemRenderer().renderItem(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn);
		}
		matrixStackIn.pop();

		// Blank Rune
		matrixStackIn.push();
		if (te.chestContents.get(0) != ItemStack.EMPTY) {
			ItemStack stack = te.chestContents.get(0);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(-1f, 1.75, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 0.35, -1.75);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 1.75f, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-45));
				matrixStackIn.translate(1f, 0.35f, 1.75f);
			}
			mc.getItemRenderer().renderItem(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn);
		}
		matrixStackIn.pop();

		// Aux Slot
		matrixStackIn.push();
		if (te.chestContents.get(1) != ItemStack.EMPTY) {
			ItemStack stack = te.chestContents.get(1);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(-1f, 1.75, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 0.35, -1.75);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 1.75f, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceDirection.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-45));
				matrixStackIn.translate(1f, 0.35f, 1.75f);
			}
			mc.getItemRenderer().renderItem(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn);
		}
		matrixStackIn.pop();
	}

}
