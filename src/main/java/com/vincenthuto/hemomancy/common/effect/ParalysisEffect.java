package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class ParalysisEffect extends MobEffect {
    public ParalysisEffect() { super(MobEffectCategory.HARMFUL, 0xF0DE5C); }
    @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
    @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Paralysis.enforceLock(entity);
        return true;
    }
}
