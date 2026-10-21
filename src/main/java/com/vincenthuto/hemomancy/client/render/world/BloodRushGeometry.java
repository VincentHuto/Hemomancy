package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodRushRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Blood Rush: the heart floods the legs and the body launches inside its own blood. Heartbeat rings pump down from a
 * swelling chest knot while the leg veins fill; on the kick-off a splash rings the ground where the caster stood and a
 * sheath of blood streams back off the shoulders and hips, flame licking at the heels; then the sheath frays upward
 * and falls behind. Points are world-axis offsets from the caster's feet.
 */
final class BloodRushGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final Vec3 UP = CasterFrame.UP;

    private BloodRushGeometry() {}

    static boolean handles(Form form) {
        return form == Form.RUSH;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer blood, VertexConsumer flame, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha, CasterFrame frame) {
        if (alpha <= 0) return;
        int seed = Math.floorMod(packet.entityId(), 113);
        double s = frame.scale();
        Vec3 f = frame.forward(), r = frame.right();
        Vec3 chest = UP.scale(frame.chest()).add(f.scale(0.08 * s));
        double surge = BloodRushRules.surge(age), wake = BloodRushRules.wake(age), fray = BloodRushRules.fray(age);
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        float body = alpha * (float) (1 - fray * 0.85);

        // 1. Heart: a knot at the chest that swells with each beat and empties into the legs.
        if (!frame.firstPerson()) {
            double beat = 1 + 0.5 * pulse + (age < BloodRushRules.SURGE_END ? 0.4 * (1 - surge) : 0);
            float heart = body * (float) (age < BloodRushRules.LAUNCH_END ? 1 : 1 - fray);
            card(p, v(blood), chest, right, up, 0.20 * s * beat, 0.20 * s * beat, FOCUS, seed, TINT, heart * 0.85f);
            card(p, v(blood), chest, right, up, 0.07 * s * beat, 0.07 * s * beat, FOCUS, seed + 1, CORE, heart);
        }

        // 2. Heartbeat rings travelling from chest to feet, one beat apart.
        for (int ring = 0; ring < 3; ring++) {
            double h = BloodRushRules.beatHeight(age, ring);
            if (h < 0 || h >= 1) continue;
            double y = 0.08 * s + (frame.chest() - 0.08 * s) * h;
            if (frame.firstPerson() && y > 0.9 * s) continue;
            double radius = (0.30 + 0.06 * Math.sin(h * Math.PI)) * s;
            List<Vec3> loop = new ArrayList<>(17);
            for (int i = 0; i <= 16; i++) {
                double a = i * Math.PI / 8 + seed;
                loop.add(new Vec3(Math.cos(a) * radius, y, Math.sin(a) * radius));
            }
            float fade = body * (float) Math.sin(h * Math.PI);
            ribbonPath(p, blood, loop, camera, 0.05 * s, RIBBON, seed + 10 + ring, ring == 0 ? CLOT : TINT, fade);
            ribbonPath(p, blood, loop, camera, 0.012 * s, RIBBON, seed + 13 + ring, CORE, fade * 0.8f);
        }

        // 3. Leg veins fill downward with the surge and stay lit through the launch.
        double filled = Math.min(1, surge * 1.1);
        for (int leg = -1; leg <= 1; leg += 2) {
            Vec3 hip = UP.scale((frame.crouching() ? 0.62 : 0.78) * s).add(r.scale(leg * 0.12 * s));
            Vec3 ankle = UP.scale(0.1 * s).add(r.scale(leg * 0.13 * s)).add(f.scale(0.02 * s));
            Vec3 knee = hip.lerp(ankle, 0.5).add(f.scale(0.09 * s));
            List<Vec3> vein = new ArrayList<>(7);
            for (int i = 0; i <= 6; i++) {
                double t = filled * i / 6.0;
                Vec3 a = hip.lerp(knee, Math.min(1, t * 2)), b = knee.lerp(ankle, Math.max(0, t * 2 - 1));
                vein.add(t < 0.5 ? a : b);
            }
            float lit = body * (0.7f + 0.3f * pulse);
            ribbonPath(p, blood, vein, camera, 0.035 * s, 0.02 * s, RIBBON, seed + 20 + leg, TINT, lit);
            ribbonPath(p, blood, vein, camera, 0.01 * s, 0.006 * s, RIBBON, seed + 22 + leg, CORE, lit * 0.8f);
        }

        // 4. Kick-off: a splash rings the ground where the caster stood.
        if (age >= BloodRushRules.SURGE_END - 0.5 && age < BloodRushRules.LAUNCH_END + 4) {
            double t = (age - BloodRushRules.SURGE_END + 0.5) / (BloodRushRules.LAUNCH_END + 4.5 - BloodRushRules.SURGE_END);
            Vec3 at = new Vec3(frame.origin().x, 0.05, frame.origin().z);
            float fade = alpha * (float) (1 - t);
            ground(p, blood, at, (0.35 + 0.9 * t) * s, RIPPLE, seed + 30, TINT, fade * 0.8f);
            ground(p, blood, at, (0.25 + 0.3 * t) * s, POOL, seed + 31, CLOT, fade * 0.6f);
        }

        // 5. Wake: strands of blood stream back off the shoulders and hips, against the direction of travel.
        if (wake > 0) {
            Vec3 back = frame.travel().scale(-1);
            Vec3 side = back.cross(UP).normalize();
            double stretch = wake * (1 + Math.min(1.5, frame.speed() * 2.5));
            List<Vec3> center = null;
            for (int strand = 0; strand < BloodRushRules.WAKE_STRANDS; strand++) {
                double lane = strand - (BloodRushRules.WAKE_STRANDS - 1) / 2.0;
                double height = (strand % 2 == 0 ? 1.32 : 0.86) * s - (frame.crouching() ? 0.25 * s : 0);
                Vec3 root = UP.scale(height).add(side.scale(lane * 0.11 * s)).add(back.scale(0.12 * s));
                double length = (1.1 + 0.25 * (strand % 3)) * s * stretch;
                List<Vec3> path = new ArrayList<>(9);
                for (int i = 0; i <= 8; i++) {
                    double t = i / 8.0;
                    double wave = Math.sin(t * 4 - time * 0.5 + strand * 1.3) * 0.07 * t * s;
                    path.add(root.add(back.scale(length * t)).add(side.scale(wave + lane * 0.08 * t * s))
                            .add(UP.scale((fray * 0.5 * t - 0.12 * t * t) * s)));
                }
                if (frame.firstPerson() && strand % 2 == 0) {
                    // The shoulder strands start at the camera; draw only their trailing half.
                    path = path.subList(4, path.size());
                }
                if (strand == BloodRushRules.WAKE_STRANDS / 2) center = path;
                float strandAlpha = body * (0.8f + 0.2f * (float) Math.sin(time * 0.9 + strand * 1.7 + seed));
                ribbonPath(p, blood, path, camera, 0.06 * s * (1 + fray), 0.012 * s, RIBBON, seed + 40 + strand, TINT, strandAlpha);
                ribbonPath(p, blood, path.subList(path.size() / 2, path.size()), camera, 0.035 * s, 0.008 * s, RIBBON,
                        seed + 50 + strand, CLOT, strandAlpha * 0.9f);
                ribbonPath(p, blood, path, camera, 0.012 * s, 0.003 * s, RIBBON, seed + 60 + strand, CORE, strandAlpha * 0.7f);
                // Flame-like wisps peel back off the strand.
                for (int i = 2; i < path.size(); i += 2) {
                    double flicker = 0.85 + 0.25 * Math.sin(time * 0.9 + i * 1.7 + strand + seed);
                    double w = 0.13 * s * flicker * (1 + fray * 0.8) * (1 - i / 12.0);
                    card(p, blood, path.get(i).add(UP.scale(fray * 0.2 * s)), right, up, w, w * 1.2, WISP,
                            seed + 70 + strand * 8 + i, TINT, body * 0.3f);
                }
            }
            // Shell: one broad translucent sheath down the middle of the wake.
            if (center != null) ribbonPath(p, blood, center, camera, 0.24 * s * (1 + fray * 0.5), 0.05 * s, RIBBON,
                    seed + 90, TINT, body * 0.28f);

            // Heels: flame licks where the blood is spent, for the launch only.
            if (age < BloodRushRules.LAUNCH_END + 3) {
                float heat = alpha * (float) (1 - Math.max(0, (age - BloodRushRules.LAUNCH_END) / 3));
                for (int leg = -1; leg <= 1; leg += 2) {
                    Vec3 heel = UP.scale(0.08 * s).add(r.scale(leg * 0.13 * s)).add(back.scale(0.1 * s));
                    ThermalGeometry.flameRibbon(p, flame, ThermalGeometry.tongue(heel, back.add(UP.scale(0.4)),
                            0.35 * s * wake, time, seed + 95 + leg), camera, 0.07 * s, seed + 95 + leg, heat * 0.8f);
                }
            }
        }
    }

    private static VertexConsumer v(VertexConsumer blood) {
        return blood;
    }
}
