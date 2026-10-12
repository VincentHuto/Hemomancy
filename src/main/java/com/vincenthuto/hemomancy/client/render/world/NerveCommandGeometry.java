package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.card;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.ribbonPath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.NerveCommandRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Hematic Rebuke and Impressment: a command carried down another body's nerves. Rebuke fires a crackling impulse from
 * the caster's eyes, clamps a jittering net over the skull that recoils on arrival, and drives fearful pulses down the
 * spine into twitching legs. Impressment sends a calm impulse, then holds a steady conducted leash from the caster to
 * a sigil at the nape, with slow loyal pulses travelling along it and down the spine. Everything is on the Ductilis
 * nerve material; points are world-axis offsets from the commanded body's feet.
 */
final class NerveCommandGeometry {
    private static final int NERVE = 0xFFE8A0, BLOOD = 0x9C263B, CORE = 0xFFD2C4;
    /** Ductilis shader shapes. */
    private static final int LINE = 0, GLOW = 1, CRACKLE = 2, SPARK = 3;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private NerveCommandGeometry() {}

    static boolean handles(Form form) {
        return form == Form.NERVE_COMMAND;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, LivingEntity body) {
        if (alpha <= 0) return;
        boolean rebuke = NerveCommandRules.rebuke(packet.count());
        int seed = Math.floorMod(packet.entityId(), 113);
        double h = Math.max(0.6, packet.radius());
        double w = body != null ? Math.max(0.3, body.getBbWidth()) : 0.6;
        double yaw = body != null ? Math.toRadians(body.yBodyRot) : 0;
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 side = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        double jitter = NerveCommandRules.jitter(age, rebuke);
        Vec3 crown = UP.scale(h * 1.04);
        Vec3 nape = UP.scale(h * 0.84).subtract(forward.scale(w * 0.35));
        Vec3 hips = UP.scale(h * 0.45).subtract(forward.scale(w * 0.3));

        // 1. Cranial net: strands from the nape over the skull, crackling and recoiling under a rebuke.
        int shape = rebuke ? CRACKLE : LINE;
        for (int i = 0; i < NerveCommandRules.NET_STRANDS; i++) {
            double a = i * Math.PI * 2 / NerveCommandRules.NET_STRANDS + yaw;
            Vec3 out = new Vec3(Math.cos(a), 0, Math.sin(a));
            Vec3 over = crown.add(out.scale(w * 0.18)).add(shake(time, seed, i, 0, jitter));
            Vec3 brow = UP.scale(h * 0.86).add(out.scale(w * 0.52)).add(shake(time, seed, i, 1, jitter));
            ribbonPath(p, v, List.of(nape, over, brow), camera, 0.024, 0.008, shape, seed + i, NERVE, alpha * 0.85f);
        }
        if (rebuke) {
            float flicker = (float) (0.7 + 0.3 * Math.sin(time * 1.9 + seed));
            card(p, v, nape, right, up, 0.14 * flicker, 0.14 * flicker, GLOW, seed + 10, NERVE, alpha);
        } else {
            // The impressed body's sigil: a slow ring of blood-lit nodes around the nape.
            card(p, v, nape, right, up, 0.18, 0.18, GLOW, seed + 10, BLOOD, alpha * 0.8f);
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3 + time * 0.03;
                Vec3 at = nape.add(side.scale(Math.cos(a) * 0.16)).add(UP.scale(Math.sin(a) * 0.16));
                card(p, v, at, right, up, 0.035, 0.035, GLOW, seed + 11 + k, CORE, alpha * 0.8f);
            }
        }

