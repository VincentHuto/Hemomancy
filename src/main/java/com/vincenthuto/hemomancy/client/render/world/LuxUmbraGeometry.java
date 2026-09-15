package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Unshaded surfaces carrying a shader shape and stable seed in otherwise unused UV tiles. */
final class LuxUmbraGeometry {
    static final int RIBBON = 0, WISP = 1, POOL = 2, EYE = 3, FOCUS = 4, BEAM = 5, RIPPLE = 6, SLASH = 7, WHITE_BEAM = 8, LUX_SHELL = 9;
    static final int WHITE = 0xFFFAEF;

    private LuxUmbraGeometry() {}

    static double[] slashOffsets() {
        return new double[] {-0.34D, 0.0D, 0.34D};
    }

    static boolean handles(Form form) {
        return switch (form) {
            case VERDICT, WHITE_VERDICT, VERDICT_CHARGE, BEACON, SUTURE, FLARE, EYE, WELL, WELL_CHARGE,
                    VEIL, TELEPORT, UMBRA_ARRIVAL, LUX_MENDING, UMBRA_SLASH, LUX_MIST, UMBRA_MIST -> true;
            default -> false;
        };
    }

    static boolean umbra(Form form) {
        return switch (form) {
            case WELL, WELL_CHARGE, VEIL, TELEPORT, UMBRA_ARRIVAL, UMBRA_SLASH, UMBRA_MIST -> true;
            default -> false;
        };
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float formation, float opacity) {
        if (opacity <= 0) return;
        Form form = packet.form();
        double r = packet.radius();
        Vec3 end = packet.to().subtract(packet.from());
        int seed = Math.floorMod(packet.entityId() >= 0 ? packet.entityId() : packet.from().hashCode(), 113);
        switch (form) {
            case VERDICT, WHITE_VERDICT, VERDICT_CHARGE -> {
                if (end.lengthSqr() < 1e-8) return;
                Vec3 axis = end.normalize();
                if (form == Form.VERDICT_CHARGE) {
                    Vec3 focus = axis.scale(1.15);
                    card(p, v, focus, right, up, .20 + r * .14, .20 + r * .14, FOCUS, seed, WHITE, opacity);
                    for (int i = 0; i < 5; i++) {
                        double a = i * Math.PI * 2 / 5 + time * .014;
                        Vec3 source = focus.add(right.scale(Math.cos(a) * (.7 - r * .25)))
                                .add(up.scale(Math.sin(a) * (.55 - r * .2)));
                        stream(p, v, source, focus, camera, .085, .12, time * .04 + i,
                                RIBBON, seed + i, WHITE, opacity * .65f);
                    }
                    beam(p, v, focus, end, camera, .05 + r * .10, seed, opacity * .35f, false);
                } else {
                    beam(p, v, Vec3.ZERO, end, camera, r, seed, opacity, form == Form.WHITE_VERDICT);
                    if (form != Form.WHITE_VERDICT) {
                        Vec3 side = facingSide(axis, camera.subtract(end.scale(.5)));
                        float edgeOpacity = opacity * .28f * beamSideVisibility(Vec3.ZERO, end, camera, r);
                        for (int i = -1; i <= 1; i += 2) {
                            Vec3 offset = side.scale(r * .97 * i);
                            ribbon(p, v, offset, end.add(offset), camera, r * .02,
                                    RIBBON, seed + i + 2, WHITE, edgeOpacity, 0, 1);
                        }
                    }
                }
            }
            case BEACON -> {
                Vec3 focus = new Vec3(0, 1.35 + Math.sin(time * .045) * .065, 0);
                card(p, v, focus, right, up, .38, .57, FOCUS, seed, WHITE, opacity);
                card(p, v, new Vec3(0, 1.3, 0), right, up, .65, 1.45, WISP, seed + 3, WHITE, opacity * .7f);
                for (int i = 0; i < 2; i++) {
                    double a = i * Math.PI + time * .012;
                    Vec3 bottom = new Vec3(Math.cos(a) * .13, .12, Math.sin(a) * .13);
                    stream(p, v, bottom, focus.add(Math.sin(a) * .28, .85 + i * .22, Math.cos(a) * .23), camera,
                            .18, .19, time * .035 + i * 2.1, RIBBON, seed + i, WHITE, opacity * .42f);
                }
                float pulse = (float) ((age % 20) / 20.0);
                ground(p, v, Vec3.ZERO.add(0, .08, 0), r * (.25 + pulse * .7), RIPPLE, seed,
                        WHITE, opacity * (1 - pulse) * .42f);
            }
            case EYE -> {
                Vec3 center = new Vec3(0, 2.5 + Math.sin(time * .03) * .035, 0);
                card(p, v, center, right, up, .83, .58, EYE, seed, WHITE, opacity);
                card(p, v, center.add(camera.subtract(center).normalize().scale(.012)), right, up,
                        .15, .30, FOCUS, seed + 1, 0xFF5268, opacity);
                if (formation < 1) stream(p, v, new Vec3(0, 1.25, 0), center, camera, .09, .06,
                        time * .03, RIBBON, seed, WHITE, opacity * (1 - formation));
            }
            case FLARE -> {
                double expansion = 1 - Math.pow(1 - Mth.clamp(age / 9.0, 0, 1), 3);
                double spread = r * (.12 + expansion * .88);
                lightShell(p, v, spread, camera, seed, opacity);
                card(p, v, Vec3.ZERO, right, up, spread * .90, spread * .90,
                        FOCUS, seed, 0xFFFFFF, opacity);
                for (int i = 0; i < 18; i++) {
                    double y = 1 - 2 * (i + .5) / 18;
                    double horizontal = Math.sqrt(1 - y * y);
                    double angle = i * 2.399963 + seed;
                    Vec3 direction = new Vec3(Math.cos(angle) * horizontal, y, Math.sin(angle) * horizontal);
                    Vec3 tip = direction.scale(spread);
                    if (i % 2 == 0)
                        card(p, v, tip.scale(.46), right, up, spread * .52, spread * .52,
                                FOCUS, seed + i, 0xFFFFFF, opacity * .75F);
                    ribbon(p, v, tip.scale(.35), tip, camera, r * .025,
                            RIBBON, seed + i, 0xFFFFFF, opacity * .65f, 0, 1);
                }
            }
            case SUTURE -> stream(p, v, Vec3.ZERO, end, camera, .15, .12, time * .035,
                    RIBBON, seed, WHITE, opacity);
            case LUX_MENDING -> {
                for (int i = 0; i < 5; i++) {
                    double a = i * Math.PI * 2 / 5 + time * .017;
                    Vec3 start = new Vec3(Math.cos(a) * .33, .3, Math.sin(a) * .33);
                    Vec3 tip = new Vec3(Math.cos(a + .8) * .36, 1.45, Math.sin(a + .8) * .36);
                    stream(p, v, start, tip, camera, .11, .07, time * .03 + i,
                            RIBBON, seed + i, WHITE, opacity * .65f);
                }
            }
            case WELL -> {
                double spread = r * (.45 + formation * .55);
                ground(p, v, new Vec3(0, .07, 0), spread, POOL, seed, 0xFFFFFF, opacity);
                for (int i = 0; i < 7; i++) {
                    double a = i * 2.39996 + time * .012;
                    double drift = (time * .011 + i * .137) % 1;
                    Vec3 at = new Vec3(Math.cos(a) * spread * (.8 - drift * .4),
                            .14 + Math.sin(drift * Math.PI) * .19, Math.sin(a) * spread * (.8 - drift * .4));
                    card(p, v, at, right, up, Math.min(.7, r * .27), .32, WISP, seed + i,
                            0xFFFFFF, opacity * .62f);
                }
            }
            case WELL_CHARGE -> {
                Vec3 focus = end.lengthSqr() > 1e-8 ? end.normalize().scale(1.15) : new Vec3(0, 0, 1.15);
                card(p, v, focus, right, up, .24 + r * .2, .22 + r * .16, WISP, seed, 0xFFFFFF, opacity);
                for (int i = 0; i < 4; i++) {
                    double a = i * Math.PI / 2 + time * .018;
                    Vec3 from = focus.add(right.scale(Math.cos(a) * .65)).add(up.scale(Math.sin(a) * .46));
                    stream(p, v, from, focus, camera, .17, .13, i + time * .025,
                            RIBBON, seed + i, 0xFFFFFF, opacity * .8f);
                }
            }
            case VEIL, TELEPORT, UMBRA_ARRIVAL -> {
                double collapse = switch (form) {
                    case TELEPORT -> Math.max(.05, 1 - age / 20.0);
                    case UMBRA_ARRIVAL -> Math.min(1, .05 + age / 12.0);
                    default -> 1;
                };
                for (int i = 0; i < 6; i++) {
                    double a = i * 2.39996 + time * .012;
                    Vec3 at = new Vec3(Math.cos(a) * .42 * collapse,
                            .3 + (i % 3) * .48, Math.sin(a) * .42 * collapse);
                    card(p, v, at, right, up, .40 * collapse, .63, WISP, seed + i,
                            0xFFFFFF, opacity * .65f);
                }
            }
            case UMBRA_SLASH -> {
                Vec3 across = end.lengthSqr() > 1e-8 ? facingSide(end.normalize(), new Vec3(0, 1, 0)) : right;
                Vec3 cutUp = end.lengthSqr() > 1e-8 ? across.cross(end.normalize()).normalize() : up;
                double roll = Math.toRadians(packet.count());
                double cos = Math.cos(roll), sin = Math.sin(roll);
                Vec3 rotatedAcross = across.scale(cos).add(cutUp.scale(sin));
                cutUp = cutUp.scale(cos).subtract(across.scale(sin));
                across = rotatedAcross;
                double[] offsets = slashOffsets();
                for (int strand = 0; strand < offsets.length; strand++) {
                    double offset = offsets[strand] * r;
                    double cut = Mth.clamp((age - strand * .65) / 3.0, .015, 1);
                    Vec3 start = across.scale(-r).add(cutUp.scale(offset - r * .32));
                    Vec3 finish = across.scale(r).add(cutUp.scale(offset + r * .32));
                    Vec3 tip = start.lerp(finish, cut);
                    stream(p, v, start, tip, camera, .16, .11, age * .02 + strand * .13,
                            SLASH, seed + strand * 17, 0xFFFFFF, opacity * (strand == 1 ? 1.0f : .82f));
                }
            }
            case LUX_MIST, UMBRA_MIST -> {
                boolean dark = form == Form.UMBRA_MIST;
                for (int i = 0; i < 3; i++) {
                    double angle = i * 2.39996 + seed;
                    double distance = age * .008;
                    Vec3 at = new Vec3(Math.cos(angle) * distance, age * .009, Math.sin(angle) * distance);
                    card(p, v, at, right, up, r * (.6 + age * .02), r * (.8 + age * .025),
                            dark ? WISP : FOCUS, seed + i, dark ? 0xFFFFFF : WHITE, opacity * .65f);
                }
            }
            default -> throw new IllegalArgumentException("Not a flow form: " + form);
        }
    }

