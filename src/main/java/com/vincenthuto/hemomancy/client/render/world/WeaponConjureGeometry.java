package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.card;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.ribbonPath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ferric.WeaponConjureRules;
import com.vincenthuto.hemomancy.common.manipulation.ferric.WeaponConjureRules.School;
import com.vincenthuto.hemomancy.common.manipulation.ferric.WeaponConjureRules.Stroke;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The Living Arsenal's shared conjure: blood hardening into a weapon. Strands leave the forearm and coil into a knot at
 * the palm; fresh blood then sketches the chosen weapon's outline in the air above the hand, stroke after stroke;
 * a pale sweep runs along it as it hardens into the weapon's school; and the hardened outline is drawn down into the
 * grip with a flash as the real item forms, shedding drops. One class serves every weapon: only the strokes from
 * {@link WeaponConjureRules} and the school material change. Points are world-axis offsets from the caster's feet.
 */
final class WeaponConjureGeometry {
    private static final Vec3 UP = CasterFrame.UP;
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final int NERVE = 0xFFE8A0, BLOOD = 0x9C263B;

    /** How a school draws an outline: shader shapes for line, glow and knot, and its body/core/clot colours. */
    private record Look(LuxUmbraBatch.Material material, int line, int glow, int knot, int body, int core, int clot) {}

    private static final Look FRESH = new Look(LuxUmbraBatch.Material.ANIMUS, LuxUmbraGeometry.RIBBON, LuxUmbraGeometry.WISP,
            LuxUmbraGeometry.FOCUS, TINT, CORE, CLOT);

    private WeaponConjureGeometry() {}

    static boolean handles(Form form) {
        return form == Form.WEAPON_CONJURE;
    }

    private static Look look(School school) {
        return switch (school) {
            case FERRIC -> new Look(LuxUmbraBatch.Material.DUCTILIS, 4, 1, 3, BLOOD, CORE, BLOOD);
            case DUCTILIS -> new Look(LuxUmbraBatch.Material.DUCTILIS, 0, 1, 3, NERVE, CORE, BLOOD);
            case LUX -> new Look(LuxUmbraBatch.Material.LUX, LuxUmbraGeometry.RIBBON, LuxUmbraGeometry.WISP, LuxUmbraGeometry.FOCUS,
                    LuxUmbraGeometry.WHITE, LuxUmbraGeometry.GOLD, LuxUmbraGeometry.CRIMSON);
            case UMBRA -> new Look(LuxUmbraBatch.Material.UMBRA, LuxUmbraGeometry.RIBBON, LuxUmbraGeometry.WISP, LuxUmbraGeometry.FOCUS,
                    TINT, CORE, CLOT);
            case MORTEM -> new Look(LuxUmbraBatch.Material.MORTEM, LuxUmbraGeometry.RIBBON, LuxUmbraGeometry.WISP, LuxUmbraGeometry.FOCUS,
                    TINT, CORE, CLOT);
            // Cruor's soft card reads as boxy powder up close; its frost strand serves as the glow as well.
            case CRUOR -> new Look(LuxUmbraBatch.Material.CRUOR, ThermalGeometry.FROST_STRAND, ThermalGeometry.FROST_STRAND,
                    ThermalGeometry.FROST_STRAND, TINT, CORE, CLOT);
            // The torch's haft is darkened blood; its flame is drawn separately.
            case FLAME, ANIMUS -> FRESH;
        };
    }

    /**
     * The outline's plane: {@code a} raised up and forward from the hand, {@code b} across it. In first person it leans
     * into the view's depth instead, so it never crosses the screen.
     */
    static Vec3[] plane(CasterFrame frame) {
        Vec3 look = frame.look().lengthSqr() < 1e-6 ? frame.forward() : frame.look();
        Vec3 a = frame.firstPerson() ? look.scale(0.9).add(UP.scale(0.4)).normalize() : look.scale(0.55).add(UP.scale(0.85)).normalize();
        Vec3 b = a.cross(UP).lengthSqr() < 1e-6 ? frame.right() : a.cross(UP).normalize();
        return new Vec3[] { a, b };
    }

    /** The conjured weapon as actually drawn this frame: its faces and grip, as offsets from the caster's feet. */
    record Held(List<Vec3[]> faces, Vec3 grip) {}

    static void draw(ManipulationVisualPacket packet, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron, VertexConsumer core,
            boolean solids, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha, CasterFrame frame) {
        draw(packet, p, flows, iron, core, solids, camera, right, up, time, age, alpha, frame, null, null);
    }

