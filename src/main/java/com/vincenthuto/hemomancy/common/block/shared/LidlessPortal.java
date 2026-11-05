package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.config.HemoConfigValues;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * A doorway into the Lidless void: what {@link LidlessPassageService} needs to know about the block a crossing
 * starts in. The door and the broken Sanguine Monolith each answer it their own way; the service keeps the pin,
 * the keys, the exits and the interruptions.
 */
public interface LidlessPortal {
	/** The lower cell of the doorway; the player is pinned at its bottom centre. */
	BlockPos anchor();

	/** The doorway's front; the crossing runs along this axis. */
	Direction facing();

	/** Whether this player may step in right now. */
	boolean admits(ServerPlayer player);

	/** Whether the player, standing where they are, is heading toward the front (so the far cell is on the front side). */
	boolean towardFront(ServerPlayer player);

	LidlessPassageService.Mode mode();

	/** Whether the doorway still stands as it did when the crossing began. */
	boolean stillValid(ServerLevel level);

	/**
	 * The player reached the far end. In a mode that lands in the level they are already standing there; in one that
	 * leaves it they are still at the anchor and this must put them somewhere.
	 */
	void completed(ServerPlayer player, boolean towardFront);

	/** How long the crossing is, in blocks. */
	default int length() {
		return HemoConfigValues.get(HemoServerConfig.LIDLESS_PASSAGE_LENGTH, 16);
	}

	/** The colours of this player's crossing. */
	default LidlessTheme theme(ServerPlayer player) {
		return LidlessTheme.QLIPHOTH;
	}
}
