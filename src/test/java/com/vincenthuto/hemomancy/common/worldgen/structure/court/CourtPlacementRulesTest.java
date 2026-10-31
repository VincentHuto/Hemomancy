package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.ToDoubleBiFunction;

import static org.junit.jupiter.api.Assertions.*;

class CourtPlacementRulesTest {
	private static final long SEED = 42;

	/**
	 * A seabed with one surface habitat and one floor function. Layers follow the Pelagic biome wrapper: quart-sampled
	 * height, nothing below five blocks under the floor, the surface habitat from Y34 up, depth layers below.
	 */
	private record World(PelagicLayer surface, ToDoubleBiFunction<Integer, Integer> floor, boolean cold, boolean landmark)
			implements CourtPlacementRules.Probe {
		static World flat(PelagicLayer surface, double floor) {
			return new World(surface, (x, z) -> floor, false, false);
		}

		World cold(boolean value) {
			return new World(surface, floor, value, landmark);
		}

		World landmark(boolean value) {
			return new World(surface, floor, cold, value);
		}

		@Override
		public PelagicLayer layerAt(int x, int y, int z) {
			int quart = Math.floorDiv(y, 4) * 4;
			if (quart < floor.applyAsDouble(x, z) - 5) return null;
			if (surface == PelagicLayer.SHORE) return quart < 90 ? PelagicLayer.SHORE : null;
			if (quart > 63) return null;
			return quart >= 34 ? surface : PelagicLayer.atWaterY(quart);
		}

		@Override
		public PelagicTerrainSampler.Column column(int x, int z) {
			double y = floor.applyAsDouble(x, z);
			var form = y < -34 ? PelagicTerrainSampler.Form.PLAIN : PelagicTerrainSampler.Form.CANYON;
			return new PelagicTerrainSampler.Column(y, 1, form, 0, false);
		}

		@Override
		public boolean coldSurface(int x, int z) {
			return cold;
		}

		@Override
		public PelagicTerrainSampler terrain() {
			return new PelagicTerrainSampler(SEED);
		}

		@Override
		public boolean nearLandmark(CourtSite site, int x, int z, int radius) {
			return landmark;
		}
	}

	/** A linear spread like vanilla's: one chunk per cell, offset by a seeded hash inside spacing minus separation. */
	private static CourtLandmarkCells.Spread spread(int spacing, int separation, long salt) {
		return spread(spacing, separation, salt, 0, 0);
	}

	private static CourtLandmarkCells.Spread spread(int spacing, int separation, long salt, int reach, int slack) {
		return new CourtLandmarkCells.Spread(spacing, (cellX, cellZ) -> {
			long hash = PelagicTerrainSampler.mix(SEED ^ salt ^ cellX * 0x632BE59BD9B4E019L ^ cellZ * 0x9E3779B97F4A7C15L);
			int span = spacing - separation;
			return new ChunkPos(cellX * spacing + (int) Math.floorMod(hash, (long) span),
					cellZ * spacing + (int) Math.floorMod(hash >>> 20, (long) span));
		}, reach, slack);
	}

	private static Optional<BlockPos> stub(CourtSite site, CourtPlacementRules.Probe world) {
		return CourtPlacementRules.stub(site, world, SEED, 8, 8);
	}

	@Test
	void nothingGeneratesWhenPelagicIsOff() {
		for (var site : CourtSite.values()) {
			assertTrue(CourtPlacementRules.stub(site, null, SEED, 8, 8).isEmpty(), site.id());
			assertFalse(CourtPlacementRules.habitat(site, null, 8, 8), site.id());
		}
	}

