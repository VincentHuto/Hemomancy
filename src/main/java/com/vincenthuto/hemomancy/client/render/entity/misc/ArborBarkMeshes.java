package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborCanopyGeometry;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillLayout;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillVisualRules;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborSkeleton;
import net.minecraft.client.renderer.LightTexture;

import java.util.ArrayList;
import java.util.List;

import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.BARK_TINT;
import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.DARK_BARK_TINT;
import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.FAMILY_COLORS;
import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.mix;

/**
 * The Arbor's heartwood: a furrowed trunk that flares into root lobes at its base, a few knots, one faintly lit
 * vein per skill family climbing from the roots onto its bough, and a raw gash in the bark for every Pome eaten.
 * Pome gashes are cached geometry; their glowing seams are drawn by the renderer along {@link #pomeWoundLine}.
 */
final class ArborBarkMeshes {
    private static final float MIN_TRUNK_HEIGHT = 3.6F;
    private static final double BASE_RADIUS = .61;
    private static final double TOP_RADIUS = .22;
    // The trunk rounds off into its crown between these fractions of its height.
    private static final double TIP_START = .92;
    private static final double TIP_END = 1.04;
    private static final double BELOW_GROUND = .15;
    private static final int FURROWS = 9;
    private static final double FURROW_DEPTH = .10;
    private static final double FURROW_TWIST = 3.2;
    private static final int ROOT_LOBES = 7;
    private static final double FLARE = .9;
    private static final double FLARE_HEIGHT = .16;
    private static final int FURROW_COLOR = 0xFF24100F;
    private static final int VEIN_COLOR = 0xFFB0182C;
    // How far each vein winds around the trunk on its way up to its bough.
    private static final double VEIN_SPIRAL = 2.4;
    private static final int VEIN_TRUNK_SAMPLES = 18;
    private static final int RAW_WOUND = 0xFF5A1418;
    private static final int TRUNK_RINGS = 30;
    private static final int TRUNK_SEGMENTS = 36;
    private static final double[][] KNOTS = {{.30, 1.1, .17}, {.48, 3.6, .14}, {.63, 5.3, .16}};

    private ArborBarkMeshes() { }

    /** The trunk height actually built; boughs need some heartwood even on a Degree 0 sapling. */
    static float trunkHeight(int degree) {
        return Math.max(MIN_TRUNK_HEIGHT, (float) ArborOfWillVisualRules.treeHeight(degree));
    }

    static void emitTrunk(PoseStack.Pose pose, ArborMeshEmitter.MeshConsumers consumers, int light,
            float height, int degree, int pomes) {
        double top = height * ArborSkeleton.TRUNK_SPAN;
        double span = top * TIP_END + BELOW_GROUND;
        // Rings crowd toward the base so the root flare stays round.
        java.util.function.DoubleUnaryOperator heightAt = t -> -BELOW_GROUND + Math.pow(t, 1.3) * span;
        ArborFruitMeshes.organBody(pose, consumers.bark(), light,
                t -> centre(heightAt.applyAsDouble(t), top),
                (t, phi) -> trunkRadius(heightAt.applyAsDouble(t), phi, top) * Math.min(1, t * 40),
                (t, phi) -> barkColor(heightAt.applyAsDouble(t) / top, phi),
                TRUNK_RINGS, TRUNK_SEGMENTS, 3, (float) Math.max(2, top));
        for (double[] knot : KNOTS) {
            double y = knot[0] * top, phi = knot[1], size = knot[2] * Math.min(1, height / 6.0);
            ArborCanopyGeometry.Point at = surfacePoint(y, phi, top, -size * .4);
            ArborFruitMeshes.sphere(pose, consumers.bark(), light, at, size,
                    (t, p) -> ArborFruitMeshes.shade(mix(DARK_BARK_TINT, FURROW_COLOR, (float) (.5 * (1 - t))),
                            ArborFruitMeshes.mottle(t, p, 5)), 5, 8);
        }
        emitVeins(pose, consumers, top, degree);
        for (int i = 0; i < Math.min(9, pomes); i++) emitPomeWound(pose, consumers, light, top, i);
    }

    /** Centre of the heartwood at height {@code y}; the trunk leans and wanders slightly as it climbs. */
    static ArborCanopyGeometry.Point centre(double y, double top) {
        double t = Math.max(0, Math.min(1, y / top));
        return new ArborCanopyGeometry.Point(Math.sin(t * 4.8) * (.06 + .08 * t), y, Math.cos(t * 4.1) * (.05 + .06 * t));
    }

    static double trunkRadius(double y, double phi, double top) {
        double t = Math.max(0, y / top);
        double taper = BASE_RADIUS + (TOP_RADIUS - BASE_RADIUS) * Math.min(1, t);
        double tip = t > TIP_START ? Math.sqrt(Math.max(0, 1 - Math.pow((t - TIP_START) / (TIP_END - TIP_START), 2))) : 1;
        double flare = 1 + FLARE * Math.pow(Math.max(0, 1 - t / FLARE_HEIGHT), 2) * (.55 + .45 * Math.cos(ROOT_LOBES * phi));
        return taper * tip * flare * (1 - FURROW_DEPTH * furrow(t, phi));
    }

