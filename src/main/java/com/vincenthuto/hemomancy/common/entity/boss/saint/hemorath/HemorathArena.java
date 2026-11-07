package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChamberLayout;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Hemorath's chamber as his circulation (design section 9): one scan of the chamber for his four wells
 * ({@code blood_basin}), the two dais flank basins ({@code venous_basin}) and every {@code blood_gutter}, each gutter
 * cell sorted into the run it belongs to (the pit ring, its sump, a spoke to a well, the west channel and processional,
 * the east channel to the dais). Every fill it writes goes through the kit's {@link SaintArena}, which records the
 * original and puts it back; fronts are the arena's schedules. With no chamber (a test pad), there are no wells and the
 * pit is a 5-block circle round where he rose.
 */
public final class HemorathArena {
	public static final String WELLS = "hemorath_wells";
	public static final String GUTTERS = "hemorath_gutters";
	public static final String BASINS = "hemorath_basins";
	/** The pit's centre cell in chamber-local coordinates ({@code themes/hemorath.py} PIT), and its ring's radius. */
	public static final int PIT_X = 17;
	public static final int PIT_RADIUS = 5;
	/** The two spoke columns out to the wells (SPOKES). */
	public static final int[] SPOKES = {15, 19};
	/** Flank basins stand on the dais, from this local x. */
	public static final int DAIS_FROM_X = 26;
	/** Fills are re-read this often; the arena's own per-cell limit is 10 ticks. */
	public static final int UPDATE_EVERY = 10;
	/** Gutter writes notify clients only: a fill never changes how a gutter connects. */
	public static final int FILL_FLAGS = Block.UPDATE_CLIENTS;
	/** A wanted fill of LEAVE keeps the cell as it stands; REVERT puts the authored fill back. */
	public static final int LEAVE = -1;
	public static final int REVERT = -2;

	public enum Run { PIT, SUMP, SPOKE, WEST, PROCESSIONAL, EAST, OTHER }

	/** One gutter cell: its run, the well a spoke serves (-1 otherwise) and its distance out from the pit's ring. */
	public record Cell(BlockPos pos, Run run, int well, double fromPit) {
	}

	private final SaintArena arena;
	private final List<BlockPos> wells;
	private final List<Cell> gutters;
	private final List<BlockPos> flankBasins;
	private final Vec3 pit;

	private HemorathArena(SaintArena arena, List<BlockPos> wells, List<Cell> gutters, List<BlockPos> flankBasins, Vec3 pit) {
		this.arena = arena;
		this.wells = wells;
		this.gutters = gutters;
		this.flankBasins = flankBasins;
		this.pit = pit;
	}

	/** One scan, when the kit resolves the arena. {@code rise} is the fallback pit's centre. */
	public static HemorathArena scan(SaintArena arena, Vec3 rise) {
		if (!arena.chamberFound()) return new HemorathArena(arena, List.of(), List.of(), List.of(), rise);
		Vec3 pit = arena.toWorld(PIT_X + 0.5D, SaintChamberLayout.FLOOR_Y, SaintChamberLayout.ZC + 0.5D);
		List<BlockPos> wells = new ArrayList<>(arena.find(WELLS, state -> state.is(BlockInit.blood_basin.get())));
		// a stable order: the near aisle's pair first, each pair door-side first
		wells.sort(Comparator.<BlockPos>comparingDouble(pos -> local(arena, pos).z)
				.thenComparingDouble(pos -> local(arena, pos).x));
		if (wells.size() > HemorathWellRules.WELLS) wells = new ArrayList<>(wells.subList(0, HemorathWellRules.WELLS));
		List<BlockPos> basins = new ArrayList<>();
		for (BlockPos pos : arena.find(BASINS, state -> state.is(BlockInit.venous_basin.get()))) {
			if (local(arena, pos).x >= DAIS_FROM_X) basins.add(pos);
		}
		List<Cell> cells = new ArrayList<>();
		for (BlockPos pos : arena.find(GUTTERS, state -> state.is(BlockInit.blood_gutter.get()))) {
			cells.add(classify(arena, pos, wells));
		}
		return new HemorathArena(arena, List.copyOf(wells), List.copyOf(cells), List.copyOf(basins), pit);
	}