	@Test
	void eachHouseStandsInItsOwnBiomeAndBand() {
		assertEquals(Optional.of(new BlockPos(8, 41, 8)), stub(CourtSite.LIGHTHOUSE_GARDEN, World.flat(PelagicLayer.REEF, 40)));
		assertEquals(Optional.of(new BlockPos(8, 61, 8)), stub(CourtSite.TIDE_REFUGE, World.flat(PelagicLayer.SHORE, 60.5)));
		assertEquals(Optional.of(new BlockPos(8, 21, 8)), stub(CourtSite.LINE_CAMP, World.flat(null, 20)));
		assertEquals(Optional.of(new BlockPos(8, 1, 8)), stub(CourtSite.SCREENED_CLEFT, World.flat(null, 0)));
		assertEquals(Optional.of(new BlockPos(8, 1, 8)), stub(CourtSite.STILL_BELL, World.flat(null, 0).cold(true)));
		assertEquals(Optional.of(new BlockPos(8, -19, 8)), stub(CourtSite.WHALE_FALL, World.flat(null, -20)));

		// Each depth house refuses every other band.
		assertTrue(stub(CourtSite.LINE_CAMP, World.flat(null, 0)).isEmpty());
		assertTrue(stub(CourtSite.LINE_CAMP, World.flat(null, 45)).isEmpty());
		assertTrue(stub(CourtSite.WHALE_FALL, World.flat(null, 0)).isEmpty());
		assertTrue(stub(CourtSite.WHALE_FALL, World.flat(null, -45)).isEmpty());
		assertTrue(stub(CourtSite.SCREENED_CLEFT, World.flat(null, 20)).isEmpty());
		assertTrue(stub(CourtSite.SCREENED_CLEFT, World.flat(null, -20)).isEmpty());
		assertTrue(stub(CourtSite.LIGHTHOUSE_GARDEN, World.flat(PelagicLayer.OPEN, 40)).isEmpty());
		assertTrue(stub(CourtSite.LIGHTHOUSE_GARDEN, World.flat(PelagicLayer.REEF, 20)).isEmpty());
		// The Tide Refuge keeps to the waterline, not the shore's submerged shelf.
		assertTrue(stub(CourtSite.TIDE_REFUGE, World.flat(PelagicLayer.SHORE, 50)).isEmpty());
		assertTrue(stub(CourtSite.TIDE_REFUGE, World.flat(PelagicLayer.REEF, 60)).isEmpty());
	}

	@Test
	void theGardenFindsReefBesideItsCellCentre() {
		// Reef floor only in a patch whose middle is 48 blocks east of the cell centre.
		var patch = new World(PelagicLayer.REEF, (x, z) -> Math.abs(x - 56) <= 30 && Math.abs(z - 8) <= 30 ? 40 : 200, false, false);
		assertEquals(Optional.of(new BlockPos(56, 41, 8)), stub(CourtSite.LIGHTHOUSE_GARDEN, patch));
		var far = new World(PelagicLayer.REEF, (x, z) -> Math.abs(x - 208) <= 30 ? 40 : 200, false, false);
		assertTrue(stub(CourtSite.LIGHTHOUSE_GARDEN, far).isEmpty(), "Reef beyond the search square is not the cell's");
		assertTrue(CourtPlacementRules.SEAT_REACH >= CourtPlacementRules.VENT_SEARCH_RADIUS
				&& CourtPlacementRules.SEAT_REACH * CourtPlacementRules.SEAT_REACH
				>= 2 * CourtPlacementRules.GARDEN_SEARCH_RADIUS * CourtPlacementRules.GARDEN_SEARCH_RADIUS);
	}

	@Test
	void midnightSplitsByTheSurfaceClimate() {
		var warm = World.flat(null, 0).cold(false);
		var cold = World.flat(null, 0).cold(true);
		assertTrue(stub(CourtSite.SCREENED_CLEFT, warm).isPresent());
		assertTrue(stub(CourtSite.SCREENED_CLEFT, cold).isEmpty());
		assertTrue(stub(CourtSite.STILL_BELL, cold).isPresent());
		assertTrue(stub(CourtSite.STILL_BELL, warm).isEmpty());
		assertTrue(CourtPlacementRules.habitat(CourtSite.LINE_CAMP, World.flat(null, 20).cold(true), 8, 8),
				"Only the Midnight houses read the surface climate");
	}

	@Test
	void theVentSanctumSitsOnASeededVentFieldCentre() {
		var terrain = new PelagicTerrainSampler(SEED);
		var plain = World.flat(null, -50);
		int found = 0;
		for (int cx = -6; cx < 6; cx++) for (int cz = -6; cz < 6; cz++) {
			var site = terrain.ventSite(cx, cz);
			var stub = CourtPlacementRules.stub(CourtSite.VENT_SANCTUM, plain, SEED, site.x() + 5, site.z() - 3);
			if (!site.active()) continue;
			assertTrue(stub.isPresent(), "An active vent field beside the cell centre must anchor the Sanctum");
			var anchor = terrain.ventSite(Math.floorDiv(stub.get().getX(), PelagicTerrainSampler.VENT_CELL),
					Math.floorDiv(stub.get().getZ(), PelagicTerrainSampler.VENT_CELL));
			assertEquals(anchor.x(), stub.get().getX());
			assertEquals(anchor.z(), stub.get().getZ());
			assertTrue(PelagicLandmarks.hostsVentField(anchor, plain.column(anchor.x(), anchor.z())));
			assertEquals(-49, stub.get().getY());
			found++;
		}
		assertTrue(found > 0, "Seed 42 must have active vent fields near the origin");
		// A Carrion-depth fan has no vent field, whatever the seeded candidates say.
		var fan = World.flat(null, -20);
		for (int cx = -6; cx < 6; cx++) for (int cz = -6; cz < 6; cz++) {
			var site = terrain.ventSite(cx, cz);
			assertTrue(CourtPlacementRules.stub(CourtSite.VENT_SANCTUM, fan, SEED, site.x(), site.z()).isEmpty());
		}
	}

