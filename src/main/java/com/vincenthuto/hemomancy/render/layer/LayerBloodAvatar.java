package com.vincenthuto.hemomancy.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.rune.RunesCapabilities;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class LayerBloodAvatar<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

	public LayerBloodAvatar(RenderLayerParent<T, M> p_117346_) {
		super(p_117346_);

	}

	@Override
	public void render(PoseStack ms, MultiBufferSource pBuffer, int pPackedLight, T ent, float pLimbSwing,
			float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		if (ent instanceof Player player) {
			player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manip -> {
				ResourceLocation glowTexture = new ResourceLocation(Hemomancy.MOD_ID,
						"textures/models/armor/avatar_glow.png");
				if (manip.isAvatarActive()) {
					float f = (float) ent.tickCount + pPartialTicks;
					EntityModel<T> entitymodel = this.getParentModel();
					entitymodel.prepareMobModel(ent, pLimbSwing, pLimbSwingAmount, pPartialTicks);
					this.getParentModel().copyPropertiesTo(entitymodel);
					ms.pushPose();
					ms.scale(2.5f, 2.5f, 2.5f);
					VertexConsumer swirlConsumer = pBuffer
							.getBuffer(RenderType.energySwirl(glowTexture, this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
					entitymodel.setupAnim(ent, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
					entitymodel.renderToBuffer(ms, swirlConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F,
							0.5F, 1.0F);
					ms.popPose();

				}
			});
		}
	}

	protected float xOffset(float p_116683_) {
		return p_116683_ * 0.01F;
	}
}
