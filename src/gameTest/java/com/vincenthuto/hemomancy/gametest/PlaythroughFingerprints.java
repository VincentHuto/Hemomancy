package com.vincenthuto.hemomancy.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * The playthrough suite's door to {@link PlayerFingerprints}, which stays package-private. Each method delegates
 * unchanged.
 */
public final class PlaythroughFingerprints {
	private PlaythroughFingerprints() {
	}

	/** The player's saved data, as a save file would hold it. */
	public static CompoundTag saved(ServerPlayer player) {
		return PlayerFingerprints.saved(player);
	}

	/** The Hemomancy advancements the player has completed, by path. */
	public static Set<String> earned(ServerPlayer player) {
		return PlayerFingerprints.earned(player);
	}

	/** What changed between two saves, two levels deep, without the volatile entity keys. */
	public static String diff(CompoundTag before, CompoundTag after) {
		return PlayerFingerprints.diff(before, after);
	}

	/** Item drops within 8 blocks of {@code near}, by item id. The drops are removed. */
	public static String drops(ServerLevel level, BlockPos near) {
		return PlayerFingerprints.drops(level, near);
	}
}
