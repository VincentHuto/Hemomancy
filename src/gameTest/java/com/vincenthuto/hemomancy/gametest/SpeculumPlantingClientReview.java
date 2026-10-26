package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Captures the shared planting animation from both cameras in a disposable review world. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SpeculumPlantingClientReview {
    private static int ticks;
    private static volatile boolean ready;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.speculumPlantingReview")) return;
        var client = Minecraft.getInstance();
        if (!client.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("speculum-planting-review")) return;
        if (client.player == null || client.level == null || client.getSingleplayerServer() == null) return;
        client.options.pauseOnLostFocus = false;
        client.options.bobView().set(false);
        client.options.hideGui = false;
        if (client.screen != null) client.setScreen(null);
        if (++ticks == 20) {
            var server = client.getSingleplayerServer();
            var uuid = client.player.getUUID();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                var commands = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String command : new String[]{"time set 6000", "gamerule doDaylightCycle false",
                        "gamerule doMobSpawning false", "weather clear"})
                    server.getCommands().performPrefixedCommand(commands, command);
                for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(-8, 119, -8), new BlockPos(8, 124, 10)))
                    level.setBlock(pos, pos.getY() == 119 ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                player.setGameMode(GameType.CREATIVE);
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.terrestrial_speculum.get()));
                player.teleportTo(level, .5, 120, .5, 0, 0);
                ready = true;
            });
        }
        if (ticks < 80 || !ready) return;
        int elapsed = ticks - 80;
        int view = elapsed / 30;
        if (view == 2) {
            Hemomancy.LOGGER.info("SPECULUM_PLANTING_REVIEW completed both camera views");
            client.stop();
            return;
        }
        int frame = elapsed % 30;
        client.options.setCameraType(view == 0 ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_FRONT);
        client.player.setXRot(0);
        client.player.xRotO = 0;
        if (frame == 0) CardinalRiteStaffPlantingClientState.start(client.player.getId(),
                client.player.blockPosition().south(), new ItemStack(ItemInit.terrestrial_speculum.get()));
        if (frame == 3 || frame == 8 || frame == 13 || frame == 19) {
            String name = "planting-" + System.getProperty("hemomancy.speculumPlantingReviewLabel", "after")
                    + (view == 0 ? "-first-" : "-third-") + frame + ".png";
            Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("SPECULUM_PLANTING_REVIEW captured {}", name);
        }
    }
}
