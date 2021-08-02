package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.item.tool.living.ItemLivingSpear;
import com.huto.hemomancy.model.entity.armor.ModelLivingSpear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class RenderItemLivingSpear extends BlockEntityWithoutLevelRenderer {
	public RenderItemLivingSpear(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		// TODO Auto-generated constructor stub
	}

	public final ModelLivingSpear spearModel = new ModelLivingSpear();

	public static ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_living_spear_hand.png");


	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {

		if (stack.getItem() instanceof ItemLivingSpear) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3f.XP, 180, true));
			ms.mulPose(new Quaternion(Vector3f.YP, 180, true));

			MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
					.immediate(Tesselator.getInstance().getBuilder());
			VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(spearModel.renderType(TEXTURE));

			boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
			InteractionHand activeHand = player.getUsedItemHand();
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.75, -0.95, 0.75);
			if (p_239207_2_ == ItemTransforms.TransformType.GUI) {
				ms.translate(-0, -0.25, 0);
				ms.scale(0.75f, 0.75f, 0.75f);
			}
			if (p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
					|| p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
				ms.mulPose(new Quaternion(Vector3f.XP, 65, true));
				ms.translate(0, 0, -0.25);

			}

			if (itemIsInUse) {
				if (activeHand == InteractionHand.MAIN_HAND) {
					if (p_239207_2_ == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3f.XP, 180, true));
						ms.translate(0, 0 - .55, 0.);

					}
					if (p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3f.XP, -20, true));
						ms.translate(-.55, 0, 0.);

					}
				} else {
					if (p_239207_2_ == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
						ms.mulPose(new Quaternion(Vector3f.XP, 180, true));
						ms.translate(0, 0 - .55, 0.);

					}
					if (p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
						ms.mulPose(new Quaternion(Vector3f.XP, -20, true));
						ms.translate(.55, 0, 0.);

					}
				}
				if (player.getUseItem() == stack) {

					VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
					VertexConsumer buffer = VertexMultiConsumer.create(glint, ivertexbuilder);
					spearModel.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				} else {
					spearModel.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
							1.0F);
				}
			} else {
				spearModel.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			irendertypebuffer$impl.endBatch();
			ms.popPose();
		}
	}

	public ModelLivingSpear getModel() {
		return spearModel;
	}
}