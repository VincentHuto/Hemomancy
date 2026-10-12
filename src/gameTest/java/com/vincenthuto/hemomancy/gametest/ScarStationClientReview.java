package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ScarStationBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.scarStationReview, places fully stocked Cerebral Scarring Stations on all four facings (plus an
 * empty north-facing one) in a disposable world, and captures them by day and night into
 * build/scar-station-client-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ScarStationClientReview {
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    /** Eye offset from the row, the point looked at, then 1 for night; the last pose shows the hotbar. */
    private static final double[][] SHOTS = {
            {0, 2.6, -6.5, .5, 1.2, .5, 0}, {-4, 1.9, -1.9, -3.5, 1.0, .5, 0}, {-4, 2.7, -1.0, -3.5, .85, .6, 0},
            {-1, 2.0, -1.5, -1.5, 1.0, .5, 0}, {1, 2.0, -1.5, 2.5, 1.0, .5, 0}, {0, 2.0, 2.9, .5, 1.0, .5, 0},
            {4, 1.9, -1.9, 4.5, 1.0, .5, 0},
            {0, 2.6, -6.5, .5, 1.2, .5, 1}, {-4, 1.9, -1.9, -3.5, 1.0, .5, 1},
            {0, 2.2, -4.5, .5, 1.2, .5, 0}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.scarStationReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("scar-station-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = shot < SHOTS.length - 1;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                row = player.blockPosition().offset(0, 0, 6);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-8, -1, -8), row.offset(8, 5, 5)))
                    level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                var station = BlockInit.scar_station.get();
                // x offset, facing, stocked
                Object[][] placements = {{-4, Direction.NORTH, true}, {-2, Direction.EAST, true}, {0, Direction.SOUTH, true},
                        {2, Direction.WEST, true}, {4, Direction.NORTH, false}};
                for (Object[] placement : placements) {
                    var pos = row.offset((int) placement[0], 0, 0);
                    var state = station.defaultBlockState().setValue(ScarStationBlock.FACING, (Direction) placement[1]);
                    level.setBlock(pos, state, 3);
                    station.setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
                    if (!(boolean) placement[2]) continue;
                    var te = (ScarStationBlockEntity) level.getBlockEntity(pos);
                    te.setItem(0, new ItemStack(ItemInit.scar_blank.get()));
                    te.setItem(1, new ItemStack(ItemInit.vivacious_enzyme.get()));
                    te.setItem(2, new ItemStack(ItemInit.scar_heart.get()));
                    te.setItem(3, new ItemStack(ItemInit.hematic_iron_knapper.get()));
                    te.setItem(4, new ItemStack(ItemInit.scar_pattern.get()));
                    te.sendUpdates();
                }
                Hemomancy.LOGGER.info("SCAR_STATION_REVIEW placed {} stations at {}", placements.length, row);
            });
        }
        if (ticks < 80 || row == null) return;
        int local = (ticks - 80) % 60;
        if (local == 0) {
            if (shot == SHOTS.length) { mc.stop(); return; }
            double[] at = SHOTS[shot];
            boolean hotbar = shot == SHOTS.length - 1;
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source, at[6] > 0 ? "time set 18000" : "time set 6000");
                double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
                double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
                if (hotbar) {
                    player.setGameMode(GameType.CREATIVE);
                    player.getInventory().setItem(0, new ItemStack(BlockInit.scar_station.get()));
                    player.getInventory().setItem(1, new ItemStack(BlockInit.morphling_incubator.get()));
                }
            });
        }
        if (local == 5 && shot == SHOTS.length - 1) mc.player.getInventory().selected = 0;
        if (local == 40) {
            Screenshot.grab(mc.gameDirectory, "scar-station-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("SCAR_STATION_REVIEW captured {}", shot);
            shot++;
        }
    }
}
