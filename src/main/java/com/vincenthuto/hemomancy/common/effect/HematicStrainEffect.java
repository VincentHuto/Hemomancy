package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HematicStrainEffect extends MobEffect {

    public HematicStrainEffect(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MAX_HEALTH, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "hematic_strain_max_health"),
                -0.40D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
