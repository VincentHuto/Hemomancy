package com.vincenthuto.hemomancy.common.tile;

public final class BloodContainerTransferTest {

    public static void main(String[] args) {
        assertFalse(BloodContainerTransfer.canAcceptFilledContainer(3900, 4000, 2500),
                "filled containers require their full amount of free capacity");
        assertTrue(BloodContainerTransfer.canAcceptFilledContainer(1500, 4000, 2500),
                "filled containers fit exactly when missing capacity equals their amount");
        assertEquals(10, BloodContainerTransfer.calculateGourdTransfer(500, 1000, 4000, 10),
                "gourd transfer is rate limited");
        assertEquals(4, BloodContainerTransfer.calculateGourdTransfer(500, 3996, 4000, 10),
                "gourd transfer is tank-space limited");
        assertEquals(3, BloodContainerTransfer.calculateGourdTransfer(3, 1000, 4000, 10),
                "gourd transfer is source limited");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void assertFalse(boolean value, String message) {
        if (value) throw new AssertionError(message);
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
