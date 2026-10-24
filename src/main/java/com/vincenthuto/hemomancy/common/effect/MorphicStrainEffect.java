package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MorphicStrainEffect extends MobEffect {
	public MorphicStrainEffect(MobEffectCategory category, int color) {
		super(category, color);
		addAttributeModifier(Attributes.MAX_HEALTH,
				Hemomancy.rloc("morphic_strain_max_health"),
				-0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		addAttributeModifier(Attributes.MOVEMENT_SPEED,
				Hemomancy.rloc("morphic_strain_movement_speed"),
				-0.04D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
