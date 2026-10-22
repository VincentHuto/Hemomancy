package com.vincenthuto.hemomancy.common.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The one way a block entity saves a change and sends its update packet to the players watching it. */
public final class BlockEntitySync {
	private BlockEntitySync() {
	}

	/**
	 * Marks the block entity changed and asks the level to resend it. On a server the update packet goes out with the
	 * chunk's block changes at the end of the tick; on a client the block's section re-renders. A block entity without
	 * a level is left alone.
	 */
	public static void markAndSync(BlockEntity blockEntity) {
		if (blockEntity.getLevel() == null) return;
		blockEntity.setChanged();
		BlockState state = blockEntity.getBlockState();
		blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_ALL);
	}
}
