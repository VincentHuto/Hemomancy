package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusKnifeThrowerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeActs;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeWant;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
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
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusPerformanceController {
	private CircusPerformanceController() {
	}

	private static CircusProgress act(Player player) {
		return HemoCapabilityAccess.circusProgress(player);
	}

	/** The challenge index of the act {@code player} is performing, or -1 when none runs. */
	public static int activeChallenge(Player player) {
		return act(player).actChallenge();
	}

	/** How many ticks the running act has been held. */
	public static int actTicks(Player player) {
		return act(player).actTicks();
	}

	public static boolean beginNext(ServerPlayer player, BlockPos origin) {
		if (activeChallenge(player) >= 0) return false;
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
		act(player).beginAct(challenge, origin.asLong());
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
		act(player).setActSubject(spider.getUUID());
	}

	private static void armAct(ServerPlayer player, BlockPos origin, int challenge) {
		if (challenge < 2) return;
		// Only the original cast performs; the school's faculty stays out of the ring and keeps teaching.
		List<CircusPerformerEntity> troupe = player.serverLevel().getEntitiesOfClass(CircusPerformerEntity.class,
				new AABB(origin).inflate(20.0D), CircusPerformerEntity::participatesInFinale);
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
		int challenge = activeChallenge(player);
		if (challenge < 0) return;
		BlockPos origin = BlockPos.of(act(player).actOrigin());
		// A dead or spectating owner fails the act: spectators earn nothing in the pavilion.
		if (!player.isAlive() || player.isSpectator() || player.distanceToSqr(origin.getCenter()) > 24.0D * 24.0D) {
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
		int ticks = act(player).actTicks() + 1;
		act(player).setActTicks(ticks);
		int required = challenge == 0 ? 200 : challenge == 2 ? 200 : challenge == 3 ? 240 : 300;
		if (ticks == required / 2)
			player.displayClientMessage(Component.translatable("hemomancy.circus.challenge.hold." + challenge)
					.withStyle(ChatFormatting.GRAY), true);
		if (ticks < required) return;
		CircusPlayerProgress.completeChallenge(player, challenge, challenge == 0 ? 150 : 100);
		// A performance held to the end is a spectacle the Ringmaster's act card will take.
		NpcTradeActs.witnessed(player, NpcTradeWant.Act.SPECTACLE);
		player.displayClientMessage(Component.translatable("hemomancy.circus.challenge.complete." + challenge)
				.withStyle(ChatFormatting.RED), false);
		reset(player, origin, false);
	}

	private static boolean watchingSubject(ServerPlayer player) {
		UUID subject = act(player).actSubject();
		if (subject == null) return false;
		Entity entity = player.serverLevel().getEntity(subject);
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
		UUID id = act(player).actSubject();
		if (id != null) {
			Entity subject = player.serverLevel().getEntity(id);
			if (subject != null) subject.discard();
		}
	}

	private static void clearPlayerState(ServerPlayer player) {
		clearSubject(player);
		act(player).clearAct();
	}

	public static void cleanup(Player player) {
		if (!(player instanceof ServerPlayer serverPlayer) || activeChallenge(player) < 0) return;
		reset(serverPlayer, BlockPos.of(act(player).actOrigin()), false);
	}
}
