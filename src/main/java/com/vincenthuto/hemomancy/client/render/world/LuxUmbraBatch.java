package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** Sorts translucent quads across manipulation materials, retaining buffers between frames. */
final class LuxUmbraBatch {
    enum Material { LUX, UMBRA, FLAME, MOLTEN, GLASS, CRUOR, DUCTILIS, ANIMUS, MORTEM }
    private final List<Quad> quads = new ArrayList<>();
    private final Writer[] writers = java.util.Arrays.stream(Material.values()).map(Writer::new).toArray(Writer[]::new);
    private int count;

    VertexConsumer vertices(boolean umbra) { return vertices(umbra ? Material.UMBRA : Material.LUX); }
    VertexConsumer vertices(Material material) { return writers[material.ordinal()]; }

    void clear() {
        count = 0;
        for (Writer writer : writers) writer.vertex = 3;
    }

    void draw(Function<Boolean, VertexConsumer> destination) {
        drawMaterials(material -> destination.apply(material == Material.UMBRA));
    }

    void drawMaterials(Function<Material, VertexConsumer> destination) {
        var visible = quads.subList(0, count);
        for (Quad quad : visible) {
            float x = 0, y = 0, z = 0;
            for (int i = 0; i < 4; i++) {
                x += quad.positionUv[i * 5]; y += quad.positionUv[i * 5 + 1]; z += quad.positionUv[i * 5 + 2];
            }
            quad.distance = x * x + y * y + z * z;
        }
        visible.sort(Comparator.comparingDouble((Quad quad) -> quad.distance).reversed());
        for (Quad quad : visible) {
            VertexConsumer out = destination.apply(quad.material);
            for (int i = 0; i < 4; i++) {
                int p = i * 5, color = quad.color[i];
                out.addVertex(quad.positionUv[p], quad.positionUv[p + 1], quad.positionUv[p + 2])
                        .setColor(color >>> 24, (color >>> 16) & 255, (color >>> 8) & 255, color & 255)
                        .setUv(quad.positionUv[p + 3], quad.positionUv[p + 4]);
            }
        }
    }

    private static final class Quad {
        final float[] positionUv = new float[20];
        final int[] color = new int[4];
        Material material;
        float distance;
    }

    private final class Writer implements VertexConsumer {
        final Material material;
        int vertex = 3;
        Quad quad;
        Writer(Material material) { this.material = material; }

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            vertex = (vertex + 1) % 4;
            if (vertex == 0) {
                if (count == quads.size()) quads.add(new Quad());
                quad = quads.get(count++);
                quad.material = material;
            }
            int p = vertex * 5;
            quad.positionUv[p] = x; quad.positionUv[p + 1] = y; quad.positionUv[p + 2] = z;
            return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) {
            quad.color[vertex] = r << 24 | g << 16 | b << 8 | a;
            return this;
        }
        @Override public VertexConsumer setUv(float u, float v) {
            quad.positionUv[vertex * 5 + 3] = u; quad.positionUv[vertex * 5 + 4] = v;
            return this;
        }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
