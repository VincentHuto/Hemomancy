package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.card;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.facingSide;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.ground;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.ribbonPath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Phoenix Debt: the body pays to survive and erupts. A flame column and molten shock pool burst from the feet while
 * wings of burning blood emerge folded from the back, unfurl, beat twice shedding embers, then fold and burn away.
 * Each wing is a jointed bone of molten blood with flames licking its leading edge and flame feathers splayed from the
 * hand. While armed, an ember heart beats slowly in the chest. Points are offsets from the wearer's feet.
 */
final class PhoenixDebtGeometry {
    private static final int WHITE = 0xFFFFFF;
    /** Flame shader shapes. */
    private static final int FLAME = 0, SMOKE = 1, EMBER = 2, TORCH = 3;
    /** Molten shader shapes. */
    private static final int MOLTEN_POOL = 0, CRUST = 2;
    static final int TICKS = 48, ERUPT_END = 6, SPREAD_END = 14, BEAT_END = 36;
    static final double BEAT_PERIOD = 11, PRIMARIES = 8, COVERTS = 5;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    /** Wearer pose: wings root on the body and follow its facing. */
    record Frame(float bodyYaw, float scale, boolean crouching, boolean firstPerson) {}

    private PhoenixDebtGeometry() {}

    static boolean handles(Form form) {
        return form == Form.PHOENIX || form == Form.PHOENIX_READY;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer flame, VertexConsumer molten, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha, Frame frame) {
        if (alpha <= 0) return;
        int seed = Math.floorMod(packet.entityId() >= 0 ? packet.entityId() : packet.from().hashCode(), 113);
        if (packet.form() == Form.PHOENIX_READY) ready(p, flame, molten, camera, right, up, time, alpha, frame, seed);
        else erupt(packet, p, flame, molten, camera, right, up, time, age, alpha, frame, seed);
    }

    /** How far the wings have spread: emerge folded, unfurl past full, hold through the beats, fold as they burn away. */
    static double spread(double age) {
        if (age < ERUPT_END) return .25 * HemoEasing.smoothstep(age / ERUPT_END);
        if (age < SPREAD_END) {
            double t = (age - ERUPT_END) / (SPREAD_END - ERUPT_END), c = 1.6;
            return .25 + .75 * (1 + (c + 1) * Math.pow(t - 1, 3) + c * Math.pow(t - 1, 2));
        }
        if (age < BEAT_END) return 1;
        return 1 - .7 * HemoEasing.smoothstep((age - BEAT_END) / (TICKS - BEAT_END));
    }

    /** Wing elevation through the beats, from -1 (down stroke) to 1 (raised), easing in and out of the beat phase. */
    static double beat(double age) {
        double envelope = HemoEasing.smoothstep((age - (SPREAD_END - 2)) / 4) * (1 - HemoEasing.smoothstep((age - BEAT_END) / 4));
        return envelope <= 0 ? 0 : Math.sin((age - SPREAD_END) * Math.PI * 2 / BEAT_PERIOD) * envelope;
    }

    /**
     * A wing joint in wing space (x outward, y up, z behind), blended from folded to spread and raised by the beat about
     * the shoulder's forward axis. Joint 0 is the shoulder root.
     */
    static Vec3 joint(int index, double spread, double beat) {
        double[][] folded = { { 0, 0, 0 }, { .25, .25, .25 }, { .35, -.15, .45 }, { .4, -.75, .55 } };
        double[][] open = { { 0, 0, 0 }, { .7, .35, .15 }, { 1.9, .6, .25 }, { 3.2, .25, .45 } };
        double x = folded[index][0] + (open[index][0] - folded[index][0]) * spread;
        double y = folded[index][1] + (open[index][1] - folded[index][1]) * spread;
        double z = folded[index][2] + (open[index][2] - folded[index][2]) * spread;
        return lift(new Vec3(x, y, z), beat);
    }

    private static Vec3 lift(Vec3 local, double beat) {
        double a = beat * .5, cos = Math.cos(a), sin = Math.sin(a);
        return new Vec3(local.x * cos - local.y * sin, local.x * sin + local.y * cos, local.z);
    }

