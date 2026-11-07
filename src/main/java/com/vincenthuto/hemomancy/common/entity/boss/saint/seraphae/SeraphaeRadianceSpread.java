package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * "The chamber floods with the light she emits" (design section 9): as she loses control the healed stone the theme
 * grew in her chamber turns to glowing {@code radiant_healed_quartz}, nearest to her first: 24 cells at phase II, 48
 * more at phase III, 24 more at each Radiance Flood, at most 96. Every cell goes through the arena, which restores it
 * from the walls inward when she is contained.
 */
public final class SeraphaeRadianceSpread {
	public static final String SCAN_KEY = "seraphae_healed_stone";
	public static final int PHASE_TWO = 24;
	public static final int PHASE_THREE = 48;
	public static final int FLOOD = 24;
	public static final int MAX = 96;

	private SeraphaeRadianceSpread() {
	}

	/** How many cells the spread may still turn, given how many it has. */
	public static int allowance(int turned, int wanted) {
		return Math.max(0, Math.min(wanted, MAX - turned));
	}

	public static boolean healedStone(BlockState state) {
		return state.is(BlockInit.healed_quartz_bricks.get()) || state.is(BlockInit.healed_calcite.get());
	}

	/** Turns up to {@code wanted} more healed cells nearest {@code from}; returns how many turned. */
	public static int spread(SaintArena arena, Vec3 from, int turned, int wanted) {
		int allowance = allowance(turned, wanted);
		if (allowance <= 0 || !arena.chamberFound()) return 0;
		List<BlockPos> cells = new ArrayList<>(arena.find(SCAN_KEY, SeraphaeRadianceSpread::healedStone));
		cells.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(from)));
		BlockState radiant = BlockInit.radiant_healed_quartz.get().defaultBlockState();
		int done = 0;
		for (BlockPos pos : cells) {
			if (done >= allowance) break;
			if (arena.changed(pos)) continue;
			if (arena.set(pos, radiant)) done++;
		}
		return done;
	}
}
