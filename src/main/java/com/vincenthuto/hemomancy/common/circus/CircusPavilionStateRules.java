package com.vincenthuto.hemomancy.common.circus;

import java.util.UUID;

public final class CircusPavilionStateRules {
	private CircusPavilionStateRules() {
	}

	public static boolean canBegin(UUID activeOwner, Outcome outcome) {
		return activeOwner == null && outcome == Outcome.NEUTRAL;
	}

	public static boolean canAct(UUID activeOwner, UUID player) {
		return activeOwner != null && activeOwner.equals(player);
	}

	public static Phase resetPhase(Outcome outcome) {
		return outcome == Outcome.NEUTRAL ? Phase.IDLE : Phase.COMPLETE;
	}

	public static Outcome resetOutcome(Outcome outcome) {
		return outcome;
	}

	public enum Outcome { NEUTRAL, SUCCESSION, RUIN }
	public enum Phase { IDLE, PERFORMANCE, RAFTERS, CAROUSEL, DESCENT, COMPLETE }
}
