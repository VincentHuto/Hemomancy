package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

final class CircusJourneyAutomation {
	private CircusJourneyAutomation() { }

	static void perform(ServerPlayer player) {
		BlockPos origin = CircusJourneyController.origin(player);
			switch (CircusJourneyController.stage(player)) {
			case DISCOVERY -> CircusDiscoveryProgress.markDiscovered(player);
			case PERFORMERS -> interactWithPerformers(player, origin);
			case ACCLIMATING -> { }
			case ATTENTION -> runAttentionAct(player, origin);
			case ROUTE -> CircusJourneyFixtures.ringmaster(player, origin).handleChoice(player,
					CircusJourneyController.route(player) == com.vincenthuto.hemomancy.common.circus.CircusRouteRules.Route.SUCCESSION
							? CircusRingmasterEntity.EVENT_ACCEPT : CircusRingmasterEntity.EVENT_REJECT);
			case ACTS -> runActs(player, origin);
			case FINALE -> runFinale(player, origin);
			case REWARD, COMPLETE -> { }
		}
	}

	private static void runAttentionAct(ServerPlayer player, BlockPos origin) {
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4, false, false));
		if (player.getPersistentData().getInt("hemomancy.circus_active_challenge") <= 0) {
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
		if (player.getPersistentData().getInt("hemomancy.circus_active_challenge") <= 0) {
			CircusJourneyFixtures.ringmaster(player, origin).handleChoice(player, CircusRingmasterEntity.EVENT_TRIAL);
		}
		player.teleportTo(player.serverLevel(), origin.getX() + 0.5D, origin.getY() + 1.0D,
				origin.getZ() - 3.5D, 0.0F, 0.0F);
	}

	private static void runFinale(ServerPlayer player, BlockPos origin) {
		CircusRingmasterEntity ringmaster = CircusJourneyFixtures.ringmaster(player, origin);
		CircusCarouselEntity carousel = CircusJourneyFixtures.carousel(player, origin);
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(player.serverLevel())
				.site(player.serverLevel(), carousel.encounterOrigin());
		if (site.activeOwner() == null && site.outcome() == CircusPavilionStateRules.Outcome.NEUTRAL) {
			ringmaster.handleChoice(player, CircusRingmasterEntity.EVENT_FINALE);
			return;
		}
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4, false, false));
		if (site.phase() == CircusPavilionStateRules.Phase.CAROUSEL) {
			if (CircusJourneyController.mode(player).equals("succession")) {
				for (CircusPerformerEntity performer : CircusJourneyFixtures.performers(player, origin))
					if (!performer.isDowned()) performer.hurt(player.damageSources().playerAttack(player), performer.getMaxHealth());
			} else {
				advanceLiberationCarousel(player, carousel);
			}
		} else if (site.phase() == CircusPavilionStateRules.Phase.DESCENT) {
			player.teleportTo(player.serverLevel(), ringmaster.getX(), ringmaster.getY(), ringmaster.getZ() - 2.0D, 0.0F, 0.0F);
			ringmaster.hurt(player.damageSources().playerAttack(player), ringmaster.getMaxHealth() + 1.0F);
		}
	}

	private static void advanceLiberationCarousel(ServerPlayer player, CircusCarouselEntity carousel) {
		for (int horse = 0; horse < 3; horse++) {
			if (carousel.isRiderSevered(horse)) continue;
			aimAtHorse(player, carousel, horse, true);
			int selectedHorse = horse;
			carousel.getPassengers().stream()
					.filter(BloodDrunkPuppeteerEntity.class::isInstance)
					.map(BloodDrunkPuppeteerEntity.class::cast)
					.filter(captive -> captive.getCarouselHorse() == selectedHorse)
					.findFirst().ifPresent(captive -> captive.interact(player, InteractionHand.MAIN_HAND));
			return;
		}
		for (int horse = 0; horse < 3; horse++) {
			if (carousel.isAnchorBroken(horse)) continue;
			aimAtHorse(player, carousel, horse, false);
			carousel.hurt(player.damageSources().playerAttack(player), 1.0F);
			return;
		}
	}

	private static void aimAtHorse(ServerPlayer player, CircusCarouselEntity carousel, int horse, boolean rider) {
		CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(carousel.getYRot(), horse);
		Vec3 target = new Vec3(carousel.getX() + pose.x(), carousel.getY() + (rider ? 2.6D : 1.1D) + pose.bob(),
				carousel.getZ() + pose.z());
		Vec3 away = target.subtract(carousel.position()).normalize().scale(2.0D);
		player.teleportTo(player.serverLevel(), target.x + away.x, carousel.getY() + 1.0D, target.z + away.z, 0.0F, 0.0F);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
	}

	static boolean hasReward(ServerPlayer player, String mode) {
		boolean topper = player.getInventory().contains(new net.minecraft.world.item.ItemStack(ItemInit.ringmaster_topper.get()));
		if (!topper) return false;
		if ("liberation".equals(mode)) return HemoCapabilityAccess.requireKnownManipulations(player)
				.getKnownManips().containsKey(ManipulationInit.thread_ripper.get());
		return HemoCapabilityAccess.requireKnownSummons(player).getKnownSummonNames()
				.contains(PuppeteerSummonDefinitions.RINGMASTER_PATTERN);
	}
}
