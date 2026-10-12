package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.basis;
import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.smooth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ForgedNeedleRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Forged Blood Needles (Ferric). While charging, blood is drawn from the wrist in Ferric gather ribbons and hardens
 * one needle at a time into square-section iron flechettes with tail fins; lances grow backward barbs, and a full
 * charge draws magnetic field lines into the bundle. In flight the iron trails a thin blood wake; lodged, it shivers,
 * beads blood at the wound and stains. Iron goes to the solid Ferric pass, blood and field to the Ductilis flow pass;
 * each entry point is called once per pass and the other buffer is {@link FerricDuctilisGeometry#DISCARD}.
 */
final class ForgedNeedleGeometry {
    private static final int WHITE = 0xFFFFFF, CORE = 0xFFD2C4, FIELD = 0xBE8C73;
    /** Ductilis shader shapes: blood gather and soft glow. */
    private static final int BLOOD_SHAPE = 4, GLOW = 1;
    static final float PUNCTURE = .4F;

    private ForgedNeedleGeometry() {}

    static boolean handlesCharge(Form form) {
        return form == Form.NEEDLE_CHARGE || form == Form.FAN_CHARGE || form == Form.LANCE_CHARGE;
    }

    /** One iron flechette from {@code base} along {@code axis}: tapered square shaft, swollen bodkin head, tail fins. */
    static void flechette(PoseStack p, VertexConsumer iron, Vec3 base, Vec3 axis, double length, double width,
            boolean lance, int seed, float alpha) {
        if (iron == FerricDuctilisGeometry.DISCARD || length < 1e-3 || alpha <= 0) return;
        Vec3[] b = basis(axis);
        double[] stations = {0, ForgedNeedleRules.SHOULDER, 1};
        Vec3[][] rings = new Vec3[stations.length][4];
        for (int s = 0; s < stations.length; s++) {
            Vec3 center = base.add(axis.scale(length * stations[s]));
            double r = ForgedNeedleRules.profile(stations[s], width);
            for (int k = 0; k < 4; k++) {
                double a = k * Math.PI / 2 + Math.PI / 4;
                rings[s][k] = center.add(b[0].scale(Math.cos(a) * r)).add(b[1].scale(Math.sin(a) * r));
            }
        }
        for (int s = 0; s + 1 < stations.length; s++)
            for (int k = 0; k < 4; k++)
                FerricDuctilisGeometry.quad(p, iron, rings[s][k], rings[s][(k + 1) % 4], rings[s + 1][(k + 1) % 4],
                        rings[s + 1][k], 0, seed + s * 4 + k, WHITE, alpha);
        FerricDuctilisGeometry.quad(p, iron, rings[0][3], rings[0][2], rings[0][1], rings[0][0], 0, seed + 12, WHITE, alpha);
        // Tail fins: two crossed blades that keep the flechette true.
        for (int fin = 0; fin < 4; fin++) {
            Vec3 out = (fin % 2 == 0 ? b[0] : b[1]).scale(fin < 2 ? 1 : -1);
            Vec3 root = base.add(axis.scale(length * 0.16));
            FerricDuctilisGeometry.quad(p, iron, root, base.subtract(axis.scale(length * 0.04)),
                    base.subtract(axis.scale(length * 0.08)).add(out.scale(width * 2.6)),
                    root.subtract(axis.scale(length * 0.02)).add(out.scale(width * 1.4)), 0, seed + 20 + fin, WHITE, alpha);
        }
        if (!lance) return;
        // Lance barbs: hooked spurs angled back toward the tail, so a struck body cannot pull free.
        for (int i = 0; i < ForgedNeedleRules.LANCE_BARBS; i++) {
            Vec3 side = (i % 2 == 0 ? b[0] : b[1]).scale(i == 1 ? -1 : 1);
            Vec3 across = i % 2 == 0 ? b[1] : b[0];
            Vec3 root = base.add(axis.scale(length * ForgedNeedleRules.barbStation(i)));
            Vec3 tip = root.subtract(axis.scale(length * 0.12)).add(side.scale(width * 2.6));
            Vec3 lip = root.add(side.scale(width * 0.8));
            FerricDuctilisGeometry.quad(p, iron, lip.add(across.scale(width * 0.4)), lip.subtract(across.scale(width * 0.4)),
                    tip, tip, 0, seed + 30 + i, WHITE, alpha);
            FerricDuctilisGeometry.quad(p, iron, lip.add(axis.scale(length * 0.05)), lip.add(across.scale(width * 0.4)),
                    tip, tip, 0, seed + 34 + i, WHITE, alpha);
        }
    }

    /** Iron body of a needle in flight or lodged; positions are world coordinates relative to the pose. */
    static void flightIron(PoseStack p, VertexConsumer iron, List<Vec3> path, Vec3 heading, Vec3 camera, float age,
            int seed, float alpha, boolean lance, boolean lodged, float shake) {
        if (path.isEmpty() || alpha <= 0) return;
        Vec3 head = path.getLast();
        Vec3 axis = heading.lengthSqr() > 1e-8 ? heading.normalize() : new Vec3(0, -1, 0);
        double length = (lance ? ForgedNeedleRules.LANCE_LENGTH : ForgedNeedleRules.LENGTH) * (.7 + .3 * smooth(age / 2.0));
        Vec3 base = head.subtract(axis.scale(length * .5));
        if (lodged) {
            // The buried point stays put while the shaft shivers.
            Vec3 side = LuxUmbraGeometry.facingSide(axis, camera.subtract(head));
            Vec3 tip = head.add(axis.scale(length * .5));
            base = base.add(side.scale(Math.sin(shake * 3) * shake * .02));
            axis = tip.subtract(base).normalize();
        }
        flechette(p, iron, base, axis, length, lance ? ForgedNeedleRules.LANCE_WIDTH : ForgedNeedleRules.WIDTH, lance, seed, alpha);
    }

    /** Blood wake behind a needle in flight, a hot glint at its point, and the bead and stain of a lodged one. */
    static void flightFlow(PoseStack p, VertexConsumer flow, List<Vec3> path, Vec3 heading, Vec3 camera, Vec3 right,
            Vec3 up, double time, float age, int seed, float alpha, float wake, boolean lance, boolean lodged) {
        if (path.isEmpty()) return;
        Vec3 head = path.getLast();
        if (path.size() >= 2 && wake > 0)
            LuxUmbraGeometry.ribbonPath(p, flow, path, camera, .003, lance ? .028 : .02, BLOOD_SHAPE, seed, FerricDuctilisGeometry.BLOOD, wake * .55f);
        Vec3 axis = heading.lengthSqr() > 1e-8 ? heading.normalize() : new Vec3(0, -1, 0);
        double length = lance ? ForgedNeedleRules.LANCE_LENGTH : ForgedNeedleRules.LENGTH;
        Vec3 tip = head.add(axis.scale(length * .5));
        if (lodged) {
            Vec3 wound = tip.subtract(axis.scale(length * .1));
            FerricDuctilisGeometry.card(p, flow, wound, right, up, .035, .05, BLOOD_SHAPE, seed + 9, FerricDuctilisGeometry.BLOOD, alpha);
            FerricDuctilisGeometry.ribbon(p, flow, wound, wound.add(0, -.12, 0), .012, .01, time, seed + 10, camera,
                    FerricDuctilisGeometry.BLOOD, alpha * .8f);
            return;
        }
        float glint = (float) (.5 + .5 * Math.pow(Math.max(0, Math.sin(time * .6 + seed)), 4));
        FerricDuctilisGeometry.card(p, flow, tip.subtract(axis.scale(length * .06)), right, up, .035, .035, GLOW, seed + 11, CORE, alpha * glint);
    }

    /** Charge preview: the same layout as the blood needles, forged in iron from gathered blood. */
    static void charge(ManipulationVisualPacket packet, PoseStack p, VertexConsumer iron, VertexConsumer flow, Vec3 camera,
            Vec3 right, Vec3 up, double time, float alpha) {
        if (alpha <= 0) return;
        Form form = packet.form();
        boolean fan = form == Form.FAN_CHARGE, lance = form == Form.LANCE_CHARGE;
        Vec3 end = packet.to().subtract(packet.from());
        Vec3 aim = end.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : end.normalize();
        Vec3 focus = ChargeVisualGeometry.focus(form, aim);
        Vec3[] b = basis(aim);
        double progress = Mth.clamp(packet.radius(), 0, 1);
        int seed = Math.floorMod(packet.entityId(), 113);
        int count = fan ? 7 : lance ? 3 : 4;
        Vec3 source = focus.subtract(aim.scale(.75)).add(b[1].scale(-.3));
        boolean ready = progress >= .999;
        float pulse = ready ? (float) Math.pow(Math.max(0, Math.sin(time * .6)), 4) : 0;
        double width = lance ? ForgedNeedleRules.LANCE_WIDTH : ForgedNeedleRules.WIDTH;
        for (int i = 0; i < count; i++) {
            Vec3 offset, axis;
            if (fan) {
                double t = (i - (count - 1) / 2.0) / ((count - 1) / 2.0);
                offset = b[0].scale(t * .42).add(b[1].scale(-Math.abs(t) * .05));
                axis = aim.add(b[0].scale(t * .35)).normalize();
            } else {
                double a = i * Math.PI * 2 / count + time * (lance ? .02 : .04);
                double ring = lance ? .07 : .17;
                offset = b[0].scale(Math.cos(a) * ring).add(b[1].scale(Math.sin(a) * ring));
                axis = aim;
            }
            double own = ForgedNeedleRules.forged(progress, i, count);
            double length = (lance ? ForgedNeedleRules.LANCE_LENGTH * .8 : ForgedNeedleRules.LENGTH * .6) * (.3 + .7 * own);
            Vec3 center = focus.add(offset);
            Vec3 base = center.subtract(axis.scale(length * .45));
            // The iron condenses out of the blood: faces fill in as the needle hardens.
            flechette(p, iron, base, axis, length, width * (.6 + .4 * own), lance, seed + i * 11, alpha * (float) (.15 + .85 * own));
            if (own < 1)
                FerricDuctilisGeometry.ribbon(p, flow, source, base, .03, .08, time, seed + 60 + i, camera,
                        FerricDuctilisGeometry.BLOOD, alpha * (float) (1 - own) * .9f);
            else if (pulse > 0)
                FerricDuctilisGeometry.card(p, flow, base.add(axis.scale(length)), right, up, .05, .05, GLOW, seed + 80 + i, CORE, alpha * pulse);
        }
        // A full charge magnetises the bundle: field lines draw in from around the aim.
        if (ready)
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + time * .03;
                Vec3 dir = b[0].scale(Math.cos(a)).add(b[1].scale(Math.sin(a)));
                double travel = (time * .05 + i * .17) % 1;
                FerricDuctilisGeometry.ribbon(p, flow, focus.add(dir.scale(.6 - .25 * travel)), focus.add(dir.scale(.22)),
                        .016, .03, time, seed + 90 + i, camera, FIELD, alpha * .6f * (float) Math.sin(travel * Math.PI));
            }
    }
}
