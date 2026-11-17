package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * The world's answer (core-loop Track 10): a creature an awake Harbinger kills bleeds into the region's count (a kill
 * by draw does not: that blood was taken, not left), and each level's scavenger check.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class WorldAnswerEvents {
	private WorldAnswerEvents() {
	}

	@SubscribeEvent
	public static void onKill(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		LivingEntity victim = event.getEntity();
		if (!(victim.level() instanceof ServerLevel level) || !(source.getEntity() instanceof Player killer)) return;
		if (!HemoDamageTypes.paysKillBounty(source) || HemoEntityPredicates.NOBLOOD.test(victim)) return;
		if (!HemoCapabilityAccess.getBloodVolume(killer).isActive()) return;
		WorldAnswer.killed(level, victim);
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel level) WorldAnswer.tickScavengers(level);
	}
}
