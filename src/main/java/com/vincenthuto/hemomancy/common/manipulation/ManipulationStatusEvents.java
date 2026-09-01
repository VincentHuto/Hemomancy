package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
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
		if (event.getEntity().hasEffect(EffectInit.insatiable_hunger)) {
			event.setAmount(event.getAmount() * ManipulationStatusRules.INSATIABLE_HEAL_MULTIPLIER);
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
		defender.removeEffect(EffectInit.iron_retort);
		if (attacker.hurt(defender.damageSources().thorns(defender), ManipulationStatusRules.IRON_RETORT_DAMAGE)) {
			SchoolHitHelper.tryTriggerConductiveArc(defender, attacker, EnumBloodTendency.FERRIC,
					EnumBloodTendency.DUCTILIS, ManipulationStatusRules.IRON_RETORT_DAMAGE);
		}
		sendIronRetortParticles(defender, attacker);
		defender.level().playSound(null, defender.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS,
				0.65F, 1.6F);
	}

	@SubscribeEvent
	public static void onLivingDamagePost(LivingDamageEvent.Post event) {
		float previousHealth = event.getEntity().getHealth() + event.getNewDamage();
		SchoolHitHelper.tryTriggerGraveDebtBurst(event.getEntity(), previousHealth);
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
			serverLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(150, 150, 145)),
					x + (defender.getRandom().nextDouble() - 0.5D) * 0.9D,
					y + (defender.getRandom().nextDouble() - 0.5D) * 0.7D,
					z + (defender.getRandom().nextDouble() - 0.5D) * 0.9D,
					1, 0.0D, 0.04D, 0.0D, 0.018D);
		}
	}
}
