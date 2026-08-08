package com.vincenthuto.hemomancy.common.command;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

public final class SilentArchonCommandRules {
	private SilentArchonCommandRules() {
	}

	public static Transition transition(String state, int currentDegree) {
		return switch (state) {
			case "pending" -> new Transition(7, EnumArchonPath.SILENT_PENDING, false);
			case "complete" -> new Transition(7, EnumArchonPath.SILENT_ARCHON, false);
			case "clear" -> new Transition(currentDegree, EnumArchonPath.NONE, true);
			default -> throw new IllegalArgumentException("Unknown Silent Archon state: " + state);
		};
	}

	public record Transition(int degreeNumber, EnumArchonPath archonPath, boolean clearLegacyChoice) {
	}
}
