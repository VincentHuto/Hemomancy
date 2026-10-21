package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.common.lightning.LightningTestBoltFactory;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * The cast outpouring's door into the manipulation toolkit, which is package-private here: the shared material batch
 * (living blood, flame, molten, cruor, glass, ductilis, mortem, lux, umbra), the flame/ice/iron geometry, the school
 * particles and the HutosLib lightning the Ductilis manipulations strike with. Everything drawn through here looks like
 * the rest of the mod because it is the rest of the mod's art.
 */
public final class OutpouringBrushes {
    /** The batch materials, in the toolkit's own order. */
    public enum Mat { LUX, UMBRA, FLAME, MOLTEN, GLASS, CRUOR, DUCTILIS, ANIMUS, MORTEM }
    /** Shape ids the material shaders decode from UV (see each manipulation_*.fsh). */
    public static final int RIBBON = LuxUmbraGeometry.RIBBON, WISP = LuxUmbraGeometry.WISP, POOL = LuxUmbraGeometry.POOL,
            FOCUS = LuxUmbraGeometry.FOCUS, RIPPLE = LuxUmbraGeometry.RIPPLE, FROST_STRAND = ThermalGeometry.FROST_STRAND;
    public static final int NERVE = FerricDuctilisGeometry.NERVE, BLOOD = FerricDuctilisGeometry.BLOOD;
    private static final int BOLT_OUTER = 0xE8FFE84A, BOLT_INNER = 0xFFFFFFFF;
    private static final LuxUmbraBatch BATCH = new LuxUmbraBatch();

    private OutpouringBrushes() {}

    /** Starts a frame: clears the batch and sets every material's clock. */
    public static void begin(double time) {
        BATCH.clear();
        LuxUmbraRenderTypes.begin(time);
        ThermalRenderTypes.begin(time);
        AnimusMortemRenderTypes.begin(time);
        FerricDuctilisRenderTypes.begin(time);
        FerricDuctilisGeometry.begin();
    }

    public static VertexConsumer flow(Mat material) { return BATCH.vertices(LuxUmbraBatch.Material.values()[material.ordinal()]); }
    /** Solid frozen blood (the clot cores inside frost shells). */
    public static VertexConsumer core(MultiBufferSource buffers) { return buffers.getBuffer(ThermalRenderTypes.CORE); }
    /** Solid blood-iron: alpha is formation, the ferric shader's rising wet-blood front. */
    public static VertexConsumer iron(MultiBufferSource buffers) { return buffers.getBuffer(FerricDuctilisRenderTypes.IRON); }

    /** Ends the frame: solids first, then every translucent quad sorted back to front across materials. */
    public static void flush(MultiBufferSource.BufferSource buffers) {
        buffers.endBatch(FerricDuctilisRenderTypes.IRON);
        buffers.endBatch(ThermalRenderTypes.CORE);
        BATCH.drawMaterials(material -> buffers.getBuffer(ThermalRenderTypes.material(material)));
        for (var material : LuxUmbraBatch.Material.values()) buffers.endBatch(ThermalRenderTypes.material(material));
        LuxUmbraRenderTypes.finish(buffers);
    }

    //  Geometry (camera-relative points; the camera is the origin)

    public static void card(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double width, double height, int shape,
                            int seed, int color, float alpha) {
        LuxUmbraGeometry.card(p, v, at, right, up, width, height, shape, seed, color, alpha);
    }

    public static void ribbonPath(PoseStack p, VertexConsumer v, List<Vec3> points, double startWidth, double endWidth,
                                  int shape, int seed, int color, float alpha) {
        LuxUmbraGeometry.ribbonPath(p, v, points, Vec3.ZERO, startWidth, endWidth, shape, seed, color, alpha);
    }

    public static void ground(PoseStack p, VertexConsumer v, Vec3 at, double radius, int shape, int seed, int color, float alpha) {
        LuxUmbraGeometry.ground(p, v, at, radius, shape, seed, color, alpha);
    }