        // 2. Spine: the order running down to the legs; fast and fearful for a rebuke, slow for an impressment.
        ribbonPath(p, v, List.of(nape, nape.lerp(hips, 0.5).subtract(forward.scale(w * 0.05)), hips), camera, 0.02, 0.016,
                LINE, seed + 20, NERVE, alpha * 0.7f);
        double phase = NerveCommandRules.spinePulse(time, rebuke, seed);
        card(p, v, nape.lerp(hips, phase), right, up, rebuke ? 0.08 : 0.06, rebuke ? 0.08 : 0.06, SPARK, seed + 21, CORE, alpha);
        for (int leg = -1; leg <= 1; leg += 2) {
            Vec3 foot = side.scale(leg * w * 0.25).add(UP.scale(0.08));
            Vec3 knee = hips.lerp(foot, 0.5).add(forward.scale(w * 0.1)).add(shake(time, seed, 30 + leg, 2, rebuke ? jitter * 1.5 : 0));
            ribbonPath(p, v, List.of(hips, knee, foot), camera, 0.016, 0.008, rebuke ? CRACKLE : LINE, seed + 25 + leg, NERVE,
                    alpha * (rebuke ? 0.75f : 0.45f));
        }

        // 3. The command itself: a cast impulse from the caster's eyes, and for an impressment the held leash.
        Vec3 caster = packet.to().subtract(packet.from());
        Vec3 toward = caster.subtract(crown);
        double reach = toward.length();
        if (reach < 0.5) return;
        // Start a little out from the caster so the line never walls off their own view.
        Vec3 start = crown.add(toward.scale(Math.max(0, 1 - 1.2 / reach)));
        double impulse = NerveCommandRules.impulse(age), fade = NerveCommandRules.impulseFade(age);
        if (fade > 0) {
            List<Vec3> bolt = jagged(start, crown, rebuke ? 0.18 : 0.06, time, seed, impulse);
            ribbonPath(p, v, bolt, camera, 0.03, 0.02, rebuke ? CRACKLE : LINE, seed + 40, NERVE, alpha * (float) fade);
            card(p, v, bolt.getLast(), right, up, 0.12, 0.12, SPARK, seed + 41, CORE, alpha * (float) fade);
        }
        if (!rebuke && impulse >= 1) {
            List<Vec3> leash = new ArrayList<>(13);
            for (int i = 0; i <= 12; i++) {
                double t = i / 12.0;
                leash.add(start.lerp(nape, t).add(UP.scale(-Math.sin(t * Math.PI) * 0.25)));
            }
            ribbonPath(p, v, leash, camera, 0.016, 0.012, LINE, seed + 50, NERVE, alpha * 0.55f);
            double t = NerveCommandRules.leashPulse(time, seed);
            Vec3 at = leash.get((int) Math.min(12, Math.round(t * 12)));
            card(p, v, at, right, up, 0.07, 0.07, SPARK, seed + 51, CORE, alpha * (float) Math.sin(t * Math.PI));
        }
    }

    /** A seeded tremor that re-rolls a few times a second, so nerves twitch rather than drift. */
    private static Vec3 shake(double time, int seed, int strand, int point, double amount) {
        if (amount <= 0) return Vec3.ZERO;
        double step = Math.floor(time * 0.5);
        return new Vec3(hash(step, seed, strand * 7 + point), hash(step, seed, strand * 7 + point + 3) * 0.5,
                hash(step, seed, strand * 7 + point + 5)).scale(amount);
    }

    /** A jagged nerve path from {@code from} toward {@code to}, cut off at {@code extent}. */
    private static List<Vec3> jagged(Vec3 from, Vec3 to, double roughness, double time, int seed, double extent) {
        Vec3 axis = to.subtract(from);
        Vec3[] b = BloodShotGeometry.basis(axis);
        double step = Math.floor(time * 0.7);
        List<Vec3> points = new ArrayList<>(10);
        int segments = 8;
        for (int i = 0; i <= segments; i++) {
            double t = extent * i / segments;
            double envelope = Math.sin(t * Math.PI) * roughness;
            points.add(from.add(axis.scale(t)).add(b[0].scale(hash(step, seed, i) * envelope))
                    .add(b[1].scale(hash(step, seed, i + 20) * envelope)));
        }
        return points;
    }

    /** Deterministic noise in -1..1. */
    private static double hash(double step, int seed, int slot) {
        double x = Math.sin(step * 12.9898 + seed * 78.233 + slot * 37.719) * 43758.5453;
        return (x - Math.floor(x)) * 2 - 1;
    }
}
