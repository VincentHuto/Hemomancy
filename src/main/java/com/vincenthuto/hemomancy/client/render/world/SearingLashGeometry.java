package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.SearingLashRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Searing Lash: a whip of boiling blood. A molten core inside a flame sheath uncoils from the hand with a travelling
 * wave, cracks against the target in a burst of flame tongues and embers, then sags under its own weight and burns
 * away into smoke. Points are offsets from the hand; the far end is the target.
 */
final class SearingLashGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4;
    private static final int RIBBON = 0, FOCUS = 4, SAMPLES = 16;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private SearingLashGeometry() {}

    static boolean handles(Form form) {
        return form == Form.SEARING_LASH;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer flame, VertexConsumer molten,
            Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha) {
        if (alpha <= 0) return;
        Vec3 line = packet.to().subtract(packet.from());
        double length = line.length();
        if (length < 0.2) return;
        Vec3 dir = line.scale(1 / length);
        Vec3 side = dir.cross(UP);
        side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
        int swing = SearingLashRules.side(packet.count());
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        double extent = SearingLashRules.extent(age), slack = SearingLashRules.slack(age), crack = SearingLashRules.crack(age);
        float a = alpha * (float) (1 - slack * 0.85);
        double waveScale = Math.min(1.5, length * 0.25);

        List<Vec3> whip = new ArrayList<>(SAMPLES + 1);
        for (int i = 0; i <= SAMPLES; i++) {
            double t = i / (double) SAMPLES;
            double along = t * extent;
            double sag = Math.sin(t * Math.PI) * (0.05 + slack * 1.1);
            whip.add(dir.scale(length * along).add(side.scale(SearingLashRules.wave(t, age, swing) * waveScale))
                    .add(UP.scale(-sag)));
        }
        Vec3 tip = whip.getLast();

        // 1. Flame sheath with a molten core and a hot line down its middle, thinning toward the tip.
        if (a > 0.002) {
            ThermalGeometry.flameRibbon(p, flame, whip, camera, 0.09 * (1 - slack * 0.5), seed, a * 0.8f);
            LuxUmbraGeometry.ribbonPath(p, molten, whip, camera, 0.04 * (1 - slack * 0.6), 0.012, RIBBON, seed + 1, TINT, a);
            LuxUmbraGeometry.ribbonPath(p, molten, whip, camera, 0.012, 0.004, RIBBON, seed + 2, CORE, a);
        }

        // 2. The crack: a flare at the tip, flame tongues licking outward and a spray of embers.
        if (crack > 0) {
            float c = alpha * (float) crack;
            ThermalGeometry.fire(p, flame, tip.subtract(0, 0.2 * crack, 0), right, up, 0.3 * crack, 0.45 * crack, seed + 3, c);
            for (int k = 0; k < 5; k++) {
                double angle = k * 2.39996 + seed;
                Vec3 lean = dir.scale(0.4).add(side.scale(Math.cos(angle) * 0.6)).add(UP.scale(0.3 + 0.3 * Math.sin(angle)));
                ThermalGeometry.flameRibbon(p, flame, ThermalGeometry.tongue(tip, lean, 0.45 * crack, time, seed + 10 + k),
                        camera, 0.05, seed + 10 + k, c * 0.9f);
                Vec3 ember = tip.add(lean.normalize().scale(0.55 * crack + 0.1 * k * crack));
                LuxUmbraGeometry.card(p, molten, ember, right, up, 0.035, 0.035, FOCUS, seed + 20 + k, CORE, c);
            }
        }

        // 3. Burn-away: the spent whip smokes along its length as it sags.
        if (slack > 0 && slack < 1)
            for (int i = 2; i < whip.size(); i += 3) {
                float smoke = alpha * (float) (slack * (1 - slack) * 1.6);
                ThermalGeometry.smoke(p, flame, whip.get(i).add(UP.scale(slack * 0.6)), right, up, 0.12 + slack * 0.2,
                        0.18 + slack * 0.3, seed + 30 + i, smoke);
            }
    }
}
