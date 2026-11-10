package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpenings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeIntegrityRulesTest {
    @Test void startsAtThirtyWithPhasesAtFortyAndSeventyFive() {
        assertEquals(30.0F, SeraphaeIntegrityRules.START);
        assertArrayEquals(new float[]{0.40F, 0.75F, 1.0F}, SeraphaeIntegrityRules.THRESHOLDS);
        assertEquals(1.0F, SeraphaeIntegrityRules.THRESHOLDS[SeraphaeIntegrityRules.CONTAINED_THRESHOLD]);
    }

    @Test void decayScalesByHalfPerExtraChallengerAndEachFragmentPulls() {
        assertEquals(0.010F, SeraphaeIntegrityRules.decay(1, 0), 1.0E-6F);
        assertEquals(0.020F, SeraphaeIntegrityRules.decay(3, 0), 1.0E-6F);
        assertEquals(0.010F + 3 * 0.008F, SeraphaeIntegrityRules.decay(1, 3), 1.0E-6F);
        // twelve a minute solo, thirty with four
        assertEquals(12.0F, SeraphaeIntegrityRules.decay(1, 0) * 1200, 1.0E-3F);
        assertEquals(30.0F, SeraphaeIntegrityRules.decay(4, 0) * 1200, 1.0E-3F);
    }

    @Test void openingHitsGainByWindowDeliveryAndSchool() {
        int recoil = SeraphaeOpenings.RECOIL_ID, lapse = SeraphaeOpenings.LAPSE_ID;
        assertEquals(2.0F, SeraphaeIntegrityRules.hitGain(recoil, SaintOpening.Delivery.MELEE, null));
        assertEquals(2.0F, SeraphaeIntegrityRules.hitGain(recoil, SaintOpening.Delivery.PROJECTILE, null));
        assertEquals(3.0F, SeraphaeIntegrityRules.hitGain(recoil, SaintOpening.Delivery.SCHOOL_CAST, EnumBloodTemper.ANIMUS));
        assertEquals(4.5F, SeraphaeIntegrityRules.hitGain(recoil, SaintOpening.Delivery.SCHOOL_CAST, EnumBloodTemper.CONGEATIO));
        assertEquals(1.5F, SeraphaeIntegrityRules.hitGain(recoil, SaintOpening.Delivery.SCHOOL_CAST, EnumBloodTemper.LUX));
        assertEquals(3.0F, SeraphaeIntegrityRules.hitGain(lapse, SaintOpening.Delivery.MELEE, null));
        assertEquals(6.0F, SeraphaeIntegrityRules.hitGain(lapse, SaintOpening.Delivery.SCHOOL_CAST, EnumBloodTemper.MORTEM));
        assertEquals(2.0F, SeraphaeIntegrityRules.hitGain(lapse, SaintOpening.Delivery.SCHOOL_CAST, EnumBloodTemper.DUCTILIS));
        assertEquals(3.0F, SeraphaeIntegrityRules.hitGain(SeraphaeOpenings.SEIZED_ID, SaintOpening.Delivery.SCHOOL_CAST, null));
    }

    @Test void capsGrowByOnePerExtraChallengerAndThenHitsCountNothing() {
        SaintOpenings openings = new SaintOpenings();
        openings.open(SeraphaeOpenings.RECOIL, 0);
        for (int i = 0; i < 4; i++) assertEquals(SaintOpening.Admit.COUNTED, openings.admit(SaintOpening.Delivery.MELEE, 1));
        assertEquals(SaintOpening.Admit.CAPPED, openings.admit(SaintOpening.Delivery.MELEE, 1));
        assertEquals(5, SeraphaeOpenings.RECOIL.cap(2));
        assertEquals(8, SeraphaeOpenings.CONDENSING.cap(4));
        assertEquals(SaintOpening.Admit.NOT_COUNTED, openings.admit(SaintOpening.Delivery.CHANNEL, 1));
    }

    @Test void theCondensingFeedCountsBloodUpToItsCap() {
        assertEquals(0.25F, SeraphaeIntegrityRules.feed(4.0D, 0.0F), 1.0E-6F);
        assertEquals(1.0F, SeraphaeIntegrityRules.feed(16.0D, 0.0F), 1.0E-6F);
        assertEquals(0.5F, SeraphaeIntegrityRules.feed(16.0D, 9.5F), 1.0E-6F);
        assertEquals(0.0F, SeraphaeIntegrityRules.feed(16.0D, 10.0F), 1.0E-6F);
    }

    @Test void fragmentsAnchorsAndLossesHaveTheirNumbers() {
        assertEquals(3.0F, SeraphaeIntegrityRules.fragment(SeraphaeIntegrityRules.FragmentEnd.ABSORBED));
        assertEquals(5.0F, SeraphaeIntegrityRules.fragment(SeraphaeIntegrityRules.FragmentEnd.CAPTURED));
        assertEquals(1.5F, SeraphaeIntegrityRules.fragment(SeraphaeIntegrityRules.FragmentEnd.STRUCK));
        assertEquals(-2.0F, SeraphaeIntegrityRules.fragment(SeraphaeIntegrityRules.FragmentEnd.GRAFTED));
        assertEquals(-4.0F, SeraphaeIntegrityRules.fragment(SeraphaeIntegrityRules.FragmentEnd.RETURNED));
        assertEquals(0.0F, SeraphaeIntegrityRules.fragment(SeraphaeIntegrityRules.FragmentEnd.RECALLED));
        assertEquals(10.0F, SeraphaeIntegrityRules.reserveWoken(true));
        assertEquals(5.0F, SeraphaeIntegrityRules.reserveWoken(false));
        assertEquals(8.0F, SeraphaeIntegrityRules.REBIND);
        assertEquals(-6.0F, SeraphaeIntegrityRules.SLIP);
        assertEquals(-3.0F, SeraphaeIntegrityRules.BURST);
    }

    @Test void theFloodFiresOncePerFallAndRearmsAboveTwelve() {
        assertTrue(SeraphaeIntegrityRules.floods(0.0F, true));
        assertFalse(SeraphaeIntegrityRules.floods(0.0F, false), "it fires once per fall");
        assertFalse(SeraphaeIntegrityRules.floods(0.5F, true));
        assertFalse(SeraphaeIntegrityRules.rearms(SeraphaeIntegrityRules.FLOOD_RESET));
        assertTrue(SeraphaeIntegrityRules.rearms(12.0F));
    }

    @Test void onlyCirculationAndSensationCastsBreakAHold() {
        assertTrue(SeraphaeIntegrityRules.breaksHold(EnumBloodTemper.CONGEATIO));
        assertTrue(SeraphaeIntegrityRules.breaksHold(EnumBloodTemper.TENEBRIS));
        assertFalse(SeraphaeIntegrityRules.breaksHold(EnumBloodTemper.MORTEM));
        assertFalse(SeraphaeIntegrityRules.breaksHold(null));
    }
}
