package com.vincenthuto.hemomancy.common.effect;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * A beneficial effect that periodically damages nearby hostile mobs, simulating
 * a swarming verminous aura. Applied by the pests morphling while it is
 * attached to the player. The radius and damage scale with the amplifier.
 */
public class VerminousAuraEffect extends MobEffect {

	public VerminousAuraEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return;

		Level level = entity.level();
		double radius = 6.0 + amplifier * 2.0;
		AABB area = entity.getBoundingBox().inflate(radius);
		List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, area);

		float damage = 1.0F + amplifier * 0.5F;
		for (Monster mob : hostiles) {
			mob.hurt(entity.damageSources().magic(), damage);
		}
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.verminous_aura");
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
