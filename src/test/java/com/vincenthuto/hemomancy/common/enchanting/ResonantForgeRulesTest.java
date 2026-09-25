package com.vincenthuto.hemomancy.common.enchanting;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResonantForgeRulesTest {
    private static ResonantPattern pattern(boolean master, ResonantPattern.Entry... entries) {
        return new ResonantPattern(List.of(entries), "", 0, 0, master);
    }

    @Test
    void pricesEveryRecordedLevelAndCurseSeverity() {
        ResonantPattern pattern = new ResonantPattern(List.of(
                new ResonantPattern.Entry("minecraft:sharpness", 5),
                new ResonantPattern.Entry("minecraft:unbreaking", 3)),
                "thirsting_edge", 2, 1, false);

        assertEquals(350, ResonantForgeRules.grindingCost(pattern));
        assertEquals(700, ResonantForgeRules.hammeringCost(pattern));
    }

    @Test
    void tierGatesSelectionAndMasterWork() {
        assertFalse(ResonantForgeRules.canSelectIndividual(ResonantForgeTier.BASE));
        assertTrue(ResonantForgeRules.canSelectIndividual(ResonantForgeTier.PRECISION));
        assertFalse(ResonantForgeRules.canUseMaster(ResonantForgeTier.PRECISION));
        assertTrue(ResonantForgeRules.canUseMaster(ResonantForgeTier.MASTERWORK));
    }

    @Test
    void higherRecordedLevelWinsWithoutCombiningEquals() {
        ResonantPattern existing = pattern(false,
                new ResonantPattern.Entry("minecraft:sharpness", 3),
                new ResonantPattern.Entry("minecraft:unbreaking", 3));
        ResonantPattern recorded = pattern(false,
                new ResonantPattern.Entry("minecraft:sharpness", 5),
                new ResonantPattern.Entry("minecraft:unbreaking", 3));

        ResonantForgeRules.Merge merge = ResonantForgeRules.merge(existing, recorded);

        assertTrue(merge.changed());
        assertEquals(List.of(
                new ResonantPattern.Entry("minecraft:sharpness", 5),
                new ResonantPattern.Entry("minecraft:unbreaking", 3)), merge.pattern().enchantments());
    }

    @Test
    void matchingCurseKeepsHigherSeverityAndDifferentCurseIsRejected() {
        ResonantPattern existing = new ResonantPattern(List.of(), "thirsting_edge", 1, 0, false);
        ResonantPattern matching = new ResonantPattern(List.of(), "thirsting_edge", 3, 0, false);
        ResonantPattern conflicting = new ResonantPattern(List.of(), "open_vessel", 2, 0, false);

        ResonantForgeRules.Merge merged = ResonantForgeRules.merge(existing, matching);

        assertTrue(merged.changed());
        assertEquals(3, merged.pattern().curseSeverity());
        assertEquals(ResonantForgeRules.Failure.DIFFERENT_SCRIPTORIUM_CURSE,
                ResonantForgeRules.merge(existing, conflicting).failure());
    }

    @Test
    void emptyOrEqualPlaybackMakesNoChange() {
        ResonantPattern existing = pattern(false, new ResonantPattern.Entry("minecraft:sharpness", 5));

        assertEquals(ResonantForgeRules.Failure.NO_CHANGE,
                ResonantForgeRules.merge(existing, existing).failure());
        assertEquals(ResonantForgeRules.Failure.EMPTY_PATTERN,
                ResonantForgeRules.merge(existing, ResonantPattern.EMPTY).failure());
    }

    @Test
    void onlyCompletelyBlankCylindersMayBeRecorded() {
        assertTrue(ResonantForgeRules.isBlankCylinder(false, false, false));
        assertFalse(ResonantForgeRules.isBlankCylinder(true, false, false));
        assertFalse(ResonantForgeRules.isBlankCylinder(false, true, false));
        assertFalse(ResonantForgeRules.isBlankCylinder(false, false, true));
    }

    @Test
    void precisionSelectionCanSeparateCurseFromEnchantments() {
        ResonantPattern complete = new ResonantPattern(List.of(
                new ResonantPattern.Entry("minecraft:sharpness", 6),
                new ResonantPattern.Entry("minecraft:unbreaking", 3)),
                "thirsting_edge", 2, 1, false);

        ResonantPattern curse = ResonantForgeRules.select(complete, ResonantForgeRules.CURSE_SELECTION);
        ResonantPattern sharpness = ResonantForgeRules.select(complete, "minecraft:sharpness");

        assertEquals(List.of(), curse.enchantments());
        assertEquals("thirsting_edge", curse.curse());
        assertEquals(List.of(new ResonantPattern.Entry("minecraft:sharpness", 6)), sharpness.enchantments());
        assertEquals("", sharpness.curse());
    }

    @Test
    void maintenanceBeginsOnlyAfterCompletedServiceInterval() {
        assertFalse(ResonantForgeRules.hammerServiceRequired(63));
        assertTrue(ResonantForgeRules.hammerServiceRequired(64));
        assertFalse(ResonantForgeRules.wheelServiceRequired(31));
        assertTrue(ResonantForgeRules.wheelServiceRequired(32));
    }
}
