package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Hematic Riposte's grown limb: an oversized blood-arm rooted between the shoulder blades. A flickering translucent
 * shell trails behind the sweep around a core of twisting sinew, ending in a clawed hand that slaps across the front
 * and then frays apart. All points are world-axis offsets from the wearer's feet so camera-facing cards stay true.
 */
final class HematicRiposteGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final double BEHIND = Math.toRadians(150), OVERSHOOT = Math.toRadians(35);
    private static final int SPINE_SAMPLES = 14;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    /** Wearer pose for an attached arm: body yaw roots it, head yaw aims the slap through the view. */
    record Frame(float bodyYaw, float headYaw, float scale, boolean crouching, boolean firstPerson) {}

    private HematicRiposteGeometry() {}

    static boolean handles(Form form) {
        return form == Form.RIPOSTE || form == Form.RIPOSTE_STRIKE;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, Frame frame) {
        if (alpha <= 0) return;
        if (packet.form() == Form.RIPOSTE) arm(packet, p, v, camera, right, up, time, age, alpha, frame);
        else strike(packet, p, v, camera, right, up, time, age, alpha);
    }

    /** The hand's position at {@code age} ticks into the swing, relative to the wearer's feet. */
    static Vec3 hand(int side, int tilt, double age, Frame frame) {
        double s = frame.scale();
        double shoulder = (frame.crouching() ? 1.12 : 1.38) * s;
        double endHeight = (1.18 + (tilt - 2) * 0.14 - (frame.firstPerson() ? 0.1 : 0)) * s;
        double theta, reach;
        if (age < HematicRiposteRules.EMERGE_END) {
            double e = smooth(age / HematicRiposteRules.EMERGE_END);
            theta = side * BEHIND;
            reach = 0.3 + 0.9 * e;
        } else if (age < HematicRiposteRules.SWEEP_END) {
            double e = easeInOut((age - HematicRiposteRules.EMERGE_END)
                    / (HematicRiposteRules.SWEEP_END - HematicRiposteRules.EMERGE_END));
            theta = side * BEHIND * (1 - e);
            reach = 1.2 + 1.0 * e;
        } else if (age < HematicRiposteRules.STRIKE_END) {
            double t = (age - HematicRiposteRules.SWEEP_END)
                    / (HematicRiposteRules.STRIKE_END - HematicRiposteRules.SWEEP_END);
            theta = -side * OVERSHOOT * Math.sin(t * Math.PI / 2);
            reach = 2.2 + 0.35 * Math.sin(t * Math.PI);
        } else {
            double d = smooth((age - HematicRiposteRules.STRIKE_END)
                    / (HematicRiposteRules.VISUAL_TICKS - HematicRiposteRules.STRIKE_END));
            theta = -side * OVERSHOOT;
            reach = 2.2 - 0.6 * d;
        }
        double raised = Math.min(1, Math.abs(theta) / BEHIND);
        double height = endHeight + (shoulder + 0.6 * s - endHeight) * raised;
        return UP.scale(height).add(sweep(frame.headYaw(), theta).scale(reach * s));
    }

    private static void arm(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right,
            Vec3 up, double time, float age, float alpha, Frame frame) {
        int side = HematicRiposteRules.side(packet.count()), tilt = HematicRiposteRules.tilt(packet.count());
        int seed = Math.floorMod(packet.entityId(), 113);
        double s = frame.scale();
        Vec3 f = vec(HematicRiposteRules.forward(frame.bodyYaw())), r = vec(HematicRiposteRules.right(frame.bodyYaw()));
        Vec3 root = UP.scale((frame.crouching() ? 1.12 : 1.38) * s).add(f.scale(-0.24 * s)).add(r.scale(side * 0.10 * s));
        Vec3 hand = hand(side, tilt, age, frame);
        Vec3 previous = hand(side, tilt, Math.max(0, age - 1), frame);
        Vec3 motion = hand.subtract(previous);
        Vec3 reachDir = horizontal(hand.subtract(root), f);
        Vec3 over = root.add(f.scale(-0.35 * s)).add(UP.scale(0.55 * s)).add(r.scale(side * 0.25 * s));
        Vec3 elbow = hand.subtract(reachDir.scale(0.7 * s)).add(UP.scale(0.25 * s));

        float grow = (float) smooth(age / HematicRiposteRules.EMERGE_END);
        float dissolve = (float) smooth((age - HematicRiposteRules.STRIKE_END)
                / (double) (HematicRiposteRules.VISUAL_TICKS - HematicRiposteRules.STRIKE_END));
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        float body = alpha * (1 - dissolve * 0.85f);

        List<Vec3> spine = new ArrayList<>(SPINE_SAMPLES + 1);
        List<Double> stations = new ArrayList<>(SPINE_SAMPLES + 1);
        for (int i = 0; i <= SPINE_SAMPLES; i++) {
            double t = grow * i / (double) SPINE_SAMPLES;
            // In first person the root sits at the screen edge; keep the near limb from walling off the view.
            if (frame.firstPerson() && t < 0.4) continue;
            stations.add(t);
            spine.add(bezier(root, over, elbow, hand, t).add(UP.scale(dissolve * 0.3 * t)));
        }
        if (spine.size() < 2) return;

        // 1. Shell: a broad translucent sheath with flame-like cards trailing against the sweep.
        ribbonPath(p, v, spine, camera, 0.30 * s * (1 + dissolve * 0.4), RIBBON, seed, TINT, body * 0.28f);
        Vec3 trail = motion.lengthSqr() < 1e-6 ? Vec3.ZERO : motion.normalize().scale(-1);
        for (int i = 0; i < spine.size(); i++) {
            double t = stations.get(i);
            double flicker = 0.85 + 0.25 * Math.sin(time * 0.9 + i * 1.7 + seed);
            double width = (0.42 - 0.20 * t) * s * flicker * (1 + dissolve * 0.6);
            Vec3 at = spine.get(i).add(trail.scale((0.12 + 0.18 * t) * s)).add(UP.scale(dissolve * 0.25 * t));
            card(p, v, at, right, up, width, width * 1.25, WISP, seed + i, TINT, body * 0.30f);
        }

        // 2. Sinew: strands twisting about the spine, thick at the root and pulsing like a heartbeat.
        float sinew = body * (0.75f + 0.25f * pulse);
        for (int strand = 0; strand < 3; strand++) {
            List<Vec3> points = new ArrayList<>(spine.size());
            for (int i = 0; i < spine.size(); i++) {
                double t = stations.get(i);
                Vec3 tangent = spine.get(Math.min(i + 1, spine.size() - 1)).subtract(spine.get(Math.max(0, i - 1)));
                Vec3[] basis = basis(tangent);
                double angle = t * 5 + strand * Math.PI * 2 / 3 + time * 0.15;
                double radius = (0.11 - 0.075 * t) * s * (1 + dissolve * 2.5);
                points.add(spine.get(i).add(basis[0].scale(Math.cos(angle) * radius)).add(basis[1].scale(Math.sin(angle) * radius)));
            }
            int third = Math.max(2, points.size() / 3);
            ribbonPath(p, v, points.subList(0, Math.min(points.size(), third + 1)), camera, 0.07 * s, RIBBON, seed + strand, TINT, sinew);
            ribbonPath(p, v, points.subList(Math.min(points.size() - 1, third), Math.min(points.size(), 2 * third + 1)),
                    camera, 0.05 * s, RIBBON, seed + strand + 3, TINT, sinew);
            ribbonPath(p, v, points.subList(Math.min(points.size() - 1, 2 * third), points.size()),
                    camera, 0.035 * s, RIBBON, seed + strand + 6, TINT, sinew);
        }

        // 3. Knots: the erupting root, the elbow and the wrist.
        if (!frame.firstPerson()) {
            double burst = age < HematicRiposteRules.EMERGE_END + 1 ? 1.4 - 0.4 * grow : 1;
            card(p, v, root, right, up, 0.26 * s * burst, 0.26 * s * burst, FOCUS, seed, TINT, body * (0.6f + 0.4f * pulse));
        }
        if (grow >= 1) {
            card(p, v, bezier(root, over, elbow, hand, 0.55), right, up, 0.12 * s, 0.12 * s, FOCUS, seed + 1, TINT, body * 0.8f);
            card(p, v, bezier(root, over, elbow, hand, 0.93), right, up, 0.14 * s, 0.14 * s, FOCUS, seed + 2, TINT, body * 0.85f);
        }

        // 4. Hand: a palm and four long claws fanned across the sweep, curling inward through the slap.
        Vec3 swing = motion.lengthSqr() < 1e-6 ? r.scale(-side) : motion.normalize();
        // Fingers spread vertically, so the open palm leads the horizontal slap.
        Vec3 fan = basis(reachDir)[1].dot(UP) < 0 ? basis(reachDir)[1].scale(-1) : basis(reachDir)[1];
        double curl = smooth((age - HematicRiposteRules.SWEEP_END) / 2.0) * (1 - dissolve * 0.5);
        card(p, v, hand, right, up, 0.24 * s * grow, 0.20 * s * grow, FOCUS, seed + 3, TINT, body);
        for (int claw = 0; claw < 4; claw++) {
            double spread = claw - 1.5;
            Vec3 base = hand.add(fan.scale(spread * 0.09 * s));
            Vec3 outward = reachDir.add(fan.scale(spread * 0.18)).normalize();
            double length = (0.62 + 0.08 * (1.5 - Math.abs(spread))) * s * grow;
            Vec3 mid = base.add(outward.scale(length * 0.55)).add(swing.scale(curl * 0.12 * s));
            Vec3 tip = base.add(outward.scale(length * (1 - curl * 0.2))).add(swing.scale(curl * 0.38 * s))
                    .add(UP.scale(-curl * 0.10 * s));
            ribbonPath(p, v, List.of(base, mid), camera, 0.055 * s, SLASH, seed + 10 + claw, TINT, body);
            ribbonPath(p, v, List.of(mid, tip), camera, 0.035 * s, SLASH, seed + 14 + claw, CLOT, body);
            ribbonPath(p, v, List.of(base, mid.lerp(tip, 0.5)), camera, 0.014 * s, RIBBON, seed + 18 + claw, CORE, body * 0.9f);
        }

        // 5. Swipe trail and, at the slap, three parallel claw streaks.
        if (age >= HematicRiposteRules.EMERGE_END && age < HematicRiposteRules.STRIKE_END + 2) {
            List<Vec3> path = new ArrayList<>(9);
            for (int i = 8; i >= 0; i--) path.add(hand(side, tilt, Math.max(0, age - i * 0.5), frame));
            float fade = age > HematicRiposteRules.STRIKE_END ? 1 - (age - HematicRiposteRules.STRIKE_END) / 2f : 1;
            ribbonPath(p, v, path.subList(0, 5), camera, 0.10 * s, SLASH, seed + 30, TINT, alpha * 0.35f * fade);
            ribbonPath(p, v, path.subList(4, 9), camera, 0.16 * s, SLASH, seed + 31, TINT, alpha * 0.6f * fade);
        }
        if (age >= HematicRiposteRules.SWEEP_END && age < HematicRiposteRules.STRIKE_END + 1) {
            float streak = alpha * (1 - (age - HematicRiposteRules.SWEEP_END) / 3f);
            for (double offset : slashOffsets())
                stream(p, v, hand.subtract(swing.scale(0.6 * s)).add(UP.scale(offset * s)),
                        hand.add(swing.scale(0.45 * s)).add(UP.scale(offset * s)),
                        camera, 0.05 * s, 0.06, time * 0.2, SLASH, seed + 40, TINT, streak);
        }
    }

    /** Success flash at the point of contact: count 1 is a stunned melee attacker, 2 a swatted projectile. */
    private static void strike(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha) {
        Vec3 d = packet.to().subtract(packet.from());
        d = d.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : d.normalize();
        Vec3[] basis = basis(d);
        double r = Math.max(0.3, packet.radius());
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        float fade = alpha * Math.max(0, 1 - age / 8f);
        double flash = r * Math.max(0.1, 0.45 - age * 0.12);
        card(p, v, Vec3.ZERO, right, up, flash, flash, FOCUS, seed, TINT, fade);
        if (packet.count() == 2) {
            double length = Math.min(1.4, 0.3 + age * 0.35) * r;
            stream(p, v, d.scale(-0.2), d.scale(length), camera, 0.07, 0.05, time * 0.1, SLASH, seed + 1, TINT, fade);
            for (int i = 0; i < 4; i++)
                card(p, v, d.scale(0.25 * i * r).add(basis[0].scale(Math.sin(i * 2.1) * 0.12)), right, up,
                        0.05, 0.05, FOCUS, seed + 2 + i, TINT, fade * 0.8f);
            return;
        }
        double reach = (0.5 + age * 0.06) * r;
        for (int i = 0; i < 7; i++) {
            double a = i * 2.39996 + seed;
            Vec3 ray = d.add(basis[0].scale(Math.cos(a) * 0.7)).add(basis[1].scale(Math.sin(a) * 0.7)).normalize();
            stream(p, v, ray.scale(0.15 * r), ray.scale(reach), camera, 0.045, 0.03, time * 0.05 + i, SLASH, seed + i, TINT, fade);
        }
        double cross = Math.min(1, 0.4 + age * 0.2) * 0.5 * r;
        for (double offset : slashOffsets())
            stream(p, v, basis[0].scale(-cross).add(basis[1].scale(offset * r)),
                    basis[0].scale(cross).add(basis[1].scale(offset * r)), camera, 0.05, 0.05,
                    time * 0.1, SLASH, seed + 9, TINT, fade);
    }

    private static Vec3 sweep(float headYaw, double theta) {
        Vec3 f = vec(HematicRiposteRules.forward(headYaw)), r = vec(HematicRiposteRules.right(headYaw));
        return f.scale(Math.cos(theta)).add(r.scale(Math.sin(theta)));
    }

    private static Vec3 horizontal(Vec3 v, Vec3 fallback) {
        Vec3 flat = new Vec3(v.x, 0, v.z);
        return flat.lengthSqr() < 1e-6 ? fallback : flat.normalize();
    }

    /** Two unit vectors perpendicular to {@code axis} and to each other. */
    private static Vec3[] basis(Vec3 axis) {
        Vec3 a = axis.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : axis.normalize();
        Vec3 reference = Math.abs(a.y) > 0.9 ? new Vec3(1, 0, 0) : UP;
        Vec3 first = a.cross(reference).normalize();
        return new Vec3[] { first, first.cross(a).normalize() };
    }

    private static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double t) {
        double u = 1 - t;
        return a.scale(u * u * u).add(b.scale(3 * u * u * t)).add(c.scale(3 * u * t * t)).add(d.scale(t * t * t));
    }

    private static Vec3 vec(double[] xyz) {
        return new Vec3(xyz[0], xyz[1], xyz[2]);
    }

    private static double smooth(double t) {
        double c = Math.max(0, Math.min(1, t));
        return c * c * (3 - 2 * c);
    }

    private static double easeInOut(double t) {
        double c = Math.max(0, Math.min(1, t));
        return c < 0.5 ? 4 * c * c * c : 1 - Math.pow(-2 * c + 2, 3) / 2;
    }
}
