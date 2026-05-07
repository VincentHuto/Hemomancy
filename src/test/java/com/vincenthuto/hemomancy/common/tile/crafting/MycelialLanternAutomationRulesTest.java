package com.vincenthuto.hemomancy.common.tile.crafting;

public final class MycelialLanternAutomationRulesTest {

    public static void main(String[] args) {
        assertSlots(MycelialLanternAutomationRules.Side.TOP, new int[] { 0 });
        assertSlots(MycelialLanternAutomationRules.Side.SIDE, new int[] { 1 });
        assertSlots(MycelialLanternAutomationRules.Side.BOTTOM, new int[] { 2, 3 });

        assertTrue(MycelialLanternAutomationRules.canInsert(0, MycelialLanternAutomationRules.Side.TOP), "top inserts culture");
        assertFalse(MycelialLanternAutomationRules.canInsert(1, MycelialLanternAutomationRules.Side.TOP), "top cannot insert blood");
        assertTrue(MycelialLanternAutomationRules.canInsert(1, MycelialLanternAutomationRules.Side.SIDE), "side inserts blood");
        assertFalse(MycelialLanternAutomationRules.canInsert(0, MycelialLanternAutomationRules.Side.SIDE), "side cannot insert culture");
        assertFalse(MycelialLanternAutomationRules.canInsert(2, MycelialLanternAutomationRules.Side.BOTTOM), "bottom cannot insert output");

        assertTrue(MycelialLanternAutomationRules.canExtract(2, MycelialLanternAutomationRules.Side.BOTTOM), "bottom extracts enzymes");
        assertTrue(MycelialLanternAutomationRules.canExtract(3, MycelialLanternAutomationRules.Side.BOTTOM), "bottom extracts empties");
        assertFalse(MycelialLanternAutomationRules.canExtract(0, MycelialLanternAutomationRules.Side.BOTTOM), "bottom cannot extract culture");
        assertFalse(MycelialLanternAutomationRules.canExtract(2, MycelialLanternAutomationRules.Side.SIDE), "side cannot extract enzymes");
    }

    private static void assertSlots(MycelialLanternAutomationRules.Side side, int[] expected) {
        int[] actual = MycelialLanternAutomationRules.slotsFor(side);
        assertEquals(expected.length, actual.length, "slot count for " + side);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], "slot " + i + " for " + side);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void assertFalse(boolean value, String message) {
        if (value) throw new AssertionError(message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
