package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeAnchorBindingRulesTest {
    @Test void anAnchorTakesFourATickOrTheItemsRateIfLower() {
        assertEquals(4.0D, SeraphaeAnchorBindingRules.take(100.0D));
        assertEquals(2.5D, SeraphaeAnchorBindingRules.take(2.5D));
        assertEquals(0.0D, SeraphaeAnchorBindingRules.take(-1.0D));
    }

    @Test void aRebindTakesSixtyFedTicksAndAReserveEighty() {
        float progress = 0.0F;
        int ticks = 0;
        while (!SeraphaeAnchorBindingRules.complete(progress)) {
            progress = SeraphaeAnchorBindingRules.feed(progress, SeraphaeAnchorBindingRules.RATE,
                    SeraphaeAnchorBindingRules.REBIND_BLOOD);
            ticks++;
        }
        assertEquals(60, ticks);
        progress = 0.0F;
        ticks = 0;
        while (!SeraphaeAnchorBindingRules.complete(progress)) {
            progress = SeraphaeAnchorBindingRules.feed(progress, SeraphaeAnchorBindingRules.RATE,
                    SeraphaeAnchorBindingRules.RESERVE_BLOOD);
            ticks++;
        }
        assertEquals(80, ticks);
    }

    @Test void theChargeHoldsThroughTheGraceThenBleedsAtHalfSpeed() {
        float progress = 0.5F;
        assertEquals(progress, SeraphaeAnchorBindingRules.bleed(progress, SeraphaeAnchorBindingRules.GRACE, 240.0D));
        float bled = SeraphaeAnchorBindingRules.bleed(progress, SeraphaeAnchorBindingRules.GRACE + 1, 240.0D);
        assertEquals(progress - 2.0F / 240.0F, bled, 1.0E-6F);
        assertEquals(0.0F, SeraphaeAnchorBindingRules.bleed(0.001F, 10, 240.0D));
    }

    @Test void anAnchorFedAtTheWrenchsCommitIsHeld() {
        assertTrue(SeraphaeAnchorBindingRules.heldAtCommit(0));
        assertTrue(SeraphaeAnchorBindingRules.heldAtCommit(SeraphaeAnchorBindingRules.HELD_AT_COMMIT));
        assertFalse(SeraphaeAnchorBindingRules.heldAtCommit(SeraphaeAnchorBindingRules.HELD_AT_COMMIT + 1));
    }

    @Test void everyDrainHasItsOwnNamedSource() {
        assertEquals("seraphae_anchor_binding", SeraphaeAnchorBindingRules.SOURCE.id());
        assertEquals("seraphae_reserve_wake", SeraphaeAnchorBindingRules.RESERVE_SOURCE.id());
        assertEquals("seraphae_condensing_feed", SeraphaeAnchorBindingRules.CONDENSING_SOURCE.id());
        assertEquals("blood_source.hemomancy.seraphae_anchor_binding", SeraphaeAnchorBindingRules.SOURCE.label());
        assertFalse(SeraphaeAnchorBindingRules.SOURCE.reserveProtected());
        assertTrue(SeraphaeAnchorBindingRules.SOURCE.countsAsSpend());
    }
}
