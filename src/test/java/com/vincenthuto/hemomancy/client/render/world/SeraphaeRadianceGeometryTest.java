package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.*;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeraphaeRadianceGeometryTest {
    private static final Vec3 CAMERA = new Vec3(4, 3, -6), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);

    @Test void persistentStatesAndDispersedWispsRemainVisibleWithoutParticleProviders() {
        Vec3[] feathers = {new Vec3(-1, 2, 0), new Vec3(-1.5, 2, 0), new Vec3(-2, 2, 0),
                new Vec3(1, 2, 0), new Vec3(1.5, 2, 0), new Vec3(2, 2, 0)};
        for (var state : SeraphaeState.values()) for (float remaining : new float[]{0, 1, 20, 60, 100, 200}) {
            Capture mesh = new Capture();
            SeraphaeRadianceGeometry.body(new PoseStack(), mesh, CAMERA, RIGHT, UP,
                    new SeraphaeRadianceGeometry.Frame(state, remaining, 100 - remaining, 17, feathers));
            mesh.check(2400);
            assertTrue(mesh.vertices > 0, state.name());
        }
    }

    @Test void impactCaptureAndVictoryCuesFrayInsteadOfLeavingResidue() {
        for (int kind = 1; kind <= 6; kind++) for (float age : new float[]{0, 1, 8, 16, 23, 44}) {
            int life = kind == SeraphaeVisualRules.SEALED ? 44 : 24;
            Capture mesh = new Capture();
            var packet = new ManipulationVisualPacket(Form.SERAPHAE_RELIQUARY, -1, Vec3.ZERO, new Vec3(5, 1, 0), 2, life, kind);
            SeraphaeRadianceGeometry.draw(packet, new PoseStack(), mesh, CAMERA, RIGHT, UP, 100 + age, age, 1);
            mesh.check(2400);
            if (age < life) assertTrue(mesh.vertices > 0, kind + " at " + age);
            else assertEquals(0, mesh.vertices);
        }
        assertTrue(SeraphaeRadianceGeometry.handles(Form.SERAPHAE_RELIQUARY));
        assertFalse(AnimusMortemGeometry.handles(Form.SERAPHAE_RELIQUARY));
        assertEquals(ManipulationMaterials.LUX, ManipulationMaterials.forForm(Form.SERAPHAE_RELIQUARY));
    }

    @Test void fourLongContainmentTethersStayWithinOneBossSurfaceBudget() {
        Capture mesh = new Capture();
        for (int i = 0; i < 4; i++)
            SeraphaeRadianceGeometry.tether(new PoseStack(), mesh, new Vec3(i % 2 == 0 ? -10 : 10, .8, i < 2 ? -10 : 10),
                    new Vec3(.5, 1.5, 0), CAMERA, RIGHT, UP, 100, i, 1);
        mesh.check(1800);
        assertTrue(mesh.vertices > 1000);
    }

    @Test void fragmentShardsAndAnchorSealsHaveVisibleBoundedGeometry() {
        for (float age : new float[]{0, 5, 20, 100}) {
            Capture shard = new Capture();
            SeraphaeRadianceGeometry.fragment(new PoseStack(), shard, new Vec3(0, .7, 0), RIGHT, UP, age, 7);
            shard.check(300);
            assertTrue(shard.vertices > 70);
            for (boolean active : new boolean[]{false, true}) {
                Capture seal = new Capture();
                SeraphaeRadianceGeometry.anchor(new PoseStack(), seal, new Vec3(0, 0, 3), RIGHT, UP, age, 7, active);
                seal.check(40);
                assertTrue(seal.vertices > 10);
            }
        }
    }

    private static final class Capture implements VertexConsumer {
        int vertices;
        public VertexConsumer addVertex(float x, float y, float z) {
            assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
            vertices++;
            return this;
        }
        public VertexConsumer setColor(int r, int g, int b, int a) {
            assertTrue(r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255 && a >= 0 && a <= 255);
            return this;
        }
        public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
        void check(int budget) { assertEquals(0, vertices % 4); assertTrue(vertices <= budget, "Vertices: " + vertices); }
    }
}
