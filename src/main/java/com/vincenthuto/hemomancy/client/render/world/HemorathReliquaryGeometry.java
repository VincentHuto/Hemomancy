package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Hollow iron and circulating blood: Ferric links and layered Animus pressure. */
final class HemorathReliquaryGeometry {
    private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    private static final Vec3 UP = new Vec3(0, 1, 0);
    record Frame(int visual, float remaining, float age, int seed, boolean phase2, boolean charging,
                 float stored, Vec3 core, Vec3 claw, Vec3 staff) {}

    private HemorathReliquaryGeometry() {}
    static boolean handles(Form form) { return form == Form.HEMORATH_RELIQUARY; }

    static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron, Vec3 camera,
                        Vec3 right, Vec3 up, double time, float partial, boolean solids) {
        for (HemorathEntity boss : level.getEntitiesOfClass(HemorathEntity.class, new AABB(camera, camera).inflate(96))) {
            if (!boss.isAlive() || boss.isInvisible()) continue;
            var motion = HemorathAnimState.of(boss);
            motion.update(boss, partial);
            Vec3 origin = boss.getPosition(partial);
            Vec3 eye = camera.subtract(origin);
            float rise = boss.riseTime(partial);
            p.pushPose();
            p.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
            p.translate(0, -SaintRise.depth(rise, boss.getBbHeight()), 0);
            Frame frame = new Frame(boss.getVisualState(), motion.remaining(), AnimationTime.age(boss, partial),
                    boss.getId(), boss.isInPhase2(), boss.isCollapseCharging(),
                    HemorathVisualRules.storedPressure(boss.getAbsorbedBlood()), motion.core(), motion.claw(), motion.staff());
            VertexConsumer blood = flows.vertices(LuxUmbraBatch.Material.ANIMUS), shade = blood;
            if (!solids) body(p, blood, shade, eye, right, up, frame);
            if (!solids && boss.getVisualState() != HemorathEntity.VISUAL_OVERLOAD) {
                double warning = motion.siphon();
                ring(p, blood, 6, .05, eye, boss.getId(), .025, TINT, .14F);
                if (warning > .01) ring(p, blood, 12, .06, eye, boss.getId() + 3,
                        .045 + warning * .05, TINT, (float) warning * .75F);
            }
            var target = level.getEntity(boss.getVisualTargetId());
            if (target != null && boss.getVisualState() == HemorathEntity.VISUAL_COLLAPSE_WINDUP
                    && (rise < 0 || rise >= SaintRise.TICKS)) {
                Vec3 to = target.getPosition(partial).subtract(origin).add(0, target.getBbHeight() * .6, 0);
                var action = HemorathVisualRules.pose(frame.visual(), frame.charging(), frame.phase2(), frame.remaining());
                if (solids) debtLinks(p, iron, frame.claw(), to, frame.seed(), action.pressure());
                else debt(p, blood, shade, frame.claw(), to, eye, right, up, time, frame.seed(), action.pressure());
            }
            p.popPose();
        }
    }

    static void body(PoseStack p, VertexConsumer blood, VertexConsumer shade, Vec3 camera, Vec3 right, Vec3 up, Frame f) {
        var action = HemorathVisualRules.pose(f.visual(), f.charging(), f.phase2(), f.remaining());
        float intensity = Math.max(f.stored(), action.pressure());
        if (f.phase2()) intensity = Math.max(intensity, .22F);
        Vec3 core = f.core();
        Vec3 across = new Vec3(f.staff().x - core.x, 0, f.staff().z - core.z);
        across = across.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : across.normalize();
        Vec3 depth = across.cross(UP);
        float beat = (float) Math.pow(Math.max(0, Math.sin(f.age() * .22)), 4);
        // Blood climbs the actual hollow chest, leaving open spaces between the strands.
        for (int i = 0; i < 4; i++) {
            double side = i < 2 ? -1 : 1;
            Vec3 low = core.add(across.scale(side * .22)).add(0, -.7 + (i % 2) * .25, 0);
            Vec3 middle = core.add(across.scale(side * (.32 + action.ribs() * .15))).add(depth.scale(.04));
            Vec3 high = core.add(across.scale(side * .1)).add(0, .5, 0);
            ribbonPath(p, shade, List.of(low, middle, high), camera, .09, .03, WISP, f.seed() + i, TINT, intensity * .18F);
            ribbonPath(p, blood, List.of(low, middle, high), camera, .025, .008, RIBBON, f.seed() + i, TINT, intensity * .7F);
        }
        card(p, blood, core, right, up, .08 + intensity * .09, .1 + intensity * .14, FOCUS, f.seed(), CORE,
                intensity * (.4F + beat * .3F));
        if (action.lift() > .01 && f.visual() != HemorathEntity.VISUAL_OVERLOAD)
            debt(p, blood, shade, core, f.claw(), camera, right, up, f.age(), f.seed(), action.lift() * .65F);
        if (f.visual() == HemorathEntity.VISUAL_EMPTY_PULSE) {
            ring(p, blood, HemorathVisualRules.PULSE_RADIUS, .06, camera, f.seed(), .035, CLOT, .45F);
            ring(p, blood, 2.8 - action.pressure() * 1.9, .1, camera, f.seed() + 1, .065, TINT, .65F);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + f.age() * .035;
                Vec3 root = new Vec3(Math.cos(a) * 2.8, .2, Math.sin(a) * 2.8);
                Vec3 bend = root.scale(.45).add(0, core.y * .7, 0);
                ribbonPath(p, shade, List.of(root, bend, core), camera, .12, .02, WISP, f.seed() + i, TINT, action.pressure() * .22F);
                ribbonPath(p, blood, List.of(root, bend, core), camera, .025, .008, RIBBON, f.seed() + i, TINT, action.pressure() * .7F);
            }
        }
        if (f.visual() == HemorathEntity.VISUAL_OVERLOAD) {
            for (int i = 0; i < 8; i++) {
                double a = i * 2.39996 + f.seed();
                double travel = ((f.age() * .07 + i * .13) % 1 + 1) % 1;
                Vec3 vent = core.add(Math.cos(a) * (.3 + travel), .5 - travel * 1.7, Math.sin(a) * (.3 + travel));
                ribbonPath(p, shade, List.of(core, vent.add(0, .25, 0), vent), camera,
                        .13, .025, WISP, f.seed() + i, TINT, action.pressure() * .22F);
                ribbonPath(p, blood, List.of(core, vent), camera, .035, .012, SLASH, f.seed() + i, TINT, action.pressure() * .75F);
                card(p, blood, vent, right, up, .045, .07, FOCUS, f.seed() + i, CORE, action.pressure() * .65F);
            }
        }
    }

    /** A dark pressure sheath around two clot strands and a pale moving debt knot. */
    static void debt(PoseStack p, VertexConsumer blood, VertexConsumer shade, Vec3 from, Vec3 to, Vec3 camera,
                     Vec3 right, Vec3 up, double time, int seed, float pressure) {
        Vec3 axis = to.subtract(from);
        if (axis.lengthSqr() < 1e-6 || pressure <= 0) return;
        Vec3 side = axis.cross(UP);
        side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
        Vec3 lift = axis.normalize().cross(side).normalize();
        List<Vec3> spine = new ArrayList<>(17);
        for (int i = 0; i <= 16; i++) {
            double t = i / 16.0;
            spine.add(from.lerp(to, t).add(0, -Math.sin(t * Math.PI) * .25 * (1 - pressure), 0));
        }
        ribbonPath(p, shade, spine, camera, .18, .08, BEAM, seed, TINT, pressure * .25F);
        for (int strand = 0; strand < 2; strand++) {
            List<Vec3> points = new ArrayList<>(17);
            for (int i = 0; i <= 16; i++) {
                double a = i * .8 + time * .15 + strand * Math.PI;
                points.add(spine.get(i).add(side.scale(Math.cos(a) * .06)).add(lift.scale(Math.sin(a) * .06)));
            }
            ribbonPath(p, blood, points, camera, .075, .04, RIBBON, seed + strand, strand == 0 ? TINT : CLOT, pressure * .8F);
        }
        double knot = 1 - ((time * .045 + seed * .13) % 1 + 1) % 1;
        card(p, blood, from.lerp(to, knot), right, up, .07, .1, FOCUS, seed, CORE, pressure * .8F);
        card(p, shade, to, right, up, .18 + pressure * .12, .32, WISP, seed + 4, TINT, pressure * .25F);
    }

    static void debtLinks(PoseStack p, VertexConsumer iron, Vec3 from, Vec3 to, int seed, float pressure) {
        if (pressure <= .05 || from.distanceToSqr(to) < .01) return;
        Vec3 axis = to.subtract(from).normalize(), side = axis.cross(UP);
        side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
        for (int i = 0; i < 8; i++) {
            Vec3 at = i < 4 ? from.add(axis.scale(.1 + i * .23)) : to.subtract(axis.scale(.1 + (i - 4) * .23));
            Vec3 width = i % 2 == 0 ? side : axis.cross(side).normalize();
            link(p, iron, at, axis.scale(.14 * pressure), width.scale(.085 * pressure), seed + i);
        }
    }

    private static void link(PoseStack p, VertexConsumer iron, Vec3 at, Vec3 along, Vec3 wide, int seed) {
        Vec3[] outer = {at.subtract(along).subtract(wide), at.add(along).subtract(wide), at.add(along).add(wide), at.subtract(along).add(wide)};
        Vec3[] inner = {at.subtract(along.scale(.72)).subtract(wide.scale(.5)), at.add(along.scale(.72)).subtract(wide.scale(.5)),
                at.add(along.scale(.72)).add(wide.scale(.5)), at.subtract(along.scale(.72)).add(wide.scale(.5))};
        for (int i = 0; i < 4; i++) {
            Vec3 a = outer[i], b = outer[(i + 1) % 4], c = inner[(i + 1) % 4], d = inner[i];
            MaterialQuads.quad(iron, p.last().pose(), a.x,a.y,a.z,b.x,b.y,b.z,c.x,c.y,c.z,d.x,d.y,d.z,
                    0, MaterialQuads.seedTile(seed), TINT, 1, 0, 1, false);
        }
    }

    private static void ring(PoseStack p, VertexConsumer v, double radius, double y, Vec3 camera, int seed,
                             double width, int color, float alpha) {
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI / 12, b = a + Math.PI / 15;
            ribbonPath(p, v, List.of(new Vec3(Math.cos(a) * radius, y, Math.sin(a) * radius),
                    new Vec3(Math.cos(b) * radius, y, Math.sin(b) * radius)), camera, width, RIBBON, seed + i, color, alpha);
        }
    }

    static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer iron, VertexConsumer blood,
                     VertexConsumer shade, boolean solids, Vec3 camera, Vec3 right, Vec3 up, double time, float age, float alpha) {
        float fade = alpha * HemorathVisualRules.fade(age, packet.ticks());
        if (fade <= 0) return;
        int kind = packet.count(), seed = Math.floorMod(packet.from().hashCode(), 113);
        double r = packet.radius();
        if (solids) {
            if (kind != HemorathVisualRules.PHASE_BREAK && kind != HemorathVisualRules.EXSANGUINATE) return;
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + seed, travel = HemoEasing.easeOutCubic(age / 14.0);
                Vec3 at = new Vec3(Math.cos(a) * r * travel, Math.sin(travel * Math.PI) * .65, Math.sin(a) * r * travel);
                link(p, iron, at, new Vec3(.13 * fade, .08 * fade, 0), new Vec3(0, .07 * fade, .06 * fade), seed + i);
            }
            return;
        }
        if (kind == HemorathVisualRules.SIPHON) {
            double radius = r * (1 - HemoEasing.easeOutCubic(age / packet.ticks()));
            ring(p, blood, Math.max(.04, radius), .08, camera, seed, .08, TINT, fade);
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                Vec3 at = new Vec3(Math.cos(a) * radius, .1, Math.sin(a) * radius);
                ribbonPath(p, blood, List.of(at, at.scale(.5).add(0, .25, 0), new Vec3(0, 1.4, 0)),
                        camera, .04, .006, RIBBON, seed + i, TINT, fade * .7F);
            }
            return;
        }
        if (kind == HemorathVisualRules.INTAKE) {
            Vec3 end = packet.to().subtract(packet.from());
            double t = HemoEasing.smoothstep(age / 12.0);
            Vec3 head = end.scale(t), tail = end.scale(Math.max(0, t - .4));
            debt(p, blood, shade, tail, head, camera, right, up, time, seed, fade);
            card(p, blood, head, right, up, .09, .15, FOCUS, seed, CORE, fade);
            return;
        }
        if (kind == HemorathVisualRules.PULSE_RELEASE) {
            double radius = Math.min(r, .4 + HemoEasing.easeOutCubic(age / 7.0) * (r - .4));
            ring(p, blood, radius, .12, camera, seed, .095 * fade, TINT, fade * .85F);
            ring(p, shade, radius * .97, .14, camera, seed + 25, .22, CLOT, fade * .3F);
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                Vec3 tip = new Vec3(Math.cos(a) * radius, .16, Math.sin(a) * radius);
                ribbonPath(p, blood, List.of(tip.scale(.82), tip.add(0, .25, 0), tip), camera,
                        .045, .006, SLASH, seed + i, CORE, fade * .6F);
            }
            return;
        }
        if (kind == HemorathVisualRules.MELEE) {
            for (double offset : slashOffsets())
                ribbonPath(p, blood, List.of(new Vec3(offset - .3, .4, 0), new Vec3(offset, 0, 0), new Vec3(offset + .1, -.4, 0)),
                        camera, .06, .012, SLASH, seed, TINT, fade);
            return;
        }
        if (kind == HemorathVisualRules.DEBT_HIT) {
            double squeeze = HemoEasing.easeOutCubic(age / 9.0);
            for (int i = 0; i < 10; i++) {
                double a = i * 2.39996 + seed;
                Vec3 direction = new Vec3(Math.cos(a), Math.sin(a * 1.5) * .65, Math.sin(a)).normalize();
                Vec3 start = direction.scale(r * (1 - squeeze * .8));
                Vec3 end = direction.scale(r * .06);
                List<Vec3> path = List.of(start, start.lerp(end, .5).add(0, .06, 0), end);
                ribbonPath(p, shade, path, camera, .16, .02, WISP, seed + i, TINT, fade * .25F);
                ribbonPath(p, blood, path, camera, .065, .012, RIBBON, seed + i, TINT, fade * .85F);
            }
            card(p, blood, Vec3.ZERO, right, up, .2, .3, FOCUS, seed, CORE, fade);
            return;
        }
        boolean finale = kind == HemorathVisualRules.EXSANGUINATE;
        double grow = HemoEasing.easeOutCubic(age / 9.0);
        for (int i = 0; i < 12; i++) {
            double a = i * 2.39996 + seed;
            Vec3 tip = new Vec3(Math.cos(a) * r * grow, finale ? -.5 : Math.sin(a * 1.7) * r * .45, Math.sin(a) * r * grow);
            Vec3 root = finale ? new Vec3(0, .45, 0) : tip.scale(1.5 - grow);
            Vec3 middle = root.lerp(tip, .5).add(0, finale ? .7 : .1, 0);
            List<Vec3> path = List.of(root, middle, tip);
            ribbonPath(p, shade, path, camera, .19 * fade, .03, WISP, seed + i, TINT, fade * .25F);
            ribbonPath(p, blood, path, camera, .045 * fade, .01, RIBBON, seed + i, TINT, fade * .8F);
            card(p, blood, tip, right, up, .045, .07, FOCUS, seed + i, CORE, fade * .7F);
        }
        card(p, shade, Vec3.ZERO, right, up, .2 + grow * .45, .35 + grow * .55, WISP, seed, TINT, fade * .25F);
    }
}
