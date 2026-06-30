package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingFungalHeadModel;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MycophantTendrilFungalizationLayer<T extends LivingEntity, M extends HumanoidModel<T>>
		extends RenderLayer<T, M> {
	private static final ResourceLocation FUNGAL_HEAD_TEXTURE =
			Hemomancy.rloc("textures/models/morphling/fungal_head.png");
	private static final ResourceLocation SWIRL_TEXTURE =
			Hemomancy.rloc("textures/models/armor/avatar_glow.png");

	private MorphlingFungalHeadModel fungalHeadModel;

	public MycophantTendrilFungalizationLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch) {
		if (!(entity instanceof Player player) || !hasMycophantTendril(player)) {
			return;
		}

		M model = this.getParentModel();
		float pulse = 0.78F + 0.22F * Mth.sin(ageInTicks * 0.06F);

		poseStack.pushPose();
		VertexConsumer bodyConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(getSkinTexture(player)));
		model.renderToBuffer(poseStack, bodyConsumer, packedLight, OverlayTexture.NO_OVERLAY,
				packColor(0.95F, 0.24F, 0.04F, 0.54F * pulse));
		poseStack.popPose();

		poseStack.pushPose();
		VertexConsumer sporeConsumer = buffer.getBuffer(RenderTypeInit.energySwirl(SWIRL_TEXTURE,
				(ageInTicks * 0.010F) % 1.0F, (ageInTicks * 0.004F) % 1.0F));
		model.renderToBuffer(poseStack, sporeConsumer, packedLight, OverlayTexture.NO_OVERLAY,
				packColor(1.0F, 0.70F, 0.10F, 0.34F * pulse));
		poseStack.popPose();

		renderFungalHead(poseStack, buffer, packedLight, entity, model, limbSwing, limbSwingAmount, ageInTicks,
				netHeadYaw, headPitch);
	}

	private void renderFungalHead(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
			M parentModel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		MorphlingFungalHeadModel headModel = fungalHeadModel();
		headModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

		poseStack.pushPose();
		parentModel.head.translateAndRotate(poseStack);
		VertexConsumer headConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(FUNGAL_HEAD_TEXTURE));
		headModel.renderToBuffer(poseStack, headConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		poseStack.popPose();
	}

	private MorphlingFungalHeadModel fungalHeadModel() {
		if (this.fungalHeadModel == null) {
			this.fungalHeadModel = new MorphlingFungalHeadModel(Minecraft.getInstance().getEntityModels()
					.bakeLayer(MorphlingFungalHeadModel.LAYER_LOCATION));
		}
		return this.fungalHeadModel;
	}

	private static boolean hasMycophantTendril(Player player) {
		return HemoCapabilityAccess.getEquipment(player)
				.map(inv -> inv.isRenderLayerVisible(HarbingerEquipmentMenu.CHARM_SLOT_INDEX)
						&& inv.getStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX).is(ItemInit.mycophant_tendril.get()))
				.orElse(false);
	}

	private static ResourceLocation getSkinTexture(Player player) {
		if (player instanceof AbstractClientPlayer acp) {
			return acp.getSkin().texture();
		}
		return DefaultPlayerSkin.get(player.getUUID()).texture();
	}

	private static int packColor(float red, float green, float blue, float alpha) {
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = Mth.clamp((int) (red * 255.0F), 0, 255);
		int g = Mth.clamp((int) (green * 255.0F), 0, 255);
		int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}
