package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.UmbralRendRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Umbral Rend's tear through an Obscured body. The ink already clinging to the victim is drawn tight, three parallel
 * rends rip across it on a seeded diagonal facing the caster, the wounds gape over a void with blood spitting out of
 * them, then the lips fray into rising smoke. Everything is ink on the Umbra material; points are offsets from the
 * victim's middle.
 */
final class UmbralRendGeometry {
    private static final int INK = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final int SAMPLES = 10;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private UmbralRendGeometry() {}

    static boolean handles(Form form) {
        return form == Form.UMBRAL_REND;
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha) {
        if (alpha <= 0) return;
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        double r = Math.max(0.5, packet.radius());
        Vec3 toward = packet.to().subtract(packet.from());
        toward = new Vec3(toward.x, 0, toward.z);
        toward = toward.lengthSqr() < 1e-6 ? new Vec3(0, 0, 1) : toward.normalize();
        Vec3 side = toward.cross(UP).normalize();
        double slant = UmbralRendRules.slant(packet.count());
        Vec3 along = side.scale(Math.cos(slant)).add(UP.scale(Math.sin(slant)));
        Vec3 across = side.scale(-Math.sin(slant)).add(UP.scale(Math.cos(slant)));
        double gather = UmbralRendRules.gather(age), gape = UmbralRendRules.gape(age), fray = UmbralRendRules.fray(age);
        float a = alpha * (float) (1 - fray * 0.9);
        double[] offsets = slashOffsets();

        // 1. Gather: the shadow already on the body is pulled taut toward it before the tear.
        float drawIn = alpha * (float) (1 - UmbralRendRules.smooth((age - UmbralRendRules.REND_END) / 2.0));
        if (drawIn > 0.002)
            for (int i = 0; i < 6; i++) {
                double angle = i * Math.PI / 3 + seed;
                Vec3 rim = side.scale(Math.cos(angle) * 1.4 * r).add(UP.scale(Math.sin(angle) * 1.4 * r)).add(toward.scale(-0.2 * r));
                Vec3 tip = rim.scale(1 - 0.75 * gather);
                Vec3 mid = rim.lerp(tip, 0.5).add(toward.scale(0.15 * r * Math.sin(time * 0.4 + i)));
                ribbonPath(p, v, List.of(rim, mid, tip), camera, 0.06 * r, 0.012 * r, RIBBON, seed + i, INK, drawIn * 0.8f);
                double wisp = 0.22 * r * (1 - gather * 0.6);
                card(p, v, rim, right, up, wisp, wisp * 1.2, WISP, seed + 10 + i, INK, drawIn * 0.45f);
            }

        // 2. The void behind the wounds, opening as they gape.
        if (gape > 0.02)
            card(p, v, toward.scale(-0.05 * r), right, up, 0.55 * r * gape, 0.6 * r * gape, POOL, seed + 20, INK, a * 0.85f);

        // 3. The rends: an ink lip, a violet cut and a thin line of blood, torn across in sequence.
        for (int claw = 0; claw < 3; claw++) {
            double tear = UmbralRendRules.tear(age, claw);
            if (tear <= 0.01) continue;
            Vec3 start = across.scale(offsets[claw] * r * 0.8).subtract(along.scale(0.75 * r));
            List<Vec3> path = new ArrayList<>(SAMPLES + 1);
            for (int i = 0; i <= SAMPLES; i++) {
                double t = tear * i / SAMPLES;
                // Rends bow toward the caster at their middle, as if dragged out of the body.
                double bow = Math.sin(t / Math.max(0.01, tear) * Math.PI) * 0.12 * r;
                path.add(start.add(along.scale(1.5 * r * t)).add(toward.scale(bow)).add(UP.scale(fray * 0.4 * r * t)));
            }
            double flicker = 0.85 + 0.2 * Math.sin(time * 0.9 + claw * 1.7 + seed);
            double lip = 0.075 * r * flicker * (1 + fray * 0.9);
            ribbonPath(p, v, path, camera, lip * 0.5, lip, RIBBON, seed + 30 + claw, INK, a);
            ribbonPath(p, v, path, camera, 0.035 * r * (1 + gape * 0.8), 0.02 * r, SLASH, seed + 33 + claw, INK, a);
            if (gape > 0.05) {
                int from = SAMPLES / 6, to = SAMPLES - SAMPLES / 6;
                ribbonPath(p, v, path.subList(from, to + 1), camera, 0.014 * r * (1 + gape), RIBBON, seed + 36 + claw, CLOT, a);
            }
            if (tear < 1) card(p, v, path.getLast(), right, up, 0.13 * r, 0.13 * r, FOCUS, seed + 39 + claw, CORE, a);
        }

        // 4. Blood spits out of the open wounds toward the caster.
        if (gape > 0.05 && fray < 0.8)
            for (int claw = 0; claw < 3; claw++)
                for (int i = 0; i < 2; i++) {
                    Vec3 wound = across.scale(offsets[claw] * r * 0.8).add(along.scale((i - 0.5) * 0.5 * r));
                    Vec3 spray = toward.add(UP.scale(0.3 - 0.5 * i)).add(along.scale(0.2 * (claw - 1))).normalize();
                    stream(p, v, wound, wound.add(spray.scale(0.5 * r * gape)), camera, 0.03 * r, 0.06, time * 0.1 + claw,
                            SLASH, seed + 50 + claw * 2 + i, CLOT, a * (float) (1 - fray));
                }

        // 5. Fray: the torn lips come apart as smoke drifting upward.
        if (fray > 0)
            for (int i = 0; i < 5; i++) {
                double rise = fray * 0.9 * r;
                Vec3 at = across.scale(offsets[i % 3] * r * 0.8).add(along.scale((i - 2) * 0.3 * r)).add(UP.scale(rise));
                double width = 0.18 * r * (1 + fray);
                card(p, v, at, right, up, width, width * 1.3, WISP, seed + 60 + i, INK, alpha * (float) ((1 - fray) * 0.5));
            }
    }
}
