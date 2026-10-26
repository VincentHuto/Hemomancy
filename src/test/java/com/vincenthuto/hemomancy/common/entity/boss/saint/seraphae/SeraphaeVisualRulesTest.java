package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeraphaeVisualRulesTest {
    @Test void anticipationPrecedesTheWingSpreadAndStrain() {
        var recoil = SeraphaeVisualRules.pose(SeraphaeState.FRACTURING, 92, 8);
        var opening = SeraphaeVisualRules.pose(SeraphaeState.FRACTURING, 60, 40);
        var strain = SeraphaeVisualRules.pose(SeraphaeState.FRACTURING, 8, 92);
        assertTrue(recoil.recoil() > .9F);
        assertEquals(0, recoil.spread());
        assertTrue(opening.spread() > recoil.spread());
        assertTrue(strain.spread() > .95F && strain.strain() > .95F);
        assertEquals(SeraphaeState.FRACTURING.duration(), SeraphaeVisualRules.FRACTURE.total());
        assertEquals(SeraphaeState.CONDENSING.duration(), SeraphaeVisualRules.CONDENSE.total());
    }

    @Test void condensationEndsInTheStartingStablePose() {
        var end = SeraphaeVisualRules.pose(SeraphaeState.CONDENSING, 0, 100);
        var start = SeraphaeVisualRules.pose(SeraphaeState.STABLE, 200, 100);
        assertEquals(end.fold(), start.fold());
        assertEquals(end.spread(), start.spread());
        assertEquals(end.lift(), start.lift());
        assertEquals(0, SeraphaeVisualRules.pose(SeraphaeState.STABLE, 160, 140).fold());
    }

    @Test void everyTimelineChannelStaysFiniteAndWithinItsPhysicalRange() {
        for (var state : SeraphaeState.values()) for (int remaining = -20; remaining < 240; remaining++) {
            var pose = SeraphaeVisualRules.pose(state, remaining, remaining * 1.7F);
            for (float value : new float[]{pose.spread(), pose.fold(), pose.recoil(), pose.strain(), pose.lift(), pose.glow()})
                assertTrue(Float.isFinite(value) && value >= 0 && value <= 1, state + ": " + value);
        }
    }

    @Test void burstsAreVisibleImmediatelyAndFrayToZeroBeforeExpiry() {
        assertEquals(1, SeraphaeVisualRules.burstOpacity(0, 24));
        assertTrue(SeraphaeVisualRules.burstOpacity(23, 24) < .01F);
        assertEquals(0, SeraphaeVisualRules.burstOpacity(24, 24));
        assertEquals(0, SeraphaeVisualRules.burstOpacity(-1, 24));
    }
}
