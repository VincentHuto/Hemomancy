package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.*;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Restrained Lux: branching nerves, white vessels and gold impulses in the shared school batch. */
final class SeraphaeRadianceGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    record Frame(SeraphaeState state, float remaining, float age, int seed, Vec3[] feathers) {}

    private SeraphaeRadianceGeometry() {}

    static boolean handles(Form form) { return form == Form.SERAPHAE_RELIQUARY; }

    static void collect(ClientLevel level, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
                        double time, float partial) {
        AABB near = new AABB(camera, camera).inflate(96);
        for (SeraphaeEntity boss : level.getEntitiesOfClass(SeraphaeEntity.class, near)) {
            if (!boss.isAlive() || boss.isInvisible() && boss.getSeraphaeState() != SeraphaeState.DISPERSED) continue;
            var motion = SeraphaeAnimState.of(boss);
            motion.update(boss, partial);
            Vec3 origin = boss.getPosition(partial);
            float age = AnimationTime.age(boss, partial);
            Vec3[] feathers = new Vec3[6];
            for (int i = 0; i < 6; i++) feathers[i] = motion.feather(i);
            p.pushPose();
            p.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
            float rise = boss.riseTime(partial);
            p.translate(0, -SaintRise.depth(rise, boss.getBbHeight()), 0);
            body(p, v, camera.subtract(origin), right, up,
                    new Frame(boss.getSeraphaeState(), motion.remaining(), age, boss.getId(), feathers));
            p.popPose();
            if (rise >= 0 && rise < SaintRise.TICKS) continue;
            for (ContainmentAnchorEntity anchor : level.getEntitiesOfClass(ContainmentAnchorEntity.class,
                    boss.getBoundingBox().inflate(40), a -> boss.getUUID().equals(a.getParentId()))) {
                Vec3 at = anchor.getPosition(partial).subtract(camera);
                anchor(p, v, at, right, up, time, anchor.getId(), anchor.isActive());
                if (boss.getSeraphaeState() != SeraphaeState.CONDENSING && !anchor.isActive()) continue;
                Vec3 local = anchor.position().subtract(origin);
                boolean left = local.x * Math.cos(Math.toRadians(boss.yBodyRot))
                        + local.z * Math.sin(Math.toRadians(boss.yBodyRot)) < 0;
                Vec3 wrist = origin.add(motion.wrist(left)).subtract(camera);
                float reveal = boss.getSeraphaeState() == SeraphaeState.CONDENSING
                        ? HemoEasing.smoothstep(SeraphaeVisualRules.elapsed(boss.getSeraphaeState(), motion.remaining()) / 12) : .45F;
                tether(p, v, at.add(0, .8, 0), wrist, Vec3.ZERO, right, up, time, anchor.getId(), reveal);
            }
        }
        for (SeraphaeFragmentEntity fragment : level.getEntitiesOfClass(SeraphaeFragmentEntity.class, near)) {
            if (!fragment.isAlive() || fragment.isInvisible()) continue;
            Vec3 at = fragment.getPosition(partial).subtract(camera).add(0, .7, 0);
            fragment(p, v, at, right, up, AnimationTime.age(fragment, partial), fragment.getId());
        }
    }

    static void body(PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up, Frame frame) {
        var state = frame.state();
        var pose = SeraphaeVisualRules.pose(state, frame.remaining(), frame.age());
        float age = frame.age(), elapsed = SeraphaeVisualRules.elapsed(state, frame.remaining());
        int seed = frame.seed();
        boolean dispersed = state == SeraphaeState.DISPERSED;
        if (dispersed || state == SeraphaeState.STABLE && elapsed % 80 >= 56) {
            float warning = dispersed ? .25F + SeraphaeVisualRules.burstOpacity(elapsed % 40, 40) * .35F : pose.strain();
            ground(p, v, new Vec3(0, .04, 0), 10, RIPPLE, seed, TINT, warning * .6F);
        }
        if (!dispersed) {
            Vec3 heart = new Vec3(0, 1.55 + pose.lift(), 0);
            card(p, v, heart, right, up, .2, .45, WISP, seed, TINT, .1F + pose.strain() * .08F);
            card(p, v, heart, right, up, .07 + pose.glow() * .04, .1 + pose.glow() * .05, FOCUS, seed, CORE, .6F);
            for (int i = 0; i < frame.feathers().length; i++) {
                Vec3 tip = frame.feathers()[i];
                double sway = Math.sin(age * .11 - i * .8) * .1;
                Vec3 end = tip.add((i < 3 ? -1 : 1) * (.12 + pose.strain() * .3), .25 + sway, .08);
                ribbonPath(p, v, List.of(tip, tip.lerp(end, .5).add(0, .06, 0), end), camera,
                        .035 + pose.strain() * .035, .008, RIBBON, seed + i, TINT, .26F + pose.strain() * .35F);
                card(p, v, tip, right, up, .055, .11, WISP, seed + i, CORE, .22F + pose.strain() * .3F);
                if (pose.strain() > .01F) {
                    Vec3 outward = tip.subtract(heart).normalize();
                    double length = .45 + .6 * (.5 + .5 * Math.sin(age * .12 - i));
                    Vec3 fray = tip.add(outward.scale(length)).add(0, .15, 0);
                    List<Vec3> tear = List.of(tip, tip.lerp(fray, .5).add(0, .08, 0), fray);
                    ribbonPath(p, v, tear, camera, .14, .015, WISP, seed + i + 20, TINT, pose.strain() * .3F);
                    ribbonPath(p, v, tear, camera, .025, .003, SLASH, seed + i + 30, CORE, pose.strain() * .85F);
                }
            }
        }
        if (dispersed || state == SeraphaeState.CONDENSING || state == SeraphaeState.FRACTURING && pose.spread() > .2) {
            float gather = state == SeraphaeState.CONDENSING ? HemoEasing.smoothstep(elapsed / 38) : 0;
            float fade = state == SeraphaeState.FRACTURING ? pose.spread() * .65F : 1 - gather;
            double orbit = (dispersed ? 3.4 : state == SeraphaeState.CONDENSING ? 3.4 * (1 - gather) : 1.8);
            for (int i = 0; i < 10; i++) {
                double angle = i * 2.39996 + age * .025 + seed;
                Vec3 at = new Vec3(Math.cos(angle) * orbit, 1.2 + Math.sin(angle * 1.7 + age * .04) * .8,
                        Math.sin(angle) * orbit);
                Vec3 drift = new Vec3(-Math.sin(angle) * .45, .2, Math.cos(angle) * .45);
                if (state == SeraphaeState.CONDENSING) drift = at.subtract(new Vec3(0, 1.5, 0)).normalize().scale(.6);
                feather(p, v, at, drift, camera, right, up, age, seed + i, fade);
            }
        }
    }

    /** An exposed white axon with forked gold impulses; no feather vane. */
    private static void feather(PoseStack p, VertexConsumer v, Vec3 at, Vec3 drift, Vec3 camera, Vec3 right, Vec3 up,
                                double time, int seed, float alpha) {
        float flicker = .72F + .2F * (float) Math.sin(time * .13 + seed * 1.7);
        Vec3 tail = at.add(drift), middle = at.lerp(tail, .5).add(0, .06, 0);
        ribbonPath(p, v, List.of(at, middle, tail), camera, .035, .006, WISP, seed, TINT, alpha * .26F * flicker);
        ribbonPath(p, v, List.of(at, middle, tail), camera, .018, .003, RIBBON, seed + 1, CORE, alpha * .8F);
        float impulse = (float) Math.pow(Math.max(0, Math.sin(time * .32 - seed * .7)), 6);
        for (int side : new int[]{-1, 1}) {
            Vec3 fork = middle.add(right.scale(side * .14)).add(0, .12, 0);
            ribbonPath(p, v, List.of(middle, fork, fork.add(drift.scale(.35))), camera,
                    .012, .003, RIBBON, seed + 2, 0xFFD563, alpha * (.25F + impulse * .7F));
        }
        card(p, v, at, right, up, .04, .065, FOCUS, seed + 3, CORE, alpha * .65F);
    }

    static void anchor(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, double time, int seed, boolean active) {
        float pulse = .7F + .2F * (float) Math.sin(time * .09 + seed);
        ground(p, v, at.add(0, .035, 0), active ? 1.4 : .75, RIPPLE, seed, TINT, active ? .65F : .22F);
        card(p, v, at.add(0, .8, 0), right, up, active ? .17 : .07, active ? .28 : .12,
                FOCUS, seed, CORE, pulse * (active ? .85F : .4F));
        for (int i = 0; i < 3; i++) {
            double a = i * Math.PI * 2 / 3 + time * .018;
            Vec3 root = at.add(Math.cos(a) * .22, .15, Math.sin(a) * .22);
            ribbonPath(p, v, List.of(root, root.add(0, active ? 1.2 : .5, 0)), Vec3.ZERO,
                    .018, RIBBON, seed + i, TINT, active ? .55F : .2F);
        }
    }

    /** Shell, three twisting chain strands, a narrow core and returning knots of captured light. */
    static void tether(PoseStack p, VertexConsumer v, Vec3 from, Vec3 to, Vec3 camera, Vec3 right, Vec3 up,
                       double time, int seed, float alpha) {
        Vec3 axis = to.subtract(from);
        if (axis.lengthSqr() < 1e-6 || alpha <= 0) return;
        Vec3 side = axis.cross(UP);
        if (side.lengthSqr() < 1e-6) side = new Vec3(1, 0, 0); else side = side.normalize();
        Vec3 lift = side.cross(axis.normalize()).normalize();
        List<Vec3> spine = new ArrayList<>(19);
        for (int i = 0; i <= 18; i++) {
            double t = i / 18.0;
            spine.add(from.lerp(to, t).add(0, -Math.sin(t * Math.PI) * .28 * (1 - alpha), 0));
        }
        ribbonPath(p, v, spine, camera, .11, BEAM, seed, TINT, alpha * .15F);
        for (int strand = 0; strand < 3; strand++) {
            List<Vec3> braid = new ArrayList<>(19);
            for (int i = 0; i <= 18; i++) {
                double angle = i * .85 - time * .13 + strand * Math.PI * 2 / 3;
                braid.add(spine.get(i).add(side.scale(Math.cos(angle) * .055)).add(lift.scale(Math.sin(angle) * .055)));
            }
            ribbonPath(p, v, braid, camera, .019, RIBBON, seed + strand, strand == 1 ? CLOT : TINT, alpha * .7F);
        }
        ribbonPath(p, v, spine, camera, .012, RIBBON, seed + 4, CORE, alpha * .9F);
        for (int i = 0; i < 3; i++) {
            double t = ((time * .025 + i / 3.0 + seed * .1) % 1 + 1) % 1;
            card(p, v, from.lerp(to, t), right, up, .045, .065, FOCUS, seed + i, CORE, alpha * .8F);
        }
    }

    static void fragment(PoseStack p, VertexConsumer v, Vec3 at, Vec3 right, Vec3 up, float age, int seed) {
        float beat = .75F + .25F * (float) Math.sin(age * .13 + seed);
        card(p, v, at, right, up, .13, .32, FOCUS, seed, CORE, .75F);
        ribbonPath(p, v, List.of(at.add(0, -.48, 0), at, at.add(0, .55, 0)), Vec3.ZERO,
                new double[]{.01, .11, .005}, SLASH, seed, TINT, .9F);
        for (int i = 0; i < 3; i++) {
            double angle = age * .065 + i * Math.PI * 2 / 3 + seed;
            Vec3 point = at.add(Math.cos(angle) * .34, Math.sin(age * .1 + i) * .18, Math.sin(angle) * .34);
            List<Vec3> shard = List.of(point.add(0, -.35, 0), point, point.add(0, .35, 0));
            ribbonPath(p, v, shard, Vec3.ZERO, new double[]{.008, .075, .003}, SLASH, seed + i, TINT, .8F * beat);
            ribbonPath(p, v, shard, Vec3.ZERO, .01, RIBBON, seed + i + 4, CORE, .9F);
            feather(p, v, point, new Vec3(-Math.sin(angle) * .3, .15, Math.cos(angle) * .3),
                    Vec3.ZERO, right, up, age, seed + i, .7F);
        }
    }

    /** Short server-authored cues survive the body disappearing; no shared formation fade swallows the impact. */
    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
                     double time, float age, float alpha) {
        float fade = alpha * SeraphaeVisualRules.burstOpacity(age, packet.ticks());
        if (fade <= 0) return;
        int seed = Math.floorMod(packet.from().hashCode(), 113), kind = packet.count();
        double radius = packet.radius(), grow = HemoEasing.easeOutCubic(age / 9.0);
        if (kind == SeraphaeVisualRules.TRANSFUSION) {
            Vec3 end = packet.to().subtract(packet.from());
            double travel = HemoEasing.smoothstep(age / 12);
            Vec3 head = end.scale(travel), root = end.scale(Math.max(0, travel - .4));
            ribbonPath(p, v, List.of(root, root.lerp(head, .5).add(0, .08, 0), head), camera,
                    .025, .006, RIBBON, seed, TINT, fade);
            for (int i = 0; i < 3; i++) {
                Vec3 fork = head.add(right.scale((i - 1) * .18)).add(0, .12 * (i % 2), 0);
                ribbonPath(p, v, List.of(head, fork, fork.add(0, -.18, 0)), camera,
                        .012, .003, RIBBON, seed + i, 0xFFD563, fade);
            }
            return;
        }
        if (kind == SeraphaeVisualRules.CONVULSION) {
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3;
                Vec3 low = new Vec3(Math.cos(a) * .2, -.7, Math.sin(a) * .2);
                ribbonPath(p, v, List.of(low, low.add(.1, .45, -.08), low.add(-.06, .8, .06), low.add(0, 1.2, 0)),
                        camera, .025, .004, RIBBON, seed + i, 0xFFD563, fade);
            }
            return;
        }
        if (kind == SeraphaeVisualRules.CAPTURE) {
            Vec3 end = packet.to().subtract(packet.from());
            float travel = HemoEasing.smoothstep(age / 18);
            Vec3 head = end.scale(travel);
            tether(p, v, end.scale(Math.max(0, travel - .45)), head, camera, right, up, time, seed, fade);
            card(p, v, head, right, up, .16, .24, FOCUS, seed, CORE, fade);
            return;
        }
        boolean sealed = kind == SeraphaeVisualRules.SEALED;
        double size = radius * (sealed ? 1 - HemoEasing.smoothstep(age / 32.0) * .88 : .15 + grow * .85);
        card(p, v, Vec3.ZERO, right, up, Math.max(.04, .28 * radius * (1 - age / packet.ticks())),
                Math.max(.04, .28 * radius * (1 - age / packet.ticks())), FOCUS, seed, CORE, fade * .8F);
        for (int i = 0; i < (sealed ? 16 : 10); i++) {
            double a = i * 2.39996 + seed;
            Vec3 ray = new Vec3(Math.cos(a), Math.sin(a * 1.3) * .65, Math.sin(a)).normalize();
            Vec3 tip = ray.scale(size);
            if (sealed) {
                Vec3 inward = tip.scale(.55);
                ribbonPath(p, v, List.of(tip, tip.lerp(inward, .5).add(0, .1, 0), inward), camera,
                        .045, .008, RIBBON, seed + i, TINT, fade * .7F);
            } else {
                feather(p, v, tip, ray.scale(-.35 * radius), camera, right, up, time, seed + i, fade);
            }
        }
        ground(p, v, new Vec3(0, -.7, 0), Math.max(.05, size), RIPPLE, seed, TINT, fade * .32F);
    }
}
