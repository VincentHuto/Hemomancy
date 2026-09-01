package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import java.util.List;

public final class VialCentrifugeStartupRules {
	private VialCentrifugeStartupRules() {
	}

	public static VialCentrifugeStartupResult evaluate(boolean running, boolean balanced,
			List<SampleState> samples) {
		if (running) return VialCentrifugeStartupResult.ALREADY_RUNNING;
		if (samples.stream().noneMatch(SampleState::present)) {
			return VialCentrifugeStartupResult.NO_PROCESSABLE_SAMPLES;
		}
		if (!balanced) return VialCentrifugeStartupResult.IMBALANCE;
		if (samples.stream().anyMatch(sample -> sample.present() && !sample.processable())) {
			return VialCentrifugeStartupResult.INVALID_SAMPLE;
		}
		if (samples.stream().anyMatch(sample -> sample.present() && !sample.enzymeOutputFits())) {
			return VialCentrifugeStartupResult.BLOCKED_ENZYME_OUTPUT;
		}
		if (samples.stream().anyMatch(sample -> sample.present() && !sample.vialReturnFits())) {
			return VialCentrifugeStartupResult.BLOCKED_VIAL_RETURN;
		}
		return VialCentrifugeStartupResult.SUCCESS;
	}

	public record SampleState(boolean present, boolean processable, boolean enzymeOutputFits,
			boolean vialReturnFits) {
	}
}
