package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.*;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Blood Aneurysm: a vessel knot swells at the caster's hand, its beat quickening with the charge. On release a bulging
 * vessel reaches the victim and a veined sac swells inside them, then tears: a hot flash, flung shreds of the sac,
 * ruptured vessels whipping out and going limp, a sphere of spray, and a clotted haze that lingers.
 */
final class BloodAneurysmGeometry {
    static final int RUPTURE_TICKS = 28, SWELL_END = 4, TEAR_END = 6;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private BloodAneurysmGeometry() {}

    static boolean handles(Form form) {
        return form == Form.ANEURYSM_CHARGE || form == Form.ANEURYSM_RUPTURE;
    }

    /** Heart rate of the knot: it beats faster as the charge nears bursting. */
    static double beatRate(double progress) {
        return .18 + .42 * Mth.clamp(progress, 0, 1);
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer blood, VertexConsumer rot, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha) {
        if (alpha <= 0) return;
        if (packet.form() == Form.ANEURYSM_CHARGE) charge(packet, p, blood, camera, right, up, time, alpha);
        else rupture(packet, p, blood, rot, camera, right, up, time, age, alpha);
    }

    private static void charge(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right,
            Vec3 up, double time, float alpha) {
        Vec3 end = packet.to().subtract(packet.from());
        Vec3 aim = end.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : end.normalize();
        double progress = Mth.clamp(packet.radius(), 0, 1);
        int seed = Math.floorMod(packet.entityId(), 113);
        boolean ready = progress >= .999;
        // On the brink it trembles.
        Vec3 shake = ready ? right.scale(Math.sin(time * 3.1) * .012).add(up.scale(Math.cos(time * 2.7) * .012)) : Vec3.ZERO;
        Vec3 focus = ChargeVisualGeometry.focus(Form.ANEURYSM_CHARGE, aim).add(shake);
        Vec3[] b = basis(aim);
        double beat = 1 + .14 * Math.pow(Math.max(0, Math.sin(time * beatRate(progress) * Math.PI)), 6);
        double size = (.05 + .11 * smooth(progress)) * beat;

        // 1. Feeding vessels from the wrist, thinning as the knot fills.
        Vec3 source = focus.subtract(aim.scale(.75)).add(b[1].scale(-.3));
        for (int i = 0; i < 3; i++)
            stream(p, v, source.add(b[0].scale((i - 1) * .06)), focus, camera, .035, .08, time * .05 + i, RIBBON,
                    seed + i, TINT, alpha * (float) (1 - progress * .7) * .8f);

        // 2. The knot: sheath, glossy body, clotted wall.
        card(p, v, focus, right, up, size * 1.6, size * 1.6, FOCUS, seed + 4, TINT, alpha * .3f);
        card(p, v, focus, right, up, size, size * 1.1, FOCUS, seed + 5, TINT, alpha);
        card(p, v, focus.add(right.scale(size * .3)).add(up.scale(-size * .25)), right, up, size * .45, size * .4, FOCUS,
                seed + 6, CLOT, alpha * .8f);
        card(p, v, focus.add(right.scale(-size * .3)).add(up.scale(size * .3)), right, up, size * .25, size * .25, FOCUS,
                seed + 7, CORE, alpha * (float) (.5 + .5 * (beat - 1) / .14));

        // 3. Veins wrapping the knot, swelling with each beat.
        for (int vein = 0; vein < 4; vein++) {
            List<Vec3> loop = new ArrayList<>(10);
            double tilt = vein * Math.PI / 4 + seed;
            for (int i = 0; i <= 9; i++) {
                double a = i * Math.PI / 9 - Math.PI / 2 + time * .02;
                Vec3 ring = right.scale(Math.cos(a) * size * 1.05).add(up.scale(Math.sin(a) * size * 1.05));
                Vec3 tilted = ring.scale(Math.cos(tilt)).add(aim.scale(Math.sin(tilt) * size * .4 * Math.sin(a)));
                loop.add(focus.add(tilted));
            }
            ribbonPath(p, v, loop, camera, size * .1, size * .18 * beat, RIBBON, seed + 10 + vein, vein % 2 == 0 ? CLOT : TINT, alpha * .9f);
        }
    }

    /** Where shred or droplet {@code i} sits {@code age} ticks after the tear: flung outward in every direction, then falling. */
    static Vec3 spray(int i, int seed, double age, double radius) {
        double spin = ThermalMotion.random(seed, i) * Math.PI * 2;
        double rise = ThermalMotion.random(seed, i + 41) * 2 - 1;
        double h = Math.sqrt(1 - rise * rise);
        Vec3 heading = new Vec3(Math.cos(spin) * h, rise * .8 + .25, Math.sin(spin) * h).normalize();
        double t = Math.max(0, age - SWELL_END);
        double speed = radius * .3 * (.5 + .5 * ThermalMotion.random(seed, i + 83));
        return heading.scale(speed * (1 - Math.exp(-t * .3)) / .3).add(0, -.014 * t * t, 0);
    }

