package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that passively regenerates the player's health over time.
 * Applied by the Gravecap Morphling while it is attached to the player. Each
 * tick, the entity heals a small amount that scales with the amplifier.
 */
public class MycorrhizalMendingEffect extends MobEffect {
	private final String displayKey;

	public MycorrhizalMendingEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.mycorrhizal_mending");
	}

	public MycorrhizalMendingEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
		super(typeIn, liquidColorIn);
		this.displayKey = displayKey;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null) return true;
		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(0.5F + amplifier * 0.25F);
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
		return duration % 40 == 0;
	}

}

