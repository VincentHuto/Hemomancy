package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Putriciel's fire: the Iron Brazier's Flammeus recipe (BrazierFlames) without its blood. Steady shader tongues stand on
 * a pyre's ember bed, a central one ringed by smaller ones, over a crimson ground glow, under a smoke card, fed by rising
 * licks. Also the Ash Garden pylon's purge: a ring of the same fire sweeping out over the floor.
 */
public final class PyreFlames {
    private static final int FLAME = 0, GLOW = 2, TORCH = 3;
    private static final int WHITE = 0xFFFFFF;
    /** The torch palette chars with red, its age; red 0 keeps the tongues at their hottest. */
    private static final int TORCH_COLOR = 0x00FFFF;
    /** A flame card's drawn tip stands about this many card heights over its root once the shader trims it. */
    private static final double REACH = 1.45;
    private static final int RING = 6;
    /** Purge tongues stand about this far apart round the ring, in blocks. */
    private static final double PURGE_SPACING = .5;
    private static final Long2LongOpenHashMap FED = new Long2LongOpenHashMap();
    private static ClientLevel world;

    private PyreFlames() {}

    /**
     * Draws a pyre's fire in its block space and feeds it licks while it is in view. bed is the fire's root in the
     * block's space (blocks); its tallest tongue reaches height blocks over it and the smaller ones stand radius out
     * round it. hot burns it in the hotter torch palette.
     */
    public static void render(PoseStack poses, MultiBufferSource buffers, BlockPos pos, Vec3 bed, double radius,
            double height, boolean hot, double time) {
        if (height <= .01) return;
        int seed = Long.hashCode(pos.asLong());
        Vec3 right = facing(Vec3.atLowerCornerOf(pos).add(bed));
        Vec3 up = new Vec3(0, 1, 0);
        int shape = hot ? TORCH : FLAME;
        int color = hot ? TORCH_COLOR : WHITE;
        VertexConsumer vertices = buffers.getBuffer(ThermalRenderTypes.STEADY_FLAME);

        LuxUmbraGeometry.ground(poses, vertices, bed.add(0, .01, 0), radius * 1.7, GLOW, seed + 5, WHITE,
                (float) Math.min(.6, .25 + height));
        double card = height / REACH;
        double breath = 1 + .05 * Math.sin(time * .045 + seed);
        tongue(poses, vertices, bed.add(0, -.03, 0), right, up, card * .6, card * breath, shape, seed, color, .92F);
        for (int i = 0; i < RING; i++) {
            double angle = i * Math.PI * 2 / RING + seed;
            double swell = 1 + .08 * Math.sin(time * (.04 + i * .007) + i * 1.9);
            Vec3 at = bed.add(Math.cos(angle) * radius, -.02, Math.sin(angle) * radius);
            tongue(poses, vertices, at, right, up, card * .32, card * .55 * swell, shape, seed + 7 + i * 13, color, .8F);
        }
        if (!hot) {
            ThermalGeometry.smoke(poses, vertices, bed.add(0, height * 1.25, 0), right, up, .2 + height * .1,
                    .28 + height * .15, seed + 3, .18F);
        }
        feed(pos, bed, radius, height, hot, seed);
    }

    /**
     * The purge in the block's space: a ring of fire tongues radius blocks out from centre over the floor. alpha fades
     * the ring out; age, 0 to 1, chars its tongues to soot from the tips. Tongues that would stand in a wall or over
     * nothing are left out.
     */
    public static void purge(PoseStack poses, MultiBufferSource buffers, BlockPos pos, Vec3 centre, double radius,
            float alpha, float age, double time) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || alpha <= .01F || radius <= 0) return;
        int seed = Long.hashCode(pos.asLong()) * 31;
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        Vec3 up = new Vec3(0, 1, 0);
        int color = TORCH_COLOR | Math.round(255 * Math.min(1, Math.max(0, age))) << 16;
        VertexConsumer vertices = buffers.getBuffer(ThermalRenderTypes.FLAME);
        int count = Math.max(10, Math.min(120, (int) (radius * Math.PI * 2 / PURGE_SPACING)));
        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        for (int ring = 0; ring < 2; ring++) {
            // The front, then the lower wake burning behind it.
            double r = ring == 0 ? radius : radius * .82;
            int n = ring == 0 ? count : count / 2;
            double scale = ring == 0 ? 1 : .6;
            float a = alpha * (ring == 0 ? .95F : .5F);
            for (int i = 0; i < n; i++) {
                double angle = (i + .5 * ring) * Math.PI * 2 / n + seed * .37;
                Vec3 at = centre.add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
                Vec3 inWorld = origin.add(at);
                cell.set(inWorld.x, inWorld.y + .1, inWorld.z);
                if (level.getBlockState(cell).isCollisionShapeFullBlock(level, cell)) continue;
                cell.move(0, -1, 0);
                if (level.getBlockState(cell).isAir()) continue;
                double flicker = 1 + .15 * Math.sin(time * .9 + i * 1.7 + seed);
                double height = (.32 + .1 * Math.sin(i * 2.3 + seed)) * scale * flicker * (.6 + .4 * alpha);
                Vec3 right = facing(inWorld);
                tongue(poses, vertices, at, right, up, height * .75, height, TORCH, seed + i * 17 + ring, color, a);
                if (ring == 0 && i % 5 == 0) {
                    ThermalGeometry.smoke(poses, vertices, at.add(0, height * 1.6, 0), right, up, .26, .36,
                            seed + i, .14F * alpha);
                }
            }
        }
    }

    /** The horizontal right of a camera-facing card at a world point: tongues stand upright and turn only about y. */
    private static Vec3 facing(Vec3 at) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 toward = at.subtract(camera.getPosition());
        Vec3 right = new Vec3(toward.z, 0, -toward.x);
        return right.lengthSqr() > 1e-4 ? right.normalize()
                : new Vec3(new Vector3f(1, 0, 0).rotate(camera.rotation())).multiply(1, 0, 1).normalize();
    }

    /** A flame card standing on its root: the shader curls the bottom quarter in, so the card is lifted to meet it. */
    private static void tongue(PoseStack poses, VertexConsumer vertices, Vec3 root, Vec3 right, Vec3 up,
            double width, double height, int shape, int seed, int color, float alpha) {
        LuxUmbraGeometry.card(poses, vertices, root.add(0, height * .75, 0), right, up, width, height,
                shape, seed, color, alpha);
    }

    private static void feed(BlockPos pos, Vec3 bed, double radius, double height, boolean hot, int seed) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (world != level) { FED.clear(); world = level; }
        long now = level.getGameTime();
        // Frames outnumber ticks, so only the first frame of each tick feeds the fire.
        if (FED.put(pos.asLong(), now) == now) return;
        if (FED.size() > 256) FED.long2LongEntrySet().removeIf(entry -> now - entry.getLongValue() > 20);
        if (now % (hot ? 2 : height < .3 ? 6 : 3) != 0) return;
        var random = level.random;
        double angle = random.nextDouble() * Math.PI * 2, reach = Math.sqrt(random.nextDouble()) * radius * .8;
        double x = Math.cos(angle) * reach, z = Math.sin(angle) * reach;
        // Licks lean in toward the central tongue as they rise.
        ThermalParticles.lick(Vec3.atLowerCornerOf(pos).add(bed).add(x, .06, z),
                new Vec3(-x * .03, .02 + height * .01 + random.nextDouble() * .01, -z * .03), seed + (int) now,
                22 + random.nextInt(8), .12 + height * .25 + random.nextDouble() * .05);
    }
}