    public static void fire(PoseStack p, VertexConsumer v, Vec3 bottom, Vec3 right, Vec3 up, double width, double height, int seed, float alpha) {
        ThermalGeometry.fire(p, v, bottom, right, up, width, height, seed, alpha);
    }

    public static void smoke(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double width, double height, int seed, float alpha) {
        ThermalGeometry.smoke(p, v, at, right, up, width, height, seed, alpha);
    }

    /** A bent flame tongue rising from {@code root}, drawn root to sooty tip as one flame. */
    public static void tongue(PoseStack p, VertexConsumer flame, Vec3 root, Vec3 lean, double height, double width, double time,
                              int seed, float alpha) {
        ThermalGeometry.flameRibbon(p, flame, ThermalGeometry.tongue(root, lean, height, time, seed), Vec3.ZERO, width, seed, alpha);
    }

    /** Frozen blood: a solid clot inside a cracked frost shell. */
    public static void clot(PoseStack p, VertexConsumer core, Vec3 at, double size, int seed, double rotation, float alpha) {
        ThermalGeometry.clot(p, core, flow(Mat.CRUOR), at, size, seed, rotation, alpha);
    }

    /** A thick five-sided glass shard in the pose's XY plane, rising along +Y. */
    public static void shard(PoseStack p, double size, int seed, float alpha) {
        ThermalGeometry.shard(p, flow(Mat.GLASS), size, seed, alpha);
    }

    public static void plate(PoseStack p, VertexConsumer iron, Vec3 center, double width, double height, double depth, int seed, float alpha) {
        FerricDuctilisGeometry.plate(p, iron, center, width, height, depth, seed, alpha);
    }

    /** Any quad in the ferric toolkit's encoding, for meshes the toolkit has no helper for (finer spikes). */
    public static void ferricQuad(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int seed, float alpha) {
        FerricDuctilisGeometry.quad(p, v, a, b, c, d, 0, seed, 0xFFFFFF, alpha);
    }

    /** A blood (or nerve) ribbon on the Ductilis material, with the ferric toolkit's sway. */
    public static void sinew(PoseStack p, Vec3 start, Vec3 end, double width, double bend, double time, int seed, int color, float alpha) {
        FerricDuctilisGeometry.ribbon(p, flow(Mat.DUCTILIS), start, end, width, bend, time, seed, Vec3.ZERO, color, alpha);
    }

    //  The school particles (world space)

    public static void flames(Vec3 at, Vec3 direction, int count, boolean soft) { ThermalParticles.emit(at, direction, false, count, soft); }
    public static void frost(Vec3 at, Vec3 direction, int count, boolean soft) { ThermalParticles.emit(at, direction, true, count, soft); }
    public static void lick(Vec3 at, Vec3 velocity, int seed, int life, double size) { ThermalParticles.lick(at, velocity, seed, life, size); }
    public static void blood(Vec3 at, Vec3 direction, boolean mortem, int count, boolean soft) {
        BloodEffectParticles.emit(at, direction, mortem, count, soft, false);
    }
    public static void mist(Vec3 at, Vec3 velocity, boolean umbra, float size, int life) { LuxUmbraEffects.mist(at, velocity, umbra, size, life); }
    /** Mist that thins over the body of the caster it came from, so it never hides them. */
    public static void mist(Vec3 at, Vec3 velocity, boolean umbra, float size, int life, int clearOf) { LuxUmbraEffects.mist(at, velocity, umbra, size, life, clearOf); }

    /** A Ductilis bolt as the live manipulations strike it: a yellow sheath around a white core (world space). */
    public static void bolt(Vec3 from, Vec3 to, long seed, int fract, float maxOffset, float size) {
        var config = new LightningTestConfig(LightningTestConfig.Backend.BOLT, BOLT_OUTER, BOLT_OUTER, BOLT_INNER,
                (float) Math.max(8, from.distanceTo(to) + 4), 0, 0, 0, 62, 2.1F, 5, fract, maxOffset, size, true, seed, false, 20).clamped();
        BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(from, to, seed, config.outerColor(), config.size(), config), 0);
        BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(from, to, seed, config.innerColor(), Math.max(.01F, config.size() * .45F), config), 0);
    }
}
