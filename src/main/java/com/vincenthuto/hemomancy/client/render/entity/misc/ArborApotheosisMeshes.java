package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborCanopyGeometry;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborSkeleton;
import net.minecraft.client.renderer.LightTexture;

import java.util.List;

import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.mix;

/**
 * The Apotheosis crown: a fungal stalk rises from the top of the heartwood and spreads into one domed cap above
 * the willow, so the boughs still weep out underneath it. Gill fins line the underside and a short lace veil
 * hangs from the lobed rim. Gill glow and drifting spores are drawn by the renderer from {@link #gillGlowLine}
 * and {@link #sporeOrigin}.
 */
final class ArborApotheosisMeshes {
    private static final double CAP_RADIUS = 3.0;
    private static final double CAP_LIFT = 1.4;
    private static final double CAP_DOME = 1.25;
    private static final double UNDERSIDE_RISE = .22;
    private static final double RIM_SAG = .18;
    private static final double RIM_FLARE = .12;
    // Lathe parameter ranges for the underside, the curled rim and the dome.
    private static final double UNDERSIDE_END = .40;
    private static final double RIM_END = .50;
    private static final double STALK_RADIUS = .34;
    private static final int GILLS = 36;
    private static final double GILL_INNER = .45;
    private static final double GILL_DEPTH = .16;
    private static final int VEIL_STRANDS = 30;
    private static final double VEIL_LENGTH = .5;
    private static final int CAP_TOP = 0xFFB92A40;
    private static final int CAP_CLOT = 0xFF5A0E1C;
    private static final int PALE_SPOT = 0xFFE3C99A;
    private static final int GILL_FLESH = 0xFFD8A88E;
    private static final int VEIL_LACE = 0xFFD9B8B0;
    private static final int WARTS = 22;

    private ArborApotheosisMeshes() { }

    /** Height of the cap's underside at its centre. */
    static double undersideHeight(int degree) {
        return ArborSkeleton.trunkTop(degree) + CAP_LIFT * ArborSkeleton.crownScale(degree);
    }

    static void emitCrown(PoseStack.Pose pose, ArborMeshEmitter.MeshConsumers consumers, int light, int degree) {
        VertexConsumer fungus = consumers.fungus();
        double under = undersideHeight(degree);
        double stalkBase = ArborSkeleton.trunkTop(degree) - .45;
        ArborFruitMeshes.organBody(pose, fungus, light,
                t -> new ArborCanopyGeometry.Point(0, stalkBase + (under + .15 - stalkBase) * t, 0),
                (t, phi) -> STALK_RADIUS * (1 - .18 * t) * Math.min(1, Math.sin(Math.PI * t) * 6),
                (t, phi) -> ArborFruitMeshes.shade(mix(CAP_CLOT, GILL_FLESH, (float) (.3 + .4 * t)),
                        ArborFruitMeshes.mottle(t, phi, 61)), 10, 16, 2, 2);
        ArborFruitMeshes.organBody(pose, fungus, light,
                t -> new ArborCanopyGeometry.Point(0, capHeight(t, under), 0),
                ArborApotheosisMeshes::capRadius, ArborApotheosisMeshes::capColor, 32, 40, 4, 2);
        for (int gill = 0; gill < GILLS; gill++) gillFin(pose, fungus, light, gill, under);
        emitWarts(pose, fungus, light, under);
        emitVeil(pose, fungus, under);
    }

    private static double rimRadius(double phi) {
        return CAP_RADIUS * (1 + .08 * Math.sin(5 * phi) + .04 * Math.sin(9 * phi + 1));
    }

    private static double capHeight(double t, double under) {
        if (t <= UNDERSIDE_END) {
            double s = t / UNDERSIDE_END;
            return under + UNDERSIDE_RISE * s * s;
        }
        if (t <= RIM_END) {
            // The rim runs out and down to a drooping lip below the gills.
            double s = (t - UNDERSIDE_END) / (RIM_END - UNDERSIDE_END);
            return under + UNDERSIDE_RISE - RIM_SAG * Math.sin(Math.PI * .5 * s);
        }
        double s = (t - RIM_END) / (1 - RIM_END);
        return under + UNDERSIDE_RISE - RIM_SAG + (CAP_DOME + RIM_SAG) * Math.sin(Math.PI * .5 * s);
    }

