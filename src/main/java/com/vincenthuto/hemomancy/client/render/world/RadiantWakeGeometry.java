package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.lux.RadiantWakeRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Radiant Wake: wings of hematic light. Two fans of filament feathers unfurl from between the shoulders, each with a
 * crimson undertone and a gold bead at its tip, and beat slowly; a soft shell glows at their root. While the caster
 * glides the wings flatten and long streamers of light trail beneath them. Points are world-axis offsets from the
 * caster's feet so camera-facing cards stay true.
 */
final class RadiantWakeGeometry {
    private static final Vec3 UP = new Vec3(0, 1, 0);

    /** Wearer pose: body yaw roots the wings. */
    record Frame(float bodyYaw, float scale, boolean crouching, boolean firstPerson) {}

    private RadiantWakeGeometry() {}

    static boolean handles(Form form) {
        return form == Form.RADIANT_WAKE;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, Frame frame) {
        if (alpha <= 0) return;
        double s = frame.scale();
        double yaw = Math.toRadians(frame.bodyYaw());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 side = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        boolean gliding = RadiantWakeRules.gliding(packet.count());
        double spread = RadiantWakeRules.unfurl(age), beat = RadiantWakeRules.beat(time, gliding);
        int seed = Math.floorMod(packet.entityId(), 113);
        Vec3 shoulder = UP.scale((frame.crouching() ? 1.15 : 1.4) * s).subtract(forward.scale(0.22 * s));
        // In first person the wings sit beside the view; keep them faint so they never wall it off.
        float a = alpha * (frame.firstPerson() ? 0.45f : 1f);

        // 1. Root shell: a soft gold glow where the wings leave the back.
        card(p, v, shoulder.subtract(forward.scale(0.08 * s)), right, up, 0.45 * s * spread, 0.45 * s * spread, LUX_SHELL,
                seed, GOLD, a * 0.35f);
        card(p, v, shoulder, right, up, 0.16 * s * spread, 0.16 * s * spread, FOCUS, seed + 1, WHITE, a * 0.8f);

        // 2. Feathers: filaments fanned back from the shoulder line, outer ones longer and trailing.
        for (int wing = -1; wing <= 1; wing += 2) {
            Vec3 root = shoulder.add(side.scale(wing * 0.12 * s));
            for (int f = 0; f < RadiantWakeRules.FEATHERS; f++) {
                double sweep = RadiantWakeRules.featherSweep(f, spread);
                double lift = (0.55 - f * 0.17 + beat * 0.25) * spread;
                Vec3 dir = side.scale(wing * Math.cos(sweep)).subtract(forward.scale(Math.sin(sweep))).add(UP.scale(lift)).normalize();
                double length = (0.85 + 0.22 * f) * s * spread;
                double flicker = 0.85 + 0.2 * Math.sin(time * 0.7 + f * 1.7 + wing + seed);
                Vec3 mid = root.add(dir.scale(length * 0.5)).add(UP.scale(0.08 * s * spread));
                Vec3 tip = root.add(dir.scale(length)).add(UP.scale((beat * 0.12 - f * 0.04) * s));
                List<Vec3> feather = List.of(root, mid, tip);
                ribbonPath(p, v, feather, camera, 0.055 * s, 0.01 * s, RIBBON, seed + 10 + f * 2 + wing, WHITE, a * 0.9f);
                ribbonPath(p, v, feather, camera, 0.016 * s, 0.004 * s, RIBBON, seed + 30 + f * 2 + wing, CRIMSON, a * 0.6f);
                card(p, v, tip, right, up, 0.06 * s * flicker, 0.06 * s * flicker, FOCUS, seed + 50 + f * 2 + wing, GOLD, a * 0.7f);
            }
        }

        // 3. Gliding: long streamers of light trail beneath and behind the caster.
        if (gliding)
            for (int k = 0; k < 4; k++) {
                Vec3 start = UP.scale(0.9 * s).add(side.scale((k - 1.5) * 0.18 * s));
                Vec3 end = start.subtract(forward.scale((1.6 + 0.3 * k) * s)).subtract(UP.scale((0.5 + 0.12 * k) * s));
                stream(p, v, start, end, camera, 0.04 * s, 0.12, time * 0.15 + k, RIBBON, seed + 70 + k, WHITE, a * 0.5f);
            }
    }
}
