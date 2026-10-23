package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.world.rite.RiteSurface;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every material geometry writes the same vertices through {@code MaterialQuads} as it did with its own encoder: the
 * digest covers each vertex's position, colour and UV bits for Lux and Umbra, Thermal, Ferric and the rite surfaces,
 * across seeds that wrap (Ferric's 128 tiles included). The pinned digest was taken before the encoders were merged.
 */
class MaterialQuadsEncodingTest {
    private static final String DIGEST = "bf9d5654781adfef3b41a4ad197458c7df97707ca37a25d04beb14a1168a2b18";
    private static final int[] SEEDS = {0, 1, 63, 126, 127, 128, 254, 255, -1, -128, 40_001};

    @Test
    void everyGeometryWritesTheSameVertices() throws NoSuchAlgorithmException {
        Recorder out = new Recorder();
        PoseStack p = new PoseStack();
        p.translate(.25, -1.5, 3);
        p.mulPose(Axis.YP.rotationDegrees(33));
        p.mulPose(Axis.XP.rotationDegrees(-12));
        Vec3 camera = new Vec3(2, 1.5, -4);
        Vec3 right = new Vec3(.8, 0, .6), up = new Vec3(0, 1, 0);
        List<Vec3> path = List.of(new Vec3(0, 0, 0), new Vec3(.4, .3, .1), new Vec3(.9, .2, .5), new Vec3(1.3, .8, .4));
        for (int seed : SEEDS) {
            float alpha = .2F + Math.floorMod(seed, 7) * .13F;
            LuxUmbraGeometry.card(p, out, new Vec3(.1, .2, .3), right, up, .4, .6, 3, seed, 0xC81E32, alpha);
            LuxUmbraGeometry.ribbon(p, out, Vec3.ZERO, new Vec3(1, 2, .5), camera, .3, 5, seed, 0x3366FF, alpha, .1, .9);
            LuxUmbraGeometry.surface(p, out, Vec3.ZERO, new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(0, 1, 0), 7, seed,
                    0xFFFFFF, alpha);
            LuxUmbraGeometry.ground(p, out, new Vec3(0, -.5, 0), .7, 2, seed, 0x80FF40, alpha);
            LuxUmbraGeometry.ribbonPath(p, out, path, camera, .2, .05, 0, seed, 0xFF0000, alpha);
            LuxUmbraGeometry.stream(p, out, Vec3.ZERO, new Vec3(0, 3, 1), camera, .15, .4, .3, 1, seed, 0xEEDDCC, alpha);
            ThermalGeometry.flameRibbon(p, out, path, camera, .25, seed, alpha);
            ThermalGeometry.fire(p, out, Vec3.ZERO, right, up, .3, .8, seed, alpha);
            ThermalGeometry.smoke(p, out, Vec3.ZERO, right, up, .3, .8, seed, alpha);
            ThermalGeometry.powder(p, out, Vec3.ZERO, right, up, .2, seed, alpha);
            ThermalGeometry.shard(p, out, .6, seed, alpha);
            ThermalGeometry.clot(p, out, out, new Vec3(.3, 0, .1), .4, seed, .7, alpha);
            ThermalGeometry.blockFaces(p, out, 0b101101, seed, alpha);
            FerricDuctilisGeometry.begin();
            FerricDuctilisGeometry.quad(p, out, Vec3.ZERO, new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(0, 1, 0), 4, seed,
                    0x9C263B, alpha);
            FerricDuctilisGeometry.card(p, out, Vec3.ZERO, right, up, .5, .5, 1, seed, 0xFFE8A0, alpha);
            FerricDuctilisGeometry.ribbon(p, out, Vec3.ZERO, new Vec3(1, 2, 0), .2, .6, 37.5, seed, camera, 0x9C263B, alpha);
            FerricDuctilisGeometry.plate(p, out, new Vec3(0, .2, 0), .6, .4, .1, seed, alpha);
            FerricDuctilisGeometry.spike(p, out, .9, .2, seed, alpha);
            var view = new RiteSurface.View().set(2, 1.5, -4, .8, 0, .6, 0, 1, 0);
            RiteSurface.card(p.last().pose(), out, view, .1, .2, .3, .4, .5, 4, seed, 0xFF2040, alpha);
            RiteSurface.ground(p.last().pose(), out, 0, -.2, 0, .9, 6, seed, 0x204080, alpha);
            RiteSurface.dome(p.last().pose(), out, view, 0, 0, 0, 1.2, 3, 5, seed, 0xFFFFFF, alpha * .5F, alpha);
            RiteSurface.quad(p.last().pose(), out, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 9, seed, 0x112233, alpha, .25, .75);
        }
        assertEquals(DIGEST, out.digest(), out.vertices + " vertices");
    }

    /** Hashes each vertex's float bits and colour bytes in the order they arrive. */
    private static final class Recorder implements VertexConsumer {
        private final MessageDigest sha;
        private final ByteBuffer scratch = ByteBuffer.allocate(16);
        int vertices;

        Recorder() throws NoSuchAlgorithmException {
            sha = MessageDigest.getInstance("SHA-256");
        }

        String digest() {
            return HexFormat.of().formatHex(sha.digest());
        }

        private VertexConsumer floats(float... values) {
            scratch.clear();
            for (float value : values) scratch.putInt(Float.floatToRawIntBits(value));
            sha.update(scratch.array(), 0, scratch.position());
            return this;
        }

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            vertices++;
            return floats(x, y, z);
        }

        @Override public VertexConsumer setColor(int r, int g, int b, int a) {
            sha.update(new byte[]{(byte) r, (byte) g, (byte) b, (byte) a});
            return this;
        }

        @Override public VertexConsumer setUv(float u, float v) {
            return floats(u, v);
        }

        @Override public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override public VertexConsumer setNormal(float x, float y, float z) {
            return floats(x, y, z);
        }
    }
}
