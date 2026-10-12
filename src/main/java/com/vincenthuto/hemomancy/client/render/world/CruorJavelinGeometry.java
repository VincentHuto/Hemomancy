package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Cruor Javelin: blood arrested into a thrown shaft. Frost strands freeze together around a spine of solid cruor clots
 * at the hand, the shaft flies point-first shedding powder, bursts frost fingers across the body it strikes, then
 * shatters and falls. Points are offsets from the throwing hand; the far end is the target.
 */
final class CruorJavelinGeometry {
    private static final int WHITE = 0xFFFFFF;
    private static final double SHAFT = 1.4;

    private CruorJavelinGeometry() {}

    static boolean handles(Form form) {
        return form == Form.CRUOR_JAVELIN;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer core, VertexConsumer frost,
            Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha) {
        if (alpha <= 0) return;
        Vec3 line = packet.to().subtract(packet.from());
        double length = line.length();
        if (length < 0.3) return;
        Vec3 dir = line.scale(1 / length);
        Vec3[] basis = basis(dir);
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        double formed = CruorJavelinRules.formation(age), travel = CruorJavelinRules.travel(age);
        double impact = CruorJavelinRules.impact(age), shatter = CruorJavelinRules.shatter(age);
        float a = alpha * (float) (1 - shatter);
        double rest = Math.min(0.6, length * 0.3);
        // The head waits just ahead of the hand, then flies until it lodges in the target.
        Vec3 head = dir.scale(rest + (length - rest) * travel);
        Vec3 tail = head.subtract(dir.scale(SHAFT * formed));
        double fall = shatter * shatter * 0.9;

        // 1. Shaft: three frost strands twisting about the line, thick at the tail and fine at the point.
        if (a > 0.002 && formed > 0.02) {
            for (int strand = 0; strand < 3; strand++) {
                List<Vec3> points = new ArrayList<>(9);
                for (int i = 0; i <= 8; i++) {
                    double t = i / 8.0;
                    double angle = t * 4 + strand * Math.PI * 2 / 3 + time * 0.05;
                    double radius = 0.05 * (1 - t * 0.7) * (1 + shatter * 2);
                    points.add(tail.lerp(head, t).add(basis[0].scale(Math.cos(angle) * radius))
                            .add(basis[1].scale(Math.sin(angle) * radius)).add(0, -fall * (0.5 + t * 0.5), 0));
                }
                LuxUmbraGeometry.ribbonPath(p, frost, points, camera, 0.035, 0.008, ThermalGeometry.FROST_STRAND,
                        seed + strand, WHITE, a);
            }
            // A spine of solid cruor clots, the heaviest at the head, and a frost point beyond it.
            for (int i = 0; i < 3; i++) {
                double t = 0.3 + i * 0.3;
                Vec3 at = tail.lerp(head, t).add(0, -fall * (0.6 + t * 0.4), 0);
                double size = (0.045 + 0.03 * i) * formed * (1 - shatter * 0.6);
                ThermalGeometry.clot(p, core, frost, at, size, seed + 10 + i, time * 0.05 + i, a);
            }
            LuxUmbraGeometry.ribbonPath(p, frost, List.of(head.add(0, -fall, 0), head.add(dir.scale(0.32)).add(0, -fall, 0)),
                    camera, 0.06, 0.004, ThermalGeometry.FROST_STRAND, seed + 20, WHITE, a);
        }

        // 2. Flight: powder shed in its wake.
        if (travel > 0 && travel < 1)
            for (int k = 1; k <= 4; k++)
                ThermalGeometry.powder(p, frost, tail.subtract(dir.scale(k * 0.3)), right, up, 0.1 + k * 0.02, seed + 30 + k,
                        alpha * 0.5f * (1 - k / 5f));

        // 3. Impact: frost fingers race across the body around the wound, with a pale burst of powder.
        if (impact > 0 && shatter < 1) {
            Vec3 wound = line;
            for (int finger = 0; finger < 7; finger++) {
                double angle = finger * Math.PI * 2 / 7 + seed;
                Vec3 out = basis[0].scale(Math.cos(angle)).add(basis[1].scale(Math.sin(angle)));
                List<Vec3> path = new ArrayList<>(6);
                for (int i = 0; i <= 5; i++) {
                    double t = i / 5.0;
                    double wander = Math.sin(t * 5 + finger) * 0.08 * t;
                    path.add(wound.subtract(dir.scale(0.05)).add(out.scale(0.55 * impact * t))
                            .add(out.cross(dir).scale(wander)));
                }
                LuxUmbraGeometry.ribbonPath(p, frost, path, camera, 0.05, 0.006, ThermalGeometry.FROST_STRAND, seed + 40 + finger,
                        WHITE, a * 0.85f);
            }
            float burst = alpha * (float) (impact * (1 - impact * 0.5) * (1 - shatter));
            ThermalGeometry.powder(p, frost, wound.subtract(dir.scale(0.1)), right, up, 0.35 * impact, seed + 50, burst);
        }

        // 4. Shatter: fragments of the shaft drop and the frost dusts away upward.
        if (shatter > 0 && shatter < 1)
            for (int k = 0; k < 4; k++) {
                Vec3 at = tail.lerp(head, 0.2 + k * 0.2).add(0, -fall - shatter * 0.3 * k, 0);
                ThermalGeometry.powder(p, frost, at.add(0, shatter * 0.5, 0), right, up, 0.12 + shatter * 0.15, seed + 60 + k,
                        alpha * (float) (shatter * (1 - shatter) * 1.4));
            }
    }

    private static Vec3[] basis(Vec3 axis) {
        Vec3 reference = Math.abs(axis.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 first = axis.cross(reference).normalize();
        return new Vec3[] {first, first.cross(axis).normalize()};
    }
}
