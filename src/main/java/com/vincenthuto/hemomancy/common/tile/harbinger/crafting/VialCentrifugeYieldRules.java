package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

/** Ordinary specimen recovery; sacred residuum never receives quantity or fraction bonuses. */
public final class VialCentrifugeYieldRules {
    private VialCentrifugeYieldRules() {}

    public static int duration(int stage) { return 200 - Math.clamp(stage, 0, 2) * 50; }

    public static int quantity(int base, int stage, double fractionalRoll) {
        double bonus = base * Math.clamp(stage, 0, 2) * .25;
        int whole = (int) bonus;
        return base + whole + (fractionalRoll < bonus - whole ? 1 : 0);
    }

    public static boolean powder(int stage, double roll) {
        return roll < .5 + Math.clamp(stage, 0, 2) * .25;
    }

    public static boolean secondFraction(int stage, int outputTypes, double roll) {
        return stage == 2 && outputTypes > 1 && roll < .5;
    }
}
