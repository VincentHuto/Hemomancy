package com.vincenthuto.hemomancy.render.item.living;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hemomancy.item.tool.living.LivingSpearItem;
import com.vincenthuto.hemomancy.model.item.LivingSpearModel;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class LivingSpearItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static ResourceLocation living_spear = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_spear_hand.png");

	public final LivingSpearModel spearModel;

	public LivingSpearItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		spearModel= new LivingSpearModel(p_172551_.bakeLayer(LivingSpearModel.living_spear));
	}

	public LivingSpearModel getModel() {
		return spearModel;
	}
	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof LivingSpearItem) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			ms.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());

			MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
					.immediate(Tesselator.getInstance().getBuilder());

			VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(living_spear));

			boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
			InteractionHand activeHand = player.getUsedItemHand();
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.675, -0, 0.25);
			if (p_239207_2_ == ItemTransforms.TransformType.GUI) {
				ms.scale(0.75f, 0.75f, 0.75f);
			}
			if (p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
					|| p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
				ms.mulPose(new Quaternion(Vector3.XP, 65, true).toMoj());
				ms.translate(0, 0.2, -0.25);

			}

			if (itemIsInUse) {
				if (activeHand == InteractionHand.MAIN_HAND) {
					if (p_239207_2_ == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
						ms.translate(0,1, -1);

					}
					if (p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, -20, true).toMoj());
						ms.translate(-.55, 0, 0.);

					}
				} else {
					if (p_239207_2_ == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
						ms.translate(0, 0 - .55, 0.);

					}
					if (p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, -20, true).toMoj());
						ms.translate(.55, 0, 0.);

					}
				}
				if (player.getUseItem() == stack) {
					VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
					VertexConsumer buffer = VertexMultiConsumer.create(glint, ivertexbuilder);
					spearModel.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				} else {
					spearModel.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				}
			} else {
				spearModel.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			irendertypebuffer$impl.endBatch();
			ms.popPose();
		}
	}
}