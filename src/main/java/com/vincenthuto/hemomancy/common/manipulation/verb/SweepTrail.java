package com.vincenthuto.hemomancy.common.manipulation.verb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The last cell a swept Sculpting worked, per player, so a sweep faster than the pulse still leaves an unbroken line
 * (the Glacial and Ferric Ramparts, core-loop round two P4). Session state keyed by player UUID; cleared through
 * {@code TransientServerStateEvents}. Each mark belongs to the Sculpting that made it, so switching memories never
 * joins one wall to another.
 */
public final class SweepTrail {
	private static final Map<UUID, Mark> LAST = new HashMap<>();

	private record Mark(VerbSculpting sculpting, BlockPos cell) {
	}

	private SweepTrail() {
	}

	/**
	 * The cells {@code sculpting} works this pulse: those between its last cell and {@code cell} (at most {@code join}
	 * apart), then {@code cell}, which becomes the last cell.
	 */
	public static List<BlockPos> sweep(Player player, VerbSculpting sculpting, BlockPos cell, int join) {
		List<BlockPos> cells = new ArrayList<>();
		last(player, sculpting).ifPresent(last -> cells.addAll(SculptingRules.sweepBetween(last, cell, join)));
		cells.add(cell.immutable());
		LAST.put(player.getUUID(), new Mark(sculpting, cell.immutable()));
		return cells;
	}

	/** The last cell {@code sculpting} worked for the player in this sweep. */
	public static Optional<BlockPos> last(Player player, VerbSculpting sculpting) {
		Mark mark = LAST.get(player.getUUID());
		return mark != null && mark.sculpting() == sculpting ? Optional.of(mark.cell()) : Optional.empty();
	}

	/** The sweep is over: the next cell starts a new line. */
	public static void forget(Player player) {
		LAST.remove(player.getUUID());
	}

	public static void clearSessionState() {
		LAST.clear();
	}

	public static void logout(Player player) {
		LAST.remove(player.getUUID());
	}
}
