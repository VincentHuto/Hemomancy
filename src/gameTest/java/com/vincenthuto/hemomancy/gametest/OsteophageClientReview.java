package com.vincenthuto.hemomancy.gametest;

import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.OsteophageEntity;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.Files;

/** Explicit, disposable Nether fixtures. Does not claim natural world spawning. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class OsteophageClientReview {
    private static final String[] SCENES = {"perched", "flight", "hunting", "jar_perched", "jar_flight"};
    private static final BlockPos CENTER = new BlockPos(256, 72, 256);
    private static int scene, tick;
    private static boolean started;
    private static volatile boolean ready;
    private static OsteophageEntity actor;

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.osteophage.clientReview")) return;
        var client = Minecraft.getInstance();
        client.options.pauseOnLostFocus = false;
        if (client.player == null || client.getSingleplayerServer() == null) return;
        try {
            if (scene == SCENES.length) {
                Files.writeString(client.gameDirectory.toPath().resolve("osteophage-visuals.txt"),
                        "Native Nether fixtures: perched, flight, hunting, perched jar, flight jar. Natural spawns and multiplayer not exercised.\n");
                client.stop(); return;
            }
            var server = client.getSingleplayerServer(); var uuid = client.player.getUUID();
            if (!started) {
                started = true; ready = false;
                client.options.hideGui = true; client.options.renderDistance().set(8); client.options.gamma().set(.5);
                server.execute(() -> setup(server.getPlayerList().getPlayer(uuid), SCENES[scene]));
                return;
            }
            if (!ready) return;
            tick++;
            if (SCENES[scene].equals("hunting") && tick == 20) server.execute(() -> {
                if (!actor.isFlying() || actor.getTarget() == null)
                    throw new IllegalStateException("Native hunting fixture did not begin a skeleton pursuit");
                LogUtils.getLogger().info("OSTEOPHAGE_NATIVE_HUNT_STARTED position={} target={}", actor.position(), actor.getTarget());
            });
            if (SCENES[scene].equals("hunting") && tick % 5 == 0)
                server.execute(() -> camera(server.getPlayerList().getPlayer(uuid), actor.position().add(0,.7,0), 5));
            if (tick == 45 || tick == 100) {
                String name = "osteophage-" + SCENES[scene] + "-" + tick + ".png";
                Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(),
                        message -> LogUtils.getLogger().info("OSTEOPHAGE_SCREENSHOT {}", message.getString()));
                if (SCENES[scene].startsWith("jar")) {
                    var jar = (SpecimenJarBlockEntity) client.level.getBlockEntity(CENTER);
                    if (jar == null || !jar.getSpecimenCopy().getString("id").equals("hemomancy:osteophage"))
                        throw new IllegalStateException("Client jar did not receive the Osteophage specimen");
                }
            }
            if (tick > 130) { scene++; tick = 0; started = false; }
        } catch (Exception exception) {
            LogUtils.getLogger().error("Osteophage native review failed", exception); client.stop();
        }
    }

    private static void setup(ServerPlayer player, String scene) {
        var server = player.getServer(); var level = server.getLevel(Level.NETHER);
        if (level == null) throw new IllegalStateException("Review world has no Nether");
        var source = player.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
        for (String cmd : new String[]{"gamemode spectator", "gamerule doMobSpawning false", "effect give @s minecraft:night_vision infinite 0 true",
                "fillbiome 240 60 240 272 88 272 minecraft:soul_sand_valley"})
            server.getCommands().performPrefixedCommand(source, cmd);
        for (BlockPos pos : BlockPos.betweenClosed(CENTER.offset(-12,-8,-12), CENTER.offset(12,10,12)))
            level.setBlock(pos, pos.getY() == CENTER.getY()-8 ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        level.getEntitiesOfClass(Mob.class, new AABB(CENTER).inflate(30)).forEach(Mob::discard);
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(CENTER).inflate(30)).forEach(Entity::discard);
        for (int z=-7; z<=7; z++) level.setBlock(CENTER.offset(0,-3,z), Blocks.BONE_BLOCK.defaultBlockState(), 2);
        for (int z : new int[]{-6,-3,0,3,6}) for (int side : new int[]{-1,1}) {
            for (int x=1; x<=3; x++) level.setBlock(CENTER.offset(side*x,-3,z), Blocks.BONE_BLOCK.defaultBlockState(), 2);
            for (int y=-6; y<=-3; y++) level.setBlock(CENTER.offset(side*3,y,z), Blocks.BONE_BLOCK.defaultBlockState(), 2);
        }
        level.setBlock(CENTER.below(2), Blocks.BONE_BLOCK.defaultBlockState(), 2);
        level.setBlock(CENTER.below(), Blocks.BONE_BLOCK.defaultBlockState(), 2);
        BlockPos seat = CENTER;
        actor = EntityInit.osteophage.get().create(level);
        actor.setPersistenceRequired(); actor.setPos(Vec3.atBottomCenterOf(seat)); actor.setYRot(0);
        boolean flying = scene.endsWith("flight");
        if (!flying) actor.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.SPAWN_EGG, null);
        actor.setNoAi(!scene.equals("hunting"));
        if (scene.startsWith("jar")) {
            level.setBlock(CENTER.below(), Blocks.BONE_BLOCK.defaultBlockState(), 2);
            level.setBlock(CENTER, BlockInit.specimen_jar.get().defaultBlockState(), 2);
            ((SpecimenJarBlockEntity) level.getBlockEntity(CENTER)).setSpecimen(SpecimenJarData.captureEntity(actor));
            camera(player, Vec3.atBottomCenterOf(CENTER).add(0,.55,0), 1.7);
        } else {
            level.addFreshEntity(actor);
            if (scene.equals("hunting")) {
                Skeleton skeleton = EntityType.SKELETON.create(level);
                skeleton.setPos(Vec3.atBottomCenterOf(CENTER.east(10).below(7)));
                skeleton.setNoAi(true); skeleton.setPersistenceRequired(); level.addFreshEntity(skeleton);
            }
            camera(player, actor.position().add(0,.7,0), flying ? 4.3 : 3.0);
        }
        LogUtils.getLogger().info("OSTEOPHAGE_CLIENT_REVIEW {}", scene);
        ready = true;
    }

    private static void camera(ServerPlayer player, Vec3 focus, double distance) {
        double x=focus.x+distance*.35, y=focus.y+distance*.22, z=focus.z+distance;
        float yaw=(float) Math.toDegrees(Math.atan2(x-focus.x,focus.z-z));
        float pitch=(float) Math.toDegrees(Math.atan2(y-focus.y,Math.hypot(x-focus.x,z-focus.z)));
        player.teleportTo(player.getServer().getLevel(Level.NETHER), x, y-player.getEyeHeight(), z, yaw, pitch);
    }
}
