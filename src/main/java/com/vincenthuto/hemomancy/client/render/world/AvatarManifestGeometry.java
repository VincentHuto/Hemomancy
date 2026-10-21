package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The blood avatar's manifestation, layered over the avatar layer's own emergence. Blood wells from the body's exits
 * into a pool and climbs the body in rings; sinew then lashes out to the parts this stage grows (a spine always, arms,
 * a ribcage over the chest, coils round the legs, a hood over the head) and the avatar breaks the surface with a
 * splash. Dismissed, the shell slides down the body in drip curtains into a pool that drains back up the legs. Points
 * are world-axis offsets from the caster's feet, sized to the avatar.
 */
final class AvatarManifestGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final Vec3 UP = CasterFrame.UP;

    private AvatarManifestGeometry() {}

    static boolean handles(Form form) {
        return form == Form.AVATAR_MANIFEST;
    }

    /** The avatar's size over the player's: only the complete avatar is larger. */
    static double avatarScale(int stage) {
        return stage >= 4 ? 2 : 1;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, CasterFrame frame) {
        if (alpha <= 0) return;
        int stage = AvatarManifestRules.stage(packet.count());
        int seed = Math.floorMod(packet.entityId(), 113);
        double big = frame.scale() * avatarScale(stage);
        // Ground pools and splashes grow far less than the body: a doubled pool swallows the whole scene.
        double ground = frame.scale() * (1 + 0.3 * (avatarScale(stage) - 1));
        // In first person the body is below and around the camera: keep to the lower body, faintly.
        float a = alpha * (frame.firstPerson() ? 0.5f : 1f);
        double ceiling = frame.firstPerson() ? 1.0 * frame.scale() : Double.MAX_VALUE;
        if (AvatarManifestRules.dismissing(packet.count())) dismiss(p, v, camera, right, up, time, age, a, seed, big, ground, ceiling, frame);
        else manifest(p, v, camera, right, up, time, age, a, seed, big, ground, stage, ceiling, frame);
    }

    private static void manifest(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float age,
            float a, int seed, double big, double ground, int stage, double ceiling, CasterFrame frame) {
        Vec3 f = frame.forward(), r = frame.right();
        double height = 1.85 * big;
        double rise = AvatarManifestRules.rise(age), grow = AvatarManifestRules.grow(age);
        double emerge = AvatarManifestRules.emerge(age), fray = AvatarManifestRules.fray(age);
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        float body = a * (float) (1 - fray * 0.9);
        double drift = fray * 0.35 * big;

        // 1. Wells: blood bursts from the body's exits and pools at the feet.
        float well = body * (float) (1 - rise * 0.6);
        for (Vec3 exit : new Vec3[] { r.scale(0.25 * big).add(UP.scale(1.0 * big)), r.scale(-0.25 * big).add(UP.scale(1.0 * big)),
                f.scale(0.14 * big).add(UP.scale(0.85 * big)), f.scale(-0.14 * big).add(UP.scale(1.1 * big)) }) {
            if (exit.y > ceiling) continue;
            double burst = age < 3 ? 1.4 - 0.13 * age : 1;
            card(p, v, exit, right, up, 0.1 * big * burst, 0.1 * big * burst, FOCUS, seed + (int) (exit.y * 10), TINT, well);
        }
        ground(p, v, new Vec3(0, 0.05, 0), (0.3 + 0.5 * rise) * ground * (1 - fray * 0.5), POOL, seed + 1, TINT, body * 0.7f);

        // 2. Rings climb the body as the blood rises over it.
        int rings = 4;
        for (int ring = 0; ring < rings; ring++) {
            double h = AvatarManifestRules.ringHeight(age, ring, rings);
            if (h < 0) continue;
            double y = 0.06 * big + (height - 0.1 * big) * h;
            if (y > ceiling) continue;
            double radius = (0.42 - 0.12 * Math.abs(h - 0.55)) * big;
            List<Vec3> loop = new ArrayList<>(17);
            for (int i = 0; i <= 16; i++) {
                double angle = i * Math.PI / 8 + seed + ring;
                loop.add(new Vec3(Math.cos(angle) * radius, y + drift, Math.sin(angle) * radius));
            }
            float fade = body * (float) (h >= 1 ? Math.max(0, 1 - (age - AvatarManifestRules.RISE_END) / 4) : 1);
            ribbonPath(p, v, loop, camera, 0.08 * ground, RIBBON, seed + 10 + ring, TINT, fade * 0.28f);
            ribbonPath(p, v, loop, camera, 0.03 * ground, RIBBON, seed + 14 + ring, ring == 0 ? CLOT : TINT, fade);
            ribbonPath(p, v, loop, camera, 0.01 * big, RIBBON, seed + 18 + ring, CORE, fade * 0.8f);
        }

        // 3. Grow: sinew lashes out to the parts this stage grows.
        if (grow > 0) {
            float sinew = body * (0.8f + 0.2f * pulse);
            Vec3 hips = UP.scale(0.8 * big).subtract(f.scale(0.12 * big));
            Vec3 nape = UP.scale(1.5 * big).subtract(f.scale(0.14 * big));
            limb(p, v, camera, List.of(hips, hips.lerp(nape, 0.5).subtract(f.scale(0.06 * big)), nape), grow, 0.06 * big, seed + 30,
                    time, sinew, drift, ceiling);
            if (stage >= 1) for (int side = -1; side <= 1; side += 2) {
                Vec3 shoulder = UP.scale(1.42 * big).add(r.scale(side * 0.36 * big));
                Vec3 elbow = UP.scale(1.12 * big).add(r.scale(side * 0.48 * big)).subtract(f.scale(0.05 * big));
                Vec3 hand = UP.scale(0.82 * big).add(r.scale(side * 0.5 * big)).add(f.scale(0.08 * big));
                limb(p, v, camera, List.of(nape, shoulder, elbow, hand), grow, 0.05 * big, seed + 40 + side, time, sinew, drift, ceiling);
            }
            if (stage >= 2) for (int rib = 0; rib < 4; rib++) {
                double y = (1.38 - rib * 0.12) * big;
                List<Vec3> arc = new ArrayList<>(9);
                for (int i = 0; i <= 8; i++) {
                    double angle = Math.PI * (i / 8.0) - Math.PI / 2;
                    double reach = (0.3 - rib * 0.02) * big;
                    arc.add(UP.scale(y - 0.06 * big * Math.cos(angle)).add(f.scale(Math.cos(angle) * reach * 0.8))
                            .add(r.scale(Math.sin(angle) * reach)).subtract(f.scale(0.12 * big)));
                }
                limb(p, v, camera, arc, grow, 0.03 * big, seed + 50 + rib, time, sinew, drift, ceiling);
            }
            if (stage >= 3) for (int side = -1; side <= 1; side += 2) {
                List<Vec3> coil = new ArrayList<>(13);
                for (int i = 0; i <= 12; i++) {
                    double t = i / 12.0, angle = t * Math.PI * 4 + side + seed;
                    Vec3 axis = UP.scale((0.78 - 0.7 * t) * big).add(r.scale(side * 0.13 * big));
                    coil.add(axis.add(r.scale(Math.cos(angle) * 0.11 * big)).add(f.scale(Math.sin(angle) * 0.11 * big)));
                }
                limb(p, v, camera, coil, grow, 0.035 * big, seed + 60 + side, time, sinew, drift, ceiling);
            }
            if (stage >= 4) for (int band = -1; band <= 1; band++) {
                Vec3 crown = UP.scale(1.98 * big).add(r.scale(band * 0.12 * big));
                List<Vec3> hood = List.of(nape, UP.scale(1.75 * big).add(r.scale(band * 0.2 * big)).subtract(f.scale(0.16 * big)),
                        crown, crown.add(f.scale(0.22 * big)).subtract(UP.scale(0.08 * big)));
                limb(p, v, camera, hood, grow, 0.04 * big, seed + 70 + band, time, sinew, drift, ceiling);
            }
        }

        // 4. Emerge: the avatar breaks the surface with a splash ringing the ground and blood flung up off the body.
        if (emerge > 0) {
            ground(p, v, new Vec3(0, 0.06, 0), (0.6 + 0.9 * (age - AvatarManifestRules.GROW_END) / 5.0) * ground, RIPPLE, seed + 80,
                    TINT, a * (float) emerge * 0.8f);
            for (int i = 0; i < 7; i++) {
                double angle = i * 2.39996 + seed;
                double out = (age - AvatarManifestRules.GROW_END) * 0.08 * big;
                Vec3 at = new Vec3(Math.cos(angle) * (0.35 * big + out), (0.5 + 0.18 * i) * big + out * 0.5, Math.sin(angle) * (0.35 * big + out));
                if (at.y > ceiling) continue;
                double w = 0.14 * big * (0.85 + 0.25 * Math.sin(time * 0.9 + i * 1.7 + seed));
                card(p, v, at, right, up, w, w * 1.3, WISP, seed + 81 + i, TINT, a * (float) emerge * 0.35f);
                card(p, v, at, right, up, 0.03 * big, 0.045 * big, FOCUS, seed + 90 + i, CLOT, a * (float) emerge);
            }
            if (!frame.firstPerson()) card(p, v, UP.scale(1.15 * big), right, up, 0.35 * big * emerge, 0.35 * big * emerge, FOCUS,
                    seed + 99, CORE, a * (float) emerge * 0.6f);
        }
    }

    private static void dismiss(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float age,
            float a, int seed, double big, double ground, double ceiling, CasterFrame frame) {
        Vec3 r = frame.right(), f = frame.forward();
        double height = 1.85 * big;
        double melt = AvatarManifestRules.melt(age), drain = AvatarManifestRules.drain(age);
        float body = a * (float) (1 - drain * 0.9);

        // 1. The shell's lower edge slides down the body, hanging curtains of drips beneath it.
        double band = 0.06 * big + (height - 0.06 * big) * melt;
        if (melt > 0.02) {
            double radius = 0.42 * big;
            List<Vec3> loop = new ArrayList<>(17);
            for (int i = 0; i <= 16; i++) {
                double angle = i * Math.PI / 8 + seed;
                loop.add(new Vec3(Math.cos(angle) * radius, Math.min(band, ceiling), Math.sin(angle) * radius));
            }
            ribbonPath(p, v, loop, camera, 0.14 * big, RIBBON, seed, TINT, body * 0.3f);
            ribbonPath(p, v, loop, camera, 0.04 * big, RIBBON, seed + 1, CLOT, body);
            for (int i = 0; i < 10; i++) {
                double angle = i * Math.PI / 5 + seed + 0.2;
                double hang = (0.15 + 0.25 * (0.5 + 0.5 * Math.sin(i * 1.7 + seed))) * big * (0.4 + 0.6 * (1 - melt));
                Vec3 top = new Vec3(Math.cos(angle) * radius, Math.min(band, ceiling), Math.sin(angle) * radius);
                Vec3 bottom = top.subtract(UP.scale(Math.min(hang, top.y - 0.05)));
                ribbonPath(p, v, List.of(top, top.lerp(bottom, 0.6), bottom), camera, 0.035 * big, 0.012 * big, RIBBON, seed + 10 + i,
                        TINT, body);
                card(p, v, bottom, right, up, 0.025 * big, 0.04 * big, FOCUS, seed + 20 + i, CLOT, body);
            }
        }

        // 2. The pool spreads as the shell comes down, then shrinks as it drains.
        double spread = (0.3 + 0.7 * (1 - melt)) * (1 - drain * 0.8);
        ground(p, v, new Vec3(0, 0.05, 0), spread * 0.75 * ground, POOL, seed + 30, TINT, a * (float) (1 - drain) * 0.8f);

        // 3. Drain: the blood climbs back into the legs and is gone.
        if (drain > 0 && drain < 1)
            for (int side = -1; side <= 1; side += 2) {
                Vec3 ankle = UP.scale(0.08 * big).add(r.scale(side * 0.13 * big));
                Vec3 hip = UP.scale(0.78 * big).add(r.scale(side * 0.12 * big)).add(f.scale(0.02 * big));
                Vec3 head = ankle.lerp(hip, drain);
                ribbonPath(p, v, List.of(ankle, ankle.lerp(head, 0.5), head), camera, 0.03 * big, 0.012 * big, RIBBON, seed + 40 + side,
                        TINT, a * (float) (1 - drain));
                card(p, v, head, right, up, 0.04 * big, 0.04 * big, FOCUS, seed + 42 + side, CORE, a * (float) (1 - drain));
            }
    }

    /** Sinew lashed out along {@code spine} to fraction {@code grow}: shell, body and pale core, clotted at the tip. */
    private static void limb(PoseStack p, VertexConsumer v, Vec3 camera, List<Vec3> spine, double grow, double width, int seed,
            double time, float alpha, double drift, double ceiling) {
        List<Vec3> path = new ArrayList<>(13);
        double reach = Math.min(1.08, grow);
        for (int i = 0; i <= 12; i++) {
            double t = reach * i / 12.0;
            Vec3 at = sample(spine, Math.min(1, t));
            if (t > 1) at = at.add(sample(spine, 1).subtract(sample(spine, 0.95)).scale((t - 1) * 20));
            at = at.add(UP.scale(drift * t));
            if (at.y > ceiling) break;
            path.add(at);
        }
        if (path.size() < 2) return;
        double flicker = 0.85 + 0.2 * Math.sin(time * 0.9 + seed * 1.7);
        ribbonPath(p, v, path, camera, width * 2.4 * flicker, width * 1.2, WISP, seed, TINT, alpha * 0.25f);
        ribbonPath(p, v, path, camera, width, width * 0.45, RIBBON, seed + 1, TINT, alpha);
        ribbonPath(p, v, path.subList(path.size() * 2 / 3, path.size()), camera, width * 0.6, width * 0.25, RIBBON, seed + 2, CLOT,
                alpha);
        ribbonPath(p, v, path, camera, width * 0.25, width * 0.1, RIBBON, seed + 3, CORE, alpha * 0.8f);
    }

    /** A point {@code t} of the way along a polyline, by segment count. */
    private static Vec3 sample(List<Vec3> points, double t) {
        double scaled = t * (points.size() - 1);
        int i = (int) Math.min(points.size() - 2, Math.floor(scaled));
        return points.get(i).lerp(points.get(i + 1), scaled - i);
    }
}
