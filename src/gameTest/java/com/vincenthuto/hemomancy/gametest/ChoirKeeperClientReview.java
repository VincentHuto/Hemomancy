package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Frames the Keeper through its ordinary perch, Eye pursuit and return cycle. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ChoirKeeperClientReview {
    private static final int SHOT_TICKS = 65;
    private static final Shot[] SHOTS = {
            new Shot("perch-front", 0, .25, 3.2, 49),
            new Shot("perch-profile", 3.35, .4, .8, 49),
            new Shot("pursuit-front", 0, .45, 3.8, 40),
            new Shot("pursuit-three-quarter", 2.8, .8, 3.0, 43),
            new Shot("pursuit-wing-stroke", 2.8, .8, 3.0, 48),
            new Shot("return-profile", -3.1, .55, .85, 56)
    };
    private static BlockPos seat;
    private static volatile ChoirKeeperEntity actor;
    private static ItemEntity eye;
    private static int ticks;
    private static int captured;

    private record Shot(String name, double x, double y, double z, int at) {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.choirKeeperReview")) return;
        var mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        if (mc.player == null || server == null
                || !mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("choir-keeper-model-review")) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        UUID player = mc.player.getUUID();
        int scene = ticks / SHOT_TICKS;
        int local = ticks++ % SHOT_TICKS;
        if (scene >= SHOTS.length) {
            Hemomancy.LOGGER.info("CHOIR_KEEPER_REVIEW captured {}/{}", captured, SHOTS.length);
            mc.stop();
            return;
        }
        Shot shot = SHOTS[scene];
        server.execute(() -> advance(server, player, scene, local, shot));
        if (local == shot.at() && actor != null && mc.level.getEntity(actor.getId()) instanceof ChoirKeeperEntity keeper) {
            boolean ready = scene < 2 ? !keeper.isFlying()
                    : keeper.isFlying() && (scene == 5 ? keeper.getFlareAmount(1) < .6F : keeper.getFlareAmount(1) > .9F);
            Hemomancy.LOGGER.info("CHOIR_KEEPER_REVIEW {} ready={} flight={} flare={}", shot.name(),
                    ready, keeper.getFlightPoseAmount(1), keeper.getFlareAmount(1));
            if (ready) captured++;
            Screenshot.grab(mc.gameDirectory, shot.name() + ".png", mc.getMainRenderTarget(), message -> {});
        }
    }

    private static void advance(MinecraftServer server, UUID uuid, int scene, int tick, Shot shot) {
        var player = server.getPlayerList().getPlayer(uuid);
        if (player == null) return;
        var level = player.serverLevel();
        if (seat == null) {
            seat = player.blockPosition().above(22);
            player.setGameMode(GameType.SPECTATOR);
            var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            for (String command : new String[]{"time set 6000", "gamerule doDaylightCycle false",
                    "gamerule doMobSpawning false", "weather clear"}) {
                server.getCommands().performPrefixedCommand(source, command);
            }
            for (BlockPos pos : BlockPos.betweenClosed(seat.offset(-9, -4, -9), seat.offset(9, -4, 22))) {
                level.setBlock(pos, Blocks.END_STONE.defaultBlockState(), 2);
            }
            level.setBlockAndUpdate(seat.below(3), Blocks.CHORUS_PLANT.defaultBlockState());
            level.setBlockAndUpdate(seat.below(2), Blocks.CHORUS_PLANT.defaultBlockState());
            level.setBlockAndUpdate(seat.below(), Blocks.CHORUS_FLOWER.defaultBlockState());
        }
        if (tick == 0) {
            if (eye != null) eye.discard();
            if (actor != null) actor.discard();
            actor = EntityInit.choir_keeper.get().create(level);
            actor.setPos(Vec3.atBottomCenterOf(seat));
            actor.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
            level.addFreshEntity(actor);
        }
        if (scene >= 2 && tick == 20) {
            var target = actor.position().add(0, .25, 8);
            eye = new ItemEntity(level, target.x, target.y, target.z, new ItemStack(Items.ENDER_EYE));
            eye.setNoGravity(true);
            eye.setDeltaMovement(Vec3.ZERO);
            level.addFreshEntity(eye);
        }
        if (scene == 5 && tick == 44) eye.discard();
        if (scene >= 2 && tick >= 20 && eye.isAlive()) eye.setPos(actor.position().add(0, .25, 8));
        Vec3 focus = actor.position().add(0, scene < 2 ? .65 : .92, 0);
        Vec3 offset = new Vec3(shot.x(), shot.y(), shot.z());
        if (scene == 5) offset = offset.yRot((float) Math.PI);
        Vec3 camera = focus.add(offset);
        float yaw = (float) Math.toDegrees(Math.atan2(offset.x, -offset.z));
        float pitch = (float) Math.toDegrees(Math.atan2(offset.y, offset.horizontalDistance()));
        player.teleportTo(level, camera.x, camera.y - player.getEyeHeight(), camera.z, yaw, pitch);
    }
}
