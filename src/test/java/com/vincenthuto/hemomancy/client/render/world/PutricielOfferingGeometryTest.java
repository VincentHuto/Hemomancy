package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Putriciel's split world geometry: the rot never burns, every piece stays bounded, every burst expires. */
class PutricielOfferingGeometryTest {
    private static final Vec3 CAMERA = new Vec3(4, 4, -8), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);

    private static PutricielOfferingGeometry.Body body(PutricielMove move, float t, PutricielStage stage, float ending,
                                                       int opening, float sinceAccepted) {
        var state = PutricielAnimState.detached();
        state.set(move == null ? 0 : move.id(), move == null ? -1 : t, stage, 40, ending, opening, opening == 0 ? -1 : 8,
                .5F, 3, sinceAccepted);
        return new PutricielOfferingGeometry.Body(state, 11, 100 + t, CAMERA, RIGHT, UP, Vec3.ZERO);
    }

    @Test void theRotNeverDrawsFire() {
        for (PutricielMove move : PutricielMove.values()) {
            if (move.fire()) continue;
            var b = body(move, 30, PutricielStage.ROT, -1, 0, 1000);
            assertFalse(b.fire());
            var fire = new Capture();
            PutricielFireGeometry.body(new PoseStack(), fire, new Capture(), b);
            assertEquals(0, fire.vertices, move + " drew fire in the rot");
        }
    }

    @Test void theFireAndTheOfferingDrawAndStayBounded() {
        for (PutricielMove move : PutricielMove.values()) {
            for (float t : new float[]{0, 6, 20, 40, 80}) {
                PutricielStage stage = move.fire() ? PutricielStage.BURNING : PutricielStage.ROT;
                int opening = move == PutricielMove.OFFERING ? PutricielOpenings.OFFERING_ID : 0;
                var b = body(move, t, stage, -1, opening, t < 10 ? t : 1000);
                var rot = new Capture();
                var fire = new Capture();
                PutricielRotGeometry.body(new PoseStack(), rot, b);
                if (b.fire()) PutricielFireGeometry.body(new PoseStack(), fire, rot, b);
                rot.check(2400);
                fire.check(2400);
                if (move == PutricielMove.OFFERING) assertTrue(fire.vertices > 0, "the Offering's focal geometry");
            }
        }
    }

    @Test void theEndingBurnsThenGoesToSoot() {
        for (float t : new float[]{0, 20, 35, 60, 90}) {
            var b = body(null, 0, PutricielStage.BURNING, t, 0, 1000);
            var fire = new Capture();
            PutricielFireGeometry.body(new PoseStack(), fire, new Capture(), b);
            fire.check(2400);
            if (t == 35) assertTrue(fire.vertices > 0);
        }
    }

    @Test void everyBurstExpires() {
        for (int kind = 1; kind <= 9; kind++) for (float age : new float[]{0, 1, 8, 20, 31, 32}) {
            var mesh = new Capture();
            var packet = new ManipulationVisualPacket(Form.PUTRICIEL_OFFERING, -1, Vec3.ZERO, Vec3.ZERO, 6,
                    PutricielVisualRules.BURST_TICKS, kind);
            PutricielOfferingGeometry.draw(packet, new PoseStack(), mesh, mesh, CAMERA, RIGHT, UP, 100 + age, age, 1);
            mesh.check(1800);
            if (age >= PutricielVisualRules.BURST_TICKS) assertEquals(0, mesh.vertices);
            else if (age < 20) assertTrue(mesh.vertices > 0, "kind " + kind + " at " + age);
        }
        assertFalse(AnimusMortemGeometry.handles(Form.PUTRICIEL_OFFERING));
        assertTrue(PutricielOfferingGeometry.handles(Form.PUTRICIEL_OFFERING));
    }

    @Test void theNewNoduleSwellsWithAnOvershoot() {
        assertEquals(1.0F, PutricielFireGeometry.overshoot(-1));
        assertTrue(PutricielFireGeometry.overshoot(4) > 1.5F);
        assertEquals(1.0F, PutricielFireGeometry.overshoot(9));
    }

    private static final class Capture implements VertexConsumer {
        int vertices;
        public VertexConsumer addVertex(float x, float y, float z) {
            assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z)); vertices++; return this;
        }
        public VertexConsumer setColor(int r, int g, int b, int a) {
            for (int color : new int[]{r, g, b, a}) assertTrue(color >= 0 && color <= 255);
            return this;
        }
        public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
        void check(int max) { assertEquals(0, vertices % 4); assertTrue(vertices <= max, "Vertices: " + vertices); }
    }
}
