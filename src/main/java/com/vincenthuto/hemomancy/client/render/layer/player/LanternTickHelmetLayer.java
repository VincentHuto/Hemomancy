package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.LanternTickModel;
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
import net.neoforged.neoforge.common.util.Lazy;

public class LanternTickHelmetLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final float HEAD_Y_OFFSET = -1.25F;
	private static final float HEAD_Z_OFFSET = -0.02F;
	private static final float HEAD_SCALE = 0.56F;
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/lantern_tick/model_lantern_tick.png");
	private static final Lazy<LanternTickModel> CACHED_MODEL = Lazy.of(
			() -> new LanternTickModel(
					Minecraft.getInstance().getEntityModels().bakeLayer(LanternTickModel.LAYER_LOCATION)));

	public LanternTickHelmetLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() != ItemInit.lantern_tick_helmet.get()) {
			return;
		}
		poseStack.pushPose();
		this.getParentModel().head.translateAndRotate(poseStack);
		poseStack.translate(0.0F, HEAD_Y_OFFSET, HEAD_Z_OFFSET);
		poseStack.scale(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE);
		VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		CACHED_MODEL.get().renderToBuffer(poseStack, consumer, Math.max(packedLight, 0x00F000F0),
				OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();
	}
}
