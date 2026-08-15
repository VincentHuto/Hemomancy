package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that grants the player increased movement speed and
 * attack speed, embodying Emberfang's heated venom reflexes. Applied by the Emberfang Morphling
 * while it is attached to the player. Speed bonuses are applied via attribute
 * modifiers in EffectInit.
 */
public class SerpentineGuileEffect extends MobEffect {
	private final String displayKey;

	public SerpentineGuileEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.serpentine_guile");
	}

	public SerpentineGuileEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
		super(typeIn, liquidColorIn);
		this.displayKey = displayKey;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		// Attribute modifiers handle the passive bonus; no per-tick logic needed.
	
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
