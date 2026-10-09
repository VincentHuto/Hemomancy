package com.vincenthuto.hemomancy.client.render.entity.misc;

/** Shared unit ellipsoid; animation only changes its position and scale. */
final class ArborGlowGeometry {
    private static final float[] VERTICES = build();

    private ArborGlowGeometry() { }

    static int vertexCount() { return VERTICES.length / 3; }
    static float x(int vertex) { return VERTICES[vertex * 3]; }
    static float y(int vertex) { return VERTICES[vertex * 3 + 1]; }
    static float z(int vertex) { return VERTICES[vertex * 3 + 2]; }

    private static float[] build() {
        float[] vertices = new float[6 * 10 * 4 * 3];
        int offset = 0;
        for (int lat = 0; lat < 6; lat++) {
            double t0 = Math.PI * lat / 6, t1 = Math.PI * (lat + 1) / 6;
            for (int lon = 0; lon < 10; lon++) {
                double p0 = Math.PI * 2 * lon / 10, p1 = Math.PI * 2 * (lon + 1) / 10;
                offset = put(vertices, offset, t0, p0);
                offset = put(vertices, offset, t1, p0);
                offset = put(vertices, offset, t1, p1);
                offset = put(vertices, offset, t0, p1);
            }
        }
        return vertices;
    }

    private static int put(float[] vertices, int offset, double theta, double phi) {
        vertices[offset++] = (float) (Math.sin(theta) * Math.cos(phi));
        vertices[offset++] = (float) Math.cos(theta);
        vertices[offset++] = (float) (Math.sin(theta) * Math.sin(phi));
        return offset;
    }
}
