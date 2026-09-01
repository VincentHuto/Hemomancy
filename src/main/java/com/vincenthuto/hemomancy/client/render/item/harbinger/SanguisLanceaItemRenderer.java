package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.SanguisLanceaModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.SanguisLanceaItem;
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
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SanguisLanceaItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static ResourceLocation texture = Hemomancy.rloc("textures/entity/sanguis_lancea/model_sanguis_lancea.png");

	public final SanguisLanceaModel spearModel;
	private final SanguisLanceaAnimContext animCtx = new SanguisLanceaAnimContext(new AnimationState());

	public SanguisLanceaItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		spearModel = new SanguisLanceaModel(p_172551_.bakeLayer(SanguisLanceaModel.LAYER_LOCATION));
		animCtx.state.start(0);

	}

	public SanguisLanceaModel getModel() {
		return spearModel;
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext p_239207_2_, PoseStack ms, MultiBufferSource buffers,
			int light, int overlay) {

		if (stack.getItem() instanceof SanguisLanceaItem) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;

			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			ms.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());

			if (player == null || mc.level == null) {
				VertexConsumer base = buffers.getBuffer(this.spearModel.renderType(texture));
				spearModel.renderToBuffer(ms, base, light, OverlayTexture.NO_OVERLAY, -1);
				VertexConsumer glint0 = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				spearModel.renderToBuffer(ms, glint0, light, OverlayTexture.NO_OVERLAY, -1);
				ms.popPose();
				return;
			}

			boolean itemIsInUse = player.isUsingItem() && !player.getUseItem().isEmpty()
					&& ItemStack.isSameItemSameComponents(player.getUseItem(), stack);
			InteractionHand activeHand = player.getUsedItemHand();
			ms.scale(0.65f, 0.65f, 0.65f);
			ms.translate(-0.75, -0, 0.75);
			if (p_239207_2_ == ItemDisplayContext.GUI) {
				ms.translate(-0.5f, -0.25f, -0.25);
				ms.mulPose(new Quaternion(Vector3.XP, -25f, true).toMoj());
				ms.scale(0.45f, 0.45f, 0.45f);
			}
			if (p_239207_2_ == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
					|| p_239207_2_ == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
				ms.scale(0.4f, 0.4f, 0.4f);
				if (itemIsInUse) {
					ms.mulPose(new Quaternion(Vector3.XN, -45f, true).toMoj());
					ms.translate(0, -1, -1);

				}

			}

			if (itemIsInUse) {
				if (activeHand == InteractionHand.MAIN_HAND) {
					if (p_239207_2_ == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
						ms.translate(0, 1, -1);

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
				if (ItemStack.isSameItemSameComponents(player.getUseItem(), stack)) {
					if (p_239207_2_ != ItemDisplayContext.GUI) {
						ms.translate(0, 0, 1);
					}

					spearModel.setupAnimation(mc.level, mc.getTimer().getGameTimeDeltaPartialTick(false), animCtx);
				}

				VertexConsumer base = buffers.getBuffer(this.spearModel.renderType(texture));
				spearModel.renderToBuffer(ms, base, light, OverlayTexture.NO_OVERLAY, -1);
				VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				spearModel.renderToBuffer(ms, glint, light, OverlayTexture.NO_OVERLAY, -1);
			} else {
				if (p_239207_2_ == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
					ms.scale(0.75f, 0.75f, 0.75f);

				}
				if (p_239207_2_ == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {

				}
				if (p_239207_2_ == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
					ms.scale(0.75f, 0.75f, 0.75f);

				}
				if (p_239207_2_ == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {

				}

				VertexConsumer base = buffers.getBuffer(this.spearModel.renderType(texture));
				spearModel.renderToBuffer(ms, base, light, OverlayTexture.NO_OVERLAY, -1);
				VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				spearModel.renderToBuffer(ms, glint, light, OverlayTexture.NO_OVERLAY, -1);
			}

			ms.popPose();
		}
	}

	public record SanguisLanceaAnimContext(AnimationState state) {
	}

}
