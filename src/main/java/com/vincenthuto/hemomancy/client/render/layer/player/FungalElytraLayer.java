package com.vincenthuto.hemomancy.client.render.layer.player;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FungalElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final ElytraModel<T> modelElytra;

	public static final ResourceLocation fungalElytraTex = Hemomancy.rloc("textures/models/armor/fungal_elytra.png");

	public FungalElytraLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
		this.modelElytra = new ElytraModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));

	}

	public void render(@NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn,
			T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {

		if (entity instanceof Player player) {
			player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
				if (inv.getStackInSlot(0).getItem() == ItemInit.noctifly_agaric.get()) {
					matrixStackIn.pushPose();
					matrixStackIn.translate(0.0D, 0.0D, 0.125D);
					this.getParentModel().copyPropertiesTo(this.modelElytra);
					this.modelElytra.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
					VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn,
							this.modelElytra.renderType(Hemomancy.rloc("textures/models/armor/pontiff_elytra.png")),
							false, false);
					this.modelElytra.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn,
							OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
					matrixStackIn.popPose();
				}

			});
		}
	}
}
