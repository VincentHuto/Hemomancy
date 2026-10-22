package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.ferric.LodestoneCollapseRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/**
 * Lodestone Collapse: a magnetised point drags iron out of every body in reach. The field's lines tighten, forged
 * shards spiral in from the rim trailing blood, slam together into a grinding knot with a pale flash, and the knot
 * sinks and comes apart. Iron is drawn in the solid pass, field and blood in the Ductilis flow pass, so both batches
 * call this once per frame; each discards the other's geometry. Points are offsets from the collapse point.
 */
final class LodestoneCollapseGeometry {
    private static final int FIELD = 0xBE8C73, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final int RIBBON = 0, FOCUS = 4;

    private LodestoneCollapseGeometry() {}

    static boolean handles(Form form) {
        return form == Form.LODESTONE_COLLAPSE;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer iron, VertexConsumer flow, Vec3 camera,
            Vec3 right, Vec3 up, double time, float age, float alpha) {
        if (alpha <= 0) return;
        double radius = Math.max(1.5, packet.radius());
        int seed = Math.floorMod(Math.max(1, packet.count()) * 31 + packet.from().hashCode(), 113);
        double sink = LodestoneCollapseRules.sink(age), crush = LodestoneCollapseRules.crush(age);
        float body = alpha * (float) (1 - sink);
        Vec3 knot = new Vec3(0, 0.55 - sink * 0.5, 0);

        // 1. Field: the magnetic lines run inward and tighten as the shards are drawn.
        if (age < LodestoneCollapseRules.CRUSH_END + 2)
            FerricDuctilisGeometry.field(p, flow, Vec3.ZERO, radius * LodestoneCollapseRules.fieldRadius(age), time, seed,
                    camera, alpha * (float) (1 - HemoEasing.smoothstep((double) ((age - LodestoneCollapseRules.DRAW_END) / 4))));

        // 2. Shards: forged iron spirals in from the rim, each dragging a thread of blood out behind it.
        for (int i = 0; i < LodestoneCollapseRules.SHARDS; i++) {
            double t = LodestoneCollapseRules.draw(age, i);
            if (age < LodestoneCollapseRules.FIELD_END - 1) continue;
            Vec3 at = shardPoint(i, t, seed, radius, knot);
            double size = (0.16 + 0.05 * (i % 3)) * (1 - sink * 0.7) * (0.6 + 0.4 * Math.min(1, (age - 3) / 3.0));
            double grind = crush * 0.08 * Math.sin(time * 1.7 + i * 1.9);
            double[] polar = LodestoneCollapseRules.shard(i, t, seed);
            p.pushPose();
            p.translate(at.x + grind, at.y, at.z - grind);
            // Shards travel point-first toward the knot and tumble as they come.
            p.mulPose(Axis.YP.rotation((float) (-polar[0] + Math.PI / 2)));
            p.mulPose(Axis.XP.rotation((float) (t * 1.4 + i * 0.3)));
            FerricDuctilisGeometry.plate(p, iron, Vec3.ZERO, size * 0.7, size * 1.6, 0.045, seed + i, body);
            p.popPose();
            if (t > 0.02 && t < 1) {
                Vec3 behind = shardPoint(i, Math.max(0, t - 0.22), seed, radius, knot);
                FerricDuctilisGeometry.ribbon(p, flow, behind, at, 0.035, 0.05, time, seed + 20 + i, camera,
                        FerricDuctilisGeometry.BLOOD, body * 0.8f);
            }
        }

        // 3. Crush: a pale flash in the knot and blood squeezed out between the plates.
        if (crush > 0) {
            FerricDuctilisGeometry.card(p, flow, knot, right, up, 0.5 * crush, 0.5 * crush, FOCUS, seed + 40, CORE, alpha * (float) Math.min(1, crush));
            for (int i = 0; i < 7; i++) {
                double a = i * 2.39996 + seed;
                Vec3 out = new Vec3(Math.cos(a), 0.35 + 0.25 * Math.sin(i * 1.3), Math.sin(a)).normalize();
                FerricDuctilisGeometry.ribbon(p, flow, knot, knot.add(out.scale(radius * 0.35 * crush)), 0.03, 0.04,
                        time, seed + 50 + i, camera, CLOT, alpha * (float) Math.min(1, crush));
            }
        }

        // 4. Settle: the knot sinks and rusts away, leaving a fading ring of field lines on the ground.
        if (sink > 0 && sink < 1) {
            FerricDuctilisGeometry.card(p, flow, knot, right, up, 0.3 * (1 - sink), 0.3 * (1 - sink), FOCUS, seed + 60,
                    CLOT, body * 0.6f);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + seed;
                Vec3 rim = new Vec3(Math.cos(a) * radius * 0.25 * (1 + sink), 0.05, Math.sin(a) * radius * 0.25 * (1 + sink));
                FerricDuctilisGeometry.ribbon(p, flow, rim, rim.lerp(knot, 0.5), 0.02, 0.03, time, seed + 70 + i, camera,
                        FIELD, body * 0.4f);
            }
        }
    }

    private static Vec3 shardPoint(int index, double t, int seed, double radius, Vec3 knot) {
        double[] polar = LodestoneCollapseRules.shard(index, t, seed);
        double distance = polar[1] * radius;
        Vec3 out = new Vec3(Math.cos(polar[0]) * distance, polar[2] * 1.4 + 0.2, Math.sin(polar[0]) * distance);
        // Close in, everything converges on the knot rather than the floor beneath it.
        return out.lerp(knot, HemoEasing.smoothstep((t - 0.75) / 0.25));
    }
}
