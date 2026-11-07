package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeDeedsTest {
    private static int mask(ChapelRules.Kind... kinds) {
        int mask = SaintDeeds.FORCED;
        for (ChapelRules.Kind kind : kinds) mask |= 1 << kind.ordinal();
        return mask;
    }

    @Test void eachSkippedChapelLendsItsDeed() {
        assertTrue(SeraphaeDeeds.unwatchedHands(mask(ChapelRules.Kind.BEHOLDING)));
        assertFalse(SeraphaeDeeds.extraction(mask(ChapelRules.Kind.BEHOLDING)));
        assertTrue(SeraphaeDeeds.extraction(mask(ChapelRules.Kind.DOWNFALL)));
        assertEquals(1, SeraphaeDeeds.fragmentBonus(mask(ChapelRules.Kind.DOWNFALL)));
        assertTrue(SeraphaeDeeds.noRest(mask(ChapelRules.Kind.REPOSE)));
        assertEquals(0, SeraphaeDeeds.fragmentBonus(0));
    }

    @Test void aProperAwakeningLendsNothing() {
        int proper = SaintDeeds.of("PROPER", List.of("BEHOLDING", "DOWNFALL", "REPOSE")).mask();
        assertFalse(SeraphaeDeeds.unwatchedHands(proper));
        assertFalse(SeraphaeDeeds.extraction(proper));
        assertFalse(SeraphaeDeeds.noRest(proper));
    }

    @Test void noRestGivenShortensRecoveriesButKeepsOpeningsAndTheFloor() {
        int repose = mask(ChapelRules.Kind.REPOSE);
        assertEquals(14, SeraphaeDeeds.recovery(repose, 20, false));
        assertEquals(10, SeraphaeDeeds.recovery(repose, 12, false), "never below ten");
        assertEquals(50, SeraphaeDeeds.recovery(repose, 50, true), "an opening-bearing recovery keeps its length");
        assertEquals(20, SeraphaeDeeds.recovery(0, 20, false));
        assertEquals(SeraphaeOpenings.RECOIL_REPOSE_TICKS, SeraphaeOpenings.recoil(false, true).ticks());
        assertEquals(SeraphaeOpenings.STRAINING_RECOIL_REPOSE_TICKS, SeraphaeOpenings.recoil(true, true).ticks());
        assertEquals(40, SeraphaeOpenings.recoil(false, false).ticks());
    }

    @Test void allThreeTogether() {
        int all = mask(ChapelRules.Kind.BEHOLDING, ChapelRules.Kind.DOWNFALL, ChapelRules.Kind.REPOSE);
        assertTrue(SeraphaeDeeds.unwatchedHands(all) && SeraphaeDeeds.extraction(all) && SeraphaeDeeds.noRest(all));
        assertEquals(10, SeraphaeDeeds.decisionDelay(all, 14));
    }
}
