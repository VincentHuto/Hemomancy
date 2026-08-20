package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.shared.NoeticDiscoveryRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoeticDiscoveryRulesTest {
    @Test
    void conductiveMarkHasTwoDegreeThreeRecognitionRoutes() {
        assertFalse(NoeticDiscoveryRules.canRecognizeConductiveMark(2, true, true));
        assertFalse(NoeticDiscoveryRules.canRecognizeConductiveMark(3, false, false));
        assertTrue(NoeticDiscoveryRules.canRecognizeConductiveMark(3, true, false));
        assertTrue(NoeticDiscoveryRules.canRecognizeConductiveMark(3, false, true));
    }
}
