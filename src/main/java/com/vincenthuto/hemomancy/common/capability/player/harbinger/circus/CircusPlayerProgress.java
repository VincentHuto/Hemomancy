package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusPerception;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformanceController;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusPlayerProgress {
	/**
	 * The finale's one-time rewards (the AGENTS.md documentation contract): the Ringmaster's Topper, and the Thread
	 * Ripper memory when Liberation could not teach it. The completed finale records the exact owed count here before
	 * the first delivery, and the route is complete, so a finale can never owe them twice.
	 */
	public static final PendingDelivery.Claim FINALE_REWARDS = new PendingDelivery.Claim("circus_finale_rewards",
			player -> {
				CircusProgress progress = progress(player);
				List<ItemStack> owed = new ArrayList<>();
				if (progress.pendingTopper() > 0)
					owed.add(new ItemStack(ItemInit.ringmaster_topper.get(), progress.pendingTopper()));
				if (progress.pendingRipperMemory() > 0)
					owed.add(new ItemStack(ItemInit.memory_thread_ripper.get(), progress.pendingRipperMemory()));
				return owed;
			},
			player -> true,
			(player, left) -> {
				CircusProgress progress = progress(player);
				progress.setPendingTopper(count(left, ItemInit.ringmaster_topper.get()));
				progress.setPendingRipperMemory(count(left, ItemInit.memory_thread_ripper.get()));
			});

	private CircusPlayerProgress() {
	}

	private static CircusProgress progress(Player player) {
		return HemoCapabilityAccess.circusProgress(player);
	}

	public static int acclimation(Player player) {
		return CircusProgressRules.clamp(progress(player).acclimation());
	}

	public static int addAcclimation(ServerPlayer player, int points) {
		int score = CircusProgressRules.clamp(acclimation(player) + Math.max(0, points));
		progress(player).setAcclimation(score);
		return score;
	}

	public static boolean awardMilestone(ServerPlayer player, String milestone, int points) {
		CircusProgress progress = progress(player);
		if (progress.hasMilestone(milestone)) return false;
		progress.addMilestone(milestone);
		addAcclimation(player, points);
		return true;
	}

	public static CircusRouteRules.Route route(Player player) {
		return CircusRouteRules.Route.fromSerializedName(progress(player).route());
	}

	public static boolean chooseRoute(ServerPlayer player, CircusRouteRules.Route choice) {
		CircusRouteRules.Route current = route(player);
		CircusRouteRules.Route selected = CircusRouteRules.choose(current, choice);
		if (selected == current) return false;
		progress(player).setRoute(selected.serializedName());
		return true;
	}

	public static boolean repairRoute(ServerPlayer player) {
		CircusProgress progress = progress(player);
		CircusRouteRules.Route current = route(player);
		if (!CircusRouteRules.canRepair(current, progress.routeRepaired())) return false;
		progress.setRouteRepaired(true);
		progress.setRoute((current == CircusRouteRules.Route.SUCCESSION
				? CircusRouteRules.Route.LIBERATION : CircusRouteRules.Route.SUCCESSION).serializedName());
		return true;
	}

	public static boolean canRepairRoute(Player player) {
		return CircusRouteRules.canRepair(route(player), progress(player).routeRepaired());
	}

	public static void completeRoute(ServerPlayer player, CircusRouteRules.Route route) {
		progress(player).setRoute((route == CircusRouteRules.Route.SUCCESSION
				? CircusRouteRules.Route.SUCCESSION_COMPLETE
				: CircusRouteRules.Route.LIBERATION_COMPLETE).serializedName());
	}

	public static int challenges(Player player) {
		return progress(player).challenges();
	}

	public static boolean completeChallenge(ServerPlayer player, int challenge, int acclimationReward) {
		if (challenge < 0 || challenge > 4) return false;
		int bit = 1 << challenge;
		int current = challenges(player);
		if ((current & bit) != 0) return false;
		progress(player).setChallenges(current | bit);
		addAcclimation(player, acclimationReward);
		return true;
	}

	/** Records the Topper as owed (once) and tries to hand it over now. */
	public static void oweTopper(ServerPlayer player) {
		progress(player).setPendingTopper(1);
		PendingDelivery.deliver(player, FINALE_REWARDS);
	}

	/** Records the Thread Ripper memory as owed (once) and tries to hand it over now. */
	public static void oweRipperMemory(ServerPlayer player) {
		progress(player).setPendingRipperMemory(1);
		PendingDelivery.deliver(player, FINALE_REWARDS);
	}

	private static int count(List<ItemStack> stacks, Item item) {
		int count = 0;
		for (ItemStack stack : stacks) if (stack.is(item)) count += stack.getCount();
		return count;
	}

	/** Records whether the player stands inside a pavilion; a change raises the perception dirty flag. */
	public static void setInside(ServerPlayer player, boolean inside) {
		progress(player).setInside(inside);
	}

	/** The {@code PlayerStateSync} lifecycle send: a login, respawn or new dimension starts outside the pavilion. */
	public static void resyncPerception(ServerPlayer player) {
		progress(player).setInside(false);
		syncPerception(player);
	}

	/** The {@code PlayerStateSync} send for the perception overlay: acclimation and the inside flag, to the owner. */
	public static void syncPerception(ServerPlayer player) {
		CircusProgress progress = progress(player);
		progress.clearPerceptionDirty();
		PacketHandler.sendToPlayer(player, new PacketSyncCircusPerception(acclimation(player), progress.inside()));
	}

	/**
	 * The attachment carries progress and owed rewards through death; the live act and the pavilion this player held
	 * do not. The copy already holds the act, so it is cleared on the new player as well.
	 */
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		CircusPerformanceController.cleanup(event.getOriginal());
		progress(event.getEntity()).clearAct();
		if (event.getEntity() instanceof ServerPlayer player)
			CircusPavilionSavedData.get(player.serverLevel()).resetOwned(player.getUUID());
	}
}
