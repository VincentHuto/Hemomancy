package com.vincenthuto.hemomancy.common.brewing;

public final class BoundBrewRules {
    private BoundBrewRules() {}

    public static int bindDoses() { return 1; }

    public static int refillDoses(int current) {
        if (current < 0 || current >= 3) throw new IllegalArgumentException("vessel is full or invalid");
        return current + 1;
    }

    public static boolean substitutesDose(double spendableBlood, double roll) {
        return spendableBlood >= 1000 && roll >= 0 && roll < 0.2;
    }
}
