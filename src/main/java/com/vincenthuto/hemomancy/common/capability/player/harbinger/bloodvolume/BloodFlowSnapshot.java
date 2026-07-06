package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record BloodFlowSnapshot(List<BloodFlowContribution> contributions, double positivePerTick,
		double negativePerTick, double netPerTick, double circulationBandwidthPerWindow,
		double circulationUsedInWindow, double circulationRemainingInWindow, int circulationWindowTicks,
		boolean circulationEnabled) {
	public static final BloodFlowSnapshot EMPTY = new BloodFlowSnapshot(List.of(), 0.0D, 0.0D, 0.0D,
			0.0D, 0.0D, 0.0D, 20, true);

	public BloodFlowSnapshot {
		contributions = Collections.unmodifiableList(new ArrayList<>(contributions == null ? List.of() : contributions));
		positivePerTick = finiteOrZero(positivePerTick);
		negativePerTick = finiteOrZero(negativePerTick);
		netPerTick = finiteOrZero(netPerTick);
		circulationBandwidthPerWindow = Math.max(0.0D, finiteOrZero(circulationBandwidthPerWindow));
		circulationUsedInWindow = Math.max(0.0D, finiteOrZero(circulationUsedInWindow));
		circulationRemainingInWindow = Math.max(0.0D, finiteOrZero(circulationRemainingInWindow));
		circulationWindowTicks = Math.max(1, circulationWindowTicks);
	}

	public Optional<BloodFlowContribution> contribution(String sourceId) {
		for (BloodFlowContribution contribution : contributions) {
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
