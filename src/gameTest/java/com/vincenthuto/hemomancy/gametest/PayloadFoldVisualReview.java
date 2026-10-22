package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Opt-in screenshot study of the single-purpose VFX payloads, driven through their {@code PacketHandler} helpers so
 * the same scenes run before and after a payload change. Only runs in the disposable PayloadFoldReview save.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PayloadFoldVisualReview {
    private static final String SAVE = "PayloadFoldReview";
    private static final int SCENE_TICKS = 60;
    /** Ticks spent at the scene area first, so the client has its chunks before the first capture. */
    private static final int WARM_UP_TICKS = 100;
    private record Scene(String name, BiConsumer<ServerLevel, LivingEntity> send) {}
    private static final Scene[] SCENES = {
            new Scene("avatar_hit", (level, target) -> PacketHandler.sendAvatarHitParticles(
                    target.position().add(0, 1, 0), ParticleColor.WHITE, 16, level)),
            new Scene("blood_flask", (level, target) -> PacketHandler.sendBloodFlaskParticles(
                    target.position(), ParticleColor.BLOOD, 64, level)),
            new Scene("blood_claw", (level, target) -> PacketHandler.sendClawParticles(
                    target.position().add(0, 1, 0), ParticleColor.BLOOD, 64, level)),
            new Scene("living_tool_break", (level, target) -> PacketHandler.sendLivingToolBreakParticles(
                    target.position(), ParticleColor.BLOOD, 64, level)),
            new Scene("monolith_shatter", (level, target) -> PacketHandler.sendMonolithShatterBurst(
                    target.position().add(0, 1, 0), 64, level)),
            new Scene("monolith_shard_pulse", (level, target) -> PacketHandler.sendMonolithShardPulse(
                    target.position().add(0, 1, 0), 64, level)),
            new Scene("pome_pulse", (level, target) -> PacketHandler.sendPomePulse(
                    target.position().add(0, 1, 0), 64, level)),
            new Scene("monolithic_dislocation", (level, target) -> PacketHandler.sendMonolithicDislocationVisual(target, 60)),
            new Scene("crimson_fire", (level, target) -> PacketHandler.sendCrimsonFireVisual(target, 60))};
    private static int ticks;
    private static volatile int baseX = Integer.MIN_VALUE, baseZ;
    private static volatile int target = -1;

    private PayloadFoldVisualReview() {}

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.payloadFoldReview")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        if (!mc.getSingleplayerServer().getWorldData().getLevelName().equals(SAVE))
            throw new IllegalStateException("Payload fold review requires the disposable " + SAVE + " save");
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        if (mc.screen != null) mc.setScreen(null);
        int tick = ticks++;
        if (tick == 0) mc.getSingleplayerServer().execute(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            if (player == null) return;
            var spawn = player.serverLevel().getSharedSpawnPos();
            baseX = spawn.getX();
            baseZ = spawn.getZ();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(player.serverLevel(), baseX + .5, 200, baseZ - 2.5, 0, 12);
        });
        if (tick < WARM_UP_TICKS || baseX == Integer.MIN_VALUE) return;
        int sceneTick = tick - WARM_UP_TICKS, sceneIndex = sceneTick / SCENE_TICKS, phase = sceneTick % SCENE_TICKS;
        if (sceneIndex >= SCENES.length) {
            org.slf4j.LoggerFactory.getLogger(PayloadFoldVisualReview.class).info("PAYLOAD_FOLD_REVIEW captured {}", SCENES.length * 3);
            mc.stop();
            return;
        }
        Scene scene = SCENES[sceneIndex];
        double x = baseX + sceneIndex * 12 + .5;
        int z0 = baseZ;
        if (phase == 0) {
            mc.options.particles().set(ParticleStatus.ALL);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if (player == null) return;
                var level = player.serverLevel();
                level.setDayTime(6000);
                level.setWeatherParameters(6000, 0, false, false);
                for (int dx = -6; dx <= 6; dx++) for (int dz = -4; dz <= 8; dz++)
                    level.setBlockAndUpdate(new BlockPos((int) x + dx, 199, z0 + dz), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                player.setGameMode(GameType.CREATIVE);
                player.teleportTo(level, x, 200, z0 - 2.5, 0, 12);
                var husk = EntityType.HUSK.create(level);
                husk.setPos(x, 200, z0 + 3);
                husk.setNoAi(true);
                husk.setSilent(true);
                level.addFreshEntity(husk);
                target = husk.getId();
                org.slf4j.LoggerFactory.getLogger(PayloadFoldVisualReview.class).info("PAYLOAD_FOLD_REVIEW scene={}", scene.name());
            });
        }
        if (phase == 20 || phase == 30) mc.getSingleplayerServer().execute(() -> {
            var level = mc.getSingleplayerServer().overworld();
            if (level.getEntity(target) instanceof LivingEntity living) scene.send().accept(level, living);
        });
        if (phase == 23 || phase == 33 || phase == 45) {
            Screenshot.grab(mc.gameDirectory, String.format(Locale.ROOT, "payload-fold-%s-%02d.png", scene.name(), phase),
                    mc.getMainRenderTarget(), message -> {});
        }
        if (phase == SCENE_TICKS - 1) mc.getSingleplayerServer().execute(() -> {
            var level = mc.getSingleplayerServer().overworld();
            var entity = level.getEntity(target);
            if (entity != null) entity.discard();
        });
    }
}
