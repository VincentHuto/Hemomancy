package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LuxUmbraBatchTest {
    @Test void overlappingSchoolsSortTogetherAndTheNextFrameStartsEmpty() {
        var batch = new LuxUmbraBatch();
        quad(batch.vertices(false), 2);
        quad(batch.vertices(true), 8);
        quad(batch.vertices(false), 12);
        List<Boolean> schools = new ArrayList<>();
        List<Float> depths = new ArrayList<>();
        var capture = new Capture(depths);
        batch.draw(dark -> { schools.add(dark); return capture; });
        assertEquals(List.of(false, true, false), schools);
        assertEquals(List.of(12f, 12f, 12f, 12f, 8f, 8f, 8f, 8f, 2f, 2f, 2f, 2f), depths);
        batch.clear();
        schools.clear(); depths.clear();
        quad(batch.vertices(true), 5);
        batch.draw(dark -> { schools.add(dark); return capture; });
        assertEquals(List.of(true), schools);
        assertEquals(List.of(5f, 5f, 5f, 5f), depths);
    }

    private static void quad(VertexConsumer v, float z) {
        for (int i = 0; i < 4; i++) v.addVertex(0, 0, z).setColor(255, 250, 239, 128).setUv(i % 2, i / 2);
    }

    private record Capture(List<Float> depths) implements VertexConsumer {
        public VertexConsumer addVertex(float x, float y, float z) { depths.add(z); return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { assertEquals(128, a); return this; }
        public VertexConsumer setUv(float u, float v) { assertTrue(u >= 0 && u <= 1 && v >= 0 && v <= 1); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
