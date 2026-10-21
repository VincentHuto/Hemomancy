package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.card;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.ribbonPath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.SanguineWardRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Sanguine Ward: a sheath of live nerve woven around the body. Two counter-wound families of strands climb from the
 * feet into a diamond lattice over a faint glow shell, with bright knots where they cross and a current running up
 * each lit strand. Spent pool goes dark strand by strand as frayed stubs; a crown ring closes over the head while the
 * pool is full. Points are world-axis offsets from the warded body's feet.
 */
final class SanguineWardGeometry {
    private static final int NERVE = 0xFFE8A0, BLOOD = 0x9C263B, CORE = 0xFFD2C4;
    /** Ductilis shader shapes. */
    private static final int LINE = 0, GLOW = 1, CRACKLE = 2, SPARK = 3;
    private static final int SAMPLES = 10;

    private SanguineWardGeometry() {}

    static boolean handles(Form form) {
        return form == Form.WARD;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, CasterFrame frame, double height, double width) {
        if (alpha <= 0) return;
        int seed = Math.floorMod(packet.entityId(), 113);
        int live = SanguineWardRules.live(packet.count());
        double raise = SanguineWardRules.raise(age);
        double radius = Math.max(0.42, width * 0.75 + 0.12);
        double top = height * 0.95 * raise;
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        // In first person the lattice surrounds the camera; keep it to a faint sheath below the eyes.
        float a = alpha * (frame.firstPerson() ? 0.4f : 1f);
        double ceiling = frame.firstPerson() ? height * 0.6 : top;

        // 1. Shell: a soft glow sheath behind the lattice.
        if (!frame.firstPerson()) {
            for (int i = 0; i < 3; i++) {
                double y = top * (0.2 + 0.3 * i);
                double flicker = 0.85 + 0.2 * Math.sin(time * 0.9 + i * 1.7 + seed);
                card(p, v, new Vec3(0, y, 0), right, up, radius * 1.15 * flicker, top * 0.22, GLOW, seed + i, NERVE, a * 0.16f * (float) raise);
            }
        }

        // 2. Lattice: lit strands carry current, spent ones are dark frayed stubs.
        List<List<Vec3>> lit = new ArrayList<>();
        for (int family = -1; family <= 1; family += 2) {
            for (int i = 0; i < SanguineWardRules.STRANDS; i++) {
                boolean on = i < live;
                double reach = on ? 1 : 0.35;
                List<Vec3> path = new ArrayList<>(SAMPLES + 1);
                for (int k = 0; k <= SAMPLES; k++) {
                    double t = reach * k / (double) SAMPLES;
                    double angle = SanguineWardRules.angle(i, family, t, time) + seed;
                    double r = radius * (1 + 0.06 * Math.sin(t * Math.PI * 3 + time * 0.2 + i));
                    // Spent strands sag and fray outward.
                    if (!on) r *= 1 + t * 0.5;
                    double y = 0.08 + top * t;
                    if (y > ceiling) break;
                    path.add(new Vec3(Math.cos(angle) * r, y, Math.sin(angle) * r));
                }
                if (path.size() < 2) continue;
                if (on) {
                    lit.add(path);
                    float strand = a * (0.75f + 0.25f * pulse);
                    ribbonPath(p, v, path, camera, 0.04, 0.022, LINE, seed + 10 + i + family * 7, NERVE, strand);
                    ribbonPath(p, v, path, camera, 0.012, 0.006, LINE, seed + 30 + i + family * 7, CORE, strand * 0.9f);
                } else {
                    ribbonPath(p, v, path, camera, 0.02, 0.004, CRACKLE, seed + 50 + i + family * 7, BLOOD, a * 0.55f);
                }
            }
        }

        // 3. Current: a bright bead runs up each lit strand, one after another.
        for (int i = 0; i < lit.size(); i++) {
            List<Vec3> path = lit.get(i);
            double run = (time * 0.05 + i * 0.37) % 1;
            Vec3 bead = path.get((int) Math.min(path.size() - 1, Math.floor(run * (path.size() - 1))));
            card(p, v, bead, right, up, 0.05, 0.05, SPARK, seed + 70 + i, CORE, a * 0.9f);
        }

        // 4. Knots where the two families cross, brightening with each heartbeat.
        if (raise > 0.6 && live > 0) {
            for (int i = 0; i < live; i++) {
                for (int j = 0; j < live; j++) {
                    double t = SanguineWardRules.crossing(i, j, time);
                    if (t < 0) continue;
                    double angle = SanguineWardRules.angle(i, 1, t, time) + seed;
                    double y = 0.08 + top * t;
                    if (y > ceiling) continue;
                    Vec3 at = new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
                    card(p, v, at, right, up, 0.055 + 0.03 * pulse, 0.055 + 0.03 * pulse, SPARK, seed + 90 + i * 7 + j, NERVE, a * 0.8f);
                }
            }
        }

        // 5. Crown: a closed ring over the head while the pool is full.
        if (SanguineWardRules.crowned(packet.count()) && raise >= 1 && !frame.firstPerson()) {
            List<Vec3> crown = new ArrayList<>(25);
            for (int k = 0; k <= 24; k++) {
                double angle = k * Math.PI / 12 + time * 0.02;
                crown.add(new Vec3(Math.cos(angle) * radius * 0.85, top + 0.04 * Math.sin(k * 1.5 + time * 0.3), Math.sin(angle) * radius * 0.85));
            }
            ribbonPath(p, v, crown, camera, 0.035, LINE, seed + 120, NERVE, a * (0.7f + 0.3f * pulse));
            ribbonPath(p, v, crown, camera, 0.01, LINE, seed + 121, CORE, a);
            card(p, v, new Vec3(0, top, 0), right, up, radius * 0.6, 0.08, GLOW, seed + 122, NERVE, a * 0.3f);
        }
    }
}