    /**
     * Draws the conjure. With the held weapon known ({@code main}, plus {@code off} for the paired claws) the blood
     * hardens on the weapon's own faces in the hand that holds it; otherwise (remote casters not yet drawn, echo casts)
     * it sketches the authored outline above the hand.
     */
    static void draw(ManipulationVisualPacket packet, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron, VertexConsumer core,
            boolean solids, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha, CasterFrame frame,
            Held main, Held off) {
        if (alpha <= 0) return;
        int weapon = WeaponConjureRules.decodeWeapon(packet.count());
        int seed = Math.floorMod(packet.entityId(), 113) + 17 * WeaponConjureRules.decodeVariant(packet.count());
        School school = WeaponConjureRules.school(weapon);
        if (main != null) {
            if (solids) return;
            float a = alpha * (frame.firstPerson() ? 0.85f : 1f);
            onWeapon(weapon, school, seed, main, p, flows, camera, right, up, time, age, a, frame);
            if (off != null) onWeapon(weapon, school, seed + 50, off, p, flows, camera, right, up, time, age, a, frame);
            return;
        }
        // In first person the outline floats in the lower view; keep it lighter so it never walls the screen off.
        float a = alpha * (frame.firstPerson() ? 0.7f : 1f);
        draw(weapon, school, seed, frame.hand(), p, flows, iron, core, solids, camera, right, up, time, age, a, frame);
        if (weapon == WeaponConjureRules.CLAWS) {
            // The baghnakh pairs: the off hand mirrors the main hand across the body.
            Vec3 side = frame.right();
            Vec3 offHand = frame.hand().subtract(side.scale(2 * frame.hand().dot(side)));
            draw(weapon, school, seed + 50, offHand, p, flows, iron, core, solids, camera, right, up, time, age, a, frame);
        }
    }

