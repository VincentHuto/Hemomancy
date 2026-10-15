package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The Iron Brazier's fire: a pool of blood in its bowl, steady shader flame tongues burning on it, and rising
 * Flammeus licks feeding them.
 */
public final class BrazierFlames {
    /** The blood's surface in blocks above the brazier's base when the bowl is full: over the coals and embers. */
    private static final double POOL_Y = 12.3 / 16.0;
    /** The surface of the first blood poured in: just over the glowing ember bed (11.5 px), so blood always covers it. */
    private static final double POOL_EMPTY_Y = 11.55 / 16.0;
    /** Just past the bowl's inner wall (5 px), so the pool meets it all round; the wall hides the overhang. */
    private static final float POOL_RADIUS = 5.4F / 16.0F;
    private static final int FLAME = 0, TORCH = 3;
    private static final int WHITE = 0xFFFFFF;
    /** The torch palette chars with red, its age; red 0 keeps sanguine tongues at their hottest. */
    private static final int TORCH_COLOR = 0x00FFFF;
    private static final Long2LongOpenHashMap FED = new Long2LongOpenHashMap();
    private static ClientLevel world;

    private BrazierFlames() {}

    /**
     * Draws a brazier's blood and fire in its block space, and feeds a lit fire a lick every few game ticks while it is
     * in view. fill is how full the bowl is, 0 to 1; pour is how fast blood is running in or out of it, which ripples
     * its surface. The fire burns lower as the bowl drains. Sanguine flames, awaiting the echo, burn taller in the
     * hotter torch palette and simmer the blood. drop is how far the brazier's mount holds its bowl below a standing
     * one's (BrazierMount), in blocks.
     */
    public static void render(PoseStack poses, MultiBufferSource buffers, BlockPos pos, boolean lit, boolean sanguine,
            float fill, float pour, double time, double drop) {
        int seed = Long.hashCode(pos.asLong());
        double surfaceY = drop + POOL_EMPTY_Y + (POOL_Y - POOL_EMPTY_Y) * fill;
        if (fill > .002F) {
            pool(poses.last().pose(), buffers.getBuffer(HemoRenderTypes.bloodPool((float) (time % 24000), seed % 1000)),
                    (float) surfaceY, lit, sanguine, pour);
        }
        if (!lit) return;

        // The tongues stand upright and only turn to face the camera about the vertical, like a real fire: seen from
        // above they thin out rather than lying flat across the bowl and hiding the blood.
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 toward = Vec3.atCenterOf(pos).subtract(camera.getPosition());
        Vec3 right = new Vec3(toward.z, 0, -toward.x);
        right = right.lengthSqr() > 1e-4 ? right.normalize()
                : new Vec3(new Vector3f(1, 0, 0).rotate(camera.rotation())).multiply(1, 0, 1).normalize();
        // A deepening rite leans its braziers toward the Focus, feeds them taller and turns them sanguine from
        // halfway; strain gutters them.
        var flame = com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.flameAt(pos, time);
        boolean hot = sanguine || flame.sanguine();
        Vec3 up = new Vec3(flame.leanX(), 1, flame.leanZ()).normalize();
        double scale = (hot ? 1.2 : 1.0) * (.35 + .65 * fill) * flame.height();
        int shape = hot ? TORCH : FLAME;
        int color = hot ? TORCH_COLOR : WHITE;
        VertexConsumer vertices = buffers.getBuffer(ThermalRenderTypes.STEADY_FLAME);
        Vec3 surface = new Vec3(.5, surfaceY, .5);

        // Each tongue keeps its place and only breathes slowly, so the fire reads as one steady blaze.
        double breath = 1 + .05 * Math.sin(time * .045 + seed);
        tongue(poses, vertices, surface.add(0, -.04, 0), right, up, .25 * scale, .42 * scale * breath,
                shape, seed, color, .92F);
        for (int i = 0; i < 5; i++) {
            double angle = i * Math.PI * 2 / 5 + seed;
            double swell = 1 + .08 * Math.sin(time * (.04 + i * .007) + i * 1.9);
            Vec3 at = surface.add(Math.cos(angle) * .14, -.03, Math.sin(angle) * .14);
            tongue(poses, vertices, at, right, up, .13 * scale, .25 * scale * swell, shape, seed + 7 + i * 13,
                    color, .8F);
        }
        if (!hot) ThermalGeometry.smoke(poses, vertices, surface.add(0, .9 * scale, 0), right, up, .22, .32, seed + 3, .18F);
        feed(pos, hot, seed, drop);
    }

    /** The blood in the bowl: rippled by any pour, simmered and lit from above only while the fire burns. */
    private static void pool(Matrix4f pose, VertexConsumer vertices, float y, boolean lit, boolean sanguine, float pour) {
        BloodPoolSurface.draw(pose, vertices, .5F, y, .5F, POOL_RADIUS, Math.round(255 * pour),
                lit ? sanguine ? 255 : 115 : 0, lit ? 255 : 0, 255);
    }

    /** A flame card standing on its root: the shader curls the bottom quarter in, so the card is lifted to meet it. */
    private static void tongue(PoseStack poses, VertexConsumer vertices, Vec3 root, Vec3 right, Vec3 up,
            double width, double height, int shape, int seed, int color, float alpha) {
        LuxUmbraGeometry.card(poses, vertices, root.add(0, height * .75, 0), right, up, width, height,
                shape, seed, color, alpha);
    }

    private static void feed(BlockPos pos, boolean sanguine, int seed, double drop) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (world != level) { FED.clear(); world = level; }
        long now = level.getGameTime();
        // Frames outnumber ticks, so only the first frame of each tick feeds the fire.
        if (FED.put(pos.asLong(), now) == now) return;
        if (FED.size() > 256) FED.long2LongEntrySet().removeIf(entry -> now - entry.getLongValue() > 20);
        if (now % (sanguine ? 2 : 3) != 0) return;
        var random = level.random;
        double angle = random.nextDouble() * Math.PI * 2, reach = Math.sqrt(random.nextDouble()) * .16;
        double x = Math.cos(angle) * reach, z = Math.sin(angle) * reach;
        // Licks lean in toward the main tongue as they rise.
        ThermalParticles.lick(Vec3.atLowerCornerOf(pos).add(.5 + x, drop + POOL_Y + .08, .5 + z),
                new Vec3(-x * .03, .024 + random.nextDouble() * .01, -z * .03), seed + (int) now,
                24 + random.nextInt(8), (sanguine ? .26 : .22) + random.nextDouble() * .06);
    }
}
