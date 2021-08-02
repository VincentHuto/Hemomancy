package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityChitinite;
import com.huto.hemomancy.model.entity.mob.ModelChitinite;
import com.huto.hemomancy.render.layer.RenderChitiniteLayer;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderChitinite extends MobRenderer<EntityChitinite, ModelChitinite> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chitinite/model_chitinite.png");

	public RenderChitinite(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new ModelChitinite(), 0.45f);
		this.addLayer(new RenderChitiniteLayer(this));
	}

	@Override
	public ResourceLocation getEntityTexture(EntityChitinite entity) {
		return TEXTURE;

	}

}
