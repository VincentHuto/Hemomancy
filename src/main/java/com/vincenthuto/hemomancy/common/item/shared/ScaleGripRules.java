package com.vincenthuto.hemomancy.common.item.shared;

import java.util.List;
import java.util.OptionalInt;

public final class ScaleGripRules {
	private ScaleGripRules() {
	}

	public static OptionalInt selectProtectedSlot(List<Candidate> candidates) {
		for (Candidate candidate : candidates) {
			if (!candidate.empty() && !candidate.scaleGrip()) {
				return OptionalInt.of(candidate.slot());
			}
		}
		return OptionalInt.empty();
	}

	public record Candidate(int slot, String itemId, boolean empty, boolean scaleGrip) {
	}
}
