package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationFlowPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hutoslib.client.particle.data.TendrilGeometry;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LuxUmbraEffects {
    private static final List<Connection> FLOWS = new ArrayList<>();
    private static final List<Mist> MISTS = new ArrayList<>();
    private static ClientLevel world;

    private LuxUmbraEffects() {}

    public static void clear() { FLOWS.clear(); MISTS.clear(); world = null; }

    private static boolean ready() {
        var level = Minecraft.getInstance().level;
        if (level != world) { clear(); world = level; }
        return level != null;
    }

    public static void accept(ManipulationFlowPacket packet) {
        if (!ready()) return;
        if (FLOWS.size() >= 128) FLOWS.removeFirst();
        var config = packet.config().clamped();
        var flow = new LuxUmbraFlow(new ManipulationFlowPacket(packet.umbra(), packet.start(), packet.end(), config), world.getGameTime());
        if (flow.resolve(TendrilAnchor.forLevel(world), world.getGameTime())) {
            var connection = new Connection(flow);
            connection.update(world);
            FLOWS.add(connection);
        }
    }

    static void mist(Vec3 at, Vec3 velocity, boolean umbra, float size, int life) {
        if (!ready() || !Double.isFinite(at.lengthSqr()) || !Double.isFinite(velocity.lengthSqr())) return;
        if (MISTS.size() >= 128) MISTS.removeFirst();
        MISTS.add(new Mist(at, velocity, umbra, size, world.getGameTime(), life));
    }

    static void tick() {
        if (!ready()) return;
        long now = world.getGameTime();
        MISTS.removeIf(m -> now - m.born >= m.life);
        FLOWS.removeIf(c -> {
            if (c.flow.finished(now)) return true;
            if (c.flow.resolve(TendrilAnchor.forLevel(world), now)) c.update(world);
            else c.previous = c.points;
            return false;
        });
    }

    static void render(PoseStack p, LuxUmbraBatch batch, Vec3 camera, Vec3 right, Vec3 up,
            double time, float partial) {
        if (world == null) return;
        for (Connection c : FLOWS) {
            var flow = c.flow;
            if (flow.start == null || Math.min(flow.start.distanceToSqr(camera), flow.end.distanceToSqr(camera)) > 128 * 128) continue;
            boolean dark = flow.packet.umbra();
            var v = batch.vertices(dark);
            float alpha = flow.opacity(time);
            int seed = (int) flow.packet.config().seed();
            p.pushPose();
            p.translate(-camera.x, -camera.y, -camera.z);
            if (flow.start.distanceToSqr(flow.end) < .02) {
                LuxUmbraGeometry.card(p, v, flow.end, right, up, .35, .45, LuxUmbraGeometry.FOCUS,
                        seed, LuxUmbraGeometry.WHITE, alpha);
            } else {
                List<Vec3> points = new ArrayList<>(c.points.size());
                for (int i = 0; i < c.points.size(); i++)
                    points.add(c.previous.get(i).lerp(c.points.get(i), partial));
                double width = Math.min(.23, flow.packet.config().baseWidth() * (dark ? 2.1 : 2));
                LuxUmbraGeometry.ribbonPath(p, v, points, camera, width, LuxUmbraGeometry.RIBBON,
                        seed, dark ? 0xFFFFFF : LuxUmbraGeometry.WHITE, alpha);
            }
            p.popPose();
        }
        var setting = Minecraft.getInstance().options.particles().get();
        if (setting == net.minecraft.client.ParticleStatus.MINIMAL) return;
        for (int i = 0; i < MISTS.size(); i++) {
            if (setting == net.minecraft.client.ParticleStatus.DECREASED && i % 2 != 0) continue;
            Mist mist = MISTS.get(i);
            if (mist.at.distanceToSqr(camera) > 48 * 48) continue;
            float age = (float) (time - mist.born), life = age / mist.life;
            Vec3 at = mist.at.add(mist.velocity.scale(age));
            var packet = new ManipulationVisualPacket(mist.umbra ? Form.UMBRA_MIST : Form.LUX_MIST,
                    -1, at, at, mist.size, mist.life, 1);
            p.pushPose();
            p.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
            LuxUmbraGeometry.draw(packet, p, batch.vertices(mist.umbra),
                    camera.subtract(at), right, up, time, age, 1, (float) Math.sin(Math.PI * net.minecraft.util.Mth.clamp(life, 0, 1)));
            p.popPose();
        }
    }

    private record Mist(Vec3 at, Vec3 velocity, boolean umbra, float size, long born, int life) {}

    private static final class Connection {
        final LuxUmbraFlow flow;
        List<Vec3> previous = List.of(), points = List.of();
        Connection(LuxUmbraFlow flow) { this.flow = flow; }

        void update(ClientLevel level) {
            var config = flow.packet.config().withShape(12, 1, flow.packet.config().baseWidth(), .2f)
                    .withBranching(0, 0, 0, 0).clamped();
            var geometry = TendrilGeometry.generate(flow.start, flow.end, config, config.seed(),
                    level.getGameTime() - flow.born, surfaceResolver(level, config));
            previous = points;
            points = geometry.strands().isEmpty() ? List.of() : geometry.strands().getFirst().rings().stream()
                    .map(TendrilGeometry.Ring::center).toList();
            if (previous.size() != points.size()) previous = points;
        }
    }

    private static TendrilGeometry.SurfaceResolver surfaceResolver(ClientLevel level, TendrilEffectConfig config) {
        if (config.mode() != TendrilEffectConfig.Mode.SURFACE) return TendrilGeometry.SurfaceResolver.NONE;
        // Surface casts sample once per game tick; interpolation does not repeat world raycasts each frame.
        return (point, tangent, settings) -> {
            Vec3 nearest = null;
            double distance = Double.MAX_VALUE;
            for (Direction direction : Direction.values()) {
                Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
                var hit = level.clip(new ClipContext(point, point.add(normal.scale(settings.surfaceSnapDistance())),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Minecraft.getInstance().cameraEntity));
                if (hit.getType() != HitResult.Type.BLOCK) continue;
                Vec3 at = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(settings.surfaceLift()));
                if (at.distanceToSqr(point) < distance) { nearest = at; distance = at.distanceToSqr(point); }
            }
            return Optional.ofNullable(nearest);
        };
    }
}