    /**
     * Blood hardening on the held weapon itself: strands leave the forearm for the grip, fresh blood spreads over the
     * weapon's faces outward from the grip, a pale sweep hardens it into the school, and the skin sinks into the real
     * item as it forms, shedding drops from the far end.
     */
    private static void onWeapon(int weapon, School school, int seed, Held held, PoseStack p, LuxUmbraBatch flows, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float a, CasterFrame frame) {
        double s = frame.scale();
        Vec3 grip = held.grip();
        double sketch = WeaponConjureRules.spread(age), harden = WeaponConjureRules.harden(age), fray = WeaponConjureRules.fray(age);
        double settle = 1 - (WeaponConjureRules.seat(age) - 0.25) / 0.75;
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        Look hard = look(school);
        VertexConsumer fresh = flows.vertices(FRESH.material()), schoolV = flows.vertices(hard.material());
        float skin = a * (float) (1 - 0.8 * settle) * (float) (1 - fray);

        // Centroid and reach of the weapon, measured from the grip.
        Vec3 centroid = Vec3.ZERO;
        double reach = 1e-3;
        List<Vec3> centers = new ArrayList<>(held.faces().size());
        for (Vec3[] face : held.faces()) {
            Vec3 c = face[0].add(face[1]).add(face[2]).add(face[3]).scale(0.25);
            centers.add(c);
            centroid = centroid.add(c);
            reach = Math.max(reach, c.distanceTo(grip));
        }
        if (centers.isEmpty()) return;
        centroid = centroid.scale(1.0 / centers.size());

        // 1. Gather: strands leave the forearm and coil into the grip.
        Vec3 look = frame.look().lengthSqr() < 1e-6 ? frame.forward() : frame.look();
        Vec3 elbow = frame.firstPerson() ? grip.subtract(UP.scale(0.28 * s)).add(frame.right().scale(0.08 * s)).subtract(look.scale(0.1 * s))
                : grip.subtract(look.scale(0.32 * s)).subtract(UP.scale(0.22 * s));
        Vec3 armDir = grip.subtract(elbow).normalize();
        Vec3 side = armDir.cross(UP).lengthSqr() < 1e-6 ? frame.right() : armDir.cross(UP).normalize();
        Vec3 normal = side.cross(armDir).normalize();
        double gather = WeaponConjureRules.gather(age);
        float feed = a * (float) (age < WeaponConjureRules.SKETCH_END ? 1 : Math.max(0, 1 - (age - WeaponConjureRules.SKETCH_END) / 3));
        if (feed > 0) {
            for (int strand = 0; strand < 3; strand++) {
                List<Vec3> path = new ArrayList<>(10);
                for (int i = 0; i <= 9; i++) {
                    double t = gather * i / 9.0;
                    double angle = t * 7 + strand * Math.PI * 2 / 3 + time * 0.25;
                    double r = 0.045 * s * (1 - t * 0.6);
                    path.add(elbow.lerp(grip, t).add(side.scale(Math.cos(angle) * r)).add(normal.scale(Math.sin(angle) * r)));
                }
                ribbonPath(p, fresh, path, camera, 0.026 * s, 0.01 * s, FRESH.line(), seed + strand, TINT, feed * 0.9f);
                ribbonPath(p, fresh, path, camera, 0.007 * s, 0.003 * s, FRESH.line(), seed + 3 + strand, CORE, feed * 0.7f);
            }
            double knot = 0.06 * s * gather * (1 + 0.4 * pulse);
            card(p, fresh, grip, right, up, knot, knot, FRESH.knot(), seed + 7, TINT, feed);
        }

        // 2. Skin: fresh blood spreads over the faces outward from the grip, then hardens into the school.
        for (int i = 0; i < centers.size(); i++) {
            Vec3[] face = held.faces().get(i);
            Vec3 c = centers.get(i);
            double t = c.distanceTo(grip) / reach;
            double shown = Math.max(0, Math.min(1, (sketch * 1.15 - t) / 0.15));
            if (shown <= 0) continue;
            Vec3 n = face[1].subtract(face[0]).cross(face[3].subtract(face[0]));
            n = n.lengthSqr() < 1e-12 ? Vec3.ZERO : n.normalize();
            if (n.dot(c.subtract(centroid)) < 0) n = n.scale(-1);
            // Lift the skin just off the real faces so it never fights them for depth.
            Vec3[] body = inflate(face, c, 1.02, n.scale(0.004 * s));
            float reveal = skin * (float) shown;
            int sd = seed + 20 + i;
            if (harden < 1) LuxUmbraGeometry.surface(p, fresh, body[0], body[1], body[2], body[3], FRESH_SKIN, sd, TINT,
                    reveal * (float) (1 - harden) * 0.85f);
            if (harden > 0) LuxUmbraGeometry.surface(p, schoolV, body[0], body[1], body[2], body[3], skin(school), sd, hard.body(),
                    reveal * (float) harden * 0.8f);
            // The pale hardening sweep runs out from the grip.
            if (harden > 0 && harden < 1 && Math.abs(t - harden) < 0.09)
                LuxUmbraGeometry.surface(p, schoolV, body[0], body[1], body[2], body[3], hard.line(), sd + 1, hard.core(), reveal);
            // A soft shell around the leading edge while the blood is still spreading.
            // Third person only: magnified right in front of the camera these swell into ovals.
            if (shown < 1 && i % 2 == 0 && !frame.firstPerson()) {
                Vec3[] shell = inflate(face, c, 1.35, n.scale(0.02 * s));
                LuxUmbraGeometry.surface(p, fresh, shell[0], shell[1], shell[2], shell[3], FRESH.glow(), sd + 2, TINT, skin * 0.3f);
            }
        }

        // 3. The far end: the torch catches.
        Vec3 tip = grip;
        double far = -1;
        for (Vec3 c : centers) if (c.distanceTo(grip) > far) { far = c.distanceTo(grip); tip = c; }
        if (weapon == WeaponConjureRules.TORCH && harden > 0) {
            VertexConsumer flame = flows.vertices(LuxUmbraBatch.Material.FLAME);
            for (int i = 0; i < 3; i++)
                ThermalGeometry.flameRibbon(p, flame, ThermalGeometry.tongue(tip.add(side.scale((i - 1) * 0.03 * s)),
                        side.scale((i - 1) * 0.15).add(UP.scale(0.3)), (0.2 + 0.05 * (i % 2)) * s * harden, time, seed + 90 + i),
                        camera, 0.05 * s, seed + 90 + i, a * 0.9f);
        }

        // 4. Seat: a flash at the grip as the real item forms, then drops shed from the far end.
        double flash = Math.sin(Math.max(0, Math.min(1, (age - WeaponConjureRules.HARDEN_END) / (WeaponConjureRules.SEAT_END
                - WeaponConjureRules.HARDEN_END + 1))) * Math.PI);
        if (flash > 0) {
            if (!frame.firstPerson())
                card(p, schoolV, grip, right, up, 0.2 * s * flash, 0.2 * s * flash, hard.glow(), seed + 100, hard.body(), a * (float) flash * 0.45f);
            card(p, fresh, grip, right, up, 0.07 * s * flash, 0.07 * s * flash, FRESH.knot(), seed + 101, CORE, a * (float) flash);
        }
        if (fray > 0)
            for (int i = 0; i < 5; i++) {
                Vec3 from = centers.get(Math.floorMod(i * 7919 + seed, centers.size()));
                if (from.distanceTo(grip) < reach * 0.4) from = from.lerp(tip, 0.6);
                Vec3 drip = from.add(UP.scale(-fray * (0.25 + 0.08 * i) * s));
                card(p, fresh, drip, right, up, 0.02 * s, 0.032 * s, FRESH.knot(), seed + 110 + i, CLOT, a * (float) (1 - fray));
            }
    }

