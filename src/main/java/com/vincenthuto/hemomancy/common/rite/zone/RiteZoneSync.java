package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.SanguineDominionSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.SanguineFervorSavedData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Sends each player the visible rite zones of the dimension they stand in. */
public final class RiteZoneSync {
	private static volatile boolean changed;

	private RiteZoneSync() {}

	/** Flags a visible zone change; {@link RiteZoneEvents} rebroadcasts on its next sync pass. */
	public static void markChanged() {
		changed = true;
	}

	static boolean consumeChanged() {
		boolean was = changed;
		changed = false;
		return was;
	}

	public static void broadcast(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			sendTo(player, player.level().dimension());
		}
	}

	public static void sendTo(ServerPlayer player, ResourceKey<Level> dimension) {
		PacketHandler.sendToPlayer(player, new RiteZoneSyncPacket(visibleZones(player.server, dimension)));
	}

	static List<RiteZoneSyncPacket.Zone> visibleZones(MinecraftServer server, ResourceKey<Level> dimension) {
		ServerLevel overworld = server.overworld();
		long gameTime = overworld.getGameTime();
		List<RiteZoneSyncPacket.Zone> zones = new ArrayList<>();
		for (RiteZoneSavedData data : List.<RiteZoneSavedData>of(
				SanguineDominionSavedData.get(overworld), SanguineFervorSavedData.get(overworld))) {
			for (RiteZone zone : data.activeIn(dimension, gameTime)) {
				if (zones.size() >= RiteZoneSyncPacket.MAX_ZONES) return zones;
				zones.add(RiteZoneSyncPacket.Zone.of(zone));
			}
		}
		return zones;
	}
}