    private static void erupt(ManipulationVisualPacket packet, PoseStack p, VertexConsumer flame, VertexConsumer molten,
            Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha, Frame frame, int seed) {
        double s = frame.scale(), r = Math.max(1, packet.radius());
        Vec3 f = vec(HematicRiposteRules.forward(frame.bodyYaw())), side = vec(HematicRiposteRules.right(frame.bodyYaw()));
        double spread = spread(age), beat = beat(age);
        float burn = (float) HemoEasing.smoothstep((double) ((age - BEAT_END) / (TICKS - BEAT_END)));

        // 1. Eruption: a flame column and a molten shock pool racing out to the blast's edge.
        if (age < ERUPT_END + 6 && !frame.firstPerson()) {
            float surge = alpha * (1 - (float) HemoEasing.smoothstep((age - 2) / (ERUPT_END + 4.0)));
            double height = 1.8 * s * (.6 + .4 * HemoEasing.smoothstep(age / 2.0));
            card(p, flame, UP.scale(height * .75), right, UP, .55 * s, height, TORCH, seed, WHITE, surge);
            card(p, flame, UP.scale(height * .7), right, UP, .9 * s, height * 1.1, FLAME, seed + 1, WHITE, surge * .7f);
        }
        if (age < 20) {
            double wave = r * HemoEasing.smoothstep(age / 8.0);
            float pool = alpha * (1 - (float) HemoEasing.smoothstep((age - 8) / 12.0));
            ground(p, molten, new Vec3(0, .05, 0), wave, MOLTEN_POOL, seed + 2, WHITE, pool * .9f);
            if (age > 3 && age < 14)
                for (int i = 0; i < 10; i++) {
                    double a = i * Math.PI / 5 + seed;
                    Vec3 at = new Vec3(Math.cos(a) * wave, 0, Math.sin(a) * wave);
                    card(p, flame, at.add(0, .3, 0), right, UP, .22, .4, FLAME, seed + 3 + i, WHITE, pool);
                }
        }

        // 2. Heart: the debt itself, beating in the chest.
        if (!frame.firstPerson()) heart(p, molten, f, s, frame.crouching(), time, seed, alpha * (1 - burn * .7f), .13);

        // 3. Wings.
        Vec3 root = UP.scale((frame.crouching() ? 1.12 : 1.35) * s).add(f.scale(-.2 * s));
        float body = alpha * (1 - burn * .85f);
        double grow = HemoEasing.smoothstep((age - 1) / 8.0);
        for (int w = -1; w <= 1; w += 2) {
            Vec3 shoulder = root.add(side.scale(w * .12 * s));
            Vec3[] bone = new Vec3[4];
            for (int j = 0; j < 4; j++) bone[j] = world(joint(j, spread, beat), shoulder, side, f, w, s);
            List<Vec3> spine = new ArrayList<>(13);
            for (int i = 0; i <= 12; i++) {
                double t = i / 12.0;
                if (frame.firstPerson() && t < .3) continue;
                spine.add(bezier(bone[0], bone[1], bone[2], bone[3], t));
            }
            // Bone of molten blood, flames licking up its leading edge.
            ribbonPath(p, molten, spine, camera, .1 * s, .03 * s, MOLTEN_POOL, seed + 10 + w, WHITE, body);
            for (int i = 1; i <= 6; i++) {
                double t = .12 + i * .14;
                if (frame.firstPerson() && t < .3) continue;
                Vec3 at = bezier(bone[0], bone[1], bone[2], bone[3], t);
                double h = .32 * s * (1 - t * .4) * (.85 + .15 * Math.sin(time * .8 + i + w));
                card(p, flame, at.add(0, h * .75, 0), right, UP, .13 * s, h, FLAME, seed + 20 + i + w * 7, WHITE, body);
            }
            // Primaries splay from the hand; coverts layer the arm near the body.
            for (int k = 0; k < PRIMARIES + COVERTS; k++) {
                boolean primary = k < PRIMARIES;
                double u = primary ? k / (PRIMARIES - 1) : (k - PRIMARIES) / (COVERTS - 1);
                double t = primary ? .45 + .55 * u : .08 + .35 * u;
                if (frame.firstPerson() && t < .3) continue;
                Vec3 base = bezier(bone[0], bone[1], bone[2], bone[3], t);
                Vec3 local = primary ? new Vec3(.25 + .75 * u, -1 + .55 * u, .35) : new Vec3(.3, -1, .5);
                Vec3 dir = world(lift(local.normalize(), beat), Vec3.ZERO, side, f, w, 1).normalize();
                double length = (primary ? .8 + .9 * u : .45 + .25 * u) * s * grow * (1 - burn * .7)
                        * (.92 + .08 * Math.sin(time * .7 + k + w));
                if (length < .02) continue;
                Vec3 center = base.add(dir.scale(length * .5));
                double width = (primary ? .2 - .06 * u : .14) * s;
                card(p, flame, center, facingSide(dir, camera.subtract(center)), dir, width, length * .5, FLAME,
                        seed + 40 + k + w * 13, WHITE, body);
                // Embers shed from the primaries as the wings beat and burn.
                if (primary && age > SPREAD_END && k % 2 == 0) {
                    double cycle = (age * .06 + k * .13 + (w + 1) * .21) % 1;
                    Vec3 ember = base.add(dir.scale(length)).add(f.scale(-cycle * .2)).add(0, -cycle * 1.4, 0);
                    card(p, flame, ember, right, up, .05 * s, .05 * s, EMBER, seed + 70 + k, WHITE, alpha * (float) (1 - cycle));
                }
            }
            // As it folds the wing goes to smoke.
            if (burn > 0)
                for (int i = 0; i < 3; i++) {
                    Vec3 at = bezier(bone[0], bone[1], bone[2], bone[3], .35 + i * .25).add(0, burn * (.4 + i * .2), 0);
                    card(p, flame, at, right, up, (.35 + burn * .3) * s, (.5 + burn * .4) * s, SMOKE, seed + 90 + i + w * 3, WHITE, alpha * burn * .55f);
                }
        }
    }

