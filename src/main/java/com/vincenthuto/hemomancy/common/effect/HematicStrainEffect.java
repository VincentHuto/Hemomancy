package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HematicStrainEffect extends MobEffect {

    public HematicStrainEffect(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MAX_HEALTH, "F3E2D1C0-B9A8-4765-3210-FEDCBA987654",
                -0.40D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
