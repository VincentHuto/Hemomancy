package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.CurvedHornModel;
import com.vincenthuto.hemomancy.client.model.armor.OpenBloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.OpenCurvedHornModel;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
		if (ent instanceof Player player) {
			HemoCapabilityAccess.getScars(player).ifPresent(inv -> {
				var stack = inv.getStackInSlot(6);
				if (stack.getItem() instanceof BloodGourdItem gourd) {
					this.translateToBody(matrixStack);

					// Check if the gourd is open
					boolean isOpen = stack.hasTag() && stack.getTag().getBoolean(BloodGourdItem.TAG_STATE);

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
						MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
								.immediate(Tesselator.getInstance().getBuilder());
						VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(text));
						if (isOpen) {
							modelOpenCurvedHorn.renderToBuffer(matrixStack, ivertexbuilder, lightness,
									OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
						} else {
							modelCurvedHorn.renderToBuffer(matrixStack, ivertexbuilder, lightness,
									OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
						}
						irendertypebuffer$impl.endBatch();

					} else {
						MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
								.immediate(Tesselator.getInstance().getBuilder());
						VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(text));
						if (isOpen) {
							modelOpenBloodGourd.renderToBuffer(matrixStack, ivertexbuilder, lightness,
									OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
						} else {
							modelBloodGourd.renderToBuffer(matrixStack, ivertexbuilder, lightness,
									OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
						}
						irendertypebuffer$impl.endBatch();

					}

				}
			});
		}
	}

	private void translateToBody(PoseStack matrixStack) {
		this.getParentModel().body.translateAndRotate(matrixStack);
	}
}