package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Silver Ward — a passive beneficial effect for followers of the Unstained
 * path. Provides resistance to blood magic (via attribute modifiers defined
 * in EffectInit) and a subtle armour bonus that scales with amplifier level.
 * The armour bonus is applied via attribute modifier in EffectInit registration.
 */
public class SilverWardEffect extends MobEffect {

	public SilverWardEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		// Attribute modifiers handle the passive bonus; no per-tick logic needed.
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.silver_ward");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
