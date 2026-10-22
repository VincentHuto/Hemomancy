package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.basis;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Deadly Gaze: intent carried through the eyes before the hand moves. While charging, an eye of nerve opens along the
 * aim, fed by optic nerves sheathed in ink, its slit pupil narrowing as the sightline reaches out. A hit seizes the
 * target in nerve tendrils knotted above its head and tethered back to the caster's eyes; a full charge slams it into
 * a bursting pool of ink. Nerves draw on the Ductilis material, ink on Umbra.
 */
final class DeadlyGazeGeometry {
    static final int NERVE = 0xFFE8A0, INK = 0xFFFFFF;
    /** Ductilis shader shapes. */
    private static final int LINE = 0, GLOW = 1, CRACKLE = 2, SPARK = 3;
    /** Umbra shader shapes: ink ribbon, liquid pool, violet-cored cut; anything else is smoke. */
    private static final int INK_LINE = RIBBON, INK_POOL = POOL, INK_CUT = SLASH, SMOKE = WISP;
    static final int GRIP_TICKS = 14, SEIZE_END = 3, HOLD_END = 12, SLAM_TICKS = 16;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private DeadlyGazeGeometry() {}

    static boolean handles(Form form) {
        return form == Form.GAZE_CHARGE || form == Form.GAZE_GRIP || form == Form.GAZE_SLAM;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer nerve, VertexConsumer ink, Vec3 camera,
            Vec3 origin, Vec3 right, Vec3 up, double time, float age, float alpha, LivingEntity body, boolean firstPerson) {
        if (alpha <= 0) return;
        switch (packet.form()) {
            case GAZE_CHARGE -> charge(packet, p, nerve, ink, camera, right, up, time, alpha, firstPerson);
            case GAZE_GRIP -> grip(packet, p, nerve, ink, camera, origin, right, up, time, age, alpha, body);
            case GAZE_SLAM -> slam(packet, p, nerve, ink, camera, right, up, time, age, alpha);
            default -> throw new IllegalArgumentException("Not a gaze form: " + packet.form());
        }
    }

    /** How far the eye's lids have parted at a charge {@code progress}. */
    static double opening(double progress) {
        return HemoEasing.smoothstep(Mth.clamp(progress, 0, 1) * 1.25);
    }

    private static void charge(ManipulationVisualPacket packet, PoseStack p, VertexConsumer nerve, VertexConsumer ink,
            Vec3 camera, Vec3 right, Vec3 up, double time, float alpha, boolean firstPerson) {
        Vec3 end = packet.to().subtract(packet.from());
        Vec3 aim = end.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : end.normalize();
        Vec3 focus = ChargeVisualGeometry.focus(Form.GAZE_CHARGE, aim);
        double progress = Mth.clamp(packet.radius(), 0, 1), open = opening(progress);
        int seed = Math.floorMod(packet.entityId(), 113);
        boolean ready = progress >= .999;
        float pulse = ready ? (float) Math.pow(Math.max(0, Math.sin(time * .6)), 4) : 0;
        // The caster sees the eye from behind its own; keep it lighter so the target stays visible.
        float view = firstPerson ? .6f : 1;

        // 1. Optic nerves: from each eye socket, wound in an ink sheath, into the back of the eye.
        for (int side = -1; side <= 1; side += 2) {
            Vec3 socket = right.scale(side * .085).add(up.scale(-.02));
            Vec3 back = focus.subtract(aim.scale(.05));
            Vec3 c1 = socket.add(aim.scale(.4)).add(right.scale(side * .12));
            Vec3 c2 = back.subtract(aim.scale(.35)).add(right.scale(side * .05));
            List<Vec3> path = new ArrayList<>(13);
            for (int i = 0; i <= 12; i++) {
                double t = i / 12.0;
                if (firstPerson && t < .35) continue;
                path.add(bezier(socket, c1, c2, back, t));
            }
            ribbonPath(p, ink, path, camera, .028, .016, INK_LINE, seed + side, INK, alpha * .35f * view);
            nerveLine(p, nerve, path, camera, .012, .006, LINE, seed + 3 + side, NERVE, alpha * view);
        }

        // 2. Eye: nerve lids parting around an iris, an ink slit pupil, a gold glow within.
        double w = .2 * (.6 + .4 * open), h = Math.max(.008, .085 * open);
        card(p, nerve, focus, right, up, w * .9, h * 1.1 + .02, GLOW, seed + 5, NERVE, alpha * .5f * (float) open * view);
        lid(p, nerve, focus, right, up, camera, w, h, seed + 6, alpha * view);
        lid(p, nerve, focus, right, up, camera, w, -h * .85, seed + 7, alpha * view);
        if (open > .25) {
            double iris = Math.min(h * .95, w * .45);
            List<Vec3> ring = new ArrayList<>(17);
            for (int i = 0; i <= 16; i++) {
                double a = i * Math.PI * 2 / 16 + time * .02;
                ring.add(focus.add(right.scale(Math.cos(a) * iris)).add(up.scale(Math.sin(a) * iris)));
            }
            nerveLine(p, nerve, ring, camera, .006, .006, LINE, seed + 8, NERVE, alpha * (float) open * view);
            // The slit contracts as the gaze locks on.
            double slit = iris * (.45 - .25 * progress);
            card(p, ink, focus.subtract(aim.scale(.01)), right, up, slit, iris * .9, INK_POOL, seed + 9, INK, alpha * (float) open);
        }

        // 3. Sightline: a faint nerve filament reaching along the aim, current running down it.
        if (progress > .05) {
            Vec3 start = focus.add(aim.scale(.08));
            ribbonPath(p, ink, List.of(start, end), camera, .02, .004, INK_LINE, seed + 10, INK, alpha * .2f * (float) progress);
            nerveLine(p, nerve, List.of(start, end), camera, .007, .002, LINE, seed + 11, NERVE, alpha * .45f * (float) progress);
        }

        // 4. Locked: the pupil flares and current crackles round the lids.
        if (ready) {
            card(p, nerve, focus, right, up, .05 + .03 * pulse, .05 + .03 * pulse, SPARK, seed + 12, NERVE, alpha * (.6f + .4f * pulse));
            card(p, nerve, focus, right, up, w * 1.5, w * 1.1, CRACKLE, seed + 13, NERVE, alpha * .5f * pulse * view);
        }
    }

