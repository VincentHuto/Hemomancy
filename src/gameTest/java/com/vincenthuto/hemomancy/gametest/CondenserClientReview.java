package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineCondenserBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.condenserReview, seats a row of Sanguine Condensers in a disposable world: a plain cauldron, a
 * Sanguine Cauldron mid-condensation, one holding its formation, a full Latticed Condenser and a full Druse
 * Condenser. Captures them by day and night into build/condenser-client-review/screenshots, then stops.
 * Run with condenserReviewClient and drop {"op":"create","name":"CondenserReview"} into operator/inbox after startup.
 * Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CondenserClientReview {
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    /** Eye offset from the row, the point looked at, then 1 for night. */
    private static final double[][] SHOTS = {
            {0, 2.6, -5.0, .5, .6, .5, 0}, {-4, 1.9, -1.4, -3.5, .5, .5, 0}, {-2, 1.9, -1.4, -1.5, .5, .5, 0},
            {0, 1.9, -1.4, .5, .5, .5, 0}, {2, 1.9, -1.4, 2.5, .5, .5, 0}, {4, 1.9, -1.4, 4.5, .5, .5, 0},
            {2, 1.2, -2.2, 2.5, .6, .5, 0}, {4, 1.2, -2.2, 4.5, .6, .5, 0}, {0, 2.6, -5.0, .5, .6, .5, 1},
            {4, 1.9, -1.4, 4.5, .5, .5, 1},
            // Straight down into the half-full Sanguine Cauldron and the full Druse, for the square pool's edges.
            {-2, 2.6, -.15, -1.5, .3, .5, 0}, {4, 2.6, -.15, 4.5, .5, .5, 0}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.condenserReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("condenser-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
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
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-7, -1, -7), row.offset(7, 4, 4)))
                    level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                // x offset, stage, blood, ticks to run (a mid-condensation cauldron stops part-way).
                int[][] seats = {{-2, 0, 300, 120}, {0, 0, 300, 230}, {2, 1, 750, 500}, {4, 2, 1500, 400}};
                level.setBlock(row.offset(-4, 0, 0), Blocks.CAULDRON.defaultBlockState(), 3);
                for (int[] seat : seats) {
                    BlockPos pos = row.offset(seat[0], 0, 0);
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    var condenser = SanguineCondenserBlock.convertCauldron(level, pos, player);
                    if (condenser == null) continue;
                    if (seat[1] > 0) level.setBlock(pos, level.getBlockState(pos).setValue(StationTierProperty.STAGE, seat[1]), 3);
                    condenser.receiveProjectedBlood(player, seat[2]);
                    for (int i = 0; i < seat[3] && !condenser.isRemoved(); i++)
                        SanguineCondenserBlockEntity.serverTick(level, pos, level.getBlockState(pos), condenser);
                    condenser.receiveProjectedBlood(player, seat[2]);
                }
                Hemomancy.LOGGER.info("CONDENSER_REVIEW placed {} condensers at {}", seats.length, row);
            });
        }
        if (ticks < 80 || row == null) return;
        int local = (ticks - 80) % 60;
        if (local == 0) {
            if (shot == SHOTS.length) { mc.stop(); return; }
            double[] at = SHOTS[shot];
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source, at[6] > 0 ? "time set 18000" : "time set 6000");
                double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
                double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
            });
        }
        if (local == 40) {
            Screenshot.grab(mc.gameDirectory, "condenser-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("CONDENSER_REVIEW captured {}", shot);
            shot++;
        }
    }
}
