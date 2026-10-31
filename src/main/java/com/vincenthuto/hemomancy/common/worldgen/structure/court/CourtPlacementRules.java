package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;

import java.util.Optional;

/**
 * Where a Brined Court site may stand. Every Court structure asks this first, in this order: the Pelagic gate, the
 * biome source at the house's band and the surface climate, the terrain sampler's floor at a few footprint points,
 * then other factions' potential cells. Nothing here loads a chunk or scans every column.
 */
public final class CourtPlacementRules {
	/** No two faction landmarks closer than this, horizontally. */
	public static final int LANDMARK_RADIUS = 96;
	/** The Vent Sanctum takes the nearest built vent field within this distance of its cell's chunk centre. */
	public static final int VENT_SEARCH_RADIUS = 72;
	/**
	 * The Lighthouse Garden's reef shelf is patchy, so it tries a grid of points this far around its cell's chunk
	 * centre, nearest first, and takes the first that passes.
	 */
	public static final int GARDEN_SEARCH_RADIUS = 48;
	private static final int GARDEN_SEARCH_STEP = 24;
	/** How far a Seat can stand from its cell's chunk centre, for anything that keeps clear of Seats. */
	public static final int SEAT_REACH = Math.max(VENT_SEARCH_RADIUS, (int) Math.ceil(GARDEN_SEARCH_RADIUS * Math.sqrt(2)));
	private static final int[][] GARDEN_OFFSETS = offsets(GARDEN_SEARCH_RADIUS, GARDEN_SEARCH_STEP);
	/** The Tide Refuge sits at the waterline: pools and benches, not the shelf below the shore. */
	public static final int SHORE_MIN_FLOOR = 56;
	public static final int SHORE_MAX_FLOOR = 68;
	/**
	 * The Wake nursery stands in open water on a bank or the upper slope: its floor at or above this, so its marker
	 * reads Pelagic Ocean or upper Twilight. Pelagic Ocean banks shallower than Y 34 alone never passed on the three
	 * review seeds (the few Pelagic Ocean cells are deep basins), so the 2026-10-30 review widened it to the upper
	 * slope under any ocean and let it search its cell; {@link CourtSite#WAKE_NURSERY}'s rarity keeps it rare.
	 */
	public static final int NURSERY_MIN_FLOOR = 18;
	/** Where the nursery's second habitat check samples: upper Twilight water, just above its lowest floor. */
	public static final int NURSERY_SLOPE_Y = 24;
	/**
	 * The nursery tries a grid of points this far round its cell's chunk centre, nearest first. It stays inside its
	 * start chunk: vanilla {@code /locate} reports the start chunk, and a small nursery 60 blocks off it under fog
	 * reads as nothing there.
	 */
	public static final int NURSERY_SEARCH_RADIUS = 8;
	private static final int NURSERY_SEARCH_STEP = 8;
	private static final int[][] NURSERY_OFFSETS = offsets(NURSERY_SEARCH_RADIUS, NURSERY_SEARCH_STEP);
	/** Shaping strength a footprint point needs; lower means vanilla or foreign terrain is taking over. */
	private static final double FOOTPRINT_INFLUENCE = .5;

	private CourtPlacementRules() {
	}

	/** The world as the predicate reads it: biome source, sampler columns and other sets' cells. */
	public interface Probe {
		/** The Pelagic layer the biome source gives this block, or null for any other biome. */
		@Nullable PelagicLayer layerAt(int x, int y, int z);

		PelagicTerrainSampler.Column column(int x, int z);

		/** Whether the surface climate above this column is cold or frozen. */
		boolean coldSurface(int x, int z);

		PelagicTerrainSampler terrain();

		/** Whether a landmark this site must keep clear of may stand within the radius. */
		boolean nearLandmark(CourtSite site, int x, int z, int radius);
	}

	/**
	 * The marker position for a site whose cell centre is ({@code x}, {@code z}): the first water block above the
	 * chosen floor. Empty when Pelagic worldgen is off ({@code probe} null) or any rule refuses.
	 */
	public static Optional<BlockPos> stub(CourtSite site, @Nullable Probe probe, long seed, int x, int z) {
		return screen(site, probe, seed, x, z)
				.filter(position -> !probe.nearLandmark(site, position.getX(), position.getZ(), LANDMARK_RADIUS));
	}

	/** Every rule except the landmark exclusion. */
	public static Optional<BlockPos> screen(CourtSite site, @Nullable Probe probe, long seed, int x, int z) {
		if (probe == null) return Optional.empty();
		if (site.rarity() > 1 && Math.floorMod(PelagicTerrainSampler.mix(seed ^ site.ordinal() * 0x9E3779B97F4A7C15L
				^ Math.floorDiv(x, 16) * 0x632BE59BD9B4E019L ^ Math.floorDiv(z, 16) * 0xD1B54A32D192ED03L), site.rarity()) != 0)
			return Optional.empty();
		if (site == CourtSite.VENT_SANCTUM) {
			var vent = ventAnchor(probe, x, z);
			return vent == null ? Optional.empty() : screenAt(site, probe, vent.x(), vent.z());
		}
		if (site == CourtSite.LIGHTHOUSE_GARDEN || site == CourtSite.WAKE_NURSERY) {
			for (int[] offset : site == CourtSite.WAKE_NURSERY ? NURSERY_OFFSETS : GARDEN_OFFSETS) {
				var found = screenAt(site, probe, x + offset[0], z + offset[1]);
				if (found.isPresent()) return found;
			}
			return Optional.empty();
		}
		return screenAt(site, probe, x, z);
	}

