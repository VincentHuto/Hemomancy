package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Opt-in moving material study. Only runs in the disposable review save. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LuxUmbraVisualReview {
    private static final Form[] FORMS = {Form.VERDICT_CHARGE, Form.VERDICT, Form.BEACON, Form.EYE,
            Form.FLARE, Form.LUX_MENDING, Form.WELL_CHARGE, Form.WELL, Form.VEIL, Form.TELEPORT,
            Form.UMBRA_SLASH, Form.SUTURE, Form.UMBRA_ARRIVAL};
    private static int ticks, frame;
    private static volatile int source = -1, target = -1;
    private static boolean reloaded;
    private static long lastFrame;
    private static final java.util.List<Double> activeFrames = new java.util.ArrayList<>();
    private static final java.util.List<Double> idleFrames = new java.util.ArrayList<>();

    @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
        if (!Boolean.getBoolean("hemomancy.luxUmbraReview") || event.getStage() != net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        long now = System.nanoTime();
        int phase = ticks % 80;
        if (lastFrame != 0 && ticks > 80) {
            double ms = (now - lastFrame) / 1_000_000.0;
            if (phase >= 16 && phase <= 40) activeFrames.add(ms);
            if (phase >= 62 && phase <= 76) idleFrames.add(ms);
        }
        lastFrame = now;
    }

    private static String frameStats(java.util.List<Double> samples) {
        if (samples.isEmpty()) return "none";
        var sorted = samples.stream().sorted().toList();
        return String.format(java.util.Locale.ROOT, "n=%d mean=%.2fms p95=%.2fms", sorted.size(),
                sorted.stream().mapToDouble(Double::doubleValue).average().orElse(0), sorted.get((int)(sorted.size() * .95)));
    }

    private LuxUmbraVisualReview() {}

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.luxUmbraReview")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        if (!mc.getSingleplayerServer().getWorldData().getLevelName().equals("VisceralReview"))
            throw new IllegalStateException("Lux/Umbra review requires the disposable VisceralReview save");
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        if (mc.screen != null) mc.setScreen(null);
        int tick = ticks++, scene = tick / 80, phase = tick % 80;
        if (scene >= FORMS.length * 3) {
            org.slf4j.LoggerFactory.getLogger(LuxUmbraVisualReview.class).info("LUX_UMBRA_REVIEW COMPLETE frames={} fps={}", frame, mc.getFps());
            org.slf4j.LoggerFactory.getLogger(LuxUmbraVisualReview.class).info("LUX_UMBRA_REVIEW FRAME_INTERVALS active=[{}] idle=[{}] screenshots_included=true", frameStats(activeFrames), frameStats(idleFrames));
            mc.stop(); return;
        }
        if (phase == 0 && scene == FORMS.length && !reloaded) {
            reloaded = true;
            mc.reloadResourcePacks().thenRun(() -> org.slf4j.LoggerFactory.getLogger(LuxUmbraVisualReview.class)
                    .info("LUX_UMBRA_REVIEW RESOURCE_RELOAD_COMPLETE"));
        }
        int mode = scene / FORMS.length;
        Form form = FORMS[scene % FORMS.length];
        double x = 2000 + scene * 32 + .5;
        if (phase == 0) {
            mc.options.particles().set(mode == 2 ? ParticleStatus.MINIMAL : ParticleStatus.ALL);
            mc.options.setCameraType(mode == 2 && form != Form.WELL ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if (player == null) return;
                var level = player.serverLevel();
                ManipulationReactiveEvents.clearSessionState();
                level.setDayTime(mode == 1 ? 18000 : 6000);
                level.setWeatherParameters(6000, 0, false, false);
                for (int dx = -10; dx <= 10; dx++) for (int dz = -8; dz <= 15; dz++) {
                    level.setBlockAndUpdate(new BlockPos((int)x + dx, 99, dz),
                            ((dx + dz) % 2 == 0 ? Blocks.STONE : Blocks.POLISHED_DEEPSLATE).defaultBlockState());
                }
                for (int y = 100; y <= 102; y++) {
                    level.setBlockAndUpdate(new BlockPos((int)x + 1, y, 6), Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(new BlockPos((int)x - 3, y, 8), Blocks.GLASS.defaultBlockState());
                }
                player.setGameMode(GameType.CREATIVE);
                player.teleportTo(level, x + 4.8, 100, -.8, 38, 12);
                var anchor = EntityType.ARMOR_STAND.create(level);
                anchor.setPos(x, form == Form.VERDICT || form == Form.FLARE || form == Form.SUTURE
                        || form == Form.UMBRA_SLASH ? 101.35 : 100, 4); anchor.setNoGravity(true); anchor.setInvisible(true);
                level.addFreshEntity(anchor); source = anchor.getId();
                var victim = EntityType.HUSK.create(level);
                victim.setPos(x, 100, 8); victim.setNoAi(true);
                level.addFreshEntity(victim); target = victim.getId();
                org.slf4j.LoggerFactory.getLogger(LuxUmbraVisualReview.class).info("LUX_UMBRA_REVIEW scene={} form={} mode={}", scene, form, mode);
            });
        }
        if (phase > 8 && phase < 68) mc.getSingleplayerServer().execute(() -> {
            var level = mc.getSingleplayerServer().overworld();
            var victim = level.getEntity(target);
            if (victim != null) victim.setPos(x + Math.sin(phase * .08) * .5, 100, 8);
            // Sweep around the Eye and look through the well from a low angle in the third mode.
            var player = level.getServer().getPlayerList().getPlayer(mc.player.getUUID());
            if (player != null && mode == 2) {
                float yaw = 38 + (phase - 8) * .24f;
                player.teleportTo(level, x + 4.8 - (phase - 8) * .045, 100, -.8 + (phase - 8) * .027, yaw, 9);
            }
        });
        if (phase == 9 || phase == 28 || phase == 45) mc.getSingleplayerServer().execute(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            if (player == null) return;
            var level = player.serverLevel();
            var anchor = level.getEntity(source);
            if (anchor == null) return;
            Vec3 at = form.name().endsWith("_CHARGE") ? anchor.getEyePosition() : anchor.position();
            Vec3 end = form == Form.VERDICT && mode == 1 ? at.add(0, 7, 0) : at.add(0, 0, 5);
            float radius = form == Form.WELL || form == Form.BEACON ? 3 : form.name().endsWith("_CHARGE") ? .8f : 1;
            int duration = phase == 45 ? 0 : phase == 28 ? 17 : 36;
            PacketDistributor.sendToPlayer(player, new ManipulationVisualPacket(form, source, at, end, radius, duration, 1));
            if (form == Form.SUTURE && phase == 9) {
                var victim = level.getEntity(target);
                if (victim instanceof net.minecraft.world.entity.LivingEntity living) {
                    HemomancyTendrilEffects.luxRelease(player, living.getEyePosition());
                    com.vincenthuto.hemomancy.common.network.particle.ManipulationFlowPacket.send(level, true,
                            new com.vincenthuto.hutoslib.common.tendril.TendrilAnchor.Entity(player.getId(), com.vincenthuto.hutoslib.common.tendril.TendrilAnchor.AnchorPoint.CENTER, Vec3.ZERO),
                            new com.vincenthuto.hutoslib.common.tendril.TendrilAnchor.Entity(living.getId(), com.vincenthuto.hutoslib.common.tendril.TendrilAnchor.AnchorPoint.CENTER, Vec3.ZERO),
                            HemomancyTendrilEffects.voidConfig(15, 71).withLifecycle(3, 40, 10));
                }
                HemomancyTendrilEffects.lumenSuture(player, player);
                HemomancyTendrilEffects.blackVeil(level, player, new BlockPos((int)x, 100, 4), 3);
                if (mode == 1) com.vincenthuto.hemomancy.common.network.PacketHandler.sendBlackVeil(new Vec3(x, 100.5, 4), 3, level, 36);
            }
            if (mode == 2 && phase == 9) {
                PacketDistributor.sendToPlayer(player, new ManipulationVisualPacket(Form.WELL, -1,
                        new Vec3(x, 100, 4), new Vec3(x, 100, 4), 3, 35, 1));
            }
            if (form == Form.SUTURE && phase == 28) {
                var victim = level.getEntity(target);
                if (victim != null) victim.discard();
            }
        });
        if (phase >= 6 && phase % 4 == 0) {
            Screenshot.grab(mc.gameDirectory, String.format(java.util.Locale.ROOT, "lux-umbra-frame-%05d.png", frame++),
                    mc.getMainRenderTarget(), message -> {});
        }
        if (phase == 20 || phase == 38 || phase == 52) {
            Screenshot.grab(mc.gameDirectory, "lux-umbra-" + mode + "-" + form.name().toLowerCase(java.util.Locale.ROOT)
                    + "-" + phase + ".png", mc.getMainRenderTarget(), message -> {});
        }
    }
}