    private static double capRadius(double t, double phi) {
        double rim = rimRadius(phi);
        if (t <= UNDERSIDE_END) return rim * Math.sin(Math.PI * .5 * t / UNDERSIDE_END);
        if (t <= RIM_END) {
            double s = (t - UNDERSIDE_END) / (RIM_END - UNDERSIDE_END);
            return rim + RIM_FLARE * Math.sin(Math.PI * .5 * s);
        }
        double s = (t - RIM_END) / (1 - RIM_END);
        return (rim + RIM_FLARE) * Math.pow(Math.cos(Math.PI * .5 * s), .8);
    }

    private static int capColor(double t, double phi) {
        if (t <= UNDERSIDE_END) return mix(GILL_FLESH, CAP_CLOT, (float) (.35 * (1 - t / UNDERSIDE_END)));
        int flesh = mix(CAP_CLOT, CAP_TOP, (float) Math.min(1, (t - UNDERSIDE_END) * 2.2));
        return ArborFruitMeshes.shade(flesh, ArborFruitMeshes.mottle(t * 3, phi * 2, 67));
    }

    private static double undersideAt(double radius, double under) {
        double s = Math.min(1, radius / CAP_RADIUS);
        return under + UNDERSIDE_RISE * s * s - .01;
    }

    private static double gillDepth(double radius) {
        double reach = CAP_RADIUS * .95 - GILL_INNER;
        return GILL_DEPTH * Math.sin(Math.PI * Math.min(1, Math.max(0, (radius - GILL_INNER) / reach)));
    }

    /** A gill is a thin double-faced fin hanging radially from the underside of the cap. */
    private static void gillFin(PoseStack.Pose pose, VertexConsumer out, int light, int gill, double under) {
        double angle = gill * Math.PI * 2 / GILLS;
        double cx = Math.cos(angle), cz = Math.sin(angle), tx = -cz, tz = cx, half = .008;
        double[] radii = {GILL_INNER, CAP_RADIUS * .4, CAP_RADIUS * .7, CAP_RADIUS * .95};
        for (int face = -1; face <= 1; face += 2) {
            for (int i = 0; i < radii.length - 1; i++) {
                double r0 = radii[i], r1 = radii[i + 1], ox = tx * half * face, oz = tz * half * face;
                ArborCanopyGeometry.Point top0 = new ArborCanopyGeometry.Point(cx * r0 + ox, undersideAt(r0, under), cz * r0 + oz);
                ArborCanopyGeometry.Point top1 = new ArborCanopyGeometry.Point(cx * r1 + ox, undersideAt(r1, under), cz * r1 + oz);
                ArborCanopyGeometry.Point low0 = top0.add(new ArborCanopyGeometry.Point(0, -gillDepth(r0), 0));
                ArborCanopyGeometry.Point low1 = top1.add(new ArborCanopyGeometry.Point(0, -gillDepth(r1), 0));
                ArborCanopyGeometry.Point[] quad = face > 0
                        ? new ArborCanopyGeometry.Point[]{low0, low1, top1, top0}
                        : new ArborCanopyGeometry.Point[]{low0, top0, top1, low1};
                for (ArborCanopyGeometry.Point v : quad) {
                    ArborMeshEmitter.texturedVertex(pose, out, light, v.x(), v.y(), v.z(),
                            (float) (Math.hypot(v.x(), v.z()) / CAP_RADIUS), (float) (v.y() - under) * 4,
                            tx * face, 0, tz * face, GILL_FLESH);
                }
            }
        }
    }

