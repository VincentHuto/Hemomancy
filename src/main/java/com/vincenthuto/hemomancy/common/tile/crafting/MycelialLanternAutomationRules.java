package com.vincenthuto.hemomancy.common.tile.crafting;

public final class MycelialLanternAutomationRules {

    public enum Side {
        TOP,
        SIDE,
        BOTTOM
    }

    public static final int SLOT_CULTURE = 0;
    public static final int SLOT_BLOOD = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_EMPTY_CONTAINER = 3;

    private static final int[] TOP_SLOTS = { SLOT_CULTURE };
    private static final int[] SIDE_SLOTS = { SLOT_BLOOD };
    private static final int[] BOTTOM_SLOTS = { SLOT_OUTPUT, SLOT_EMPTY_CONTAINER };

    private MycelialLanternAutomationRules() {
    }

    public static int[] slotsFor(Side side) {
        return switch (side) {
            case TOP -> TOP_SLOTS;
            case BOTTOM -> BOTTOM_SLOTS;
            case SIDE -> SIDE_SLOTS;
        };
    }

    public static boolean canInsert(int slot, Side side) {
        return (side == Side.TOP && slot == SLOT_CULTURE)
                || (side == Side.SIDE && slot == SLOT_BLOOD);
    }

    public static boolean canExtract(int slot, Side side) {
        return side == Side.BOTTOM && (slot == SLOT_OUTPUT || slot == SLOT_EMPTY_CONTAINER);
    }
}
