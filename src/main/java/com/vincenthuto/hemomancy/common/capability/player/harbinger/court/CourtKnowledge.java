package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Whether a player knows of the Brined Court. Strain text reads it to decide whether to show a strain's house line
 * (decision M7): the Morphling tooltip's Court line and the Bestiary's Court paragraph. Both run on the client, which
 * holds the owner's {@link CourtProgress} through its {@code PlayerStateSync} row.
 */
public final class CourtKnowledge {
	private CourtKnowledge() {
	}

	public static boolean isKnown(@Nullable Player player) {
		return player != null && HemoCapabilityAccess.courtProgress(player).known();
	}
}