    /** Pale raised warts scattered over the dome, sunk a little into the cap so they read as part of it. */
    private static void emitWarts(PoseStack.Pose pose, VertexConsumer out, int light, double under) {
        for (int i = 0; i < WARTS; i++) {
            double phi = i * 2.399963229728653;
            double t = RIM_END + (1 - RIM_END) * (.12 + .78 * ((i * 11) % WARTS) / (double) WARTS);
            double radius = capRadius(t, phi);
            double size = .11 + .07 * ((i * 7) % 5) / 4.0;
            ArborCanopyGeometry.Point at = new ArborCanopyGeometry.Point(Math.cos(phi) * radius * .97,
                    capHeight(t, under) - size * .2, Math.sin(phi) * radius * .97);
            ArborFruitMeshes.sphere(pose, out, light, at, size, (tt, pp) -> mix(PALE_SPOT, CAP_TOP, (float) (.25 * (1 - tt))), 4, 6);
        }
    }

    /** Short lace hanging from the rim: strands joined by a zig-zag of links. */
    private static void emitVeil(PoseStack.Pose pose, VertexConsumer out, double under) {
        int laceLight = LightTexture.pack(12, 9);
        for (int i = 0; i < VEIL_STRANDS; i++) {
            ArborCanopyGeometry.Point top = veilPoint(i, 0, under);
            ArborCanopyGeometry.Point bottom = veilPoint(i, 1, under);
            ArborMeshEmitter.texturedTubeProfiled(pose, out, laceLight,
                    List.of(top, veilPoint(i, .5, under), bottom), .014F, .009F, .009F, VEIL_LACE, .5F, 1.0F,
                    ArborRenderBudget.STREAMER_TUBE_SIDES, 1);
            for (int row = 1; row <= 1; row++) {
                double u = .55;
                int next = (i + 1) % VEIL_STRANDS;
                ArborCanopyGeometry.Point from = veilPoint(i, u + (i % 2 == 0 ? .08 : -.08), under);
                ArborCanopyGeometry.Point to = veilPoint(next, u + (i % 2 == 0 ? -.08 : .08), under);
                ArborMeshEmitter.texturedTubeProfiled(pose, out, laceLight, List.of(from, to), .008F, .008F, .008F,
                        VEIL_LACE, .3F, 1.0F, ArborRenderBudget.STREAMER_TUBE_SIDES, 1);
            }
        }
    }

    /** Point on veil strand {@code strand}, {@code u} from the rim (0) to its hem (1). */
    private static ArborCanopyGeometry.Point veilPoint(int strand, double u, double under) {
        double phi = strand * Math.PI * 2 / VEIL_STRANDS;
        double length = VEIL_LENGTH * (1 + .35 * Math.sin(3 * phi + 1));
        double radius = rimRadius(phi) * .97 + .04 * u;
        double y = under + UNDERSIDE_RISE - RIM_SAG * .6 - length * u;
        return new ArborCanopyGeometry.Point(Math.cos(phi) * radius, y, Math.sin(phi) * radius);
    }

    /** Lower edge of a gill, from inner to outer end, for the renderer's gill glow. */
    static ArborCanopyGeometry.Point[] gillGlowLine(int gill, int degree) {
        double under = undersideHeight(degree);
        double angle = gill * Math.PI * 2 / GILLS;
        double inner = CAP_RADIUS * .3, outer = CAP_RADIUS * .9;
        return new ArborCanopyGeometry.Point[]{
                new ArborCanopyGeometry.Point(Math.cos(angle) * inner, undersideAt(inner, under) - gillDepth(inner) - .006, Math.sin(angle) * inner),
                new ArborCanopyGeometry.Point(Math.cos(angle) * outer, undersideAt(outer, under) - gillDepth(outer) - .006, Math.sin(angle) * outer)};
    }

    static int gillCount() {
        return GILLS;
    }

    /** Where spore {@code index} leaves the gills; the renderer drifts it down and fades it out. */
    static ArborCanopyGeometry.Point sporeOrigin(int index, int degree) {
        double angle = index * 2.399963229728653;
        double radius = CAP_RADIUS * (.35 + .5 * ((index * 7) % 10) / 10.0);
        return new ArborCanopyGeometry.Point(Math.cos(angle) * radius, undersideAt(radius, undersideHeight(degree)) - .2,
                Math.sin(angle) * radius);
    }
}
