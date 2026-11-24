package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class NaeglerophaeonMembraneTest {
    private static NaeglerophaeonModel.Input pose(int phase, double yaw, double ticks, Vec3[] release, double blend) {
        Vec3 victim = phase == CAPTURE || phase == GRAB ? new Vec3(4, -2, 5) : null;
        return new NaeglerophaeonModel.Input(123, 71, yaw, .4, phase, ticks, victim, 1.2,
                1.8, release, blend, 0, NaeglerophaeonPose.TurnLag.ZERO, false);
    }

    @Test void renderedIdleTailUsesTheAuthoredHookAndRoot() {
        var input = new NaeglerophaeonModel.Input(0, 71, 0, 0, IDLE, 0, null, 0,
                1.8, null, 0, 0, NaeglerophaeonPose.TurnLag.ZERO, false);
        var frame = NaeglerophaeonBlockModel.pose(input, null, 0);
        var tail = frame.tail();
        assertEquals(.4375, tail[0].horizontalDistance(), 1e-6);
        assertTrue(tail[tail.length - 1].distanceTo(tail[0]) < 3, "idle axon ends on its returning hook");
        assertTrue(tail[tail.length - 1].distanceTo(tail[tail.length / 2]) > 1, "hook curls back toward the arms");
        assertEquals(12, frame.tips().length);
        assertTrue(Arrays.stream(frame.tips()).allMatch(p -> p != null && Double.isFinite(p.lengthSqr())));
    }

    @Test void renderedPhasesKeepFiniteGeometryAndVisibleWebs() {
        int[] phases = {IDLE, CAPTURE, GRAB, DISCHARGE, RECOVERY, LUNGE_WINDUP, LUNGE, LASH,
                VOLLEY, CONDUCT, NOVA, NERVE_DIVE, NERVE_EMERGE, TRANSITION, OVERLOAD, DRAINED};
        for (int phase : phases) {
            var input = pose(phase, .8, 22, null, 0);
            var frame = NaeglerophaeonBlockModel.pose(input, new Vec3(.2, -.5, .4), .6F);
            int body = renderVertices(frame, 0, input.drained(), false);
            int lights = renderVertices(frame, 1, input.drained(), false);
            int web = renderVertices(frame, 2, input.drained(), false);
            assertEquals(36 * 2 * 4, web);
            assertTrue(body > 0 && (body + lights + web) / 4 <= 4000);
            if (input.drained()) assertEquals(0, lights);
            else assertTrue(lights > 0);
        }
        var captured = NaeglerophaeonBlockModel.pose(pose(GRAB, 1, 35, null, 0), null, 0);
        assertTrue(renderVertices(captured, 3, false, true) > 0);
        assertEquals(0, renderVertices(captured, 3, false, false));
    }

    @Test void lungeAndDiveMoveTheAuthoredTendrilTips() {
        var flare = NaeglerophaeonBlockModel.pose(pose(LUNGE_WINDUP, 0, 14, null, 0), null, 0);
        var tuck = NaeglerophaeonBlockModel.pose(pose(LUNGE, 0, 2, null, 0), null, 0);
        assertTrue(flare.tips()[0].distanceTo(tuck.tips()[0]) > .5);
        var dive = NaeglerophaeonBlockModel.pose(pose(NERVE_DIVE, 0, 12, null, 0), null, 0);
        var emerge = NaeglerophaeonBlockModel.pose(pose(NERVE_EMERGE, 0, 6, null, 0), null, 0);
        var core = new Vec3(0, .8, 0);
        assertTrue(dive.tips()[0].distanceTo(core) < .25 * emerge.tips()[0].distanceTo(core));
    }

    @Test void pulsesHaveThreeSlotsAndOrderedTravel() {
        Map<Long, Double> progress = new HashMap<>();
        for (int tick = 0; tick < 5000; tick++) {
            var pulses = NaeglerophaeonModel.pulses(tick, 71);
            assertTrue(pulses.size() <= 3);
            Set<Integer> limbs = new HashSet<>();
            for (var p : pulses) {
                assertTrue(p.limb() < 12);
                assertTrue(limbs.add(p.limb()));
                Double old = progress.put(p.event(), p.progress());
                if (old != null) assertTrue(p.progress() > old);
                assertTrue(p.progress() >= 0 && p.progress() < 1);
            }
        }
    }

    @Test void everyPulseHasOneTerminalSparkTick() {
        Map<Long, Integer> sparks = new HashMap<>();
        for (int tick = 0; tick < 5000; tick++) {
            for (var pulse : NaeglerophaeonModel.pulses(tick, 71)) {
                if (pulse.spark()) sparks.merge(pulse.event(), 1, Integer::sum);
            }
        }
        assertTrue(sparks.size() > 100);
        sparks.values().forEach(n -> assertEquals(1, n));
    }

    @Test void renderedGripMatchesSharedWeakPointWhileBossTurns() {
        for (double yaw : new double[]{0, Math.PI, 50}) {
            var input = pose(GRAB, yaw, 50, null, 0);
            var tail = NaeglerophaeonBlockModel.pose(input, null, 0).tail();
            var expected = NaeglerophaeonCombatRules.gripPosition(input.victim(), input.victimHeight(),
                    (float) Math.toDegrees(input.victimYaw()));
            assertEquals(0, tail[tail.length - 1].distanceTo(expected), 1e-6);
            assertTrue(tail[tail.length - 1].y < input.victim().y + 1.2);
        }
    }

    @Test void historyResetsAfterTeleportOrTrackingGap() {
        var pose = new NaeglerophaeonPose();
        pose.update(0, Vec3.ZERO);
        pose.update(1, new Vec3(.2, 0, 0), 1, .4);
        assertTrue(pose.offsets(new Vec3(.2, 0, 0))[1].x < 0);
        pose.update(2, new Vec3(100, 0, 0), 2, .7);
        for (var offset : pose.offsets(new Vec3(100, 0, 0))) assertEquals(0, offset.lengthSqr());
        for (float yaw : pose.turnLag(1).tailYaw()) assertEquals(0, yaw);
    }

    @Test void releaseStartsAtTheCapturedCurveAndReturnsToTheAuthoredHook() {
        var held = NaeglerophaeonBlockModel.pose(pose(GRAB, 0, 35, null, 0), null, 0).tail();
        var release = NaeglerophaeonBlockModel.pose(pose(RECOVERY, 0, 0, held, 1), null, 0).tail();
        assertArrayEquals(held, release);
        var settled = NaeglerophaeonBlockModel.pose(pose(RECOVERY, 0, 40, held, 0), null, 0).tail();
        assertTrue(settled[settled.length - 1].distanceTo(settled[0]) < 3);
    }

    @Test void captureCurveRetainsItsLengthWhenReachingForward() {
        var input = pose(GRAB, 0, 35, null, 0);
        var curve = NaeglerophaeonModel.captureCurve(input, new Vec3(0, .8, -.4375), 96);
        double length = 0;
        for (int j = 1; j < curve.length; j++) length += curve[j].distanceTo(curve[j - 1]);
        assertEquals(30, length, .8);
    }

    private static int renderVertices(NaeglerophaeonBlockModel.Frame frame, int pass, boolean drained, boolean mark) {
        var consumer = new GeometryProbe();
        NaeglerophaeonBlockModel.render(frame, new PoseStack(), consumer, pass, 0, 0, drained, mark);
        assertEquals(0, consumer.vertices % 4);
        return consumer.vertices;
    }

    private static final class GeometryProbe implements VertexConsumer {
        private int vertices;

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
            vertices++;
            return this;
        }
        @Override public VertexConsumer setUv(float u, float v) {
            assertTrue(u >= 0 && u <= 1 && v >= 0 && v <= 1);
            return this;
        }
        @Override public VertexConsumer setNormal(float x, float y, float z) {
            assertEquals(1, x * x + y * y + z * z, 1e-4);
            return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
    }
}
