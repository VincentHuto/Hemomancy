package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.smooth;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Umbral Step and Reversal travel through a tear in the world. On departure a seam opens in the air, its lips cored in
 * violet around a swirling void; ink reaches out and the body's smoke is drawn in before the seam seals with a flash.
 * On arrival a tear opens where they land, ink spills to the ground, the body leaks out of it and the seam closes.
 * Everything is ink on the Umbra material; points are offsets from the feet.
 */
final class UmbralTearGeometry {
    private static final int INK = 0xFFFFFF;
    /** Umbra shapes: ink ribbon, liquid pool, violet-cored cut; anything else is smoke. */
    private static final int INK_LINE = RIBBON, VOID = POOL, CUT = SLASH, SMOKE = WISP;
    static final int DEPART_TICKS = 18, ARRIVE_TICKS = 22;
    static final double HEIGHT = 2.1, WIDTH = .55;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private UmbralTearGeometry() {}

    static boolean handles(Form form) {
        return form == Form.TELEPORT || form == Form.UMBRA_ARRIVAL;
    }

    /** How far the tear gapes: open quickly, hold while the body passes, seal. */
    static double opening(Form form, double age) {
        if (form == Form.TELEPORT) return smooth(age / 3) * (1 - smooth((age - 9) / 5));
        return smooth(age / 4) * (1 - smooth((age - 12) / 4));
    }

    /**
     * How much of the body stands outside the tear, 1 whole to 0 gone: drawn in on departure, poured out on arrival.
     * This is the silhouette's horizontal spread.
     */
    static double presence(Form form, double age) {
        return form == Form.TELEPORT ? 1 - smooth((age - 2) / 8) : smooth((age - 3) / 9);
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha) {
        if (alpha <= 0) return;
        Form form = packet.form();
        boolean depart = form == Form.TELEPORT;
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        // The seam stands upright and turns to face the viewer around its own axis.
        Vec3 flat = new Vec3(right.x, 0, right.z);
        Vec3 side = flat.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : flat.normalize();
        double open = opening(form, age), body = presence(form, age);
        float life = (float) (1 - smooth((age - (depart ? 14 : 17)) / (depart ? 4.0 : 5.0)));
        float a = alpha * life;
        if (a <= .002) return;

        // 1. The void between the lips, and the lips themselves: ink outside, violet within.
        Vec3 center = UP.scale(HEIGHT * .5 + .05);
        if (open > .02) {
            card(p, v, center, side, UP, WIDTH * .8 * open, HEIGHT * .48, VOID, seed, INK, a);
            for (int lip = -1; lip <= 1; lip += 2) {
                List<Vec3> edge = new ArrayList<>(13);
                for (int i = 0; i <= 12; i++) {
                    double t = i / 12.0;
                    double bulge = Math.sin(t * Math.PI) * WIDTH * open * (1 + .08 * Math.sin(time * .4 + t * 6 + lip));
                    edge.add(UP.scale(.05 + t * HEIGHT).add(side.scale(lip * bulge)));
                }
                ribbonPath(p, v, edge, camera, .06, .06, INK_LINE, seed + 2 + lip, INK, a);
                ribbonPath(p, v, edge, camera, .035, .035, CUT, seed + 5 + lip, INK, a);
            }
        }

        // 2. Ink reaches out of the seam: grasping on departure, spilling to the ground on arrival.
        if (open > .2)
            for (int i = 0; i < 4; i++) {
                double h = HEIGHT * (.25 + i * .17);
                int lip = i % 2 == 0 ? 1 : -1;
                Vec3 root = UP.scale(h).add(side.scale(lip * WIDTH * open * .8));
                double reach = .5 * open * (.7 + .3 * Math.sin(time * .3 + i));
                Vec3 tip = depart ? root.add(side.scale(lip * reach)).add(0, -.1, 0)
                        : new Vec3(side.x * lip * reach * 1.4, 0.04, side.z * lip * reach * 1.4);
                Vec3 mid = root.lerp(tip, .5).add(0, depart ? .15 : -.05, 0);
                ribbonPath(p, v, List.of(root, mid, tip), camera, .05, .01, CUT, seed + 10 + i, INK, a * .9f);
            }

        // 3. The body as smoke: pulled in toward the seam, or pouring out of it.
        if (body > .02 && body < .98)
            for (int i = 0; i < 6; i++) {
                double h = .25 + i * .3;
                double drift = (1 - body) * (depart ? 1 : -1) * .1 * (i - 2.5);
                Vec3 at = UP.scale(h).add(side.scale(drift * .3));
                double width = .32 * body + .05;
                card(p, v, at, side, UP, width, .28, SMOKE, seed + 20 + i, INK, a * .7f);
            }

        // 4. The seal: a violet flash as the lips meet, and ink pooled where the traveller came through.
        double sealAge = depart ? age - 12 : age - 14;
        if (sealAge > -1 && sealAge < 3) {
            float flash = a * (float) (1 - Math.abs(sealAge - 1) / 2);
            ribbonPath(p, v, List.of(UP.scale(.1), center, UP.scale(HEIGHT)), camera, .09, .09, CUT, seed + 30, INK, flash);
        }
        if (!depart) {
            double pool = .55 * smooth((age - 2) / 6) * (1 - smooth((age - 15) / 7));
            if (pool > .02) ground(p, v, new Vec3(0, .04, 0), pool, VOID, seed + 31, INK, a);
        }
        for (int i = 0; i < 3; i++) {
            double rise = Math.max(0, age - (depart ? 11 : 14)) * .05;
            if (rise <= 0) continue;
            Vec3 at = UP.scale(.6 + i * .5 + rise).add(side.scale((i - 1) * .2));
            card(p, v, at, right, up, .25 + rise * .3, .3 + rise * .3, SMOKE, seed + 40 + i, INK, a * .5f);
        }
    }
}
