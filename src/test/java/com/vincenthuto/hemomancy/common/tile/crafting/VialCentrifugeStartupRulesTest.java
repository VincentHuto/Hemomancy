package com.vincenthuto.hemomancy.common.tile.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class VialCentrifugeStartupRulesTest {
	@Test
	void reportsEveryStartupOutcomeBeforeChangingMachineState() {
		var validPair = List.of(sample(true, true, true, true), sample(true, true, true, true));

		assertEquals(VialCentrifugeStartupResult.ALREADY_RUNNING,
				VialCentrifugeStartupRules.evaluate(true, true, validPair));
		assertEquals(VialCentrifugeStartupResult.NO_PROCESSABLE_SAMPLES,
				VialCentrifugeStartupRules.evaluate(false, true, List.of()));
		assertEquals(VialCentrifugeStartupResult.IMBALANCE,
				VialCentrifugeStartupRules.evaluate(false, false, validPair));
		assertEquals(VialCentrifugeStartupResult.INVALID_SAMPLE,
				VialCentrifugeStartupRules.evaluate(false, true,
						List.of(sample(true, false, true, true), sample(true, false, true, true))));
		assertEquals(VialCentrifugeStartupResult.BLOCKED_ENZYME_OUTPUT,
				VialCentrifugeStartupRules.evaluate(false, true,
						List.of(sample(true, true, false, true), sample(true, true, false, true))));
		assertEquals(VialCentrifugeStartupResult.BLOCKED_VIAL_RETURN,
				VialCentrifugeStartupRules.evaluate(false, true,
						List.of(sample(true, true, true, false), sample(true, true, true, false))));
		assertEquals(VialCentrifugeStartupResult.SUCCESS,
				VialCentrifugeStartupRules.evaluate(false, true, validPair));
	}

	private static VialCentrifugeStartupRules.SampleState sample(boolean present, boolean processable,
			boolean enzymeOutputFits, boolean vialReturnFits) {
		return new VialCentrifugeStartupRules.SampleState(present, processable, enzymeOutputFits, vialReturnFits);
	}
}
