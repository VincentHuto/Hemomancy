package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MasonsEffigyModel;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MasonsEffigyBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MasonsEffigyRenderer implements BlockEntityRenderer<MasonsEffigyBlockEntity> {
	private final ItemRenderer itemRenderer;
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_masons_effigy.png");


	private final MasonsEffigyModel model;


	public MasonsEffigyRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
		this.model = new MasonsEffigyModel(context.bakeLayer(MasonsEffigyModel.LAYER_LOCATION));

	}

	@Override
	public void render(MasonsEffigyBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {



		poseStack.pushPose();
		poseStack.translate(0.5D, 1.5D, 0.5D);
		poseStack.mulPose(Vector3.XP.rotationDegrees(180.0F).toMoj());

		Direction facing = te.getBlockState().getValue(MasonsEffigyBlock.FACING);
		float yRot = switch (facing) {
			case NORTH -> 180.0F;
			case EAST -> 270.0F;
			case SOUTH -> 0.0F;
			case WEST -> 90.0F;
			default -> 0.0F;
		};
		poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		float ageInTicks = partialTicks;
		if (te.getLevel() != null) {
			ageInTicks += te.getLevel().getGameTime();
		}
		this.model.setupAnim(ageInTicks);

		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();



		ItemStack displayed = te.getPendingMotifDisplayStack();
		if (displayed.isEmpty()) {
			return;
		}

		long gameTime = te.getLevel() != null ? te.getLevel().getGameTime() : 0L;
		float time = gameTime + partialTicks;
		float bob = Mth.sin(time * 0.08F) * 0.035F;
		float spin = (time * 2.0F) % 360.0F;

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.75F + bob, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(spin));
		poseStack.mulPose(Axis.XP.rotationDegrees(12.0F));
		poseStack.scale(0.55F, 0.55F, 0.55F);
		this.itemRenderer.renderStatic(null, displayed, ItemDisplayContext.FIXED, false,
				poseStack, buffer, te.getLevel(), combinedLight, combinedOverlay, 0);
		poseStack.popPose();
	}
}
