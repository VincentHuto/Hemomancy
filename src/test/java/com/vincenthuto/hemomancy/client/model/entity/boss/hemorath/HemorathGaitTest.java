package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathGait.*;
import static org.junit.jupiter.api.Assertions.*;

class HemorathGaitTest {
    @Test
    void aPlantedFootKeepsPaceWithTheGroundSoItNeverSkates() {
        for (float swell : new float[]{0, .5F, 1}) {
            float amplitude = amplitude(swell), cycle = cycleBlocks(swell) * BLOCK;
            for (float start : new float[]{LEFT, RIGHT}) {
                float base = Float.NaN;
                // planted from the end of its swing round to its start
                for (float into = .34F; into < .99F; into += .01F) {
                    float p = wrap(start + into);
                    float body = -(start + into) * cycle;
                    float foot = body + (THIGH + SHIN) * (float) Math.sin(legSwing(p, start, amplitude));
                    if (Float.isNaN(base)) base = foot;
                    assertEquals(base, foot, .05F, "swell " + swell + ", foot " + start + " at " + into);
                }
            }
        }
    }

    @Test
    void thePlantedStaffKeepsPaceWithTheGroundToo() {
        float cycle = cycleBlocks(.3F) * BLOCK;
        float base = staffFoot(0, .3F);
        for (float p = 0; p < .66F; p += .01F) assertEquals(base, -p * cycle + staffFoot(p, .3F), .05F);
    }

    @Test
    void theThreeBeatsComeLeftFootRightFootThenStaff() {
        assertEquals(1, lift(1 / 6F, LEFT), 1e-4F);
        assertEquals(0, lift(1 / 6F, RIGHT));
        assertEquals(0, lift(1 / 6F, STAFF));
        assertEquals(1, lift(.5F, RIGHT), 1e-4F);
        assertEquals(0, lift(.5F, LEFT));
        assertEquals(1, lift(5 / 6F, STAFF), 1e-4F);
        assertEquals(0, lift(5 / 6F, RIGHT));
    }

    @Test
    void aFullBodyTakesShorterHeavierSteps() {
        assertTrue(cycleBlocks(1) < cycleBlocks(0));
        assertTrue(amplitude(1) < amplitude(0));
        assertTrue(amplitude(0) < .6F, "a hollow stride still fits a leg");
    }

    @Test
    void theKneesFoldToCrouchHimAndKeepTheSoleUnderTheHip() {
        assertEquals(0, fold(0, 0));
        float fold = fold(1, 0);
        assertTrue(reach(-fold, 2 * fold) < reach(0, 0) - .5F, "a crouch shortens the leg");
        assertEquals(FOLD_MAX, fold(5, 2));
        float deep = fold(3, 0);
        float under = THIGH * (float) Math.sin(-deep) + SHIN * (float) Math.sin(deep);
        assertEquals(0, under, .3F);
    }

    @Test
    void theLandingJoltFollowsEachBeat() {
        assertEquals(0, landing(.3F, RIGHT));
        assertTrue(landing(RIGHT + .03F, RIGHT) > .5F);
        assertEquals(0, landing(RIGHT + .32F, RIGHT));
    }
}
