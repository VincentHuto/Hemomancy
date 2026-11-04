package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.HematicConstructModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.HematicConstructEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the Hematic Construct from its generated model (tools/model_export/hematic_construct.py): the iron body,
 * then a full-bright pass for the blood that glows at its seams, visor and fists.
 */
public class HematicConstructRenderer extends MobRenderer<HematicConstructEntity, HematicConstructModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/hematic_construct/hematic_construct.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc(
			"textures/entity/hematic_construct/hematic_construct_glow.png"));

	public HematicConstructRenderer(EntityRendererProvider.Context context) {
		super(context, new HematicConstructModel(context.bakeLayer(HematicConstructModel.LAYER_LOCATION)), 0.5F);
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(HematicConstructEntity entity) {
		return TEXTURE;
	}
}
