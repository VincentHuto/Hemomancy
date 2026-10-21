package com.vincenthuto.hemomancy.client.render.outpouring;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringSim.Mote;
import com.vincenthuto.hemomancy.client.render.world.OutpouringBrushes;
import com.vincenthuto.hemomancy.client.render.world.OutpouringBrushes.Mat;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws every caster's blood and the marks it leaves through the manipulation toolkit (living blood, flame, cruor and
 * glass, blood-iron, mortem, lux, umbra) so the outpouring wears the mod's own art. Runs after particles while the camera
 * rotation is on the model-view stack, so every point is camera-relative. The caster's own blood near a first-person
 * camera is skipped so it never walls off the view.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class OutpouringRenderer {
    private static final double NEAR_SELF = .8;
    private static final int WHITE = 0xFFF4EE, CRIMSON = 0xF0566A;

    private OutpouringRenderer() {}

    private record View(PoseStack p, Vec3 camera, Vec3 right, Vec3 up, double time, float partial,
                        MultiBufferSource.BufferSource buffers) {
        Vec3 local(double x, double y, double z) { return new Vec3(x - camera.x, y - camera.y, z - camera.z); }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var casts = OutpouringClientState.tracker().all();
        var marks = OutpouringMarks.client();
        if (casts.isEmpty() && marks.isEmpty()) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        var cameraInfo = event.getCamera();
        Quaternionf rotation = cameraInfo.rotation();
        long now = mc.level.getGameTime();
        var view = new View(new PoseStack(), cameraInfo.getPosition(), new Vec3(new Vector3f(1, 0, 0).rotate(rotation)),
                new Vec3(new Vector3f(0, 1, 0).rotate(rotation)), now + partial, partial, mc.renderBuffers().bufferSource());
        OutpouringBrushes.begin(view.time);
        for (var m : OutpouringMarks.visible(marks, mc.level, view.camera)) {
            double age = now - m.born() + partial;
            float alpha = (float) (Math.clamp(age / 8, 0, 1) * (1 - OutpouringMarks.fade(age, m.life())));
            if (alpha <= .01f) continue;
            if (m.pool()) pool(view, m, now, age, alpha);
            else mark(view, m, OutpouringMarks.quad(m, now, partial), alpha);
        }
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        for (var cast : casts) {
            var sim = cast.sim;
            if (sim == null) continue;
            var entity = mc.level.getEntity(cast.entityId);
            if (entity != null && entity.isInvisible()) continue;
            boolean self = firstPerson && mc.player != null && cast.player.equals(mc.player.getUUID());
            if (!self && entity instanceof Player player && cast.emitting(now)) exits(view, cast, player);
            for (Mote m : sim.motes()) {
                double x = m.prevX + (m.x - m.prevX) * partial, y = m.prevY + (m.y - m.prevY) * partial;
                double z = m.prevZ + (m.z - m.prevZ) * partial;
                Vec3 at = view.local(x, y, z);
                if (self && at.lengthSqr() < NEAR_SELF * NEAR_SELF) continue;
                float age = m.age + partial;
                float alpha = Math.clamp(age / 2f, 0, 1) * Math.clamp((m.life - age) / (m.life * .25f), 0, 1);
                // Smoke (Tenebris puffs, burnt-out flame) thins over the caster's own body so it frames them instead.
                if (entity != null && (m.kind == OutpouringSim.Kind.PUFF || m.kind == OutpouringSim.Kind.FLAME && age / m.life >= .65f)) {
                    Vec3 feet = entity.getPosition(partial);
                    alpha *= OutpouringRules.smokeNearBody(x - feet.x, y - feet.y, z - feet.z);
                }
                if (alpha > .01f) mote(view, cast.school, m, at, age, alpha);
            }
        }
        OutpouringBrushes.flush(view.buffers);
    }

    //  Blood leaving the body

    /** At each open exit the school's own flourish: flame tongues licking off Flammeus, a light bud on Lux, a welling bead. */
    private static void exits(View view, OutpouringTracker.Cast cast, Player player) {
        var pulse = cast.pulse(player.level().getGameTime(), view.partial);
        float strength = pulse.strength();
        if (strength <= .02f) return;
        double[] outward = new double[15];
        double[] exits = OutpouringAnchors.exits(player, cast.section, cast.rank, view.partial, outward);
        int rank = cast.rank.ordinal();
        for (int i = 0; i < exits.length / 3; i++) {
            Vec3 at = view.local(exits[i * 3], exits[i * 3 + 1], exits[i * 3 + 2]);
            Vec3 lean = new Vec3(outward[i * 3], 0, outward[i * 3 + 2]).scale(.35);
            int seed = (int) cast.sequence * 31 + i;
            switch (cast.school) {
                case FLAMMEUS -> {
                    for (int t = 0; t <= rank / 2; t++)
                        OutpouringBrushes.tongue(view.p, OutpouringBrushes.flow(Mat.FLAME), at, lean.add(t * .08 - .08, 0, 0),
                                (.18 + .06 * rank) * strength, .045 + .01 * rank, view.time, seed + t * 7, strength);
                }
                case LUX -> OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.LUX), at, view.right, view.up,
                        .06 + .015 * rank, .06 + .015 * rank, OutpouringBrushes.FOCUS, seed, WHITE, strength);
                case ANIMUS, MORTEM -> OutpouringBrushes.card(view.p, OutpouringBrushes.flow(cast.school == EnumBloodTendency.ANIMUS
                        ? Mat.ANIMUS : Mat.MORTEM), at, view.right, view.up, .045, .045, OutpouringBrushes.FOCUS, seed, 0xFFFFFF,
                        strength * .9f);
                case TENEBRIS -> OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.UMBRA), at, view.right, view.up,
                        .1, .1, OutpouringBrushes.WISP, seed, 0xFFFFFF, strength * .7f);
                default -> {}
            }
        }
    }

    private static void mote(View view, EnumBloodTendency school, Mote m, Vec3 at, float age, float alpha) {
        var p = view.p;
        switch (m.kind) {
            case DROP -> {
                List<Vec3> path = trail(view, m, at);
                if (school == EnumBloodTendency.CONGEATIO) {
                    // Blood freezing as it flies: a frost-shelled clot trailing a frozen strand.
                    if (path.size() > 1) OutpouringBrushes.ribbonPath(p, OutpouringBrushes.flow(Mat.CRUOR), path, m.size * .5, m.size * .1,
                            OutpouringBrushes.FROST_STRAND, m.seed, 0xFFFFFF, alpha * .8f);
                    OutpouringBrushes.clot(p, OutpouringBrushes.core(view.buffers), at, m.size * .7, m.seed, age * .1, alpha);
                } else {
                    if (path.size() > 1) OutpouringBrushes.ribbonPath(p, OutpouringBrushes.flow(Mat.ANIMUS), path, m.size * .55, m.size * .12,
                            OutpouringBrushes.RIBBON, m.seed, 0xFFFFFF, alpha);
                    OutpouringBrushes.card(p, OutpouringBrushes.flow(Mat.ANIMUS), at, view.right, view.up, m.size * .5, m.size * .5,
                            OutpouringBrushes.FOCUS, m.seed, 0xFFFFFF, alpha);
                }
            }
            case WISP -> {
                // The Riposte's wisp mist, wound into a whirlwind of blood.
                double flicker = .85 + .25 * Math.sin(view.time * .9 + m.seed * 1.7);
                double width = m.size * flicker;
                OutpouringBrushes.card(p, OutpouringBrushes.flow(Mat.ANIMUS), at, view.right, view.up, width, width * 1.25,
                        OutpouringBrushes.WISP, m.seed, 0xFFFFFF, alpha * .32f);
                List<Vec3> path = trail(view, m, at);
                if (path.size() > 2) OutpouringBrushes.ribbonPath(p, OutpouringBrushes.flow(Mat.ANIMUS), path, m.size * .18, m.size * .04,
                        OutpouringBrushes.RIBBON, m.seed + 3, 0xFFFFFF, alpha * .3f);
            }
            case STRAND -> {
                List<Vec3> path = trail(view, m, at);
                if (path.size() > 1) OutpouringBrushes.ribbonPath(p, OutpouringBrushes.flow(Mat.DUCTILIS), path, .022, .006, 0,
                        m.seed, OutpouringBrushes.NERVE, alpha * .85f);
            }
            case FLAME -> {
                float burn = age / m.life;
                if (burn < .65f) OutpouringBrushes.fire(p, OutpouringBrushes.flow(Mat.FLAME), at, view.right, view.up,
                        m.size * .55, m.size * 1.1, m.seed, alpha);
                else OutpouringBrushes.smoke(p, OutpouringBrushes.flow(Mat.FLAME), at.add(0, m.size * .8, 0), view.right, view.up,
                        m.size * .7, m.size * .9, m.seed, alpha * .55f);
            }
            case ORB -> {
                int color = (m.seed & 1) == 0 ? WHITE : CRIMSON;
                OutpouringBrushes.card(p, OutpouringBrushes.flow(Mat.LUX), at, view.right, view.up, m.size * 1.4, m.size * 1.4,
                        OutpouringBrushes.FOCUS, m.seed, color, alpha);
                OutpouringBrushes.card(p, OutpouringBrushes.flow(Mat.LUX), at, view.right, view.up, m.size * 3, m.size * 3,
                        OutpouringBrushes.POOL, m.seed + 1, color, alpha * .35f);
            }
            case PUFF -> {
                // Billowing smoke: three lobes that churn round the puff's centre, each turning and breathing on its own.
                double spin = (m.seed & 1) == 0 ? 1 : -1;
                for (int k = 0; k < 3; k++) {
                    double orbit = view.time * (.03 + .015 * k) * spin + k * 2.094 + m.seed;
                    double turn = view.time * (.025 + .01 * k) * -spin + m.seed * .7 + k;
                    double breathe = .7 + .18 * Math.sin(view.time * .09 + m.seed + k * 1.9);
                    Vec3 offset = view.right.scale(Math.cos(orbit) * m.size * .35).add(view.up.scale(Math.sin(orbit) * m.size * .25));
                    Vec3 right = view.right.scale(Math.cos(turn)).add(view.up.scale(Math.sin(turn)));
                    Vec3 up = view.up.scale(Math.cos(turn)).subtract(view.right.scale(Math.sin(turn)));
                    Vec3 lobe = at.add(offset);
                    // A blood-mist halo just behind each lobe: the fog is still blood, glowing red at its edges.
                    OutpouringBrushes.card(p, OutpouringBrushes.flow(Mat.ANIMUS), lobe.add(lobe.normalize().scale(.04)), right, up,
                            m.size * breathe * 1.2, m.size * breathe, OutpouringBrushes.WISP, m.seed + k * 13 + 5, 0xFFFFFF, alpha * .22f);
                    OutpouringBrushes.card(p, OutpouringBrushes.flow(Mat.UMBRA), lobe, right, up, m.size * breathe,
                            m.size * breathe * .85, OutpouringBrushes.WISP, m.seed + k * 13, 0xFFFFFF, alpha * .42f);
                }
                // Heavy as liquid: each puff drags a thin, dark blood smear down behind it.
                List<Vec3> path = trail(view, m, at);
                if (path.size() > 2) OutpouringBrushes.ribbonPath(p, OutpouringBrushes.flow(Mat.ANIMUS), path, m.size * .12, m.size * .02,
                        OutpouringBrushes.RIBBON, m.seed + 7, 0xFFFFFF, alpha * .35f);
            }
            case BLOB -> {
                if (m.stuck) {
                    faceCard(view, OutpouringBrushes.flow(Mat.MORTEM), at, m.face, m.size * 1.6, OutpouringBrushes.POOL, m.seed, alpha);
                } else {
                    List<Vec3> path = trail(view, m, at);
                    if (path.size() > 1) OutpouringBrushes.ribbonPath(p, OutpouringBrushes.flow(Mat.MORTEM), path, m.size * .5, m.size * .15,
                            OutpouringBrushes.RIBBON, m.seed, 0xFFFFFF, alpha);
                    OutpouringBrushes.card(p, OutpouringBrushes.flow(Mat.MORTEM), at, view.right, view.up, m.size * .9, m.size * .9,
                            OutpouringBrushes.FOCUS, m.seed, 0xFFFFFF, alpha);
                }
            }
            case SHARD -> icicles(view, at, m, alpha);
            case PLATE -> {
                var iron = OutpouringBrushes.iron(view.buffers);
                float formation = Math.clamp(age / 6f, 0, 1) * alpha;
                p.pushPose();
                p.translate(at.x, at.y, at.z);
                if (m.stuck) {
                    face(p, m.face);
                    p.mulPose(Axis.ZP.rotation((m.seed & 63) * .1f));
                    OutpouringBrushes.plate(p, iron, new Vec3(0, 0, m.size * .18), m.size * 1.6, m.size * 1.1, m.size * .35, m.seed, formation);
                } else {
                    p.mulPose(Axis.of(new Vector3f(1, (m.seed & 7) - 3.5f, .5f).normalize()).rotation(age * .3f));
                    OutpouringBrushes.plate(p, iron, Vec3.ZERO, m.size * 1.6, m.size * 1.1, m.size * .35, m.seed, formation);
                }
                p.popPose();
            }
            case SPIKE -> {
                if (m.stuck) spike(view, at, m, alpha);
                else {
                    // Still liquid in flight: a blood sinew that hardens where it lands.
                    double speed = Math.max(1e-6, Math.sqrt(m.vx * m.vx + m.vy * m.vy + m.vz * m.vz));
                    Vec3 tail = at.subtract(m.vx / speed * m.size * 1.6, m.vy / speed * m.size * 1.6, m.vz / speed * m.size * 1.6);
                    OutpouringBrushes.sinew(p, tail, at, .05, .04, view.time, m.seed, OutpouringBrushes.BLOOD, alpha);
                }
            }
        }
    }

    /** Ice growing out of the surface the blood froze on: a cluster of thick glass shards around a frozen clot. */
    private static void icicles(View view, Vec3 at, Mote m, float alpha) {
        var p = view.p;
        float grow = Math.min(1, (m.settled + 1) / 4f);
        p.pushPose();
        p.translate(at.x, at.y, at.z);
        upTo(p, m.face);
        for (int k = 0; k < 3; k++) {
            p.pushPose();
            p.mulPose(Axis.YP.rotation(k * 2.1f + (m.seed & 15) * .4f));
            p.mulPose(Axis.ZP.rotation(((m.seed >> (k + 2)) & 7) * .06f - .2f));
            OutpouringBrushes.shard(p, m.size * (2.6 - k * .5) * grow, m.seed + k, alpha * .95f);
            p.popPose();
        }
        p.popPose();
        OutpouringBrushes.clot(p, OutpouringBrushes.core(view.buffers), at, m.size * .9 * grow, m.seed, m.seed * .3, alpha);
    }

    /** A forged blood-iron spike erupting from the surface: eight-sided, tapering in two stages, its blood front rising. */
    private static void spike(View view, Vec3 at, Mote m, float alpha) {
        var p = view.p;
        var iron = OutpouringBrushes.iron(view.buffers);
        double grow = Math.min(1, (m.settled + 1) / 3.0);
        double height = m.size * 2.4 * grow, radius = m.size * .32;
        float formation = (float) (alpha * grow);
        p.pushPose();
        p.translate(at.x, at.y, at.z);
        upTo(p, m.face);
        p.mulPose(Axis.YP.rotation((m.seed & 31) * .2f));
        int sides = 8;
        Vec3[] base = new Vec3[sides], waist = new Vec3[sides];
        for (int i = 0; i < sides; i++) {
            double a = i * Math.PI * 2 / sides, flute = i % 2 == 0 ? 1 : .82;
            base[i] = new Vec3(Math.cos(a) * radius * flute, 0, Math.sin(a) * radius * flute);
            waist[i] = new Vec3(Math.cos(a + .18) * radius * .5 * flute, height * .45, Math.sin(a + .18) * radius * .5 * flute);
        }
        Vec3 tip = new Vec3(radius * .08, height, 0);
        for (int i = 0; i < sides; i++) {
            int j = (i + 1) % sides;
            OutpouringBrushes.ferricQuad(p, iron, base[i], base[j], waist[j], waist[i], m.seed + i, formation);
            OutpouringBrushes.ferricQuad(p, iron, waist[i], waist[j], tip, tip, m.seed + i + 8, formation);
        }
        p.popPose();
    }

    //  Marks and pools

    /** Animus blood that lands on the floor stands in a pool of brazier blood. */
    private static void pool(View view, OutpouringMarks.Mark m, long now, double age, float alpha) {
        var level = Minecraft.getInstance().level;
        var pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        // A pool spreads only over floor at its own level with open air above: walls and ledges stop it.
        OutpouringMarks.Floor floor = (x, z) -> {
            if (!level.getBlockState(pos.set(x, m.by(), z)).blocksMotion()) return false;
            var above = level.getBlockState(pos.set(x, m.by() + 1, z));
            return !above.blocksMotion() && above.getFluidState().isEmpty();
        };
        double[] c = OutpouringMarks.poolCentre(m, now, view.partial, floor);
        float radius = (float) OutpouringMarks.fit(c[0], c[2], OutpouringMarks.poolRadius(m) * Math.min(1, age / 10 + .3), floor);
        if (radius < .05f) return;
        var type = HemoRenderTypes.bloodPool((float) (view.time % 24000), m.seed() % 1000, 1F);
        BloodPoolSurface.draw(view.p.last().pose(), view.buffers.getBuffer(type), (float) (c[0] - view.camera.x),
                (float) (c[1] - view.camera.y + .002), (float) (c[2] - view.camera.z), radius, 0, 40, 0, Math.round(alpha * 235));
        view.buffers.endBatch(type);
    }

    private static void mark(View view, OutpouringMarks.Mark m, double[] q, float alpha) {
        Mat material;
        int shape, color = 0xFFFFFF;
        switch (m.school()) {
            case ANIMUS -> { material = Mat.ANIMUS; shape = OutpouringBrushes.POOL; }
            case FLAMMEUS -> { material = Mat.MOLTEN; shape = 2; }
            case DUCTILIS -> { material = Mat.DUCTILIS; shape = 2; color = OutpouringBrushes.NERVE; }
            case LUX -> { material = Mat.LUX; shape = OutpouringBrushes.POOL; alpha *= .4f; }
            case MORTEM -> { material = Mat.MORTEM; shape = 8; }
            case CONGEATIO -> { material = Mat.CRUOR; shape = 2; }
            case TENEBRIS -> { material = Mat.UMBRA; shape = OutpouringBrushes.POOL; }
            default -> { ironMark(view, m, q, alpha); return; }
        }
        var v = OutpouringBrushes.flow(material);
        int phase = Math.floorMod(m.seed(), 127) * 2;
        int a = Math.round(Math.clamp(alpha, 0, 1) * 255);
        for (int i = 0; i < 4; i++) {
            v.addVertex((float) (q[i * 5] - view.camera.x), (float) (q[i * 5 + 1] - view.camera.y), (float) (q[i * 5 + 2] - view.camera.z))
                    .setColor(color >> 16 & 255, color >> 8 & 255, color & 255, a)
                    .setUv((float) (shape * 2 + q[i * 5 + 3]), (float) (phase + q[i * 5 + 4]));
        }
    }

    /** Ferric blood that hardened against a surface: a flush iron plate whose formation front recedes as it fades. */
    private static void ironMark(View view, OutpouringMarks.Mark m, double[] q, float alpha) {
        var p = view.p;
        double cx = (q[0] + q[10]) * .5, cy = (q[1] + q[11]) * .5, cz = (q[2] + q[12]) * .5;
        p.pushPose();
        p.translate(cx - view.camera.x, cy - view.camera.y, cz - view.camera.z);
        face(p, m.face());
        p.mulPose(Axis.ZP.rotation((m.seed() & 63) * .1f));
        OutpouringBrushes.plate(p, OutpouringBrushes.iron(view.buffers), new Vec3(0, 0, .02), m.size() * .7, m.size() * .5, .04, m.seed(), alpha);
        p.popPose();
    }

    //  Helpers

    private static List<Vec3> trail(View view, Mote m, Vec3 head) {
        List<Vec3> path = new ArrayList<>(6);
        path.add(head);
        // trail[0] is this tick's position, ahead of the interpolated head; the path runs on from the one before.
        for (int i = 1; i < Math.min(6, m.trailCount); i++) path.add(view.local(m.trail[i * 3], m.trail[i * 3 + 1], m.trail[i * 3 + 2]));
        return path;
    }

    private static void faceCard(View view, VertexConsumer v, Vec3 at, int face, double half, int shape, int seed, float alpha) {
        Vec3 normal = OutpouringGeometry.faceNormal(face);
        Vec3[] axes = OutpouringGeometry.faceAxes(normal);
        OutpouringBrushes.card(view.p, v, at.add(normal.scale(.01)), axes[0], axes[1], half, half, shape, seed, 0xFFFFFF, alpha);
    }

    /** Turns the pose so local +Z points out of the face. */
    private static void face(PoseStack p, int face) {
        Vec3 n = OutpouringGeometry.faceNormal(face);
        p.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 0, 1), new Vector3f((float) n.x, (float) n.y, (float) n.z)));
    }

    /** Turns the pose so local +Y points out of the face. */
    private static void upTo(PoseStack p, int face) {
        Vec3 n = OutpouringGeometry.faceNormal(face);
        p.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), new Vector3f((float) n.x, (float) n.y, (float) n.z)));
    }
}
