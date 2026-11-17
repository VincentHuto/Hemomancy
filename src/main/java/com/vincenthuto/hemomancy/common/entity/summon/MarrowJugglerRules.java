package com.vincenthuto.hemomancy.common.entity.summon;

public final class MarrowJugglerRules {
    /** NQ-15-7: the third dagger of a volley pins its target for about 1.5 seconds, with no blood loss. */
    public static final int PIN_TICKS = 30;
    private MarrowJugglerRules() {}
    public static boolean throwTick(int tick) { return tick == 10 || tick == 15 || tick == 20; }
    public static boolean pinsTarget(int tick) { return tick == 20; }
    public static float daggerDamage(double attackDamage) { return (float) (Math.max(2, attackDamage) / 3.0); }
    public static float impactDamage(double daggerDamage, double projectileSpeed) {
        // Divide the former arrow's rounded impact, rather than rounding three smaller arrows up.
        return (float) (Math.ceil(projectileSpeed * daggerDamage * 3.0 - .000001) / 3.0);
    }
}
