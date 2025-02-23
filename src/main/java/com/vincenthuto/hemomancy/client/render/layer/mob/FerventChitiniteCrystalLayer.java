package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.FerventChitiniteModel;
import com.vincenthuto.hemomancy.common.entity.mob.FerventChitiniteEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class FerventChitiniteCrystalLayer<T extends LivingEntity>
		extends RenderLayer<FerventChitiniteEntity, FerventChitiniteModel> {

	private static final RenderType GLOW = RenderType.entityTranslucent(
			new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/fervent_chitinite/model_fervent_crystal.png"));

	private final FerventChitiniteModel model;

	public FerventChitiniteCrystalLayer(RenderLayerParent<FerventChitiniteEntity, FerventChitiniteModel> p_117346_,
			EntityModelSet p_174537_) {
		super(p_117346_);
		this.model = new FerventChitiniteModel(p_174537_.bakeLayer(FerventChitiniteModel.CRYSTAL_LAYER_LOCATION));

	}

	@Override
	public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight,
			FerventChitiniteEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks,
			float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity) && pLivingEntity.isInvisible();
		if (!pLivingEntity.isInvisible() || flag) {
			VertexConsumer vertexconsumer;
			if (flag) {
				vertexconsumer = pBuffer.getBuffer(RenderType.outline(this.getTextureLocation(pLivingEntity)));
			} else {
				vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucent(new ResourceLocation(Hemomancy.MOD_ID,
						"textures/entity/fervent_chitinite/model_fervent_crystal.png")));
			}

			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
			this.model.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
			this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight,
					LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

}