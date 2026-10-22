package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.*;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Blood Needles: tapered spines of glossy blood fired by the Living Pistol, Bloodburst armour and Vesper. In flight each
 * trails a hairline wake; lodged, it quivers and wells a bead at the point. Flight points are world positions. The
 * Ferric Blood Needle manipulation forges its needles in iron instead; see {@link ForgedNeedleGeometry}.
 */
final class BloodNeedleGeometry {
    static final double LENGTH = .8, LANCE_LENGTH = 1.4, WIDTH = .09, LANCE_WIDTH = .12;
    /** Splash radius for a needle puncture. */
    static final float PUNCTURE = .45F;

    private BloodNeedleGeometry() {}

    /** One needle from base to tip: a glossy spindle around a dark core with a glint at the point; lances grow barbs. */
    static void needle(PoseStack p, VertexConsumer v, Vec3 base, Vec3 tip, Vec3 camera, Vec3 right, Vec3 up,
            double width, int seed, float alpha, float glint, boolean barbs) {
        Vec3 axis = tip.subtract(base);
        double length = axis.length();
        if (length < 1e-4 || alpha <= 0) return;
        axis = axis.scale(1 / length);
        Vec3 belly = base.add(axis.scale(length * .38));
        ribbonPath(p, v, List.of(base, belly), camera, width * .35, width, RIBBON, seed, TINT, alpha);
        ribbonPath(p, v, List.of(belly, tip), camera, width, width * .06, RIBBON, seed + 1, TINT, alpha);
        ribbonPath(p, v, List.of(base.add(axis.scale(length * .1)), tip.subtract(axis.scale(length * .14))), camera,
                width * .3, width * .08, RIBBON, seed + 2, CLOT, alpha * .8f);
        if (glint > 0)
            card(p, v, tip.subtract(axis.scale(length * .08)), right, up, width * .9, width * .9, FOCUS, seed + 3, CORE, alpha * glint);
        if (!barbs) return;
        Vec3 side = facingSide(axis, camera.subtract(belly));
        for (double at : new double[] { .55, .74 })
            for (int s = -1; s <= 1; s += 2) {
                Vec3 root = base.add(axis.scale(length * at));
                Vec3 end = root.subtract(axis.scale(length * .13)).add(side.scale(s * width * 1.6));
                ribbonPath(p, v, List.of(root, end), camera, width * .4, width * .05, RIBBON, seed + 5 + s, TINT, alpha * .9f);
            }
    }

    static void flight(PoseStack p, VertexConsumer v, List<Vec3> path, Vec3 heading, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, int seed, float alpha, float wake, boolean lance, boolean lodged, float shake) {
        if (path.isEmpty()) return;
        Vec3 head = path.getLast();
        if (path.size() >= 2 && wake > 0)
            ribbonPath(p, v, path, camera, .004, lance ? .05 : .035, RIBBON, seed, TINT, wake * .6f);
        Vec3 axis = heading.lengthSqr() > 1e-8 ? heading.normalize() : new Vec3(0, -1, 0);
        double length = (lance ? LANCE_LENGTH : LENGTH) * (.7 + .3 * HemoEasing.smoothstep(age / 2.0));
        Vec3 tip = head.add(axis.scale(length * .5)), base = head.subtract(axis.scale(length * .5));
        float glint = (float) (.55 + .45 * Math.pow(Math.max(0, Math.sin(time * .6 + seed)), 4));
        if (lodged) {
            // The buried point stays put while the shaft shivers; blood wells where it went in.
            Vec3 side = facingSide(axis, camera.subtract(head));
            base = base.add(side.scale(Math.sin(shake * 3) * shake * .02));
            card(p, v, tip.subtract(axis.scale(length * .06)), right, up, .045, .06, FOCUS, seed + 9, CLOT, alpha);
            glint = 0;
        }
        needle(p, v, base, tip, camera, right, up, lance ? LANCE_WIDTH : WIDTH, seed, alpha, glint, lance);
    }
}
