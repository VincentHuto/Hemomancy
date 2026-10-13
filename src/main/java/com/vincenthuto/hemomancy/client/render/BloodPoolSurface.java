package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/** The surface of a pool of blood standing in a vessel, drawn into {@link HemoRenderTypes#bloodPool}. */
public final class BloodPoolSurface {
    private BloodPoolSurface() {}

    /**
     * A level square the shader trims to a disc of the given radius, centred on x, z at height y. Jet, heat and fire
     * (0 to 255) say what works the blood: a jet falling into its centre, heat simmering it, a fire burning over it.
     * Alpha is the pool's opacity.
     */
    public static void draw(Matrix4f pose, VertexConsumer vertices, float x, float y, float z, float radius,
            int jet, int heat, int fire, int alpha) {
        float x0 = x - radius, x1 = x + radius, z0 = z - radius, z1 = z + radius;
        vertices.addVertex(pose, x0, y, z0).setUv(0, 0).setColor(jet, heat, fire, alpha);
        vertices.addVertex(pose, x0, y, z1).setUv(0, 1).setColor(jet, heat, fire, alpha);
        vertices.addVertex(pose, x1, y, z1).setUv(1, 1).setColor(jet, heat, fire, alpha);
        vertices.addVertex(pose, x1, y, z0).setUv(1, 0).setColor(jet, heat, fire, alpha);
    }
}
