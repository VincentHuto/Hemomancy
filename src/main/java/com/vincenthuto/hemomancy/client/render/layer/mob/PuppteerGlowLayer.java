package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.BloodDrunkPuppeteerModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;


public class PuppteerGlowLayer<T extends BloodDrunkPuppeteerEntity> extends EyesLayer<T, BloodDrunkPuppeteerModel<T>> {
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer_glow.png"));

	public PuppteerGlowLayer(RenderLayerParent<T, BloodDrunkPuppeteerModel<T>> p_116981_) {
		super(p_116981_);
	}

	@Override
	public RenderType renderType() {
		return GLOW;
	}
}
