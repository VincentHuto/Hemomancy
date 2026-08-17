package com.vincenthuto.hemomancy.common.tile.crafting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistillationConsumptionRulesTest {

    @Test
    void tinctureConsumesAllThreeRecipeInputs() {
        DistillationConsumptionRules.Consumption consumption =
                DistillationConsumptionRules.forRecipe(true, true);

        assertEquals(1, consumption.mainInput());
        assertEquals(1, consumption.catalyst());
        assertEquals(1, consumption.bloodInput());
    }

    @Test
    void legacyCatalystRecipeStillConsumesOnlyMainInput() {
        DistillationConsumptionRules.Consumption consumption =
                DistillationConsumptionRules.forRecipe(false, false);

        assertEquals(1, consumption.mainInput());
        assertEquals(0, consumption.catalyst());
        assertEquals(0, consumption.bloodInput());
    }
}
