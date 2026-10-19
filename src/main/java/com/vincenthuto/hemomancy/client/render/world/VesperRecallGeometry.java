package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperRecallRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.summon.PuppetStringsRules;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Vesper taking a puppet back. Three vein strings whip out from his raised Crossbar, hook the puppet's shoulders and
 * draw taut while blood beads climb them toward his hand; a sinew collar cinches the shoulders and the strings tremble
 * harder as the haul nears. At the haul they snap rigid, lift the body as it peels away, then reel the last clot of it
 * up the strings into the Crossbar, where it flashes out. Points are offsets from the puppet's feet; the Crossbar
 * point is supplied live by the renderer so the strings follow the mount.
 */
final class VesperRecallGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final int SAMPLES = 14;

    private VesperRecallGeometry() {}

    static boolean handles(Form form) {
        return form == Form.VESPER_RECALL;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, Vec3 crossbar, double width) {
        if (alpha <= 0) return;
        double height = Math.max(0.4, packet.radius());
        width = Math.max(0.3, width);
        int seed = Math.floorMod(packet.entityId(), 127);
        double hauled = age - VesperRecallRules.HAUL_TICK;
        double lift = hauled > 0 ? PuppetStringsRules.unspoolLift(hauled) : 0;
        double scale = hauled > 0 ? PuppetStringsRules.unspoolScale(hauled) : 1;
        double twist = hauled > 0 ? PuppetStringsRules.unspoolTwist(hauled) : 0;
        double reach = VesperRecallRules.reach(age);
        double slack = VesperRecallRules.slack(age);
        double strain = VesperRecallRules.strain(age);
        double reel = VesperRecallRules.reelIn(age);
        double arrival = VesperRecallRules.arrival(age);
        float pulse = heartbeat(time);
        double hook = age < VesperRecallRules.CAST_END ? 0
                : Math.max(0, 1 - (age - VesperRecallRules.CAST_END) / 4.0);
        // The strings let go of nothing once the clot is in his hand: they fade as it arrives.
        float stringAlpha = alpha * (float) (1 - PuppetStringsRules.smooth((reel - 0.75) / 0.25));

        // The sinew collar cinching the shoulders while the strings hold them.
        float collar = alpha * (float) (PuppetStringsRules.smooth((age - VesperRecallRules.CAST_END) / 3.0) * (1 - reel));
        if (collar > 0) {
            double radius = (0.42 - 0.12 * strain) * width * scale;
            double y = lift + height * (1 - 0.1 * scale);
            for (int strand = 0; strand < 2; strand++) {
                List<Vec3> ring = new ArrayList<>(SAMPLES + 1);
                for (int i = 0; i <= SAMPLES; i++) {
                    double angle = i * Math.PI * 2 / SAMPLES + strand * Math.PI + twist - time * 0.15;
                    ring.add(new Vec3(Math.cos(angle) * radius, y + Math.sin(angle * 3 + time * 0.4) * 0.04, Math.sin(angle) * radius));
                }
                ribbonPath(p, v, ring, camera, 0.05 * width, 0.03 * width, RIBBON, seed + strand, strand == 0 ? TINT : CLOT,
                        collar * (0.7f + 0.3f * pulse));
            }
        }

        for (int i = 0; i < VesperRecallRules.STRINGS; i++) {
            double angle = i * Math.PI * 2 / VesperRecallRules.STRINGS + seed * 0.37 + twist;
            double ring = 0.32 * width * scale;
            Vec3 shoulder = new Vec3(Math.cos(angle) * ring, lift + height * (1 - 0.08 * scale), Math.sin(angle) * ring);
            Vec3 hand = crossbar.add(Math.cos(angle) * 0.06, 0, Math.sin(angle) * 0.06);
            Vec3 attach = shoulder.lerp(hand, reel);
            Vec3 end = hand.lerp(attach, reach);
            float flicker = (float) (0.85 + 0.15 * Math.sin(time * 0.9 + i * 1.7 + seed));
            List<Vec3> points = path(hand, end, slack, strain, time, seed + i * 7);
            string(p, v, points, camera, stringAlpha * flicker, 1 + strain * 0.5);
            if (reach < 1) {
                card(p, v, end, right, up, 0.14, 0.2, WISP, seed + i, TINT, stringAlpha * 0.6f);
                continue;
            }
            // The hook bites, then holds as a pulsing knot.
            if (reel < 1) {
                double size = 0.18 * Math.max(0.6, width) * (1 + pulse * 0.3) + hook * 0.4;
                card(p, v, attach, right, up, size, size, FOCUS, seed + i, hook > 0 ? CORE : TINT,
                        alpha * (0.55f + 0.45f * pulse + (float) hook * 0.4f));
            }
            // Blood beads climbing the strings toward his hand while they hold.
            float beads = stringAlpha * (float) (strain * (1 - reel));
            if (beads > 0) {
                for (int k = 0; k < 2; k++) {
                    double t = (time * 0.07 + k * 0.5 + i * 0.21) % 1.0;
                    Vec3 at = points.get((int) Math.round((1 - t) * SAMPLES));
                    double size = 0.06 + 0.04 * Math.sin(t * Math.PI);
                    card(p, v, at, right, up, size, size, FOCUS, seed + i * 3 + k, CLOT, beads * 0.85f);
                }
            }
        }

        // What is left of the body, reeled up the strings as a clot.
        if (reel > 0 && reel < 1) {
            Vec3 heart = new Vec3(0, lift + height - height * scale * 0.5, 0).lerp(crossbar, reel);
            double size = (0.32 - 0.18 * reel) * width;
            card(p, v, heart, right, up, size, size, FOCUS, seed, CLOT, alpha * 0.9f);
            card(p, v, heart, right, up, size * 0.5, size * 0.5, FOCUS, seed + 1, CORE, alpha * 0.7f);
        }

        // His hand gathers as the strings strain, and flashes as the clot arrives.
        double gather = Math.max(strain * 0.6, arrival);
        if (gather > 0 && reach >= 1) {
            double size = 0.24 + 0.5 * gather;
            card(p, v, crossbar, right, up, size * 1.6, size * 1.6, FOCUS, seed + 5, TINT, alpha * (float) gather * 0.45f);
            card(p, v, crossbar, right, up, size, size, FOCUS, seed + 6, arrival > 0 ? CORE : CLOT, alpha * (float) gather * 0.85f);
        }
    }

    /** A string from his hand to {@code end}: sagging while loose, trembling under strain, rigid once hauled. */
    private static List<Vec3> path(Vec3 hand, Vec3 end, double slack, double strain, double time, int seed) {
        List<Vec3> points = new ArrayList<>(SAMPLES + 1);
        double length = hand.distanceTo(end);
        for (int k = 0; k <= SAMPLES; k++) {
            double t = k / (double) SAMPLES;
            double bow = Math.sin(t * Math.PI);
            double sag = bow * slack * Math.min(2.5, length * 0.18);
            double shake = bow * strain * 0.07;
            points.add(hand.lerp(end, t).add(Math.sin(time * 2.3 + seed + t * 9) * shake,
                    -sag + Math.sin(time * 0.25 + seed + t * 4) * 0.05 * slack,
                    Math.cos(time * 2.1 + seed + t * 7) * shake));
        }
        return points;
    }

    /** One vein string: a translucent shell, a tapering sinew core and a hot pale line. */
    private static void string(PoseStack p, VertexConsumer v, List<Vec3> points, Vec3 camera, float alpha, double widthScale) {
        if (alpha <= 0) return;
        // Read from across the arena: a wide blood haze, a sinew core that thickens toward his hand, a hot line.
        ribbonPath(p, v, points, camera, 0.16 * widthScale, RIBBON, 11, TINT, alpha * 0.3f);
        ribbonPath(p, v, points, camera, 0.045 * widthScale, 0.065 * widthScale, RIBBON, 12, TINT, alpha);
        ribbonPath(p, v, points, camera, 0.016 * widthScale, RIBBON, 13, CORE, alpha * 0.85f);
    }

    private static float heartbeat(double time) {
        return (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
    }
}
