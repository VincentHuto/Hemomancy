package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LuxUmbraTouchUpTest {
    @Test void eclipseWellCarriesAVioletRim() {
        var mesh = draw(Form.WELL, new Vec3(0, 0, 0), 20);
        assertTrue(mesh.shapes.contains(LuxUmbraGeometry.SLASH), "the well's rim should be violet-cored cuts");
        assertTrue(mesh.shapes.contains(LuxUmbraGeometry.POOL));
    }

    @Test void lacerationFlashesWhereItsCutsLand() {
        assertTrue(draw(Form.UMBRA_SLASH, new Vec3(0, 0, 3), 5).count > draw(Form.UMBRA_SLASH, new Vec3(0, 0, 3), 1).count,
                "landed cuts add their flash");
    }

    @Test void luxLightRunsWithBlood() {
        int crimson = LuxUmbraGeometry.CRIMSON & 0xFFFFFF;
        for (Form form : new Form[] { Form.SUTURE, Form.LUX_MENDING, Form.BEACON, Form.VERDICT })
            assertTrue(draw(form, new Vec3(0, 6, 0), 8).colors.contains(crimson), form + " lost its hematic undertone");
    }

    private static Mesh draw(Form form, Vec3 end, float age) {
        var mesh = new Mesh();
        LuxUmbraGeometry.draw(new ManipulationVisualPacket(form, 4, Vec3.ZERO, end, 1.4f, 60, 30), new PoseStack(), mesh,
                new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 100 + age, age, 1, 1);
        return mesh;
    }

    private static final class Mesh implements VertexConsumer {
        final Set<Integer> shapes = new HashSet<>(), colors = new HashSet<>();
        int count;
        public VertexConsumer addVertex(float x, float y, float z) { count++; return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { colors.add(r << 16 | g << 8 | b); return this; }
        public VertexConsumer setUv(float u, float v) { shapes.add((int) (u / 2)); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
