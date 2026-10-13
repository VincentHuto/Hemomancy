package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.brazierReview, places Iron Braziers in each ritual phase (unlit, lit, sanguine, and an east-facing
 * sanguine one) holding offerings in a disposable world, and captures them by day and night into
 * build/brazier-client-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BrazierClientReview {
    private static final int POURED_SHOT = 9, DRAINED_SHOT = 10;
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    /** Eye offset from the row, the point looked at, then 1 for night; the last pose shows the hotbar. */
    private static final double[][] SHOTS = {
            {0, 2.4, -5.5, .5, .9, .5, 0}, {-3, 1.6, -2.1, -2.5, .75, .5, 0}, {-1, 1.6, -2.1, -.5, .75, .5, 0},
            {1, 1.6, -2.1, 1.5, .75, .5, 0}, {2.4, 2.3, -1.2, 1.5, .8, .5, 0}, {4.6, 1.8, -1.6, 3.5, .8, .5, 0},
            {1, 2.6, -.8, 1.5, .7, .5, 0},
            // Close over the lit and the sanguine bowl, for the blood pool under the flames.
            {-1, 1.75, -.6, -.5, .74, .5, 0}, {1, 1.75, -.6, 1.5, .74, .5, 0},
            // The unlit bowl half poured, then the lit bowl drawn down to a third (POURED_SHOT, DRAINED_SHOT).
            {-3, 1.75, -.6, -2.5, .74, .5, 0}, {-1, 1.75, -.6, -.5, .74, .5, 0},
            {0, 2.4, -5.5, .5, .9, .5, 1}, {-1, 1.6, -2.1, -.5, .75, .5, 1}, {1, 1.6, -2.1, 1.5, .75, .5, 1},
            {0, 2.0, -4.0, .5, .9, .5, 0}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.brazierReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("brazier-client-review")) return;
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
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-7, -1, -7), row.offset(7, 4, 4)))
                    level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                var brazier = BlockInit.iron_brazier.get();
                var offerings = new ItemStack[]{new ItemStack(Items.BONE), new ItemStack(ItemInit.morphling_polyp.get()),
                        new ItemStack(ItemInit.vivacious_enzyme.get()), new ItemStack(Items.ROTTEN_FLESH)};
                Object[][] placements = {{-3, Direction.NORTH, 0}, {-1, Direction.NORTH, 1}, {1, Direction.NORTH, 2},
                        {3, Direction.EAST, 2}};
                for (int i = 0; i < placements.length; i++) {
                    var pos = row.offset((int) placements[i][0], 0, 0);
                    level.setBlock(pos, brazier.defaultBlockState().setValue(BrazierBlock.FACING, (Direction) placements[i][1])
                            .setValue(BrazierBlock.RITUAL_PHASE, (int) placements[i][2]), 3);
                    ((IronBrazierBlockEntity) level.getBlockEntity(pos)).insertOffering(null, offerings[i].copy());
                }
                Hemomancy.LOGGER.info("BRAZIER_REVIEW placed {} braziers at {}", placements.length, row);
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
                    player.getInventory().setItem(0, new ItemStack(BlockInit.iron_brazier.get()));
                    player.getInventory().setItem(1, new ItemStack(BlockInit.mycelial_lantern.get()));
                }
            });
        }
        if (local == 5 && shot == SHOTS.length - 1) mc.player.getInventory().selected = 0;        if (local == 20 && (shot == POURED_SHOT || shot == DRAINED_SHOT)) setBowl(server, shot == POURED_SHOT ? -3 : -1,
                shot == POURED_SHOT ? 25.0D : 17.0D);
        if (local == 40) {
            Screenshot.grab(mc.gameDirectory, "brazier-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("BRAZIER_REVIEW captured {}", shot);
            shot++;
        }
    }

    /** Sets a brazier's bowl as a part-finished pour or drain would leave it, for the shot that follows. */
    private static void setBowl(net.minecraft.server.MinecraftServer server, int offset, double pooled) {
        server.execute(() -> {
            var level = server.overworld();
            if (level.getBlockEntity(row.offset(offset, 0, 0)) instanceof IronBrazierBlockEntity brazier)
                brazier.setPooledBlood(pooled);
        });
    }
}