    static Vec3 facingSide(Vec3 direction, Vec3 view) {
        Vec3 side = direction.cross(view);
        return side.lengthSqr() > 1e-8 ? side.normalize() : VisceralGeometry.side(direction);
    }

    private static void lightShell(PoseStack poses, VertexConsumer vertices, double radius,
            Vec3 camera, int seed, float opacity) {
        for (int latitude = 0; latitude < 16; latitude++) {
            for (int longitude = 0; longitude < 32; longitude++) {
                shellVertex(poses, vertices, longitude / 32.0, latitude / 16.0, radius, camera, seed, opacity);
                shellVertex(poses, vertices, (longitude + 1) / 32.0, latitude / 16.0, radius, camera, seed, opacity);
                shellVertex(poses, vertices, (longitude + 1) / 32.0, (latitude + 1) / 16.0, radius, camera, seed, opacity);
                shellVertex(poses, vertices, longitude / 32.0, (latitude + 1) / 16.0, radius, camera, seed, opacity);
            }
        }
    }

    private static void shellVertex(PoseStack poses, VertexConsumer vertices, double u, double v,
            double radius, Vec3 camera, int seed, float opacity) {
        double ring = Math.sin(v * Math.PI);
        Vec3 normal = new Vec3(Math.cos(u * Math.PI * 2) * ring, Math.cos(v * Math.PI),
                Math.sin(u * Math.PI * 2) * ring);
        Vec3 at = normal.scale(radius);
        double facing = Math.abs(normal.dot(camera.subtract(at).normalize()));
        float rim = (float) (.025 + .16 * Math.pow(1 - facing, 2));
        vertex(poses, vertices, at, 0xFFFFFF, opacity * rim, LUX_SHELL * 2 + u,
                Math.floorMod(seed, 127) * 2 + v);
    }

