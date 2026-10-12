package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ferric.LodestoneFieldRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

/**
 * Living Axe Lodestone field. The critical bite drives a forged lodestone up out of the ground and cracks the floor
 * out to the rim. A ring of iron teeth marks the edge, and arched flux lines run from the rim into the lodestone,
 * carrying filings that stand on end and creep inward. Every gameplay pulse sends a contraction ripple from rim to
 * core: the teeth snap inward, the filings lurch, and the lodestone flares and spits blood. At the end, the iron
 * rusts and sinks. Iron is drawn in the solid pass, field and blood in the Ductilis flow pass; each batch discards
 * the other's geometry. Points are offsets from the field's centre on the ground.
 */
final class LodestoneFieldGeometry {
    private static final int FIELD = 0xBE8C73, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final int FOCUS = 4;

    private LodestoneFieldGeometry() {}

    static boolean handles(Form form) {
        return form == Form.LODESTONE_FIELD;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer iron, VertexConsumer flow, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha) {
        if (alpha <= 0 || age >= LodestoneFieldRules.VISUAL_TICKS) return;
        double radius = Math.max(1.0, packet.radius());
        int seed = Math.floorMod(Math.max(1, packet.count()) * 31 + packet.from().hashCode(), 113);
        double rise = LodestoneFieldRules.rise(age), pulse = LodestoneFieldRules.pulse(age);
        double gone = LodestoneFieldRules.dissolve(age);
        float body = alpha * (float) (1 - gone);
        double judder = pulse * 0.05 * Math.sin(time * 2.3);
        Vec3 crown = new Vec3(judder, 1.05 * rise - gone * 0.4, -judder);

        // 1. Bite: the floor cracks out to the rim behind a pale flash.
        double shock = LodestoneFieldRules.shock(age), crack = LodestoneFieldRules.shockFade(age);
        if (crack > 0) {
            for (int i = 0; i < 9; i++) {
                double a = i * 2.39996 + seed;
                Vec3 previous = new Vec3(0, 0.04, 0);
                for (int s = 1; s <= 3; s++) {
                    double reach = shock * radius * s / 3.0;
                    double jag = (s == 3 ? 0 : 0.35) * Math.sin(i * 3.1 + s * 1.7 + seed);
                    Vec3 next = new Vec3(Math.cos(a + jag * 0.3) * reach, 0.04, Math.sin(a + jag * 0.3) * reach);
                    FerricDuctilisGeometry.ribbon(p, flow, previous, next, 0.06 - s * 0.012, 0.02, time, seed + i * 3 + s,
                            camera, CLOT, alpha * (float) crack);
                    previous = next;
                }
            }
            double flash = Math.max(0, 1 - age / (double) LodestoneFieldRules.SHOCK_END);
            FerricDuctilisGeometry.card(p, flow, new Vec3(0, 0.45, 0), right, up, 0.9 * flash, 0.9 * flash, FOCUS,
                    seed + 5, CORE, alpha * (float) flash);
        }

        // 2. Lodestone: three forged plates standing in a cluster round a spike, slowly turning in the field.
        if (rise > 0.01) {
            double turn = time * 0.012 + seed;
            for (int i = 0; i < 3; i++) {
                double a = turn + i * Math.PI * 2 / 3;
                double h = (0.75 + 0.15 * i) * rise;
                p.pushPose();
                p.translate(Math.cos(a) * 0.16 + judder, -gone * 0.4, Math.sin(a) * 0.16 - judder);
                p.mulPose(Axis.YP.rotation((float) (-a + Math.PI / 2)));
                p.mulPose(Axis.ZP.rotation((float) (0.22 * (i % 2 == 0 ? 1 : -1))));
                FerricDuctilisGeometry.plate(p, iron, new Vec3(0, h * 0.5, 0), 0.26, h, 0.07, seed + 10 + i, body);
                p.popPose();
            }
            p.pushPose();
            p.translate(judder, -gone * 0.4, -judder);
            p.mulPose(Axis.YP.rotation((float) turn));
            FerricDuctilisGeometry.spike(p, iron, 1.15 * rise, 0.11, seed + 14, body);
            p.popPose();
            // The core glows hot on each pulse.
            FerricDuctilisGeometry.card(p, flow, crown, right, up, 0.22 + 0.35 * pulse, 0.22 + 0.35 * pulse, FOCUS,
                    seed + 15, pulse > 0.05 ? CORE : CLOT, body * (float) (0.35 + 0.65 * pulse));
        }

        // 3. Rim: a ring of iron teeth marks the reach; they lean in and snap toward the core on each pulse.
        double lean = LodestoneFieldRules.toothLean(age);
        for (int i = 0; i < LodestoneFieldRules.TEETH; i++) {
            double a = i * Math.PI * 2 / LodestoneFieldRules.TEETH + seed * 0.1;
            double grow = LodestoneFieldRules.smooth((age - 2 - (i % 3)) / 4.0);
            if (grow <= 0) continue;
            p.pushPose();
            p.translate(Math.cos(a) * radius, -gone * 0.3, Math.sin(a) * radius);
            p.mulPose(Axis.YP.rotation((float) (-a - Math.PI / 2)));
            p.mulPose(Axis.XP.rotation((float) -lean));
            FerricDuctilisGeometry.spike(p, iron, (0.32 + 0.08 * (i % 2)) * grow, 0.07, seed + 30 + i, body);
            p.popPose();
        }

        // 4. Flux: arched field lines from the rim up into the lodestone, brightening on each pulse.
        if (age > LodestoneFieldRules.RISE_END - 1) {
            float flux = body * (float) (0.4 + 0.5 * pulse) * (float) LodestoneFieldRules.smooth((age - 2) / 5.0);
            for (int i = 0; i < LodestoneFieldRules.FLUX_LINES; i++) {
                double a = i * Math.PI * 2 / LodestoneFieldRules.FLUX_LINES + seed * 0.1 + Math.PI / LodestoneFieldRules.TEETH;
                Vec3 rim = new Vec3(Math.cos(a) * radius, 0.08, Math.sin(a) * radius);
                Vec3 bow = new Vec3(Math.cos(a) * radius * 0.5, 1.15 + 0.15 * Math.sin(time * 0.05 + i), Math.sin(a) * radius * 0.5);
                Vec3 previous = rim;
                for (int s = 1; s <= 4; s++) {
                    Vec3 next = bezier(rim, bow, crown, s / 4.0);
                    FerricDuctilisGeometry.ribbon(p, flow, previous, next, 0.028, 0.02, time, seed + 40 + i * 4 + s,
                            camera, FIELD, flux);
                    previous = next;
                }
            }
            FerricDuctilisGeometry.field(p, flow, Vec3.ZERO, radius, time, seed, camera, body * 0.8f);
        }

        // 5. Filings: small iron flakes stand on end along the flux, creep inward and lurch with each pulse.
        for (int i = 0; i < LodestoneFieldRules.FILINGS; i++) {
            double t = LodestoneFieldRules.filing(age, i);
            double fade = LodestoneFieldRules.smooth(t / 0.1) * LodestoneFieldRules.smooth((1 - t) / 0.12)
                    * LodestoneFieldRules.smooth((age - LodestoneFieldRules.SHOCK_END) / 4.0);
            if (fade <= 0.01) continue;
            double a = i * 2.39996 + seed + t * 0.9;
            double distance = radius * (0.95 - t * 0.8);
            double height = (0.05 + Math.sin(t * Math.PI) * 0.45) * (1 - gone) + 0.02;
            p.pushPose();
            p.translate(Math.cos(a) * distance, height, Math.sin(a) * distance);
            p.mulPose(Axis.YP.rotation((float) (-a + Math.PI / 2)));
            p.mulPose(Axis.XP.rotation((float) (0.9 - t * 1.6 + gone * 1.4)));
            FerricDuctilisGeometry.plate(p, iron, Vec3.ZERO, 0.05, 0.17, 0.025, seed + 60 + i, body * (float) fade);
            p.popPose();
        }

        // 6. Pulse: a contraction ripple sweeps from rim to core, and the lodestone spits blood.
        double ripple = LodestoneFieldRules.ripple(age);
        if (ripple > 0) {
            int segments = 18;
            double r = ripple * radius;
            for (int i = 0; i < segments; i++) {
                double a0 = i * Math.PI * 2 / segments, a1 = (i + 0.8) * Math.PI * 2 / segments;
                Vec3 s0 = new Vec3(Math.cos(a0) * r, 0.07, Math.sin(a0) * r), s1 = new Vec3(Math.cos(a1) * r, 0.07, Math.sin(a1) * r);
                FerricDuctilisGeometry.ribbon(p, flow, s0, s1, 0.07, 0.01, time, seed + 80 + i, camera, CORE,
                        body * (float) (0.3 + 0.7 * pulse));
            }
            for (int i = 0; i < 5; i++) {
                double a = i * 2.39996 + seed + Math.floor(age / LodestoneFieldRules.PULSE);
                Vec3 out = new Vec3(Math.cos(a) * 0.55, 0.2 - 0.5 * (1 - pulse), Math.sin(a) * 0.55);
                FerricDuctilisGeometry.ribbon(p, flow, crown, crown.add(out.scale(1.2 - pulse * 0.6)), 0.035, 0.05, time,
                        seed + 100 + i, camera, FerricDuctilisGeometry.BLOOD, body * (float) pulse);
            }
        }

        // 7. Dissolve: a dark rust stain spreads under the sinking iron.
        if (gone > 0) {
            FerricDuctilisGeometry.card(p, flow, new Vec3(0, 0.05, 0), right, up, 0.5 + gone * 0.6, 0.5 + gone * 0.6,
                    FOCUS, seed + 120, CLOT, alpha * (float) (Math.sin(gone * Math.PI) * 0.6));
        }
    }

    private static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, double t) {
        return a.scale((1 - t) * (1 - t)).add(b.scale(2 * (1 - t) * t)).add(c.scale(t * t));
    }
}
