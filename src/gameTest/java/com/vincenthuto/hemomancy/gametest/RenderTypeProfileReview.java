package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * With hemomancy.renderTypeProfile, builds a fixed scene of renderers that ask for shader render types every frame
 * (a field of Hematic Stakes and a hotbar of monolith items), holds the camera still, and measures a window of frames:
 * mean and 95th-percentile frame time, a Spark CPU profile ({@code sparkc}, when Spark is in the run's mods folder) and a
 * JFR allocation recording, after a screenshot of the scene. Results land in the game directory's
 * {@code render-type-profile} folder, labelled by hemomancy.renderTypeProfileLabel. Only runs in the disposable
 * RenderTypeProfile save. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RenderTypeProfileReview {
    private static final String SAVE = "RenderTypeProfile";
    private static final int WARM_UP_TICKS = 200;
    private static final int WINDOW_TICKS = 600;
    private static final List<Supplier<? extends net.minecraft.world.level.ItemLike>> HOTBAR = List.of(
            ItemInit.monolith_fragment, ItemInit.monolith_imbued_cloth, ItemInit.memory_of_vesper,
            ItemInit.monolithic_cornerstone, BlockInit.hematic_stake, BlockInit.sanguine_monolith,
            BlockInit.covenant_throne, ItemInit.monolith_fragment, BlockInit.hematic_stake);

    private static int ticks;
    private static boolean measuring;
    private static long lastFrame;
    private static final long[] FRAMES = new long[200_000];
    private static int frames;
    private static Recording recording;

    private RenderTypeProfileReview() {}

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Post event) {
        if (!measuring) return;
        long now = System.nanoTime();
        if (lastFrame != 0 && frames < FRAMES.length) FRAMES[frames++] = now - lastFrame;
        lastFrame = now;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.renderTypeProfile")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        if (!mc.getSingleplayerServer().getWorldData().getLevelName().equals(SAVE))
            throw new IllegalStateException("Render type profile requires the disposable " + SAVE + " save");
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null) mc.setScreen(null);
        int tick = ticks++;
        if (tick == 0) {
            var server = mc.getSingleplayerServer();
            var uuid = mc.player.getUUID();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"time set 6000", "gamerule doDaylightCycle false", "weather clear",
                        "gamerule doMobSpawning false", "effect clear @s", "kill @e[type=!player]"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                player.setGameMode(GameType.CREATIVE);
                BlockPos base = player.blockPosition().above(60);
                for (BlockPos pos : BlockPos.betweenClosed(base.offset(-10, -1, 0), base.offset(10, 4, 24)))
                    level.setBlock(pos, pos.getY() < base.getY() ? Blocks.SMOOTH_STONE.defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                for (int x = -8; x <= 8; x += 2)
                    for (int z = 4; z <= 20; z += 2)
                        level.setBlock(base.offset(x, 0, z), BlockInit.hematic_stake.get().defaultBlockState(), 3);
                for (int slot = 0; slot < HOTBAR.size(); slot++)
                    player.getInventory().setItem(slot, new ItemStack(HOTBAR.get(slot).get()));
                player.getInventory().selected = 0;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                player.teleportTo(level, base.getX() + .5, base.getY() + 1.5, base.getZ() + .5, 0, 20);
            });
        }
        if (tick == WARM_UP_TICKS - 20) {
            // The held monolith fragment, the hotbar and the stake field, for before and after comparisons.
            String label = System.getProperty("hemomancy.renderTypeProfileLabel", "after");
            Screenshot.grab(mc.gameDirectory, "profile-scene-" + label + ".png", mc.getMainRenderTarget(), message -> {});
        }
        if (tick == WARM_UP_TICKS) {
            measuring = true;
            ClientCommandHandler.runCommand("sparkc profiler start --timeout " + WINDOW_TICKS / 20 + " --save-to-file");
            try {
                recording = new Recording(Configuration.getConfiguration("profile"));
                recording.start();
            } catch (Exception | LinkageError e) {
                Hemomancy.LOGGER.warn("RENDER_TYPE_PROFILE JFR unavailable", e);
            }
        }
        if (tick == WARM_UP_TICKS + WINDOW_TICKS) {
            measuring = false;
            report(mc);
        }
        if (tick == WARM_UP_TICKS + WINDOW_TICKS + 200) mc.stop();
    }

    private static void report(Minecraft mc) {
        String label = System.getProperty("hemomancy.renderTypeProfileLabel", "after");
        Path dir = mc.gameDirectory.toPath().resolve("render-type-profile");
        try {
            Files.createDirectories(dir);
            if (recording != null) {
                recording.stop();
                recording.dump(dir.resolve(label + ".jfr"));
                recording.close();
            }
            long[] sorted = java.util.Arrays.copyOf(FRAMES, frames);
            java.util.Arrays.sort(sorted);
            double mean = java.util.Arrays.stream(sorted).average().orElse(0) / 1e6;
            double p95 = sorted.length == 0 ? 0 : sorted[(int) (sorted.length * .95)] / 1e6;
            String line = String.format(Locale.ROOT, "%s frames=%d meanMs=%.3f p95Ms=%.3f", label, frames, mean, p95);
            Files.writeString(dir.resolve(label + ".txt"), line + System.lineSeparator());
            Hemomancy.LOGGER.info("RENDER_TYPE_PROFILE {}", line);
        } catch (Exception e) {
            Hemomancy.LOGGER.error("RENDER_TYPE_PROFILE report failed", e);
        }
    }
}
