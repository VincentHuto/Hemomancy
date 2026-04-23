package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that passively regenerates the player's health over time.
 * Applied by the fungal morphling while it is attached to the player. Each
 * tick, the entity heals a small amount that scales with the amplifier.
 */
public class MycorrhizalMendingEffect extends MobEffect {

	public MycorrhizalMendingEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
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
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.mycorrhizal_mending");
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

