package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;

/** First-person hand roles and limits. Tracks are authored right-handed; the lead hand is the main arm. */
public final class FirstPersonHandRules {
    /** Vanilla hand rest in camera blocks: the right hand sits at (+0.64, −0.6, −0.72). */
    private static final float REST_X = .64F, REST_Y = -.6F, REST_Z = -.72F;
    /** Band half-width and raised line at the rest depth; both scale with depth, as the projection does. */
    private static final float BAND = .12F, RAISED = -.35F;
    private static final float MAX_X = .35F, MAX_Y = .25F, MAX_Z = .2F;
    /**
     * The empty hand at vanilla rest, right arm, in camera blocks: the fist (bottom face centre of the arm box) and the
     * cut end (top face centre), from the vanilla first-person arm transform, averaged over the slim and wide models.
     */
    private static final double FIST_X = .735, FIST_Y = -.483, FIST_Z = -1.160, CUT_X = .485, CUT_Y = -.836, CUT_Z = -.547;
    /** View-tangent limits for {@link #onScreen} and the fist half of {@link #insideCrosshairZone}. */
    private static final double FIST_SIDE = .95, FIST_LOW = -.60, FIST_HIGH = .45, CUT_LOW = -.80, CUT_SIDE = 1.25, FIST_CENTRE = .16;
    private FirstPersonHandRules() {}

    public static HandPose.Hand handFor(HandPose pose, boolean rightArm, boolean leftHanded) {
        var p = leftHanded ? pose.mirror() : pose;
        return rightArm ? p.lead() : p.support();
    }
    /** Humilis and Awakened keep the off hand vanilla. Null arguments mean the other lane. */
    public static boolean affectsOffHand(EnumManipulationRank rank, EnumClarityStage stage) {
        if (rank != null) return rank != EnumManipulationRank.HUMILIS;
        if (stage != null) return stage != EnumClarityStage.AWAKENED;
        return true;
    }
    /** Bespoke styles author their own support hand at every rank, so only school bases follow the rank rule. */
    public static boolean affectsOffHand(EnumManipulationRank rank, EnumClarityStage stage, CastStyle style) {
        return style != null && style != CastStyle.SCHOOL || affectsOffHand(rank, stage);
    }
    public static HandPose.Hand clamp(HandPose.Hand h) {
        float tx = Math.clamp(h.tx(), -MAX_X, MAX_X), ty = Math.clamp(h.ty(), -MAX_Y, MAX_Y), tz = Math.clamp(h.tz(), -MAX_Z, MAX_Z);
        return tx == h.tx() && ty == h.ty() && tz == h.tz() ? h : new HandPose.Hand(tx, ty, tz, h.rx(), h.ry(), h.rz());
    }
    /**
     * Carries the hand through the render transform (translate, then rotate X, Y, Z about the eye) and tests whether it
     * covers the crosshair: either the rest point lands in the central band, raised toward the crosshair, or the fist
     * itself lands in the central zone (|x|, y within {@link #FIST_CENTRE} of the crosshair in view tangents). The fist
     * test catches gestures that fold both hands to the middle while the rest point stays low (Phoenix Debt's fold).
     */
    public static boolean insideCrosshairZone(HandPose.Hand h, boolean rightArm) {
        var p = carry(h, rightArm ? REST_X : -REST_X, REST_Y, REST_Z);
        if (p[2] <= -.05) {
            double depth = p[2]/REST_Z;
            if (Math.abs(p[0]) < BAND*depth && p[1] > RAISED*depth) return true;
        }
        var fist = fistView(h, rightArm);
        return fist != null && Math.abs(fist[0]) < FIST_CENTRE && fist[1] > -FIST_CENTRE;
    }
    /**
     * Whether the posed hand reads on screen: its fist sits inside a conservative visible frame and the arm's cut end
     * stays out of it. Views are x/depth, y/depth tangents; the hand projection is a fixed 70 degree vertical FOV, so the
     * frame edge is at y = ±0.70 and, at 16:10, x = ±1.12 (±0.93 at 4:3).
     * <p>Calibrated on the 2026-10-19 two-client captures (1280×800, slim): the vanilla rest fist sits at (0.63, −0.42)
     * and reads clearly; Lux Mediocritas (−0.74), Ductilis Magister (−0.69), Animus Perfectus (−0.71), Ferric Humilis
     * (−0.89), Radiant Wake (−0.80) and Flammeus Perfectus (−1.6) showed no hand, Iron Choir (−0.69) and Crimson
     * Coronation (≈−0.63) only fingertips at the hotbar, Blood Rush (−0.59) a readable fist; hence {@link #FIST_LOW}.
     * The cut end showed in Ironheart (cut at −0.48 / −0.27), faintly in Ground Touch (−0.66) and Carrion Communion
     * (−0.76) and not at all at −0.84 (Penumbral Drift) or below; hence {@link #CUT_LOW}. Raising a hand more than about
     * 0.2 blocks while pitching it up (positive rx) is what brings the cut end into view.
     */
    public static boolean onScreen(HandPose.Hand h, boolean rightArm) {
        var fist = fistView(h, rightArm);
        if (fist == null || Math.abs(fist[0]) > FIST_SIDE || fist[1] < FIST_LOW || fist[1] > FIST_HIGH) return false;
        var cut = cutView(h, rightArm);
        return cut == null || cut[1] <= CUT_LOW || cut[1] >= -CUT_LOW || Math.abs(cut[0]) >= CUT_SIDE;
    }
    /** Projected (x/depth, y/depth) of the fist and of the arm's cut end; null when behind the eye. */
    static double[] fistView(HandPose.Hand h, boolean rightArm) { return view(carry(h, rightArm ? FIST_X : -FIST_X, FIST_Y, FIST_Z)); }
    static double[] cutView(HandPose.Hand h, boolean rightArm) { return view(carry(h, rightArm ? CUT_X : -CUT_X, CUT_Y, CUT_Z)); }
    private static double[] view(double[] p) { return p[2] > -.05 ? null : new double[]{p[0]/-p[2], p[1]/-p[2]}; }
    /** Applies the hand transform to a camera-space point. A pose stack applies its last rotation first: Z, Y, X, then the translation. */
    private static double[] carry(HandPose.Hand h, double x, double y, double z) {
        double c = Math.cos(h.rz()), s = Math.sin(h.rz()), t = x*c - y*s;
        y = x*s + y*c; x = t;
        c = Math.cos(h.ry()); s = Math.sin(h.ry()); t = x*c + z*s;
        z = -x*s + z*c; x = t;
        c = Math.cos(h.rx()); s = Math.sin(h.rx()); t = y*c - z*s;
        z = y*s + z*c; y = t;
        return new double[]{x + h.tx(), y + h.ty(), z + h.tz()};
    }
}