    /** One lid: an arc from corner to corner, thin at the corners and thickest at the crest. */
    private static void lid(PoseStack p, VertexConsumer v, Vec3 focus, Vec3 right, Vec3 up, Vec3 camera, double w, double h,
            int seed, float alpha) {
        List<Vec3> first = new ArrayList<>(7), second = new ArrayList<>(7);
        for (int i = 0; i <= 12; i++) {
            double t = i / 12.0;
            Vec3 at = focus.add(right.scale((t * 2 - 1) * w)).add(up.scale(h * Math.sin(t * Math.PI)));
            if (i <= 6) first.add(at);
            if (i >= 6) second.add(at);
        }
        nerveLine(p, v, first, camera, .004, .014, LINE, seed, NERVE, alpha);
        nerveLine(p, v, second, camera, .014, .004, LINE, seed + 1, NERVE, alpha);
    }

    /**
     * Point {@code t} along grip tendril {@code strand} of {@code count}: it starts on a ring at the feet and winds up
     * the body to the knot above the head, tightening as the hold sets in.
     */
    static Vec3 tendril(int strand, int count, double t, double height, double width, double tighten, double time) {
        double a = strand * Math.PI * 2 / count + t * 2.4 + time * .03;
        double radius = width * (.85 - .3 * tighten) * (1 - t * .75);
        return new Vec3(Math.cos(a) * radius, t * height * 1.15, Math.sin(a) * radius);
    }

    private static void grip(ManipulationVisualPacket packet, PoseStack p, VertexConsumer nerve, VertexConsumer ink, Vec3 camera,
            Vec3 origin, Vec3 right, Vec3 up, double time, float age, float alpha, LivingEntity body) {
        double height = body == null ? 1.8 : body.getBbHeight(), width = body == null ? .6 : body.getBbWidth();
        boolean charged = packet.count() >= 2;
        int seed = Math.floorMod(packet.entityId(), 113);
        double seize = HemoEasing.smoothstep((double) (age / SEIZE_END)), tighten = HemoEasing.smoothstep((age - SEIZE_END) / 4.0);
        float release = 1 - (float) HemoEasing.smoothstep((age - HOLD_END) / (GRIP_TICKS - HOLD_END + 2.0));
        float a = alpha * release;
        if (a <= .002) return;
        int count = charged ? 6 : 5;
        Vec3 knot = new Vec3(0, height * 1.15, 0);

        // 1. Tendrils: they lash up from the feet, wind round the body and tighten into the knot.
        for (int strand = 0; strand < count; strand++) {
            List<Vec3> path = new ArrayList<>(13);
            for (int i = 0; i <= 12; i++) path.add(tendril(strand, count, seize * i / 12.0, height, width, tighten, time));
            ribbonPath(p, ink, path, camera, .045, .02, INK_LINE, seed + strand, INK, a * .3f);
            nerveLine(p, nerve, path, camera, charged ? .022 : .016, .008, LINE, seed + 10 + strand, NERVE, a);
        }

        // 2. Knot: the grasp itself, pulsing above the head.
        if (seize > .6) {
            float beat = (float) Math.pow(Math.max(0, Math.sin(time * .5 + seed)), 4);
            card(p, nerve, knot, right, up, .09 + .04 * beat, .09 + .04 * beat, SPARK, seed + 20, NERVE, a);
            card(p, nerve, knot, right, up, .32, .26, GLOW, seed + 21, NERVE, a * .6f);
        }

        // 3. Tether: a taut line from the knot back to the caster's eyes.
        Vec3 eye = packet.to().subtract(origin);
        if (seize > .3 && eye.subtract(knot).lengthSqr() > .25) {
            double reach = HemoEasing.smoothstep((age - 1) / 3.0);
            Vec3 tip = knot.lerp(eye, reach);
            ribbonPath(p, ink, List.of(knot, knot.lerp(tip, .5), tip), camera, .035, .012, INK_LINE, seed + 30, INK, a * .3f);
            nerveLine(p, nerve, List.of(knot, knot.lerp(tip, .5), tip), camera, .014, .004, LINE, seed + 31, NERVE, a * .7f);
        }

        // 4. Ink drips off the lifted body; a full charge crackles with current.
        for (int i = 0; i < 3; i++) {
            double cycle = (age * .08 + i * .33) % 1;
            double angle = i * 2.1 + seed;
            Vec3 at = new Vec3(Math.cos(angle) * width * .35, -cycle * .9, Math.sin(angle) * width * .35);
            card(p, ink, at, right, up, .09, .16, SMOKE, seed + 40 + i, INK, a * .5f * (float) (1 - cycle));
        }
        if (charged) {
            float flicker = (float) (.4 + .6 * Math.pow(Math.max(0, Math.sin(time * 1.3 + seed)), 2));
            card(p, nerve, new Vec3(0, height * .55, 0), right, up, width * 1.1, height * .6, CRACKLE, seed + 50, NERVE, a * .55f * flicker);
        }
    }

