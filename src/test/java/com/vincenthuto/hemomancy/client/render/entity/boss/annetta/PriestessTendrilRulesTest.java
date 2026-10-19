package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Pose;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Wave;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriestessTendrilRulesTest {
    private static final Wave STILL = new Wave(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

    /** Finger tips of the left wing with the waves stilled, in pixels of body space. */
    private static float[][] fingerTips(Pose pose) {
        float[] weights = PriestessTendrilRules.weightsFor(pose);
        float[] arm = PriestessTendrilRules.integrate(PriestessTendrilRules.WING_ROOT,
                PriestessTendrilRules.armHeading(weights, 0.0F, 0.0F), PriestessTendrilRules.ARM_LENGTH,
                PriestessTendrilRules.SEGMENTS, STILL, 0.0F);
        float[][] tips = new float[PriestessTendrilRules.FINGERS][];
        for (int finger = 0; finger < PriestessTendrilRules.FINGERS; finger++) {
            float[] root = PriestessTendrilRules.pointAt(arm, PriestessTendrilRules.fingerRoot(finger));
            float[] line = PriestessTendrilRules.integrate(root,
                    PriestessTendrilRules.fingerHeading(finger, weights, 0.0F, 0.0F),
                    PriestessTendrilRules.fingerLength(finger, weights), PriestessTendrilRules.SEGMENTS, STILL, 0.0F);
            int last = line.length - 3;
            tips[finger] = new float[]{line[last], line[last + 1], line[last + 2]};
        }
        return tips;
    }

    @Test
    void spreadSpanAboutFiveBlocks() {
        float reach = 0.0F;
        for (float[] tip : fingerTips(Pose.SPREAD)) reach = Math.max(reach, Math.abs(tip[0]));
        float blocks = reach * PriestessTendrilRules.RENDER_SCALE / 16.0F;
        assertTrue(blocks >= 4.5F && blocks <= 5.5F, "spread reach " + blocks + " blocks");
    }

    @Test
    void shieldTipsInFront() {
        for (float[] tip : fingerTips(Pose.SHIELD)) assertTrue(tip[2] < -4.0F, "shield tip z " + tip[2]);
    }

    @Test
    void foldedTipsBehind() {
        for (float[] tip : fingerTips(Pose.FOLDED)) assertTrue(tip[2] > 2.0F, "folded tip z " + tip[2]);
    }

    @Test
    void rootsPinned() {
        float[] root = {3.0F, -2.0F, 1.5F};
        Wave wild = new Wave(2.4F, 0.2F, 1.1F, 0.4F, 0.5F);
        for (float time = 0.0F; time < 200.0F; time += 7.3F) {
            float[] line = PriestessTendrilRules.integrate(root, new PriestessTendrilRules.Heading(30.0F, 10.0F),
                    40.0F, 18, wild, time);
            assertEquals(root[0], line[0], 1.0E-6F);
            assertEquals(root[1], line[1], 1.0E-6F);
            assertEquals(root[2], line[2], 1.0E-6F);
            assertEquals(19 * 3, line.length);
        }
    }

    @Test
    void blendingIsRateLimited() {
        float[] weights = PriestessTendrilRules.weightsFor(Pose.FOLDED);
        for (int frame = 0; frame < 40; frame++) {
            float[] before = weights.clone();
            Pose target = frame % 2 == 0 ? Pose.SPREAD : Pose.SHIELD;
            weights = PriestessTendrilRules.stepWeights(weights, target, 0.5F);
            float sum = 0.0F;
            for (int i = 0; i < 3; i++) {
                assertTrue(Math.abs(weights[i] - before[i]) <= 0.5F / 6.0F + 1.0E-5F, "jump at frame " + frame);
                sum += weights[i];
            }
            assertEquals(1.0F, sum, 1.0E-4F);
        }
        float[] settled = PriestessTendrilRules.weightsFor(Pose.FOLDED);
        for (int tick = 0; tick < 7; tick++) settled = PriestessTendrilRules.stepWeights(settled, Pose.SPREAD, 1.0F);
        assertEquals(1.0F, settled[Pose.SPREAD.ordinal()], 1.0E-4F);
    }

    @Test
    void heavyDroopHangsInsteadOfHooking() {
        Wave dead = new Wave(0.0F, 0.0F, 0.0F, 0.0F, 6.0F);
        float[] line = PriestessTendrilRules.integrate(new float[3], new PriestessTendrilRules.Heading(0.0F, 0.0F),
                30.0F, 18, dead, 0.0F);
        for (int i = 3; i < line.length; i += 3) {
            assertTrue(line[i] >= line[i - 3] - 1.0E-4F, "a drooping tendril never curls back on itself");
            assertTrue(line[i + 1] >= line[i - 2] - 1.0E-4F, "it only ever falls");
        }
    }

    @Test
    void integrateNeverProducesNaN() {
        Wave wild = new Wave(3.0F, 0.3F, 0.0F, 0.0F, 4.0F);
        float[] line = PriestessTendrilRules.integrate(new float[3], new PriestessTendrilRules.Heading(0.0F, 90.0F),
                0.0F, 12, wild, 55.0F);
        for (float value : line) assertFalse(Float.isNaN(value) || Float.isInfinite(value));
    }
}
