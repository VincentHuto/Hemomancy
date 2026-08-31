package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingSpearModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSpearItem;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LivingSpearItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static ResourceLocation living_spear = Hemomancy.rloc("textures/entity/model_living_spear_hand.png");

	public final LivingSpearModel spearModel;

	public LivingSpearItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		spearModel= new LivingSpearModel(p_172551_.bakeLayer(LivingSpearModel.living_spear));
	}

	public LivingSpearModel getModel() {
		return spearModel;
	}
	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof LivingSpearItem) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			if (player == null) {
				return;
			}
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			ms.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());

			boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
			InteractionHand activeHand = player.getUsedItemHand();
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.675, -0, 0.25);
			if (p_239207_2_ == ItemDisplayContext.GUI) {
				ms.scale(0.75f, 0.75f, 0.75f);
			}
			if (p_239207_2_ == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
					|| p_239207_2_ == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
				ms.mulPose(new Quaternion(Vector3.XP, 65, true).toMoj());
				ms.translate(0, 0.2, -0.25);

			}

			if (itemIsInUse) {
				if (activeHand == InteractionHand.MAIN_HAND) {
					if (p_239207_2_ == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
						ms.translate(0,1, -1);

					}
					if (p_239207_2_ == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, -20, true).toMoj());
						ms.translate(-.55, 0, 0.);

					}
				} else {
					if (p_239207_2_ == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
						ms.translate(0, 0 - .55, 0.);

					}
					if (p_239207_2_ == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, -20, true).toMoj());
						ms.translate(.55, 0, 0.);

					}
				}
				if (player.getUseItem() == stack && !LivingStaffMorphRenderer.isMorphBuffer(buffers)) {
					VertexConsumer baseBuffer = buffers.getBuffer(spearModel.renderType(living_spear));
					spearModel.renderToBuffer(ms, baseBuffer, light, OverlayTexture.NO_OVERLAY, -1);
					VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
					spearModel.renderToBuffer(ms, glint, light, OverlayTexture.NO_OVERLAY, -1);
				} else {
					VertexConsumer baseBuffer = buffers.getBuffer(spearModel.renderType(living_spear));
					spearModel.renderToBuffer(ms, baseBuffer, light, OverlayTexture.NO_OVERLAY, -1);
				}
			} else {
				VertexConsumer baseBuffer = buffers.getBuffer(spearModel.renderType(living_spear));
				spearModel.renderToBuffer(ms, baseBuffer, light, OverlayTexture.NO_OVERLAY, -1);
			}

			ms.popPose();
		}
	}
}
