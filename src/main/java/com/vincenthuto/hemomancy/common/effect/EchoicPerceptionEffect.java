package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * A beneficial effect that grants echolocation-like perception, causing nearby
 * entities to glow visibly (even through walls). Applied by the Witch's Ear Morphling
 * while it is attached to the player. The detection radius scales with the
 * amplifier.
 */
public class EchoicPerceptionEffect extends MobEffect {
	private final String displayKey;

	public EchoicPerceptionEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.echoic_perception");
	}

	public EchoicPerceptionEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
		super(typeIn, liquidColorIn);
		this.displayKey = displayKey;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return true;

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

