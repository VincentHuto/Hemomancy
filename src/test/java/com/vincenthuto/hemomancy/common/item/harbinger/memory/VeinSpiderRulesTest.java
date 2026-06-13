package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public final class VeinSpiderRulesTest {
    public static void main(String[] args) {
        transferSideProbeIncludesAllSidesBeforeNullFallback();
        courierProgressExpiresAfterDuration();
        partialFirstEndpointStoresSecondEndpointNext();
    }

    private static void transferSideProbeIncludesAllSidesBeforeNullFallback() {
        int[] sides = VeinSpiderRules.transferSideProbeOrder();

        assertEquals(7, sides.length, "side probe count");
        assertEquals(VeinSpiderRules.SIDE_DOWN, sides[0], "down side first");
        assertEquals(VeinSpiderRules.SIDE_UP, sides[1], "up side second");
        assertEquals(VeinSpiderRules.SIDE_NORTH, sides[2], "north side third");
        assertEquals(VeinSpiderRules.SIDE_SOUTH, sides[3], "south side fourth");
        assertEquals(VeinSpiderRules.SIDE_WEST, sides[4], "west side fifth");
        assertEquals(VeinSpiderRules.SIDE_EAST, sides[5], "east side sixth");
        assertEquals(VeinSpiderRules.SIDE_NULL, sides[6], "null side fallback last");
    }

    private static void courierProgressExpiresAfterDuration() {
        assertEquals(0.0F, VeinSpiderRules.courierProgress(20, 20, 0.0F), "new courier progress");
        assertEquals(0.5F, VeinSpiderRules.courierProgress(10, 20, 0.0F), "halfway courier progress");
        assertTrue(VeinSpiderRules.isCourierExpired(0), "zero remaining ticks expires");
        assertFalse(VeinSpiderRules.isCourierExpired(1), "positive remaining ticks stays visible");
    }

    private static void partialFirstEndpointStoresSecondEndpointNext() {
        assertEquals(VeinSpiderRules.EndpointAction.STORE_FIRST,
                VeinSpiderRules.endpointAction(false, false, false), "empty stack stores first endpoint");
        assertEquals(VeinSpiderRules.EndpointAction.STORE_SECOND,
                VeinSpiderRules.endpointAction(true, false, false), "partial link stores second endpoint");
        assertEquals(VeinSpiderRules.EndpointAction.STORE_SECOND,
                VeinSpiderRules.endpointAction(true, false, true), "sneaking with partial link still completes link");
        assertEquals(VeinSpiderRules.EndpointAction.STORE_FIRST,
                VeinSpiderRules.endpointAction(true, true, true), "sneaking with complete link restarts source");
        assertEquals(VeinSpiderRules.EndpointAction.STORE_SECOND,
                VeinSpiderRules.endpointAction(true, true, false), "complete link can be retargeted without sneaking");
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " got " + actual);
        }
    }

    private static void assertEquals(float expected, float actual, String label) {
        if (Math.abs(expected - actual) > 0.001F) {
            throw new AssertionError(label + ": expected " + expected + " got " + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean value, String label) {
        if (value) {
            throw new AssertionError(label);
        }
    }
}
