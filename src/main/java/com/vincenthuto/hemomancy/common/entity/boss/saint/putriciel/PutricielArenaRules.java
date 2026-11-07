package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

/**
 * What Putriciel's fight does to its chamber (design section 9): the fire scorches floor cells to packed ash (48 an
 * interval, 160 standing, the oldest reverting first past that), the Bloom of Rot spares the ash, the pyre circles and
 * the body, and reclaims the empty ash after its last pulse; the Downfall faithful rise from the bog bodies. Cells are
 * column keys ({@code BossFootprint.cell}). Pure, so it is JVM-tested; the entity makes every edit through
 * {@code SaintArena}.
 */
public final class PutricielArenaRules {
	public static final String SCORCH_BUDGET = "putriciel_scorch";
	public static final int SCORCH_PER_INTERVAL = 48;
	/** The scorch budget's window: about one interval. */
	public static final int SCORCH_WINDOW = 600;
	public static final int SCORCH_RECORDED = 160;
	/** Cells within this of a pyre's centre and of the body stay clear of the Bloom. */
	public static final double PYRE_SAFE = 1.5D;
	public static final double BODY_SAFE = 1.2D;
	public static final double BLOOM_RADIUS = 12.0D;
	/** Per move: Purging Sweep 8, Censer Cast 12, Cauterising Lane 12, Pyre Lines 16. */
	public static final int SWEEP_SCORCH = 8, CAST_SCORCH = 12, LANE_SCORCH = 12, LINES_SCORCH = 16;
	public static final int FAITHFUL_ALIVE = 2;
	public static final int FAITHFUL_PER_FIGHT = 8;
	/** How far round the body pyres feed Pyre Lines and Cauterising Lane. */
	public static final double PYRE_REACH = 20.0D;
	public static final int MAX_PYRE_LINES = 10;
	/**
	 * The second budget (revision 2026-11-06, R2.5): the ash a burned sludge pool leaves, 8 cells a pool, 24 a
	 * 600-tick window, 64 standing. It is ordinary fight ash: the Bloom's safe ground, reclaimed with the rest.
	 */
	public static final String SLUDGE_ASH_BUDGET = "putriciel_sludge_ash";
	public static final int SLUDGE_ASH_PER_POOL = 8, SLUDGE_ASH_PER_WINDOW = 24, SLUDGE_ASH_WINDOW = 600,
			SLUDGE_ASH_RECORDED = 64;

	private PutricielArenaRules() {
	}

	/**
	 * The Bloom's safe cells inside {@code radius} of {@code centre}: scorched ash, cells within {@link #PYRE_SAFE} of a
	 * pyre, and cells within {@link #BODY_SAFE} of the body. The Bloom's {@code Region} footprint excludes them.
	 */
	public static LongSet safeCells(LongSet ash, List<Vec3> pyres, Vec3 body, Vec3 centre, double radius) {
		LongSet safe = new LongOpenHashSet();
		int x0 = Mth.floor(centre.x - radius), x1 = Mth.floor(centre.x + radius);
		int z0 = Mth.floor(centre.z - radius), z1 = Mth.floor(centre.z + radius);
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double cx = x + 0.5D, cz = z + 0.5D;
				if (Math.hypot(cx - centre.x, cz - centre.z) > radius + 0.75D) continue;
				long cell = BossFootprint.cell(x, z);
				if (ash.contains(cell) || Math.hypot(cx - body.x, cz - body.z) <= BODY_SAFE || nearPyre(cx, cz, pyres)) {
					safe.add(cell);
				}
			}
		}
		return safe;
	}

	/**
	 * As {@link #safeCells(LongSet, List, Vec3, Vec3, double)}, but no cell under a live sludge pool ({@code pooled})
	 * is safe: the pools are part of the rot. Ash is never under a pool, so the ash stays safe.
	 */
	public static LongSet safeCells(LongSet ash, List<Vec3> pyres, Vec3 body, Vec3 centre, double radius, LongSet pooled) {
		LongSet safe = safeCells(ash, pyres, body, centre, radius);
		for (long cell : pooled) if (!ash.contains(cell)) safe.remove(cell);
		return safe;
	}

	private static boolean nearPyre(double x, double z, List<Vec3> pyres) {
		for (Vec3 pyre : pyres) if (Math.hypot(x - pyre.x, z - pyre.z) <= PYRE_SAFE) return true;
		return false;
	}

	/** The ash cells the rot takes back after the Bloom: every one nobody stands on. */
	public static LongList reclaim(Collection<Long> ash, LongSet occupied) {
		LongList taken = new LongArrayList();
		for (long cell : ash) if (!occupied.contains(cell)) taken.add(cell);
		return taken;
	}

	/** Cells along a ground segment from {@code a} to {@code b}, in order, at most {@code cap}. */
	public static LongList lineCells(Vec3 a, Vec3 b, double halfWidth, int cap) {
		LongLinkedOpenHashSet cells = new LongLinkedOpenHashSet();
		double length = Math.hypot(b.x - a.x, b.z - a.z);
		int steps = Math.max(1, (int) Math.ceil(length * 2.0D));
		double sx = length < 1.0E-6D ? 0.0D : -(b.z - a.z) / length, sz = length < 1.0E-6D ? 0.0D : (b.x - a.x) / length;
		for (int i = 0; i <= steps && cells.size() < cap; i++) {
			double t = i / (double) steps;
			double x = a.x + (b.x - a.x) * t, z = a.z + (b.z - a.z) * t;
			for (double off = -halfWidth; off <= halfWidth + 1.0E-6D && cells.size() < cap; off += 0.5D) {
				cells.add(BossFootprint.cell(Mth.floor(x + sx * off), Mth.floor(z + sz * off)));
			}
		}
		return new LongArrayList(cells);
	}

	/** Cells under a disc, nearest the centre first, at most {@code cap}. */
	public static LongList discCells(Vec3 centre, double radius, int cap) {
		LongArrayList cells = new LongArrayList();
		int r = (int) Math.ceil(radius);
		for (int ring = 0; ring <= r && cells.size() < cap; ring++) {
			for (int dx = -ring; dx <= ring && cells.size() < cap; dx++) {
				for (int dz = -ring; dz <= ring && cells.size() < cap; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
					int x = Mth.floor(centre.x) + dx, z = Mth.floor(centre.z) + dz;
					if (Math.hypot(x + 0.5D - centre.x, z + 0.5D - centre.z) <= radius) cells.add(BossFootprint.cell(x, z));
				}
			}
		}
		return cells;
	}

	/** Whether another faithful may rise: fewer than two alive and fewer than eight raised this fight. */
	public static boolean faithfulMayRise(int alive, int raised) {
		return alive < FAITHFUL_ALIVE && raised < FAITHFUL_PER_FIGHT;
	}
}
