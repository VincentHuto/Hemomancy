package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.BloodShotGeometry.*;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Crimson Coronation: a crown of blood rings the head, one spike for every sword it will hold. Each armed sword hangs
 * from its spike by twisted sinew, a beat of blood running down to the pommel, and the swords condense one after
 * another as the coronation forms. Points are offsets from the wearer's feet.
 */
final class CrimsonCoronationGeometry {
    static final int SLOTS = 8;
    static final double CROWN_HEIGHT = 2.25, CROWN_RADIUS = .38;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private CrimsonCoronationGeometry() {}

    static boolean handles(Form form) {
        return form == Form.CROWN || form == Form.CROWN_CHARGE;
    }

    /** How formed sword {@code slot} is at an overall {@code formation}; each condenses after the one before. */
    static double swordFormed(double formation, int slot, int swords) {
        if (swords <= 0 || slot >= swords) return 0;
        return smooth(Mth.clamp(formation, 0, 1) * (swords + 1.5) - slot);
    }

    /** Where spike {@code slot} rises from the crown, turning with the swords it holds. */
    static Vec3 spike(double time, int slot) {
        double a = time * .025 + slot * Math.PI / 4;
        return new Vec3(Math.cos(a) * CROWN_RADIUS, CROWN_HEIGHT, Math.sin(a) * CROWN_RADIUS);
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float formation, float alpha) {
        if (alpha <= 0) return;
        boolean charge = packet.form() == Form.CROWN_CHARGE;
        double progress = charge ? Mth.clamp(packet.radius(), .02, 1) : formation;
        int swords = charge ? SLOTS : Math.min(SLOTS, packet.count());
        int seed = Math.floorMod(packet.entityId(), 113);
        p.pushPose();
        if (charge) p.translate(0, -1.62, 0);
        Vec3 cam = charge ? camera.add(0, 1.62, 0) : camera;
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * .45 + seed)), 4);

        // 1. Crown: a ring of blood with a clotted core.
        double crown = smooth(progress * 2);
        List<Vec3> ring = new ArrayList<>(25);
        for (int i = 0; i <= 24; i++) {
            double a = i * Math.PI * 2 / 24 + time * .025;
            double r = CROWN_RADIUS * (1 + .04 * Math.sin(a * 8 + time * .1));
            ring.add(new Vec3(Math.cos(a) * r, CROWN_HEIGHT + Math.sin(a * 4 + time * .05) * .015, Math.sin(a) * r));
        }
        ribbonPath(p, v, ring, cam, .05 * crown, RIBBON, seed, TINT, alpha * (float) crown);
        ribbonPath(p, v, ring, cam, .016 * crown, RIBBON, seed + 1, CLOT, alpha * (float) crown);

        for (int slot = 0; slot < SLOTS; slot++) {
            double own = swordFormed(progress, slot, swords);
            Vec3 base = spike(time, slot);
            // 2. Spikes: armed slots stand tall and glint; empty ones are stubs.
            double tall = (slot < swords ? .1 + .18 * own : .06) * crown;
            Vec3 tip = base.add(base.x * .12, tall, base.z * .12);
            ribbonPath(p, v, List.of(base, tip), cam, .035 * crown, .004, RIBBON, seed + 10 + slot, slot < swords ? TINT : CLOT, alpha * (float) crown);
            if (own <= .01) continue;
            card(p, v, tip, right, up, .03, .03, FOCUS, seed + 20 + slot, CORE, alpha * (float) own * (.6f + .4f * pulse));

            // 3. Tether: twisted sinew from the spike to the sword's pommel, a bead of blood running down it.
            Vec3 at = ManipulationVisuals.swordOffset(time, slot);
            Vec3 pommel = at.add(0, -.94, 0);
            Vec3 lift = tip.lerp(pommel, .5).add(0, .35, 0);
            List<Vec3> center = new ArrayList<>(11);
            for (int i = 0; i <= 10; i++) {
                double t = i / 10.0 * own, u = 1 - t;
                center.add(tip.scale(u * u).add(lift.scale(2 * u * t)).add(pommel.scale(t * t)));
            }
            for (int strand = 0; strand < 2; strand++) {
                List<Vec3> twist = new ArrayList<>(center.size());
                for (int i = 0; i < center.size(); i++) {
                    Vec3 tangent = center.get(Math.min(i + 1, center.size() - 1)).subtract(center.get(Math.max(0, i - 1)));
                    Vec3[] b = basis(tangent);
                    double a = i * .9 + strand * Math.PI + time * .12;
                    twist.add(center.get(i).add(b[0].scale(Math.cos(a) * .03)).add(b[1].scale(Math.sin(a) * .03)));
                }
                ribbonPath(p, v, twist, cam, .03, .018, RIBBON, seed + 30 + slot * 2 + strand, strand == 0 ? CLOT : TINT, alpha * (float) own);
            }
            double run = (time * .04 + slot * .17) % 1;
            if (own >= 1) card(p, v, center.get((int) Math.round(run * 10)), right, up, .035, .05, FOCUS, seed + 50 + slot, TINT, alpha);

            // 4. Sword: leaning outward, condensing up from the pommel.
            p.pushPose();
            p.translate(at.x, at.y, at.z);
            double a = time * .025 + slot * Math.PI / 4;
            p.mulPose(new Quaternionf().rotationAxis(-.22f, (float) -Math.sin(a), 0, (float) Math.cos(a)));
            p.translate(0, -.94 * (1 - own), 0);
            p.scale(1, (float) Math.max(.02, own), 1);
            VisceralMesh.sword(p, new BloodSurfaceVertices(v), alpha, time + slot * 7);
            p.popPose();
        }
        p.popPose();
    }
}
