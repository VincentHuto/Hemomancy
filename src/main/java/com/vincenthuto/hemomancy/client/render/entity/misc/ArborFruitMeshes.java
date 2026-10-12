package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborCanopyGeometry;
import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborFruitGeometry;
import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborOfWillVisualRules;
import net.minecraft.client.renderer.LightTexture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;

import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.FAMILY_COLORS;
import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.mix;

/**
 * Organ-like Arbor fruit, cached once per {@link ArborFruitMeshKey}. Bodies are closed surfaces of revolution
 * shaded from a dark clot at the base through the family colour to a pale wet highlight, with raised veins,
 * sinew and fissures layered on. Coordinates are fruit-local; the renderer translates and scales each instance.
 */
final class ArborFruitMeshes {
    /** Reference fruit radius; every family shape is authored in multiples of it. */
    static final double R = .24;
    private static final int PALE_CORE = 0xFFFFD2C4;
    private static final int CLOT = 0xFF7A1020;
    private static final int RAW_FISSURE = 0xFFC0405A;
    private static final int SINEW = 0xFF5A1C24;
    private static final int SCAR_FISSURES = 5;
    private static final int GILLS = 12;

    private ArborFruitMeshes() { }

    @FunctionalInterface
    interface SurfaceColor { int at(double t, double phi); }

    static void emit(ArborMeshEmitter.MeshConsumers consumers, ArborFruitMeshKey key) {
        PoseStack.Pose pose = new PoseStack().last();
        int color = FAMILY_COLORS.getOrDefault(key.family(), FAMILY_COLORS.get("core"));
        VertexConsumer flesh = consumers.mycelium();
        switch (key.state()) {
            case DEGREE_SEALED_BUD, DORMANT_BUD -> eggSac(pose, flesh, color, key.state());
            case CLOSED_CALYX -> clenchedCalyx(pose, flesh);
            case RIPE_FRUIT -> {
                int light = LightTexture.pack(13, 10);
                switch (ArborFruitGeometry.profile(key.family()).shape()) {
                    case HEART_POME -> heart(pose, flesh, light, color);
                    case HOOKED_PEAR -> hookedGland(pose, flesh, light, color);
                    case THREAD_BERRIES -> cystClutch(pose, flesh, light, color);
                    case JOINED_FRUIT -> boundTwins(pose, flesh, light, color);
                    case FISSURED_NUT -> scarKnot(pose, flesh, light, color);
                    case GILLED_POD -> fruitingBody(pose, flesh, light, color);
                }
            }
        }
    }

    // ---- Core: a chambered heart leaning on its apex, with atria, vessel stubs and coronary veins.

    private static ArborCanopyGeometry.Point heartAxis(double t) {
        double y = -1.0 * R + 1.75 * R * t;
        return new ArborCanopyGeometry.Point(y * .18, y, 0);
    }

    private static double heartRadius(double t, double phi) {
        return .80 * R * Math.pow(Math.sin(Math.PI * Math.pow(t, 1.45)), .85)
                * (1 + .10 * Math.cos(2 * phi) * smooth((t - .55) / .45));
    }

