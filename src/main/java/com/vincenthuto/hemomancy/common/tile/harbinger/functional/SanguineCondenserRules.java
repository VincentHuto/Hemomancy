package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

/** Pool, pace and yield of a Sanguine Condenser at each installed stage (0 = Sanguine Cauldron). */
public final class SanguineCondenserRules {
    private SanguineCondenserRules() {
    }

    public static double poolCapacity(int stage) {
        return switch (clamp(stage)) {
            case 0 -> 300.0D;
            case 1 -> 750.0D;
            default -> 1500.0D;
        };
    }

    public static double formationCost(int stage) {
        return switch (clamp(stage)) {
            case 0 -> 150.0D;
            case 1 -> 125.0D;
            default -> 100.0D;
        };
    }

    public static int ticksPerFormation(int stage) {
        return switch (clamp(stage)) {
            case 0 -> 200;
            case 1 -> 120;
            default -> 60;
        };
    }

    public static int heldCapacity(int stage) {
        return switch (clamp(stage)) {
            case 0 -> 1;
            case 1 -> 4;
            default -> 8;
        };
    }

    /** Only a bare cauldron can lose a formation; Sanguine Crystallization steadies it by 5% a level. */
    public static double collapseChance(int stage, int crystallizationLevel) {
        return clamp(stage) == 0 ? Math.max(0.0D, 0.25D - 0.05D * crystallizationLevel) : 0.0D;
    }

    public static double bonusChance(int stage) {
        return clamp(stage) == 2 ? 0.25D : 0.0D;
    }

    /** Blood surface above the block's floor, from the cauldron's inner floor (4/16) to its brim (15/16). */
    public static double surfaceHeight(double fill) {
        return 0.25D + 0.6875D * Math.clamp(fill, 0.0D, 1.0D);
    }

    private static int clamp(int stage) {
        return Math.clamp(stage, 0, 2);
    }
}
