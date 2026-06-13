package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ConstrictedEffect extends MobEffect {
	public ConstrictedEffect(MobEffectCategory category, int color) {
		super(category, color);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, Hemomancy.rloc("constricted_movement_speed"),
				ConstrictorCordRules.MOVEMENT_SPEED_MULTIPLIER - 1.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
