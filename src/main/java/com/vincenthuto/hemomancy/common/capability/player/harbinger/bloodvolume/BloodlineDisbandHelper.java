package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingSanctumSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Function;

public final class BloodlineDisbandHelper {

	private BloodlineDisbandHelper() {
	}

	public static int resetOnlineMembers(MinecraftServer server, Bloodline disbandedLine,
			@Nullable Function<ServerPlayer, Component> notificationFactory) {
		int resetCount = 0;
		for (ServerPlayer online : server.getPlayerList().getPlayers()) {
			if (!disbandedLine.hasMember(online.getUUID())) {
				continue;
			}

			HemoCapabilityAccess.getBloodVolume(online).ifPresent(volume -> {
				volume.setBloodLine(Bloodline.NOBLOODLINE);
				BloodVolumeEvents.syncVolume(online, volume);
				if (notificationFactory != null) {
					Component notification = notificationFactory.apply(online);
					if (notification != null) {
						online.displayClientMessage(notification, false);
					}
				}
			});
			resetCount++;
		}
		return resetCount;
	}

	public static int removeOwnedSanctums(MinecraftServer server, Bloodline disbandedLine) {
		if (server == null || disbandedLine == null || !disbandedLine.isValid()) {
			return 0;
		}

		int removed = 0;
		for (ServerLevel level : server.getAllLevels()) {
			FoundingSanctumSavedData sanctumData = FoundingSanctumSavedData.get(level);
			for (UUID memberUuid : disbandedLine.getPlayerUUIDS()) {
				if (!sanctumData.hasSanctum(memberUuid)) {
					continue;
				}
				sanctumData.remove(memberUuid);
				removed++;
			}
		}
		return removed;
	}
}

