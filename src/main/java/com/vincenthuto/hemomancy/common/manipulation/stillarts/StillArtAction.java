package com.vincenthuto.hemomancy.common.manipulation.stillarts;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StillArtAction {
	StillArtAction NOOP = (player, level, heldItem, position, art) -> false;

	boolean cast(ServerPlayer player, ServerLevel level, ItemStack heldItem, BlockPos position, StillArt art);
}
