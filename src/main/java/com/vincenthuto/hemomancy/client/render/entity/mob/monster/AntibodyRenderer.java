package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.AntibodyModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AntibodyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AntibodyRenderer extends MobRenderer<AntibodyEntity, AntibodyModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/antibody/antibody.png");

	public AntibodyRenderer(EntityRendererProvider.Context context) {
		super(context, new AntibodyModel(context.bakeLayer(AntibodyModel.LAYER_LOCATION)), 0.2F);
	}

	@Override
	public ResourceLocation getTextureLocation(AntibodyEntity entity) {
		return TEXTURE;
	}
}
