package com.vincenthuto.hemomancy.common.enchanting;

public final class ScriptoriumBalance {
    private static final int[] BIAS_STRAIN = {0, 3, 7, 12};
    private static final int[] EXCESS_STRAIN = {0, 18, 40, 55, 70, 82, 90};
    private static final int[] USE_BLOOD = {0, 0, 2, 4, 6, 8, 12};
    private static final double[] WEAR = {1, 1.5, 2, 2.5, 3, 3.5, 4};

    private ScriptoriumBalance() {}

    public static int enzymeCost(int level, int naturalCap) {
        return Math.clamp((int) Math.ceil(3.0 * level / Math.max(1, naturalCap)), 1, 3);
    }

    public static double weight(int base, int primaryUnits, int secondaryUnits) {
        return base * Math.min(3.5, 1 + primaryUnits * 0.75 + secondaryUnits * 0.35);
    }

    public static int normalStrain(int selectedUnits, int resultCount, int atCapCount) {
        int bias = BIAS_STRAIN[Math.clamp(selectedUnits, 0, 3)];
        int result = Math.max(0, resultCount - atCapCount) * 2 + atCapCount * 5;
        return Math.min(45, bias + result + Math.max(0, resultCount - 1) * 3);
    }

    public static int totalStrain(int selectedUnits, int resultCount, int atCapCount, int excessLoad) {
        return Math.min(excessLoad > 0 ? 95 : 45,
                normalStrain(selectedUnits, resultCount, atCapCount) + excessStrain(excessLoad));
    }

    public static int excessStrain(int excessLoad) {
        return EXCESS_STRAIN[Math.clamp(excessLoad, 0, 6)];
    }

    public static int useBloodCost(int excessLoad) {
        return USE_BLOOD[Math.clamp(excessLoad, 0, 6)];
    }

    public static double wearMultiplier(int excessLoad) {
        return WEAR[Math.clamp(excessLoad, 0, 6)];
    }
}
