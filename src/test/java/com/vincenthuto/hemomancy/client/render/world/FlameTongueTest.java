package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlameTongueTest {
    @Test void tonguesRiseLeanAndSway() {
        Vec3 lean = new Vec3(.3, 0, 0);
        List<Vec3> a = ThermalGeometry.tongue(Vec3.ZERO, lean, 1.2, 10, 4), b = ThermalGeometry.tongue(Vec3.ZERO, lean, 1.2, 30, 4);
        assertEquals(Vec3.ZERO, a.getFirst(), "the root stays planted");
        assertEquals(1.2, a.getLast().y, 1e-9);
        assertTrue(a.getLast().x > .2, "the tip leans away from the source");
        assertNotEquals(a.getLast().z, b.getLast().z, "the upper tongue should sway over time");
    }

    @Test void oneFlameRunsRootToTipAlongTheBentSpine() {
        var uvs = new Uvs();
        ThermalGeometry.flameRibbon(new PoseStack(), uvs, ThermalGeometry.tongue(Vec3.ZERO, new Vec3(.3, 0, 0), 1, 0, 2),
                new Vec3(0, 1, -4), .2, 2, 1);
        assertEquals(24, uvs.v.size(), "six segments, one quad each");
        double phase = Math.floorMod(2, 127) * 2;
        assertEquals(phase, uvs.v.stream().mapToDouble(Float::doubleValue).min().orElseThrow(), 1e-6, "root at the flame's base");
        assertEquals(phase + 1, uvs.v.stream().mapToDouble(Float::doubleValue).max().orElseThrow(), 1e-6, "tip at the flame's top");
        assertTrue(uvs.u.stream().allMatch(u -> u == 0 || u == 1), "the flame spans the ribbon's width");
    }

    private static final class Uvs implements VertexConsumer {
        final List<Float> u = new ArrayList<>(), v = new ArrayList<>();
        public VertexConsumer addVertex(float x, float y, float z) { return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setUv(float uu, float vv) { u.add(uu); v.add(vv); return this; }
        public VertexConsumer setUv1(int a, int b) { return this; }
        public VertexConsumer setUv2(int a, int b) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
