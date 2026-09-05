package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusPerception;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusPlayerProgress {
	private static final String ACCLIMATION = "hemomancy.circus_acclimation";
	private static final String ROUTE = "hemomancy.circus_route";
	private static final String CHALLENGES = "hemomancy.circus_challenges";
	private static final String MILESTONE_PREFIX = "hemomancy.circus_milestone.";
	private static final String ROUTE_REPAIRED = "hemomancy.circus_route_repaired";

	private CircusPlayerProgress() {
	}

	public static int acclimation(Player player) {
		return CircusProgressRules.clamp(player.getPersistentData().getInt(ACCLIMATION));
	}

	public static int addAcclimation(ServerPlayer player, int points) {
		int score = CircusProgressRules.clamp(acclimation(player) + Math.max(0, points));
		player.getPersistentData().putInt(ACCLIMATION, score);
		return score;
	}

	public static boolean awardMilestone(ServerPlayer player, String milestone, int points) {
		CompoundTag data = player.getPersistentData();
		String key = MILESTONE_PREFIX + milestone;
		if (data.getBoolean(key)) return false;
		data.putBoolean(key, true);
		addAcclimation(player, points);
		return true;
	}

	public static CircusRouteRules.Route route(Player player) {
		return CircusRouteRules.Route.fromSerializedName(player.getPersistentData().getString(ROUTE));
	}

	public static boolean chooseRoute(ServerPlayer player, CircusRouteRules.Route choice) {
		CircusRouteRules.Route current = route(player);
		CircusRouteRules.Route selected = CircusRouteRules.choose(current, choice);
		if (selected == current) return false;
		player.getPersistentData().putString(ROUTE, selected.serializedName());
		return true;
	}

	public static boolean repairRoute(ServerPlayer player) {
		CompoundTag data = player.getPersistentData();
		CircusRouteRules.Route current = route(player);
		if (!CircusRouteRules.canRepair(current, data.getBoolean(ROUTE_REPAIRED))) return false;
		data.putBoolean(ROUTE_REPAIRED, true);
		data.putString(ROUTE, (current == CircusRouteRules.Route.SUCCESSION
				? CircusRouteRules.Route.LIBERATION : CircusRouteRules.Route.SUCCESSION).serializedName());
		return true;
	}

	public static boolean canRepairRoute(Player player) {
		return CircusRouteRules.canRepair(route(player), player.getPersistentData().getBoolean(ROUTE_REPAIRED));
	}

	public static void completeRoute(ServerPlayer player, CircusRouteRules.Route route) {
		player.getPersistentData().putString(ROUTE,
				(route == CircusRouteRules.Route.SUCCESSION
						? CircusRouteRules.Route.SUCCESSION_COMPLETE
						: CircusRouteRules.Route.LIBERATION_COMPLETE).serializedName());
	}

	public static int challenges(Player player) {
		return player.getPersistentData().getInt(CHALLENGES);
	}

	public static boolean completeChallenge(ServerPlayer player, int challenge, int acclimationReward) {
		if (challenge < 0 || challenge > 4) return false;
		int bit = 1 << challenge;
		int current = challenges(player);
		if ((current & bit) != 0) return false;
		player.getPersistentData().putInt(CHALLENGES, current | bit);
		addAcclimation(player, acclimationReward);
		return true;
	}

	public static void sync(ServerPlayer player, boolean active) {
		PacketHandler.sendToPlayer(player, new PacketSyncCircusPerception(acclimation(player), active));
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		CompoundTag source = event.getOriginal().getPersistentData();
		CompoundTag target = event.getEntity().getPersistentData();
		for (String key : source.getAllKeys()) {
			if (key.startsWith("hemomancy.circus_") && !key.startsWith("hemomancy.circus_active_")
					&& !key.startsWith("hemomancy.circus_challenge_") && source.get(key) != null)
				target.put(key, source.get(key).copy());
		}
		CircusPerformanceController.cleanup(event.getOriginal());
		if (event.getEntity() instanceof ServerPlayer player)
			CircusPavilionSavedData.get(player.serverLevel()).resetOwned(player.getUUID());
	}
}
