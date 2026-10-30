package com.vincenthuto.hemomancy.common.rite.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A Blood Structure that becomes a block in place instead of dropping its result (a Hematic Stake, a Mycelial
 * Synapse). It is checked before any blood is fed and placed when the craft ring closes; the rest of the pattern is
 * cleared as usual.
 */
public interface BloodStructureFormation {
	/** The pattern cell that turns into the placed block and is not cleared. */
	BlockPos pos();

	/** Re-checks and places the block; false leaves the cell to be cleared and {@link #fallbackDrops} to drop. */
	boolean place(ServerLevel level);

	/** What drops when {@link #place} fails at the last moment. */
	default List<ItemStack> fallbackDrops(ItemStack result) {
		return List.of(result);
	}
}
