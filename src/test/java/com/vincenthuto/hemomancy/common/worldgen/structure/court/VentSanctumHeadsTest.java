package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks.Chimney;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VentSanctumHeadsTest {
	private static final BlockPos CENTRE = new BlockPos(100, -40, 200);

	@Test
	void everyChimneyIsCrownedOnItsMouth() {
		var chimneys = List.of(new Chimney(107, 200, -42, 5), new Chimney(96, 205, -41, 8), new Chimney(98, 193, -42, 3));
		var spots = VentSanctumHeads.plan(CENTRE, chimneys, (x, z) -> -42, CENTRE.getY());
		for (var chimney : chimneys) {
			var crown = new BlockPos(chimney.x(), chimney.mouthY() + 1, chimney.z());
			assertTrue(spots.stream().anyMatch(s -> s.pos().equals(crown) && s.kind() == VentSanctumHeads.Kind.CROWN),
					"No crown on " + chimney);
		}
		assertEquals(3, spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.CROWN).count());
	}

	@Test
	void theThresholdIsTheTallestAndItsWalkwayRisesToTheMouth() {
		var tallest = new Chimney(96, 205, -41, 8);
		var chimneys = List.of(new Chimney(107, 200, -42, 5), tallest, new Chimney(98, 193, -42, 8));
		assertEquals(tallest, VentSanctumHeads.threshold(chimneys));
		var spots = VentSanctumHeads.plan(CENTRE, chimneys, (x, z) -> -42, CENTRE.getY());
		int highest = spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.WALK_TOP).mapToInt(s -> s.pos().getY())
				.max().orElseThrow();
		assertEquals(tallest.mouthY(), highest, "The walkway ends level with the threshold's mouth");
		var lowest = spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.WALK_TOP)
				.min((a, b) -> Integer.compare(a.pos().getY(), b.pos().getY())).orElseThrow();
		double fromCentre = Math.hypot(lowest.pos().getX() - CENTRE.getX(), lowest.pos().getZ() - CENTRE.getZ());
		assertTrue(fromCentre > 11, "The walkway starts at the processional ring: " + fromCentre);
	}

	@Test
	void basinsStandBesideTheirChimneysOnTheSeabed() {
		var chimneys = List.of(new Chimney(107, 200, -42, 5), new Chimney(96, 205, -41, 8));
		var spots = VentSanctumHeads.plan(CENTRE, chimneys, (x, z) -> x > 100 ? -43 : -42, CENTRE.getY());
		var basins = spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.BASIN).toList();
		assertEquals(3, basins.size(), "One basin per head, two flanking the threshold");
		for (var basin : basins) {
			int floor = basin.pos().getX() > 100 ? -43 : -42;
			assertEquals(floor + 1, basin.pos().getY());
			boolean beside = chimneys.stream().anyMatch(c -> Math.abs(c.x() - basin.pos().getX()) + Math.abs(c.z() - basin.pos().getZ()) == 2);
			assertTrue(beside, "A basin two blocks from its chimney: " + basin);
		}
	}

	@Test
	void aChimneyOnTheCentreStillGetsAWalkway() {
		var spots = VentSanctumHeads.plan(CENTRE, List.of(new Chimney(100, 200, -41, 6)), (x, z) -> -42, CENTRE.getY());
		assertTrue(spots.stream().anyMatch(s -> s.kind() == VentSanctumHeads.Kind.WALK_TOP));
	}
}
