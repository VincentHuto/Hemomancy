package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.smooth;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Funeral Bell: a hollow spectral bell condenses above the caster, hung from a rope of tendons, swaying harder as the
 * charge builds. It tolls once: the bell swings on its pivot against its clapper, a ring of sound bursts from the mouth
 * and three echoes roll out across a pool of rot to the bell's reach while souls rise from it. Mortem material.
 */
final class FuneralBellGeometry {
    private static final int TINT = 0xFFFFFF;
    /** Mortem shapes: tendon ribbon, wisp, ripple ring, bell shell, rot pool with hyphae, spore. */
    private static final int TENDON = RIBBON, SOUL = WISP, RING = RIPPLE, SHELL = 7, ROT_POOL = 8, SPORE = 10, CLAPPER = 4;
    static final int TOLL_TICKS = 40;
    static final double BELL_HALF_HEIGHT = .85, BELL_HALF_WIDTH = .95, ECHO_SPACING = 6, ECHO_LIFE = 14;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private FuneralBellGeometry() {}

    static boolean handles(Form form) {
        return form == Form.BELL || form == Form.BELL_CHARGE;
    }

    /** The bell's swing in radians after the toll: a hard strike that rings down. */
    static double swing(double age) {
        return .55 * Math.exp(-age / 9) * Math.sin(age * .7);
    }

    /** Radius of echo {@code ring} {@code age} ticks after the toll, or a negative value before it starts or after it fades. */
    static double echo(int ring, double age, double reach) {
        double t = (age - ring * ECHO_SPACING) / ECHO_LIFE;
        return t < 0 || t > 1 ? -1 : reach * smooth(t);
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha) {
        if (alpha <= 0) return;
        boolean charge = packet.form() == Form.BELL_CHARGE;
        int seed = Math.floorMod(packet.entityId() >= 0 ? packet.entityId() : packet.from().hashCode(), 113);
        double sway = seed * .37;
        Vec3 axis = new Vec3(Math.cos(sway), 0, Math.sin(sway));
        if (charge) {
            double progress = Mth.clamp(packet.radius(), .05, 1);
            Vec3 pivot = new Vec3(0, 1.0 + BELL_HALF_HEIGHT * 2, 0);
            double angle = Math.sin(time * .12) * .08 * progress;
            bell(p, v, pivot, axis, angle, progress, camera, time, seed, alpha);
            rope(p, v, pivot, camera, seed, alpha * (float) progress);
            if (progress >= .999) {
                float shimmer = (float) Math.pow(Math.max(0, Math.sin(time * .5)), 4);
                card(p, v, pivot.subtract(0, BELL_HALF_HEIGHT * 2, 0), right, up, 1.1, 1.1, RING, seed + 5, TINT, alpha * .5f * shimmer);
            }
            return;
        }

        double reach = Math.max(1, packet.radius());
        float fade = alpha * (1 - (float) smooth((age - 28) / 12.0));
        Vec3 pivot = new Vec3(0, 2.2 + BELL_HALF_HEIGHT, 0);
        double angle = swing(age);
        Vec3 mouth = pivot.add(rotate(UP.scale(-BELL_HALF_HEIGHT * 2), axis, angle));

        // 1. The bell swings against its clapper, still hanging from its rope.
        bell(p, v, pivot, axis, angle, 1, camera, time, seed, fade);
        rope(p, v, pivot, camera, seed, fade);
        Vec3 clapper = pivot.add(rotate(UP.scale(-BELL_HALF_HEIGHT * 1.75), axis, -angle * .6));
        ribbonPath(p, v, List.of(pivot, clapper), camera, .03, .02, TENDON, seed + 6, TINT, fade);
        card(p, v, clapper, right, up, .12, .14, CLAPPER, seed + 7, TINT, fade);

        // 2. The toll: a ring of sound from the mouth and echoes rolling out over a pool of rot.
        if (age < 10) {
            double burst = 1.2 + age * .35;
            card(p, v, mouth, right, up, burst, burst, RING, seed + 8, TINT, alpha * (1 - age / 10f));
        }
        double pool = reach * .6 * smooth(age / 10.0);
        ground(p, v, new Vec3(0, .05, 0), pool, ROT_POOL, seed + 9, TINT, fade * .75f);
        for (int ring = 0; ring < 3; ring++) {
            double radius = echo(ring, age, reach);
            if (radius <= 0) continue;
            float strength = alpha * (1 - (float) ((age - ring * ECHO_SPACING) / ECHO_LIFE)) * (1 - ring * .2f);
            ground(p, v, new Vec3(0, .06 + ring * .01, 0), radius, RING, seed + 10 + ring, TINT, strength);
        }

        // 3. Souls rise from the mouth and spores drift over the pool.
        for (int i = 0; i < 6; i++) {
            double cycle = ((age * .035) + i / 6.0) % 1;
            double a = i * 2.399 + seed;
            Vec3 at = mouth.add(Math.cos(a) * cycle * 1.2, cycle * 1.6, Math.sin(a) * cycle * 1.2);
            card(p, v, at, right, up, .22 + cycle * .2, .32 + cycle * .25, SOUL, seed + 20 + i, TINT, fade * .5f * (float) Math.sin(cycle * Math.PI));
        }
        for (int i = 0; i < 8; i++) {
            double a = i * 2.399 + seed + time * .01;
            double d = pool * (.3 + .6 * ((i * .37) % 1));
            Vec3 at = new Vec3(Math.cos(a) * d, .3 + Math.sin(time * .05 + i) * .15 + age * .01, Math.sin(a) * d);
            card(p, v, at, right, up, .06, .06, SPORE, seed + 30 + i, TINT, fade * .7f);
        }
    }

    /** A hollow bell of crossed shell planes hanging from {@code pivot}, condensing downward from its crown. */
    private static void bell(PoseStack p, VertexConsumer v, Vec3 pivot, Vec3 axis, double angle, double formed, Vec3 camera,
            double time, int seed, float alpha) {
        double height = BELL_HALF_HEIGHT * formed;
        Vec3 down = rotate(UP.scale(-1), axis, angle);
        Vec3 center = pivot.add(down.scale(height));
        Vec3 upright = down.scale(-1);
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 4 + Math.sin(time * .02) * .05;
            Vec3 across = rotate(new Vec3(Math.cos(a), 0, Math.sin(a)), axis, angle);
            card(p, v, center, across, upright, BELL_HALF_WIDTH * (.6 + .4 * formed), height, SHELL, seed + i, TINT, alpha * .8f);
        }
    }

    /** Tendons twisting up from the bell's crown into the dark. */
    private static void rope(PoseStack p, VertexConsumer v, Vec3 pivot, Vec3 camera, int seed, float alpha) {
        for (int strand = 0; strand < 2; strand++) {
            List<Vec3> points = new ArrayList<>(9);
            for (int i = 0; i <= 8; i++) {
                double t = i / 8.0, a = t * 5 + strand * Math.PI;
                points.add(pivot.add(Math.cos(a) * .03, t * 1.6, Math.sin(a) * .03));
            }
            ribbonPath(p, v, points, camera, .035, .01, TENDON, seed + 40 + strand, TINT, alpha * .8f);
        }
    }

    /** Rotates {@code v} about the unit {@code axis} by {@code angle} radians. */
    static Vec3 rotate(Vec3 v, Vec3 axis, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        return v.scale(cos).add(axis.cross(v).scale(sin)).add(axis.scale(axis.dot(v) * (1 - cos)));
    }
}