    /**
     * The shader surface a school's hardened skin uses: its line band, which fills each face edge to edge. (Pool and
     * glow shapes draw a round spot per face, so a weapon's skin read as a chain of beads.)
     */
    private static int skin(School school) {
        return look(school).line();
    }

    private static final int FRESH_SKIN = LuxUmbraGeometry.RIBBON;

    private static Vec3[] inflate(Vec3[] face, Vec3 center, double scale, Vec3 lift) {
        Vec3[] out = new Vec3[4];
        for (int i = 0; i < 4; i++) out[i] = center.add(face[i].subtract(center).scale(scale)).add(lift);
        return out;
    }

    private static void draw(int weapon, School school, int seed, Vec3 hand, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron,
            VertexConsumer core, boolean solids, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float a, CasterFrame frame) {
        // First person sees the outline from just behind the hand; a smaller outline reads as the same weapon there.
        double s = frame.scale() * (frame.firstPerson() ? 0.55 : 1);
        Vec3[] plane = plane(frame);
        Vec3 axis = plane[0], across = plane[1];
        Vec3 grip = hand.add(axis.scale(0.1 * s));
        double seat = WeaponConjureRules.seat(age), harden = WeaponConjureRules.harden(age), fray = WeaponConjureRules.fray(age);
        double gather = WeaponConjureRules.gather(age);
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        Look hard = look(school);
        float body = a * (float) (1 - fray * 0.9);

        if (solids) {
            // Blood-iron plates set along the hardened Ferric cutting edges, laid flat along the edge.
            if (school != School.FERRIC || harden < 0.5) return;
            float set = (float) Math.min(1, (harden - 0.5) * 2) * body;
            Vec3 flat = axis.cross(across).normalize();
            for (Stroke stroke : WeaponConjureRules.strokes(weapon)) {
                if (!stroke.edge()) continue;
                List<Vec3> points = points(stroke, grip, axis, across, s, seat, 1);
                for (int i = 1; i < points.size(); i++) {
                    Vec3 from = points.get(i - 1), to = points.get(i);
                    double length = from.distanceTo(to);
                    if (length < 1e-4) continue;
                    p.pushPose();
                    Vec3 at = from.lerp(to, 0.5);
                    p.translate(at.x, at.y, at.z);
                    Vec3 dir = to.subtract(from).normalize();
                    // Plate local Y runs along the edge, local Z out of the weapon's flat.
                    Vec3 side = dir.cross(flat).normalize(), out = side.cross(dir);
                    p.mulPose(new org.joml.Quaternionf().setFromNormalized(new org.joml.Matrix3f(
                            (float) side.x, (float) side.y, (float) side.z, (float) dir.x, (float) dir.y, (float) dir.z,
                            (float) out.x, (float) out.y, (float) out.z)));
                    double width = Math.max(0.035, stroke.width() * 2.2) * s * seat;
                    FerricDuctilisGeometry.plate(p, iron, Vec3.ZERO, width, length * 0.9, 0.025 * s * seat, seed + 200 + i, set);
                    p.popPose();
                }
            }
            return;
        }

        VertexConsumer fresh = flows.vertices(FRESH.material());
        VertexConsumer school_ = flows.vertices(hard.material());

        // 1. Gather: strands leave the forearm and coil into a knot at the palm.
        Vec3 elbow = hand.subtract(frame.look().lengthSqr() < 1e-6 ? frame.forward().scale(0.4 * s) : frame.look().scale(0.38 * s))
                .subtract(UP.scale(0.2 * s));
        if (frame.firstPerson()) elbow = hand.subtract(UP.scale(0.25 * s)).add(frame.right().scale(0.12 * s));
        float feed = body * (float) (age < WeaponConjureRules.SKETCH_END ? 1 : Math.max(0, 1 - (age - WeaponConjureRules.SKETCH_END) / 3));
        if (feed > 0) {
            for (int strand = 0; strand < 3; strand++) {
                List<Vec3> path = new ArrayList<>(10);
                for (int i = 0; i <= 9; i++) {
                    double t = gather * i / 9.0;
                    double angle = t * 7 + strand * Math.PI * 2 / 3 + time * 0.25;
                    double r = 0.05 * s * (1 - t * 0.6);
                    path.add(elbow.lerp(hand, t).add(across.scale(Math.cos(angle) * r)).add(axis.cross(across).scale(Math.sin(angle) * r)));
                }
                ribbonPath(p, fresh, path, camera, 0.03 * s, 0.012 * s, FRESH.line(), seed + strand, TINT, feed * 0.9f);
                ribbonPath(p, fresh, path, camera, 0.008 * s, 0.003 * s, FRESH.line(), seed + 3 + strand, CORE, feed * 0.7f);
            }
            double knot = 0.08 * s * gather * (1 + 0.4 * pulse);
            // The soft glow turns blocky when magnified right in front of the camera.
            if (!frame.firstPerson()) card(p, fresh, hand, right, up, knot * 1.8, knot * 1.8, FRESH.glow(), seed + 6, TINT, feed * 0.35f);
            card(p, fresh, hand, right, up, knot, knot, FRESH.knot(), seed + 7, TINT, feed);
        }

        // 2. Sketch and harden: each stroke is drawn out in fresh blood, then turns to the weapon's school.
        Stroke[] strokes = WeaponConjureRules.strokes(weapon);
        float freshAlpha = body * (float) (1 - harden), hardAlpha = body * (float) harden;
        for (int k = 0; k < strokes.length; k++) {
            Stroke stroke = strokes[k];
            double drawn = WeaponConjureRules.sketch(age, k, strokes.length);
            if (drawn <= 0) continue;
            List<Vec3> points = points(stroke, grip, axis, across, s, seat, drawn);
            if (fray > 0) for (int i = 0; i < points.size(); i++)
                points.set(i, points.get(i).add(UP.scale(-fray * 0.25 * s * (0.5 + 0.5 * Math.sin(i * 1.7 + seed)))));
            if (points.size() < 2) continue;
            double w = stroke.width() * s * seat;
            int sd = seed + 20 + k * 6;
            // Shell, body and clotted tip in fresh blood, cross-faded into the school's look as it hardens.
            ribbonPath(p, fresh, points, camera, w * 2.4, w * 1.6, FRESH.glow(), sd, TINT, freshAlpha * 0.28f);
            ribbonPath(p, fresh, points, camera, w, w * 0.7, FRESH.line(), sd + 1, TINT, freshAlpha);
            if (hardAlpha > 0) {
                ribbonPath(p, school_, points, camera, w * 2.2, w * 1.4, hard.glow(), sd + 2, hard.body(), hardAlpha * 0.25f);
                ribbonPath(p, school_, points, camera, w, w * 0.6, hard.line(), sd + 3, hard.body(), hardAlpha);
                if (stroke.edge())
                    ribbonPath(p, school_, points, camera, w * 0.3, w * 0.15, hard.line(), sd + 4, hard.core(), hardAlpha * 0.9f);
            }
            Vec3 tip = points.getLast();
            ribbonPath(p, harden > 0.5 ? school_ : fresh, List.of(points.get(points.size() - 2).lerp(tip, 0.5), tip), camera,
                    w * 0.8, w * 0.3, harden > 0.5 ? hard.line() : FRESH.line(), sd + 5, harden > 0.5 ? hard.clot() : CLOT, body);
            // The drawing head leads each stroke with a bright bead while it is being pulled out.
            if (drawn < 1) card(p, fresh, tip, right, up, w * 2.2 + 0.02 * s, w * 2.2 + 0.02 * s, FRESH.knot(), sd, CORE, body);
            // The hardening sweep: a pale spark runs from grip to tip.
            if (harden > 0 && harden < 1) {
                Vec3 sweep = along(points, harden);
                card(p, school_, sweep, right, up, w * 2.5 + 0.03 * s, w * 2.5 + 0.03 * s, hard.knot(), sd + 3, hard.core(), body);
            }
        }

        // 3. School finish: the torch catches, the flail's chain and head hang from its tip.
        if (weapon == WeaponConjureRules.TORCH && harden > 0) {
            Vec3 head = grip.add(axis.scale(0.44 * s * seat));
            VertexConsumer flame = flows.vertices(LuxUmbraBatch.Material.FLAME);
            for (int i = 0; i < 3; i++) {
                Vec3 lean = across.scale((i - 1) * 0.15).add(UP.scale(0.3));
                ThermalGeometry.flameRibbon(p, flame, ThermalGeometry.tongue(head.add(across.scale((i - 1) * 0.03 * s)), lean,
                        (0.22 + 0.06 * (i % 2)) * s * harden * seat, time, seed + 90 + i), camera, 0.06 * s * seat, seed + 90 + i, body * 0.9f);
            }
        }
        if (weapon == WeaponConjureRules.FLAIL && harden > 0) {
            Vec3 top = grip.add(axis.scale(0.32 * s * seat));
            Vec3 drop = UP.scale(-0.42 * s * seat).add(across.scale(0.08 * s * Math.sin(time * 0.15)));
            List<Vec3> chain = new ArrayList<>(7);
            for (int i = 0; i <= 6; i++) {
                double t = i / 6.0;
                chain.add(top.add(drop.scale(t)).add(axis.scale(0.1 * s * Math.sin(t * Math.PI) * seat)));
            }
            ribbonPath(p, school_, chain, camera, 0.016 * s, hard.line(), seed + 95, hard.body(), hardAlpha);
            ThermalGeometry.clot(p, core, school_, chain.getLast(), 0.09 * s * seat * harden, seed + 96, time * 0.05, hardAlpha);
        }

        // 4. Seat: the outline is drawn into the grip with a flash as the real item forms.
        double flash = Math.sin(Math.max(0, Math.min(1, (age - WeaponConjureRules.HARDEN_END) / (WeaponConjureRules.SEAT_END
                - WeaponConjureRules.HARDEN_END + 1))) * Math.PI);
        if (flash > 0 && !frame.firstPerson()) {
            card(p, school_, grip, right, up, 0.22 * s * flash, 0.22 * s * flash, hard.glow(), seed + 100, hard.body(), a * (float) flash * 0.5f);
            card(p, fresh, grip, right, up, 0.09 * s * flash, 0.09 * s * flash, FRESH.knot(), seed + 101, CORE, a * (float) flash);
        }
        // 5. Fray: drops shed from the seated outline and fall away.
        if (fray > 0)
            for (int i = 0; i < 5; i++) {
                double angle = i * 2.39996 + seed;
                Vec3 drip = grip.add(across.scale(Math.cos(angle) * 0.12 * s)).add(axis.scale((0.05 + 0.06 * i) * s))
                        .add(UP.scale(-fray * (0.3 + 0.1 * i) * s));
                card(p, fresh, drip, right, up, 0.025 * s, 0.04 * s, FRESH.knot(), seed + 110 + i, CLOT, a * (float) (1 - fray));
            }
    }

