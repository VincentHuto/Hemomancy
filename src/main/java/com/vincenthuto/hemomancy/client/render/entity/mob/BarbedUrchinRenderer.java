package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.BarbedUrchinBigModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.BarbedUrchinMidModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.BarbedUrchinModel;
import com.vincenthuto.hemomancy.common.entity.mob.BarbedUrchinEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BarbedUrchinRenderer extends MobRenderer<BarbedUrchinEntity, EntityModel<BarbedUrchinEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/barbed_urchin/model_barbed_urchin.png");
	private int puffStateO = 3;

	private final EntityModel<BarbedUrchinEntity> small= this.getModel();
	private final EntityModel<BarbedUrchinEntity> mid;
	private final EntityModel<BarbedUrchinEntity> big ;

	public BarbedUrchinRenderer(Context renderManagerIn) {
		super(renderManagerIn, new BarbedUrchinModel<BarbedUrchinEntity>(renderManagerIn.bakeLayer(BarbedUrchinModel.LAYER_LOCATION)),
				0.5F);
		this.mid = new BarbedUrchinMidModel<BarbedUrchinEntity>(renderManagerIn.bakeLayer(BarbedUrchinMidModel.LAYER_LOCATION));
		this.big = new BarbedUrchinBigModel<BarbedUrchinEntity>(renderManagerIn.bakeLayer(BarbedUrchinBigModel.LAYER_LOCATION));

	}

	@Override
	public void render(BarbedUrchinEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		int i = pEntity.getPuffState();
		if (i != this.puffStateO) {
			if (i == 0) {
				this.model = this.small;
			} else if (i == 1) {
				this.model = this.mid;
			} else {
				this.model = this.big;
			}
		}

		this.puffStateO = i;
		this.shadowRadius = 0.1F + 0.1F * (float) i;
		super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(BarbedUrchinEntity entity) {
		return TEXTURE;

	}

}
