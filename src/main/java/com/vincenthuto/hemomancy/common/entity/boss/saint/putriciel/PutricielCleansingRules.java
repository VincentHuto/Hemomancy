package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

/** Cleansing fire waits for the colony's opening recoil before reaching nearby bodies. */
public final class PutricielCleansingRules {
    public static final double RADIUS = 3;
    public static final int WARNING = 20;
    private PutricielCleansingRules() {}
    public static boolean burns(int remaining) {
        return remaining > 0 && remaining <= PutricielVisualRules.ABSOLUTION_WINDOW - WARNING && remaining % 20 == 0;
    }
}
