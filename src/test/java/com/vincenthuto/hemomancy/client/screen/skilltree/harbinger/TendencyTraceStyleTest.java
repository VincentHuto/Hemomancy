package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TendencyTraceStyleTest {
	@Test
	void lockedEndpointAlwaysDimsTheConnection() {
		assertEquals(TendencyTraceStyle.LOCKED,
				TendencyTraceStyle.resolve(true, true, true, false));
	}

	@Test
	void connectionIsBrightOnlyWhenBothEndpointsAreKnown() {
		assertEquals(TendencyTraceStyle.KNOWN,
				TendencyTraceStyle.resolve(true, false, true, false));
		assertEquals(TendencyTraceStyle.UNKNOWN,
				TendencyTraceStyle.resolve(true, false, false, false));
		assertEquals(TendencyTraceStyle.UNKNOWN,
				TendencyTraceStyle.resolve(false, false, true, false));
	}
}
