package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.EnumCorpusState;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity;
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
 * With hemomancy.sarcophagusReview, places four Saint Sarcophagi in a disposable world: north-facing and sealed
 * with two offerings in the bowl, east-facing consecrated and open (responsive), south-facing consecrated and
 * open (awakened), and west-facing sealed and unlit. Captures them from fixed poses into
 * build/sarcophagus-client-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SarcophagusClientReview {
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    /** x offset, facing, offerings, consecrated and open, corpus state. */
    private static final Object[][] PLACEMENTS = {{0, Direction.NORTH, 2, false, EnumCorpusState.DORMANT},
            {5, Direction.EAST, 3, true, EnumCorpusState.RESPONSIVE},
            {10, Direction.SOUTH, 3, true, EnumCorpusState.AWAKENED},
            {15, Direction.WEST, 0, false, EnumCorpusState.DORMANT}};
    /** Eye offset from the north sarcophagus, then the point looked at; the last pose shows the hotbar. */
    private static final double[][] SHOTS = {
            {7.5, 5.5, -8.5, 7.5, .3, .5}, {-2.2, 1.9, -3.6, .5, .55, -.5}, {-2.4, 3.3, 1.8, .5, .9, .5},
            {5.5, 3.6, -2.6, 5.5, .6, .5}, {13.4, 3.2, .5, 10.5, .6, .5}, {11.3, 2.4, -1.1, 10.5, .55, -.4},
            {11.5, 2.0, 2.8, 10.5, .5, 1.6}, {15.5, 1.6, -3.4, 15.5, .6, .5}, {7.5, 2.2, -5.0, 7.5, .8, .5}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.sarcophagusReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("sarcophagus-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = shot < SHOTS.length - 1;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                row = player.blockPosition().offset(0, 0, 8);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-6, -1, -10), row.offset(20, 6, 6)))
                    level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                var block = (SaintSarcophagusBlock) BlockInit.saint_sarcophagus.get();
                for (Object[] placement : PLACEMENTS) {
                    var pos = row.offset((int) placement[0], 0, 0);
                    var state = block.defaultBlockState().setValue(SaintSarcophagusBlock.FACING, (Direction) placement[1]);
                    level.setBlock(pos, state, 3);
                    block.placeFillers(level, pos, state);
                    if (!(level.getBlockEntity(pos) instanceof SaintSarcophagusBlockEntity tomb)) continue;
                    for (int i = 0; i < (int) placement[2]; i++) tomb.addOffering();
                    tomb.setCorpusState((EnumCorpusState) placement[4]);
                    if ((boolean) placement[3]) {
                        tomb.setConsecrated(true);
                        tomb.setOpen(true);
                    }
                }
                Hemomancy.LOGGER.info("SARCOPHAGUS_REVIEW placed {} sarcophagi at {}", PLACEMENTS.length, row);
            });
        }
        if (ticks < 100 || row == null) return;
        int local = (ticks - 100) % 60;
        if (local == 0) {
            if (shot == SHOTS.length) { mc.stop(); return; }
            double[] at = SHOTS[shot];
            boolean hotbar = shot == SHOTS.length - 1;
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
                double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
                if (hotbar) {
                    player.setGameMode(GameType.CREATIVE);
                    player.getInventory().setItem(0, new ItemStack(BlockInit.saint_sarcophagus.get()));
                    player.getInventory().setItem(1, new ItemStack(BlockInit.saint_sarcophagus.get()));
                }
            });
        }
        if (local == 5 && shot == SHOTS.length - 1) mc.player.getInventory().selected = 1;
        if (local == 40) {
            Screenshot.grab(mc.gameDirectory, "sarcophagus-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("SARCOPHAGUS_REVIEW captured {}", shot);
            shot++;
        }
    }
}
