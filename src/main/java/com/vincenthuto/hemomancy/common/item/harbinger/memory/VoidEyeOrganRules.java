package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public final class VoidEyeOrganRules {
    private VoidEyeOrganRules() {
    }

    public static boolean shouldConsumeOnUse(boolean creativeMode) {
        return true;
    }

    public static boolean shouldApplyCooldownAfterUse() {
        return false;
    }
}
