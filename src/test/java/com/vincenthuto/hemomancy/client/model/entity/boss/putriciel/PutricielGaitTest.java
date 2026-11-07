package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PutricielGaitTest {
    /** Model units the body travels per unit of phase in a cycle of {@code blocks}. */
    private static float travel(float blocks) {
        return blocks * PutricielGait.BLOCK;
    }

    @Test
    void thePlantedShambleFeetSweepBackAtTheBodysSpeed() {
        float rate = travel(PutricielGait.SHAMBLE_BLOCKS);
        float dp = 0.001F;
        for (float p = PutricielGait.LEAD_END + 0.01F; p < 0.99F; p += 0.01F) {
            float moved = PutricielGait.shambleLead(p + dp) - PutricielGait.shambleLead(p);
            assertEquals(rate * dp, moved, 1e-3F, "the planted lead foot keeps pace with the ground at " + p);
            assertEquals(0.0F, PutricielGait.shambleLift(p, true), 1e-6F);
        }
        for (float p = 0.01F; p < PutricielGait.DRAG_START - 0.01F; p += 0.01F) {
            float moved = PutricielGait.shambleTrail(p + dp) - PutricielGait.shambleTrail(p);
            assertEquals(rate * dp, moved, 1e-3F, "the planted trailing foot keeps pace with the ground at " + p);
            assertEquals(0.0F, PutricielGait.shambleLift(p, false), 1e-6F);
        }
    }

    @Test
    void eachLungeLeavesTheFeetTogetherAndTheLeadFootAhead() {
        assertEquals(0.0F, PutricielGait.shambleLead(0.0F), 1e-4F);
        assertEquals(0.0F, PutricielGait.shambleTrail(0.0F), 1e-4F);
        assertEquals(0.0F, PutricielGait.shambleLead(0.9999F), 0.01F);
        assertEquals(0.0F, PutricielGait.shambleTrail(0.9999F), 0.01F);
        assertTrue(PutricielGait.shambleLead(PutricielGait.LEAD_END) < -3.0F, "the lead foot lurches well ahead");
        assertTrue(PutricielGait.shambleTrail(PutricielGait.DRAG_START) > 3.0F, "the trailing foot is left well behind");
        // each foot covers one lunge of ground per cycle
        float lead = PutricielGait.shambleLead(PutricielGait.LEAD_END) - PutricielGait.shambleLead(0.0F);
        assertEquals(-travel(PutricielGait.SHAMBLE_BLOCKS), lead - PutricielGait.LEAD_END * travel(PutricielGait.SHAMBLE_BLOCKS),
                1e-3F);
    }

    @Test
    void theLeadFootLiftsHighAndTheTrailingFootOnlyScrapes() {
        float lead = 0, drag = 0;
        for (float p = 0; p < 1; p += 0.01F) {
            lead = Math.max(lead, PutricielGait.shambleLift(p, true));
            drag = Math.max(drag, PutricielGait.shambleLift(p, false));
        }
        assertEquals(1.0F, lead, 0.01F);
        assertTrue(drag > 0.0F && drag <= PutricielGait.DRAG_LIFT + 1e-4F);
        assertTrue(PutricielGait.lurch(PutricielGait.LEAD_END + 0.08F) > 0.9F, "the weight is thrown over the lead foot");
        assertEquals(0.0F, PutricielGait.lurch(0.05F), 1e-4F);
    }

    @Test
    void theStrideNeverSkatesAndAlternatesTheFeet() {
        float rate = travel(PutricielGait.STRIDE_BLOCKS);
        float dp = 0.001F;
        for (float p = 0.51F; p < 0.99F; p += 0.01F) {
            assertEquals(rate * dp, PutricielGait.strideFoot(p + dp, PutricielGait.LEFT) - PutricielGait.strideFoot(p, PutricielGait.LEFT),
                    1e-3F, "the planted left foot at " + p);
            assertEquals(0.0F, PutricielGait.strideLift(p, PutricielGait.LEFT), 1e-6F);
            assertTrue(PutricielGait.strideLift(p, PutricielGait.RIGHT) >= 0.0F);
        }
        assertTrue(PutricielGait.strideLift(0.25F, PutricielGait.LEFT) > 0.9F);
        assertTrue(PutricielGait.strideLift(0.75F, PutricielGait.RIGHT) > 0.9F);
    }

    @Test
    void theThighReachesTheFootAndThePhaseFollowsDistance() {
        float along = 2.5F;
        float thigh = PutricielGait.thigh(along);
        assertEquals(along, (PutricielGait.THIGH + PutricielGait.SHIN) * (float) Math.sin(thigh), 1e-4F);
        assertTrue(thigh > 0.0F, "a foot behind is a positive thigh angle");
        // four walk units a block: one lunge is one cycle
        assertEquals(0.5F, PutricielGait.phase(4.0F * PutricielGait.SHAMBLE_BLOCKS * 0.5F, PutricielGait.SHAMBLE_BLOCKS), 1e-4F);
        // the phase is a circle: three whole lunges land on its start, from either side of the wrap
        float whole = PutricielGait.phase(4.0F * PutricielGait.SHAMBLE_BLOCKS * 3.0F, PutricielGait.SHAMBLE_BLOCKS);
        assertEquals(0.0F, Math.min(whole, 1.0F - whole), 1e-4F);
    }

    @Test
    void aKneelingLegTouchesWithItsKneeAndAStandingOneWithItsSole() {
        float rad = (float) Math.PI / 180.0F;
        float standing = PutricielGait.contact(-6 * rad, 10 * rad);
        assertEquals(PutricielGait.reach(-6 * rad, 10 * rad), standing, 1e-6F, "standing, the sole is lowest");
        float kneeling = PutricielGait.contact(5 * rad, 95 * rad);
        assertEquals(PutricielGait.THIGH * (float) Math.cos(5 * rad) + PutricielGait.KNEE_PAD, kneeling, 1e-6F,
                "kneeling, the kneecap is lowest");
        float planted = PutricielGait.contact(-72 * rad, 72 * rad);
        assertEquals(kneeling, planted, 0.05F, "the one-knee kneel's front sole and back knee meet the same floor");
        assertEquals(0.0F, PutricielGait.fold(0.0F), 1e-6F);
        assertEquals(1.3F, PutricielGait.fold(10.0F), 1e-6F);
    }
}