	@Test
	void steepOrHalfDryFootprintsAreRefused() {
		var slope = new World(null, (x, z) -> 20 + (x - 8) * .5, false, false);
		assertTrue(stub(CourtSite.LINE_CAMP, slope).isEmpty(), "Eight blocks of fall across the footprint exceeds six");
		var gentle = new World(null, (x, z) -> 20 + (x - 8) * .25, false, false);
		assertTrue(stub(CourtSite.LINE_CAMP, gentle).isPresent());
		var halfDry = World.flat(null, 20);
		IntBinaryOperator dryCorner = (x, z) -> x > 12 && z > 12 ? 0 : 1;
		CourtPlacementRules.Probe dry = new CourtPlacementRules.Probe() {
			public PelagicLayer layerAt(int x, int y, int z) { return halfDry.layerAt(x, y, z); }
			public PelagicTerrainSampler.Column column(int x, int z) {
				return new PelagicTerrainSampler.Column(20, dryCorner.applyAsInt(x, z), PelagicTerrainSampler.Form.TWILIGHT, 0, false);
			}
			public boolean coldSurface(int x, int z) { return false; }
			public PelagicTerrainSampler terrain() { return halfDry.terrain(); }
			public boolean nearLandmark(CourtSite site, int x, int z, int radius) { return false; }
		};
		assertTrue(stub(CourtSite.LINE_CAMP, dry).isEmpty());
	}

	@Test
	void anotherFactionsLandmarkRefusesOnlyAfterTheScreen() {
		var world = World.flat(null, 20).landmark(true);
		assertTrue(CourtPlacementRules.screen(CourtSite.LINE_CAMP, world, SEED, 8, 8).isPresent());
		assertTrue(stub(CourtSite.LINE_CAMP, world).isEmpty());
	}

	@Test
	void theWakeNurseryTakesBanksAndTheUpperSlope() {
		// Since the 2026-10-30 review: banks and the upper slope under any ocean, one valid cell in 6.
		int banks = kept(World.flat(PelagicLayer.OPEN, 40));
		assertTrue(banks > 400 / 12 && banks < 400 / 3, "About one in 6 bank cells keeps a nursery, got " + banks);
		assertTrue(kept(World.flat(PelagicLayer.OPEN, 20)) > 4, "The upper slope under Pelagic Ocean is nursery ground");
		assertTrue(kept(World.flat(null, 22)) > 4, "So is the upper slope under any other ocean");
		assertEquals(0, kept(World.flat(PelagicLayer.OPEN, 15)), "Below the upper slope is too deep");
		assertEquals(0, kept(World.flat(PelagicLayer.OPEN, -20)), "Open water over the deep layers is not nursery ground");
		var patch = new World(PelagicLayer.OPEN, (x, z) -> Math.abs(x - 16) <= 12 && Math.abs(z - 8) <= 12 ? 30 : 0,
				false, false);
		var found = java.util.stream.IntStream.range(0, 400)
				.mapToObj(i -> CourtPlacementRules.stub(CourtSite.WAKE_NURSERY, shifted(patch, i * 448), SEED, i * 448 + 8, 8))
				.flatMap(Optional::stream).findFirst();
		assertTrue(found.isPresent() && found.get().getY() == 31 && Math.floorMod(found.get().getX(), 448) == 16,
				"The nursery searches its chunk for a bank off the centre: " + found);
		var far = new World(PelagicLayer.OPEN, (x, z) -> Math.abs(x - 56) <= 12 && Math.abs(z - 8) <= 12 ? 30 : 0,
				false, false);
		assertTrue(java.util.stream.IntStream.range(0, 400)
				.mapToObj(i -> CourtPlacementRules.stub(CourtSite.WAKE_NURSERY, shifted(far, i * 448), SEED, i * 448 + 8, 8))
				.noneMatch(Optional::isPresent), "A bank outside the start chunk is not taken: /locate reports the start chunk");
	}

	private static int kept(CourtPlacementRules.Probe world) {
		int kept = 0;
		for (int i = 0; i < 400; i++)
			if (CourtPlacementRules.stub(CourtSite.WAKE_NURSERY, world, SEED, i * 448 + 8, 8).isPresent()) kept++;
		return kept;
	}

	/** The patch world repeated every {@code offset} blocks along x. */
	private static World shifted(World world, int offset) {
		return new World(world.surface(), (x, z) -> world.floor().applyAsDouble(x - offset, z), false, false);
	}

