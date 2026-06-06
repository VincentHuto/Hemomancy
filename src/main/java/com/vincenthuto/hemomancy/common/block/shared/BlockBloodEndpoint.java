package com.vincenthuto.hemomancy.common.block.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block-level hook for Blood Absorption and Blood Projection interactions.
 * Implement this on blocks that should behave like ritual blood conduits,
 * reservoirs, wounds, or future block-bound blood sources without requiring a
 * tile blood-volume attachment.
 */
public interface BlockBloodEndpoint {

	double absorbBloodFromBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount);

	double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount);
}
