package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.vein.EarthenVeinDestination;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EarthenVeinDestinationLayoutTest {
	private static final ResourceLocation OVERWORLD = ResourceLocation.withDefaultNamespace("overworld");
	private static final ResourceLocation NETHER = ResourceLocation.withDefaultNamespace("the_nether");
	private static final Vec3 SOURCE = new Vec3(0.5D, 64.0D, 0.5D);

	@Test
	void sameDimensionMarkersPreserveHorizontalWorldBearing() {
		var east = destination("East", OVERWORLD, new BlockPos(500, 200, 0), 1_000);
		var north = destination("North", OVERWORLD, new BlockPos(0, -200, -500), 1_000);
		var placements = EarthenVeinDestinationLayout.layout(SOURCE, BlockPos.ZERO, OVERWORLD,
				List.of(east, north));

		assertTrue(placements.get(0).center().x > SOURCE.x + 2.0D);
		assertEquals(SOURCE.z, placements.get(0).center().z, 0.0001D);
		assertTrue(placements.get(1).center().z < SOURCE.z - 2.0D);
		assertEquals(SOURCE.x, placements.get(1).center().x, 0.0001D);
	}

	@Test
	void crowdedBearingsStaggerOutwardWithoutDroppingDestinations() {
		var placements = EarthenVeinDestinationLayout.layout(SOURCE, BlockPos.ZERO, OVERWORLD, List.of(
				destination("Near East", OVERWORLD, new BlockPos(500, 64, 0), 1_000),
				destination("Far East", OVERWORLD, new BlockPos(1_500, 64, 0), 2_000)));

		assertEquals(2, placements.size());
		assertTrue(placements.get(1).center().distanceTo(SOURCE) > placements.get(0).center().distanceTo(SOURCE) + 0.5D);
	}

	@Test
	void crossDimensionMarkersUseTheRaisedHalo() {
		var placements = EarthenVeinDestinationLayout.layout(SOURCE, BlockPos.ZERO, OVERWORLD, List.of(
				destination("Nether One", NETHER, BlockPos.ZERO, 4_000),
				destination("Nether Two", NETHER, new BlockPos(1, 2, 3), 4_000)));

		assertEquals(2, placements.size());
		assertTrue(placements.stream().allMatch(marker -> marker.center().y >= SOURCE.y + 2.2D));
		assertTrue(placements.get(0).center().distanceTo(placements.get(1).center()) > 2.0D);
	}

	@Test
	void crowdedMarkersRemainWithinInteractionReach() {
		var destinations = java.util.stream.IntStream.range(0, 12)
				.mapToObj(index -> destination("East " + index, OVERWORLD,
						new BlockPos(500 + index * 100, 64, 0), 1_000))
				.toList();
		var placements = EarthenVeinDestinationLayout.layout(SOURCE, BlockPos.ZERO, OVERWORLD, destinations);

		assertEquals(12, placements.size());
		assertTrue(placements.stream().allMatch(marker -> marker.center().distanceTo(SOURCE) < 7.0D));
		assertEquals(12, placements.stream().map(marker -> marker.center()).distinct().count());
	}

	private static EarthenVeinDestination destination(String name, ResourceLocation dimension, BlockPos pos, int cost) {
		return new EarthenVeinDestination(UUID.nameUUIDFromBytes(name.getBytes()), name, dimension, pos, cost);
	}
}