    private static void ready(PoseStack p, VertexConsumer flame, VertexConsumer molten, Vec3 camera, Vec3 right, Vec3 up,
            double time, float alpha, Frame frame, int seed) {
        double s = frame.scale();
        Vec3 f = vec(HematicRiposteRules.forward(frame.bodyYaw())), side = vec(HematicRiposteRules.right(frame.bodyYaw()));
        heart(p, molten, f, s, frame.crouching(), time, seed, alpha, .07);
        if (frame.firstPerson()) return;
        Vec3 chest = chest(f, s, frame.crouching());
        for (int i = -1; i <= 1; i += 2) {
            double h = .14 * s * (.8 + .2 * Math.sin(time * .9 + i * 2 + seed));
            card(p, flame, chest.add(side.scale(i * .04 * s)).add(0, h * .75 + .04 * s, 0), right, UP, .05 * s, h, FLAME,
                    seed + 5 + i, WHITE, alpha * .85f);
        }
        // Folded wings, banked like coals down the back.
        Vec3 root = UP.scale((frame.crouching() ? 1.12 : 1.35) * s).add(f.scale(-.2 * s));
        for (int w = -1; w <= 1; w += 2) {
            Vec3 shoulder = root.add(side.scale(w * .12 * s));
            List<Vec3> spine = new ArrayList<>(9);
            Vec3[] bone = new Vec3[4];
            for (int j = 0; j < 4; j++) bone[j] = world(joint(j, 0, 0), shoulder, side, f, w, s);
            for (int i = 0; i <= 8; i++) spine.add(bezier(bone[0], bone[1], bone[2], bone[3], i / 8.0));
            ribbonPath(p, molten, spine, camera, .04 * s, .012 * s, MOLTEN_POOL, seed + 10 + w, WHITE, alpha * .35f);
        }
    }

    private static void heart(PoseStack p, VertexConsumer molten, Vec3 f, double s, boolean crouching, double time, int seed,
            float alpha, double size) {
        if (alpha <= 0) return;
        double beat = 1 + .12 * Math.pow(Math.max(0, Math.sin(time * .35 + seed)), 6);
        Vec3 at = chest(f, s, crouching);
        p.pushPose();
        p.translate(at.x, at.y, at.z);
        p.mulPose(Axis.YP.rotation((float) (time * .01 + seed)));
        ThermalGeometry.chunk(p, molten, size * s * beat, seed, alpha, CRUST);
        p.popPose();
    }

    private static Vec3 chest(Vec3 f, double s, boolean crouching) {
        return UP.scale((crouching ? .85 : 1.05) * s).add(f.scale(.14 * s));
    }

    /** Maps wing space onto the body: x along the wing's side, y up, z behind the wearer. */
    private static Vec3 world(Vec3 local, Vec3 origin, Vec3 side, Vec3 forward, int wing, double scale) {
        return origin.add(side.scale(wing * local.x * scale)).add(UP.scale(local.y * scale)).add(forward.scale(-local.z * scale));
    }

    private static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double t) {
        double u = 1 - t;
        return a.scale(u * u * u).add(b.scale(3 * u * u * t)).add(c.scale(3 * u * t * t)).add(d.scale(t * t * t));
    }

    private static Vec3 vec(double[] xyz) {
        return new Vec3(xyz[0], xyz[1], xyz[2]);
    }
}
