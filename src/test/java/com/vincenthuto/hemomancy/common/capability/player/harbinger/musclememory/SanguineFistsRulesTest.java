package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SanguineFistsRulesTest {

    @Test
    void directMeleeHitConsumesBloodAndAddsDamageAndArmStrain() {
        SanguineFistsRules.Result result = SanguineFistsRules.evaluate(true, true, false, 10.0);

        assertTrue(result.triggers());
        assertEquals(3.0, result.bloodCost());
        assertEquals(2.0F, result.bonusDamage());
        assertEquals(0.25F, result.armStrain());
    }

    @Test
    void insufficientBloodLeavesAttackUntouched() {
        SanguineFistsRules.Result result = SanguineFistsRules.evaluate(true, true, false, 2.99);

        assertFalse(result.triggers());
        assertEquals(0.0, result.bloodCost());
        assertEquals(0.0F, result.bonusDamage());
        assertEquals(0.0F, result.armStrain());
    }

    @Test
    void indirectOrDuplicateDamageNeverTriggers() {
        assertFalse(SanguineFistsRules.evaluate(true, false, false, 10.0).triggers());
        assertFalse(SanguineFistsRules.evaluate(true, true, true, 10.0).triggers());
    }
}
