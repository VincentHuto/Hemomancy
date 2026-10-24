package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusPerception;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformanceController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusPlayerProgress {
	// The root keys circus progress lived under before the CircusProgress attachment (S-6c, sunset 2027-04-23).
	private static final String LEGACY_DISCOVERED = "hemomancy.circus_discovered";
	private static final String LEGACY_ACCLIMATION = "hemomancy.circus_acclimation";
	private static final String LEGACY_ROUTE = "hemomancy.circus_route";
	private static final String LEGACY_CHALLENGES = "hemomancy.circus_challenges";
	private static final String LEGACY_MILESTONE_PREFIX = "hemomancy.circus_milestone.";
	private static final String LEGACY_ROUTE_REPAIRED = "hemomancy.circus_route_repaired";
	private static final String LEGACY_APPRENTICESHIP = "hemomancy.circus_apprenticeship";

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

	public static void sync(ServerPlayer player, boolean active) {
		PacketHandler.sendToPlayer(player, new PacketSyncCircusPerception(acclimation(player), active));
	}

	/** A {@code PlayerDataMigrations} entry: folds the circus root keys into the attachment and removes them. */
	public static void migrateLegacyRootKeys(ServerPlayer player) {
		foldLegacyRootKeys(player.getPersistentData(), progress(player));
	}

	/**
	 * Moves the old circus root keys out of {@code data} into {@code progress}. Root keys that are present win over
	 * the attachment's values; milestones and apprenticeship entries are added.
	 */
	public static void foldLegacyRootKeys(CompoundTag data, CircusProgress progress) {
		if (data.contains(LEGACY_DISCOVERED)) progress.setDiscovered(data.getBoolean(LEGACY_DISCOVERED));
		if (data.contains(LEGACY_ACCLIMATION)) progress.setAcclimation(data.getInt(LEGACY_ACCLIMATION));
		if (data.contains(LEGACY_ROUTE)) progress.setRoute(data.getString(LEGACY_ROUTE));
		if (data.contains(LEGACY_ROUTE_REPAIRED)) progress.setRouteRepaired(data.getBoolean(LEGACY_ROUTE_REPAIRED));
		if (data.contains(LEGACY_CHALLENGES)) progress.setChallenges(data.getInt(LEGACY_CHALLENGES));
		if (data.contains(LEGACY_APPRENTICESHIP, CompoundTag.TAG_COMPOUND))
			progress.apprenticeship().merge(data.getCompound(LEGACY_APPRENTICESHIP));
		for (String key : List.copyOf(data.getAllKeys())) {
			if (key.startsWith(LEGACY_MILESTONE_PREFIX)) {
				if (data.getBoolean(key)) progress.addMilestone(key.substring(LEGACY_MILESTONE_PREFIX.length()));
				data.remove(key);
			}
		}
		for (String key : List.of(LEGACY_DISCOVERED, LEGACY_ACCLIMATION, LEGACY_ROUTE, LEGACY_ROUTE_REPAIRED,
				LEGACY_CHALLENGES, LEGACY_APPRENTICESHIP)) {
			data.remove(key);
		}
	}

	/** The attachment carries progress through death; the live act and the pavilion this player held do not. */
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		CircusPerformanceController.cleanup(event.getOriginal());
		if (event.getEntity() instanceof ServerPlayer player)
			CircusPavilionSavedData.get(player.serverLevel()).resetOwned(player.getUUID());
	}
}
