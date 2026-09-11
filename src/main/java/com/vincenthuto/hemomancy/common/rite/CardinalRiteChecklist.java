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
        return inscription(optionalSigils, completedSigils, 0, 0, allies, 0, mediumReady);
    }

    public static List<String> inscription(int optionalSigils, int completedOptional,
            int requiredSigils, int completedRequired, int availableAllies, int requiredAllies,
            boolean mediumReady) {
        List<String> lines = new ArrayList<>();
        if (requiredSigils > 0) lines.add("Required sigils: " + completedRequired + "/" + requiredSigils);
        if (optionalSigils > 0) lines.add("Optional sigils: " + completedOptional + "/" + optionalSigils);
        lines.add(requiredAllies > 0 ? "Required allies: " + availableAllies + "/" + requiredAllies
                : "Optional allies: " + availableAllies + " assigned");
        lines.add(mediumReady ? "Medium seated" : "Required medium missing");
        boolean ready = mediumReady && completedRequired >= requiredSigils && availableAllies >= requiredAllies;
        lines.add(ready ? "Project into the daemon to begin" : "Fulfil required preparations before sealing");
        return List.copyOf(lines);
    }

	public static List<String> inscription(int optionalSigils, int allies, boolean mediumReady) {
		return inscription(optionalSigils, 0, allies, mediumReady);
	}

	public static String ordealObjective(String wave) {
		if (wave == null) return "Endure the ordeal";
		if (CardinalRiteWaveRules.objective(wave) != CardinalRiteWaveRules.Objective.SURVIVE)
			return CardinalRiteWaveRules.deadlineHint(wave);
		return switch (wave) {
			case "bloodlicker_siphon" -> "Defend the boundary from Bloodlickers";
			case "fargone_dive" -> "Survive the Fargone assault";
			case "rogue_will" -> "Drive the rogue will from the boundary";
			default -> "Endure the current ordeal";
		};
	}
}
