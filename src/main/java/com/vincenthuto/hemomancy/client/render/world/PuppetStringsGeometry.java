package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.summon.PuppetStringsRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A puppet arriving on and leaving by its strings. On arrival, vein strings fall from an unseen control above, the
 * body is lowered onto its feet while sinew knits up it, and the strings fray away. On departure they snap taut and
 * haul the shrinking, twisting body upward as it peels into sinew, until a flash takes the last of it. Points are
 * world-axis offsets from the body's feet; the packet carries its height as {@code radius} and its width as
 * {@code to.x - from.x}, and its string count as {@code count}.
 */
final class PuppetStringsGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final int SAMPLES = 10;

    private PuppetStringsGeometry() {}

    static boolean handles(Form form) {
        return form == Form.PUPPET_DESCEND || form == Form.PUPPET_UNSPOOL;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha) {
        if (alpha <= 0) return;
        double height = Math.max(0.4, packet.radius());
        double width = Math.max(0.3, packet.to().x - packet.from().x);
        int seed = Math.floorMod(packet.entityId(), 113);
        int strings = PuppetStringsRules.strings(packet.count());
        if (packet.form() == Form.PUPPET_DESCEND) descend(p, v, camera, right, up, time, age, alpha, height, width, seed, strings);
        else unspool(p, v, camera, right, up, time, age, alpha, height, width, seed, packet.count() < 0 ? 0 : strings);
    }

    private static void descend(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time,
            double age, float alpha, double height, double width, int seed, int strings) {
        double reach = PuppetStringsRules.stringReach(age);
        double lift = PuppetStringsRules.descendOffset(age);
        double scale = PuppetStringsRules.descendScale(age);
        double fray = PuppetStringsRules.descendFray(age);
        float pulse = heartbeat(time);

        // Sinew knits up the body from its feet while it is lowered, then sinks into it.
        double knit = PuppetStringsRules.knit(age);
        float knitAlpha = alpha * (float) (1 - HemoEasing.smoothstep((age - PuppetStringsRules.LOWER_END) / 2.0));
        if (knit > 0 && knitAlpha > 0) {
            double radius = 0.55 * width * scale;
            for (int strand = 0; strand < 3; strand++) {
                List<Vec3> points = new ArrayList<>(SAMPLES + 1);
                for (int i = 0; i <= SAMPLES; i++) {
                    double y = height * knit * i / SAMPLES;
                    double angle = y * 7 + strand * Math.PI * 2 / 3 - time * 0.3;
                    points.add(new Vec3(Math.cos(angle) * radius, lift + y, Math.sin(angle) * radius));
                }
                taperedSinew(p, v, points, camera, width, seed + strand, knitAlpha * (0.75f + 0.25f * pulse));
            }
            for (int i = 0; i < 4; i++) {
                double angle = i * Math.PI / 2 + time * 0.2 + seed;
                double flicker = 0.85 + 0.25 * Math.sin(time * 0.9 + i * 1.7 + seed);
                Vec3 edge = new Vec3(Math.cos(angle) * radius, lift + height * knit, Math.sin(angle) * radius);
                card(p, v, edge, right, up, 0.22 * width * flicker, 0.28 * width * flicker, WISP, seed + i, TINT, knitAlpha * 0.3f);
            }
        }

        // The strings fall from above, carry the body down, then fray and drift up once it stands.
        for (int i = 0; i < strings; i++) {
            Vec3 top = control(i, strings, seed, height, width, 0).add(UP.scale(fray * 0.6));
            Vec3 attach = attach(i, strings, seed, height, width, lift, scale, 0);
            Vec3 end = top.lerp(attach, reach * (1 - fray));
            float flicker = (float) (0.85 + 0.15 * Math.sin(time * 0.9 + i * 1.7 + seed));
            float strand = alpha * (1 - (float) fray * 0.9f) * flicker;
            string(p, v, camera, top, end, 1 - reach * 0.6, time, seed + i * 7, strand, 1 + fray * 0.8);
            if (reach < 1) card(p, v, end, right, up, 0.16 * width, 0.22 * width, WISP, seed + i, TINT, strand * 0.5f);
            else if (fray < 1) knot(p, v, end, right, up, width, seed + i, strand, pulse);
        }

        // A brief bloom where the feet will land.
        float landing = alpha * (float) (1 - HemoEasing.smoothstep(age / 6.0)) * 0.5f;
        if (landing > 0) card(p, v, new Vec3(0, 0.05, 0), right, up, 0.9 * width, 0.5 * width, FOCUS, seed, TINT, landing);
    }

    private static void unspool(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time,
            double age, float alpha, double height, double width, int seed, int strings) {
        double taut = PuppetStringsRules.taut(age);
        double lift = PuppetStringsRules.unspoolLift(age);
        double scale = PuppetStringsRules.unspoolScale(age);
        double twist = PuppetStringsRules.unspoolTwist(age);
        double fray = PuppetStringsRules.unspoolFray(age);
        double flash = PuppetStringsRules.flash(age);
        double release = HemoEasing.smoothstep((age - PuppetStringsRules.HAUL_END) / 3.0);
        float pulse = heartbeat(time);
        Vec3 heart = new Vec3(0, lift + height - height * scale * 0.5, 0);

        // Sinew peels off the rising body and falls away against the motion.
        float peel = alpha * (float) (fray * (1 - release));
        if (peel > 0) {
            for (int strand = 0; strand < 3; strand++) {
                List<Vec3> points = new ArrayList<>(SAMPLES + 1);
                for (int i = 0; i <= SAMPLES; i++) {
                    double t = i / (double) SAMPLES;
                    double angle = t * 5 + strand * Math.PI * 2 / 3 + twist * 2;
                    double radius = (0.3 * width * scale) + fray * 0.6 * t;
                    points.add(heart.add(Math.cos(angle) * radius, -t * 0.9 * fray, Math.sin(angle) * radius));
                }
                taperedSinew(p, v, points, camera, width, seed + strand, peel * (0.75f + 0.25f * pulse));
            }
            for (int i = 0; i < 5; i++) {
                double flicker = 0.85 + 0.25 * Math.sin(time * 0.9 + i * 1.7 + seed);
                Vec3 at = heart.add(Math.sin(seed + i * 2.1) * 0.12, -0.15 - i * 0.2 * fray, Math.cos(seed + i * 2.1) * 0.12);
                double size = 0.34 * width * (1 - i * 0.12) * flicker;
                card(p, v, at, right, up, size, size * 1.3, WISP, seed + i, TINT, peel * 0.3f);
            }
        }

        // Taut strings haul the body up; after the flash they let go and fray upward.
        double tremble = 0.05 * Math.exp(-age * 0.4);
        for (int i = 0; i < strings; i++) {
            Vec3 top = control(i, strings, seed, height, width, lift * 0.25 + release * 0.5);
            Vec3 attach = attach(i, strings, seed, height, width, lift, scale, twist);
            Vec3 end = top.lerp(attach, taut * (1 - release));
            float flicker = (float) (0.85 + 0.15 * Math.sin(time * 0.9 + i * 1.7 + seed));
            float strand = alpha * (1 - (float) release * 0.9f) * flicker;
            string(p, v, camera, top, end, tremble, time, seed + i * 7, strand, 1 + release);
            if (release < 1) knot(p, v, end, right, up, width, seed + i, strand, pulse);
        }

        // The flash swallows what is left of the body where it meets its strings.
        if (flash > 0) {
            double size = (0.6 + 0.9 * flash) * width;
            card(p, v, heart, right, up, size * 1.6, size * 1.6, FOCUS, seed, TINT, alpha * (float) flash * 0.5f);
            card(p, v, heart, right, up, size, size, FOCUS, seed + 1, CORE, alpha * (float) flash * 0.9f);
        }
    }

    /** Where string {@code i} meets the top of the body: a ring across the shoulders that twists with it. */
    static Vec3 attach(int i, int strings, int seed, double height, double width, double lift, double scale, double twist) {
        double angle = i * Math.PI * 2 / strings + seed * 0.37 + twist;
        double ring = 0.32 * width * scale;
        return new Vec3(Math.cos(angle) * ring, lift + height, Math.sin(angle) * ring);
    }

    /** Where string {@code i} vanishes above: they gather toward an unseen control. */
    static Vec3 control(int i, int strings, int seed, double height, double width, double raise) {
        double angle = i * Math.PI * 2 / strings + seed * 0.37;
        double spread = 0.12 * width;
        return new Vec3(Math.cos(angle) * spread, height + PuppetStringsRules.STRING_RISE + raise, Math.sin(angle) * spread);
    }

    /** One vein string: a translucent shell, a tapering sinew core and a hot pale line, swaying with {@code slack}. */
    private static void string(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 top, Vec3 end, double slack,
            double time, int seed, float alpha, double widthScale) {
        if (alpha <= 0 || top.distanceToSqr(end) < 1e-6) return;
        List<Vec3> points = new ArrayList<>(SAMPLES + 1);
        for (int k = 0; k <= SAMPLES; k++) {
            double t = k / (double) SAMPLES;
            double bow = Math.sin(t * Math.PI) * slack;
            points.add(top.lerp(end, t).add(Math.sin(time * 0.25 + seed + t * 4) * 0.06 * bow, 0,
                    Math.cos(time * 0.21 + seed + t * 3) * 0.06 * bow));
        }
        ribbonPath(p, v, points, camera, 0.07 * widthScale, RIBBON, seed, TINT, alpha * 0.28f);
        ribbonPath(p, v, points, camera, 0.028 * widthScale, 0.018 * widthScale, RIBBON, seed + 1, TINT, alpha);
        ribbonPath(p, v, points, camera, 0.009 * widthScale, RIBBON, seed + 2, CORE, alpha * 0.8f);
    }

    /** Sinew in three tapering lengths, as on the Riposte arm. */
    private static void taperedSinew(PoseStack p, VertexConsumer v, List<Vec3> points, Vec3 camera, double width,
            int seed, float alpha) {
        double size = Math.max(0.6, Math.min(1.4, width));
        int third = Math.max(2, points.size() / 3);
        ribbonPath(p, v, points.subList(0, Math.min(points.size(), third + 1)), camera, 0.07 * size, RIBBON, seed, TINT, alpha);
        ribbonPath(p, v, points.subList(Math.min(points.size() - 1, third), Math.min(points.size(), 2 * third + 1)),
                camera, 0.05 * size, RIBBON, seed + 3, TINT, alpha);
        ribbonPath(p, v, points.subList(Math.min(points.size() - 1, 2 * third), points.size()),
                camera, 0.035 * size, RIBBON, seed + 6, CLOT, alpha);
    }

    private static void knot(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double width, int seed,
            float alpha, float pulse) {
        double size = 0.14 * Math.max(0.6, width) * (1 + pulse * 0.3);
        card(p, v, at, right, up, size, size, FOCUS, seed, TINT, alpha * (0.6f + 0.4f * pulse));
    }

    private static float heartbeat(double time) {
        return (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
    }
}
