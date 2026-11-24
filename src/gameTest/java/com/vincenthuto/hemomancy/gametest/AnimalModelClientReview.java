package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.GameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

/**
 * With hemomancy.animalModelReview=&lt;entity id&gt;, frames one walking animal standing, walking, fleeing and standing at
 * night, each from several sides, into its review directory's screenshots. A water animal gets a flooded arena and
 * floats at its surface; hemomancy.animalModelReview.floor names another floor block. Run with crimsonDoeReviewClient,
 * venousStriderReviewClient, hemojellyReviewClient or myelinBorerReviewClient. Fungling and Hunter Fungus
 * reviews add a melee-swing scene. Drop
 * {"op":"create","name":"AnimalReview","seed":12345} after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class AnimalModelClientReview {
    private static final String[] SCENES = {"stand", "walk", "flee", "night"};
    private static final String[] FUNGAL_SCENES = {"stand", "walk", "flee", "night", "strike"};
    /** Camera offset from the animal: x, y, z. */
    private static final double[][] VIEWS = {{-2.2, 0.5, -2.2}, {3, 0.1, 0}, {1.8, 0.8, 2.4}, {0, 0.3, -3}};
    /** A water animal is framed from below the surface three times and once from above it. */
    private static final double[][] WATER_VIEWS = {{-2.2, 0.3, -2.2}, {3, 0.6, 0}, {1.8, -0.6, 2.4}, {0, 2.6, -2}};
    private static final int SETTLE = 40, PER_VIEW = 25, STEER_EVERY = 25;
    /** Water fills the arena this high above the floor for a water animal. */
    private static final int WATER_DEPTH = 5;
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
        String[] scenes = id.equals("fungling") || id.equals("hunter_fungus") ? FUNGAL_SCENES : SCENES;
        int span = SETTLE + VIEWS.length * PER_VIEW;
        int scene = ticks / span, local = ticks++ % span;
        if (scene >= scenes.length) {
            mc.stop();
            return;
        }
        String sceneName = scenes[scene];
        if (local == 0) server.execute(() -> setup(server, uuid, id, sceneName));
        if (actor != null && local % STEER_EVERY == 0) server.execute(() -> steer(sceneName, local / STEER_EVERY));
        if (local < SETTLE) return;
        int view = (local - SETTLE) / PER_VIEW, shot = (local - SETTLE) % PER_VIEW;
        if (actor != null && shot % 2 == 0)
            server.execute(() -> camera(server, uuid, (actor instanceof WaterAnimal ? WATER_VIEWS : VIEWS)[view]));
        if (sceneName.equals("strike") && shot == PER_VIEW - 5)
            server.execute(() -> actor.swing(InteractionHand.MAIN_HAND));
        if (shot == PER_VIEW - 2) Screenshot.grab(mc.gameDirectory, id + "-" + sceneName + "-" + view + ".png",
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
        if (actor != null) actor.discard();
        var animal = (PathfinderMob) BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(id)).create(level);
        boolean water = animal instanceof WaterAnimal;
        String floorId = System.getProperty("hemomancy.animalModelReview.floor", "");
        BlockState floor = floorId.isEmpty() ? (water ? Blocks.SAND : Blocks.GRASS_BLOCK).defaultBlockState()
                : BuiltInRegistries.BLOCK.get(ResourceLocation.parse(floorId)).defaultBlockState();
        for (BlockPos pos : BlockPos.betweenClosed(arena.offset(-12, -1, -12), arena.offset(12, 8, 12))) {
            int height = pos.getY() - arena.getY();
            level.setBlock(pos, height < 0 ? floor : water && height < WATER_DEPTH ? Blocks.WATER.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2);
        }
        animal.setPersistenceRequired();
        animal.setInvulnerable(true);
        animal.setNoAi(scene.equals("stand") || scene.equals("night") || scene.equals("strike"));
        animal.setPos(Vec3.atBottomCenterOf(arena.west(8).above(water ? WATER_DEPTH - 1 : 0)));
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
        Vec3 focus = actor.position().add(0, actor instanceof WaterAnimal ? -0.6 : Math.min(0.8, actor.getBbHeight() * 0.6), 0);
        // A small land animal is framed closer, so it fills about as much of the shot as a deer does.
        double near = actor instanceof WaterAnimal ? 1.0 : Math.clamp(Math.max(actor.getBbWidth(), actor.getBbHeight()) / 1.4, 0.4, 1.0);
        Vec3 eye = focus.add(at[0] * near, at[1] * near, at[2] * near);
        float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - eye.x), focus.z - eye.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - eye.y, Math.hypot(focus.x - eye.x, focus.z - eye.z)));
        player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
    }
}
