package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemolymphopodaModel;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;

public class HemolymphopodaHeadpieceLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/hemolymphopoda/model_hemolymphopoda.png");
	private static final Lazy<HemolymphopodaModel<?>> CACHED_MODEL = Lazy.of(
			() -> new HemolymphopodaModel<>(
					Minecraft.getInstance().getEntityModels().bakeLayer(HemolymphopodaModel.LAYER_LOCATION)));

	public HemolymphopodaHeadpieceLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() != ItemInit.hemolymphopoda_headpiece.get()) {
			return;
		}

		poseStack.pushPose();
		this.getParentModel().head.translateAndRotate(poseStack);
		poseStack.translate(0.0F, -0.22F, 0.0F);
		poseStack.scale(0.08F, -0.08F, -0.08F);
		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		CACHED_MODEL.get().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();
	}
}
