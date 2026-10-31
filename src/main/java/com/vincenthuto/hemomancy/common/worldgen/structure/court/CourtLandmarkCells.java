package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Landmarks as their structure sets could place them: each set's potential chunk per spacing cell, from its own
 * placement math. No structure start is read, so the answer is the same in any generation order. Potential cells
 * over-reject; the caller's filter drops cells where no structure of that set could stand.
 */
public final class CourtLandmarkCells {
	/** Other factions' landmark sets. Court outposts also keep clear of the Seats; Seats need nothing more. */
	public static final List<ResourceLocation> EXCLUDED_SETS = List.of(
			Hemomancy.rloc("circus_pavilion"),
			Hemomancy.rloc("troupe_teaching_camps"),
			Hemomancy.rloc("troupe_abandoned_stages"),
			Hemomancy.rloc("harbinger_voyager_wreck"),
			Hemomancy.rloc("harbinger_voyager_vessel"),
			Hemomancy.rloc("mausoleum"),
			Hemomancy.rloc("saint_mausoleums"),
			ResourceLocation.withDefaultNamespace("ocean_monuments"));
	/**
	 * Another faction's structure is measured from its potential chunk's middle, but its jigsaw start sits at the
	 * chunk's corner and its centre follows its template; this much extra distance covers that.
	 */
	public static final int FACTION_SLACK = 16;

	/**
	 * A random-spread set as cells: its spacing in chunks, the potential chunk of each cell, how far a landmark can
	 * stand from that chunk's middle ({@code reach}), and how far its centre may differ from the position the filter
	 * reports ({@code slack}).
	 */
	public record Spread(int spacing, PotentialChunk potential, int reach, int slack) {
		@FunctionalInterface
		public interface PotentialChunk {
			ChunkPos of(int cellX, int cellZ);
		}

		/** Vanilla's own potential-chunk math for the world seed. */
		public static Spread of(RandomSpreadStructurePlacement placement, long seed, int reach, int slack) {
			int spacing = placement.spacing();
			return new Spread(spacing, (cellX, cellZ) ->
					placement.getPotentialStructureChunk(seed, cellX * spacing, cellZ * spacing), reach, slack);
		}
	}

	/** Where a structure of set {@code set} would stand for this potential chunk, or null if none could. */
	@FunctionalInterface
	public interface LandmarkAt {
		@Nullable BlockPos at(int set, ChunkPos chunk);
	}

	private CourtLandmarkCells() {
	}

	/** Whether any listed set has a landmark within {@code radius} (plus that set's slack) of (x, z). */
	public static boolean within(List<Spread> spreads, LandmarkAt landmarks, int x, int z, int radius) {
		for (int set = 0; set < spreads.size(); set++) {
			var spread = spreads.get(set);
			int spacing = spread.spacing();
			long limit = (long) (radius + spread.slack()) * (radius + spread.slack());
			int scan = radius + spread.slack() + spread.reach();
			long scanLimit = (long) scan * scan;
			int minCellX = Math.floorDiv(Math.floorDiv(x - scan, 16), spacing);
			int maxCellX = Math.floorDiv(Math.floorDiv(x + scan, 16), spacing);
			int minCellZ = Math.floorDiv(Math.floorDiv(z - scan, 16), spacing);
			int maxCellZ = Math.floorDiv(Math.floorDiv(z + scan, 16), spacing);
			for (int cellX = minCellX; cellX <= maxCellX; cellX++) for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
				ChunkPos chunk = spread.potential().of(cellX, cellZ);
				long cx = chunk.getMiddleBlockX() - x, cz = chunk.getMiddleBlockZ() - z;
				if (cx * cx + cz * cz > scanLimit) continue;
				BlockPos landmark = landmarks.at(set, chunk);
				if (landmark == null) continue;
				long dx = landmark.getX() - x, dz = landmark.getZ() - z;
				if (dx * dx + dz * dz <= limit) return true;
			}
		}
		return false;
	}
}