	private static Cell classify(SaintArena arena, BlockPos pos, List<BlockPos> wells) {
		Vec3 local = local(arena, pos);
		int x = Mth.floor(local.x), y = Mth.floor(local.y), z = Mth.floor(local.z);
		int zc = SaintChamberLayout.ZC;
		double ring = Math.hypot(x - PIT_X, z - zc);
		if (y >= SaintChamberLayout.ENTRY) return new Cell(pos, Run.PROCESSIONAL, -1, PIT_X - PIT_RADIUS + (PIT_X - x));
		if (x == PIT_X && z == zc) return new Cell(pos, Run.SUMP, -1, 0.0D);
		if (Math.abs(ring - PIT_RADIUS) <= 0.9D) return new Cell(pos, Run.PIT, -1, 0.0D);
		if (z == zc && x < PIT_X) return new Cell(pos, Run.WEST, -1, PIT_X - PIT_RADIUS - x);
		if (z == zc) return new Cell(pos, Run.EAST, -1, x - PIT_X - PIT_RADIUS);
		if ((x == SPOKES[0] || x == SPOKES[1]) && Math.abs(z - zc) > PIT_RADIUS) {
			int well = -1;
			for (int i = 0; i < wells.size(); i++) {
				Vec3 at = local(arena, wells.get(i));
				if (Mth.floor(at.x) == x && Math.signum(at.z - zc - 0.5D) == Math.signum(z - zc)) well = i;
			}
			return new Cell(pos, Run.SPOKE, well, Math.abs(z - zc) - PIT_RADIUS);
		}
		return new Cell(pos, Run.OTHER, -1, ring);
	}

	private static Vec3 local(SaintArena arena, BlockPos pos) {
		return arena.toLocal(Vec3.atBottomCenterOf(pos));
	}

	public List<BlockPos> wells() {
		return wells;
	}

	public Vec3 well(int index) {
		return Vec3.atBottomCenterOf(wells.get(index)).add(0.0D, 0.9D, 0.0D);
	}

	public List<Cell> gutters() {
		return gutters;
	}

	public List<BlockPos> flankBasins() {
		return flankBasins;
	}

	/** The pit's centre at standing height. */
	public Vec3 pit() {
		return pit;
	}

	public boolean inPit(Vec3 point) {
		double dx = point.x - pit.x, dz = point.z - pit.z;
		return dx * dx + dz * dz <= HemorathWellRules.PIT_RADIUS * HemorathWellRules.PIT_RADIUS;
	}

	/** The indices of the two wells nearest {@code point}, nearest first (fewer with fewer wells). */
	public int[] nearestWells(Vec3 point, int n) {
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < wells.size(); i++) order.add(i);
		order.sort(Comparator.comparingDouble(i -> Vec3.atCenterOf(wells.get(i)).distanceToSqr(point)));
		int[] out = new int[Math.min(n, order.size())];
		for (int i = 0; i < out.length; i++) out[i] = order.get(i);
		return out;
	}

	/**
	 * Sets each gutter to the fill {@code wanted} gives its cell (negative leaves it), and each flank basin to
	 * {@code basinFill} (negative leaves them). The arena limits a cell to one change per 10 ticks; a refused write is
	 * simply tried again on the next update.
	 */
	public void apply(ServerLevel level, java.util.function.ToIntFunction<Cell> wanted, int basinFill) {
		for (Cell cell : gutters) {
			int fill = wanted.applyAsInt(cell);
			if (fill == REVERT) {
				if (arena.changed(cell.pos())) arena.revert(cell.pos());
				continue;
			}
			if (fill < 0) continue;
			BlockState state = level.getBlockState(cell.pos());
			if (!state.is(BlockInit.blood_gutter.get()) || state.getValue(BloodGutterBlock.FILL) == fill) continue;
			arena.set(cell.pos(), state.setValue(BloodGutterBlock.FILL, Mth.clamp(fill, 0, 3)), FILL_FLAGS);
		}
		if (basinFill == REVERT) {
			for (BlockPos pos : flankBasins) if (arena.changed(pos)) arena.revert(pos);
			return;
		}
		if (basinFill < 0) return;
		for (BlockPos pos : flankBasins) {
			BlockState state = level.getBlockState(pos);
			if (!state.is(BlockInit.venous_basin.get()) || state.getValue(VenousBasinBlock.FILL) == basinFill) continue;
			arena.set(pos, state.setValue(VenousBasinBlock.FILL, Mth.clamp(basinFill, 0, 3)), FILL_FLAGS);
		}
	}

	/** A wet front: each listed cell fills to {@code fill} after the delay its cell gives. */
	public void front(ServerLevel level, List<Cell> cells, int fill, java.util.function.ToIntFunction<Cell> delay) {
		for (Cell cell : cells) {
			BlockState state = level.getBlockState(cell.pos());
			if (!state.is(BlockInit.blood_gutter.get())) continue;
			arena.schedule(cell.pos(), state.setValue(BloodGutterBlock.FILL, Mth.clamp(fill, 0, 3)),
					Math.max(0, delay.applyAsInt(cell)));
		}
	}

	/** The spoke cells that serve a well, far end first. */
	public List<Cell> spoke(int well) {
		List<Cell> cells = new ArrayList<>();
		for (Cell cell : gutters) if (cell.run() == Run.SPOKE && cell.well() == well) cells.add(cell);
		cells.sort(Comparator.comparingDouble(Cell::fromPit).reversed());
		return cells;
	}

	public double longestSpoke(int well) {
		double longest = 0.0D;
		for (Cell cell : gutters) if (cell.run() == Run.SPOKE && cell.well() == well) longest = Math.max(longest, cell.fromPit());
		return longest;
	}

	/** Every gutter, for the ending's flood. */
	public List<Cell> all() {
		return gutters;
	}
}
