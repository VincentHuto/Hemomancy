package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Kinbud's sting (Brined Court balance rule 6): one damage a second per level, its own damage type so death messages
 * name it. It is not Poison: it hurts undead and poison immunity does not cleanse it. Reapplying refreshes the
 * duration rather than stacking.
 */
public final class StungEffect extends MobEffect {
	public StungEffect() {
		super(MobEffectCategory.HARMFUL, 0xEC7A2C);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 20 == 0;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity.level().isClientSide) return true;
		DamageSource source = new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(HemoDamageTypes.STUNG));
		entity.hurt(source, 1.0F + amplifier);
		return true;
	}
}
