package com.vincenthuto.hemomancy.render.tile;

//GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.tile.BlockEntityChiselStation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RenderChiselStation implements BlockEntityRenderer<BlockEntityChiselStation> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderChiselStation(BlockEntityRendererProvider.Context p_173636_) {
	}
	
	@Override
	public void render(BlockEntityChiselStation te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// Add items above block
		// 
		// GlStateManager._color4f(1F, 1F, 1F, 1F);
		// GlStateManager._translatef(te.getBlockPos().getX(), te.getBlockPos().getY(),
		// te.getBlockPos().getZ());
		// 
		Minecraft.getInstance().textureManager.bindForSetup(TextureAtlas.LOCATION_BLOCKS);
		Minecraft mc = Minecraft.getInstance();

		// Chisel
		matrixStackIn.pushPose();
		if (te.contents.get(3) != null) {
			ItemStack stack = te.contents.get(3);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, -1.0, 1.85f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-360));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-90));
				matrixStackIn.translate(1.3, 1.0, -0.5f + 0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, -1.0, -0.5f + 0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90));
				matrixStackIn.translate(1.3, 1f, 1.5f + 0.35f);
			}
			mc.getItemRenderer().renderStatic(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn, 0);

		}

		matrixStackIn.popPose();

		// Output
		matrixStackIn.pushPose();
		if (te.contents.get(2) != ItemStack.EMPTY) {
			ItemStack stack = te.contents.get(2);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(75));
				matrixStackIn.translate(-1f, 0.25, -1f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(75));
				matrixStackIn.translate(1f, -1.65, -1.5);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(75));
				matrixStackIn.translate(1f, 0.25, -1f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-75));
				matrixStackIn.translate(1f, -1.65, 1.5f);
			}
			mc.getItemRenderer().renderStatic(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn, 0);
		}
		matrixStackIn.popPose();

		// Blank Rune
		matrixStackIn.pushPose();
		if (te.contents.get(0) != ItemStack.EMPTY) {
			ItemStack stack = te.contents.get(0);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(-1f, 1.75, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 0.35, -1.75);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 1.75f, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-45));
				matrixStackIn.translate(1f, 0.35f, 1.75f);
			}
			mc.getItemRenderer().renderStatic(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn, 0);
		}
		matrixStackIn.popPose();

		// Aux Slot
		matrixStackIn.pushPose();
		if (te.contents.get(1) != ItemStack.EMPTY) {
			ItemStack stack = te.contents.get(1);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(-1f, 1.75, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 0.35, -1.75);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(45));
				matrixStackIn.translate(1f, 1.75f, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(0));
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-45));
				matrixStackIn.translate(1f, 0.35f, 1.75f);
			}
			mc.getItemRenderer().renderStatic(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn, 0);
		}
		matrixStackIn.popPose();
	}

}
