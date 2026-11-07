package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeRadianceGeometryTest {
    private static final Vec3 CAMERA = new Vec3(4, 3, -6), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);

    @Test void slackChainsDrawOneRibbonAndTautChainsShowTheBraid() {
        Capture slack = new Capture();
        SeraphaeRadianceGeometry.chain(new PoseStack(), slack, new Vec3(-8, 1.5, -6), new Vec3(0, 2, 0), RIGHT, UP, 100, 3,
                0.2F, 1.0F);
        Capture taut = new Capture();
        SeraphaeRadianceGeometry.chain(new PoseStack(), taut, new Vec3(-8, 1.5, -6), new Vec3(0, 2, 0), RIGHT, UP, 100, 3,
                1.0F, 1.0F);
        slack.check(200);
        taut.check(1200);
        assertTrue(slack.vertices > 0 && taut.vertices > slack.vertices * 3, "the braid shows only near taut");
    }

    @Test void fourTautChainsAndTheHoldStayWithinOneBossSurfaceBudget() {
        Capture mesh = new Capture();
        for (int i = 0; i < 4; i++)
            SeraphaeRadianceGeometry.chain(new PoseStack(), mesh, new Vec3(i % 2 == 0 ? -10 : 10, 1.5, i < 2 ? -10 : 10),
                    new Vec3(0.5, 2, 0), RIGHT, UP, 100, i, 1.0F, 1.0F);
        SeraphaeRadianceGeometry.hold(new PoseStack(), mesh, new Vec3(0.6, 1.6, -0.6), new Vec3(0, 1.1, -1.4), 100, 9);
        mesh.check(5000);
    }

    @Test void theSubtletyBudgetHolds() {
        assertTrue(SeraphaeRadianceGeometry.STRAND <= 0.019D, "no strand wider than the tether's 0.019");
        assertTrue(SeraphaeRadianceGeometry.SHELL <= 0.28F, "shells at most 0.28 alpha");
        assertTrue(SeraphaeRadianceGeometry.PRESENCE_ALPHA <= 0.16F, "the presence ring stays faint");
    }

    @Test void growthFragmentsAndAnchorsHaveVisibleBoundedGeometry() {
        for (float age : new float[]{0, 5, 20, 100}) {
            Capture shard = new Capture();
            SeraphaeRadianceGeometry.fragment(new PoseStack(), shard, shard, new Vec3(0, .7, 0), RIGHT, UP, age, 7, age / 100F);
            shard.check(300);
            assertTrue(shard.vertices > 20);
            for (boolean held : new boolean[]{false, true}) {
                Capture seal = new Capture();
                SeraphaeRadianceGeometry.anchor(new PoseStack(), seal, new Vec3(0, 0, 3), RIGHT, UP, age, 7, held, held ? 1 : 0);
                seal.check(60);
                assertTrue(seal.vertices > 4);
            }
            for (boolean knotting : new boolean[]{false, true}) {
                Capture growth = new Capture();
                SeraphaeRadianceGeometry.growth(new PoseStack(), growth, new Vec3(2, 0, 1), 30, RIGHT, UP, age, 4, knotting, false);
                growth.check(200);
                assertTrue(growth.vertices > 0);
            }
        }
    }

    @Test void everyCueFraysInsteadOfLeavingResidue() {
        for (int kind = 1; kind <= 13; kind++) for (float age : new float[]{0, 1, 8, 16, 23, 44}) {
            int life = SeraphaeVisualRules.life(kind);
            Capture mesh = new Capture();
            var packet = new ManipulationVisualPacket(Form.SERAPHAE_RELIQUARY, -1, Vec3.ZERO, new Vec3(5, 1, 0), 2, life, kind);
            SeraphaeRadianceGeometry.draw(packet, new PoseStack(), mesh, mesh, CAMERA, RIGHT, UP, 100 + age, age, 1);
            mesh.check(3000);
            if (age >= life) assertEquals(0, mesh.vertices, kind + " at " + age);
        }
        assertTrue(SeraphaeRadianceGeometry.handles(Form.SERAPHAE_RELIQUARY));
        assertEquals(ManipulationMaterials.LUX, ManipulationMaterials.forForm(Form.SERAPHAE_RELIQUARY));
    }

    @Test void theRecoilSpikeOvershootsThenSettles() {
        assertEquals(0, SeraphaeRadianceGeometry.recoilSpike(0), 1.0E-4F);
        assertTrue(SeraphaeRadianceGeometry.recoilSpike(3) > 0.5F);
        assertEquals(0, SeraphaeRadianceGeometry.recoilSpike(7), 1.0E-4F);
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
