package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * A beneficial effect that emanates a hemorrhagic aura, periodically
 * inflicting minor damage to nearby hostile mobs as the tick's anticoagulant
 * saliva prevents their wounds from clotting. Applied by the tick morphling
 * while it is attached to the player. The damage and radius scale with
 * the amplifier.
 */
public class HemorrhagicVenomEffect extends MobEffect {

	public HemorrhagicVenomEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return true;

		Level level = entity.level();
		double radius = 5.0 + amplifier * 2.0;
		AABB area = entity.getBoundingBox().inflate(radius);
		List<Monster> nearbyHostiles = level.getEntitiesOfClass(Monster.class, area);

		// Inflict minor hemorrhage damage to nearby hostiles
		float damage = 1.0f + amplifier * 0.5f;
        var hit = com.vincenthuto.hemomancy.common.damage.SchoolHitContext.direct(
                com.vincenthuto.hemomancy.Hemomancy.rloc("tick_morphling"),
                com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.MORTEM,
                com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.TENEBRIS, entity)
                .child(com.vincenthuto.hemomancy.common.damage.SchoolHitContext.Kind.PERIODIC);
        for (Monster mob : nearbyHostiles) {
            if (!com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductionManager.canHarm(entity, mob)) continue;
            mob.hurt(com.vincenthuto.hemomancy.common.damage.SchoolDamage.attributed(
                    entity.damageSources().magic(), hit, entity), damage);
        }
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.hemorrhagic_venom");
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

