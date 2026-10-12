package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ConsecratedBloodwellBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.bloodwellReview, places Consecrated Bloodwells in a disposable world and feeds their client block
 * entities empty, low, half and full pool values, so the fountain renders against the new basin without a Founding
 * Fane. Captures day and night views into build/bloodwell-client-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BloodwellClientReview {
    private static final double[] FULLNESS = {0.0, 0.18, 0.55, 1.0};
    private static final int[] OFFSETS = {-3, -1, 1, 3};
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    /** Eye offset from the row, the point looked at, then 1 for night; the last pose shows the hotbar. */
    private static final double[][] SHOTS = {
            {0, 2.6, -6.0, .5, 1.0, .5, 0}, {-3, 1.7, -2.2, -2.5, .7, .5, 0}, {-1, 1.9, -2.2, -.5, .8, .5, 0},
            {1, 2.0, -2.4, 1.5, 1.0, .5, 0}, {3, 2.2, -2.8, 3.5, 1.1, .5, 0}, {4.8, 1.2, -1.8, 3.5, .6, .5, 0},
            {3, 3.2, -.9, 3.5, .8, .5, 0}, {-3, .9, -1.6, -2.5, .45, .5, 0},
            {0, 2.6, -6.0, .5, 1.0, .5, 1}, {3, 2.2, -2.8, 3.5, 1.1, .5, 1},
            {0, 2.0, -4.0, .5, .9, .5, 0}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.bloodwellReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("bloodwell-client-review")) return;
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
                for (int offset : OFFSETS)
                    level.setBlock(row.offset(offset, 0, 0), BlockInit.consecrated_bloodwell.get().defaultBlockState(), 3);
                Hemomancy.LOGGER.info("BLOODWELL_REVIEW placed {} bloodwells at {}", OFFSETS.length, row);
            });
        }
        if (row != null && mc.level != null) {
            // The unregistered wells' server ticks hold zero, which they never resend, so the client values stick.
            for (int i = 0; i < OFFSETS.length; i++) {
                if (mc.level.getBlockEntity(row.offset(OFFSETS[i], 0, 0)) instanceof ConsecratedBloodwellBlockEntity well) {
                    var tag = new CompoundTag();
                    tag.putDouble("poolBlood", FULLNESS[i] * 10000.0);
                    tag.putDouble("poolMax", 10000.0);
                    well.handleUpdateTag(tag, mc.level.registryAccess());
                }
            }
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
                    player.getInventory().setItem(0, new ItemStack(BlockInit.consecrated_bloodwell.get()));
                    player.getInventory().setItem(1, new ItemStack(BlockInit.iron_brazier.get()));
                }
            });
        }
        if (local == 5 && shot == SHOTS.length - 1) mc.player.getInventory().selected = 0;
        if (local == 40) {
            Screenshot.grab(mc.gameDirectory, "bloodwell-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("BLOODWELL_REVIEW captured {}", shot);
            shot++;
        }
    }
}
