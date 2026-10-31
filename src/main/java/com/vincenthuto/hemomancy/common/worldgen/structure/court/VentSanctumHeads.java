package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.IntBinaryOperator;

/**
 * What the Vent Sanctum adds to the terrain's chimneys, which stay exactly as the terrain built them: a Hydra Crown on
 * every mouth, offering basins at their feet, and the threshold, a walkway from the processional ring up to the mouth
 * of the tallest chimney, where a member at the end of life enters. Pure: chimneys and a floor lookup in, block spots
 * out. {@code CourtStructure.afterPlace} writes them, and only into water, so a chimney block is never touched.
 */
public final class VentSanctumHeads {
	public enum Kind { CROWN, BASIN, WALK, WALK_TOP, POST, LIGHT }

	public record Spot(BlockPos pos, Kind kind, Direction facing) {
	}

	/** The ring's inner edge, where the threshold walkway starts. Matches sanctum.py's RING. */
	static final double RING_EDGE = 13;

	private VentSanctumHeads() {
	}

	/** The threshold chimney: the tallest, the first of equals. */
	public static PelagicLandmarks.Chimney threshold(List<PelagicLandmarks.Chimney> chimneys) {
		PelagicLandmarks.Chimney best = chimneys.get(0);
		for (var chimney : chimneys) if (chimney.height() > best.height()) best = chimney;
		return best;
	}

	/**
	 * Every spot round the heads. {@code floorY} gives the seabed's top block at a column; {@code walkY} is the
	 * processional ring's walking surface (the marker row).
	 */
	public static List<Spot> plan(BlockPos centre, List<PelagicLandmarks.Chimney> chimneys, IntBinaryOperator floorY,
			int walkY) {
		if (chimneys.isEmpty()) return List.of();
		var spots = new LinkedHashMap<BlockPos, Spot>();
		var threshold = threshold(chimneys);
		for (var chimney : chimneys) {
			var pos = new BlockPos(chimney.x(), chimney.mouthY() + 1, chimney.z());
			spots.put(pos, new Spot(pos, Kind.CROWN, Direction.NORTH));
		}
		for (var chimney : chimneys) {
			Direction out = outward(centre, chimney);
			var sides = chimney == threshold ? List.of(out.getClockWise(), out.getCounterClockWise())
					: List.of(out.getOpposite());
			for (Direction side : sides) {
				int bx = chimney.x() + side.getStepX() * 2, bz = chimney.z() + side.getStepZ() * 2;
				var pos = new BlockPos(bx, floorY.applyAsInt(bx, bz) + 1, bz);
				spots.putIfAbsent(pos, new Spot(pos, Kind.BASIN, side.getOpposite()));
			}
		}
		walkway(centre, threshold, floorY, walkY, spots);
		return new ArrayList<>(spots.values());
	}

	/** From the ring's inner edge straight in toward the threshold, rising to its mouth, three wide. */
	private static void walkway(BlockPos centre, PelagicLandmarks.Chimney threshold, IntBinaryOperator floorY, int walkY,
			LinkedHashMap<BlockPos, Spot> spots) {
		double ux = threshold.x() - centre.getX(), uz = threshold.z() - centre.getZ();
		double length = Math.hypot(ux, uz);
		if (length < 1.5) {
			ux = 1;
			uz = 0;
		} else {
			ux /= length;
			uz /= length;
		}
		double sx = centre.getX() + ux * RING_EDGE, sz = centre.getZ() + uz * RING_EDGE;
		double ex = threshold.x() + ux * 2.5, ez = threshold.z() + uz * 2.5;
		double run = Math.hypot(ex - sx, ez - sz);
		int steps = Math.max(1, (int) Math.ceil(run * 2));
		int mouth = threshold.mouthY();
		for (int i = 0; i <= steps; i++) {
			double t = i / (double) steps;
			double px = sx + (ex - sx) * t, pz = sz + (ez - sz) * t;
			int top = (int) Math.round(walkY + (mouth - walkY) * t);
			for (int w = -1; w <= 1; w++) {
				int bx = (int) Math.floor(px - uz * w + .5), bz = (int) Math.floor(pz + ux * w + .5);
				int floor = floorY.applyAsInt(bx, bz);
				for (int y = floor + 1; y <= top; y++) {
					var pos = new BlockPos(bx, y, bz);
					var kind = y == top ? Kind.WALK_TOP : Kind.WALK;
					var old = spots.get(pos);
					if (old == null || old.kind() == Kind.WALK) spots.put(pos, new Spot(pos, kind, Direction.NORTH));
				}
				if (w != 0 && i % 6 == 3) {
					var post = new BlockPos(bx, top + 1, bz);
					spots.putIfAbsent(post, new Spot(post, Kind.POST, Direction.NORTH));
					var light = post.above();
					spots.putIfAbsent(light, new Spot(light, Kind.LIGHT, Direction.NORTH));
				}
			}
		}
	}

	/** The compass side of a chimney facing away from the field's centre (east for a chimney on the centre). */
	static Direction outward(BlockPos centre, PelagicLandmarks.Chimney chimney) {
		int dx = chimney.x() - centre.getX(), dz = chimney.z() - centre.getZ();
		if (dx == 0 && dz == 0) return Direction.EAST;
		if (Math.abs(dx) >= Math.abs(dz)) return dx > 0 ? Direction.EAST : Direction.WEST;
		return dz > 0 ? Direction.SOUTH : Direction.NORTH;
	}
}