	private static Optional<BlockPos> screenAt(CourtSite site, Probe probe, int centreX, int centreZ) {
		if (!habitat(site, probe, centreX, centreZ)) return Optional.empty();
		var centre = probe.column(centreX, centreZ);
		if (centre.influence() < .999) return Optional.empty();
		int stubY = centre.floorY() + 1;
		if (site.layer() == PelagicLayer.SHORE && (centre.floorY() < SHORE_MIN_FLOOR || centre.floorY() > SHORE_MAX_FLOOR))
			return Optional.empty();
		if (site == CourtSite.WAKE_NURSERY && centre.floorY() < NURSERY_MIN_FLOOR) return Optional.empty();
		// The marker block and the jigsaw start above it must both read as the house's biome (vanilla checks the start).
		if (!markerLayer(site, probe.layerAt(centreX, stubY, centreZ))
				|| !markerLayer(site, probe.layerAt(centreX, stubY + 1, centreZ)))
			return Optional.empty();
		if (!levelFootprint(probe, centreX, centreZ, site.footprintRadius(), site.maxFloorVariation())) return Optional.empty();
		return Optional.of(new BlockPos(centreX, stubY, centreZ));
	}

	/**
	 * Whether a site's marker may stand in this layer: its own, or for the Wake nursery the upper Twilight under a
	 * Pelagic Ocean surface too (the nursery's structure biome tag lists both).
	 */
	public static boolean markerLayer(CourtSite site, @Nullable PelagicLayer layer) {
		return layer == site.layer() || site == CourtSite.WAKE_NURSERY && layer == PelagicLayer.TWILIGHT;
	}

	private static int[][] offsets(int radius, int step) {
		var offsets = new java.util.ArrayList<int[]>();
		for (int dx = -radius; dx <= radius; dx += step)
			for (int dz = -radius; dz <= radius; dz += step)
				offsets.add(new int[]{dx, dz});
		offsets.sort(java.util.Comparator.<int[]>comparingInt(o -> o[0] * o[0] + o[1] * o[1])
				.thenComparingInt(o -> o[0]).thenComparingInt(o -> o[1]));
		return offsets.toArray(int[][]::new);
	}

	/** The first two steps alone: the gate, the house's biome at its band's middle, and the surface climate. */
	public static boolean habitat(CourtSite site, @Nullable Probe probe, int x, int z) {
		if (probe == null) return false;
		if (site == CourtSite.WAKE_NURSERY) {
			// Open water: Pelagic Ocean at the band's middle, or upper Twilight water over the slope under any ocean.
			if (probe.layerAt(x, site.sampleY(), z) != PelagicLayer.OPEN
					&& probe.layerAt(x, NURSERY_SLOPE_Y, z) != PelagicLayer.TWILIGHT) return false;
		} else if (probe.layerAt(x, site.sampleY(), z) != site.layer()) return false;
		return switch (site.climate()) {
			case ANY -> true;
			case COLD_OR_FROZEN -> probe.coldSurface(x, z);
			case TEMPERATE_OR_WARM -> !probe.coldSurface(x, z);
		};
	}

	/** The nearest seeded vent field the terrain actually builds, so the Sanctum always sits on a real mound. */
	@Nullable
	public static PelagicTerrainSampler.VentSite ventAnchor(Probe probe, int x, int z) {
		int cell = PelagicTerrainSampler.VENT_CELL;
		int cellX = Math.floorDiv(x, cell), cellZ = Math.floorDiv(z, cell);
		PelagicTerrainSampler.VentSite best = null;
		long bestDistance = (long) VENT_SEARCH_RADIUS * VENT_SEARCH_RADIUS;
		for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
			var site = probe.terrain().ventSite(cx, cz);
			long dx = site.x() - x, dz = site.z() - z;
			long distance = dx * dx + dz * dz;
			if (distance > bestDistance || !site.active()) continue;
			if (!PelagicLandmarks.hostsVentField(site, probe.column(site.x(), site.z()))) continue;
			best = site;
			bestDistance = distance;
		}
		return best;
	}

	/** Corners and edge midpoints first; a steep or half-dry footprint is refused before anything reads its interior. */
	public static boolean levelFootprint(Probe probe, int x, int z, int radius, int maxVariation) {
		double low = Double.MAX_VALUE, high = -Double.MAX_VALUE;
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			var column = probe.column(x + dx * radius, z + dz * radius);
			if (column.influence() < FOOTPRINT_INFLUENCE) return false;
			low = Math.min(low, column.floor());
			high = Math.max(high, column.floor());
			if (high - low > maxVariation) return false;
		}
		return true;
	}
}
