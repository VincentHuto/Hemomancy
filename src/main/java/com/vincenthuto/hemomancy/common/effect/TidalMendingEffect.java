package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * A beneficial effect that passively regenerates the player's health, with
 * the regeneration rate doubling when submerged in water or standing in rain.
 * Applied by the coral morphling while it is attached to the player. Each
 * tick, the entity heals a small amount that scales with the amplifier and
 * is boosted by aquatic conditions.
 */
public class TidalMendingEffect extends MobEffect {

	public TidalMendingEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null) return;
		if (entity.getHealth() < entity.getMaxHealth()) {
			float baseHeal = 0.5F + amplifier * 0.25F;
			// Double healing when in water or rain
			if (entity.isInWater() || (entity.level().isRaining()
					&& entity.level().canSeeSky(entity.blockPosition()))) {
				baseHeal *= 2.0F;
			}
			entity.heal(baseHeal);
		}
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.tidal_mending");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return duration % 40 == 0;
	}

}
