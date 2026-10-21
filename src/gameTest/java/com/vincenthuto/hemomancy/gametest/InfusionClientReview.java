package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import com.vincenthuto.hemomancy.common.event.BloodFeedStyle;
import com.vincenthuto.hemomancy.common.event.BloodInfusionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

/**
 * With hemomancy.infusionReview, lays a row of stone in a disposable world and holds client infusion feeds on it
 * at rising progress so the veins can be judged mid-crawl, then runs one block through the completion linger
 * (strain, glow, cracks, break, melt) and captures it frame by frame into
 * build/infusion-client-review/screenshots. Run with infusionReviewClient and drop
 * {"op":"create","name":"InfusionReview"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class InfusionClientReview {
    private static final float[] ROW_PROGRESS = {0.08F, 0.3F, 0.55F, 0.8F, 0.98F};
    private static final int COMPLETION_TICKS = BloodInfusionRules.COLLAPSE_TICKS + 22;
    /** Eye offset from the row, the point looked at, then 1 for night. */
    private static final double[][] ROW_SHOTS = {
            {0, 1.9, -5.5, .5, .5, .5, 0}, {0, 1.2, -1.9, .5, .5, .5, 0}, {4, 1.2, -1.9, 4.5, .5, .5, 0},
            {-2, 1.2, -1.9, -1.5, .5, .5, 0}, {0, 1.9, -5.5, .5, .5, .5, 1},
            // The middle block's strike point sweeps across its face; a second look shows the veins following it.
            {0, 1.2, -1.9, .5, .5, .5, 0},
            // The still block again, later, so the sore's writhing edge can be compared with row-3.
            {-2, 1.2, -1.9, -1.5, .5, .5, 0},
            // A block set into the floor, struck right by its edge: the sore must carry over onto its neighbour.
            {-.5, 2.2, -4.4, -.4, 0, -2.5, 0}};
    /** Ticks into the completion linger at which the finale is captured. */
    private static final int[] FINALE_FRAMES = {3, 9, 15, 21, 26, 29, 33, 38, 44, 50};
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    private static int finaleStart = -1;
    private static int finaleFrame;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.infusionReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("infusion-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                BlockPos base = player.blockPosition().offset(0, 0, 6);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(base.offset(-8, -1, -8), base.offset(8, 4, 8)))
                    level.setBlock(pos, pos.getY() < base.getY() ? Blocks.SMOOTH_STONE.defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                for (int i = 0; i < ROW_PROGRESS.length; i++)
                    level.setBlock(rowBlock(base, i), Blocks.STONE.defaultBlockState(), 3);
                level.setBlock(floorBlock(base), Blocks.STONE.defaultBlockState(), 3);
                level.setBlock(finaleBlock(base), Blocks.STONE.defaultBlockState(), 3);
                row = base;
                Hemomancy.LOGGER.info("INFUSION_REVIEW placed row at {}", base);
            });
        }
        if (ticks < 80 || row == null) return;
        holdRow();

        if (shot < ROW_SHOTS.length) {
            int local = (ticks - 80) % 50;
            if (local == 0) look(server, uuid, ROW_SHOTS[shot]);
            if (local == 40) capture(mc, "infusion-row-" + shot);
            return;
        }
        if (finaleStart < 0) {
            look(server, uuid, new double[]{-.6, 1.3, 4.2, .5, .5, 6.5, 0});
            finaleStart = ticks + 40;
            return;
        }
        BlockPos finale = finaleBlock(row);
        int linger = ticks - finaleStart;
        if (linger < 0) {
            feed(finale, 0.98F, 40);
            return;
        }
        if (linger == 0) feed(finale, 1.0F, COMPLETION_TICKS);
        if (linger == BloodInfusionRules.COLLAPSE_TICKS)
            server.execute(() -> server.getPlayerList().getPlayer(uuid).serverLevel()
                    .setBlock(finale, Blocks.AIR.defaultBlockState(), 3));
        if (finaleFrame < FINALE_FRAMES.length && linger == FINALE_FRAMES[finaleFrame]) {
            capture(mc, "infusion-finale-" + finaleFrame + "-t" + linger);
            finaleFrame++;
        }
        if (linger > COMPLETION_TICKS + 10) mc.stop();
    }

    private static void holdRow() {
        for (int i = 0; i < ROW_PROGRESS.length; i++) {
            BlockPos pos = rowBlock(row, i);
            double sweep = i == 2 ? Math.sin(ticks * 0.05D) * 0.3D : 0.0D;
            feed(pos, new Vec3(pos.getX() + 0.5D + sweep, pos.getY() + 0.55D - sweep * 0.5D, pos.getZ()),
                    ROW_PROGRESS[i], 40);
        }
        BlockPos floor = floorBlock(row);
        feed(floor, new Vec3(floor.getX() + 0.93D, floor.getY() + 1.0D, floor.getZ() + 0.45D), 0.45F, 40);
    }

    private static BlockPos floorBlock(BlockPos base) {
        return base.offset(-1, -1, -3);
    }

    private static void feed(BlockPos pos, float progress, int visibleTicks) {
        // Struck on the north face, off-centre, as a player's projection usually lands.
        feed(pos, new Vec3(pos.getX() + 0.35D, pos.getY() + 0.65D, pos.getZ()), progress, visibleTicks);
    }

    private static void feed(BlockPos pos, Vec3 origin, float progress, int visibleTicks) {
        ActiveBloodStructureFeedClientData.upsert(0L, List.of(pos), progress, visibleTicks, BloodFeedStyle.INFUSION,
                origin);
    }

    private static BlockPos rowBlock(BlockPos base, int index) {
        return base.offset(-4 + index * 2, 0, 0);
    }

    private static BlockPos finaleBlock(BlockPos base) {
        return base.offset(0, 0, 6);
    }

    private static void look(net.minecraft.server.MinecraftServer server, java.util.UUID uuid, double[] at) {
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

    private static void capture(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), message -> {});
        Hemomancy.LOGGER.info("INFUSION_REVIEW captured {}", name);
        shot++;
    }
}
