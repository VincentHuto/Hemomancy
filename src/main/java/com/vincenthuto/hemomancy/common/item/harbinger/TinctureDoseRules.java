package com.vincenthuto.hemomancy.common.item.harbinger;

public final class TinctureDoseRules {
    private TinctureDoseRules() {}

    public static int normalizeRemaining(Integer remaining, int maximum) {
        int safeMaximum = Math.max(1, maximum);
        return remaining == null ? safeMaximum : Math.max(0, Math.min(safeMaximum, remaining));
    }

    public static int consume(int remaining) {
        return Math.max(0, remaining - 1);
    }
}
