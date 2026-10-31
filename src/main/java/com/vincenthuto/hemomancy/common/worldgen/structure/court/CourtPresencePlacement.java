package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.CourtFacingFixtureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.PetrifiedFollowerBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.List;

/**
 * Lays Marimo's presence (Brined Court Track 10) onto the Lighthouse Garden's undercroft at its
 * {@code court_mark/marimo_presence} mark, one chunk at a time: a low dome of Marimo Moss over the pool's island with
 * Marimo's Heart at its crown and three petrified followers sunk into its flanks, runners of moss across the pool floor
 * out to the shaft wall, and threads of it climbing the wall through every tier. Only wall and floor moss or stone is
 * replaced, never air or water, so the undercroft's dry and wet cells stay as Track 7 built them.
 *
 * <p>Layout, in the undercroft's own frame (the mark stands on the island, a block above its top): the island top is
 * one below the mark, the pool floor three below, and the coliseum's ceiling eighteen above.
 */
public final class CourtPresencePlacement {
	public static final String MARIMO_MARK = "marimo_presence";
	/** The dome covers the island, whose moss reaches this far from the mark. */
	static final double DOME_RADIUS = 3.5D;
	static final double DOME_HEIGHT = 3.4D;
	/** The threads climb the shaft wall at this radius, from the pool floor to the top gallery. */
	static final double THREAD_RADIUS = 10.7D;
	static final int FLOOR = -3;
	static final int THREAD_TOP = 15;
	static final int THREADS = 6;
	/** Followers sunk into the dome's flanks: offsets from the mark, each facing out from the heart. */
	static final int[][] FOLLOWERS = { { 3, 0 }, { -2, 2 }, { -1, -3 } };

	private CourtPresencePlacement() {
	}

	public static void place(WorldGenLevel level, BoundingBox chunkBox, PiecesContainer pieces) {
		List<StructurePiece> here = pieces.pieces().stream()
				.filter(piece -> piece.getBoundingBox().intersects(chunkBox)).toList();
		if (here.isEmpty()) return;
		for (var marker : CourtResidentMarkers.read(level.getLevel().getStructureManager(), here)) {
			if (marker.resident() || !marker.name().equals(MARIMO_MARK)) continue;
			marimo(level, chunkBox, marker.pos());
		}
	}

	static void marimo(WorldGenLevel level, BoundingBox chunkBox, BlockPos mark) {
		BlockState moss = BlockInit.marimo_moss.get().defaultBlockState();
		// The dome over the island; the heart at its crown.
		int top = 0;
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				double r = Math.hypot(dx, dz);
				if (r >= DOME_RADIUS) continue;
				// Lumps on the flanks, so the mass reads as grown, not stepped.
				int height = Math.min(domeHeight(0), domeHeight(r) + (r > 1.5D && Math.floorMod(dx * 31 + dz * 17, 3) == 0 ? 1 : 0));
				top = Math.max(top, height);
				for (int dy = -1; dy < height; dy++) set(level, chunkBox, mark.offset(dx, dy, dz), moss);
			}
		}
		set(level, chunkBox, mark.above(top), BlockInit.marimo_heart.get().defaultBlockState());
		for (int[] spot : FOLLOWERS) {
			Direction out = Direction.getNearest(spot[0], 0, spot[1]);
			set(level, chunkBox, mark.offset(spot[0], 0, spot[1]), BlockInit.petrified_follower.get().defaultBlockState()
					.setValue(CourtFacingFixtureBlock.FACING, out)
					.setValue(PetrifiedFollowerBlock.MOSS, PetrifiedFollowerBlock.Moss.WHOLE));
		}
		// Runners over the pool floor out to the wall, then threads up the wall through the tiers.
		for (int k = 0; k < THREADS; k++) {
			double angle = Math.toRadians(k * 360.0D / THREADS + 15.0D);
			for (double r = DOME_RADIUS; r <= THREAD_RADIUS; r += 0.5D) {
				replaceGrowth(level, chunkBox, mark.offset((int) Math.round(r * Math.cos(angle)), FLOOR,
						(int) Math.round(r * Math.sin(angle))), moss);
			}
			for (int dy = FLOOR; dy <= THREAD_TOP; dy++) {
				double wander = angle + Math.sin(dy * 0.55D + k) * 0.07D;
				replaceGrowth(level, chunkBox, mark.offset((int) Math.round(THREAD_RADIUS * Math.cos(wander)), dy,
						(int) Math.round(THREAD_RADIUS * Math.sin(wander))), moss);
			}
		}
	}

	/** How many blocks the dome stands above the island top at distance {@code r} from the mark. */
	static int domeHeight(double r) {
		return (int) Math.floor(DOME_HEIGHT * Math.sqrt(Math.max(0.0D, 1.0D - (r * r) / (DOME_RADIUS * DOME_RADIUS))));
	}

	private static void set(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos, BlockState state) {
		if (chunkBox.isInside(pos)) level.setBlock(pos, state, 2);
	}

	/** Moss only takes what was already wall or floor: moss, stone bricks or moss carpet. */
	private static void replaceGrowth(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos, BlockState moss) {
		if (!chunkBox.isInside(pos)) return;
		BlockState at = level.getBlockState(pos);
		if (at.is(Blocks.MOSS_BLOCK) || at.is(Blocks.MOSSY_STONE_BRICKS) || at.is(Blocks.STONE_BRICKS)
				|| at.is(Blocks.CRACKED_STONE_BRICKS)) {
			level.setBlock(pos, moss, 2);
		}
	}
}
