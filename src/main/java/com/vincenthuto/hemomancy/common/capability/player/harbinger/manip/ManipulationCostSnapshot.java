package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ManipulationCostSnapshot(double baseCost, List<ManipulationCostContribution> contributions,
		double discountMultiplier, double surchargeMultiplier, double totalMultiplier, double effectiveCost) {
	public static final ManipulationCostSnapshot EMPTY = new ManipulationCostSnapshot(0.0D, List.of(),
			1.0D, 1.0D, 1.0D, 0.0D);

	public ManipulationCostSnapshot {
		baseCost = Math.max(0.0D, finiteOrZero(baseCost));
		contributions = Collections.unmodifiableList(new ArrayList<>(
				contributions == null ? List.of() : contributions));
		discountMultiplier = Math.max(0.0D, finiteOrOne(discountMultiplier));
		surchargeMultiplier = Math.max(0.0D, finiteOrOne(surchargeMultiplier));
		totalMultiplier = Math.max(0.0D, finiteOrOne(totalMultiplier));
		effectiveCost = Math.max(0.0D, finiteOrZero(effectiveCost));
	}

	public Optional<ManipulationCostContribution> contribution(String sourceId) {
		for (ManipulationCostContribution contribution : contributions) {
			if (contribution.sourceId().equals(sourceId)) {
				return Optional.of(contribution);
			}
		}
		return Optional.empty();
	}

	public double discountPercent() {
		return Math.max(0.0D, 1.0D - discountMultiplier);
	}

	public double surchargePercent() {
		return Math.max(0.0D, surchargeMultiplier - 1.0D);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}

	private static double finiteOrOne(double value) {
		return Double.isFinite(value) ? value : 1.0D;
	}
}
