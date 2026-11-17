package com.vincenthuto.hemomancy.common.util;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.VeinMinerHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;

/**
 * A block edit a player causes in the world, held to the same rules as their hands: game mode, world border, spawn
 * protection, and the NeoForge break or place event that claim mods cancel. Every player-caused {@code setBlock} or
 * {@code destroyBlock} under {@code common/manipulation}, {@code common/network}, {@code common/event},
 * {@code common/item}, {@code common/block}, {@code common/tile}, {@code common/rite}, {@code common/entity} and
 * {@code common/world} goes through here. Each raw edit site left in those trees is counted per file, with the reason
 * it is not a player's, in {@code architecture/player-block-edit-allow-list.txt} ({@code PlayerBlockEditGuardrailTest});
 * a guarded call in a file does not excuse its other raw edits. Something acting for a player who is not standing at
 * the block (a hot or cold gouge run, a flail's frost, a rite's conversions) edits for that player here too, and only
 * while that player is in the block's level, since these methods write into the player's own level.
 */
public final class PlayerBlockEdits {
	/** Depth of break events fired as a check only; server thread. */
	private static int checkingBreak;

	private PlayerBlockEdits() {
	}

	/**
	 * True while a break event fired here is only a check ({@link #mayBreak}, {@link #replace}, {@link #lift}): no
	 * ordinary break with drops follows, so listeners that answer a real break (vein mining, Laboring Arms, link
	 * cleanup) let it pass. Listeners that protect blocks still see it and may cancel.
	 */
	public static boolean isCheckingBreak() {
		return checkingBreak > 0;
	}

	/** Whether {@code player} may break the block at {@code pos}; fires the break event, which may cancel. */
	public static boolean mayBreak(Player player, BlockPos pos) {
		checkingBreak++;
		try {
			return breakAllowed(player, pos);
		} finally {
			checkingBreak--;
		}
	}

	private static boolean breakAllowed(Player player, BlockPos pos) {
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

	/**
	 * Breaks the block at {@code pos} as {@code player} would with {@code tool}: the break event first, then the
	 * block's own break callbacks, then its drops worked out for {@code tool} (none in Creative, none when the block
	 * needs a better tool). The tool is a context for the drops only; it is not damaged.
	 */
	public static boolean breakBlock(Player player, BlockPos pos, ItemStack tool) {
		if (!(player instanceof ServerPlayer server) || !(server.level() instanceof ServerLevel level)) return false;
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !ManipulationCombatHelper.mayAlter(server, pos) || !breakAllowed(server, pos)) return false;
		BlockEntity blockEntity = level.getBlockEntity(pos);
		BlockState destroyed = state.getBlock().playerWillDestroy(level, pos, state, server);
		boolean drops = !server.isCreative()
				&& (!destroyed.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(destroyed));
		if (!destroyed.onDestroyedByPlayer(level, pos, server, drops, level.getFluidState(pos))) return false;
		destroyed.getBlock().destroy(level, pos, destroyed);
		if (drops) destroyed.getBlock().playerDestroy(level, server, pos, destroyed, blockEntity, tool);
		return true;
	}

	/**
	 * Turns the block at {@code pos} into {@code newState} in place: the old block must pass the break checks and the
	 * new one the place checks. Nothing drops. A cancelled place puts the old block back.
	 */
	public static boolean replace(Player player, BlockPos pos, BlockState newState, int flags) {
		return player instanceof ServerPlayer server && server.level() instanceof ServerLevel
				&& ManipulationCombatHelper.mayAlter(server, pos) && mayBreak(server, pos)
				&& place(server, pos, newState, flags);
	}

	/** {@link #replace} with the usual client-and-neighbour update flags. */
	public static boolean replace(Player player, BlockPos pos, BlockState newState) {
		return replace(player, pos, newState, Block.UPDATE_ALL);
	}

	/**
	 * Takes the block at {@code pos} out of the world without drops, after the break checks, and returns what was
	 * there so {@link #settle} can put it back. Refuses air and anything with a block entity, whose contents a lift
	 * would lose. Returns null when nothing was lifted.
	 */
	@Nullable
	public static BlockState lift(Player player, BlockPos pos) {
		if (!(player instanceof ServerPlayer server) || !(server.level() instanceof ServerLevel level)) return null;
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || level.getBlockEntity(pos) != null || !ManipulationCombatHelper.mayAlter(server, pos)
				|| !mayBreak(server, pos)) {
			return null;
		}
		return level.removeBlock(pos, false) ? state : null;
	}

	/**
	 * Puts a lifted {@code state} back at {@code pos}, through the place checks, when the spot is free (air or
	 * something replaceable such as grass or water).
	 */
	public static boolean settle(Player player, BlockPos pos, BlockState state) {
		if (!(player instanceof ServerPlayer server) || !(server.level() instanceof ServerLevel level)
				|| !level.getBlockState(pos).canBeReplaced()) {
			return false;
		}
		return place(server, pos, state);
	}
}
