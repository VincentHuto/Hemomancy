package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathSiphonRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeGrowthRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielCleansingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumStillnessRules;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaintPowerRulesTest {
    @Test void aSingleNearbyBloodSourceCanOutfeedTheLeakWithoutDuplicatingSpend() {
        float stored = 0;
        for (int tick = 1; tick <= 200; tick++) {
            stored = HemorathSiphonRules.leak(stored);
            if (tick % 20 == 0) stored += HemorathSiphonRules.AURA_BLOOD;
        }
        assertTrue(stored > 0);
        assertEquals(0, HemorathSiphonRules.leak(0));
        assertEquals(0, HemorathSiphonRules.leak(.01F));
    }
    @Test void transfusionHealsWoundsButFullHealthAccumulatesDangerousGrowth() {
        assertEquals(2, SeraphaeGrowthRules.healing(10, 20));
        assertEquals(.5F, SeraphaeGrowthRules.healing(19.5F, 20));
        assertEquals(0, SeraphaeGrowthRules.healing(20, 20));
        assertFalse(SeraphaeGrowthRules.convulses(1));
        assertTrue(SeraphaeGrowthRules.convulses(2));
    }
    @Test void cleansingHasAVisibleWindupAndNeverIgnitesAtTheOpenOrClosingBoundary() {
        assertFalse(PutricielCleansingRules.burns(80));
        assertTrue(PutricielCleansingRules.burns(60));
        assertTrue(PutricielCleansingRules.burns(20));
        assertFalse(PutricielCleansingRules.burns(0));
        assertFalse(PutricielCleansingRules.burns(61));
    }
    @Test void movementAndAnInterruptedSaintReleaseStillnessInsteadOfRefreshingTheRoot() {
        assertEquals(100, VelorumStillnessRules.next(100, false, false));
        assertEquals(78, VelorumStillnessRules.next(80, true, false));
        assertEquals(75, VelorumStillnessRules.next(80, false, true));
        assertEquals(0, VelorumStillnessRules.next(2, false, true));
        assertFalse(VelorumStillnessRules.rooted(79));
        assertTrue(VelorumStillnessRules.rooted(80));
    }
}
