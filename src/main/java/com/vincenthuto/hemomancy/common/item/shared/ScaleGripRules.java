package com.vincenthuto.hemomancy.common.item.shared;

import java.util.List;

public final class ScaleGripRules {
	public static final String TAG_PROTECTED = "ScaleGripProtected";

	private ScaleGripRules() {
	}

	public static boolean matchesBindingCraft(List<Candidate> candidates) {
		int grips = 0;
		int targets = 0;
		for (Candidate candidate : candidates) {
			if (candidate.empty()) {
				continue;
			}
			if (candidate.scaleGrip()) {
				grips++;
			} else if (!candidate.protectedByScaleGrip()) {
				targets++;
			} else {
				return false;
			}
		}
		return grips == 1 && targets == 1;
	}

	public record Candidate(int slot, String itemId, boolean empty, boolean scaleGrip, boolean protectedByScaleGrip) {
	}
}
