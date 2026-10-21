package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.card;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.ribbonPath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineMendingRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Sanguine Mending: blood leaves the wrist and winds out along the held tool inside a thin translucent sheath. Each
 * dark crack in the tool is stitched closed by pale blood thread in turn and set with a blood-iron rivet, then the
 * blood draws back into the wrist. Iron rivets go to the solid pass; everything else is on the Ductilis flow
 * material. Points are world-axis offsets from the caster's feet.
 */
final class SanguineMendingGeometry {
    private static final int BLOOD = 0x9C263B, CORE = 0xFFD2C4, TINT = 0xFFFFFF;
    /** Ductilis shader shapes: the blood band, the soft glow and the bright spark. */
    private static final int BLOOD_SHAPE = 4, GLOW = 1, SPARK = 3, LINE = 0;
    private static final Vec3 UP = CasterFrame.UP;

    private SanguineMendingGeometry() {}

    static boolean handles(Form form) {
        return form == Form.MENDING;
    }

    /** The held tool's axis: raised forward from the hand in the cast pose. */
    static Vec3 toolAxis(CasterFrame frame) {
        Vec3 look = frame.look().lengthSqr() < 1e-6 ? frame.forward() : frame.look();
        return look.scale(0.75).add(UP.scale(0.65)).normalize();
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer iron, VertexConsumer flow, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha, CasterFrame frame) {
        if (alpha <= 0) return;
        int seed = Math.floorMod(packet.entityId(), 113);
        double s = frame.scale();
        Vec3 grip = frame.hand(), axis = toolAxis(frame);
        Vec3 across = axis.cross(UP).lengthSqr() < 1e-6 ? frame.right() : axis.cross(UP).normalize();
        Vec3 normal = across.cross(axis).normalize();
        double length = (frame.firstPerson() ? 0.55 : 0.75) * s;
        Vec3 wrist = grip.subtract(axis.scale(0.22 * s)).subtract(normal.scale(0.04 * s));
        double drawn = SanguineMendingRules.draw(age) * (1 - SanguineMendingRules.recede(age));
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * 0.6)), 4);
        float a = alpha * (frame.firstPerson() ? 0.75f : 1f);

        // 1. Sheath: a thin translucent band of blood along the tool as far as the blood has reached.
        if (drawn > 0.02) {
            List<Vec3> sheath = List.of(grip, grip.add(axis.scale(length * drawn * 0.5)), grip.add(axis.scale(length * drawn)));
            ribbonPath(p, flow, sheath, camera, 0.07 * s, 0.03 * s, GLOW, seed, BLOOD, a * 0.3f);
        }

        // 2. Strands wind out of the wrist and along the tool.
        for (int strand = 0; strand < 3; strand++) {
            List<Vec3> path = new ArrayList<>(13);
            for (int i = 0; i <= 12; i++) {
                double t = i / 12.0;
                double along = -0.22 * s + (length + 0.22 * s) * t * drawn;
                double angle = t * 9 + strand * Math.PI * 2 / 3 + time * 0.2;
                double radius = (0.045 + 0.015 * Math.sin(t * Math.PI)) * s;
                path.add(grip.add(axis.scale(along)).add(across.scale(Math.cos(angle) * radius)).add(normal.scale(Math.sin(angle) * radius)));
            }
            if (drawn <= 0.02) continue;
            float strandAlpha = a * (0.8f + 0.2f * pulse);
            ribbonPath(p, flow, path, camera, 0.022 * s, 0.01 * s, BLOOD_SHAPE, seed + 10 + strand, BLOOD, strandAlpha);
            ribbonPath(p, flow, path, camera, 0.007 * s, 0.003 * s, LINE, seed + 13 + strand, CORE, strandAlpha * 0.7f);
        }
        card(p, flow, wrist, right, up, 0.07 * s * (1 + 0.4 * pulse), 0.07 * s * (1 + 0.4 * pulse), SPARK, seed + 20, BLOOD,
                a * (float) (0.5 + 0.5 * drawn));

        // 3. Cracks: dark zigzags that the thread stitches closed, one after another, each set with a rivet.
        for (int crack = 0; crack < SanguineMendingRules.CRACKS; crack++) {
            double at = SanguineMendingRules.crackAt(crack);
            if (at > drawn + 0.05) continue;
            double seal = SanguineMendingRules.seal(age, crack);
            Vec3 center = grip.add(axis.scale(length * at));
            double open = (1 - seal) * 0.05 * s;
            List<Vec3> zig = new ArrayList<>(6);
            for (int k = 0; k < 6; k++) {
                double t = k / 5.0 - 0.5;
                zig.add(center.add(across.scale(t * 0.12 * s)).add(axis.scale((k % 2 == 0 ? 1 : -1) * (0.008 * s + open * 0.3))));
            }
            ribbonPath(p, flow, zig, camera, 0.006 * s + open * 0.25, BLOOD_SHAPE, seed + 30 + crack, BLOOD, a * (float) (1 - seal * 0.7));
            int stitches = (int) Math.ceil(seal * 3);
            for (int k = 0; k < stitches; k++) {
                double t = (k - 1) * 0.04 * s;
                Vec3 mid = center.add(across.scale(t));
                ribbonPath(p, flow, List.of(mid.subtract(axis.scale(0.03 * s)), mid.add(axis.scale(0.03 * s))), camera,
                        0.006 * s, LINE, seed + 40 + crack * 3 + k, CORE, a);
            }
            if (seal > 0.6) {
                float set = (float) Math.min(1, (seal - 0.6) / 0.3);
                FerricDuctilisGeometry.plate(p, iron, center.add(normal.scale(0.02 * s)), 0.035 * s * set, 0.035 * s * set,
                        0.02 * s, seed + 50 + crack, a * set);
                double flash = Math.sin(Math.min(1, (seal - 0.6) / 0.4) * Math.PI);
                card(p, flow, center, right, up, 0.09 * s * flash, 0.09 * s * flash, SPARK, seed + 60 + crack, TINT, a * (float) flash);
            }
        }
    }
}
