package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that subtly deflects incoming projectiles, granting
 * knockback resistance. Applied by the Lumenlace Morphling while it is
 * attached to the player. The knockback resistance bonus is applied via
 * attribute modifier in EffectInit.
 */
public class LuminousDissipationEffect extends MobEffect {
	private final String displayKey;

	public LuminousDissipationEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.luminous_dissipation");
	}

	public LuminousDissipationEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
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
