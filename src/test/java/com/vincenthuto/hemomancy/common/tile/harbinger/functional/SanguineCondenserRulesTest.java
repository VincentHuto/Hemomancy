package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SanguineCondenserRulesTest {
    @Test
    void numbersFollowTheStageTable() {
        assertEquals(300, SanguineCondenserRules.poolCapacity(0));
        assertEquals(750, SanguineCondenserRules.poolCapacity(1));
        assertEquals(1500, SanguineCondenserRules.poolCapacity(2));
        assertEquals(150, SanguineCondenserRules.formationCost(0));
        assertEquals(125, SanguineCondenserRules.formationCost(1));
        assertEquals(100, SanguineCondenserRules.formationCost(2));
        assertEquals(200, SanguineCondenserRules.ticksPerFormation(0));
        assertEquals(120, SanguineCondenserRules.ticksPerFormation(1));
        assertEquals(60, SanguineCondenserRules.ticksPerFormation(2));
        assertEquals(1, SanguineCondenserRules.heldCapacity(0));
        assertEquals(4, SanguineCondenserRules.heldCapacity(1));
        assertEquals(8, SanguineCondenserRules.heldCapacity(2));
        assertEquals(0, SanguineCondenserRules.bonusChance(0));
        assertEquals(0, SanguineCondenserRules.bonusChance(1));
        assertEquals(0.25, SanguineCondenserRules.bonusChance(2));
    }

    @Test
    void collapseOnlyAtStageZeroAndSkillRemovesIt() {
        assertEquals(0.25, SanguineCondenserRules.collapseChance(0, 0), 1e-9);
        assertEquals(0.15, SanguineCondenserRules.collapseChance(0, 2), 1e-9);
        assertEquals(0, SanguineCondenserRules.collapseChance(0, 5), 1e-9);
        assertEquals(0, SanguineCondenserRules.collapseChance(0, 9), 1e-9);
        assertEquals(0, SanguineCondenserRules.collapseChance(1, 0), 1e-9);
        assertEquals(0, SanguineCondenserRules.collapseChance(2, 0), 1e-9);
    }

    @Test
    void bloodSurfaceRisesFromTheFloorToTheVanillaBrim() {
        assertEquals(0.25, SanguineCondenserRules.surfaceHeight(0), 1e-9);
        assertEquals(0.9375, SanguineCondenserRules.surfaceHeight(1), 1e-9);
        assertEquals(0.9375, SanguineCondenserRules.surfaceHeight(3), 1e-9);
        assertEquals(0.25, SanguineCondenserRules.surfaceHeight(-1), 1e-9);
    }

    @Test
    void outOfRangeStagesClamp() {
        assertEquals(300, SanguineCondenserRules.poolCapacity(-1));
        assertEquals(1500, SanguineCondenserRules.poolCapacity(7));
    }
}
