package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Running living blood for block renderers: the Animus flow ribbon, whose liquid scrolls from a path's first point to
 * its last with a wet highlight down its length, and the falling droplet. They write depth, so translucent water drawn
 * after them sorts behind. Points and the camera share the pose's space; for a block entity that is block-local, with
 * the camera taken relative to the block.
 */
public final class BloodStreams {
    private static final int RIBBON = 0, DROPLET = 4;
    private static final int WHITE = 0xFFFFFF;
    /** The ribbon's liquid fills about this share of its card, so a stream is drawn this much wider than it looks. */
    public static final double BODY_SHARE = .3;

    private BloodStreams() {}

    /** A stream along points, its half-width running from startWidth to endWidth, both as the liquid looks. */
    public static void stream(PoseStack poses, MultiBufferSource buffers, List<Vec3> points, Vec3 camera,
            double startWidth, double endWidth, int seed, float alpha) {
        LuxUmbraGeometry.ribbonPath(poses, buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS_DEPTH), points, camera,
                startWidth / BODY_SHARE, endWidth / BODY_SHARE, RIBBON, seed, WHITE, alpha);
    }

    /** A drop of blood facing the camera, flattened a little along its fall. */
    public static void droplet(PoseStack poses, MultiBufferSource buffers, Vec3 at, Vec3 right, Vec3 up, double size,
            int seed, float alpha) {
        LuxUmbraGeometry.card(poses, buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS_DEPTH), at, right, up, size, size * 1.2,
                DROPLET, seed, WHITE, alpha);
    }
}
