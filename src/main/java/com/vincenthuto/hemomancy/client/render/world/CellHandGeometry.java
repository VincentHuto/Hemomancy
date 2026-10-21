package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ferric.CellHandRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The cell hands of Blood Projection and Blood Absorption. Summoned, the forearm's veins burst into strands that lash
 * out, curl back and coil round the hand until a shell closes and sets with a flash: projection sets with jets
 * pointing out of the palm, absorption with hooks curled round an open palm. Dispelled, the shell unravels and its
 * blood slides back down the forearm. In use, projection spurts sagging ropes of blood from the palm with each beat,
 * shedding drops, while absorption reaches out in fine forking capillaries that beads of blood creep back along.
 * Points are world-axis offsets from the caster's feet.
 */
final class CellHandGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final Vec3 UP = CasterFrame.UP;

    private CellHandGeometry() {}

    static boolean handles(Form form) {
        return form == Form.CELL_HAND;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, CasterFrame frame) {
        draw(packet, p, v, camera, right, up, time, age, alpha, frame, List.of());
    }

    /**
     * {@code targets} are what the hand is drawing from or pouring into this frame, as offsets from the caster's feet,
     * resolved the same way as the cell particles; with none, the hand reaches out a short way instead.
     */
    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, CasterFrame frame, List<Vec3> targets) {
        draw(packet, p, v, camera, right, up, time, age, alpha, frame, targets, null);
    }

    /**
     * With {@code feet}, the caster's world position, the streams carry weight: they trail behind the hand as it moves
     * and swing back into place (see {@link StreamInertia}). Without it they move rigidly with the hand.
     */
    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha, CasterFrame frame, List<Vec3> targets, Vec3 feet) {
        if (alpha <= 0) return;
        int caster = packet.entityId();
        int mode = packet.count();
        int seed = Math.floorMod(packet.entityId(), 113);
        double s = frame.scale();
        Vec3 hand = frame.hand();
        // In first person the drawn palm points the way to grow: the fingers, leaning toward where the player looks.
        Vec3 look = frame.look().lengthSqr() < 1e-6 ? frame.forward() : frame.look().normalize();
        Vec3 aim = frame.fingers() == null ? look : frame.fingers().scale(0.6).add(look.scale(0.4)).normalize();
        // The forearm runs back from the palm along the drawn fingers when the first-person hand is known.
        Vec3 elbow = frame.fingers() != null ? hand.subtract(frame.fingers().scale(0.38 * s))
                : frame.firstPerson() ? hand.subtract(UP.scale(0.3 * s)).add(frame.right().scale(0.1 * s)).subtract(aim.scale(0.15 * s))
                : hand.subtract(aim.scale(0.36 * s)).subtract(UP.scale(0.18 * s));
        Vec3 arm = hand.subtract(elbow).normalize();
        Vec3 side = arm.cross(UP).lengthSqr() < 1e-6 ? frame.right() : arm.cross(UP).normalize();
        Vec3 normal = side.cross(arm).normalize();
        float a = alpha * (frame.firstPerson() ? 0.75f : 1f);
        if (CellHandRules.summon(mode)) summon(p, v, camera, right, up, time, age, a, seed, s, hand, elbow, arm, side, normal, aim,
                CellHandRules.absorption(mode), frame.firstPerson());
        else if (mode == CellHandRules.DISPEL) dispel(p, v, camera, right, up, time, age, a, seed, s, hand, elbow, side, normal);
        else if (CellHandRules.using(mode)) {
            if (targets.isEmpty()) use(p, v, camera, right, up, time, a, seed, s, hand, aim, side, CellHandRules.absorption(mode),
                    frame.firstPerson(), caster, feet);
            else if (CellHandRules.absorption(mode)) drink(p, v, camera, right, up, time, a, seed, s, hand, targets, frame.firstPerson(),
                    caster, feet);
            else pour(p, v, camera, right, up, time, a, seed, s, hand, targets.getFirst(), frame.firstPerson(), caster, feet);
        }
    }

    private static void summon(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float a,
            int seed, double s, Vec3 hand, Vec3 elbow, Vec3 arm, Vec3 side, Vec3 normal, Vec3 aim, boolean absorption, boolean firstPerson) {
        double erupt = CellHandRules.erupt(age), wrap = CellHandRules.wrap(age), settle = CellHandRules.settle(age);
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        float body = a * (float) (1 - settle * 0.9);

        // 1. Strands burst out of the forearm, lash outward, then coil round the hand.
        for (int strand = 0; strand < CellHandRules.STRANDS; strand++) {
            double along = 0.15 + 0.7 * strand / (CellHandRules.STRANDS - 1.0);
            double around = strand * 2.39996 + seed;
            Vec3 out = side.scale(Math.cos(around)).add(normal.scale(Math.sin(around)));
            Vec3 root = elbow.lerp(hand, along).add(out.scale(0.04 * s));
            List<Vec3> path = new ArrayList<>(11);
            for (int i = 0; i <= 10; i++) {
                double t = erupt * i / 10.0;
                // Lashing out: an arc away from the arm, falling back toward the hand as the strand wraps.
                Vec3 lash = root.add(out.scale(Math.sin(t * Math.PI) * 0.22 * s * (1 - wrap))).lerp(hand, t * (0.4 + 0.6 * wrap));
                double coil = t * 8 + strand + time * 0.3;
                double r = 0.07 * s * wrap * t;
                path.add(lash.add(side.scale(Math.cos(coil) * r)).add(normal.scale(Math.sin(coil) * r))
                        .add(UP.scale(settle * 0.2 * s * t)));
            }
            float strandAlpha = body * (0.8f + 0.2f * (float) Math.sin(time * 0.9 + strand * 1.7 + seed));
            ribbonPath(p, v, path, camera, 0.032 * s, 0.012 * s, RIBBON, seed + 10 + strand, TINT, strandAlpha);
            ribbonPath(p, v, path, camera, 0.009 * s, 0.003 * s, RIBBON, seed + 20 + strand, CORE, strandAlpha * 0.7f);
            card(p, v, root, right, up, 0.035 * s * (1 - wrap * 0.6), 0.035 * s * (1 - wrap * 0.6), FOCUS, seed + 30 + strand, CLOT, body);
        }

        // 2. Shell: a translucent sheath closes over the hand as the strands tighten.
        if (wrap > 0 && !firstPerson) {
            double flicker = 0.85 + 0.2 * Math.sin(time * 0.9 + seed);
            card(p, v, hand, right, up, 0.2 * s * wrap * flicker, 0.2 * s * wrap * flicker, WISP, seed + 40, TINT, body * 0.32f);
            card(p, v, hand, right, up, 0.08 * s * wrap * (1 + 0.3 * pulse), 0.08 * s * wrap * (1 + 0.3 * pulse), FOCUS, seed + 41, TINT, body);
        }

        // 3. Set: projection grows jets out of the palm; absorption curls hooks round it.
        double flash = CellHandRules.flash(age);
        double grown = Math.min(1, Math.max(0, (age - CellHandRules.WRAP_END + 1) / 2.0));
        if (grown > 0) {
            for (int i = 0; i < (absorption ? 4 : 3); i++) {
                double around = i * Math.PI * 2 / (absorption ? 4 : 3) + seed;
                Vec3 out = side.scale(Math.cos(around)).add(normal.scale(Math.sin(around)));
                Vec3 base = hand.add(out.scale(0.06 * s));
                List<Vec3> spine;
                if (absorption) {
                    Vec3 mid = base.add(aim.scale(0.12 * s * grown)).add(out.scale(0.08 * s * grown));
                    Vec3 tip = base.add(aim.scale(0.18 * s * grown)).subtract(out.scale(0.02 * s * grown));
                    spine = List.of(base, mid, tip);
                } else {
                    spine = List.of(base, base.add(aim.scale(0.1 * s * grown)).add(out.scale(0.02 * s)),
                            hand.add(aim.scale(0.26 * s * grown)).add(out.scale(0.03 * s)));
                }
                ribbonPath(p, v, spine, camera, 0.03 * s, 0.006 * s, SLASH, seed + 50 + i, TINT, body);
                ribbonPath(p, v, spine.subList(1, 3), camera, 0.018 * s, 0.004 * s, SLASH, seed + 54 + i, CLOT, body);
                ribbonPath(p, v, spine, camera, 0.006 * s, 0.002 * s, RIBBON, seed + 58 + i, CORE, body * 0.9f);
            }
        }
        if (flash > 0 && !firstPerson) {
            card(p, v, hand, right, up, 0.3 * s * flash, 0.3 * s * flash, WISP, seed + 60, TINT, a * (float) flash * 0.5f);
            card(p, v, hand, right, up, 0.12 * s * flash, 0.12 * s * flash, FOCUS, seed + 61, CORE, a * (float) flash);
        }
    }

    private static void dispel(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float a,
            int seed, double s, Vec3 hand, Vec3 elbow, Vec3 side, Vec3 normal) {
        double unravel = CellHandRules.unravel(age);
        float body = a * (float) (1 - Math.max(0, (age - CellHandRules.DISPEL_TICKS * 0.6) / (CellHandRules.DISPEL_TICKS * 0.4)));
        // The shell bursts open, then its blood slides back down the forearm and sinks in.
        card(p, v, hand, right, up, 0.16 * s * (1 + unravel), 0.16 * s * (1 + unravel), WISP, seed, TINT, body * 0.3f * (float) (1 - unravel));
        for (int strand = 0; strand < CellHandRules.STRANDS; strand++) {
            double around = strand * 2.39996 + seed;
            Vec3 out = side.scale(Math.cos(around)).add(normal.scale(Math.sin(around)));
            Vec3 head = hand.lerp(elbow, unravel * (0.5 + 0.1 * strand)).add(out.scale(0.05 * s));
            List<Vec3> path = new ArrayList<>(7);
            for (int i = 0; i <= 6; i++) {
                double t = i / 6.0;
                double loosen = Math.sin(t * Math.PI) * (0.12 - 0.1 * unravel) * s;
                path.add(hand.add(out.scale(0.08 * s)).lerp(head, t).add(out.scale(loosen)));
            }
            ribbonPath(p, v, path, camera, 0.025 * s, 0.01 * s, RIBBON, seed + 10 + strand, TINT, body);
            ribbonPath(p, v, path, camera, 0.007 * s, 0.003 * s, RIBBON, seed + 20 + strand, CORE, body * 0.6f);
            // Drops shed from the loosening strands and fall.
            Vec3 drop = hand.lerp(head, 0.5).add(out.scale(0.1 * s)).add(UP.scale(-unravel * (0.25 + 0.05 * strand) * s));
            card(p, v, drop, right, up, 0.022 * s, 0.035 * s, FOCUS, seed + 30 + strand, CLOT, body * (float) (1 - unravel * 0.5));
        }
    }

    /** Stations along a stream: enough for bulges to travel smoothly. */
    private static final int STREAM = 24;

    /**
     * Absorption with something to drain: blood wells out of each target where fine capillaries root into it, gathers
     * into a stream that swells as it fills on its way to the palm, and slugs of blood pulse along it into the hand.
     */
    private static void drink(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float a, int seed,
            double s, Vec3 palm, List<Vec3> targets, boolean firstPerson, int caster, Vec3 feet) {
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        int count = Math.min(4, targets.size());
        for (int k = 0; k < count; k++) {
            Vec3 target = targets.get(k);
            Vec3 span = palm.subtract(target);
            double length = span.length();
            if (length < 0.05) continue;
            Vec3 dir = span.scale(1 / length);
            Vec3 side = dir.cross(UP).lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : dir.cross(UP).normalize();
            Vec3 normal = side.cross(dir).normalize();
            int ks = seed + k * 41;
            // The stream runs target -> palm: it leaves just off the body's surface and sags a little on the way.
            Vec3 from = target.add(dir.scale(Math.min(0.12, length * 0.2) * s));
            List<Vec3> path = new ArrayList<>(STREAM + 1);
            double[] widths = new double[STREAM + 1];
            for (int i = 0; i <= STREAM; i++) {
                double t = i / (double) STREAM, bow = Math.sin(t * Math.PI);
                double sway = Math.sin(t * 5 + time * 0.09 + k) * 0.05 * bow * Math.min(1.5, length) * s;
                path.add(from.lerp(palm, t).add(side.scale(sway)).add(UP.scale(-0.08 * bow * Math.min(2, length))));
                // Thin where it is drawn out of the body, swelling as it fills toward the hand, pinched at the palm.
                widths[i] = (0.022 + 0.05 * t * t) * s * (1 - 0.35 * Math.pow(t, 12));
            }
            // Held at the body and the palm, the slung middle trails as either end moves.
            StreamInertia.apply(caster, k, path, slung(), feet, time, s);
            fluid(p, v, path, camera, right, up, widths, time, 0.09, ks, a, s);
            whirl(p, v, path, camera, right, up, widths, time, 0.09, ks + 60, a, s);
            // A thinner second stream braids round the first, so the flow never reads as a single wire.
            List<Vec3> braid = new ArrayList<>(STREAM + 1);
            double[] thin = new double[STREAM + 1];
            for (int i = 0; i <= STREAM; i++) {
                double t = i / (double) STREAM, twist = t * 7 - time * 0.2 + k;
                double r = 0.035 * Math.sin(t * Math.PI) * s;
                braid.add(path.get(i).add(side.scale(Math.cos(twist) * r)).add(normal.scale(Math.sin(twist) * r)));
                thin[i] = widths[i] * 0.45;
            }
            fluid(p, v, braid, camera, right, up, thin, time, 0.11, ks + 20, a * 0.7f, s);
            // Where it is drawn out: capillaries root into the body and blood wells from it.
            for (int root = 0; root < 4; root++) {
                double angle = root * Math.PI / 2 + ks + Math.sin(time * 0.05 + root) * 0.3;
                Vec3 tip = target.add(side.scale(Math.cos(angle) * 0.13 * s)).add(normal.scale(Math.sin(angle) * 0.13 * s))
                        .subtract(dir.scale(0.04 * s));
                ribbonPath(p, v, List.of(from, from.lerp(tip, 0.5).add(normal.scale(0.015 * s)), tip), camera, 0.018 * s, 0.003 * s,
                        RIBBON, ks + 40 + root, TINT, a * 0.85f);
            }
            double well = (0.07 + 0.025 * Math.sin(time * 0.3 + k)) * s;
            card(p, v, from, right, up, well * 2, well * 2, WISP, ks + 45, TINT, a * 0.3f);
            card(p, v, from, right, up, well, well, FOCUS, ks + 46, CLOT, a * 0.9f);
        }
        palm(p, v, right, up, palm, s, seed, a, pulse, true, firstPerson);
    }

    /**
     * Projection with a landing: a heavy stream spurts from the palm in a thrown arc, slugs of blood pulsing along it
     * with each beat; near the end it breaks into falling drops and splashes, crowning and pooling, where it lands.
     */
    private static void pour(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float a, int seed,
            double s, Vec3 palm, Vec3 target, boolean firstPerson, int caster, Vec3 feet) {
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        Vec3 span = target.subtract(palm);
        double length = span.length();
        if (length < 0.05) return;
        Vec3 dir = span.scale(1 / length);
        Vec3 side = dir.cross(UP).lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : dir.cross(UP).normalize();
        Vec3 normal = side.cross(dir).normalize();
        double lift = Math.min(0.8, length * 0.18);
        // The stream breaks up before it lands: its body runs to here, then drops fall the rest of the way.
        double breakUp = 0.86;
        List<Vec3> arc = new ArrayList<>(STREAM + 1);
        double[] widths = new double[STREAM + 1];
        for (int i = 0; i <= STREAM; i++) {
            double t = breakUp * i / STREAM;
            double sway = Math.sin(t * 5 - time * 0.3) * 0.02 * Math.sin(t * Math.PI) * s;
            arc.add(point(palm, target, lift, t).add(side.scale(sway)));
            // Pressured at the palm, thinning as it stretches through the air.
            widths[i] = (0.075 - 0.04 * t) * s * (1 + 0.3 * pulse * (1 - t));
        }
        // Blood already thrown keeps going the way it was thrown: the arc swings after the palm and the landing.
        StreamInertia.apply(caster, 8, arc, slung(), feet, time, s);
        fluid(p, v, arc, camera, right, up, widths, time, 0.12, seed + 20, a, s);
        whirl(p, v, arc, camera, right, up, widths, time, 0.12, seed + 80, a, s);
        // A thinner stream peels off the main one and runs alongside.
        List<Vec3> peel = new ArrayList<>(STREAM + 1);
        double[] thin = new double[STREAM + 1];
        for (int i = 0; i <= STREAM; i++) {
            double t = i / (double) STREAM;
            double apart = Math.sin(t * Math.PI) * 0.05 * s;
            peel.add(arc.get(i).add(side.scale(apart * Math.cos(time * 0.07))).add(normal.scale(apart * 0.6)));
            thin[i] = widths[i] * 0.4;
        }
        fluid(p, v, peel, camera, right, up, thin, time, 0.15, seed + 40, a * 0.65f, s);
        // Break-up: the end of the stream falls as separate drops, falling faster the further they go.
        for (int drop = 0; drop < 5; drop++) {
            double phase = (time * 0.09 + drop / 5.0) % 1;
            double t = breakUp + (1 - breakUp) * phase;
            Vec3 at = point(palm, target, lift, t).add(side.scale(Math.sin(drop * 2.1 + seed) * 0.03 * s));
            double size = (0.034 - 0.01 * phase) * s;
            card(p, v, at, right, up, size, size * (1.2 + 0.5 * phase), FOCUS, seed + 60 + drop, drop % 2 == 0 ? TINT : CLOT, a);
        }
        // The landing: a wet splash, a crown of drops thrown up and falling back, and a spreading pool.
        float beat = 0.75f + 0.25f * pulse;
        card(p, v, target, right, up, 0.2 * s * beat, 0.14 * s * beat, WISP, seed + 70, TINT, a * 0.3f);
        card(p, v, target, right, up, 0.08 * s * beat, 0.06 * s * beat, FOCUS, seed + 71, TINT, a);
        ground(p, v, target.add(0, 0.02, 0), 0.26 * s * (0.85 + 0.15 * Math.sin(time * 0.1)), POOL, seed + 72, CLOT, a * 0.6f);
        for (int drop = 0; drop < 7; drop++) {
            double phase = (time * 0.06 + drop / 7.0) % 1;
            double angle = drop * 2.39996 + seed;
            double reach = (0.08 + 0.16 * phase) * s;
            Vec3 at = target.add(Math.cos(angle) * reach, 0.2 * s * Math.sin(phase * Math.PI), Math.sin(angle) * reach);
            card(p, v, at, right, up, 0.012 * s, 0.017 * s, FOCUS, seed + 74 + drop, CLOT, a * (float) (1 - phase * 0.8));
        }
        palm(p, v, right, up, palm, s, seed, a, pulse, false, firstPerson);
    }

    /** A point {@code t} along a thrown arc from {@code from} that rises by {@code lift} and lands on {@code to}. */
    private static Vec3 point(Vec3 from, Vec3 to, double lift, double t) {
        return from.lerp(to, t).add(UP.scale(lift * 4 * t * (1 - t)));
    }

    /**
     * The Riposte-style whirl round a stream: two translucent strands spiral about it, turning and travelling with the
     * flow, with flickering wisps strung along them that trail back against it, so the stream reads as moving.
     */
    private static void whirl(PoseStack p, VertexConsumer v, List<Vec3> path, Vec3 camera, Vec3 right, Vec3 up, double[] base,
            double time, double speed, int seed, float alpha, double s) {
        int n = path.size();
        if (n < 3) return;
        for (int strand = 0; strand < 2; strand++) {
            List<Vec3> helix = new ArrayList<>(n);
            double[] widths = new double[n];
            for (int i = 0; i < n; i++) {
                double t = i / (n - 1.0);
                Vec3 tangent = path.get(Math.min(n - 1, i + 1)).subtract(path.get(Math.max(0, i - 1)));
                Vec3 a1 = tangent.cross(UP).lengthSqr() < 1e-8 ? new Vec3(1, 0, 0) : tangent.cross(UP).normalize();
                Vec3 a2 = a1.cross(tangent).normalize();
                // Turns about two and a half times along the stream and travels with the flow.
                double angle = Math.PI * 2 * (t * 2.5 - time * speed * 0.8) + strand * Math.PI + seed;
                // Flares out from the stream in the middle, tucked in at both ends.
                double radius = (base[i] * 2.4 + 0.035 * s) * Math.sin(t * Math.PI);
                helix.add(path.get(i).add(a1.scale(Math.cos(angle) * radius)).add(a2.scale(Math.sin(angle) * radius)));
                widths[i] = (base[i] * 0.45 + 0.004 * s) * Math.sin(t * Math.PI);
            }
            ribbonPath(p, v, helix, camera, widths, RIBBON, seed + strand, TINT, alpha * 0.5f);
            // Wisps along the whirl, flickering and trailing back against the flow like the Riposte sheath.
            for (int i = 2; i < n - 2; i += 3) {
                double t = i / (n - 1.0);
                Vec3 back = path.get(i - 1).subtract(path.get(i + 1)).normalize();
                double flicker = 0.85 + 0.25 * Math.sin(time * 0.9 + i * 1.7 + seed + strand);
                double w = (base[i] * 2.2 + 0.02 * s) * flicker * Math.sin(t * Math.PI);
                card(p, v, helix.get(i).add(back.scale(w * 0.6)), right, up, w, w * 1.25, WISP, seed + 10 + i + strand * 40, TINT,
                        alpha * 0.3f);
            }
        }
    }

    /**
     * A stream of liquid blood along {@code path}, layered like the Riposte arm: a glossy translucent sheath, a body that
     * bulges with slugs of blood travelling along it toward the end of the path at {@code speed}, a dark underside where
     * the blood pools under its own weight, and a pale wet highlight along the top.
     */
    private static void fluid(PoseStack p, VertexConsumer v, List<Vec3> path, Vec3 camera, Vec3 right, Vec3 up, double[] base,
            double time, double speed, int seed, float alpha, double s) {
        int n = path.size();
        double[] body = new double[n], sheath = new double[n], under = new double[n], shine = new double[n];
        List<Vec3> low = new ArrayList<>(n), high = new ArrayList<>(n);
        double[] slug = new double[n];
        for (int i = 0; i < n; i++) {
            double t = i / (n - 1.0);
            // Slugs: smooth swellings three or four to a stream, moving with the flow.
            double wave = Math.sin((t * 3.5 - time * speed + seed * 0.37) * Math.PI * 2);
            slug[i] = Math.pow(Math.max(0, wave), 3);
            double w = base[i] * (1 + 0.9 * slug[i]);
            body[i] = w;
            sheath[i] = w * 2.0 + 0.006 * s;
            under[i] = w * 0.55;
            shine[i] = w * 0.22;
            low.add(path.get(i).add(UP.scale(-w * 0.35)));
            high.add(path.get(i).add(UP.scale(w * 0.4)));
        }
        ribbonPath(p, v, path, camera, sheath, WISP, seed, TINT, alpha * 0.28f);
        ribbonPath(p, v, path, camera, body, RIBBON, seed + 1, TINT, alpha * 0.95f);
        ribbonPath(p, v, low, camera, under, RIBBON, seed + 2, CLOT, alpha * 0.85f);
        ribbonPath(p, v, high, camera, shine, RIBBON, seed + 3, CORE, alpha * 0.55f);
        // Each slug's crest catches the light as a glossy bead inside the stream.
        for (int i = 1; i < n - 1; i++)
            if (slug[i] > slug[i - 1] && slug[i] >= slug[i + 1] && slug[i] > 0.6)
                card(p, v, path.get(i), right, up, body[i] * 1.3, body[i] * 1.3, FOCUS, seed + 10 + i, TINT, alpha * 0.8f);
    }

    /** Slack for a stream held at both ends: pinned at each, loosest in the middle. */
    private static double[] slung() {
        double[] slack = new double[STREAM + 1];
        for (int i = 0; i <= STREAM; i++) slack[i] = Math.sin(Math.PI * i / STREAM);
        return slack;
    }

    /** Slack for a stream held at one end only: pinned at the start ({@code fromStart}) or the end, loosest at the other. */
    private static double[] trailing(boolean fromStart) {
        double[] slack = new double[STREAM + 1];
        for (int i = 0; i <= STREAM; i++) {
            double u = i / (double) STREAM;
            slack[i] = Math.pow(fromStart ? u : 1 - u, 0.8);
        }
        return slack;
    }

    /** The palm: a wet knot that gulps (absorbing) or swells (projecting) with each beat. */
    private static void palm(PoseStack p, VertexConsumer v, Vec3 right, Vec3 up, Vec3 palm, double s, int seed, float a, float pulse,
            boolean absorbing, boolean firstPerson) {
        double knot = 0.07 * s * (1 + (absorbing ? -0.25 : 0.4) * pulse);
        card(p, v, palm, right, up, knot, knot, FOCUS, seed + 50, TINT, a);
        // The soft glow turns blocky when magnified right in front of the camera.
        if (!firstPerson) card(p, v, palm, right, up, 0.15 * s, 0.15 * s, WISP, seed + 51, TINT, a * 0.25f);
    }

    /**
     * In use with nothing in reach: projection spurts a short heavy stream that sags out of the palm under its own weight
     * and breaks into falling drops; absorption sends fine fluid feelers out of the fingertips, groping for something
     * to drain, with slugs of blood drawing back along them.
     */
    private static void use(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, double time, float a, int seed,
            double s, Vec3 hand, Vec3 aim, Vec3 side, boolean absorption, boolean firstPerson, int caster, Vec3 feet) {
        Vec3 normal = side.cross(aim).normalize();
        Vec3 palm = hand.add(aim.scale(0.08 * s));
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        if (absorption) {
            for (int feeler = 0; feeler < 3; feeler++) {
                double spread = feeler - 1;
                Vec3 out = aim.add(side.scale(spread * 0.3)).add(normal.scale(0.1 * Math.sin(seed + feeler * 1.9))).normalize();
                double reach = (0.5 + 0.15 * Math.sin(time * 0.05 + feeler * 2.3)) * s;
                List<Vec3> path = new ArrayList<>(STREAM + 1);
                double[] widths = new double[STREAM + 1];
                for (int i = 0; i <= STREAM; i++) {
                    // Runs tip -> palm, so the slugs travel into the hand.
                    double t = 1 - i / (double) STREAM;
                    double grope = Math.sin(t * 6 + time * 0.14 + feeler * 1.7) * 0.06 * t * s;
                    path.add(palm.add(out.scale(reach * t)).add(side.scale(grope)).add(normal.scale(Math.cos(t * 4 + time * 0.1) * 0.03 * t * s)));
                    widths[i] = (0.008 + 0.03 * (1 - t) * (1 - t)) * s;
                }
                // Rooted in the fingertips, the groping tips whip after the hand.
                StreamInertia.apply(caster, 12 + feeler, path, trailing(false), feet, time, s);
                fluid(p, v, path, camera, right, up, widths, time, 0.1, seed + 20 + feeler * 7, a * 0.9f, s);
                whirl(p, v, path, camera, right, up, widths, time, 0.1, seed + 90 + feeler * 7, a * 0.6f, s);
            }
        } else {
            double breakUp = 0.8;
            List<Vec3> path = new ArrayList<>(STREAM + 1);
            double[] widths = new double[STREAM + 1];
            double reach = (0.55 + 0.2 * pulse) * s;
            for (int i = 0; i <= STREAM; i++) {
                double t = breakUp * i / STREAM;
                double sway = Math.sin(t * 5 - time * 0.4) * 0.02 * t * s;
                path.add(palm.add(aim.scale(reach * t)).add(UP.scale(-0.45 * s * t * t)).add(side.scale(sway)));
                widths[i] = (0.06 - 0.035 * t) * s * (1 + 0.3 * pulse * (1 - t));
            }
            // Like a hose: the spurt leaves the palm where it points, and the rope swings after it.
            StreamInertia.apply(caster, 16, path, trailing(true), feet, time, s);
            fluid(p, v, path, camera, right, up, widths, time, 0.12, seed + 20, a, s);
            whirl(p, v, path, camera, right, up, widths, time, 0.12, seed + 80, a, s);
            Vec3 end = path.getLast(), fall = aim.scale(0.2 * s).add(UP.scale(-0.5 * s));
            for (int drop = 0; drop < 4; drop++) {
                double phase = (time * 0.09 + drop / 4.0) % 1;
                Vec3 at = end.add(fall.scale(phase)).add(UP.scale(-0.25 * s * phase * phase)).add(side.scale(Math.sin(drop * 2.1) * 0.02 * s));
                double size = (0.03 - 0.008 * phase) * s;
                card(p, v, at, right, up, size, size * (1.2 + 0.5 * phase), FOCUS, seed + 60 + drop, drop % 2 == 0 ? TINT : CLOT,
                        a * (float) (1 - phase * 0.6));
            }
        }
        palm(p, v, right, up, palm, s, seed, a, pulse, absorption, firstPerson);
    }
}
