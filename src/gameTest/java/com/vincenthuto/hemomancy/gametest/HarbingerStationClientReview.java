package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.GhastlyAlembicBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.EngramBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.HematicArmatureBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * With hemomancy.harbingerStationReview, places a lit Ghastly Alembic, a Hematic Suture Node, a lit Engram and a
 * Hematic Armature in a disposable world and captures their particles from fixed poses into
 * build/harbinger-station-review/screenshots: the Alembic drip, the Suture Node seep, the Engram's bite when a bare
 * hand puts it out, and the Armature's wind-up and upgrade bursts around a stand-in wearer. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HarbingerStationClientReview {
    private static final UUID WEARER = UUID.fromString("6f1a8c2e-3b7d-4e52-9a10-5c4d2e7b8f31");
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    /** Eye offset from the row origin, the point looked at, and the scene action fired just before the capture. */
    private static final Object[][] SHOTS = {
            {new double[]{5.0, 3.2, -7.5, 5.0, 1.0, .5}, "none"},
            {new double[]{.5, 2.4, -3.2, .5, 1.3, .5}, "none"},
            {new double[]{4.5, 1.6, -2.2, 4.5, .6, .5}, "none"},
            {new double[]{7.5, 2.6, -1.8, 7.5, .9, .5}, "engram"},
            {new double[]{11.0, 2.4, -4.2, 11.0, 1.1, 1.0}, "windup"},
            {new double[]{11.0, 2.4, -4.2, 11.0, 1.1, 1.0}, "upgrade"}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.harbingerStationReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("harbinger-station-review")) return;
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
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-4, -1, -9), row.offset(15, 5, 6)))
                    level.setBlock(pos, pos.getY() < row.getY() ? Blocks.SMOOTH_STONE.defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);

                var alembic = BlockInit.ghastly_alembic.get();
                var alembicState = alembic.defaultBlockState().setValue(GhastlyAlembicBlock.FACING, Direction.NORTH)
                        .setValue(GhastlyAlembicBlock.LIT, true);
                level.setBlockAndUpdate(row, alembicState);
                alembic.setPlacedBy(level, row, alembicState, null, new ItemStack(alembic));

                level.setBlockAndUpdate(row.offset(4, 0, 0), BlockInit.hematic_suture_node.get().defaultBlockState());

                level.setBlockAndUpdate(row.offset(7, 0, 0), BlockInit.engram_block.get().defaultBlockState()
                        .setValue(EngramBlock.FACING, Direction.UP).setValue(EngramBlock.LIT, true));

                var armature = BlockInit.hematic_armature.get();
                var armatureState = armature.defaultBlockState().setValue(HematicArmatureBlock.FACING, Direction.NORTH);
                level.setBlockAndUpdate(row.offset(11, 0, 0), armatureState);
                armature.setPlacedBy(level, row.offset(11, 0, 0), armatureState, null, new ItemStack(armature));
                Hemomancy.LOGGER.info("HARBINGER_STATION_REVIEW placed alembic {}, suture node, engram {}, armature",
                        level.getBlockState(row).getValue(GhastlyAlembicBlock.LIT) ? "lit" : "unlit",
                        level.getBlockState(row.offset(7, 0, 0)));
            });
        }
        if (ticks < 80 || row == null) return;
        int local = (ticks - 80) % 60;
        if (local == 0) {
            if (shot == SHOTS.length) { mc.stop(); return; }
            double[] at = (double[]) SHOTS[shot][0];
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
                double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
            });
        }
        String action = (String) SHOTS[Math.min(shot, SHOTS.length - 1)][1];
        if (shot < SHOTS.length && action.equals("windup") && local >= 10 && local <= 40)
            server.execute(() -> armature(server.overworld(), "spawnWindupEffects", local / 40.0));
        if (shot < SHOTS.length && local == 36) {
            if (action.equals("engram")) server.execute(() -> {
                ServerLevel level = server.overworld();
                BlockPos engram = row.offset(7, 0, 0);
                var result = level.getBlockState(engram).useWithoutItem(level, wearer(level, engram),
                        new BlockHitResult(Vec3.atCenterOf(engram), Direction.UP, engram, false));
                Hemomancy.LOGGER.info("HARBINGER_STATION_REVIEW engram {} after a bare hand: {}", result, level.getBlockState(engram));
            });
            if (action.equals("upgrade")) server.execute(() -> armature(server.overworld(), "spawnUpgradeEffects", 1.0));
        }
        if (local == 40 && shot < SHOTS.length) {
            Screenshot.grab(mc.gameDirectory, "harbinger-station-" + shot + "-" + action + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("HARBINGER_STATION_REVIEW captured {}", shot);
            shot++;
        }
    }

    /** A stand-in wearer at the Armature's front, since its bursts gather on the restrained body. */
    private static ServerPlayer wearer(ServerLevel level, BlockPos near) {
        ServerPlayer wearer = FakePlayerFactory.get(level, new GameProfile(WEARER, "StationWearer"));
        wearer.moveTo(near.getX() + .5, near.getY(), near.getZ() - .6, 0, 0);
        return wearer;
    }

    /** Calls the Armature's own private effect method, so the scene shows exactly what an upgrade spawns. */
    private static void armature(ServerLevel level, String effect, double progress) {
        BlockPos pos = row.offset(11, 0, 0);
        if (!(level.getBlockEntity(pos) instanceof HematicArmatureBlockEntity station)) return;
        try {
            if (effect.equals("spawnWindupEffects")) {
                Method method = HematicArmatureBlockEntity.class.getDeclaredMethod(effect, ServerLevel.class,
                        ServerPlayer.class, int.class, double.class);
                method.setAccessible(true);
                method.invoke(station, level, wearer(level, pos), 0, progress);
            } else {
                Method method = HematicArmatureBlockEntity.class.getDeclaredMethod(effect, ServerLevel.class,
                        ServerPlayer.class, int.class);
                method.setAccessible(true);
                method.invoke(station, level, wearer(level, pos), 0);
            }
        } catch (ReflectiveOperationException e) {
            Hemomancy.LOGGER.error("HARBINGER_STATION_REVIEW could not run {}", effect, e);
        }
    }
}
