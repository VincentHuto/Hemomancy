package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;

/**
 * Player-readable ceremony objectives kept independent from HUD rendering.
 */
public final class CardinalRiteChecklist {
	private CardinalRiteChecklist() {
	}

	public static List<String> inscription(int optionalSigils, int completedSigils,
			int allies, boolean mediumReady) {
		List<String> lines = new ArrayList<>();
		lines.add("Optional sigils: " + completedSigils + "/" + optionalSigils);
		lines.add("Optional allies: " + allies + " assigned");
		lines.add(mediumReady ? "Medium seated" : "Required medium missing");
		lines.add("Project into the daemon to begin");
		return List.copyOf(lines);
	}

	public static List<String> inscription(int optionalSigils, int allies, boolean mediumReady) {
		return inscription(optionalSigils, 0, allies, mediumReady);
	}

	public static String ordealObjective(String wave) {
		if (wave == null) return "Endure the ordeal";
		if (wave.startsWith("discover_")) return "Trace the unknown Ichorian sigil";
		return switch (wave) {
			case "response_sigil" -> "Trace the demanded response sigil";
			case "bloodlicker_siphon" -> "Defend the boundary from Bloodlickers";
			case "fargone_dive" -> "Survive the Fargone assault";
			case "rogue_will" -> "Drive the rogue will from the boundary";
			case "false_omens" -> "Find the true omen and ignore the counterfeits";
			default -> "Endure the current ordeal";
		};
	}
}
