package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.world.phys.Vec3;

import java.util.List;

public record AwakenedIchorianSigilPose(
		List<Landmark> landmarks,
		List<Vessel> primaryVessels,
		List<Vessel> secondaryVessels,
		List<Membrane> membranes,
		float detachment,
		float migration,
		float quickening,
		float scale) {
	public AwakenedIchorianSigilPose {
		landmarks = List.copyOf(landmarks);
		primaryVessels = List.copyOf(primaryVessels);
		secondaryVessels = List.copyOf(secondaryVessels);
		membranes = List.copyOf(membranes);
	}

	public record Landmark(int source, Vec3 position,
			IchorianSigilAnatomy.Role role, float radius, float activation) {
	}

	public record Vessel(int from, int to, float thickness, float growth) {
	}

	public record Membrane(int a, int b, int c, float inflation) {
	}
}
