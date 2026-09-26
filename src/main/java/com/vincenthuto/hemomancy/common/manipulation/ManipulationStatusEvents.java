package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ManipulationStatusEvents {
	private ManipulationStatusEvents() {
	}

	@SubscribeEvent
	public static void onLivingHeal(LivingHealEvent event) {
		if (!event.getEntity().hasEffect(EffectInit.insatiable_hunger)
                && !event.getEntity().hasEffect(EffectInit.hemophagy) && event.getEntity().hasEffect(EffectInit.necrosis)) {
            event.setAmount(event.getAmount() * 0.75f);
        }
		if (event.getEntity().hasEffect(EffectInit.insatiable_hunger)) {
			float requested=event.getAmount();
            event.setAmount(requested * ManipulationStatusRules.INSATIABLE_HEAL_MULTIPLIER);
            if(requested>event.getAmount() && event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)
                ManipulationVisuals.burst(level,ManipulationVisuals.Form.HUNGER_COLLAPSE,event.getEntity().position(),
                        event.getEntity().position(),1,18);
		}
	}

	@SubscribeEvent
	public static void onLivingItemFinish(LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntity() instanceof Player player)
				|| !player.hasEffect(EffectInit.insatiable_hunger)
				|| event.getItem().get(DataComponents.FOOD) == null) {
			return;
		}
		player.addEffect(new MobEffectInstance(MobEffects.HUNGER,
				ManipulationStatusRules.INSATIABLE_FOOD_HUNGER_TICKS,
				ManipulationStatusRules.INSATIABLE_FOOD_HUNGER_AMPLIFIER,
				false, true, true));
		player.getFoodData().addExhaustion(ManipulationStatusRules.INSATIABLE_FOOD_EXHAUSTION);
		player.level().playSound(null, player.blockPosition(), SoundEvents.HUSK_AMBIENT, SoundSource.PLAYERS,
				0.55F, 0.65F);
	}

	@SubscribeEvent
	public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
		LivingEntity defender = event.getEntity();
		if (event.getAmount() <= 0.0F
				|| !defender.hasEffect(EffectInit.iron_retort)
				|| event.getSource().is(DamageTypes.THORNS)) {
			return;
		}
		Entity direct = event.getSource().getDirectEntity();
		if (!(direct instanceof LivingEntity attacker) || direct == defender || !event.getSource().isDirect()) {
			return;
		}

		event.setAmount(event.getAmount() * ManipulationStatusRules.IRON_RETORT_DAMAGE_MULTIPLIER);
	}

    @SubscribeEvent
    public static void ironRetortLanded(LivingDamageEvent.Post event) {
        if (!SchoolDamage.hasHealthOrAbsorptionDamage(event)) return;
        LivingEntity defender = event.getEntity();
        if (!defender.hasEffect(EffectInit.iron_retort) || event.getSource().is(DamageTypes.THORNS)
                || !event.getSource().isDirect() || !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
                || attacker == defender || event.getSource() instanceof com.vincenthuto.hemomancy.common.damage.SchoolDamageSource school
                && school.context().kind() != com.vincenthuto.hemomancy.common.damage.SchoolHitContext.Kind.DIRECT) return;
        var origin = com.vincenthuto.hemomancy.common.damage.SchoolDamage.retortContext(defender);
        defender.removeEffect(EffectInit.iron_retort);
        var parent = event.getSource() instanceof com.vincenthuto.hemomancy.common.damage.SchoolDamageSource school ? school.context() : null;
        try (var scope = com.vincenthuto.hemomancy.common.damage.SchoolDamage.scope(parent, defender)) {
            var reaction = com.vincenthuto.hemomancy.common.damage.SchoolDamage.reaction(defender, origin);
            var hit = ((com.vincenthuto.hemomancy.common.damage.SchoolDamageSource)reaction).context();
            attacker.hurt(com.vincenthuto.hemomancy.common.damage.SchoolDamage.attributed(defender.damageSources().thorns(defender),
                    hit, defender), ManipulationStatusRules.IRON_RETORT_DAMAGE);
        }
        if (defender.level() instanceof ServerLevel level)
            ManipulationVisuals.burst(level, ManipulationVisuals.Form.FERRIC_IMPACT,
                    defender.position().add(0, defender.getBbHeight() * .6, 0),
                    attacker.position().add(0, attacker.getBbHeight() * .6, 0), .5, 14);
        sendIronRetortParticles(defender, attacker);
        defender.level().playSound(null, defender.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, .65F, 1.6F);
    }

	@SubscribeEvent
	public static void onLivingDamagePost(LivingDamageEvent.Post event) {
		if (event.getSource() instanceof com.vincenthuto.hemomancy.common.damage.SchoolDamageSource school
                && school.context().kind() == com.vincenthuto.hemomancy.common.damage.SchoolHitContext.Kind.REACTION) return;
		float previousHealth = event.getEntity().getHealth() + event.getNewDamage();
		var hit = event.getSource() instanceof com.vincenthuto.hemomancy.common.damage.SchoolDamageSource school
                ? school.context() : null;
        try (var scope = com.vincenthuto.hemomancy.common.damage.SchoolDamage.scope(hit)) {
            SchoolHitHelper.tryTriggerGraveDebtBurst(event.getEntity(), previousHealth, hit);
        }
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		SchoolHitHelper.tryRefundGraveDebt(event.getEntity());
	}

	private static void sendIronRetortParticles(LivingEntity defender, LivingEntity attacker) {
		if (!(defender.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		double x = (defender.getX() + attacker.getX()) * 0.5D;
		double y = defender.getY() + defender.getBbHeight() * 0.65D;
		double z = (defender.getZ() + attacker.getZ()) * 0.5D;
		for (int i = 0; i < 24; i++) {
			serverLevel.sendParticles(HemoParticleData.glow(new ParticleColor(150, 150, 145)),
					x + (defender.getRandom().nextDouble() - 0.5D) * 0.9D,
					y + (defender.getRandom().nextDouble() - 0.5D) * 0.7D,
					z + (defender.getRandom().nextDouble() - 0.5D) * 0.9D,
					1, 0.0D, 0.04D, 0.0D, 0.018D);
		}
	}
}
