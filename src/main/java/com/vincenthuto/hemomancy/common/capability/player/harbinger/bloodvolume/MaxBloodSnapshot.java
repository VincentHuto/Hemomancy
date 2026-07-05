package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record MaxBloodSnapshot(double baseMax, List<MaxBloodContribution> contributions, double positiveBonus,
		double negativePenalty, double netBonus, double totalMax) {
	public static final MaxBloodSnapshot EMPTY = new MaxBloodSnapshot(5000.0D, List.of(), 0.0D, 0.0D,
			0.0D, 5000.0D);

	public MaxBloodSnapshot {
		baseMax = Math.max(1.0D, finiteOrZero(baseMax));
		contributions = Collections.unmodifiableList(new ArrayList<>(contributions == null ? List.of() : contributions));
		positiveBonus = Math.max(0.0D, finiteOrZero(positiveBonus));
		negativePenalty = Math.max(0.0D, finiteOrZero(negativePenalty));
		netBonus = finiteOrZero(netBonus);
		totalMax = Math.max(1.0D, finiteOrZero(totalMax));
	}

	public Optional<MaxBloodContribution> contribution(String sourceId) {
		for (MaxBloodContribution contribution : contributions) {
			if (contribution.sourceId().equals(sourceId)) {
				return Optional.of(contribution);
			}
		}
		return Optional.empty();
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
