package com.vincenthuto.hemomancy.client.render.entity.misc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArborGlowGeometryTest {
    @Test
    void cachedVerticesExactlyMatchOriginalEllipsoidOrderAndCoordinates() {
        int vertex = 0;
        for (int lat = 0; lat < 6; lat++) {
            double t0 = Math.PI * lat / 6, t1 = Math.PI * (lat + 1) / 6;
            for (int lon = 0; lon < 10; lon++) {
                double p0 = Math.PI * 2 * lon / 10, p1 = Math.PI * 2 * (lon + 1) / 10;
                for (double[] angle : new double[][]{{t0, p0}, {t1, p0}, {t1, p1}, {t0, p1}}) {
                    assertEquals((float) (Math.sin(angle[0]) * Math.cos(angle[1])), ArborGlowGeometry.x(vertex));
                    assertEquals((float) Math.cos(angle[0]), ArborGlowGeometry.y(vertex));
                    assertEquals((float) (Math.sin(angle[0]) * Math.sin(angle[1])), ArborGlowGeometry.z(vertex));
                    vertex++;
                }
            }
        }
        assertEquals(vertex, ArborGlowGeometry.vertexCount());
    }
}
