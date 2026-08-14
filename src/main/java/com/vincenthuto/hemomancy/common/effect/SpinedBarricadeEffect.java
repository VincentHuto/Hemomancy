package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that grants passive thorns-like damage reflection and
 * minor armor bonus, simulating a sea urchin's spiny defensive posture.
 * Applied by the urchin morphling while it is attached to the player. The
 * armor bonus is applied via attribute modifier in EffectInit. When damaged,
 * melee attackers receive spike damage that scales with the amplifier.
 */
public class SpinedBarricadeEffect extends MobEffect {

	public SpinedBarricadeEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		// Attribute modifiers handle the passive armor bonus; no per-tick logic needed.
		// Thorns reflection is handled by the morphling's onEquippedHurt.
	
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.spined_barricade");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

}
