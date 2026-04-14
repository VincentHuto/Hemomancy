package com.vincenthuto.hemomancy.client.render.tile.crafting;

//GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.block.ScarStationModel;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class ScarStationRenderer implements BlockEntityRenderer<ScarStationBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_scar_station.png");

	private final ScarStationModel model;

	public ScarStationRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new ScarStationModel(context.bakeLayer(ScarStationModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(ScarStationBlockEntity te) {
		return true;
	}

	@Override
	public void render(ScarStationBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		// ── Render the chisel station entity model ──
		renderStationModel(matrixStackIn, bufferIn, te, combinedLightIn, combinedOverlayIn);

		// ── Render items on the station ──
		Minecraft.getInstance().textureManager.bindForSetup(TextureAtlas.LOCATION_BLOCKS);
		Minecraft mc = Minecraft.getInstance();

		// Chisel
		matrixStackIn.pushPose();
		if (te.contents.get(3) != null) {
			ItemStack stack = te.contents.get(3);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.ZP,90, true).toMoj());
				matrixStackIn.translate(1.3, -1.0, 1.85f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,-360,true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,180,true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.ZP,-90, true).toMoj());
				matrixStackIn.translate(1.3, 1.0, -0.5f + 0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,-90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.ZP,90, true).toMoj());
				matrixStackIn.translate(1.3, -1.0, -0.5f + 0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.ZP,90, true).toMoj());
				matrixStackIn.translate(1.3, 1f, 1.5f + 0.35f);
			}
			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn,te.getLevel(),0);

		}

		matrixStackIn.popPose();

		// Output
		matrixStackIn.pushPose();
		if (te.contents.get(2) != ItemStack.EMPTY) {
			ItemStack stack = te.contents.get(2);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,75, true).toMoj());
				matrixStackIn.translate(-1f, 0.25, -1f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,-90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,75, true).toMoj());
				matrixStackIn.translate(1f, -1.65, -1.5);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,0, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,75, true).toMoj());
				matrixStackIn.translate(1f, 0.25, -1f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,0, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,-75, true).toMoj());
				matrixStackIn.translate(1f, -1.65, 1.5f);
			}
			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn, te.getLevel(),0);
		}
		matrixStackIn.popPose();

		// Blank Scar
		matrixStackIn.pushPose();
		if (te.contents.get(0) != ItemStack.EMPTY) {
			ItemStack stack = te.contents.get(0);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,45, true).toMoj());
				matrixStackIn.translate(-1f, 1.75, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,-90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,45, true).toMoj());
				matrixStackIn.translate(1f, 0.35, -1.75);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,0, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,45, true).toMoj());
				matrixStackIn.translate(1f, 1.75f, -0.35f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,0, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,-45, true).toMoj());
				matrixStackIn.translate(1f, 0.35f, 1.75f);
			}
			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn,te.getLevel(), 0);
		}
		matrixStackIn.popPose();

		// Aux Slot
		matrixStackIn.pushPose();
		if (te.contents.get(1) != ItemStack.EMPTY) {
			ItemStack stack = te.contents.get(1);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,45, true).toMoj());
				matrixStackIn.translate(-1f, 1.75, -0.40f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,-90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,45, true).toMoj());
				matrixStackIn.translate(1f, 0.35, -1.80);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,0, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,45, true).toMoj());
				matrixStackIn.translate(1f, 1.75f, -0.40f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				matrixStackIn.mulPose(new Quaternion(Vector3.YP,0, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.XP,-45, true).toMoj());
				matrixStackIn.translate(1f, 0.35f, 1.80f);
			}
			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn, te.getLevel(), 0);
		}
		matrixStackIn.popPose();

		// Binder / Pattern - flat on top surface facing the player
		matrixStackIn.pushPose();
		if (te.contents.get(4) != ItemStack.EMPTY) {
			ItemStack stack = te.contents.get(4);
			if (te.getBlockState().getValues().get(FACING).toString().toUpperCase().equals(FaceInfo.WEST.toString())) {
				matrixStackIn.translate(0.1, 0.51, 0.5);
				matrixStackIn.mulPose(new Quaternion(Vector3.XP, -90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.ZP, -90, true).toMoj());
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.EAST.toString())) {
				matrixStackIn.translate(0.9, 0.51, 0.5);
				matrixStackIn.mulPose(new Quaternion(Vector3.XP, -90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.ZP, 90, true).toMoj());
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.NORTH.toString())) {
				matrixStackIn.translate(0.5, 0.51, 0.1);
				matrixStackIn.mulPose(new Quaternion(Vector3.XP, -90, true).toMoj());
				matrixStackIn.mulPose(new Quaternion(Vector3.ZP, 180, true).toMoj());
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			} else if (te.getBlockState().getValues().get(FACING).toString().toUpperCase()
					.equals(FaceInfo.SOUTH.toString())) {
				matrixStackIn.translate(0.5, 0.51, .9);
				matrixStackIn.mulPose(new Quaternion(Vector3.XP, -90, true).toMoj());
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			}
			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStackIn, bufferIn, te.getLevel(), 0);
		}
		matrixStackIn.popPose();
	}

	/**
	 * Renders the chisel station entity model. The model is authored Y-down
	 * (Blockbench convention) so we flip 180° on X. We rotate around Y to
	 * match the block's FACING direction.
	 */
	private void renderStationModel(PoseStack poseStack, MultiBufferSource bufferIn,
									ScarStationBlockEntity te, int combinedLightIn, int combinedOverlayIn) {
		poseStack.pushPose();

		// Centre on the block
		poseStack.translate(0.5D, 1.5D, 0.5D);

		// Flip model upside-down (Blockbench Y-down → world Y-up)
		poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

		// Rotate the model based on the block's facing direction
		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 0f;
			case EAST -> 90f;
			case SOUTH -> 180f;
			case WEST -> 270f;
			default -> 0f;
		};


		poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);

		poseStack.popPose();
	}

}
