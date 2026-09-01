package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

public final class HouseholdReadoutRulesTest {
	private HouseholdReadoutRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("dawn time band", "dawn", OssuaryClockReadout.timeBand(0));
		assertEquals("day time band", "day", OssuaryClockReadout.timeBand(6000));
		assertEquals("dusk time band", "dusk", OssuaryClockReadout.timeBand(12542));
		assertEquals("night time band", "night", OssuaryClockReadout.timeBand(18000));
		assertEquals("moon phase wraps", "full_moon", OssuaryClockReadout.moonPhase(8));

		assertEquals("clear weather", "clear", HumoralBarometerReadout.weatherState(false, false));
		assertEquals("rain weather", "rain", HumoralBarometerReadout.weatherState(true, false));
		assertEquals("thunder weather wins", "thunder", HumoralBarometerReadout.weatherState(true, true));
		assertEquals("inactive blood moon", "quiet", HumoralBarometerReadout.bloodMoonPressure(false, 0));
		assertEquals("active blood moon", "active_9m", HumoralBarometerReadout.bloodMoonPressure(true, 10800));
	}

	private static void assertEquals(String label, String expected, String actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
