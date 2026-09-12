package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public enum SchoolState {
    PRESSURE(120), NECROSIS(160), LODESTONE(160), SEARING(80), RIME(120),
    DISRUPTED(10), ILLUMINATED(120), OBSCURED(120), VEILED(120);

    public final int duration;
    SchoolState(int duration) { this.duration = duration; }

    public Holder<MobEffect> effect() {
        return switch (this) {
            case PRESSURE -> EffectInit.sanguine_pressure;
            case NECROSIS -> EffectInit.necrosis;
            case LODESTONE -> EffectInit.lodestone;
            case SEARING -> EffectInit.searing;
            case RIME -> EffectInit.rime;
            case DISRUPTED -> EffectInit.disrupted;
            case ILLUMINATED -> EffectInit.illuminated;
            case OBSCURED -> EffectInit.obscured;
            case VEILED -> EffectInit.veiled;
        };
    }
}

