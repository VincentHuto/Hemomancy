package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Spores leave the posed sacs and censers; the offering remains rooted in the open palm. */
final class PutricielOfferingGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4;
    record Frame(int visual, float remaining, boolean absolved, float window, float nova, float release, int count,
                 boolean escalated, float age, int seed, Vec3 core, Vec3 palm, Vec3[] sacs, Vec3[] censers, Vec3 floor) {}
    private PutricielOfferingGeometry() {}
    static boolean handles(Form form) { return form == Form.PUTRICIEL_OFFERING; }

    static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch batch, Vec3 camera, Vec3 right, Vec3 up, float partial) {
        for (PutricielEntity boss : level.getEntitiesOfClass(PutricielEntity.class, new AABB(camera, camera).inflate(96))) {
            if (!boss.isAlive() || boss.isInvisible()) continue;
            float rise = boss.riseTime(partial);
            if (rise >= 0 && rise < SaintRise.TICKS) continue;
            var motion = PutricielAnimState.of(boss);
            motion.update(boss, partial);
            Vec3 at = boss.getPosition(partial);
            Vec3 floor = Vec3.atBottomCenterOf(boss.blockPosition()).subtract(at);
            p.pushPose();
            p.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
            body(p, batch.vertices(LuxUmbraBatch.Material.MORTEM), batch.vertices(LuxUmbraBatch.Material.FLAME),
                    camera.subtract(at), right, up, new Frame(boss.getVisualState(), motion.visual(), boss.isAbsolved(),
                            motion.window(), motion.nova(), motion.release(), boss.getAbsolutionCount(), boss.isEscalated(), AnimationTime.age(boss, partial),
                            boss.getId(), motion.core(), motion.palm(), motion.sacs(), motion.censers(), floor));
            p.popPose();
        }
    }

    static void body(PoseStack p, VertexConsumer rot, VertexConsumer fire, Vec3 camera, Vec3 right, Vec3 up, Frame f) {
        var action = PutricielVisualRules.pose(f.visual(), f.absolved(), f.count(), f.remaining(), f.window(), f.nova(), f.release());
        float heat = Math.max(action.gather(), action.release());
        for (int i = 0; i < f.sacs().length; i++) {
            Vec3 sac = f.sacs()[i];
            for (int n = 0; n < 3; n++) {
                double t = (f.age() * .012 + i * .27 + n / 3.0) % 1;
                double a = i * 2.4 + n * 1.7 + t * 1.4;
                Vec3 mote = sac.add(Math.cos(a) * t * .35, .1 + t * (.45 + heat * .5), Math.sin(a) * t * .35);
                float opacity = (float) Math.sin(t * Math.PI) * (.25F + heat * .45F);
                card(p, rot, mote, right, up, .045 + heat * .025, .09, SLASH, f.seed() + i + n, TINT, opacity);
                card(p, fire, mote, right, up, .018, .025, FOCUS, f.seed() + n, CORE, opacity * .65F);
            }
            if (heat > .01) {
                ribbonPath(p, rot, List.of(sac, sac.lerp(f.core(), .5).add(0, .2, 0), f.core()), camera,
                        .085, .02, RIBBON, f.seed() + i, TINT, heat * .65F);
            }
        }
        for (int i = 0; i < f.censers().length; i++) {
            Vec3 censer = f.censers()[i];
            double drift = Math.sin(f.age() * .08 + i) * .12;
            ribbonPath(p, rot, List.of(censer, censer.add(drift, -.35, .06), censer.add(-drift, -.8, .1)),
                    camera, .09, .015, WISP, f.seed() + i, TINT, .4F + heat * .25F);
            card(p, fire, censer, right, up, .04 + heat * .025, .075, FOCUS, f.seed() + i, CORE, .6F);
        }
        if (f.nova() > 0 && f.nova() <= PutricielVisualRules.NOVA_WARNING) {
            double extent = PutricielVisualRules.radius(f.escalated()) + .5;
            square(p, rot, f.floor(), extent, camera, .12, f.seed(), .6F);
            square(p, fire, f.floor(), extent, camera, .04, f.seed(), .4F + heat * .5F);
            for (int i = 0; i < 8; i++) {
                Vec3 root = perimeter(f.floor(), extent, i / 8.0);
                Vec3 knee = root.lerp(f.core(), .6).add(0, -.35, 0);
                ribbonPath(p, rot, List.of(root, knee, f.core()), camera, .13, .015, RIBBON, f.seed() + i, TINT, action.gather() * .5F);
            }
        }
        if (f.absolved()) {
            Vec3 palm = f.palm().add(0, .12, 0);
            for (int i = 0; i < PutricielVisualRules.ABSOLUTIONS_NEEDED; i++) {
                double a = i * Math.PI * 2 / PutricielVisualRules.ABSOLUTIONS_NEEDED;
                Vec3 nodule = palm.add(Math.cos(a) * .28, .17 + Math.sin(f.age() * .08 + i) * .025, Math.sin(a) * .28);
                ribbonPath(p, rot, List.of(palm, nodule), camera, .035, .012, RIBBON, f.seed() + i, TINT, .8F);
                card(p, rot, nodule, right, up, .09, .15, SLASH, f.seed() + i, TINT, .9F);
                if (i < f.count()) card(p, fire, nodule, right, up, .05, .07, FOCUS, f.seed() + i, CORE, .95F);
            }
            ribbonPath(p, rot, List.of(f.core(), f.core().lerp(palm, .5).add(0, -.2, 0), palm), camera,
                    .09, .025, RIBBON, f.seed(), TINT, .75F);
            card(p, fire, palm, right, up, .08, .12, FOCUS, f.seed(), CORE, .5F + action.flinch() * .5F);
        }
    }

    /** Perimeter of the actual block-centered damage AABB, not a circular approximation. */
    static Vec3 perimeter(Vec3 center, double extent, double t) {
        double along = (t - Math.floor(t)) * 4;
        int edge = (int) along;
        double v = (along - edge) * 2 - 1;
        return center.add(switch (edge) {
            case 0 -> new Vec3(v * extent, .08, -extent);
            case 1 -> new Vec3(extent, .08, v * extent);
            case 2 -> new Vec3(-v * extent, .08, extent);
            default -> new Vec3(-extent, .08, -v * extent);
        });
    }
    private static void square(PoseStack p, VertexConsumer v, Vec3 center, double extent, Vec3 camera,
                               double width, int seed, float opacity) {
        for (int i = 0; i < 32; i++)
            ribbonPath(p, v, List.of(perimeter(center, extent, i / 32.0), perimeter(center, extent, (i + .72) / 32.0)),
                    camera, width, RIBBON, seed + i, TINT, opacity);
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer rot, VertexConsumer fire,
                     Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha) {
        float fade = alpha * PutricielVisualRules.fade(age, packet.ticks());
        if (fade <= 0) return;
        int kind = packet.count(), seed = Math.floorMod(packet.from().hashCode(), 113);
        double r = packet.radius();
        if (kind == PutricielVisualRules.NOVA) {
            double extent = r + .5;
            square(p, rot, Vec3.ZERO, extent, camera, .2 * fade, seed, fade * .65F);
            square(p, fire, Vec3.ZERO, extent, camera, .07 * fade, seed, fade);
            for (int i = 0; i < 16; i++) {
                Vec3 tip = perimeter(Vec3.ZERO, extent, i / 16.0);
                double spread = HemoEasing.easeOutCubic(age / 10.0);
                Vec3 cap = tip.scale(.2 + spread * .8).add(0, .15 + Math.sin(spread * Math.PI) * .35, 0);
                List<Vec3> branch = List.of(Vec3.ZERO.add(0, .1, 0), cap.scale(.6).add(0, .15, 0), cap);
                ribbonPath(p, rot, branch, camera, .18, .025, RIBBON, seed + i, TINT, fade * .65F);
                ribbonPath(p, fire, branch, camera, .045, .008, RIBBON, seed + i, TINT, fade * .8F);
                card(p, rot, cap, right, up, .14 * fade, .24 * fade, SLASH, seed + i, TINT, fade * .8F);
                card(p, fire, cap, right, up, .035, .05, FOCUS, seed + i, CORE, fade);
            }
            return;
        }
        if (kind == PutricielVisualRules.MELEE) {
            for (double offset : slashOffsets()) {
                List<Vec3> scratch = List.of(new Vec3(offset - .2, .35, 0), new Vec3(offset, 0, 0), new Vec3(offset + .15, -.3, 0));
                ribbonPath(p, rot, scratch, camera, .12, .02, RIBBON, seed, TINT, fade);
                ribbonPath(p, fire, scratch, camera, .035, .008, RIBBON, seed, TINT, fade * .7F);
            }
            return;
        }
        boolean rest = kind == PutricielVisualRules.REST, closing = kind == PutricielVisualRules.CLOSE;
        double travel = HemoEasing.easeOutCubic(age / (rest ? 22.0 : 12.0));
        for (int i = 0; i < 10; i++) {
            double a = i * 2.39996 + seed;
            double radius = r * (closing ? 1 - travel * .9 : .2 + travel * .8);
            Vec3 head = new Vec3(Math.cos(a) * radius, Math.sin(a * 1.3) * radius * .4 + (rest ? travel * 1.2 : 0), Math.sin(a) * radius);
            Vec3 elbow = head.scale(.5).add(0, -.15, 0);
            ribbonPath(p, rot, List.of(Vec3.ZERO, elbow, head), camera, .14 * fade, .015, RIBBON, seed + i, TINT, fade * .65F);
            card(p, rot, head, right, up, .12 * fade, .22 * fade, SLASH, seed + i, TINT, fade * .85F);
            card(p, fire, head, right, up, .035 * fade, .05 * fade, FOCUS, seed + i, CORE, fade * .75F);
        }
        if (!closing) card(p, fire, Vec3.ZERO, right, up, .12 * fade, .18 * fade, FOCUS, seed, CORE, fade);
    }
}
