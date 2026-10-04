package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.VialCentrifugeScreen;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Captures the three centrifuge menus in a disposable client; excluded from release jars. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CentrifugeClientReview {
    private static int ticks;
    private static int stage;
    private static boolean opening;
    private static BlockPos reviewPos;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.centrifugeReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("centrifuge-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (++ticks < 60) return;
        if (!opening) {
            opening = true;
            reviewPos = mc.player.blockPosition().offset(0, 0, 2);
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var level = player.serverLevel();
                level.setBlockAndUpdate(reviewPos, BlockInit.vial_centrifuge.get().defaultBlockState()
                        .setValue(StationTierProperty.STAGE, stage));
            });
            ticks = 0;
            return;
        }
        if (!(mc.screen instanceof VialCentrifugeScreen)) {
            if (mc.level.getBlockEntity(reviewPos) instanceof VialCentrifugeBlockEntity) {
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    player.openMenu((VialCentrifugeBlockEntity) player.serverLevel().getBlockEntity(reviewPos), reviewPos);
                });
                ticks = 0;
            }
            return;
        }
        Screenshot.grab(mc.gameDirectory, "centrifuge-stage-" + stage + ".png", mc.getMainRenderTarget(), message -> {});
        Hemomancy.LOGGER.info("CENTRIFUGE_CLIENT_REVIEW captured stage {}, menu slots {}", stage,
                mc.player.containerMenu.slots.size());
        if (++stage == 3) { mc.stop(); return; }
        mc.player.closeContainer();
        opening = false;
        ticks = 0;
    }
}
