package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusKnifeThrowerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusPerformanceController {
	private static final String ACTIVE = "hemomancy.circus_active_challenge";
	private static final String TICKS = "hemomancy.circus_challenge_ticks";
	private static final String ORIGIN = "hemomancy.circus_challenge_origin";
	private static final String SUBJECT = "hemomancy.circus_challenge_subject";

	private CircusPerformanceController() {
	}

	public static boolean beginNext(ServerPlayer player, BlockPos origin) {
		if (player.getPersistentData().getInt(ACTIVE) > 0) return false;
		CircusRouteRules.Route route = CircusPlayerProgress.route(player);
		int challenge = nextChallenge(CircusPlayerProgress.challenges(player));
		if (challenge < 0 || (route == CircusRouteRules.Route.NEUTRAL
				&& (challenge != 0 || !CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))))
				|| (route != CircusRouteRules.Route.NEUTRAL && route != CircusRouteRules.Route.SUCCESSION)) return false;
		if (!CircusPavilionSavedData.get(player.serverLevel()).beginPerformance(player.serverLevel(), origin,
				player.getUUID(), route)) {
			player.displayClientMessage(Component.translatable("hemomancy.circus.finale.claimed")
					.withStyle(ChatFormatting.DARK_GRAY), false);
			return false;
		}
		player.getPersistentData().putInt(ACTIVE, challenge + 1);
		player.getPersistentData().putInt(TICKS, 0);
		player.getPersistentData().putLong(ORIGIN, origin.asLong());
		if (challenge == 0) spawnAttentionSubject(player, origin);
		armAct(player, origin, challenge);
		player.displayClientMessage(Component.translatable("hemomancy.circus.challenge.begin." + challenge)
				.withStyle(ChatFormatting.DARK_RED), false);
		return true;
	}

	private static int nextChallenge(int mask) {
		for (int challenge : new int[] { 0, 2, 3, 4 }) {
			if ((mask & 1 << challenge) == 0) return challenge;
		}
		return -1;
	}

	private static void spawnAttentionSubject(ServerPlayer player, BlockPos origin) {
		PeacockSpiderEntity spider = EntityInit.peacock_spider.get().create(player.serverLevel());
		if (spider == null) return;
		spider.setNoAi(true);
		spider.setPersistenceRequired();
		spider.setCustomName(Component.translatable("entity.hemomancy.trained_peacock_spider"));
		spider.setCustomNameVisible(true);
		spider.setPos(origin.getX() + 3.5D, origin.getY() + 0.2D, origin.getZ() + 0.5D);
		spider.getPersistentData().putUUID("CircusTrialOwner", player.getUUID());
		player.serverLevel().addFreshEntity(spider);
		player.getPersistentData().putUUID(SUBJECT, spider.getUUID());
	}

	private static void armAct(ServerPlayer player, BlockPos origin, int challenge) {
		if (challenge < 2) return;
		List<CircusPerformerEntity> troupe = player.serverLevel().getEntitiesOfClass(CircusPerformerEntity.class,
				new AABB(origin).inflate(20.0D));
		for (CircusPerformerEntity performer : troupe) {
			if (challenge == 2 && performer instanceof CircusKnifeThrowerEntity) continue;
			if (challenge == 3 && !(performer instanceof CircusKnifeThrowerEntity)) continue;
			performer.beginFinale(player, false);
		}
		if (challenge == 2) troupe.stream().filter(performer -> !(performer instanceof CircusKnifeThrowerEntity))
				.findFirst().ifPresent(CircusPerformerEntity::summonControlledDolls);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		int challenge = player.getPersistentData().getInt(ACTIVE) - 1;
		if (challenge < 0) return;
		BlockPos origin = BlockPos.of(player.getPersistentData().getLong(ORIGIN));
		if (!player.isAlive() || player.distanceToSqr(origin.getCenter()) > 24.0D * 24.0D) {
			reset(player, origin, true);
			return;
		}
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(player.serverLevel())
				.site(player.serverLevel(), origin);
		if (!CircusPavilionStateRules.canAct(site.activeOwner(), player.getUUID())
				|| site.phase() != CircusPavilionStateRules.Phase.PERFORMANCE) {
			clearPlayerState(player);
			return;
		}
		if (challenge == 0 && !watchingSubject(player)) return;
		int ticks = player.getPersistentData().getInt(TICKS) + 1;
		player.getPersistentData().putInt(TICKS, ticks);
		int required = challenge == 0 ? 200 : challenge == 2 ? 200 : challenge == 3 ? 240 : 300;
		if (ticks == required / 2)
			player.displayClientMessage(Component.translatable("hemomancy.circus.challenge.hold." + challenge)
					.withStyle(ChatFormatting.GRAY), true);
		if (ticks < required) return;
		CircusPlayerProgress.completeChallenge(player, challenge, challenge == 0 ? 150 : 100);
		CircusPlayerProgress.sync(player, true);
		player.displayClientMessage(Component.translatable("hemomancy.circus.challenge.complete." + challenge)
				.withStyle(ChatFormatting.RED), false);
		reset(player, origin, false);
	}

	private static boolean watchingSubject(ServerPlayer player) {
		if (!player.getPersistentData().hasUUID(SUBJECT)) return false;
		Entity entity = player.serverLevel().getEntity(player.getPersistentData().getUUID(SUBJECT));
		if (!(entity instanceof LivingEntity living) || !living.isAlive() || player.distanceToSqr(entity) > 10.0D * 10.0D)
			return false;
		return player.getEyePosition().vectorTo(entity.getBoundingBox().getCenter()).normalize()
				.dot(player.getLookAngle()) > 0.92D;
	}

	private static void reset(ServerPlayer player, BlockPos origin, boolean failed) {
		player.serverLevel().getEntitiesOfClass(CircusPerformerEntity.class, new AABB(origin).inflate(20.0D))
				.forEach(CircusPerformerEntity::resetFinale);
		clearPlayerState(player);
		CircusPavilionSavedData.get(player.serverLevel()).reset(player.serverLevel(), origin, player.getUUID());
		if (failed) player.displayClientMessage(Component.translatable("hemomancy.circus.challenge.failed")
				.withStyle(ChatFormatting.GRAY), false);
	}

	private static void clearSubject(ServerPlayer player) {
		if (player.getPersistentData().hasUUID(SUBJECT)) {
			Entity subject = player.serverLevel().getEntity(player.getPersistentData().getUUID(SUBJECT));
			if (subject != null) subject.discard();
		}
	}

	private static void clearPlayerState(ServerPlayer player) {
		clearSubject(player);
		player.getPersistentData().remove(ACTIVE);
		player.getPersistentData().remove(TICKS);
		player.getPersistentData().remove(ORIGIN);
		player.getPersistentData().remove(SUBJECT);
	}

	static void cleanup(Player player) {
		if (!(player instanceof ServerPlayer serverPlayer) || player.getPersistentData().getInt(ACTIVE) <= 0) return;
		reset(serverPlayer, BlockPos.of(player.getPersistentData().getLong(ORIGIN)), false);
	}
}
