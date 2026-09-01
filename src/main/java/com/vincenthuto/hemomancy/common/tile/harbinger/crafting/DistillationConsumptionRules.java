package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

public final class DistillationConsumptionRules {
    private DistillationConsumptionRules() {
    }

    public static Consumption forRecipe(boolean consumeCatalyst, boolean requiresBloodInput) {
        return new Consumption(1, consumeCatalyst ? 1 : 0, requiresBloodInput ? 1 : 0);
    }

    public record Consumption(int mainInput, int catalyst, int bloodInput) {
    }
}
