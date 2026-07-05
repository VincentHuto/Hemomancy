package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import java.util.ArrayList;
import java.util.List;

public final class BloodFlowRules {
	private BloodFlowRules() {
	}

	public static BloodFlowSnapshot snapshot(List<BloodFlowContribution> requested,
			double circulationBandwidthPerWindow, double circulationUsedInWindow, int circulationWindowTicks,
			boolean circulationEnabled) {
		int windowTicks = Math.max(1, circulationWindowTicks);
		double bandwidth = Math.max(0.0D, finiteOrZero(circulationBandwidthPerWindow));
		double used = Math.max(0.0D, finiteOrZero(circulationUsedInWindow));
		double positive = 0.0D;
		double negative = 0.0D;
		List<BloodFlowContribution> applied = new ArrayList<>();

		for (BloodFlowContribution contribution : requested == null ? List.<BloodFlowContribution>of() : requested) {
			BloodFlowContribution next = contribution;
			if (contribution.circulationLimited() && contribution.requestedPerTick() > 0.0D) {
				if (circulationEnabled) {
					double requestedWindow = contribution.requestedPerTick() * windowTicks;
					double remaining = Math.max(0.0D, bandwidth - used);
					double grantedWindow = Math.min(requestedWindow, remaining);
					used += grantedWindow;
					String condition = contribution.condition();
					if (grantedWindow + 0.000001D < requestedWindow) {
						condition = "Circulation limited";
					}
					next = contribution.withAppliedPerTick(grantedWindow / windowTicks, condition);
				} else {
					next = contribution.withAppliedPerTick(contribution.requestedPerTick(), contribution.condition());
				}
			}
			applied.add(next);
			positive += next.positiveAppliedPerTick();
			negative += next.negativeAppliedPerTick();
		}

		double recordedUsed = circulationEnabled ? Math.min(used, bandwidth) : 0.0D;
		double remaining = circulationEnabled ? Math.max(0.0D, bandwidth - recordedUsed) : bandwidth;
		return new BloodFlowSnapshot(applied, positive, negative, positive - negative, bandwidth, recordedUsed,
				remaining, windowTicks, circulationEnabled);
	}

	public static BloodFlowSnapshot appliedSnapshot(List<BloodFlowContribution> applied,
			double circulationBandwidthPerWindow, double circulationUsedInWindow, int circulationWindowTicks,
			boolean circulationEnabled) {
		int windowTicks = Math.max(1, circulationWindowTicks);
		double bandwidth = Math.max(0.0D, finiteOrZero(circulationBandwidthPerWindow));
		double used = circulationEnabled ? Math.min(Math.max(0.0D, finiteOrZero(circulationUsedInWindow)), bandwidth)
				: 0.0D;
		double positive = 0.0D;
		double negative = 0.0D;
		List<BloodFlowContribution> contributions = new ArrayList<>();
		for (BloodFlowContribution contribution : applied == null ? List.<BloodFlowContribution>of() : applied) {
			contributions.add(contribution);
			positive += contribution.positiveAppliedPerTick();
			negative += contribution.negativeAppliedPerTick();
		}
		return new BloodFlowSnapshot(contributions, positive, negative, positive - negative, bandwidth, used,
				circulationEnabled ? Math.max(0.0D, bandwidth - used) : bandwidth, windowTicks,
				circulationEnabled);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
