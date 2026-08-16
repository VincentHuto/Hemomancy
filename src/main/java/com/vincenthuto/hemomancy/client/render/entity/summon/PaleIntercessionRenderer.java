package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.PaleIntercessionEntity;
import com.vincenthuto.hemomancy.client.model.entity.summon.PaleIntercessionModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.PaleIntercessionGlowLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PaleIntercessionRenderer extends MobRenderer<PaleIntercessionEntity, PaleIntercessionModel> {
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/pale_intercession/base.png");
	public static final ResourceLocation EMISSIVE = Hemomancy.rloc("textures/entity/pale_intercession/emissive.png");

	public PaleIntercessionRenderer(EntityRendererProvider.Context context) {
		super(context, new PaleIntercessionModel(context.bakeLayer(PaleIntercessionModel.LAYER_LOCATION)), 0.35f);
		addLayer(new PaleIntercessionGlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(PaleIntercessionEntity entity) { return TEXTURE; }

	@Override
	protected RenderType getRenderType(PaleIntercessionEntity entity, boolean visible, boolean translucent, boolean glowing) {
		return RenderType.entityTranslucent(TEXTURE);
	}
}
