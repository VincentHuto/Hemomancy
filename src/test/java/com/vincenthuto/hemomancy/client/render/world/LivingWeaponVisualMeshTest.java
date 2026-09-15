package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LivingWeaponVisualMeshTest {
    @Test void torchRetainsAVisibleFlameWhenViewedAlongOrAcrossItsDirection() {
        for (Vec3 camera : List.of(new Vec3(0, 0, -4), new Vec3(4, 0, 0))) {
            Capture capture = new Capture();
            LivingTorchFlames.drawTongue(new PoseStack(), capture, new Vec3(0, 0, .3), camera,
                    new Vec3(1, 0, 0), new Vec3(0, 1, 0), 14.5F, 20, 1, true);
            assertTrue(capture.vertices.size() >= 4 && capture.vertices.size() <= 12);
            assertTrue(capture.vertices.stream().allMatch(v -> v.stream().allMatch(Float::isFinite)));
            double projectedArea = 0;
            for (int i = 0; i < capture.vertices.size(); i += 4) {
                Vec3 a = point(capture.vertices.get(i)), b = point(capture.vertices.get(i + 1));
                Vec3 d = point(capture.vertices.get(i + 3));
                projectedArea += Math.abs(b.subtract(a).cross(d.subtract(a)).dot(camera.normalize()));
            }
            assertTrue(projectedArea > .1, "Torch flames disappeared when viewed along their flight");
        }
    }

    private static Vec3 point(List<Float> vertex) { return new Vec3(vertex.get(0), vertex.get(1), vertex.get(2)); }

    private static final class Capture implements VertexConsumer {
        final List<List<Float>> vertices = new ArrayList<>();
        List<Float> current;
        public VertexConsumer addVertex(float x, float y, float z) {
            current = new ArrayList<>(List.of(x, y, z)); vertices.add(current); return this;
        }
        public VertexConsumer setColor(int r, int g, int b, int a) {
            current.addAll(List.of(r / 255F, g / 255F, b / 255F, a / 255F)); return this;
        }
        public VertexConsumer setUv(float u, float v) { current.add(u); current.add(v); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
