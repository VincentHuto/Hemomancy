package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.vein.EarthenVeinDestination;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class EarthenVeinDestinationLayout {
	private static final double LOCAL_RADIUS = 2.5D;
	private static final double RING_STEP = 0.85D;
	private static final double MAX_LOCAL_RADIUS = 5.75D;
	private static final double CROWDED_ANGLE = Math.toRadians(20.0D);
	private static final double HALO_RADIUS = 3.25D;

	private EarthenVeinDestinationLayout() {
	}

	public static List<Marker> layout(Vec3 sourceCenter, BlockPos sourcePos, ResourceLocation sourceDimension,
			List<EarthenVeinDestination> destinations) {
		List<Marker> result = new ArrayList<>(destinations.size());
		List<Double> usedAngles = new ArrayList<>();
		int crossCount = (int) destinations.stream()
				.filter(destination -> !sourceDimension.equals(destination.dimension())).count();
		int crossIndex = 0;
		for (EarthenVeinDestination destination : destinations) {
			if (!sourceDimension.equals(destination.dimension())) {
				double angle = -Math.PI / 2.0D + Math.PI * 2.0D * crossIndex++ / Math.max(1, crossCount);
				result.add(new Marker(destination, sourceCenter.add(Math.cos(angle) * HALO_RADIUS,
						2.4D, Math.sin(angle) * HALO_RADIUS), true));
				continue;
			}
			double dx = destination.position().getX() - sourcePos.getX();
			double dz = destination.position().getZ() - sourcePos.getZ();
			double angle = dx == 0.0D && dz == 0.0D ? 0.0D : Math.atan2(dz, dx);
			long crowded = usedAngles.stream().filter(existing -> angularDistance(existing, angle) < CROWDED_ANGLE).count();
			usedAngles.add(angle);
			double radius = Math.min(MAX_LOCAL_RADIUS, LOCAL_RADIUS + crowded * RING_STEP);
			result.add(new Marker(destination, sourceCenter.add(Math.cos(angle) * radius,
					1.35D + crowded * 0.12D, Math.sin(angle) * radius), false));
		}
		return List.copyOf(result);
	}

	private static double angularDistance(double first, double second) {
		double delta = Math.abs(first - second) % (Math.PI * 2.0D);
		return Math.min(delta, Math.PI * 2.0D - delta);
	}

	public record Marker(EarthenVeinDestination destination, Vec3 center, boolean crossDimension) {
	}
}
