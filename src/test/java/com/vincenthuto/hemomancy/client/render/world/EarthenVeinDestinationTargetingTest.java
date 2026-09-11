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

final class EarthenVeinDestinationTargetingTest {
	@Test
	void selectsTheNearestMarkerIntersectedByTheViewRay() {
		var near = marker("Near", new Vec3(0.0D, 0.0D, 3.0D));
		var far = marker("Far", new Vec3(0.0D, 0.0D, 5.0D));
		var selected = EarthenVeinDestinationTargeting.select(List.of(far, near), Vec3.ZERO,
				new Vec3(0.0D, 0.0D, 1.0D), 6.0D, 0.45D).orElseThrow();

		assertEquals("Near", selected.destination().name());
	}

	@Test
	void ignoresMarkersOutsideTheLookRayAndReach() {
		assertTrue(EarthenVeinDestinationTargeting.select(List.of(
				marker("Side", new Vec3(2.0D, 0.0D, 3.0D)),
				marker("Too Far", new Vec3(0.0D, 0.0D, 8.0D))),
				Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), 6.0D, 0.45D).isEmpty());
	}

	private static EarthenVeinDestinationLayout.Marker marker(String name, Vec3 center) {
		var destination = new EarthenVeinDestination(UUID.randomUUID(), name,
				ResourceLocation.withDefaultNamespace("overworld"), BlockPos.ZERO, 500);
		return new EarthenVeinDestinationLayout.Marker(destination, center, false);
	}
}
