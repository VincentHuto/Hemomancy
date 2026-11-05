package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.LidlessPassageClientState;
import com.vincenthuto.hemomancy.common.block.shared.LidlessDoorBlock;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opt-in native rendering and input check for the Lidless Door, restricted to the disposable lidless-client world:
 * the door from outside at two angles, the crossing ahead at a third and two thirds, the look back, and the exit.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LidlessDoorClientReview {
    private static int ticks;
    private static boolean setupQueued;
    private static volatile boolean prepared;

    @SubscribeEvent
    public static void input(ClientTickEvent.Pre event) {
        if (!Boolean.getBoolean("hemomancy.lidlessReview") || !prepared) return;
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyUp.setDown((ticks >= 50 && ticks < 92) || (ticks >= 96 && ticks < 118) || (ticks >= 132 && ticks < 172));
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.lidlessReview")) return;
        Minecraft mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("lidless-client")) return;
        if (mc.screen instanceof AccessibilityOnboardingScreen) {
            // A fresh game directory opens on the accessibility onboarding; the review never answers it.
            mc.options.onboardAccessibility = false;
            mc.setScreen(new TitleScreen());
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof PauseScreen) mc.setScreen(null);
        if (!setupQueued) {
            setupQueued = true;
            var playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> setup(mc.getSingleplayerServer().getPlayerList().getPlayer(playerId)));
            return;
        }
        if (!prepared) return;
        int tick = ticks++;
        if (tick == 30) capture(mc, "lidless-outside-front");
        if (tick == 32) teleport(mc, 3.5, 102, 3.5, 135, 8);
        if (tick == 40) capture(mc, "lidless-outside-angle");
        if (tick == 42) teleport(mc, 0.5, 102, 4.5, 180, 0);
        if (tick == 94) capture(mc, "lidless-ahead-third");
        if (tick == 120) capture(mc, "lidless-ahead-two-thirds");
        if (tick == 122) mc.player.setYRot(0);
        if (tick == 128) capture(mc, "lidless-looking-back");
        if (tick == 130) mc.player.setYRot(180);
        if (tick == 176) capture(mc, "lidless-exited");
        if (tick == 192) mc.stop();
    }

    private static void teleport(Minecraft mc, double x, double y, double z, float yaw, float pitch) {
        var playerId = mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> {
            ServerPlayer p = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
            p.teleportTo(p.serverLevel(), x, y, z, yaw, pitch);
        });
    }

    private static void capture(Minecraft mc, String name) {
        Hemomancy.LOGGER.info("LIDLESS_REVIEW {} crossing={} progress={} yaw={} position={}", name,
                LidlessPassageClientState.local() != null, LidlessPassageClientState.progress(1.0F),
                mc.player.getYRot(), mc.player.position());
        var playerId = mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> {
            ServerPlayer p = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
            Hemomancy.LOGGER.info("LIDLESS_REVIEW server {} crossing={} progress={} noPhysics={} noGravity={} position={}",
                    name, LidlessPassageService.isCrossing(p), LidlessPassageService.progress(p), p.noPhysics,
                    p.isNoGravity(), p.position());
        });
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(),
                message -> Hemomancy.LOGGER.info("LIDLESS_REVIEW {}", message.getString()));
    }

    private static void setup(ServerPlayer p) {
        var level = p.serverLevel();
        for (BlockPos b : BlockPos.betweenClosed(new BlockPos(-6, 100, -6), new BlockPos(6, 101, 6)))
            level.setBlock(b, Blocks.STONE.defaultBlockState(), 3);
        for (BlockPos b : BlockPos.betweenClosed(new BlockPos(-6, 102, -6), new BlockPos(6, 106, 6)))
            level.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
        BlockPos lower = new BlockPos(0, 102, 0);
        BlockState door = BlockInit.lidless_door.get().defaultBlockState().setValue(LidlessDoorBlock.FACING, Direction.NORTH);
        level.setBlock(lower, door, 3);
        LidlessDoorBlock.placeUpper(level, lower, door);
        p.setGameMode(GameType.SURVIVAL);
        p.teleportTo(level, 0.5, 102, 4.5, 180, 0);
        Hemomancy.LOGGER.info("LIDLESS_REVIEW setup");
        prepared = true;
    }
}
