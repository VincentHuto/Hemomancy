package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;

/** The passive identities use event hooks; cryoprotection and wither resistance also clear existing exposure. */
public final class BloodResponseEffect extends MobEffect {
    public BloodResponseEffect(MobEffectCategory category, int color) { super(category, color); }
    @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
    @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (this == EffectInit.cryoprotection.get()) entity.setTicksFrozen(0);
        if (this == EffectInit.wither_resistance.get()) entity.removeEffect(MobEffects.WITHER);
        return true;
    }
}

