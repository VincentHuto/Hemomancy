package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.EnthralledDollModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.world.entity.LivingEntity;

public class EnthralledDollGlowLayer<T extends LivingEntity> extends EyesLayer<T, EnthralledDollModel<T>> {

	private static final RenderType GLOW = RenderType.eyes(
			Hemomancy.rloc("textures/entity/enthralled_doll/model_enthralled_doll_glow.png"));

	public EnthralledDollGlowLayer(RenderLayerParent<T, EnthralledDollModel<T>> p_116981_) {
		super(p_116981_);
	}

	@Override
	public RenderType renderType() {
		return GLOW;
	}
}
