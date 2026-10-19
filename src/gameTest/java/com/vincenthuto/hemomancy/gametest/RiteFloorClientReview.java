package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

/**
 * With hemomancy.riteFloorReview, builds one greater Cardinal Rite floor of each style in a disposable world and
 * captures them forming, settled, at night, and withering after a block is broken, into
 * build/rite-floor-client-review/screenshots. With hemomancy.riteFloorReviewSpecial it builds the three grand special
 * floors instead, then raises the player to Degree 8 and captures the Liber's Rites &amp; Crafts map.
 * Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RiteFloorClientReview {
    private static final String[] STANDARD = {"threshold_greater", "dominion_greater", "communion_greater", "working_greater"};
    private static final String[] SPECIAL = {"apotheos_grand", "qliphoth_grand", "ancestral_grand"};
    private static final int LIBER_OPEN = 40;
    private static final int LIBER_SHOT = 120;
    private static final int SPACING = 14;
    private static final int BUILD_TICK = 40;
    /** Ticks after the build, then eye offset and look offset from the row centre, then 1 for night. */
    private static final double[][] SHOTS = {
            {44, 0, 11, -17, 0, 0, 0, 0},
            {58, 1.2, 3.2, -0.2, 1.6, 1.0, -2.6, 0},
            {72, 0, 11, -17, 0, 0, 0, 0},
            {170, 0, 11, -17, 0, 0, 0, 0},
            {230, -21, 5.5, -7.5, -21, 0.5, 0, 0},
            {290, -7, 5.5, -7.5, -7, 0.5, 0, 0},
            {350, 7, 5.5, -7.5, 7, 0.5, 0, 0},
            {410, 21, 5.5, -7.5, 21, 0.5, 0, 0},
            {470, -2.5, 2.4, -6.5, -4, 1, -3, 0},
            {500, -18.5, 0.2, -8.5, -21, 0.2, -3.6, 0},
            {515, -17.0, 1.45, -4.3, -24.5, 0.95, -3.5, 0},
            {530, -14.2, 2.4, -0.6, -16.3, 1.0, -3.2, 0},
            {542, -7, 2.8, -1.2, -7, 1, 0, 0},
            {555, -3.4, 1.7, -0.4, -2, 1.0, 2, 0},
            {568, -2.6, 1.7, -0.4, -2, 1.0, 2, 0},
            {579, 25.5, 2.3, 3.2, 25.5, 0.6, 0.0, 0},
            {590, 28.8, 1.3, -2.6, 25.5, 0.7, -0.5, 0},
            {602, 2.5, 2.4, -0.9, 2.5, 1.0, -2.4, 0},
            {614, 24.4, 1.6, -6.6, 23, 1.15, -5, 0},
            {626, 24.7, 1.85, -7.2, 23.9, 0.8, -6.1, 0},
            {638, 25.1, 2.2, -7.1, 24.2, 0.85, -6.0, 0},
            {650, -7, 1.9, -3.0, -7, 0.75, -5.6, 0},
            {662, 21, 5.5, -7.5, 21, 0.5, 0, 1},
    };
    /** Breaks a floor block on the working floor so the next shot shows the membrane withering. */
    private static final int BREAK_TICK = 700;
    /** The server notices the break within a second, so three frames bracket the wither. */
    private static final int[] DISSOLVE_SHOTS = {BREAK_TICK + 12, BREAK_TICK + 22, BREAK_TICK + 32};
    private static int ticks;
    private static int shot;
    private static BlockPos row;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.riteFloorReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("rite-floor-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        ticks++;
        if (ticks == BUILD_TICK) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                row = player.blockPosition().offset(0, 0, 14);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false", "kill @e[type=!player]"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-30, -2, -12), row.offset(30, 8, 12))) {
                    Block block = pos.getY() < row.getY() ? Blocks.STONE
                            : pos.getY() == row.getY() ? Blocks.GRASS_BLOCK : Blocks.AIR;
                    level.setBlock(pos, block.defaultBlockState(), 2);
                }
                // The dominion floor sits on stone, so its trench cuts through stone rather than dirt.
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-14, 0, -9), row.offset(-1, 0, 9))) {
                    level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
                }
                String[] floors = floors();
                for (int i = 0; i < floors.length; i++) {
                    build(level, row.offset(i * SPACING - SPACING * 3 / 2, 0, 0), floors[i], i == floors.length - 1);
                }
                // A two-block pit along the threshold floor's near edge, so its membrane has a ledge to droop over.
                // The grand special floors reach that edge, so they keep their ground.
                for (BlockPos pos : special() ? List.<BlockPos>of()
                        : BlockPos.betweenClosed(row.offset(-26, -2, -9), row.offset(-16, 0, -4))) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                // East of the working floor: a one-block notch cut through the ground toward the floor, and a cliff
                // just past the trench, for drapes and the trench's outer edge to meet.
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(25, -2, 0), row.offset(26, 0, 0))) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(27, -2, -6), row.offset(30, 0, 6))) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                Hemomancy.LOGGER.info("RITE_FLOOR_REVIEW built {} floors at {}", floors.length, row);
            });
        }
        if (row == null || ticks <= BUILD_TICK) return;
        int since = ticks - BUILD_TICK;
        if (since == BREAK_TICK) {
            server.execute(() -> server.getPlayerList().getPlayer(uuid).serverLevel()
                    .setBlock(row.offset(SPACING * 3 / 2 + 1, 0, 1), Blocks.DIRT.defaultBlockState(), 3));
        }
        for (int i = 0; i < DISSOLVE_SHOTS.length; i++) {
            if (since == DISSOLVE_SHOTS[i]) capture(mc, "rite-floor-dissolve-" + i + ".png");
        }
        int afterFloors = since - (DISSOLVE_SHOTS[DISSOLVE_SHOTS.length - 1] + 10);
        if (afterFloors > 0 && !special()) {
            mc.stop();
            return;
        }
        if (afterFloors > 0) {
            reviewLiber(mc, server, uuid, afterFloors);
            return;
        }
        if (shot >= SHOTS.length) return;
        double[] at = SHOTS[shot];
        if (since == (int) at[0] - 12) look(server, uuid, at);
        if (since == (int) at[0]) {
            capture(mc, "rite-floor-" + shot + ".png");
            shot++;
            if (shot < SHOTS.length && SHOTS[shot][0] - 12 <= since) look(server, uuid, SHOTS[shot]);
        }
        if (shot == SHOTS.length && since == BREAK_TICK - 12) look(server, uuid, SHOTS[SHOTS.length - 1]);
    }

    private static boolean special() {
        return Boolean.getBoolean("hemomancy.riteFloorReviewSpecial");
    }

    private static String[] floors() {
        return special() ? SPECIAL : STANDARD;
    }

    /** At Degree 8, opens the Harbinger progress screen on its Rites &amp; Crafts tab and captures the map. */
    private static void reviewLiber(Minecraft mc, net.minecraft.server.MinecraftServer server, java.util.UUID uuid, int tick) {
        if (tick == 1) {
            mc.options.hideGui = false;
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source, "gamemode survival");
                server.getCommands().performPrefixedCommand(source, "hemo degree set 8");
            });
        } else if (tick == LIBER_OPEN) {
            var screen = new com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen();
            mc.setScreen(screen);
            selectRitesTab(screen);
        } else if (tick == LIBER_SHOT) {
            capture(mc, "liber-rites-and-crafts.png");
        } else if (tick > LIBER_SHOT + 10) {
            mc.stop();
        }
    }

    private static void selectRitesTab(Object screen) {
        try {
            Class<?> tabs = Class.forName(
                    "com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen$Tab");
            Object rites = java.util.Arrays.stream(tabs.getEnumConstants())
                    .filter(tab -> tab.toString().equals("RITES")).findFirst().orElseThrow();
            var switchTab = screen.getClass().getDeclaredMethod("switchTab", tabs);
            switchTab.setAccessible(true);
            switchTab.invoke(screen, rites);
        } catch (ReflectiveOperationException exception) {
            Hemomancy.LOGGER.error("RITE_FLOOR_REVIEW could not open the Rites & Crafts tab", exception);
        }
    }

    private static void build(ServerLevel level, BlockPos focus, String floorId, boolean withBraziers) {
        CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(Hemomancy.rloc(floorId)).orElseThrow();
        String[][] layout = floor.pattern().getPatternArray();
        var symbols = floor.pattern().getSymbolList();
        for (int z = 0; z < layout.length; z++) {
            String cells = layout[z][0];
            for (int x = 0; x < cells.length(); x++) {
                Block block = symbols.get(String.valueOf(cells.charAt(x)));
                if (block == null) continue;
                level.setBlock(focus.offset(x - floor.focus().getX(), 0, z - floor.focus().getZ()),
                        block.defaultBlockState(), 3);
            }
        }
        if (withBraziers) {
            var sockets = CardinalRiteFloorGeometry.sockets(floor, focus, Direction.NORTH);
            // Lit and sanguine braziers, so the shader flames show over the trench's blood.
            int[] phases = {1, 2, 1};
            for (int i = 0; i < 3; i++) {
                level.setBlock(sockets.get(i), BlockInit.iron_brazier.get().defaultBlockState()
                        .setValue(com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock.RITUAL_PHASE,
                                phases[i]), 3);
            }
        }
    }

    private static void look(net.minecraft.server.MinecraftServer server, java.util.UUID uuid, double[] at) {
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(uuid);
            var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            server.getCommands().performPrefixedCommand(source, at[7] > 0 ? "time set 18000" : "time set 6000");
            double x = row.getX() + .5 + at[1], y = row.getY() + at[2], z = row.getZ() + .5 + at[3];
            double fx = row.getX() + .5 + at[4], fy = row.getY() + at[5], fz = row.getZ() + .5 + at[6];
            float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
            float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
            player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
        });
    }

    private static void capture(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
        Hemomancy.LOGGER.info("RITE_FLOOR_REVIEW captured {}", name);
    }
}