	@Test
	void exclusionMatchesPotentialCellsComputedByHand() {
		var placements = List.of(spread(32, 8, 14357617), spread(48, 16, 2030148867));
		var random = new Random(7);
		int near = 0;
		for (int i = 0; i < 2000; i++) {
			int x = random.nextInt(40000) - 20000, z = random.nextInt(40000) - 20000;
			boolean expected = false;
			for (var placement : placements) {
				int spacing = placement.spacing();
				int cellX = Math.floorDiv(Math.floorDiv(x, 16), spacing), cellZ = Math.floorDiv(Math.floorDiv(z, 16), spacing);
				for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
					var chunk = placement.potential().of(cellX + dx, cellZ + dz);
					double distance = Math.hypot(chunk.getMiddleBlockX() - x, chunk.getMiddleBlockZ() - z);
					if (distance <= CourtPlacementRules.LANDMARK_RADIUS) expected = true;
				}
			}
			assertEquals(expected, CourtLandmarkCells.within(placements, (set, chunk) -> middle(chunk), x, z,
					CourtPlacementRules.LANDMARK_RADIUS), "at " + x + ", " + z);
			assertFalse(CourtLandmarkCells.within(placements, (set, chunk) -> null, x, z,
					CourtPlacementRules.LANDMARK_RADIUS));
			if (expected) near++;
		}
		assertTrue(near > 0, "The sample must include positions beside a potential cell");

		// A cell's own potential chunk is always inside its radius, and a point 97 blocks out on an empty axis is not.
		ChunkPos chunk = placements.get(0).potential().of(0, 0);
		assertTrue(CourtLandmarkCells.within(placements.subList(0, 1), (set, c) -> c.equals(chunk) ? middle(c) : null,
				chunk.getMiddleBlockX(), chunk.getMiddleBlockZ(), 96));
		assertTrue(CourtLandmarkCells.within(placements.subList(0, 1), (set, c) -> c.equals(chunk) ? middle(c) : null,
				chunk.getMiddleBlockX() + 96, chunk.getMiddleBlockZ(), 96));
		assertFalse(CourtLandmarkCells.within(placements.subList(0, 1), (set, c) -> c.equals(chunk) ? middle(c) : null,
				chunk.getMiddleBlockX() + 97, chunk.getMiddleBlockZ(), 96));
	}

	@Test
	void reachAndSlackWidenTheSearchForMovedLandmarks() {
		var plain = spread(32, 8, 14357617);
		ChunkPos chunk = plain.potential().of(0, 0);
		int x = chunk.getMiddleBlockX() + 150, z = chunk.getMiddleBlockZ();
		// The landmark moved 70 blocks towards (x, z): only a spread that declares that reach scans far enough to see it.
		CourtLandmarkCells.LandmarkAt moved = (set, c) -> c.equals(chunk) ? middle(c).offset(70, 0, 0) : null;
		assertFalse(CourtLandmarkCells.within(List.of(plain), moved, x, z, 96));
		assertTrue(CourtLandmarkCells.within(List.of(spread(32, 8, 14357617, 72, 0)), moved, x, z, 96));
		// Slack adds to the radius itself.
		CourtLandmarkCells.LandmarkAt fixed = (set, c) -> c.equals(chunk) ? middle(c) : null;
		assertFalse(CourtLandmarkCells.within(List.of(plain), fixed, chunk.getMiddleBlockX() + 110, z, 96));
		assertTrue(CourtLandmarkCells.within(List.of(spread(32, 8, 14357617, 0, 16)), fixed,
				chunk.getMiddleBlockX() + 110, z, 96));
	}

	private static BlockPos middle(ChunkPos chunk) {
		return new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
	}

	@Test
	void seatSearchReturnsTheNearestValidCell() {
		var seats = spread(64, 24, 1029834751);
		// Reef only east of X 3000: the search must cross empty rings and still pick the nearest reef cell.
		var world = new World(PelagicLayer.REEF, (x, z) -> x > 3000 ? 40 : 200, false, false);
		var found = CourtSeatCandidates.search(world, seats, SEED, CourtSite.LIGHTHOUSE_GARDEN, 0, 0, 8);
		assertTrue(found.isPresent());
		long best = Long.MAX_VALUE;
		for (int cx = -8; cx <= 8; cx++) for (int cz = -8; cz <= 8; cz++) {
			var chunk = seats.potential().of(cx, cz);
			var stub = CourtPlacementRules.stub(CourtSite.LIGHTHOUSE_GARDEN, world, SEED, chunk.getMiddleBlockX(),
					chunk.getMiddleBlockZ());
			if (stub.isEmpty()) continue;
			long dx = stub.get().getX(), dz = stub.get().getZ();
			best = Math.min(best, dx * dx + dz * dz);
		}
		long dx = found.get().getX(), dz = found.get().getZ();
		assertEquals(best, dx * dx + dz * dz);
		assertTrue(CourtSeatCandidates.search(world, seats, SEED, CourtSite.LIGHTHOUSE_GARDEN, 0, 0, 1).isEmpty(),
				"Nothing within one ring of the origin");
	}
}