    /** 1 in the bottom of a bark furrow, 0 on the plates between; furrows twist gently up the trunk. */
    private static double furrow(double t, double phi) {
        return Math.pow(Math.abs(Math.cos(phi * FURROWS * .5 + t * FURROW_TWIST)), 6);
    }

    private static int barkColor(double t, double phi) {
        int plate = mix(DARK_BARK_TINT, BARK_TINT, (float) Math.min(1, Math.max(0, t * 3)));
        int furrowed = mix(plate, FURROW_COLOR, (float) (.65 * furrow(t, phi)));
        return ArborFruitMeshes.shade(furrowed, ArborFruitMeshes.mottle(t * 6, phi, 13) * 1.5);
    }

    /** A point on the bark at height {@code y} and angle {@code phi}, lifted {@code lift} off the surface. */
    static ArborCanopyGeometry.Point surfacePoint(double y, double phi, double top, double lift) {
        ArborCanopyGeometry.Point centre = centre(y, top);
        double r = trunkRadius(y, phi, top) + lift;
        return new ArborCanopyGeometry.Point(centre.x() + Math.cos(phi) * r, y, centre.z() + Math.sin(phi) * r);
    }

    /**
     * Each family's vein spirals up from the roots to where its bough leaves the trunk, then rides the top of
     * the bough over its crest. A little block light keeps them faintly lit in the dark chamber.
     */
    private static void emitVeins(PoseStack.Pose pose, ArborMeshEmitter.MeshConsumers consumers, double top, int degree) {
        int veinLight = LightTexture.pack(12, 9);
        for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
            double angle = ArborSkeleton.familyAngle(family);
            double peel = ArborSkeleton.peelHeight(family, degree);
            List<ArborCanopyGeometry.Point> vein = new ArrayList<>(24);
            // Fine sampling keeps straight segments from cutting into the furrows they cross.
            for (int k = 0; k <= VEIN_TRUNK_SAMPLES; k++) {
                double u = k / (double) VEIN_TRUNK_SAMPLES;
                double y = .05 * top + (peel - .05 * top) * u;
                vein.add(surfacePoint(y, angle - VEIN_SPIRAL * (1 - u), top, .02));
            }
            double reach = .8 * ArborSkeleton.stationParameter(family, 1, degree);
            for (int k = 0; k <= 4; k++) {
                double u = reach * k / 4.0;
                ArborCanopyGeometry.Point onBough = ArborSkeleton.curvePoint(family, degree, u);
                vein.add(onBough.add(new ArborCanopyGeometry.Point(0, .17 - .05 * k / 4.0, 0)));
            }
            int color = mix(VEIN_COLOR, FAMILY_COLORS.get(ArborOfWillLayout.orderedFamilies().get(family)), .3F);
            ArborMeshEmitter.texturedTubeProfiled(pose, consumers.mycelium(), veinLight, vein, .036F, .016F, .016F,
                    color, 3.0F, 1.0F, ArborRenderBudget.VEIN_TUBE_SIDES, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        }
    }

    /** Raised raw lips either side of a vertical gash; the seam between them glows in the renderer. */
    private static void emitPomeWound(PoseStack.Pose pose, ArborMeshEmitter.MeshConsumers consumers, int light,
            double top, int index) {
        double phi = pomeAngle(index);
        double y0 = pomeHeight(index, top) - .22, y1 = pomeHeight(index, top) + .22;
        for (int side = -1; side <= 1; side += 2) {
            List<ArborCanopyGeometry.Point> lip = new ArrayList<>(5);
            for (int k = 0; k <= 4; k++) {
                double u = k / 4.0;
                double y = y0 + (y1 - y0) * u;
                double open = Math.sin(Math.PI * u);
                double offset = side * (.012 + .035 * open) / Math.max(.1, trunkRadius(y, phi, top));
                lip.add(surfacePoint(y, phi + offset, top, .004));
            }
            ArborMeshEmitter.texturedTubeProfiled(pose, consumers.bark(), light, lip, .022F, .012F, .012F,
                    RAW_WOUND, .5F, 1.0F, ArborRenderBudget.VEIN_TUBE_SIDES, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        }
    }

    /** Ends of a Pome gash's seam on the bark, in Arbor-local coordinates, for the renderer's glow. */
    static ArborCanopyGeometry.Point[] pomeWoundLine(int index, int degree) {
        double top = trunkHeight(degree) * ArborSkeleton.TRUNK_SPAN;
        double phi = pomeAngle(index), y = pomeHeight(index, top);
        return new ArborCanopyGeometry.Point[]{surfacePoint(y - .2, phi, top, .01), surfacePoint(y + .2, phi, top, .01)};
    }

    private static double pomeAngle(int index) {
        return index * 2.399963229728653 + .3;
    }

    private static double pomeHeight(int index, double top) {
        return (.14 + (index % 5) * .11) * top;
    }
}
