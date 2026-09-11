package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class BlackVeilRenderer {
    private static final List<Entry> VEILS = new ArrayList<>();
    private static ClientLevel world;

    private BlackVeilRenderer() {}

    private static boolean ready() {
        var current = Minecraft.getInstance().level;
        if (world != current) { VEILS.clear(); world = current; }
        return world != null;
    }

    public static void addVeil(Vec3 center, float radius, int durationTicks, int seed) {
        if (!ready() || !Double.isFinite(center.lengthSqr()) || !Float.isFinite(radius) || radius <= 0 || radius > 128) return;
        if (VEILS.size() >= 64) VEILS.removeFirst();
        long now = world.getGameTime();
        VEILS.add(new Entry(center, radius, now, now + Math.min(durationTicks, 12000), seed));
    }

    public static void render(PoseStack poses, float partialTick) {
        if (!ready()) return;
        var batch = new LuxUmbraBatch();
        LuxUmbraRenderTypes.begin(world.getGameTime() + partialTick);
        collect(poses, batch, partialTick);
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        batch.draw(dark -> buffers.getBuffer(LuxUmbraRenderTypes.UMBRA));
        LuxUmbraRenderTypes.finish(buffers);
        boundaries(poses, partialTick);
    }

    static void collect(PoseStack poses, LuxUmbraBatch batch, float partialTick) {
        if (!ready()) return;
        long now = world.getGameTime();
        VEILS.removeIf(veil -> now >= veil.until);
        if (VEILS.isEmpty()) return;
        var mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();
        Vec3 at = camera.getPosition();
        Vec3 right = new Vec3(new Vector3f(1, 0, 0).rotate(camera.rotation()));
        Vec3 up = new Vec3(new Vector3f(0, 1, 0).rotate(camera.rotation()));
        var vertices = batch.vertices(true);
        for (var veil : VEILS) {
            if (veil.center.distanceToSqr(at) > 128 * 128) continue;
            float fade = Math.min(Mth.clamp((now + partialTick - veil.born) / 8, 0, 1),
                    Mth.clamp((veil.until - now - partialTick) / 8, 0, 1));
            poses.pushPose();
            poses.translate(veil.center.x - at.x, veil.center.y - at.y, veil.center.z - at.z);
            LuxUmbraGeometry.ground(poses, vertices, new Vec3(0, -.42, 0), veil.radius,
                    LuxUmbraGeometry.POOL, veil.seed, 0xFFFFFF, fade * .72f);
            for (int i = 0; i < 12; i++) {
                double angle = i * 2.39996 + (now + partialTick) * .003;
                double radius = veil.radius * (.55 + .25 * Math.sin(i * 2.7));
                Vec3 point = new Vec3(Math.cos(angle) * radius, -.08 + (i % 3) * .28, Math.sin(angle) * radius);
                LuxUmbraGeometry.card(poses, vertices, point, right, up, .85, .75,
                        LuxUmbraGeometry.WISP, veil.seed + i, 0xFFFFFF, fade * .65f);
            }
            poses.popPose();
        }
    }

    static void boundaries(PoseStack poses, float partialTick) {
        if (!ready() || VEILS.isEmpty()) return;
        long now = world.getGameTime();
        var mc = Minecraft.getInstance();
        Vec3 at = mc.gameRenderer.getMainCamera().getPosition();
        var buffers = mc.renderBuffers().bufferSource();
        var boundaryType = com.vincenthuto.hemomancy.common.init.RenderTypeInit.RITE_BOUNDARY_CORE;
        var boundary = buffers.getBuffer(boundaryType);
        for (var veil : VEILS) {
            if (veil.center.distanceToSqr(at) > 128 * 128) continue;
            float fade = Math.min(Mth.clamp((now + partialTick - veil.born) / 8, 0, 1),
                    Mth.clamp((veil.until - now - partialTick) / 8, 0, 1));
            poses.pushPose();
            poses.translate(veil.center.x - at.x, veil.center.y - at.y - .445, veil.center.z - at.z);
            BloodCraftRingRenderer.drawBoundary(poses, null, boundary, veil.radius, now + partialTick,
                    fade * .55f, 0x767181);
            poses.popPose();
        }
        buffers.endBatch(boundaryType);
    }

    private record Entry(Vec3 center, float radius, long born, long until, int seed) {}
}
