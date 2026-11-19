package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.CircusFinaleSettlement;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformanceController;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusDiscoveryProgress;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

final class CircusJourneyAutomation {
	private CircusJourneyAutomation() { }

	static void perform(ServerPlayer player) {
		BlockPos origin = CircusJourneyController.origin(player);
			switch (CircusJourneyController.stage(player)) {
			case DISCOVERY -> CircusDiscoveryProgress.markDiscovered(player);
			case PERFORMERS -> interactWithPerformers(player, origin);
			case ACCLIMATING -> { }
			case ATTENTION -> runAttentionAct(player, origin);
			case ACTS -> runActs(player, origin);
			case FINALE -> runFinale(player, origin);
			case REWARD, COMPLETE -> { }
		}
	}

	private static void runAttentionAct(ServerPlayer player, BlockPos origin) {
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4, false, false));
		if (CircusPerformanceController.activeChallenge(player) < 0) {
			CircusJourneyFixtures.ringmaster(player, origin).handleChoice(player, CircusRingmasterEntity.EVENT_TRIAL);
		}
		var spiders = player.serverLevel().getEntitiesOfClass(PeacockSpiderEntity.class,
				HemoJourneyFixtures.bounds(origin), entity -> entity.getPersistentData().hasUUID("CircusTrialOwner"));
		if (spiders.isEmpty()) return;
		PeacockSpiderEntity spider = spiders.getFirst();
		player.teleportTo(player.serverLevel(), spider.getX(), spider.getY(), spider.getZ() - 3.0D, 0.0F, 0.0F);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, spider.getBoundingBox().getCenter());
	}

	private static void interactWithPerformers(ServerPlayer player, BlockPos origin) {
		for (CircusPerformerEntity performer : CircusJourneyFixtures.performers(player, origin)) {
			player.teleportTo(player.serverLevel(), performer.getX(), performer.getY(), performer.getZ() - 1.5D, 0.0F, 0.0F);
			performer.interact(player, InteractionHand.MAIN_HAND);
		}
	}

	private static void runActs(ServerPlayer player, BlockPos origin) {
		if (CircusJourneyController.mode(player).equals("liberation")) return;
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4, false, false));
		if (CircusPerformanceController.activeChallenge(player) < 0) {
			CircusJourneyFixtures.ringmaster(player, origin).handleChoice(player, CircusRingmasterEntity.EVENT_TRIAL);
		}
		player.teleportTo(player.serverLevel(), origin.getX() + 0.5D, origin.getY() + 1.0D,
				origin.getZ() - 3.5D, 0.0F, 0.0F);
	}

	/**
	 * The finale is fought in his Chamber (2026-11-18), which a journey fixture does not stage: the ending chosen at his
	 * kneel is laid on the fixture's site directly, as the finale's return does (a kill for Liberation, absorbing him for
	 * Succession).
	 */
	private static void runFinale(ServerPlayer player, BlockPos origin) {
		CircusCarouselEntity carousel = CircusJourneyFixtures.carousel(player, origin);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(player.serverLevel());
		CircusPavilionSavedData.Site site = data.site(player.serverLevel(), carousel.encounterOrigin());
		if (site.outcome() != CircusPavilionStateRules.Outcome.NEUTRAL) return;
		if (site.activeOwner() == null) data.begin(player.serverLevel(), carousel.encounterOrigin(), player.getUUID(),
				com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules.Route.NEUTRAL);
		CircusFinaleSettlement.apply(player.serverLevel(), carousel.encounterOrigin(), player,
				CircusJourneyController.mode(player).equals("liberation") ? FinaleRules.Ending.KILLED : FinaleRules.Ending.ABSORBED);
	}

	static boolean hasReward(ServerPlayer player, String mode) {
		boolean topper = player.getInventory().contains(new net.minecraft.world.item.ItemStack(ItemInit.ringmaster_topper.get()));
		if (!topper) return false;
		if ("liberation".equals(mode)) return HemoCapabilityAccess.getKnownManipulations(player)
				.getKnownManips().containsKey(ManipulationInit.thread_ripper.get());
		return HemoCapabilityAccess.getKnownSummons(player).getKnownSummonNames()
				.contains(PuppeteerSummonDefinitions.RINGMASTER_PATTERN);
	}
}
