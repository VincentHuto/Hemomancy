package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VesperEncounterPuppetEvents {
	static final String PUPPET_KEY = "HemomancyVesperEncounterPuppet";
	static final String BOSS_KEY = "HemomancyVesperEncounterBoss";

	private VesperEncounterPuppetEvents() {
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (!event.getEntity().getPersistentData().getBoolean(PUPPET_KEY)
				|| !(event.getEntity().level() instanceof ServerLevel server)
				|| !event.getEntity().getPersistentData().hasUUID(BOSS_KEY)) return;
		Entity boss = server.getEntity(event.getEntity().getPersistentData().getUUID(BOSS_KEY));
		if (boss instanceof VesperTheCrownedRefusalEntity crowned) crowned.applyPuppetBacklash();
	}

	@SubscribeEvent
	public static void suppressDrops(LivingDropsEvent event) {
		if (event.getEntity().getPersistentData().getBoolean(PUPPET_KEY)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void suppressExperience(LivingExperienceDropEvent event) {
		if (event.getEntity().getPersistentData().getBoolean(PUPPET_KEY)) event.setDroppedExperience(0);
	}
}
