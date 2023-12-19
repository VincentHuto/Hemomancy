package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.world.entity.Entity;

public class RenderChitiniteLayer<T extends Entity, M extends SpiderModel<T>> extends EyesLayer<T, M> {
	private static final RenderType RENDER_TYPE = RenderType
			.eyes(Hemomancy.rloc("textures/entity/chitinite/model_chitinite_warn.png"));

	public RenderChitiniteLayer(RenderLayerParent<T, M> p_117507_) {
		super(p_117507_);
	}

	@Override
	public RenderType renderType() {
		return RENDER_TYPE;
	}
}