package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.FerventChitiniteModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.FerventChitiniteCrystalLayer;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FerventChitiniteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FerventChitiniteRenderer extends MobRenderer<FerventChitiniteEntity, FerventChitiniteModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/fervent_chitinite/model_fervent_chitinite.png");

	public FerventChitiniteRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new FerventChitiniteModel(renderManagerIn.bakeLayer(FerventChitiniteModel.LAYER_LOCATION)), 0.5F);
	      this.addLayer(new FerventChitiniteCrystalLayer(this, renderManagerIn.getModelSet()));

	}
	
	

	@Override
	public ResourceLocation getTextureLocation(FerventChitiniteEntity entity) {
		return TEXTURE;

	}


	
}
