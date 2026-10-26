package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HemorathReliquaryGeometryTest {
    private static final Vec3 CAMERA = new Vec3(4, 4, -8), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);

    @Test void everyAttackAndOverloadProducesFiniteBoundedShaderSurfaces() {
        for (int visual = 0; visual <= 5; visual++) for (float remaining : new float[]{0, 1, 8, 20, 30, 60}) {
            Capture mesh = new Capture();
            var f = new HemorathReliquaryGeometry.Frame(visual, remaining, 100 - remaining, 19, true, visual == 1,
                    .6F, new Vec3(0, 2.6, 0), new Vec3(1, 2, 0), new Vec3(-1.5, 4, 0));
            HemorathReliquaryGeometry.body(new PoseStack(), mesh, mesh, CAMERA, RIGHT, UP, f);
            mesh.check(1800);
            assertTrue(mesh.vertices > 0);
        }
    }

    @Test void burstsIntakeAndFinaleExpireWithoutGenericMoteResidue() {
        for (int kind = 1; kind <= 6; kind++) for (float age : new float[]{0, 1, 6, 12, 23, 32})
            for (boolean solids : new boolean[]{true, false}) {
                Capture mesh = new Capture();
                var packet = new ManipulationVisualPacket(Form.HEMORATH_RELIQUARY, -1, Vec3.ZERO, new Vec3(4, 2, 0), 3, 32, kind);
                HemorathReliquaryGeometry.draw(packet, new PoseStack(), mesh, mesh, mesh, solids, CAMERA, RIGHT, UP, 100 + age, age, 1);
                mesh.check(800);
                if (age == 32) assertEquals(0, mesh.vertices);
                else if (!solids) assertTrue(mesh.vertices > 0);
            }
        assertTrue(HemorathReliquaryGeometry.handles(Form.HEMORATH_RELIQUARY));
        assertFalse(AnimusMortemGeometry.handles(Form.HEMORATH_RELIQUARY));
    }

    @Test void longDebtTethersAndHollowIronLinksStayWithinBudget() {
        for (Vec3 target : new Vec3[]{new Vec3(20, 1, 0), new Vec3(0, 20, 0), Vec3.ZERO}) {
            Capture mesh = new Capture();
            HemorathReliquaryGeometry.debt(new PoseStack(), mesh, mesh, Vec3.ZERO, target, CAMERA, RIGHT, UP, 100, 5, 1);
            HemorathReliquaryGeometry.debtLinks(new PoseStack(), mesh, Vec3.ZERO, target, 5, 1);
            mesh.check(400);
        }
    }

    private static final class Capture implements VertexConsumer {
        int vertices;
        public VertexConsumer addVertex(float x, float y, float z) {
            assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z)); vertices++; return this;
        }
        public VertexConsumer setColor(int r, int g, int b, int a) {
            for (int color : new int[]{r,g,b,a}) assertTrue(color >= 0 && color <= 255);
            return this;
        }
        public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
        void check(int max) { assertEquals(0, vertices % 4); assertTrue(vertices <= max, "Vertices: " + vertices); }
    }
}
