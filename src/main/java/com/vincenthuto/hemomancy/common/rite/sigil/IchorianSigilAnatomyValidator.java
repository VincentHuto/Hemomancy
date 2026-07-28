package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

public final class IchorianSigilAnatomyValidator {
	private IchorianSigilAnatomyValidator() {
	}

	public static Result validate(int sourceNodeCount, IchorianSigilAnatomy candidate) {
		List<String> errors = new ArrayList<>();
		if (candidate == null) {
			return new Result(Optional.empty(), List.of("Anatomical form is missing"));
		}
		validateForward(candidate.forward(), errors);
		validateAnimation(candidate.animation(), errors);
		validateLandmarks(sourceNodeCount, candidate.landmarks(), errors);
		validateVessels(sourceNodeCount, candidate.vessels(), errors);
		validateMembranes(sourceNodeCount, candidate.membranes(), errors);
		return errors.isEmpty()
				? new Result(Optional.of(candidate), List.of())
				: new Result(Optional.empty(), List.copyOf(errors));
	}

	private static void validateForward(Vec3 forward, List<String> errors) {
		if (!finite(forward) || forward.lengthSqr() <= 1.0E-12D) {
			errors.add("Forward vector must be finite and non-zero");
		}
	}

	private static void validateAnimation(IchorianSigilAnatomy.Animation animation,
			List<String> errors) {
		if (animation == null || animation.style() == null) {
			errors.add("Animation style is required");
			return;
		}
		validateTuning("pulse", animation.pulse(), errors);
		validateTuning("flex", animation.flex(), errors);
		validateTuning("lag", animation.lag(), errors);
	}

	private static void validateTuning(String name, float value, List<String> errors) {
		if (!Float.isFinite(value) || value < 0.0F || value > 2.0F) {
			errors.add(name + " must be finite and between 0 and 2");
		}
	}

	private static void validateLandmarks(int sourceNodeCount,
			List<IchorianSigilAnatomy.Landmark> landmarks, List<String> errors) {
		BitSet sources = new BitSet(Math.max(0, sourceNodeCount));
		int eyes = 0;
		int organs = 0;
		for (IchorianSigilAnatomy.Landmark landmark : landmarks) {
			if (landmark.source() < 0 || landmark.source() >= sourceNodeCount) {
				errors.add("Landmark source " + landmark.source() + " is out of range");
			} else if (sources.get(landmark.source())) {
				errors.add("Landmark source " + landmark.source() + " is duplicated");
			} else {
				sources.set(landmark.source());
			}
			if (!finite(landmark.position())) {
				errors.add("Landmark position must be finite");
			}
			if (!Float.isFinite(landmark.radius()) || landmark.radius() <= 0.0F) {
				errors.add("Landmark radius must be finite and positive");
			}
			if (landmark.role() == IchorianSigilAnatomy.Role.EYE) eyes++;
			if (landmark.role() == IchorianSigilAnatomy.Role.ORGAN) organs++;
		}
		if (sources.cardinality() != sourceNodeCount || landmarks.size() != sourceNodeCount) {
			errors.add("Every source node must map exactly once");
		}
		if (eyes != 1) errors.add("Anatomical form must contain exactly one eye");
		if (organs != 1) errors.add("Anatomical form must contain exactly one organ");
	}

	private static void validateVessels(int sourceNodeCount,
			List<IchorianSigilAnatomy.Vessel> vessels, List<String> errors) {
		for (IchorianSigilAnatomy.Vessel vessel : vessels) {
			if (!validIndex(vessel.from(), sourceNodeCount)
					|| !validIndex(vessel.to(), sourceNodeCount)) {
				errors.add("Vessel endpoint is out of range");
			}
			if (!Float.isFinite(vessel.thickness()) || vessel.thickness() <= 0.0F) {
				errors.add("Vessel thickness must be finite and positive");
			}
		}
	}

	private static void validateMembranes(int sourceNodeCount,
			List<IchorianSigilAnatomy.Membrane> membranes, List<String> errors) {
		for (IchorianSigilAnatomy.Membrane membrane : membranes) {
			if (!validIndex(membrane.a(), sourceNodeCount)
					|| !validIndex(membrane.b(), sourceNodeCount)
					|| !validIndex(membrane.c(), sourceNodeCount)) {
				errors.add("Membrane endpoint is out of range");
			}
		}
	}

	private static boolean validIndex(int index, int count) {
		return index >= 0 && index < count;
	}

	private static boolean finite(Vec3 vector) {
		return vector != null && Double.isFinite(vector.x)
				&& Double.isFinite(vector.y) && Double.isFinite(vector.z);
	}

	public record Result(Optional<IchorianSigilAnatomy> form, List<String> errors) {
		public Result {
			errors = List.copyOf(errors);
		}
	}
}
