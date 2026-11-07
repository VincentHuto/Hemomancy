package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeVisualRulesTest {
    @Test void nerveTipsLagTheRootAndStrainWidensTheWave() {
        float root = SeraphaeVisualRules.nerveWave(30, 0, 1, 0);
        float tip = SeraphaeVisualRules.nerveWave(30, 2, 1, 0);
        assertNotEquals(root, tip, 1.0E-4F);
        float calm = 0, strained = 0;
        for (int age = 0; age < 200; age++) {
            calm = Math.max(calm, Math.abs(SeraphaeVisualRules.nerveWave(age, 2, 1, 0)));
            strained = Math.max(strained, Math.abs(SeraphaeVisualRules.nerveWave(age, 2, 1, 1)));
        }
        assertTrue(strained > calm * 2);
    }

    @Test void renderScaleMakesSixteenTexelsPerBlock() {
        assertEquals(2.0F, SeraphaeVisualRules.MODEL_SCALE);
        assertEquals(16.0F, SeraphaeVisualRules.TEXELS * 16.0F / SeraphaeVisualRules.MODEL_SCALE, 1.0E-4F);
    }

    @Test void everyCueKindHasALife() {
        for (int kind = 1; kind <= 13; kind++) assertTrue(SeraphaeVisualRules.life(kind) > 0, "kind " + kind);
        assertEquals(SeraphaeVisualRules.CLOSE_TICKS, SeraphaeVisualRules.life(SeraphaeVisualRules.CLOSE));
        assertEquals(SeraphaeVisualRules.SEAL_TICKS, SeraphaeVisualRules.life(SeraphaeVisualRules.SEALED));
    }

    @Test void burstsAreVisibleImmediatelyAndFrayToZeroBeforeExpiry() {
        assertEquals(1, SeraphaeVisualRules.burstOpacity(0, 24));
        assertTrue(SeraphaeVisualRules.burstOpacity(23, 24) < .01F);
        assertEquals(0, SeraphaeVisualRules.burstOpacity(24, 24));
        assertEquals(0, SeraphaeVisualRules.burstOpacity(-1, 24));
    }
}
