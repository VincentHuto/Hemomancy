package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.CurvedHornModel;
import com.vincenthuto.hemomancy.client.model.armor.OpenBloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.OpenCurvedHornModel;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.config.HemoClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;

public class BloodGourdLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	public static ResourceLocation white = getGourdTexture("white");
	public static ResourceLocation red = getGourdTexture("red");
	public static ResourceLocation black = getGourdTexture("black");
	public static ResourceLocation curved = getGourdTexture("curved_horn");

	public static ResourceLocation white_open = getGourdTexture("white_open");
	public static ResourceLocation red_open = getGourdTexture("red_open");
	public static ResourceLocation black_open = getGourdTexture("black_open");
	public static ResourceLocation curved_open = getGourdTexture("curved_horn_open");

	public static ResourceLocation getGourdTexture(String path) {
		return Hemomancy.rloc("textures/entity/blood_gourd/" + path + ".png");

	}
	private final BloodGourdModel<T> modelBloodGourd;
	private final CurvedHornModel<T> modelCurvedHorn;
	private final OpenBloodGourdModel<T> modelOpenBloodGourd;
	private final OpenCurvedHornModel<T> modelOpenCurvedHorn;

	public BloodGourdLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
		modelBloodGourd = new BloodGourdModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(BloodGourdModel.blood_gourd));
		modelCurvedHorn = new CurvedHornModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(CurvedHornModel.curved_horn));
		modelOpenBloodGourd = new OpenBloodGourdModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(OpenBloodGourdModel.open_blood_gourd));
		modelOpenCurvedHorn = new OpenCurvedHornModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(OpenCurvedHornModel.open_curved_horn));
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T ent, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!HemoClientConfig.RENDER_BLOOD_GOURD_LAYER.get()) {
			return;
		}
		if (ent instanceof Player player) {
			HemoCapabilityAccess.getScars(player).ifPresent(inv -> {
				var stack = inv.getStackInSlot(6);
				if (stack.getItem() instanceof BloodGourdItem gourd) {
					this.translateToBody(matrixStack);

					// Check if the gourd is open
					boolean isOpen = stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(BloodGourdItem.TAG_STATE);

					ResourceLocation text;
					if (gourd == ItemInit.curved_horn.get()) {
						text = isOpen ? curved_open : curved;
					} else if (gourd == ItemInit.blood_gourd_white.get()) {
						text = isOpen ? white_open : white;
					} else if (gourd == ItemInit.blood_gourd_red.get()) {
						text = isOpen ? red_open : red;
					} else if (gourd == ItemInit.blood_gourd_black.get()) {
						text = isOpen ? black_open : black;
					} else {
						text = isOpen ? curved_open : curved;
					}

					if (gourd == ItemInit.curved_horn.get()) {
					VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.text(text));
					if (isOpen) {
						modelOpenCurvedHorn.renderToBuffer(matrixStack, ivertexbuilder, lightness,
								OverlayTexture.NO_OVERLAY, -1);
					} else {
						modelCurvedHorn.renderToBuffer(matrixStack, ivertexbuilder, lightness,
								OverlayTexture.NO_OVERLAY, -1);
					}

				} else {
					VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.text(text));
					if (isOpen) {
						modelOpenBloodGourd.renderToBuffer(matrixStack, ivertexbuilder, lightness,
								OverlayTexture.NO_OVERLAY, -1);
					} else {
						modelBloodGourd.renderToBuffer(matrixStack, ivertexbuilder, lightness,
								OverlayTexture.NO_OVERLAY, -1);
					}

				}

				}
			});
		}
	}

	private void translateToBody(PoseStack matrixStack) {
		this.getParentModel().body.translateAndRotate(matrixStack);
	}
}
