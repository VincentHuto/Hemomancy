package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HemorathVisualRulesTest {
    @Test void collapseKeepsBothExistingWindupDurationsAndAnticipatesBeforeReaching() {
        assertEquals(120, HemorathVisualRules.collapse(false).commitTick() + HemorathVisualRules.collapse(false).active());
        assertEquals(60, HemorathVisualRules.collapse(true).commitTick() + HemorathVisualRules.collapse(true).active());
        var brace = HemorathVisualRules.pose(1, true, false, 104);
        var raised = HemorathVisualRules.pose(1, true, false, 1);
        assertTrue(brace.bow() > .1F && brace.lift() < .01F);
        assertTrue(raised.lift() > .99F && raised.ribs() > .4F);
    }

    @Test void impactStartsAtTheEndOfTheTelegraphAndRecoversToIdle() {
        var wound = HemorathVisualRules.pose(1, true, false, 0);
        var impact = HemorathVisualRules.pose(2, false, false, 14);
        assertEquals(wound.lift(), impact.lift());
        assertEquals(wound.ribs(), impact.ribs());
        assertEquals(wound.bow(), impact.bow());
        assertEquals(HemorathVisualRules.pose(0, false, false, 0), HemorathVisualRules.pose(2, false, false, 0));
        assertEquals(HemorathVisualRules.pose(0, false, false, 0), HemorathVisualRules.pose(4, false, false, 0));
    }

    @Test void overloadOpensTheRibsBeforeTheSkeletonSlumps() {
        var pressure = HemorathVisualRules.pose(5, false, false, 20);
        var empty = HemorathVisualRules.pose(5, false, false, 0);
        assertTrue(pressure.ribs() > .95F && pressure.slump() < .01F);
        assertTrue(empty.slump() > .99F && empty.lift() < .01F);
        assertEquals(0, HemorathVisualRules.storedPressure(0));
        assertEquals(.6F, HemorathVisualRules.storedPressure(1800), .0001F);
        assertEquals(1, HemorathVisualRules.storedPressure(3000));
    }

    @Test void channelsAndBurstRetirementStayFiniteAndBounded() {
        for (int visual = 0; visual <= 5; visual++) for (int tick = -10; tick < 80; tick++) {
            var p = HemorathVisualRules.pose(visual, visual == 1, tick % 2 == 0, tick);
            for (float value : new float[]{p.lift(), p.reach(), p.bow(), p.recoil(), p.ribs(), p.slump(), p.pressure()})
                assertTrue(Float.isFinite(value) && Math.abs(value) < 1.5F);
        }
        assertEquals(1, HemorathVisualRules.fade(0, 24));
        assertEquals(0, HemorathVisualRules.fade(24, 24));
        assertTrue(HemorathVisualRules.fade(23, 24) < .01F);
    }
}
