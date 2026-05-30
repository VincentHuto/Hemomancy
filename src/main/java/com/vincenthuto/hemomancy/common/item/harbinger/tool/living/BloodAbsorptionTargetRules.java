package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class BloodAbsorptionTargetRules {
	private BloodAbsorptionTargetRules() {
	}

	public static <T> Optional<T> chooseBareTarget(List<T> candidates, Predicate<T> valid,
			Predicate<T> lookedAt, ToDoubleFunction<T> distanceSqr) {
		return candidates.stream()
				.filter(valid)
				.min(Comparator.comparingDouble(distanceSqr));
	}
}
