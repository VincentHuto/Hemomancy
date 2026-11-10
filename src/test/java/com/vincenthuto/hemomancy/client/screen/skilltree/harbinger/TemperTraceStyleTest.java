package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TemperTraceStyleTest {
	@Test
	void lockedEndpointAlwaysDimsTheConnection() {
		assertEquals(TemperTraceStyle.LOCKED,
				TemperTraceStyle.resolve(true, true, true, false));
	}

	@Test
	void connectionIsBrightOnlyWhenBothEndpointsAreKnown() {
		assertEquals(TemperTraceStyle.KNOWN,
				TemperTraceStyle.resolve(true, false, true, false));
		assertEquals(TemperTraceStyle.UNKNOWN,
				TemperTraceStyle.resolve(true, false, false, false));
		assertEquals(TemperTraceStyle.UNKNOWN,
				TemperTraceStyle.resolve(false, false, true, false));
	}
}
