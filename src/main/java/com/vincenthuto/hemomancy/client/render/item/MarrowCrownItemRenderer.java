package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.MarrowCrownModel;
import com.vincenthuto.hemomancy.client.model.item.LivingAxeModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.armor.MarrowCrownArmorItem;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingAxeItem;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingPistolItem;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MarrowCrownItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static ResourceLocation living_blade = Hemomancy.rloc("textures/models/armor/marrow_crown_layer_1.png");

	public final MarrowCrownModel crownModel;

	public MarrowCrownItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		crownModel = new MarrowCrownModel(p_172551_.bakeLayer(MarrowCrownModel.LAYER_LOCATION));
	}

	public MarrowCrownModel getModel() {
		return crownModel;
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext itemContext, PoseStack ms, MultiBufferSource buffers,
			int light, int overlay) {

		if (stack.getItem() instanceof MarrowCrownArmorItem) {
			Model model;
			ms.pushPose();
			ms.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
			ms.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());

			VertexConsumer ivertexbuilder = buffers.getBuffer(RenderType.text(living_blade));
			if (itemContext == ItemDisplayContext.GROUND) {
				ms.translate(-0.6, -0.5, 0.25);
				ms.scale(.5f, .5f, .5f);
			}
			ms.translate(-0.6, 0.5, 0.25);
			ms.scale(1.25f, 1.25f, 1.25f);
			if (itemContext == ItemDisplayContext.GUI) {
				ms.translate(0.05, -0.45, 0);
				ms.mulPose(new Quaternion(Vector3.XP, 10, true).toMoj());

			}
			if(itemContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
				ms.translate(0.05, -0.75, 0.2);
				ms.mulPose(new Quaternion(Vector3.XP, 10, true).toMoj());
				ms.scale(0.3f,	 0.3f, 0.3f);
			}
			if(itemContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
				ms.translate(0.05, -0.75, 0.2);
				ms.mulPose(new Quaternion(Vector3.XP, 10, true).toMoj());
				ms.scale(0.3f,	 0.3f, 0.3f);
			}
			if(itemContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
				ms.translate(0.05, -0.75, 0.2);
				ms.mulPose(new Quaternion(Vector3.XP, 10, true).toMoj());
				ms.scale(0.3f,	 0.3f, 0.3f);
			}
			if(itemContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
				ms.translate(0.05, -0.75, 0.2);
				ms.mulPose(new Quaternion(Vector3.XP, 10, true).toMoj());
				ms.scale(0.3f,	 0.3f, 0.3f);
			}
			model = crownModel;
			if (model == crownModel) {
				VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
				VertexConsumer buffer = VertexMultiConsumer.create(glint, ivertexbuilder);
				model.renderToBuffer(ms, buffer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				model.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}

			ms.popPose();
		}
	}
}