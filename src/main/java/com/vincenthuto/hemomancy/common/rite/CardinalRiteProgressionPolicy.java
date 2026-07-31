package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hard ceilings for the ritual vocabulary taught at each initiatory degree.
 * Recipes may remain simpler than their degree permits, but may not introduce
 * a mechanic before the player has learned it.
 */
public final class CardinalRiteProgressionPolicy {
	private static final Map<String, Integer> FOG_LEVEL = Map.of(
			"none", 0, "faint", 1, "dense", 2, "storm", 3);
	private static final Map<String, Integer> FAILURE_LEVEL = Map.of(
			"safe_retry", 0, "offering_loss", 1, "fragile_damage", 2, "collapse", 3);

	private CardinalRiteProgressionPolicy() {
	}

	public static List<String> violations(String ritePath, int degree,
			CardinalRiteCeremonyDefinition ceremony, int offeringCount) {
		if (ceremony == null) return List.of("ceremony is missing");
		// Apotheos progression is intentionally a separate design pass.
		if (ritePath != null && ritePath.endsWith("apotheos_rite")) return List.of();

		Limits limits = limits(Math.max(0, degree));
		List<String> problems = new ArrayList<>();
		if (ceremony.anchors().size() > limits.maxAnchors()) {
			problems.add("anchors " + ceremony.anchors().size() + " exceed " + limits.maxAnchors());
		}
		if (ceremony.supportSockets().size() > limits.maxSupportSockets()) {
			problems.add("support sigils " + ceremony.supportSockets().size()
					+ " exceed " + limits.maxSupportSockets());
		}
		long requiredSockets = ceremony.supportSockets().stream()
				.filter(CardinalRiteCeremonyDefinition.SupportSocket::required).count();
		if (requiredSockets > limits.maxRequiredSupportSockets()) {
			problems.add("required support sigils " + requiredSockets
					+ " exceed " + limits.maxRequiredSupportSockets());
		}
		int waveOptions = ceremony.waves().size() + ceremony.guaranteedWaves().size();
		if (waveOptions > limits.maxWaveOptions()) {
			problems.add("ordeal waves " + waveOptions + " exceed " + limits.maxWaveOptions());
		}
		if (offeringCount > limits.maxOfferings()) {
			problems.add("offerings " + offeringCount + " exceed " + limits.maxOfferings());
		}
		if (ceremony.requiredHelpers() > limits.maxRequiredHelpers()) {
			problems.add("required helpers " + ceremony.requiredHelpers()
					+ " exceed " + limits.maxRequiredHelpers());
		}
		if (!limits.focusMode().equals(ceremony.focusMode())) {
			problems.add("focus " + ceremony.focusMode() + " must be " + limits.focusMode());
		}
		if (severity(FOG_LEVEL, ceremony.atmosphere().fog()) > limits.maxFogLevel()) {
			problems.add("fog " + ceremony.atmosphere().fog() + " appears too early");
		}
		if (ceremony.atmosphere().lightning() && !limits.lightning()) {
			problems.add("rite lightning appears too early");
		}
		if (ceremony.atmosphere().dome() && !limits.dome()) {
			problems.add("boundary dome appears too early");
		}
		if (severity(FAILURE_LEVEL, ceremony.failureProfile()) > limits.maxFailureLevel()) {
			problems.add("failure profile " + ceremony.failureProfile() + " is too severe");
		}
		return List.copyOf(problems);
	}

	private static int severity(Map<String, Integer> values, String value) {
		return values.getOrDefault(value, Integer.MAX_VALUE);
	}

	private static Limits limits(int degree) {
		if (degree == 0) return new Limits(0, 0, 0, 0, 0, 0,
				"temple_medium", 0, false, false, 0);
		if (degree == 1) return new Limits(4, 0, 0, 0, 0, 0,
				"hematic_medium", 0, false, false, 0);
		if (degree <= 3) return new Limits(4, 0, 0, 0, 1, 0,
				"living_staff", 1, false, false, 0);
		if (degree == 4) return new Limits(4, 1, 1, 0, 3, 0,
				"living_staff", 2, false, false, 1);
		if (degree == 5) return new Limits(8, 3, 1, 6, 5, 0,
				"living_staff", 3, true, false, 2);
		if (degree == 6) return new Limits(12, 5, 2, 6, 9, 1,
				"living_staff", 3, true, true, 3);
		return new Limits(12, 6, 3, 6, 9, 3,
				"living_staff", 3, true, true, 3);
	}

	private record Limits(
			int maxAnchors,
			int maxSupportSockets,
			int maxRequiredSupportSockets,
			int maxWaveOptions,
			int maxOfferings,
			int maxRequiredHelpers,
			String focusMode,
			int maxFogLevel,
			boolean lightning,
			boolean dome,
			int maxFailureLevel) {
	}
}
