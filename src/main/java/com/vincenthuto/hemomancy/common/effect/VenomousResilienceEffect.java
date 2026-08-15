package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that grants the player poison immunity and a small
 * movement speed bonus, embodying Winter Shroud's sealed survival state.
 * Applied by the Winter Shroud Morphling while it is attached to the player.
 * The speed bonus is applied via attribute modifier in EffectInit.
 * Poison immunity is handled by removing the Poison effect each tick.
 */
public class VenomousResilienceEffect extends MobEffect {
	private final String displayKey;

	public VenomousResilienceEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.venomous_resilience");
	}

	public VenomousResilienceEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
		super(typeIn, liquidColorIn);
		this.displayKey = displayKey;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null) return true;
		// Passive poison immunity: strip the Poison effect if present
		if (entity.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
			entity.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
		}
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(displayKey);
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

