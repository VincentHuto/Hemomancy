package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationFlowPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Close joins and real Verdict releases, including the caster's exactly axial view. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LuxUmbraRibbonReview {
    private static final String[] SCENES = {"first-person", "third-person", "side-on", "vertical",
            "day-minimal", "beacon-close", "mending-close", "anchored-joins", "step-first-person", "step-third-person"};
    private static int ticks, frame;
    private LuxUmbraRibbonReview() {}

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.luxUmbraRibbonReview")) return;
        var mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        if (mc.player == null || mc.level == null || server == null) return;
        if (!server.getWorldData().getLevelName().equals("VisceralReview"))
            throw new IllegalStateException("Ribbon review requires the disposable VisceralReview save");
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = false;
        if (mc.screen != null) mc.setScreen(null);
        boolean trailReview = Boolean.getBoolean("hemomancy.luxUmbraVerdictTrailReview");
        int sceneTicks = trailReview ? 128 : 96;
        int tick = ticks++, scene = tick / sceneTicks + (Boolean.getBoolean("hemomancy.luxUmbraStepReview") ? 8 : 0), phase = tick % sceneTicks;
        String label = System.getProperty("hemomancy.luxUmbraReviewLabel", "after");
        if (scene >= (trailReview ? 5 : SCENES.length)) {
            org.slf4j.LoggerFactory.getLogger(LuxUmbraRibbonReview.class)
                    .info("LUX_UMBRA_JOINS COMPLETE label={} frames={}", label, frame);
            mc.stop(); return;
        }
        var id = mc.player.getUUID();
        double x = 4000.5 + scene * 32;
        if (phase == 0) {
            mc.options.particles().set(scene == 4 ? ParticleStatus.MINIMAL : ParticleStatus.ALL);
            mc.options.setCameraType(scene == 1 || scene == 6 || scene == 9 ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
            if (scene == 4) mc.reloadResourcePacks();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(id);
                if (player == null) return;
                var level = player.serverLevel();
                level.setDayTime(scene == 4 ? 6000 : 18000);
                level.setWeatherParameters(6000, 0, false, false);
                for (int dx = -8; dx <= 8; dx++) for (int dz = -6; dz <= 20; dz++)
                    level.setBlockAndUpdate(new BlockPos((int)x + dx, 99, dz),
                            ((dx + dz) % 2 == 0 ? Blocks.STONE : Blocks.POLISHED_DEEPSLATE).defaultBlockState());
                for (int dx = -3; dx <= 3; dx++) for (int y = 100; y <= 104; y++)
                    level.setBlockAndUpdate(new BlockPos((int)x + dx, y, 16), Blocks.STONE.defaultBlockState());
                player.setGameMode(GameType.CREATIVE);
                player.teleportTo(level, x, 100, 0, 0, scene == 3 ? -90 : scene == 6 || scene >= 8 ? 25 : 0);
                org.slf4j.LoggerFactory.getLogger(LuxUmbraRibbonReview.class)
                        .info("LUX_UMBRA_JOINS scene={} name={} label={}", scene, SCENES[scene], label);
            });
        }
        if (scene <= 4 && phase >= 8 && phase <= 68 && phase % 3 == 2) server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player == null) return;
            Vec3 eye = player.getEyePosition();
            float charge = Math.min(1, (phase - 8) / 60f);
            Vec3 end = ManipulationCombatHelper.clipToGeometry(player, eye.add(player.getLookAngle().scale(24)));
            PacketDistributor.sendToPlayer(player, new ManipulationVisualPacket(Form.VERDICT_CHARGE,
                    player.getId(), eye, end, charge, 5, 1));
        });
        if (scene <= 4 && phase == 70) server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player == null) return;
            PacketDistributor.sendToPlayer(player, new ManipulationVisualPacket(Form.VERDICT_CHARGE,
                    player.getId(), player.getEyePosition(), player.getEyePosition(), 1, 0, 1));
            ManipulationInit.white_verdict.get().getAction(player, player.serverLevel(), ItemStack.EMPTY,
                    player.blockPosition(), 60);
            org.slf4j.LoggerFactory.getLogger(LuxUmbraRibbonReview.class)
                    .info("LUX_UMBRA_JOINS FIRED scene={} eye={} look={}", scene, player.getEyePosition(), player.getLookAngle());
            if (scene == 2) player.teleportTo(player.serverLevel(), x + 5, 100, 5, 60, 0);
        });
        if (trailReview && scene == 0 && phase == 82) server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player != null) player.teleportTo(player.serverLevel(), x, 100, -.5, 15, 0);
        });
        if (scene >= 8 && phase == 40) server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player == null) return;
            var manipulation = ManipulationInit.umbral_step.get();
            BloodManipulation.clearSessionState();
            var blood = HemoCapabilityAccess.requireBloodVolume(player);
            blood.setActive(true);
            blood.setBloodVolume(2000);
            HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(EnumBloodTendency.TENEBRIS, 100);
            HemoCapabilityAccess.requireKnownManipulations(player).getKnownManips().put(manipulation, new ManipLevel(4, 185));
            Vec3 origin = player.position();
            boolean cast = manipulation.tryPerformAction(player, player.serverLevel(), ItemStack.EMPTY, player.blockPosition(), 0);
            if (!cast || player.position().distanceToSqr(origin) < 1 || blood.getBloodVolume() >= 2000)
                throw new IllegalStateException("Umbral Step review must teleport and pay through the shared cast path");
            org.slf4j.LoggerFactory.getLogger(LuxUmbraRibbonReview.class)
                    .info("LUX_UMBRA_STEP CAST scene={} origin={} arrival={} blood={}", scene, origin, player.position(), blood.getBloodVolume());
        });
        if (scene >= 5 && scene < 8 && phase == 8) server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player == null) return;
            var level = player.serverLevel();
            if (scene == 5) {
                Vec3 at = new Vec3(x, 100, 2);
                ManipulationVisuals.burst(level, Form.BEACON, at, at, 3, 76);
            } else if (scene == 6) {
                ManipulationVisuals.attached(player, Form.LUX_MENDING, .5, 76, 1);
                HemomancyTendrilEffects.lumenSuture(player, player);
            } else {
                for (int i = 0; i < 2; i++) {
                    Vec3 start = new Vec3(x - .6 + i * 1.2, 100.2, 2);
                    Vec3 end = start.add(.12, 2.6, .25);
                    ManipulationFlowPacket.send(level, i == 1, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end),
                            HemomancyTendrilEffects.voidConfig(8, 41 + i).withLifecycle(3, 68, 10));
                }
            }
        });
        if (phase >= 6 && phase <= sceneTicks - 4 && phase % 2 == 0)
            Screenshot.grab(mc.gameDirectory, String.format(java.util.Locale.ROOT,
                    "lux-joins-%s-frame-%05d.png", label, frame++), mc.getMainRenderTarget(), message -> {});
        if (phase == 60 || phase == 76 || phase == 90 || scene >= 8 && (phase == 46 || phase == 52)
                || trailReview && (phase == 100 || phase == 108 || phase == 114))
            Screenshot.grab(mc.gameDirectory, "lux-joins-" + label + "-" + SCENES[scene] + "-" + phase + ".png",
                    mc.getMainRenderTarget(), message -> {});
    }
}
