package com.vincenthuto.hemomancy.common.damage;

public final class SchoolStateRules {
    private SchoolStateRules() {}

    public static int pressure(int current, boolean bloodless) {
        return bloodless ? 0 : Math.clamp(current + 1, 0, 3);
    }

    public static float ruptureDamage(int levels) {
        return 2 * Math.clamp(levels, 0, 3);
    }

    public static int rime(int current, int added, long now, long lastBuild) {
        return now - lastBuild < 10 ? current : Math.clamp(current + added, 0, 3);
    }

    public static double rimeSlow(int levels) {
        return 0.15 * Math.clamp(levels, 0, 3);
    }

    public static float necrosisDebt(float stored, float healthDamage, boolean direct) {
        return Math.clamp(stored + (direct ? Math.max(0, healthDamage) * 0.25f : 0), 0, 6);
    }

    public static float healingMultiplier(boolean necrosis, boolean hunger) {
        return hunger ? 0.25f : necrosis ? 0.75f : 1;
    }

    public static float combustionDamage(long remainingTicks) {
        return Math.clamp((remainingTicks + 19) / 20, 0, 4);
    }

    public static float combustionDamage(long expires, long nextTick, long now) {
        long first = Math.max(nextTick, now);
        return first > expires ? 0 : Math.clamp(1 + (expires - first) / 20, 0, 4);
    }

    public static int rimeAfterHeat(int levels) {
        return Math.max(0, levels - 1);
    }

    public static boolean mayIgnite(int rime, boolean wet, boolean fireImmune) {
        return rime == 0 && !wet && !fireImmune;
    }

    public static boolean canPerceive(boolean obscured, boolean veiled, boolean illuminated,
                                     boolean scriptedBoss, double distanceSquared) {
        return scriptedBoss || illuminated || distanceSquared <= 16 || (!obscured && !veiled);
    }
}
