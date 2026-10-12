package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.mortem.FruitingContagionRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Fruiting Contagion. On a carrier, pale stalks push out of the body over a rot pool, swell into caps and burst into
 * spore clouds. Where the rot spreads, a thread of hyphae and a drift of spores arcs to the neighbour, which sprouts
 * a small bloom of its own. Everything is on the Mortem material; points are offsets from the carrier's feet.
 */
final class FruitingContagionGeometry {
    private static final int TINT = 0xFFFFFF, CLOT = 0x7A1020;
    private static final int SPORE = 10;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private FruitingContagionGeometry() {}

    static boolean handles(Form form) {
        return form == Form.CONTAGION;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha) {
        if (alpha <= 0) return;
        double h = Math.max(0.8, packet.radius());
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        float a = alpha * (float) (1 - FruitingContagionRules.wither(age));
        if (a <= 0.002) return;
        if (FruitingContagionRules.spreading(packet.count())) spread(packet, p, v, camera, right, up, time, age, a, h, seed);
        else fruit(p, v, camera, right, up, time, age, a, h, seed);
    }

    /** Stalks fruiting out of the carrier and bursting into spores. */
    private static void fruit(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float age,
            float a, double h, int seed) {
        double growth = FruitingContagionRules.growth(age), burst = FruitingContagionRules.burst(age);
        ground(p, v, new Vec3(0, 0.04, 0), 0.55 * growth, POOL, seed, TINT, a * 0.6f);
        for (int k = 0; k < 4; k++) {
            double angle = k * Math.PI / 2 + seed;
            Vec3 out = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            Vec3 base = out.scale(0.22).add(UP.scale(h * (0.35 + 0.14 * k)));
            Vec3 tip = base.add(out.scale(0.3 * growth)).add(UP.scale(0.38 * growth));
            Vec3 mid = base.lerp(tip, 0.5).add(out.scale(0.08 * Math.sin(time * 0.2 + k)));
            ribbonPath(p, v, List.of(base, mid, tip), camera, 0.045, 0.014, RIBBON, seed + k, TINT, a);
            double cap = 0.1 * growth * (1 - burst * 0.6);
            card(p, v, tip, right, up, cap * 1.3, cap, SPORE, seed + 10 + k, TINT, a);
            if (burst > 0) {
                double cloud = 0.3 * burst;
                card(p, v, tip.add(UP.scale(0.1 * burst)), right, up, cloud, cloud, WISP, seed + 20 + k, TINT, a * (float) burst * 0.8f);
                for (int s = 0; s < 3; s++) {
                    double sa = s * 2.1 + k + seed;
                    Vec3 drift = tip.add(new Vec3(Math.cos(sa), 0.6, Math.sin(sa)).scale(0.35 * burst));
                    card(p, v, drift, right, up, 0.045, 0.045, SPORE, seed + 30 + k * 3 + s, TINT, a * (float) burst);
                }
            }
        }
        // A dark wound where the stalks broke through.
        card(p, v, UP.scale(h * 0.55), right, up, 0.16 * growth, 0.16 * growth, FOCUS, seed + 40, CLOT, a * 0.6f);
    }

    /** A thread of hyphae and drifting spores arcing from the carrier to its neighbour, which sprouts in turn. */
    private static void spread(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right,
            Vec3 up, double time, float age, float a, double h, int seed) {
        Vec3 line = packet.to().subtract(packet.from());
        double distance = line.length();
        if (distance < 0.2) return;
        double arc = FruitingContagionRules.arc(age), bloom = FruitingContagionRules.bloom(age);
        Vec3 start = UP.scale(h * 0.8);
        List<Vec3> thread = new ArrayList<>(13);
        for (int i = 0; i <= 12; i++) {
            double t = arc * i / 12.0;
            thread.add(point(start, line, distance, t, h).add(0, 0.05 * Math.sin(time * 0.3 + i), 0));
        }
        if (arc > 0.02) ribbonPath(p, v, thread, camera, 0.02, 0.01, RIBBON, seed + 50, TINT, a * 0.55f);
        for (int k = 0; k < 6; k++) {
            double t = arc - k * 0.07;
            if (t <= 0 || t >= 1) continue;
            card(p, v, point(start, line, distance, t, h), right, up, 0.06, 0.06, SPORE, seed + 60 + k, TINT, a * (1 - k / 7f));
        }
        if (bloom > 0) {
            Vec3 landing = line.add(UP.scale(h * 0.6));
            for (int k = 0; k < 3; k++) {
                double angle = k * 2.1 + seed;
                Vec3 out = new Vec3(Math.cos(angle), 0, Math.sin(angle));
                Vec3 tip = landing.add(out.scale(0.18 * bloom)).add(UP.scale(0.25 * bloom));
                ribbonPath(p, v, List.of(landing, tip), camera, 0.03, 0.01, RIBBON, seed + 70 + k, TINT, a);
                card(p, v, tip, right, up, 0.07 * bloom, 0.05 * bloom, SPORE, seed + 80 + k, TINT, a);
            }
        }
    }

    private static Vec3 point(Vec3 start, Vec3 line, double distance, double t, double h) {
        Vec3 end = line.add(UP.scale(h * 0.6));
        return start.lerp(end, t).add(UP.scale(FruitingContagionRules.lift(t) * distance));
    }
}
