package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistillationConsumptionRulesTest {

    @Test
    void tinctureConsumesReagentsAndEmptyPackaging() {
        DistillationConsumptionRules.Consumption consumption =
                DistillationConsumptionRules.forRecipe(true, true);

        assertEquals(1, consumption.mainInput());
        assertEquals(1, consumption.catalyst());
        assertEquals(1, consumption.vesselInput());
    }

    @Test
    void legacyCatalystRecipeStillConsumesOnlyMainInput() {
        DistillationConsumptionRules.Consumption consumption =
                DistillationConsumptionRules.forRecipe(false, false);

        assertEquals(1, consumption.mainInput());
        assertEquals(0, consumption.catalyst());
        assertEquals(0, consumption.vesselInput());
    }
}
