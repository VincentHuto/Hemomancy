package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.world.phys.Vec3;

import java.util.List;

public record IchorianSigilAnatomy(
		Vec3 forward,
		Animation animation,
		List<Landmark> landmarks,
		List<Vessel> vessels,
		List<Membrane> membranes) {

	public IchorianSigilAnatomy {
		landmarks = List.copyOf(landmarks);
		vessels = List.copyOf(vessels);
		membranes = List.copyOf(membranes);
	}

	public enum Role {
		EYE,
		ORGAN,
		JOINT,
		VALVE,
		LIMB_TIP,
		HOOK,
		RIB,
		GANGLION,
		MEMBRANE_TIP
	}

	public enum Style {
		PENDULOUS_AMPULLA,
		NEEDLE_THREAD,
		CONTRACTILE_SHIELD,
		ARTERIAL_FORK,
		RECALL_RIBBON,
		FIVE_LIPPED_SHUTTER,
		WALKING_RIB_TOWER,
		VASCULAR_ARBOR,
		OPTIC_STALK_VEIL
	}

	public record Animation(Style style, float pulse, float flex, float lag) {
	}

	public record Landmark(int source, Vec3 position, Role role, float radius) {
	}

	public record Vessel(int from, int to, float thickness) {
	}

	public record Membrane(int a, int b, int c) {
	}
}