    private static void heart(PoseStack.Pose pose, VertexConsumer out, int light, int color) {
        organBody(pose, out, light, ArborFruitMeshes::heartAxis, ArborFruitMeshes::heartRadius,
                (t, phi) -> flesh(color, t, mottle(t, phi, 3)), 9, 12);
        for (int side : new int[]{-1, 1}) {
            ArborCanopyGeometry.Point atrium = new ArborCanopyGeometry.Point(side * .32 * R + .08 * R, .45 * R, .05 * R);
            sphere(pose, out, light, atrium, .38 * R, (t, phi) -> flesh(color, .35 + t * .6, mottle(t, phi, 7 + side)), 5, 8);
        }
        ArborMeshEmitter.texturedTubeProfiled(pose, out, light, List.of(p(-.10, .55, 0), p(-.15, .95, .05), p(-.05, 1.15, .12)),
                (float) (.16 * R), (float) (.12 * R), (float) (.12 * R), mix(color, PALE_CORE, .25F), .4F, 1.0F,
                6, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        ArborMeshEmitter.texturedTubeProfiled(pose, out, light, List.of(p(.15, .60, -.05), p(.22, .92, -.07), p(.27, 1.05, -.02)),
                (float) (.13 * R), (float) (.10 * R), (float) (.10 * R), mix(color, CLOT, .40F), .4F, 1.0F,
                6, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        surfaceVein(pose, out, light, ArborFruitMeshes::heartAxis, ArborFruitMeshes::heartRadius, .85, .22, .45, .95);
        surfaceVein(pose, out, light, ArborFruitMeshes::heartAxis, ArborFruitMeshes::heartRadius, .80, .30, 3.55, 3.05);
    }

    // ---- Living Staff: a segmented gland whose neck curls into a hook.

    private static ArborCanopyGeometry.Point glandAxis(double t) {
        return new ArborCanopyGeometry.Point(0, -1.0 * R + 1.5 * R * t, 0);
    }

    private static double glandRadius(double t, double phi) {
        double constriction = 1.0;
        for (double ring : new double[]{.38, .56, .72}) constriction -= .16 * Math.exp(-Math.pow((t - ring) / .04, 2));
        return .74 * R * Math.pow(Math.sin(Math.PI * Math.pow(t, .72)), .8) * constriction;
    }

    private static void hookedGland(PoseStack.Pose pose, VertexConsumer out, int light, int color) {
        organBody(pose, out, light, ArborFruitMeshes::glandAxis, ArborFruitMeshes::glandRadius, (t, phi) -> {
            double spot = mottle(t, phi, 11);
            int base = flesh(color, t, spot);
            return spot > .55 ? mix(base, CLOT, .35F) : base;
        }, 14, 12);
        ArborMeshEmitter.texturedTubeProfiled(pose, out, light,
                List.of(p(0, .42, 0), p(0, .80, 0), p(.12, 1.10, 0), p(.38, 1.20, 0), p(.55, 1.02, 0)),
                (float) (.20 * R), (float) (.12 * R), 0F, mix(color, CLOT, .25F), .6F, .55F,
                8, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        surfaceVein(pose, out, light, ArborFruitMeshes::glandAxis, ArborFruitMeshes::glandRadius, .30, .80, 2.2, 2.6);
    }

    // ---- Summons: a clutch of cysts tethered to a sinew stalk, each with a dark nucleus.

    private static ArborCanopyGeometry.Point cystCenter(int index) {
        double angle = index * 2.399963229728653 + .3;
        double distance = (.32 + (index % 2) * .08) * R;
        return new ArborCanopyGeometry.Point(Math.cos(angle) * distance, (.30 - index * .15) * R,
                Math.sin(angle) * distance);
    }

    private static double cystSize(int index) {
        return (.34 + .06 * ((index * 7) % 3)) * R;
    }

    private static void cystClutch(PoseStack.Pose pose, VertexConsumer out, int light, int color) {
        ArborMeshEmitter.texturedTubeProfiled(pose, out, light, List.of(p(0, .55, 0), p(.04, .05, .02), p(0, -.45, 0)),
                (float) (.09 * R), (float) (.05 * R), (float) (.05 * R), SINEW, .5F, 1.0F,
                6, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        for (int i = 0; i < 6; i++) {
            ArborCanopyGeometry.Point center = cystCenter(i);
            double outward = Math.atan2(center.z(), center.x());
            ArborMeshEmitter.texturedTubeProfiled(pose, out, light,
                    List.of(new ArborCanopyGeometry.Point(0, center.y() + .10 * R, 0), center),
                    (float) (.035 * R), (float) (.03 * R), (float) (.03 * R), SINEW, .3F, 1.0F, 4, 1);
            int seed = i;
            sphere(pose, out, light, center, cystSize(i), (t, phi) -> {
                boolean nucleus = t < .3 && Math.cos(phi - outward) > .35;
                return nucleus ? mix(CLOT, 0xFF100810, .4F) : flesh(color, .25 + t * .75, mottle(t, phi, seed));
            }, 6, 10);
        }
    }

    // ---- Covenant: two fused bodies with a pale seam, bound together by helical sinew.

    private static DoubleFunction<ArborCanopyGeometry.Point> twinAxis(int side) {
        return t -> new ArborCanopyGeometry.Point(side * (.33 * R + .10 * R * (t - .5)), -.85 * R + 1.6 * R * t, 0);
    }

    private static double twinRadius(double t, double phi) {
        return .55 * R * Math.pow(Math.sin(Math.PI * Math.pow(t, 1.1)), .85);
    }

    private static void boundTwins(PoseStack.Pose pose, VertexConsumer out, int light, int color) {
        for (int side : new int[]{-1, 1}) {
            organBody(pose, out, light, twinAxis(side), ArborFruitMeshes::twinRadius,
                    (t, phi) -> {
                        int base = flesh(color, t, mottle(t, phi, 17 + side));
                        return Math.cos(phi) * side < -.6 ? mix(base, PALE_CORE, .45F) : base;
                    }, 9, 12);
        }
        for (int band = 0; band < 2; band++) {
            List<ArborCanopyGeometry.Point> sinew = new ArrayList<>(9);
            for (int k = 0; k <= 8; k++) {
                double u = k / 8.0;
                double angle = band * Math.PI + u * Math.PI * 2.4;
                // Slightly inside the twins' outer silhouette, so the sinew visibly cinches the flesh.
                sinew.add(new ArborCanopyGeometry.Point(Math.cos(angle) * .97 * R, (-.45 + .9 * u) * R,
                        Math.sin(angle) * .60 * R));
            }
            ArborMeshEmitter.texturedTubeProfiled(pose, out, light, sinew, (float) (.05 * R), (float) (.035 * R),
                    (float) (.035 * R), SINEW, 1.0F, 1.0F, 5, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        }
    }

    // ---- Scars: a lumpy keloid knot split by raw fissures between raised bone-pale ridges.

    private static ArborCanopyGeometry.Point knotAxis(double t) {
        return new ArborCanopyGeometry.Point(0, -.7 * R + 1.3 * R * t, 0);
    }

    private static double fissureDistance(double phi) {
        double best = Math.PI;
        for (int k = 0; k < SCAR_FISSURES; k++) {
            double d = Math.abs(Math.IEEEremainder(phi - fissureAngle(k), Math.PI * 2));
            best = Math.min(best, d);
        }
        return best;
    }

    private static double fissureAngle(int index) {
        return index * Math.PI * 2 / SCAR_FISSURES + .3;
    }

    private static double fissureDepth(double t, double phi) {
        return Math.pow(Math.sin(Math.PI * t), 1.5) * Math.exp(-Math.pow(fissureDistance(phi) / .14, 2));
    }

    private static double knotRadius(double t, double phi) {
        double depth = Math.pow(Math.sin(Math.PI * t), 1.5);
        double d = fissureDistance(phi);
        double ridge = depth * Math.exp(-Math.pow((d - .26) / .08, 2));
        return .88 * R * Math.pow(Math.sin(Math.PI * t), .75)
                * (1 + .07 * Math.sin(3 * phi + 5 * t) * Math.sin(Math.PI * t))
                * (1 - .24 * fissureDepth(t, phi)) * (1 + .06 * ridge);
    }

    private static void scarKnot(PoseStack.Pose pose, VertexConsumer out, int light, int color) {
        organBody(pose, out, light, ArborFruitMeshes::knotAxis, ArborFruitMeshes::knotRadius, (t, phi) -> {
            double fissure = fissureDepth(t, phi);
            int base = flesh(color, t, mottle(t, phi, 23));
            return fissure > .2 ? mix(base, RAW_FISSURE, (float) Math.min(1, fissure * 1.4)) : base;
        }, 9, 40);
    }

    /** Point in the floor of a scar fissure, just proud of the flesh, for the renderer's fissure glow. */
    static ArborCanopyGeometry.Point scarFissurePoint(int fissure, double t) {
        double phi = fissureAngle(fissure);
        return liftedSurfacePoint(ArborFruitMeshes::knotAxis, ArborFruitMeshes::knotRadius, t, phi, .006);
    }

    static int scarFissureCount() {
        return SCAR_FISSURES;
    }

    // ---- Mycelial: a veined fruiting body with gill fins under the cap and pale nubs on top.

    private static final double CAP_UNDERSIDE = .18;

    private static ArborCanopyGeometry.Point capAxis(double t) {
        return new ArborCanopyGeometry.Point(0, -.2 * R + .8 * R * t, 0);
    }

    private static double capRadius(double t, double phi) {
        double profile = t < CAP_UNDERSIDE ? Math.sin(Math.PI * .5 * t / CAP_UNDERSIDE)
                : Math.pow(Math.cos((t - CAP_UNDERSIDE) / (1 - CAP_UNDERSIDE) * Math.PI * .5), .7);
        return R * profile * (1 + .05 * Math.sin(5 * phi) * t);
    }

    private static void fruitingBody(PoseStack.Pose pose, VertexConsumer out, int light, int color) {
        organBody(pose, out, light, t -> new ArborCanopyGeometry.Point(0, -.8 * R + .75 * R * t, 0),
                (t, phi) -> .22 * R * (1 + .6 * Math.pow(1 - t, 2)) * Math.min(1, Math.sin(Math.PI * t) * 3),
                (t, phi) -> mix(flesh(color, .2 + t * .4, mottle(t, phi, 29)), PALE_CORE, .2F), 6, 10);
        organBody(pose, out, light, ArborFruitMeshes::capAxis, ArborFruitMeshes::capRadius, (t, phi) -> {
            if (t < CAP_UNDERSIDE) return mix(color, CLOT, .35F);
            double spot = mottle(t, phi, 31);
            int base = flesh(color, .3 + t * .7, spot);
            return spot > .6 ? mix(base, PALE_CORE, .5F) : base;
        }, 10, 14);
        for (int gill = 0; gill < GILLS; gill++) gillFin(pose, out, light, gill, mix(color, PALE_CORE, .35F));
        for (int nub = 0; nub < 5; nub++) {
            double phi = nub * 2.399963229728653 + .7;
            double t = .45 + .09 * (nub % 3);
            sphere(pose, out, light, liftedSurfacePoint(ArborFruitMeshes::capAxis, ArborFruitMeshes::capRadius, t, phi, 0),
                    .09 * R, (tt, pp) -> mix(color, PALE_CORE, .6F), 4, 6);
        }
        surfaceVein(pose, out, light, ArborFruitMeshes::capAxis, ArborFruitMeshes::capRadius, .25, .85, 1.1, 1.6);
    }

    /** Inner and outer ends of one gill's lower edge, for the renderer's gill glow. */
    static ArborCanopyGeometry.Point[] gillGlowLine(int gill) {
        double angle = gill * Math.PI * 2 / GILLS;
        double inner = .30 * R, outer = .88 * R;
        return new ArborCanopyGeometry.Point[]{
                new ArborCanopyGeometry.Point(Math.cos(angle) * inner, gillTop(inner) - gillDepth(inner) - .004, Math.sin(angle) * inner),
                new ArborCanopyGeometry.Point(Math.cos(angle) * outer, gillTop(outer) - gillDepth(outer) - .004, Math.sin(angle) * outer)};
    }

    static int gillCount() {
        return GILLS;
    }

    private static double gillTop(double radius) {
        double t = CAP_UNDERSIDE * Math.asin(Math.min(1, radius / R)) * 2 / Math.PI;
        return capAxis(t).y() - .004;
    }

    private static double gillDepth(double radius) {
        return .12 * R * Math.sin(Math.PI * Math.min(1, Math.max(0, (radius - .25 * R) / (.67 * R))));
    }

    /** A gill is a thin double-faced fin hanging radially under the cap. */
    private static void gillFin(PoseStack.Pose pose, VertexConsumer out, int light, int gill, int color) {
        double angle = gill * Math.PI * 2 / GILLS;
        double cx = Math.cos(angle), cz = Math.sin(angle);
        double tx = -cz, tz = cx;
        double half = .006;
        double[] radii = {.25 * R, .58 * R, .92 * R};
        for (int face = -1; face <= 1; face += 2) {
            for (int i = 0; i < radii.length - 1; i++) {
                double r0 = radii[i], r1 = radii[i + 1];
                double ox = tx * half * face, oz = tz * half * face;
                ArborCanopyGeometry.Point top0 = new ArborCanopyGeometry.Point(cx * r0 + ox, gillTop(r0), cz * r0 + oz);
                ArborCanopyGeometry.Point top1 = new ArborCanopyGeometry.Point(cx * r1 + ox, gillTop(r1), cz * r1 + oz);
                ArborCanopyGeometry.Point low0 = new ArborCanopyGeometry.Point(top0.x(), gillTop(r0) - gillDepth(r0), top0.z());
                ArborCanopyGeometry.Point low1 = new ArborCanopyGeometry.Point(top1.x(), gillTop(r1) - gillDepth(r1), top1.z());
                ArborCanopyGeometry.Point[] quad = face > 0
                        ? new ArborCanopyGeometry.Point[]{low0, low1, top1, top0}
                        : new ArborCanopyGeometry.Point[]{low0, top0, top1, low1};
                for (ArborCanopyGeometry.Point v : quad) {
                    ArborMeshEmitter.texturedVertex(pose, out, light, v.x(), v.y(), v.z(),
                            (float) (v.distanceTo(new ArborCanopyGeometry.Point(0, v.y(), 0)) / R), (float) ((v.y() / R) + 1),
                            tx * face, 0, tz * face, color);
                }
            }
        }
    }

    // ---- Locked buds: membrane egg sacs. Degree-sealed ones are bone pale and stitched shut.

    private static void eggSac(PoseStack.Pose pose, VertexConsumer out, int familyColor,
            ArborOfWillVisualRules.GrowthState state) {
        boolean sealed = state == ArborOfWillVisualRules.GrowthState.DEGREE_SEALED_BUD;
        int light = LightTexture.pack(10, 7);
        int skin = sealed ? 0xFFD1C4B2 : mix(0xFF6A303B, familyColor, .24F);
        DoubleFunction<ArborCanopyGeometry.Point> axis = t -> new ArborCanopyGeometry.Point(0, -.15 + .30 * t, 0);
        DoubleBinaryOperator radius = (t, phi) -> .10 * Math.pow(Math.sin(Math.PI * Math.pow(t, 1.25)), .8);
        organBody(pose, out, light, axis, radius, (t, phi) -> {
            int base = shade(mix(mix(skin, CLOT, (float) (.45 * (1 - t))), PALE_CORE, (float) (.25 * t)), mottle(t, phi, 37));
            if (!sealed) return base;
            boolean seam = Math.abs(Math.IEEEremainder(phi, Math.PI * 2)) < .2;
            boolean stitch = ((int) (t * 10)) % 2 == 0;
            return seam ? (stitch ? mix(base, PALE_CORE, .5F) : 0xFF3A1A1E) : base;
        }, 8, 10);
        int flap = mix(skin, CLOT, sealed ? .25F : .45F);
        for (int i = 0; i < 3; i++) {
            double a = i * Math.PI * 2 / 3.0 + .5;
            ArborMeshEmitter.minorEllipsoid(pose, out, light, Math.cos(a) * .055, -.035, Math.sin(a) * .055,
                    .045, .10, .03, flap);
        }
        if (!sealed) {
            surfaceVein(pose, out, light, axis, radius, .25, .85, .9, 1.3, mix(familyColor, PALE_CORE, .2F), .006F);
        }
    }

    // ---- Toggled-off skills: fleshy sepals clenched over a dark core.

    private static void clenchedCalyx(PoseStack.Pose pose, VertexConsumer out) {
        int light = LightTexture.pack(12, 9);
        sphere(pose, out, light, new ArborCanopyGeometry.Point(0, 0, 0), .11,
                (t, phi) -> shade(mix(0xFF6A3024, CLOT, (float) (.5 * (1 - t))), mottle(t, phi, 41)), 7, 10);
        for (int i = 0; i < 5; i++) {
            double a = i * Math.PI * 2 / 5.0;
            double c = Math.cos(a), s = Math.sin(a);
            ArborMeshEmitter.texturedTubeProfiled(pose, out, light, List.of(
                            new ArborCanopyGeometry.Point(c * .12, -.07, s * .12),
                            new ArborCanopyGeometry.Point(c * .14, .03, s * .14),
                            new ArborCanopyGeometry.Point(c * .09, .10, s * .09),
                            new ArborCanopyGeometry.Point(c * .02, .13, s * .02)),
                    .045F, .03F, 0F, i % 2 == 0 ? 0xFFAA662B : 0xFF8E4A22, .4F, .55F, 6,
                    ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        }
    }

    // ---- Level nodes: one glowing bead per skill level, sitting on the fruit's surface.

    /** Fruit-local surface point for level bead {@code index} of {@code count}; the renderer scales it per instance. */
    static ArborCanopyGeometry.Point levelNode(String family, int index, int count) {
        double phi = index * Math.PI * 2 / Math.max(1, count) + .5;
        return switch (ArborFruitGeometry.profile(family).shape()) {
            case HEART_POME -> liftedSurfacePoint(ArborFruitMeshes::heartAxis, ArborFruitMeshes::heartRadius, .55, phi, .01);
            case HOOKED_PEAR -> liftedSurfacePoint(ArborFruitMeshes::glandAxis, ArborFruitMeshes::glandRadius, .28, phi, .01);
            case THREAD_BERRIES -> {
                ArborCanopyGeometry.Point cyst = cystCenter(index % 6);
                yield cyst.add(new ArborCanopyGeometry.Point(0, cystSize(index % 6) + .006, 0));
            }
            case JOINED_FRUIT -> {
                // Alternate twins, keeping each bead on the outer face away from the seam.
                int side = index % 2 == 0 ? -1 : 1;
                double outer = (side > 0 ? 0 : Math.PI) + (index / 2 - (count - 1) / 4.0) * .7;
                yield liftedSurfacePoint(twinAxis(side), ArborFruitMeshes::twinRadius, .55, outer, .01);
            }
            case FISSURED_NUT -> liftedSurfacePoint(ArborFruitMeshes::knotAxis, ArborFruitMeshes::knotRadius, .5,
                    fissureAngle(index % SCAR_FISSURES) + Math.PI / SCAR_FISSURES, .01);
            case GILLED_POD -> liftedSurfacePoint(ArborFruitMeshes::capAxis, ArborFruitMeshes::capRadius, .4, phi, .01);
        };
    }

    // ---- Shared primitives.

    /**
     * Closed surface of revolution: rings run from the bottom pole (t=0) to the top pole (t=1), and each ring is
     * a horizontal slice around {@code axis(t)}. Quads wind counter-clockwise from outside so the layer can cull.
     */
    static void organBody(PoseStack.Pose pose, VertexConsumer out, int light,
            DoubleFunction<ArborCanopyGeometry.Point> axis, DoubleBinaryOperator radius, SurfaceColor color,
            int rings, int segments) {
        organBody(pose, out, light, axis, radius, color, rings, segments, 1, 1);
    }

    /** As above, repeating the texture {@code uRepeat} times around and {@code vRepeat} times bottom to top. */
    static void organBody(PoseStack.Pose pose, VertexConsumer out, int light,
            DoubleFunction<ArborCanopyGeometry.Point> axis, DoubleBinaryOperator radius, SurfaceColor color,
            int rings, int segments, float uRepeat, float vRepeat) {
        ArborCanopyGeometry.Point[][] points = new ArborCanopyGeometry.Point[rings + 1][segments + 1];
        ArborCanopyGeometry.Point[][] normals = new ArborCanopyGeometry.Point[rings + 1][segments + 1];
        int[][] colors = new int[rings + 1][segments + 1];
        for (int i = 0; i <= rings; i++) {
            double t = i / (double) rings;
            for (int j = 0; j <= segments; j++) {
                double phi = j * Math.PI * 2 / segments;
                points[i][j] = surfacePoint(axis, radius, t, phi);
                normals[i][j] = surfaceNormal(axis, radius, t, phi);
                colors[i][j] = color.at(t, phi);
            }
        }
        for (int i = 0; i < rings; i++) {
            for (int j = 0; j < segments; j++) {
                int[][] corners = {{i, j}, {i + 1, j}, {i + 1, j + 1}, {i, j + 1}};
                for (int[] c : corners) {
                    ArborCanopyGeometry.Point v = points[c[0]][c[1]], n = normals[c[0]][c[1]];
                    ArborMeshEmitter.texturedVertex(pose, out, light, v.x(), v.y(), v.z(),
                            c[1] * uRepeat / segments, c[0] * vRepeat / rings, n.x(), n.y(), n.z(), colors[c[0]][c[1]]);
                }
            }
        }
    }

    static void sphere(PoseStack.Pose pose, VertexConsumer out, int light, ArborCanopyGeometry.Point center,
            double size, SurfaceColor color, int rings, int segments) {
        organBody(pose, out, light, t -> center.add(new ArborCanopyGeometry.Point(0, (t - .5) * 2 * size * .95, 0)),
                (t, phi) -> size * Math.pow(Math.sin(Math.PI * t), .9), color, rings, segments);
    }

    private static ArborCanopyGeometry.Point surfacePoint(DoubleFunction<ArborCanopyGeometry.Point> axis,
            DoubleBinaryOperator radius, double t, double phi) {
        ArborCanopyGeometry.Point center = axis.apply(t);
        double r = Math.max(0, radius.applyAsDouble(t, phi));
        return new ArborCanopyGeometry.Point(center.x() + Math.cos(phi) * r, center.y(), center.z() + Math.sin(phi) * r);
    }

    private static ArborCanopyGeometry.Point surfaceNormal(DoubleFunction<ArborCanopyGeometry.Point> axis,
            DoubleBinaryOperator radius, double t, double phi) {
        double e = 1.0e-3;
        ArborCanopyGeometry.Point alongT = surfacePoint(axis, radius, Math.min(1, t + e), phi)
                .subtract(surfacePoint(axis, radius, Math.max(0, t - e), phi));
        ArborCanopyGeometry.Point alongPhi = surfacePoint(axis, radius, t, phi + e)
                .subtract(surfacePoint(axis, radius, t, phi - e));
        ArborCanopyGeometry.Point normal = alongT.cross(alongPhi);
        if (normal.length() < 1.0e-12) {
            // Poles: the ring has collapsed, so face along the axis away from the body.
            ArborCanopyGeometry.Point tangent = axis.apply(Math.min(1, t + e)).subtract(axis.apply(Math.max(0, t - e)));
            return t < .5 ? tangent.scale(-1).normalized() : tangent.normalized();
        }
        return normal.normalized();
    }

    private static ArborCanopyGeometry.Point liftedSurfacePoint(DoubleFunction<ArborCanopyGeometry.Point> axis,
            DoubleBinaryOperator radius, double t, double phi, double lift) {
        return surfacePoint(axis, radius, t, phi).add(surfaceNormal(axis, radius, t, phi).scale(lift));
    }

    private static void surfaceVein(PoseStack.Pose pose, VertexConsumer out, int light,
            DoubleFunction<ArborCanopyGeometry.Point> axis, DoubleBinaryOperator radius,
            double t0, double t1, double phi0, double phi1) {
        surfaceVein(pose, out, light, axis, radius, t0, t1, phi0, phi1, mix(CLOT, 0xFF100810, .3F), .016F);
    }

    /** A raised vessel following the flesh from (t0, phi0) to (t1, phi1). */
    private static void surfaceVein(PoseStack.Pose pose, VertexConsumer out, int light,
            DoubleFunction<ArborCanopyGeometry.Point> axis, DoubleBinaryOperator radius,
            double t0, double t1, double phi0, double phi1, int color, float thickness) {
        List<ArborCanopyGeometry.Point> path = new ArrayList<>(5);
        for (int k = 0; k <= 4; k++) {
            double u = k / 4.0;
            double wander = .12 * Math.sin(u * Math.PI * 2 + phi0);
            path.add(liftedSurfacePoint(axis, radius, t0 + (t1 - t0) * u, phi0 + (phi1 - phi0) * u + wander,
                    thickness * .4));
        }
        ArborMeshEmitter.texturedTubeProfiled(pose, out, light, path, thickness, thickness * .45F, thickness * .45F,
                color, .5F, 1.0F, 4, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
    }

    /** Wet flesh: dark clot at the base, the family colour through the body, a pale sheen toward the top. */
    private static int flesh(int color, double t, double mottle) {
        int base = t < .45 ? mix(CLOT, color, (float) (t / .45))
                : mix(color, PALE_CORE, (float) (Math.max(0, (t - .45) / .55) * .38));
        return shade(base, mottle);
    }

    static int shade(int color, double amount) {
        float k = (float) (1 + amount * .1);
        int a = color >>> 24 & 255;
        int r = Math.min(255, Math.round((color >>> 16 & 255) * k));
        int g = Math.min(255, Math.round((color >>> 8 & 255) * k));
        int b = Math.min(255, Math.round((color & 255) * k));
        return a << 24 | r << 16 | g << 8 | b;
    }

    /** Deterministic blotchy noise in [-1, 1] so cached fruit keep the same mottling. */
    static double mottle(double t, double phi, int seed) {
        return Math.sin(t * 41.3 + phi * 7.1 + seed) * Math.sin(t * 17.9 - phi * 5.3 + seed * 1.7);
    }

    private static double smooth(double x) {
        double t = Math.max(0, Math.min(1, x));
        return t * t * (3 - 2 * t);
    }

    private static ArborCanopyGeometry.Point p(double x, double y, double z) {
        return new ArborCanopyGeometry.Point(x * R, y * R, z * R);
    }
}
