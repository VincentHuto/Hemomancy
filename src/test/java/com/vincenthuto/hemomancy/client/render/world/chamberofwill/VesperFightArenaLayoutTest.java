package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class VesperFightArenaLayoutTest {
	private static final BlockPos CENTER = new BlockPos(4096, 64, 128);

	@Test
	void retriesAtTheSameArenaProduceTheSameAuthoredDamage() {
		assertEquals(VesperFightArenaLayout.generate(CENTER), VesperFightArenaLayout.generate(CENTER));
		assertNotEquals(VesperFightArenaLayout.generate(CENTER),
				VesperFightArenaLayout.generate(CENTER.offset(0, 0, 128)));
	}

	@Test
	void floorUsesAReadableFourByFourMacroChecker() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		for (int macroX = -5; macroX < 5; macroX++) {
			for (int macroZ = -5; macroZ < 5; macroZ++) {
				var tile = layout.tileAt(macroX * 4, macroZ * 4);
				var east = layout.tileAt((macroX + 1) * 4, macroZ * 4);
				var south = layout.tileAt(macroX * 4, (macroZ + 1) * 4);
				assertNotEquals(tile.material(), east.material());
				assertNotEquals(tile.material(), south.material());
			}
		}
	}

	@Test
	void centralThirtyFourByThirtyFourFieldIsContinuousAndShallow() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		for (int x = -17; x <= 16; x++) {
			for (int z = -17; z <= 16; z++) {
				var tile = layout.tileAt(x, z);
				assertFalse(tile.missing(), "missing central tile at " + x + "," + z);
				assertFalse(tile.severeDamage(), "severe central damage at " + x + "," + z);
				assertTrue(Math.abs(tile.heightOffset()) <= 0.035F,
						"central tile offset too deep at " + x + "," + z);
			}
		}
	}

	@Test
	void tileCoverageLeavesNarrowSeamsAndOnlySparseCollapsedAreas() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		long missingTiles = layout.tiles().stream().filter(VesperFightArenaLayout.Tile::missing).count();

		assertTrue(layout.tiles().stream().allMatch(tile -> tile.chipAmount() <= 0.03F),
				"individual stone insets should not expose broad crimson channels");
		assertTrue(missingTiles >= 15, "the damaged perimeter should still contain visible collapsed stones");
		assertTrue(missingTiles <= 85, "collapsed stones should remain isolated instead of forming broad red fields");
	}

	@Test
	void severeDamageAndMissingTilesStayInTheOuterEightBlocks() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		List<VesperFightArenaLayout.Tile> damaged = layout.tiles().stream()
				.filter(tile -> tile.severeDamage() || tile.missing())
				.toList();
		assertTrue(damaged.size() > 80, "outer band should visibly break apart");
		assertTrue(damaged.stream().allMatch(tile -> Math.abs(tile.x()) >= 17 || Math.abs(tile.z()) >= 17));
		assertTrue(damaged.stream().anyMatch(VesperFightArenaLayout.Tile::missing));
		assertTrue(damaged.stream().anyMatch(tile -> Math.abs(tile.heightOffset()) >= 0.12F));
		assertTrue(damaged.stream().anyMatch(tile -> Math.abs(tile.tiltX()) + Math.abs(tile.tiltZ()) >= 0.08F));
	}

	@Test
	void edgeRocksRemainOutsideTheReachableBarrierBoundary() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		assertTrue(layout.rocks().size() >= 16);
		assertTrue(layout.rocks().stream().allMatch(rock ->
				Math.max(Math.abs(rock.x()), Math.abs(rock.z())) >= 25.5F));
	}

	@Test
	void visualPerimeterContinuesPastCollisionAndFadesIntoTheDistance() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		List<VesperFightArenaLayout.HorizonTile> horizon = layout.horizonTiles();

		assertTrue(horizon.size() > 3000, "the distant floor should read as an extended field, not a narrow rim");
		assertTrue(horizon.stream().noneMatch(tile -> tile.x() >= -VesperFightArenaLayout.HALF_SIZE
				&& tile.x() < VesperFightArenaLayout.HALF_SIZE
				&& tile.z() >= -VesperFightArenaLayout.HALF_SIZE
				&& tile.z() < VesperFightArenaLayout.HALF_SIZE),
				"the fading extension must not overlap the cached playable mesh");
		assertTrue(horizon.stream().anyMatch(tile ->
				Math.max(Math.abs(tile.x()), Math.abs(tile.z())) >= 58),
				"visible fragments should continue far beyond the collision arena");
		assertTrue(horizon.stream().filter(tile ->
				Math.max(Math.abs(tile.x()), Math.abs(tile.z())) <= 30).allMatch(tile -> tile.alpha() >= 190),
				"the extension should join the playable floor without an opacity step");
		assertTrue(horizon.stream().filter(tile ->
				Math.max(Math.abs(tile.x()), Math.abs(tile.z())) >= 58).allMatch(tile -> tile.alpha() <= 35),
				"the farthest fragments should dissolve into the void");
	}

	@Test
	void brokenStonesContinueThroughTheFadeWhileAllStoneCoverageBecomesScarcer() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		List<VesperFightArenaLayout.HorizonTile> horizon = layout.horizonTiles();

		long nearCells = availableHorizonCells(26, 34);
		long farCells = availableHorizonCells(54, 62);
		long nearTiles = horizon.stream().filter(tile -> distance(tile) >= 26 && distance(tile) <= 34).count();
		long farTiles = horizon.stream().filter(tile -> distance(tile) >= 54 && distance(tile) <= 62).count();
		long nearBroken = horizon.stream().filter(tile -> distance(tile) >= 26 && distance(tile) <= 34)
				.filter(tile -> tile.damage() != VesperFightArenaLayout.HorizonDamage.INTACT).count();
		long farBroken = horizon.stream().filter(tile -> distance(tile) >= 54 && distance(tile) <= 62)
				.filter(tile -> tile.damage() != VesperFightArenaLayout.HorizonDamage.INTACT).count();

		assertTrue(horizon.stream().anyMatch(tile -> distance(tile) >= 58
				&& tile.damage() != VesperFightArenaLayout.HorizonDamage.INTACT),
				"chipped and shattered silhouettes should remain present in the final fade band");
		assertTrue(horizon.stream().anyMatch(tile -> tile.damage() == VesperFightArenaLayout.HorizonDamage.CHIPPED_CORNER));
		assertTrue(horizon.stream().anyMatch(tile -> tile.damage() == VesperFightArenaLayout.HorizonDamage.FRACTURED));
		assertTrue(horizon.stream().anyMatch(tile -> tile.damage() == VesperFightArenaLayout.HorizonDamage.SHARD));
		assertTrue(farTiles / (double) farCells < nearTiles / (double) nearCells * 0.65D,
				"all horizon stones should become markedly scarcer with distance");
		assertTrue(farBroken / (double) farCells < nearBroken / (double) nearCells,
				"broken stones must thin out with the rest of the field instead of forming a distant debris ring");
	}

	@Test
	void distantExtensionContinuesTheBrokenPerimeterWithoutARebuiltShelf() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		List<VesperFightArenaLayout.HorizonTile> horizon = layout.horizonTiles();

		double nearCoverage = coverage(horizon, 26, 32);
		double middleCoverage = coverage(horizon, 39, 47);
		double farCoverage = coverage(horizon, 54, 62);
		double nearDamageShare = horizon.stream()
				.filter(tile -> distance(tile) >= 26 && distance(tile) <= 32)
				.filter(tile -> tile.damage() != VesperFightArenaLayout.HorizonDamage.INTACT)
				.count() / (double) horizon.stream()
				.filter(tile -> distance(tile) >= 26 && distance(tile) <= 32).count();

		assertTrue(nearCoverage >= 0.78D && nearCoverage <= 0.90D,
				"the extension should continue the damaged edge instead of rebuilding a dense tile shelf");
		assertTrue(middleCoverage < nearCoverage * 0.72D,
				"tile coverage should keep thinning through the middle distance");
		assertTrue(farCoverage < middleCoverage * 0.45D,
				"the final band should dissolve rather than remaining a broad fake floor");
		assertTrue(nearDamageShare >= 0.45D,
				"the first extended band should inherit the perimeter's shattered character");
	}

	@Test
	void horizonStonesKeepSinkingThroughEveryDistanceBand() {
		List<VesperFightArenaLayout.HorizonTile> horizon = VesperFightArenaLayout.generate(CENTER).horizonTiles();
		double nearHeight = averageHeight(horizon, 26, 32);
		double middleHeight = averageHeight(horizon, 39, 47);
		double farHeight = averageHeight(horizon, 54, 62);

		assertTrue(middleHeight < nearHeight - 0.45D,
				"the subsidence should remain clearly visible through the middle distance");
		assertTrue(farHeight < middleHeight - 0.55D,
				"the stones should continue sinking smoothly through the final fade band");
	}

	@Test
	void horizonStonesJoinTheInteriorAtTheSameSurfaceHeightBeforeSinking() {
		List<VesperFightArenaLayout.HorizonTile> horizon = VesperFightArenaLayout.generate(CENTER).horizonTiles();
		double seamHeight = horizon.stream().filter(tile -> distance(tile) == VesperFightArenaLayout.HALF_SIZE)
				.mapToDouble(VesperFightArenaLayout.HorizonTile::height)
				.average().orElseThrow();

		assertTrue(Math.abs(seamHeight) <= 0.03D,
				"the extended tile field must not begin as a sunken shelf");
		assertTrue(horizon.stream().filter(tile -> distance(tile) == VesperFightArenaLayout.HALF_SIZE)
				.allMatch(tile -> tile.alpha() == 255),
				"the seam tiles must be opaque so their rear faces cannot blend through the top");
	}

	@Test
	void horizonDamageConvertsToInteriorTileDataWithoutInvertingTheStone() {
		VesperFightArenaLayout.HorizonTile horizon = new VesperFightArenaLayout.HorizonTile(
				25, -4, VesperFightArenaLayout.Material.BONE_BRICK, -0.02F, 0.015F, 230,
				VesperFightArenaLayout.HorizonDamage.FRACTURED, 3);

		assertEquals(new VesperFightArenaLayout.Tile(
				25, -4, VesperFightArenaLayout.Material.BONE_BRICK,
				VesperFightArenaLayout.SurfaceVariant.CRACKED,
				false, true, -0.02F, 0.0F, 0.0F, 0.015F), horizon.asInteriorTile());
	}

	@Test
	void crimsonWoundBridgesThePlayableFloorIntoTheFadeZone() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		List<VesperFightArenaLayout.HorizonWound> wounds = layout.horizonWounds();

		assertTrue(wounds.stream().noneMatch(wound -> wound.x() >= -VesperFightArenaLayout.HALF_SIZE
				&& wound.x() < VesperFightArenaLayout.HALF_SIZE
				&& wound.z() >= -VesperFightArenaLayout.HALF_SIZE
				&& wound.z() < VesperFightArenaLayout.HALF_SIZE));
		assertTrue(wounds.stream().filter(wound ->
				Math.max(Math.abs(wound.x()), Math.abs(wound.z())) <= 27).allMatch(wound -> wound.alpha() >= 200),
				"the crimson underlayer should continue strongly across the old hard boundary");
		assertTrue(wounds.stream().anyMatch(wound -> {
			int distance = Math.max(Math.abs(wound.x()), Math.abs(wound.z()));
			return distance >= 38 && distance <= 40 && wound.alpha() >= 45 && wound.alpha() <= 150;
		}), "the wound should visibly fade through the middle transition band");
		assertTrue(wounds.stream().filter(wound ->
				Math.max(Math.abs(wound.x()), Math.abs(wound.z())) >= 47).allMatch(wound -> wound.alpha() <= 20),
				"the crimson wound should disappear before the checkerboard horizon ends");
	}

	@Test
	void crimsonWoundRemainsBelowSunkenHorizonStones() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		Map<Long, VesperFightArenaLayout.HorizonTile> tilesByPosition = new HashMap<>();
		for (VesperFightArenaLayout.HorizonTile tile : layout.horizonTiles()) {
			tilesByPosition.put(packedPosition(tile.x(), tile.z()), tile);
		}

		for (VesperFightArenaLayout.HorizonWound wound : layout.horizonWounds()) {
			VesperFightArenaLayout.HorizonTile tile = tilesByPosition.get(packedPosition(wound.x(), wound.z()));
			if (tile != null) {
				assertTrue(wound.height() <= tile.height() - 0.01F,
						"crimson underlayer rose through a sunken stone at " + wound.x() + "," + wound.z());
			}
		}
	}

	@Test
	void fissuresBranchAcrossSeamsWithoutEnteringTheBrokenOuterBand() {
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(CENTER);
		assertTrue(layout.fissures().size() >= 24);
		assertTrue(layout.fissures().stream().anyMatch(fissure -> fissure.points().size() >= 4));
		assertTrue(layout.fissures().stream().flatMap(fissure -> fissure.points().stream().skip(1)).allMatch(point ->
				isInteger(point.x()) || isInteger(point.z())), "fissure branches should follow tile seams");
		assertTrue(layout.fissures().stream().allMatch(fissure -> fissure.intensity() < 0.85F),
				"fissures must not create the old square collapsed-node glow");
		assertTrue(layout.fissures().stream().flatMap(fissure -> fissure.points().stream()).allMatch(point ->
				point.x() >= -16.0F && point.x() <= 16.0F
						&& point.z() >= -16.0F && point.z() <= 16.0F),
				"fissure ribbons must stay out of the broken outer band");
	}

	private static boolean isInteger(float value) {
		return Math.abs(value - Math.round(value)) < 0.001F;
	}

	private static int distance(VesperFightArenaLayout.HorizonTile tile) {
		return Math.max(Math.abs(tile.x()), Math.abs(tile.z()));
	}

	private static long availableHorizonCells(int minimumDistance, int maximumDistance) {
		long cells = 0;
		for (int x = -VesperFightArenaLayout.HORIZON_RADIUS; x < VesperFightArenaLayout.HORIZON_RADIUS; x++) {
			for (int z = -VesperFightArenaLayout.HORIZON_RADIUS; z < VesperFightArenaLayout.HORIZON_RADIUS; z++) {
				int distance = Math.max(Math.abs(x), Math.abs(z));
				if (distance >= minimumDistance && distance <= maximumDistance) cells++;
			}
		}
		return cells;
	}

	private static double coverage(List<VesperFightArenaLayout.HorizonTile> tiles,
			int minimumDistance, int maximumDistance) {
		long present = tiles.stream().filter(tile -> distance(tile) >= minimumDistance
				&& distance(tile) <= maximumDistance).count();
		return present / (double) availableHorizonCells(minimumDistance, maximumDistance);
	}

	private static double averageHeight(List<VesperFightArenaLayout.HorizonTile> tiles,
			int minimumDistance, int maximumDistance) {
		return tiles.stream().filter(tile -> distance(tile) >= minimumDistance
				&& distance(tile) <= maximumDistance)
				.mapToDouble(VesperFightArenaLayout.HorizonTile::height)
				.average().orElseThrow();
	}

	private static long packedPosition(int x, int z) {
		return (long) x << 32 | z & 0xffffffffL;
	}
}
