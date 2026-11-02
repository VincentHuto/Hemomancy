package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

/**
 * With hemomancy.animalModelReview=&lt;entity id&gt;, frames one walking animal standing, walking, fleeing and standing at
 * night, each from several sides, into its review directory's screenshots. Run with crimsonDoeReviewClient or
 * venousStriderReviewClient; drop {"op":"create","name":"AnimalReview","seed":12345} after startup. Excluded from release
 * jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class AnimalModelClientReview {
    private static final String[] SCENES = {"stand", "walk", "flee", "night"};
    /** Camera offset from the animal: x, y, z. */
    private static final double[][] VIEWS = {{-2.2, 0.5, -2.2}, {3, 0.1, 0}, {1.8, 0.8, 2.4}, {0, 0.3, -3}};
    private static final int SETTLE = 40, PER_VIEW = 25, STEER_EVERY = 25;
    private static int ticks;
    private static BlockPos arena;
    private static volatile PathfinderMob actor;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        String id = System.getProperty("hemomancy.animalModelReview");
        if (id == null) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        UUID uuid = mc.player.getUUID();
        int span = SETTLE + VIEWS.length * PER_VIEW;
        int scene = ticks / span, local = ticks++ % span;
        if (scene >= SCENES.length) {
            mc.stop();
            return;
        }
        if (local == 0) server.execute(() -> setup(server, uuid, id, SCENES[scene]));
        if (actor != null && local % STEER_EVERY == 0) server.execute(() -> steer(SCENES[scene], local / STEER_EVERY));
        if (local < SETTLE) return;
        int view = (local - SETTLE) / PER_VIEW, shot = (local - SETTLE) % PER_VIEW;
        if (actor != null && shot % 2 == 0) server.execute(() -> camera(server, uuid, VIEWS[view]));
        if (shot == PER_VIEW - 2) Screenshot.grab(mc.gameDirectory, id + "-" + SCENES[scene] + "-" + view + ".png",
                mc.getMainRenderTarget(), message -> {});
    }

    private static void setup(MinecraftServer server, UUID uuid, String id, String scene) {
        var player = server.getPlayerList().getPlayer(uuid);
        var level = player.serverLevel();
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        if (arena == null) {
            arena = player.blockPosition().above(30);
            for (String cmd : new String[]{"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
                server.getCommands().performPrefixedCommand(source, cmd);
        }
        server.getCommands().performPrefixedCommand(source, scene.equals("night") ? "time set 18000" : "time set 6000");
        player.setGameMode(GameType.SPECTATOR);
        for (BlockPos pos : BlockPos.betweenClosed(arena.offset(-12, -1, -12), arena.offset(12, 6, 12)))
            level.setBlock(pos, pos.getY() == arena.getY() - 1 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        if (actor != null) actor.discard();
        var animal = (PathfinderMob) BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(id)).create(level);
        animal.setPersistenceRequired();
        animal.setInvulnerable(true);
        animal.setNoAi(scene.equals("stand") || scene.equals("night"));
        animal.setPos(Vec3.atBottomCenterOf(arena.west(8)));
        level.addFreshEntity(animal);
        actor = animal;
    }

    /** Sends it back and forth across the floor. */
    private static void steer(String scene, int leg) {
        if (actor.isNoAi()) return;
        BlockPos end = leg % 8 < 4 ? arena.east(9) : arena.west(9);
        actor.getNavigation().moveTo(end.getX() + 0.5, end.getY(), end.getZ() + 0.5, scene.equals("flee") ? 1.6 : 0.6);
    }

    private static void camera(MinecraftServer server, UUID uuid, double[] at) {
        var player = server.getPlayerList().getPlayer(uuid);
        Vec3 focus = actor.position().add(0, 0.8, 0);
        Vec3 eye = focus.add(at[0], at[1], at[2]);
        float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - eye.x), focus.z - eye.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - eye.y, Math.hypot(focus.x - eye.x, focus.z - eye.z)));
        player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
    }
}
