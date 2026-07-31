package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Persistent opt-in oath linking a player to one temple hermit's displayed
 * heart. A blessing from one hermit cannot unlock another temple.
 */
public final class TempleOathRules {
	private static final String BLESSED_HERMIT = "hemomancy.blessed_hermit";
	private static final String CLAIMED_HEART_HERMIT = "hemomancy.claimed_heart_hermit";

	private TempleOathRules() {
	}

	public static boolean canClaimHeart(UUID linkedHermit, UUID blessedHermit, boolean alreadyClaimed) {
		return !alreadyClaimed && linkedHermit != null && linkedHermit.equals(blessedHermit);
	}

	public static boolean canBeginInitiation(float health, boolean heartClaimedHere,
			boolean bloodAlreadyActive, boolean riteAlreadyActive) {
		return health >= 6.0F && heartClaimedHere && !bloodAlreadyActive && !riteAlreadyActive;
	}

	public static boolean shouldShowInitiationGuidance(boolean bloodActive,
			boolean hasClaimedThisHermitsHeart) {
		return bloodActive || hasClaimedThisHermitsHeart;
	}

	public static void bless(Player player, UUID hermit) {
		if (player != null && hermit != null) {
			player.getPersistentData().putUUID(BLESSED_HERMIT, hermit);
		}
	}

	public static UUID blessedHermit(Player player) {
		return player != null && player.getPersistentData().hasUUID(BLESSED_HERMIT)
				? player.getPersistentData().getUUID(BLESSED_HERMIT) : null;
	}

	public static void recordHeartClaim(Player player, UUID hermit) {
		if (player != null && hermit != null) {
			player.getPersistentData().putUUID(CLAIMED_HEART_HERMIT, hermit);
		}
	}

	public static boolean hasClaimedHeartFrom(Player player, UUID hermit) {
		return player != null && hermit != null
				&& player.getPersistentData().hasUUID(CLAIMED_HEART_HERMIT)
				&& hermit.equals(player.getPersistentData().getUUID(CLAIMED_HEART_HERMIT));
	}
}
