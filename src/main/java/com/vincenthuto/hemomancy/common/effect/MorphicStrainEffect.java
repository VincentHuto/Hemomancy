package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MorphicStrainEffect extends MobEffect {
	public MorphicStrainEffect(MobEffectCategory category, int color) {
		super(category, color);
		addAttributeModifier(Attributes.MAX_HEALTH,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "morphic_strain_max_health"),
				-0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		addAttributeModifier(Attributes.MOVEMENT_SPEED,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "morphic_strain_movement_speed"),
				-0.04D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
