package com.vincenthuto.hemomancy.common.worldgen.feature.court;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Where the Brined Court's signs may stand (Track 8): the frequent tier that says whose water you are in without placing
 * a settlement. Every sign family has a fixed number of candidate slots per 256 x 256 region, drawn from a seeded hash
 * of the region alone, so the cap holds by construction and the result does not depend on which chunk decorates first.
 * A slot only becomes a sign if the world there suits it (the feature checks the biome, the floor and the water).
 * Vent offerings take their slots from the region's real vent fields instead of a random point. Pure and chunk-free.
 */
public final class CourtSignRules {
	public static final int REGION = 256;
	/** A slot keeps this far inside its region, so a patch of blooms never spills over the edge. */
	private static final int MARGIN = 8;
	/** Layer boundaries (the first water Y of each deeper layer, PelagicLayer.atWaterY) where relays stand. */
	private static final int[] BOUNDARIES = {33, 12, -9, -31};
	public static final int BOUNDARY_SLACK = 3;

	/** One family of signs and its per-region cap. */
	public enum Family {
		/** Patches of floating Sunburst blooms over reef and coast, rarer over open water. */
		SUNBURST_BLOOM(6, 0x5B1C0FL),
		/** A lone house marker somewhere in the house's water (outposts carry their own). */
		HOUSE_MARKER(1, 0x4D7A11L),
		/** A relay post on the floor at a depth-layer boundary. */
		RELAY_MARKER(2, 0x2E9C53L),
		/** A grown float on open water, its line trailing below. */
		MESSENGER_BUOY(2, 0x7731E1L),
		/** An offering basin at the foot of a chimney outside the Vent Sanctum. */
		VENT_OFFERING(3, 0x1A0F75L);

		private final int cap;
		private final long salt;

		Family(int cap, long salt) {
			this.cap = cap;
			this.salt = salt;
		}

		public int cap() {
			return cap;
		}
	}

	/** A candidate spot: its block column and a hash the feature uses for variety (patch size, variant, facing). */
	public record Slot(int x, int z, long hash) {
	}

	private CourtSignRules() {
	}

	public static int region(int block) {
		return Math.floorDiv(block, REGION);
	}

	/** The family's candidate slots in one region: exactly {@link Family#cap()} of them, each inside the region. */
	public static List<Slot> slots(long seed, Family family, int regionX, int regionZ) {
		var result = new ArrayList<Slot>(family.cap());
		for (int i = 0; i < family.cap(); i++) {
			long hash = PelagicTerrainSampler.mix(seed ^ family.salt * 0x9E3779B97F4A7C15L ^ regionX * 0x632BE59BD9B4E019L
					^ regionZ * 0xD1B54A32D192ED03L ^ (i + 1L) * 0xC2B2AE3D27D4EB4FL);
			int span = REGION - 2 * MARGIN;
			int x = regionX * REGION + MARGIN + (int) Math.floorMod(hash, span);
			int z = regionZ * REGION + MARGIN + (int) Math.floorMod(hash >>> 24, span);
			result.add(new Slot(x, z, PelagicTerrainSampler.mix(hash)));
		}
		return result;
	}

	/**
	 * Whether a bloom slot keeps its patch over this water: always over reef and coast, one in three over the open sea
	 * (any deeper Pelagic layer under it).
	 */
	public static boolean bloomSurface(PelagicLayer water, long hash) {
		if (water == PelagicLayer.REEF || water == PelagicLayer.SHORE) return true;
		return Math.floorMod(hash >>> 7, 3) == 0;
	}

	/** Whether a floor stands within {@link #BOUNDARY_SLACK} of a depth-layer boundary, where relays hand off. */
	public static boolean nearBoundary(int floorY) {
		for (int boundary : BOUNDARIES) if (Math.abs(floorY - boundary) <= BOUNDARY_SLACK) return true;
		return false;
	}

	/** Whose water a Pelagic layer is; the Midnight layer splits by the surface climate above it. */
	public static CourtHouse house(PelagicLayer layer, boolean coldSurface) {
		return switch (layer) {
			case SHORE -> CourtHouse.HOLDING_STONE;
			case REEF -> CourtHouse.GIVING_LIGHT;
			case OPEN -> CourtHouse.SHARED_WAKE;
			case TWILIGHT -> CourtHouse.WOVEN_CURRENT;
			case MIDNIGHT -> coldSurface ? CourtHouse.STILL_BELL : CourtHouse.VEILED_FOLD;
			case CARRION -> CourtHouse.LAST_MARROW;
			case HYDROTHERMAL -> CourtHouse.KINDLING;
		};
	}

	/** An offering at a chimney's foot: the cell beside its base on the side away from the field's centre. */
	public record Offering(int x, int y, int z, int towardX, int towardZ) {
	}

	/**
	 * The region's vent offerings: its built vent fields (sites in the region that raise a mound), ranked by a seeded
	 * hash, the first {@link Family#VENT_OFFERING} cap of them, each at the foot of its tallest chimney.
	 */
	public static List<Offering> ventOfferings(long seed, PelagicTerrainSampler terrain,
			BiFunction<Integer, Integer, PelagicTerrainSampler.Column> column, int regionX, int regionZ) {
		int cells = REGION / PelagicTerrainSampler.VENT_CELL;
		var sites = new ArrayList<PelagicTerrainSampler.VentSite>();
		for (int i = 0; i < cells; i++) for (int j = 0; j < cells; j++) {
			var site = terrain.ventSite(regionX * cells + i, regionZ * cells + j);
			if (region(site.x()) != regionX || region(site.z()) != regionZ || !site.active()) continue;
			if (!PelagicLandmarks.hostsVentField(site, column.apply(site.x(), site.z()))) continue;
			sites.add(site);
		}
		sites.sort(Comparator.comparingLong(site -> PelagicTerrainSampler.mix(seed ^ site.salt() ^ Family.VENT_OFFERING.salt)));
		var result = new ArrayList<Offering>();
		for (var site : sites.subList(0, Math.min(Family.VENT_OFFERING.cap(), sites.size()))) {
			var chimney = PelagicLandmarks.chimneys(site, column).stream()
					.max(Comparator.comparingInt(PelagicLandmarks.Chimney::height)).orElse(null);
			if (chimney == null) continue;
			int dx = Integer.signum(chimney.x() - site.x()), dz = Integer.signum(chimney.z() - site.z());
			if (dx == 0 && dz == 0) dx = 1;
			if (dx != 0 && dz != 0) dz = 0;                  // stand square to the chimney, not on a corner
			int x = chimney.x() + dx * 2, z = chimney.z() + dz * 2;
			result.add(new Offering(x, chimney.ground() + 1, z, chimney.x(), chimney.z()));
		}
		return result;
	}
}