    private static void rupture(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, VertexConsumer rot, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha) {
        double r = Math.max(1, packet.radius());
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        Vec3 caster = packet.to().subtract(packet.from());
        double swell = smooth(age / SWELL_END);
        boolean torn = age >= SWELL_END;
        float settle = (float) smooth((age - 12) / (RUPTURE_TICKS - 12.0));

        // 1. Swell: a bulging vessel reaches the victim, a veined sac swelling inside them.
        if (!torn) {
            if (caster.lengthSqr() > 1) {
                Vec3 mid = caster.scale(.5).add(UP.scale(Math.min(1.2, caster.length() * .12)));
                double reach = smooth(age / 2.0);
                List<Vec3> vessel = new ArrayList<>(13);
                for (int i = 0; i <= 12; i++) {
                    double t = 1 - i / 12.0 * reach;
                    double u = 1 - t;
                    vessel.add(caster.scale(t * t).add(mid.scale(2 * t * u)));
                }
                ribbonPath(p, v, vessel, camera, .05, .16 * swell + .05, RIBBON, seed, TINT, alpha * .8f);
                ribbonPath(p, v, vessel, camera, .015, .05, RIBBON, seed + 1, CLOT, alpha);
            }
            double sac = .25 + .45 * swell;
            card(p, v, Vec3.ZERO, right, up, sac * 1.3, sac * 1.3, FOCUS, seed + 2, TINT, alpha * .35f);
            card(p, v, Vec3.ZERO, right, up, sac, sac, FOCUS, seed + 3, TINT, alpha * .85f);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + seed;
                Vec3 tip = right.scale(Math.cos(a) * sac).add(up.scale(Math.sin(a) * sac));
                ribbonPath(p, v, List.of(Vec3.ZERO, tip.scale(.55).add(right.scale(.05 * Math.sin(a * 3))), tip), camera,
                        .05 * swell, .015, RIBBON, seed + 4 + i, CLOT, alpha);
            }
            return;
        }

        // 2. Tear: a hot flash inside a bursting sheath.
        if (age < TEAR_END + 2) {
            float flash = 1 - (age - SWELL_END) / (TEAR_END + 2f - SWELL_END);
            double burst = r * (.2 + .25 * (1 - flash));
            card(p, v, Vec3.ZERO, right, up, burst, burst, FOCUS, seed + 10, TINT, alpha * .5f * flash);
            card(p, v, Vec3.ZERO, right, up, .25 + .3 * flash, .25 + .3 * flash, FOCUS, seed + 11, CORE, alpha * flash);
        }
        float body = alpha * (1 - settle);

        // 3. Ruptured vessels whip outward, then hang limp.
        for (int i = 0; i < 6; i++) {
            Vec3 tip = spray(i, seed + 7, age, r * .8);
            double limp = smooth((age - TEAR_END) / 8.0);
            Vec3 mid = tip.scale(.5).add(0, .2 * (1 - limp) - .25 * limp, 0);
            ribbonPath(p, v, List.of(Vec3.ZERO, mid, tip), camera, .09, .012, RIBBON, seed + 20 + i, i % 2 == 0 ? CLOT : TINT, body);
        }

        // 4. Shreds of the sac and a sphere of spray, each droplet trailing a streak.
        for (int i = 0; i < 18; i++) {
            Vec3 at = spray(i, seed, age, r);
            Vec3 before = spray(i, seed, age - 1.2, r);
            Vec3 axis = at.subtract(before);
            axis = axis.lengthSqr() < 1e-8 ? UP : axis.normalize();
            boolean shred = i < 6;
            double size = (shred ? .16 : .07 + .04 * ThermalMotion.random(seed, i + 9)) * (1 - settle * .6);
            int color = shred || i % 4 == 0 ? CLOT : TINT;
            if (age < 14)
                ribbonPath(p, v, List.of(spray(i, seed, age - 2.5, r), before, at), camera, size * .1, size * .6, RIBBON,
                        seed + 30 + i, color, body * .8f);
            card(p, v, at, facingSide(axis, camera.subtract(at)), axis, size, size * (shred ? 1.1 : 1.7), FOCUS, seed + 60 + i, color, body);
        }

        // 5. Clotted haze lingers where it burst.
        float haze = alpha * (float) smooth((age - TEAR_END) / 4.0) * (1 - (float) smooth((age - 18) / 10.0));
        for (int i = 0; i < 5; i++) {
            double a = i * 1.7 + seed;
            Vec3 at = new Vec3(Math.cos(a) * r * .3, -.2 + age * .015, Math.sin(a) * r * .3);
            card(p, rot, at, right, up, r * .3, r * .35, WISP, seed + 90 + i, TINT, haze * .55f);
        }
    }
}
