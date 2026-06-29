package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.PhantasmalEchoEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class PhantasmalEchoRenderer extends HumanoidMobRenderer<PhantasmalEchoEntity, HumanoidModel<PhantasmalEchoEntity>> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/models/armor/steve.png");

	public PhantasmalEchoRenderer(Context context) {
		super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.45F);
		this.addLayer(new HumanoidArmorLayer<>(this,
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
				context.getModelManager()));
	}

	@Nullable
	@Override
	protected RenderType getRenderType(PhantasmalEchoEntity entity, boolean bodyVisible,
			boolean translucent, boolean glowing) {
		return RenderType.entityTranslucent(getTextureLocation(entity));
	}

	@Override
	public ResourceLocation getTextureLocation(PhantasmalEchoEntity entity) {
		return TEXTURE;
	}
}