    private static void beam(PoseStack p, VertexConsumer v, Vec3 from, Vec3 to, Vec3 camera,
            double radius, int seed, float alpha, boolean verdict) {
        Vec3 direction = to.subtract(from);
        double length = direction.length();
        if (length < 1e-4 || radius <= 0) return;
        Vec3 axis = direction.scale(1 / length);
        Vec3 across = VisceralGeometry.side(axis), up = axis.cross(across).normalize();
        Vec3 focus = from.add(axis.scale(Math.min(1.15, length * .25)));
        int shape = verdict ? WHITE_BEAM : BEAM;
        double coreScale = verdict ? 1.2 : 1;
        float sideVisibility = beamSideVisibility(from, to, camera, radius);
        float axialVisibility = verdict ? 1 - sideVisibility : 1;
        ribbon(p, v, from, to, camera, radius, shape, seed, WHITE,
                alpha * sideVisibility, 0, 1);
        // Offset light sheets and soft cross-sections keep an axial camera out of the strip's plane.
        // Every vertex remains inside the original damage cylinder and clipped endpoint planes.
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            Vec3 offset = across.scale(Math.cos(angle) * radius * .24 * coreScale)
                    .add(up.scale(Math.sin(angle) * radius * .24 * coreScale));
            ribbon(p, v, focus.add(offset), to.add(offset), camera, radius * .15 * coreScale,
                    shape, seed + i, WHITE, alpha * .4f * axialVisibility, 0, 1);
        }
        card(p, v, focus, across, up, radius * .48 * coreScale, radius * .48 * coreScale,
                FOCUS, seed, WHITE, alpha * .8f * axialVisibility);
        card(p, v, to.subtract(axis.scale(Math.min(.015, length * .05))), across, up,
                radius * .48 * coreScale, radius * .48 * coreScale, FOCUS, seed, WHITE,
                alpha * .55f * axialVisibility);
    }

    private static float beamSideVisibility(Vec3 from, Vec3 to, Vec3 camera, double radius) {
        double distance = to.subtract(from).normalize().cross(camera.subtract(from)).length();
        return ManipulationLifecycle.smooth((float) ((distance / Math.max(.001, radius) - .25) / .75));
    }

    static void stream(PoseStack p, VertexConsumer v, Vec3 start, Vec3 end, Vec3 camera, double width,
            double bend, double phase, int shape, int seed, int color, float alpha) {
        if (start.distanceToSqr(end) < 1e-8) return;
        Vec3 axis = end.subtract(start).normalize();
        Vec3 side = facingSide(axis, camera.subtract(start.lerp(end, .5)));
        double curvature = Math.min(bend, start.distanceTo(end) * .10);
        List<Vec3> points = new ArrayList<>(13);
        points.add(start);
        for (int i = 1; i <= 12; i++) {
            double t = i / 12.0;
            points.add(start.lerp(end, t).add(side.scale(Math.sin(t * Math.PI) * Math.sin(t * 5 + phase) * curvature)));
        }
        ribbonPath(p, v, points, camera, width, shape, seed, color, alpha);
    }

    static void ribbonPath(PoseStack p, VertexConsumer v, List<Vec3> points, Vec3 camera, double width,
            int shape, int seed, int color, float alpha) {
        if (points.size() < 2 || width <= 0 || alpha <= 0) return;
        Vec3 previousLeft = null, previousRight = null, previousSide = null;
        for (int i = 0; i < points.size(); i++) {
            Vec3 at = points.get(i);
            Vec3 tangent = points.get(Math.min(i + 1, points.size() - 1))
                    .subtract(points.get(Math.max(0, i - 1))).normalize();
            Vec3 side = tangent.lengthSqr() < 1e-8
                    ? previousSide == null ? new Vec3(1, 0, 0) : previousSide
                    : facingSide(tangent, camera.subtract(at));
            if (previousSide != null && side.dot(previousSide) < 0) side = side.scale(-1);
            // Keep inner edges from folding past neighboring stations on short, tightly curved flows.
            double localWidth = width;
            if (i > 0) localWidth = Math.min(localWidth, joinWidth(at.subtract(points.get(i - 1)), side));
            if (i + 1 < points.size()) localWidth = Math.min(localWidth, joinWidth(points.get(i + 1).subtract(at), side));
            Vec3 left = at.subtract(side.scale(localWidth)), right = at.add(side.scale(localWidth));
            if (i > 0) {
                double first = left.subtract(previousLeft).cross(right.subtract(previousLeft)).dot(camera.subtract(previousLeft));
                double second = previousRight.subtract(right).cross(previousLeft.subtract(right)).dot(camera.subtract(right));
                // Tight joins can be concave in the camera view. Use the diagonal inside that contour.
                quad(p, v, previousLeft, left, right, previousRight, shape, seed, color, alpha,
                        (i - 1.0) / (points.size() - 1), i / (points.size() - 1.0), first * second < 0);
            }
            previousLeft = left;
            previousRight = right;
            previousSide = side;
        }
    }

    private static double joinWidth(Vec3 segment, Vec3 side) {
        return segment.length() * .45 / Math.max(.001, Math.abs(segment.normalize().dot(side)));
    }

    static void ribbon(PoseStack p, VertexConsumer v, Vec3 from, Vec3 to, Vec3 camera, double width,
            int shape, int seed, int color, float alpha, double u0, double u1) {
        Vec3 axis = to.subtract(from);
        if (axis.lengthSqr() < 1e-10 || width <= 0 || alpha <= 0) return;
        Vec3 side = facingSide(axis.normalize(), camera.subtract(from.lerp(to, .5))).scale(width);
        quad(p, v, from.subtract(side), to.subtract(side), to.add(side), from.add(side),
                shape, seed, color, alpha, u0, u1);
    }

    static void card(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double width, double height,
            int shape, int seed, int color, float alpha) {
        Vec3 x = right.scale(width), y = up.scale(height);
        quad(p, v, at.subtract(x).subtract(y), at.add(x).subtract(y), at.add(x).add(y), at.subtract(x).add(y),
                shape, seed, color, alpha, 0, 1);
    }

    static void ground(PoseStack p, VertexConsumer v, Vec3 at, double radius, int shape, int seed, int color, float alpha) {
        card(p, v, at, new Vec3(1, 0, 0), new Vec3(0, 0, 1), radius, radius, shape, seed, color, alpha);
    }

    private static void quad(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
            int shape, int seed, int color, float alpha, double u0, double u1) {
        quad(p, v, a, b, c, d, shape, seed, color, alpha, u0, u1, false);
    }

    private static void quad(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
            int shape, int seed, int color, float alpha, double u0, double u1, boolean alternateDiagonal) {
        if (alpha <= 0) return;
        // Unlike material meshes, shader cards always retain the entire normalized UV domain.
        int phase = Math.floorMod(seed, 127);
        if (!alternateDiagonal) vertex(p, v, a, color, alpha, shape * 2 + u0, phase * 2);
        vertex(p, v, b, color, alpha, shape * 2 + u1, phase * 2);
        vertex(p, v, c, color, alpha, shape * 2 + u1, phase * 2 + 1);
        vertex(p, v, d, color, alpha, shape * 2 + u0, phase * 2 + 1);
        if (alternateDiagonal) vertex(p, v, a, color, alpha, shape * 2 + u0, phase * 2);
    }

    private static void vertex(PoseStack p, VertexConsumer v, Vec3 at, int color, float alpha, double u, double texV) {
        v.addVertex(p.last().pose(), (float) at.x, (float) at.y, (float) at.z)
                .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, (int) (Mth.clamp(alpha, 0, 1) * 255))
                .setUv((float) u, (float) texV);
    }
}
