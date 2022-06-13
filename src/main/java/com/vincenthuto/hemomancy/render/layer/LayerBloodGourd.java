package com.vincenthuto.hemomancy.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.item.tool.ItemBloodGourd;
import com.vincenthuto.hemomancy.model.armor.ModelBloodGourd;
import com.vincenthuto.hemomancy.model.armor.ModelCurvedHorn;

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

public class LayerBloodGourd<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	public static ResourceLocation white = getGourdTexture("white");
	public static ResourceLocation red = getGourdTexture("red");
	public static ResourceLocation black = getGourdTexture("black");
	public static ResourceLocation curved = getGourdTexture("curved_horn");

	private final ModelBloodGourd<T> modelBloodGourd;
	private final ModelCurvedHorn<T> modelCurvedHorn;

	public LayerBloodGourd(LivingEntityRenderer<T, M> owner) {
		super(owner);
		modelBloodGourd = new ModelBloodGourd<T>(
				Minecraft.getInstance().getEntityModels().bakeLayer(ModelBloodGourd.blood_gourd));
		modelCurvedHorn = new ModelCurvedHorn<T>(
				Minecraft.getInstance().getEntityModels().bakeLayer(ModelCurvedHorn.curved_horn));
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T ent, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (ent instanceof Player player) {
			player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
				if (inv.getStackInSlot(5).getItem()instanceof ItemBloodGourd gourd) {
					this.translateToBody(matrixStack);
					ResourceLocation text = gourd == ItemInit.blood_gourd_white.get() ? white
							: gourd == ItemInit.blood_gourd_red.get() ? red
									: gourd == ItemInit.blood_gourd_black.get() ? black : curved;

					if (gourd == ItemInit.curved_horn.get()) {
						MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
								.immediate(Tesselator.getInstance().getBuilder());
						VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(text));
						modelCurvedHorn.renderToBuffer(matrixStack, ivertexbuilder, lightness,
								OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
						irendertypebuffer$impl.endBatch();

					} else {
						MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
								.immediate(Tesselator.getInstance().getBuilder());
						VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(text));
						modelBloodGourd.renderToBuffer(matrixStack, ivertexbuilder, lightness,
								OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
						irendertypebuffer$impl.endBatch();

					}

				}
			});
		}
	}

	private void translateToBody(PoseStack matrixStack) {
		this.getParentModel().body.translateAndRotate(matrixStack);
	}

	public static ResourceLocation getGourdTexture(String path) {
		return new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/blood_gourd/" + path + ".png");

	}
}