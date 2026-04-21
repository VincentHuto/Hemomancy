package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MorphlingCradleModel;
import com.vincenthuto.hemomancy.common.block.functional.MorphlingCradleBlock;
import com.vincenthuto.hemomancy.common.tile.functional.MorphlingCradleBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class MorphlingCradleRenderer implements BlockEntityRenderer<MorphlingCradleBlockEntity> {
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/block/hematic_iron_block.png");
	private final MorphlingCradleModel model;

	public MorphlingCradleRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new MorphlingCradleModel(context.bakeLayer(MorphlingCradleModel.LAYER_LOCATION));
	}

	@Override
	public void render(MorphlingCradleBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 1.5D, 0.5D);
		poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

		AttachFace face = te.getBlockState().getValue(MorphlingCradleBlock.FACE);
		float yRot = switch (te.getBlockState().getValue(MorphlingCradleBlock.FACING)) {
			case NORTH -> 180f;
			case EAST -> 270f;
			case SOUTH -> 0f;
			case WEST -> 90f;
			default -> 0f;
		};
		poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());
		if (face == AttachFace.CEILING) {
			poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
		} else if (face == AttachFace.WALL) {
			poseStack.mulPose(Vector3.XP.rotationDegrees(90f).toMoj());
		}

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();

		ItemStack hosted = te.getHostedMorphling();
		if (!hosted.isEmpty()) {
			float time = (te.getLevel() != null ? te.getLevel().getGameTime() : 0) + partialTicks;
			poseStack.pushPose();
			float itemY = switch (face) {
				case CEILING -> 0.35F;
				case WALL -> 0.6F;
				case FLOOR -> 0.9F;
			};
			poseStack.translate(0.5F, itemY, 0.5F);
			poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
			poseStack.translate(0.0F, Mth.sin(time * 0.1F) * 0.04F, 0.0F);
			poseStack.scale(0.55F, 0.55F, 0.55F);
			Minecraft.getInstance().getItemRenderer().renderStatic(null, hosted, ItemDisplayContext.FIXED, true,
					poseStack, bufferIn, null, combinedLightIn, combinedOverlayIn, 0);
			poseStack.popPose();
		}
	}
}
