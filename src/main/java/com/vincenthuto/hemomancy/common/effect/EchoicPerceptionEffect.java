package com.vincenthuto.hemomancy.common.effect;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * A beneficial effect that grants echolocation-like perception, causing nearby
 * entities to glow visibly (even through walls). Applied by the bat morphling
 * while it is attached to the player. The detection radius scales with the
 * amplifier.
 */
public class EchoicPerceptionEffect extends MobEffect {

	public EchoicPerceptionEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return;

		Level level = entity.level();
		double radius = 16.0 + amplifier * 4.0;
		AABB area = entity.getBoundingBox().inflate(radius);
		List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, area,
				e -> e != entity);

		// Apply short Glowing effect to reveal nearby entities
		for (LivingEntity target : nearbyEntities) {
			if (!target.hasEffect(MobEffects.GLOWING)) {
				target.addEffect(new MobEffectInstance(MobEffects.GLOWING,
						45, 0, true, false, false));
			}
		}
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.echoic_perception");
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
