package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.player.LivingTorchBreathParticleMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Moving flame tongues emitted by the synchronized torch pose, including remote players. */
public final class LivingTorchFlames {
    private static final int MAX_FLAMES = 1024;
    private static final List<Flame> FLAMES = new ArrayList<>();
    private static ClientLevel world;

    private LivingTorchFlames() {}

    public static void emit(ClientLevel level, LivingEntity caster, Vec3 tip, Vec3 velocity, int tongue) {
        if (world != level) { FLAMES.clear(); world = level; }
        if (FLAMES.size() >= MAX_FLAMES) FLAMES.removeFirst();
        double lifetime = LivingTorchBreathParticleMotion.EFFECTIVE_FLAME_LIFETIME_TICKS;
        Vec3 end = tip.add(velocity.scale(lifetime));
        Vec3 clipped = level.clip(new ClipContext(tip, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, caster)).getLocation();
        lifetime = Math.min(lifetime, tip.distanceTo(clipped) / velocity.length());
        double now = level.getGameTime() + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        FLAMES.add(new Flame(tip, velocity, now, lifetime, caster.getId() * 31 + tongue * 17 + (int)level.getGameTime()));
    }

    static void tick() {
        ClientLevel current = Minecraft.getInstance().level;
        if (world != current) { FLAMES.clear(); world = current; }
        if (world != null) FLAMES.removeIf(flame -> world.getGameTime() - flame.born >= flame.lifetime);
    }

    static void render(PoseStack poses, LuxUmbraBatch batch, Vec3 camera, Vec3 right, Vec3 up, double time) {
        if (world == null) return;
        boolean smoke = Minecraft.getInstance().options.particles().get() != ParticleStatus.MINIMAL;
        VertexConsumer vertices = batch.vertices(LuxUmbraBatch.Material.FLAME);
        for (Flame flame : FLAMES) {
            double age = time - flame.born;
            if (age < 0 || age >= flame.lifetime || flame.origin.distanceToSqr(camera) > 64 * 64) continue;
            Vec3 at = flame.origin.add(flame.velocity.scale(age));
            poses.pushPose();
            poses.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
            float fade = (float)Math.min(1, (flame.lifetime - age) / 5.0);
            drawTongue(poses, vertices, flame.velocity, camera.subtract(at), right, up,
                    (float)age, flame.seed, fade, smoke);
            poses.popPose();
        }
    }

    static void drawTongue(PoseStack poses, VertexConsumer vertices, Vec3 velocity, Vec3 camera,
            Vec3 right, Vec3 up, float age, int seed, float fade, boolean smoke) {
        Vec3 direction = velocity.normalize();
        double progress = Mth.clamp(age / LivingTorchBreathParticleMotion.EFFECTIVE_FLAME_LIFETIME_TICKS, 0, 1);
        double curl = Math.sin(age * .38 + seed * .7) * .09 * progress;
        Vec3 center = up.scale(curl).add(0, progress * progress * .18, 0);
        Vec3 across = LuxUmbraGeometry.facingSide(direction, camera);
        double width = .13 + progress * .58;
        double length = (.35 + Math.sin(progress * Math.PI) * .95)
                * (.85 + .15 * Math.sin(seed * 1.7 + age * .5));
        int ageColor = (int)(progress * 255) << 16 | 0xFFFF;
        // UV height follows outward travel, so the hot root leads into a torn, black tip.
        LuxUmbraGeometry.card(poses, vertices, center, across, direction, width, length,
                3, seed, ageColor, fade * .55F);
        // The shorter rising tongue keeps fire visible when the view runs along the cast axis.
        float axial = (float)Math.pow(Math.abs(direction.dot(camera.normalize())), 4);
        LuxUmbraGeometry.card(poses, vertices, center.add(direction.scale(length * .3)).add(up.scale(length * .4)), right, up,
                width * .55, length * .45, 3, seed + 29, ageColor, fade * axial * .35F);
        if (smoke && progress > .5) {
            ThermalGeometry.smoke(poses, vertices, center.add(direction.scale(length)).add(0, width * .4, 0),
                    right, up, width * .8, length * .8, seed + 51, fade * .24F);
        }
    }

    private record Flame(Vec3 origin, Vec3 velocity, double born, double lifetime, int seed) {}
}
