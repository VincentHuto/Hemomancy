package com.vincenthuto.hemomancy.common.util;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.VeinMinerHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

/**
 * A block edit a player's cast or payload makes in the world, held to the same rules as their hands: game mode,
 * world border, spawn protection, and the NeoForge break or place event that claim mods cancel. Every player-caused
 * {@code setBlock} or {@code destroyBlock} under {@code common/manipulation} and {@code common/network} goes through
 * here ({@code PlayerBlockEditGuardrailTest}).
 */
public final class PlayerBlockEdits {
	private PlayerBlockEdits() {
	}

	/** Whether {@code player} may break the block at {@code pos}; fires the break event, which may cancel. */
	public static boolean mayBreak(Player player, BlockPos pos) {
		return player instanceof ServerPlayer server && VeinMinerHelper.hasBreakPermission(server, pos);
	}

	/**
	 * Places {@code state} at {@code pos} for {@code player}, or leaves the world alone and returns false when the
	 * player may not build there or a place-event listener cancels it.
	 */
	public static boolean place(Player player, BlockPos pos, BlockState state, int flags) {
		if (!(player instanceof ServerPlayer server) || !(server.level() instanceof ServerLevel level)
				|| !ManipulationCombatHelper.mayAlter(server, pos)) {
			return false;
		}
		BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos, flags);
		if (!level.setBlock(pos, state, flags)) return false;
		if (EventHooks.onBlockPlace(server, before, Direction.UP)) {
			before.restore(flags);
			return false;
		}
		return true;
	}

	/** {@link #place} with the usual client-and-neighbour update flags. */
	public static boolean place(Player player, BlockPos pos, BlockState state) {
		return place(player, pos, state, Block.UPDATE_ALL);
	}
}
