package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

public final class SanguineFistsRules {
    public static final double BLOOD_COST = 3.0;
    public static final float BONUS_DAMAGE = 2.0F;
    public static final float ARM_STRAIN = 0.25F;
    public static final int TARGET_GUARD_TICKS = 4;

    private SanguineFistsRules() {
    }

    public static Result evaluate(boolean primed, boolean directMelee, boolean duplicate, double bloodAvailable) {
        if (!primed || !directMelee || duplicate || bloodAvailable < BLOOD_COST) {
            return Result.NONE;
        }
        return new Result(true, BLOOD_COST, BONUS_DAMAGE, ARM_STRAIN);
    }

    public record Result(boolean triggers, double bloodCost, float bonusDamage, float armStrain) {
        private static final Result NONE = new Result(false, 0.0, 0.0F, 0.0F);
    }
}
