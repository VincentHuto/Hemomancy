package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.Optional;

/**
 * Bounded Seat search without {@code /locate}: walk the Seat set's spacing cells outward in square rings, test each
 * cell's potential chunk with the placement predicate against the biome source and sampler only, and keep the
 * nearest. No chunk is loaded. Callers that must never stall the server run it off the server thread.
 */
public final class CourtSeatCandidates {
	public static final ResourceLocation SEATS = Hemomancy.rloc("brined_court_seats");
	public static final ResourceLocation OUTPOSTS = Hemomancy.rloc("brined_court_outposts");

	private CourtSeatCandidates() {
	}

	/** The nearest Lighthouse Garden within {@code maxRings} Seat cells of {@code from}. */
	public static Optional<BlockPos> nearest(ServerLevel level, BlockPos from, int maxRings) {
		return nearest(level, CourtSite.LIGHTHOUSE_GARDEN, from, maxRings);
	}

	public static Optional<BlockPos> nearest(ServerLevel level, CourtSite site, BlockPos from, int maxRings) {
		if (site.tier() != CourtSite.Tier.SEAT) throw new IllegalArgumentException(site + " is not a Seat");
		var source = level.getChunkSource();
		var probe = CourtWorldProbe.of(level.registryAccess(), source.getGenerator().getBiomeSource(), source.randomState(),
				level.getSeed());
		var set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(SEATS);
		if (probe == null || set == null || !(set.placement() instanceof RandomSpreadStructurePlacement placement))
			return Optional.empty();
		return search(probe, CourtLandmarkCells.Spread.of(placement, level.getSeed(), 0, 0), level.getSeed(), site,
				from.getX(), from.getZ(), maxRings);
	}

	/**
	 * Ring 0 is the cell holding (x, z). Once a candidate is found the search finishes any ring that could still hold a
	 * nearer one: every point of ring r + 1 lies at least r cell widths away.
	 */
	public static Optional<BlockPos> search(CourtPlacementRules.Probe probe, CourtLandmarkCells.Spread seats,
			long seed, CourtSite site, int x, int z, int maxRings) {
		int spacing = seats.spacing();
		long cellBlocks = spacing * 16L;
		int originX = Math.floorDiv(Math.floorDiv(x, 16), spacing), originZ = Math.floorDiv(Math.floorDiv(z, 16), spacing);
		BlockPos best = null;
		long bestDistance = Long.MAX_VALUE;
		for (int ring = 0; ring <= maxRings; ring++) {
			if (best != null && bestDistance <= (ring - 1) * cellBlocks * (ring - 1) * cellBlocks) break;
			for (int cellX = originX - ring; cellX <= originX + ring; cellX++) for (int cellZ = originZ - ring; cellZ <= originZ + ring; cellZ++) {
				if (Math.max(Math.abs(cellX - originX), Math.abs(cellZ - originZ)) != ring) continue;
				var chunk = seats.potential().of(cellX, cellZ);
				var stub = CourtPlacementRules.stub(site, probe, seed, chunk.getMiddleBlockX(), chunk.getMiddleBlockZ());
				if (stub.isEmpty()) continue;
				long dx = stub.get().getX() - x, dz = stub.get().getZ() - z;
				long distance = dx * dx + dz * dz;
				if (distance < bestDistance) {
					best = stub.get();
					bestDistance = distance;
				}
			}
		}
		return Optional.ofNullable(best);
	}
}
