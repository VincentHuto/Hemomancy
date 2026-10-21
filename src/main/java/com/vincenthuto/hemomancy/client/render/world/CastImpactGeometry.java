package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules;
import com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A cast's impact at the point of contact, built like the Riposte's success flash: a hot flash, the delivery's shape
 * thrown in the school's own material, the school's flourish, and a thread of the secondary school. Each impact plays
 * a 10-tick flash, burst and fray from {@link CastImpactRules}. The pose is already at the contact point; {@code to}
 * gives the direction the blow travelled; sprays leave the wound outward, back toward where it came from.
 */
final class CastImpactGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020, BLOOD = 0x9C263B;
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final double GOLDEN = 2.39996;

    private CastImpactGeometry() {}

    static boolean handles(Form form) { return form == Form.CAST_IMPACT; }

    /** Solid iron (Ferric splinters) draws in the solid pass; everything else in the flow pass. */
    static void draw(ManipulationVisualPacket packet, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron,
            boolean solids, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha) {
        if (alpha <= 0) return;
        var cue = CastImpactRules.decode(packet.count());
        Vec3 dir = packet.to().subtract(packet.from());
        dir = dir.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : dir.normalize();
        Vec3[] basis = BloodShotGeometry.basis(dir);
        double s = Math.max(.6, packet.radius()) * CastImpactRules.scale(cue.weight()) * .9;
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        // Mortem is sluggish: its whole impact plays at about two thirds speed.
        float t = cue.primary() == EnumBloodTendency.MORTEM ? age * .65f : age;
        float presence = alpha * CastImpactRules.presence(t);
        if (presence <= .003f) return;
        var at = new Impact(p, camera, right, up, dir, dir.scale(-1), basis, s, seed, time, t, CastImpactRules.burst(t), presence,
                cue.primary() == EnumBloodTendency.TENEBRIS);
        if (solids) {
            if (cue.primary() == EnumBloodTendency.FERRIC) splinters(at, iron);
            return;
        }
        VertexConsumer v = flows.vertices(material(cue.primary()));
        int tint = cue.primary() == EnumBloodTendency.FERRIC ? BLOOD : TINT;
        // Cruor glass has no soft card; Congeatio's flash is the pale cold light of the ice instead.
        flash(at, cue.primary() == EnumBloodTendency.CONGEATIO ? flows.vertices(LuxUmbraBatch.Material.LUX) : v,
                cue.primary() == EnumBloodTendency.LUX ? 1.5 : 1);
        switch (cue.delivery()) {
            case MELEE -> melee(at, v, tint);
            case PROJECTILE -> projectile(at, v, tint);
            case AREA -> area(at, v, tint);
            case TETHER -> tether(at, v, tint);
        }
        flourish(at, cue.primary(), flows);
        if (cue.secondary() != null && cue.secondary() != cue.primary()) {
            VertexConsumer second = flows.vertices(material(cue.secondary()));
            for (int i = 0; i < 2; i++) ray(at, second, sphere(i * 3 + 1, seed, dir.scale(-1), .9), .1, .75, .022, SLASH, TINT, .7f, i);
        }
    }

    /** One impact's frame: everything the layers share. {@code grow} is the burst, {@code implode} turns Tenebris inward. */
    private record Impact(PoseStack p, Vec3 camera, Vec3 right, Vec3 up, Vec3 dir, Vec3 out, Vec3[] basis, double s, int seed,
                          double time, float age, float grow, float alpha, boolean implode) {}

    static LuxUmbraBatch.Material material(EnumBloodTendency school) {
        return switch (school) {
            case ANIMUS -> LuxUmbraBatch.Material.ANIMUS;
            case FLAMMEUS -> LuxUmbraBatch.Material.FLAME;
            case CONGEATIO -> LuxUmbraBatch.Material.CRUOR;
            case DUCTILIS, FERRIC -> LuxUmbraBatch.Material.DUCTILIS;
            case LUX -> LuxUmbraBatch.Material.LUX;
            case MORTEM -> LuxUmbraBatch.Material.MORTEM;
            case TENEBRIS -> LuxUmbraBatch.Material.UMBRA;
        };
    }

    //  Shared layers

    /** The contact flash: a broad card that shrinks fast, with a hot pale core. */
    private static void flash(Impact at, VertexConsumer v, double boost) {
        float flash = CastImpactRules.flash(at.age);
        double size = at.s * (.25 + .55 * flash) * boost;
        card(at.p, v, Vec3.ZERO, at.right, at.up, size, size, FOCUS, at.seed, TINT, at.alpha * (.4f + .6f * flash));
        if (flash > .01f) card(at.p, v, Vec3.ZERO, at.right, at.up, size * .45, size * .45, FOCUS, at.seed + 1, CORE, at.alpha * flash);
    }

    /**
     * A ray along {@code unit} from {@code inner} to {@code outer} (in impact sizes) as it bursts. Thrown rays grow out
     * from the wound; Tenebris rays start at the far end and are drawn in, then fray where they stopped.
     */
    private static void ray(Impact at, VertexConsumer v, Vec3 unit, double inner, double outer, double width, int shape,
            int color, float alpha, int i) {
        double a = inner * at.s, b = outer * at.s;
        Vec3 start, end;
        if (at.implode) { start = unit.scale(b); end = unit.scale(b - (b - a) * at.grow); }
        else { start = unit.scale(a); end = unit.scale(a + (b - a) * at.grow); }
        // Each ray flickers on its own seed so a burst never pulses as one.
        float flicker = (float) (.8 + .2 * Math.sin(at.time * .9 + i * 1.7 + at.seed));
        stream(at.p, v, start, end, at.camera, width * at.s, .04 * at.s, at.time * .1 + i, shape, at.seed + i, color,
                at.alpha * alpha * flicker);
    }

    /** A unit direction spread around {@code axis}: {@code spread} 0 is the axis itself, 1 roughly a hemisphere. */
    private static Vec3 cone(Vec3 axis, Vec3[] basis, int i, int seed, double spread) {
        double a = i * GOLDEN + seed, r = spread * Math.sqrt((i + .5) / 8.0);
        return axis.add(basis[0].scale(Math.cos(a) * r)).add(basis[1].scale(Math.sin(a) * r)).normalize();
    }

    /** Evenly spread directions over a whole sphere, leaning toward {@code bias}. */
    private static Vec3 sphere(int i, int seed, Vec3 bias, double lean) {
        double y = 1 - 2 * ((i * 0.618034 + seed * .01) % 1.0), r = Math.sqrt(Math.max(0, 1 - y * y)), a = i * GOLDEN + seed;
        return new Vec3(Math.cos(a) * r, y, Math.sin(a) * r).add(bias.scale(lean)).normalize();
    }

    //  How the effect landed

    /** A blow: three parallel streaks raked diagonally across the struck face and blood sprayed on along the cut. */
    private static void melee(Impact at, VertexConsumer v, int tint) {
        double cross = (.45 + .55 * at.grow) * .75 * at.s;
        // The struck face is spanned by the two basis axes; the rake runs corner to corner so it reads from any side.
        Vec3 cut = at.basis[0].scale(.55).add(at.basis[1].scale(.85)).normalize();
        Vec3 apart = at.basis[0].scale(.85).subtract(at.basis[1].scale(.55)).normalize();
        for (double offset : slashOffsets()) {
            Vec3 shift = apart.scale(offset * at.s * .6);
            stream(at.p, v, cut.scale(-cross).add(shift), cut.scale(cross).add(shift), at.camera,
                    .05 * at.s, .05 * at.s, at.time * .1, SLASH, at.seed + 9, tint, at.alpha);
        }
        for (int i = 0; i < 5; i++) {
            Vec3 along = cut.scale((i & 1) == 0 ? 1 : -1).add(at.out.scale(.45));
            ray(at, v, cone(along.normalize(), at.basis, i, at.seed, .5), .1, 1.0, .04, SLASH, tint, 1, i);
        }
    }

    /** A shot: a ring blown open where it entered, a cone sprayed out ahead and a short splash back toward the shooter. */
    private static void projectile(Impact at, VertexConsumer v, int tint) {
        double ring = (.12 + .45 * at.grow) * at.s;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4 + at.seed;
            Vec3 point = at.basis[0].scale(Math.cos(a) * ring).add(at.basis[1].scale(Math.sin(a) * ring));
            card(at.p, v, point, at.right, at.up, .06 * at.s, .06 * at.s, FOCUS, at.seed + i, tint, at.alpha * (1 - .5f * at.grow));
        }
        for (int i = 0; i < 6; i++) ray(at, v, cone(at.out, at.basis, i, at.seed, 1.1), .05, 1.1, .035, SLASH, tint, 1, i);
        ray(at, v, at.out, .05, .6, .05, SLASH, tint, .8f, 7);
    }

    /** An area: a ripple runs round the target at the hit's height and short spurts climb from it. */
    private static void area(Impact at, VertexConsumer v, int tint) {
        double radius = (at.implode ? 1.3 - .7 * at.grow : .6 + .7 * at.grow) * at.s;
        List<Vec3> loop = new ArrayList<>(17);
        for (int i = 0; i <= 16; i++) {
            double a = i * Math.PI / 8;
            loop.add(new Vec3(Math.cos(a) * radius, Math.sin(a * 3 + at.time * .3) * .03 * at.s, Math.sin(a) * radius));
        }
        ribbonPath(at.p, v, loop, at.camera, .05 * at.s, RIBBON, at.seed, tint, at.alpha * .9f);
        for (int i = 0; i < 5; i++) {
            double a = i * GOLDEN + at.seed;
            Vec3 unit = new Vec3(Math.cos(a) * .45, 1, Math.sin(a) * .45).normalize();
            ray(at, v, unit, .15, .8, .035, SLASH, tint, .9f, i);
        }
    }

    /** A tether: a strand snaps taut in from the caster's side, a knot cinches at the contact and barbs bite in. */
    private static void tether(Impact at, VertexConsumer v, int tint) {
        double length = 1.6 * at.s, sag = .3 * at.s * (1 - at.grow);
        List<Vec3> strand = new ArrayList<>(13);
        for (int i = 0; i <= 12; i++) {
            double u = i / 12.0;
            strand.add(at.dir.scale(-length * (1 - u)).add(0, -Math.sin(u * Math.PI) * sag, 0));
        }
        ribbonPath(at.p, v, strand, at.camera, .02 * at.s, .045 * at.s, RIBBON, at.seed, tint, at.alpha);
        double knot = at.s * (.32 - .16 * at.grow);
        card(at.p, v, Vec3.ZERO, at.right, at.up, knot, knot, FOCUS, at.seed + 3, CLOT, at.alpha);
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + at.seed;
            Vec3 unit = at.basis[0].scale(Math.cos(a)).add(at.basis[1].scale(Math.sin(a))).add(at.out.scale(.4)).normalize();
            ray(at, v, unit, .05, .45, .03, SLASH, tint, 1, i);
        }
    }

    //  The school's flourish

    private static void flourish(Impact at, EnumBloodTendency school, LuxUmbraBatch flows) {
        switch (school) {
            case ANIMUS -> clots(at, flows.vertices(LuxUmbraBatch.Material.ANIMUS));
            case FLAMMEUS -> flames(at, flows.vertices(LuxUmbraBatch.Material.FLAME));
            case CONGEATIO -> shards(at, flows.vertices(LuxUmbraBatch.Material.GLASS));
            case DUCTILIS -> bolts(at, flows.vertices(LuxUmbraBatch.Material.DUCTILIS));
            case LUX -> starburst(at, flows.vertices(LuxUmbraBatch.Material.LUX));
            case MORTEM -> blobs(at, flows.vertices(LuxUmbraBatch.Material.MORTEM));
            case FERRIC -> sparks(at, flows.vertices(LuxUmbraBatch.Material.MOLTEN));
            case TENEBRIS -> puff(at, flows.vertices(LuxUmbraBatch.Material.UMBRA));
        }
    }

    /** Animus: clots flung on falling arcs, each a dark clot round a pale core. */
    private static void clots(Impact at, VertexConsumer v) {
        double fall = .45 * Math.pow(at.age / CastImpactRules.LIFE, 2) * at.s;
        for (int i = 0; i < 5; i++) {
            Vec3 point = cone(at.out, at.basis, i, at.seed + 5, 1.1).scale((.2 + .95 * at.grow) * at.s).add(0, -fall, 0);
            card(at.p, v, point, at.right, at.up, .085 * at.s, .085 * at.s, FOCUS, at.seed + 20 + i, CLOT, at.alpha);
            card(at.p, v, point, at.right, at.up, .035 * at.s, .035 * at.s, FOCUS, at.seed + 30 + i, CORE, at.alpha * .8f);
        }
    }

    /** Flammeus: flame tongues lick up out of the wound, then burn down to smoke in the fray. */
    private static void flames(Impact at, VertexConsumer v) {
        boolean fray = at.age > CastImpactRules.BURST_END;
        for (int i = 0; i < 3; i++) {
            Vec3 root = at.basis[0].scale((i - 1) * .18 * at.s);
            if (!fray) {
                double height = (.3 + .5 * at.grow) * at.s;
                ThermalGeometry.flameRibbon(at.p, v, ThermalGeometry.tongue(root, at.out.scale(.25), height, at.time, at.seed + i),
                        at.camera, .07 * at.s, at.seed + i, at.alpha);
            } else ThermalGeometry.smoke(at.p, v, root.add(0, (.4 + .1 * (at.age - CastImpactRules.BURST_END)) * at.s, 0),
                    at.right, at.up, .18 * at.s, .22 * at.s, at.seed + i, at.alpha * .6f);
        }
    }

    /** Congeatio: clear ice shards burst outward, turning as they fly. */
    private static void shards(Impact at, VertexConsumer v) {
        for (int i = 0; i < 4; i++) {
            Vec3 point = cone(at.out, at.basis, i, at.seed + 3, 1.0).scale((.15 + .7 * at.grow) * at.s);
            at.p.pushPose();
            at.p.translate(point.x, point.y, point.z);
            at.p.mulPose(Axis.YP.rotation((float) (at.seed + i * 1.3 + at.age * .2)));
            at.p.mulPose(Axis.ZP.rotation((float) (i * .9 + at.age * .15)));
            ThermalGeometry.shard(at.p, v, .13 * at.s, at.seed + i, at.alpha);
            at.p.popPose();
        }
    }

    /** Ductilis: jagged nerve bolts that re-strike every half tick. */
    private static void bolts(Impact at, VertexConsumer v) {
        int strike = (int) (at.time * 2);
        for (int i = 0; i < 3; i++) {
            Vec3 end = sphere(i * 2 + 1, at.seed, at.out, 1.2).scale((.3 + .8 * at.grow) * at.s);
            List<Vec3> path = new ArrayList<>(7);
            var random = new java.util.Random(at.seed * 31L + i * 7L + strike);
            for (int k = 0; k <= 6; k++) {
                double u = k / 6.0, jag = k == 0 || k == 6 ? 0 : .12 * at.s;
                path.add(end.scale(u).add((random.nextDouble() - .5) * jag * 2, (random.nextDouble() - .5) * jag * 2,
                        (random.nextDouble() - .5) * jag * 2));
            }
            ribbonPath(at.p, v, path, at.camera, .022 * at.s, RIBBON, at.seed + i, TINT, at.alpha);
        }
    }

    /** Lux: a white-gold starburst of long thin beams all round the contact. */
    private static void starburst(Impact at, VertexConsumer v) {
        for (int i = 0; i < 8; i++)
            ray(at, v, sphere(i, at.seed, at.out, .5), .1, 1.6, .018, WHITE_BEAM, (i & 1) == 0 ? WHITE : GOLD, .9f, i + 20);
    }

    /** Mortem: heavy blobs ooze out and sink. */
    private static void blobs(Impact at, VertexConsumer v) {
        for (int i = 0; i < 3; i++) {
            Vec3 point = cone(at.out, at.basis, i, at.seed + 7, 1.2).scale(.55 * at.grow * at.s).add(0, -.35 * at.grow * at.s, 0);
            card(at.p, v, point, at.right, at.up, .16 * at.s, .14 * at.s, POOL, at.seed + 40 + i, TINT, at.alpha * .85f);
        }
    }

    /** Ferric: molten sparks thrown off the splinters. */
    private static void sparks(Impact at, VertexConsumer v) {
        for (int i = 0; i < 5; i++) ray(at, v, sphere(i, at.seed + 9, at.out, .8), .2, .9, .012, SLASH, TINT, 1, i + 30);
    }

    /** Ferric (solid pass): blood-iron splinters fly from the contact and tumble. */
    private static void splinters(Impact at, VertexConsumer iron) {
        for (int i = 0; i < 3; i++) {
            Vec3 point = cone(at.out, at.basis, i, at.seed + 11, .9).scale((.15 + .75 * at.grow) * at.s);
            at.p.pushPose();
            at.p.translate(point.x, point.y, point.z);
            at.p.mulPose(Axis.YP.rotation((float) (at.seed + i * 2.1)));
            at.p.mulPose(Axis.XP.rotation((float) (at.age * .35 + i)));
            FerricDuctilisGeometry.plate(at.p, iron, Vec3.ZERO, .07 * at.s, .2 * at.s, .03 * at.s, at.seed + i, at.alpha);
            at.p.popPose();
        }
    }

    /** Tenebris: after the rays are drawn in, a small dark puff breathes out of the point they met. */
    private static void puff(Impact at, VertexConsumer v) {
        if (at.age < CastImpactRules.BURST_END - 1) return;
        double grow = Math.min(1, (at.age - CastImpactRules.BURST_END + 1) / 3.0);
        double size = (.2 + .35 * grow) * at.s;
        card(at.p, v, Vec3.ZERO, at.right, at.up, size, size * .85, WISP, at.seed + 50, TINT, at.alpha * .7f);
    }
}
