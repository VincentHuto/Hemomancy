package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ErythromyceliumEruptusEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

/**
 * With hemomancy.eruptusReview, frames one Eruptus through every state on one timeline: the corpse, the swelling, the
 * eruption frame by frame, standing and walking, then lunging at a dummy. Run with eruptusReviewClient; drop
 * {"op":"create","name":"EruptusReview","seed":12345} after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EruptusClientReview {
    private static final int SETUP = 40, FUSE_START = 100, WALK_START = 230, LUNGE_START = 330, END = 450, SHOT_EVERY = 6, LUNGE_SHOT_EVERY = 3;
    /** Camera offset from the actor: one fixed view through the change, then round it once it stands. */
    private static final double[] FRONT = {-3.2, 1.5, -3.2}, SIDE = {4.5, 1.2, 0}, BEHIND = {2.6, 1.8, 3.6}, LUNGE = {0, 2.0, 9.0};
    private static int ticks;
    private static volatile ErythromyceliumEruptusEntity actor;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (System.getProperty("hemomancy.eruptusReview") == null) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        UUID uuid = mc.player.getUUID();
        int now = ticks++;
        if (now == 0) server.execute(() -> setup(server, uuid));
        if (now == FUSE_START) server.execute(() -> actor.setSwellDir(1));
        if (now == WALK_START) server.execute(() -> actor.setNoAi(false));
        if (now == LUNGE_START) server.execute(EruptusClientReview::spawnDummy);
        if (actor != null && now >= WALK_START && now < LUNGE_START && now % 25 == 0) server.execute(() -> {
            BlockPos at = actor.blockPosition();
            actor.getNavigation().moveTo(at.getX() + ((now / 25) % 2 == 0 ? 8 : -8), at.getY(), at.getZ(), 1.0);
        });
        if (now >= END) {
            mc.stop();
            return;
        }
        if (now < SETUP || actor == null) return;
        double[] view = now >= LUNGE_START ? LUNGE : now < WALK_START - 40 ? FRONT : now < WALK_START ? SIDE : now < WALK_START + 50 ? BEHIND : FRONT;
        if (now % 2 == 0) server.execute(() -> camera(server, uuid, view));
        if (now % (now >= LUNGE_START ? LUNGE_SHOT_EVERY : SHOT_EVERY) == 0) Screenshot.grab(mc.gameDirectory, String.format("eruptus-%03d.png", now),
                mc.getMainRenderTarget(), message -> {});
    }

    private static void setup(MinecraftServer server, UUID uuid) {
        var player = server.getPlayerList().getPlayer(uuid);
        var level = player.serverLevel();
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        BlockPos arena = player.blockPosition().above(30);
        for (String cmd : new String[]{"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false", "time set 6000"})
            server.getCommands().performPrefixedCommand(source, cmd);
        player.setGameMode(GameType.SPECTATOR);
        for (BlockPos pos : BlockPos.betweenClosed(arena.offset(-14, -1, -14), arena.offset(14, 6, 14)))
            level.setBlock(pos, pos.getY() == arena.getY() - 1 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        var eruptus = EntityInit.erythromycelium_eruptus.get().create(level);
        eruptus.setPersistenceRequired();
        eruptus.setInvulnerable(true);
        eruptus.setNoAi(true);
        eruptus.setPos(Vec3.atBottomCenterOf(arena));
        level.addFreshEntity(eruptus);
        actor = eruptus;
    }

    /** An invulnerable villager a leap away, so the Eruptus keeps lunging without a player to hunt. */
    private static void spawnDummy() {
        var dummy = net.minecraft.world.entity.EntityType.VILLAGER.create(actor.level());
        dummy.setInvulnerable(true);
        dummy.setNoAi(true);
        dummy.setPersistenceRequired();
        dummy.setPos(actor.getX() + 6.0, actor.getY(), actor.getZ());
        actor.level().addFreshEntity(dummy);
        actor.setTarget(dummy);
    }

    private static void camera(MinecraftServer server, UUID uuid, double[] at) {
        var player = server.getPlayerList().getPlayer(uuid);
        Vec3 focus = actor.position().add(0, 1.0, 0);
        Vec3 eye = focus.add(at[0], at[1], at[2]);
        float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - eye.x), focus.z - eye.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - eye.y, Math.hypot(focus.x - eye.x, focus.z - eye.z)));
        player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
    }
}
