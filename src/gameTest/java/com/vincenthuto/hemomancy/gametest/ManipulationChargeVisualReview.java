package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

/** Exercises actual input and networking with saved selections, rather than authored preview packets. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ManipulationChargeVisualReview {
    private static final String[] SPELLS = {"white_verdict", "vitric_combustion", "crimson_coronation"};
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ManipulationChargeVisualReview.class);
    private static int ticks, frame;
    private ManipulationChargeVisualReview() {}

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.chargeVisualReview")) return;
        var mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        if (mc.player == null || mc.level == null || server == null) return;
        if (!server.getWorldData().getLevelName().equals("VisceralReview"))
            throw new IllegalStateException("Charge review requires the disposable VisceralReview save");
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = false;
        if (mc.screen != null) mc.setScreen(null);
        int tick = ticks++, scene = tick / 140, phase = tick % 140;
        String label = System.getProperty("hemomancy.chargeVisualReviewLabel", "after");
        if (scene >= 6) {
            ClientEvents.useManip.setDown(false);
            LOGGER.info("CHARGE_REVIEW COMPLETE label={} frames={}", label, frame);
            mc.stop();
            return;
        }
        var manipulation = ManipulationInit.getByName(SPELLS[scene % 3]);
        int duration = manipulation.getRequiredChargeTicks();
        int held = scene < 3 ? duration : duration * 2 / 3;
        ClientEvents.useManip.setDown(phase >= 16 && phase < 16 + held);
        if (phase == 40) LOGGER.info("CHARGE_REVIEW INPUT scene={} selected={} charging={} held={} down={}", scene,
                HemoCapabilityAccess.requireKnownManipulations(mc.player).getSelectedManip().getName(),
                ClientEvents.getChargingManipulationName(), ClientEvents.getManipulationChargeTicks(), ClientEvents.useManip.isDown());
        var id = mc.player.getUUID();
        double x = 5000.5 + scene * 32;
        if (phase == 0) {
            mc.options.particles().set(ParticleStatus.ALL);
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(id);
                if (player == null) return;
                var level = player.serverLevel();
                level.setDayTime(18000);
                level.setWeatherParameters(6000, 0, false, false);
                for (int dx = -8; dx <= 8; dx++) for (int dz = -6; dz <= 20; dz++)
                    level.setBlockAndUpdate(new BlockPos((int)x + dx, 99, dz), Blocks.STONE.defaultBlockState());
                for (int dx = -3; dx <= 3; dx++) for (int y = 100; y <= 104; y++)
                    level.setBlockAndUpdate(new BlockPos((int)x + dx, y, 16), Blocks.STONE.defaultBlockState());
                player.setGameMode(GameType.CREATIVE);
                player.teleportTo(level, x, 100, 0, 0, 5);
                BloodManipulation.clearSessionState();
                var blood = HemoCapabilityAccess.requireBloodVolume(player);
                blood.setActive(true);
                blood.setBloodVolume(2000);
                for (var school : EnumBloodTendency.values())
                    HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(school, 100);
                var known = HemoCapabilityAccess.requireKnownManipulations(player);
                for (var entry : ManipulationInit.MANIPS.getEntries())
                    known.getKnownManips().put(entry.get(), new ManipLevel(4, 185));
                known.setSelectedManip(BloodManipulation.deserialize(manipulation.serialize()));
                known.setEquippedManipNames(List.of(manipulation.getName()));
                PacketDistributor.sendToPlayer(player, new KnownManipulationServerPacket(known), new BloodVolumeServerPacket(blood));
                LOGGER.info("CHARGE_REVIEW scene={} spell={} duration={} held={} savedDuration={}", scene,
                        manipulation.getName(), duration, held, known.getSelectedManip().getRequiredChargeTicks());
                if (scene == 0) {
                    try {
                        int count = ManipulationChargeVisualGameTests.verify(level, new Vec3(x + 1000, 100, 0));
                        LOGGER.info("CHARGE_PREVIEW_REGRESSION PASS count={}", count);
                    } catch (AssertionError failure) {
                        LOGGER.error("CHARGE_PREVIEW_REGRESSION FAIL", failure);
                        if (!label.equals("before")) throw failure;
                    }
                }
            });
        }
        if (phase == 16 + held + 12) server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player == null) return;
            double blood = HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
            if (blood >= 2000) throw new AssertionError("Charge review did not cast " + manipulation.getName());
            LOGGER.info("CHARGE_REVIEW CAST scene={} spell={} blood={}", scene, manipulation.getName(), blood);
        });
        if (phase >= 8 && phase <= 136 && phase % 2 == 0)
            Screenshot.grab(mc.gameDirectory, String.format(Locale.ROOT, "charge-%s-frame-%05d.png", label, frame++),
                    mc.getMainRenderTarget(), message -> {});
        if (phase == 16 + held - 6 || phase == 16 + held + 6)
            Screenshot.grab(mc.gameDirectory, "charge-" + label + "-" + scene + "-" + phase + ".png",
                    mc.getMainRenderTarget(), message -> {});
    }
}
