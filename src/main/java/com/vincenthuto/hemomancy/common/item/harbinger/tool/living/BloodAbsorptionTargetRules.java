package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class BloodAbsorptionTargetRules {
	private BloodAbsorptionTargetRules() {
	}

	/** Candidates in the order a draw takes them: {@code first} ones (a brand) ahead of the rest, each by distance. */
	public static <T> List<T> orderTargets(List<T> candidates, Predicate<T> first, ToDoubleFunction<T> distanceSqr) {
		return candidates.stream()
				.sorted(Comparator.<T>comparingInt(candidate -> first.test(candidate) ? 0 : 1)
						.thenComparingDouble(distanceSqr))
				.toList();
	}
}