    /** A stroke's points placed in the outline plane, scaled toward the grip by {@code seat} and cut at {@code drawn}. */
    static List<Vec3> points(Stroke stroke, Vec3 grip, Vec3 axis, Vec3 across, double scale, double seat, double drawn) {
        List<Vec3> all = new ArrayList<>(stroke.size());
        for (int i = 0; i < stroke.size(); i++)
            all.add(grip.add(axis.scale(stroke.points()[i * 2] * scale * seat)).add(across.scale(stroke.points()[i * 2 + 1] * scale * seat)));
        if (drawn >= 1) return all;
        double total = 0;
        for (int i = 1; i < all.size(); i++) total += all.get(i).distanceTo(all.get(i - 1));
        double remaining = total * drawn;
        List<Vec3> cut = new ArrayList<>(all.size());
        cut.add(all.getFirst());
        for (int i = 1; i < all.size() && remaining > 1e-6; i++) {
            double segment = all.get(i).distanceTo(all.get(i - 1));
            if (segment >= remaining) {
                cut.add(all.get(i - 1).lerp(all.get(i), remaining / Math.max(1e-9, segment)));
                break;
            }
            cut.add(all.get(i));
            remaining -= segment;
        }
        return cut;
    }

    private static Vec3 along(List<Vec3> points, double t) {
        double total = 0;
        for (int i = 1; i < points.size(); i++) total += points.get(i).distanceTo(points.get(i - 1));
        double remaining = total * t;
        for (int i = 1; i < points.size(); i++) {
            double segment = points.get(i).distanceTo(points.get(i - 1));
            if (segment >= remaining) return points.get(i - 1).lerp(points.get(i), remaining / Math.max(1e-9, segment));
            remaining -= segment;
        }
        return points.getLast();
    }
}