    private static void slam(ManipulationVisualPacket packet, PoseStack p, VertexConsumer nerve, VertexConsumer ink, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha) {
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        double r = Math.max(.6, packet.radius());
        double burst = HemoEasing.smoothstep(age / 6.0);
        float fade = alpha * (1 - (float) HemoEasing.smoothstep((age - 6) / (SLAM_TICKS - 6.0)));
        if (fade <= .002) return;

        // 1. Strike: the nerves snap straight down, with a flash where the body lands.
        if (age < 5) {
            float strike = alpha * (1 - age / 5f);
            for (int i = 0; i < 4; i++) {
                double a = i * Math.PI / 2 + seed;
                Vec3 top = new Vec3(Math.cos(a) * .25, 3.2, Math.sin(a) * .25);
                nerveLine(p, nerve, List.of(top, top.scale(.5), new Vec3(0, .05, 0)), camera, .004, .02, LINE, seed + i, NERVE, strike);
            }
            card(p, nerve, new Vec3(0, .3, 0), right, up, .35 * (1 - age / 5), .35 * (1 - age / 5), SPARK, seed + 5, NERVE, strike);
        }

        // 2. Ink bursts across the ground, split by violet-cored cracks, while current crawls over it.
        ground(p, ink, new Vec3(0, .04, 0), r * (.35 + .9 * burst), INK_POOL, seed + 6, INK, fade);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4 + Math.sin(i * 1.7 + seed) * .25;
            Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
            double reach = r * (.5 + .5 * Math.abs(Math.sin(i * 2.3 + seed))) * (.3 + .9 * burst);
            ribbonPath(p, ink, List.of(dir.scale(r * .15).add(0, .05, 0), dir.scale(reach).add(0, .05, 0)), camera,
                    .06, .01, INK_CUT, seed + 10 + i, INK, fade * .9f);
        }
        ground(p, nerve, new Vec3(0, .06, 0), r * (.4 + .7 * burst), CRACKLE, seed + 20, NERVE,
                fade * (float) (.35 + .65 * Math.pow(Math.max(0, Math.sin(time * 1.4 + seed)), 2)));

        // 3. Smoke lifts off the pool as it drains.
        for (int i = 0; i < 4; i++) {
            double a = i * 1.9 + seed;
            Vec3 at = new Vec3(Math.cos(a) * r * .5 * burst, .25 + age * .04, Math.sin(a) * r * .5 * burst);
            card(p, ink, at, right, up, .3 + age * .02, .4 + age * .025, SMOKE, seed + 30 + i, INK, fade * .55f);
        }
    }

    /**
     * The Ductilis filament fills only the middle of its quad with a bright core and a soft halo; widths here are the
     * visible nerve, spread to the quad that carries it.
     */
    private static void nerveLine(PoseStack p, VertexConsumer v, List<Vec3> path, Vec3 camera, double startWidth,
            double endWidth, int shape, int seed, int color, float alpha) {
        ribbonPath(p, v, path, camera, startWidth * 3, endWidth * 3, shape, seed, color, alpha);
    }

    private static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double t) {
        double u = 1 - t;
        return a.scale(u * u * u).add(b.scale(3 * u * u * t)).add(c.scale(3 * u * t * t)).add(d.scale(t * t * t));
    }
}
