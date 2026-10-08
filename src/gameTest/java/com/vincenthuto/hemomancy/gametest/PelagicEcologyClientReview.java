package com.vincenthuto.hemomancy.gametest;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BoneWormColonyBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.GiantTubeWormColonyBlock;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.plant.PelagicColonyBlockEntity;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;
import java.util.*;

/** Explicit disposable-world visual fixtures. This is not evidence of natural spawning. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PelagicEcologyClientReview {
    private static final String[] SCENES = {"chiton", "chiton_clamped", "pyrosome", "pelagic_herring", "siphonophore",
            "bloody_belly_comb_jelly", "hagfish", "hagfish_slime", "tidepool_anemone", "bone_worm_colony",
            "giant_tube_worm_colony", "tube_retracted", "herring_school", "jar_chiton", "jar_pyrosome",
            "jar_pelagic_herring", "jar_siphonophore", "jar_bloody_belly_comb_jelly", "jar_hagfish",
            "bone_worm_colony_2", "bone_worm_colony_3", "bone_worm_colony_4", "bone_worm_colony_5", "bone_worm_counts",
            "tube_worm_colony_2", "tube_worm_colony_3", "tube_worm_colony_4", "tube_worm_colony_5", "tube_worm_counts", "tube_worm_counts_retracted",
            "vampire_squid", "vampire_squid_cloaked", "jar_vampire_squid", "jar_vampire_squid_cloaked",
            "ice_fish", "jar_ice_fish", "ice_fish_school", "ice_fish_beached",
            "comb_jelly_swimming", "vampire_squid_swimming", "comb_jelly_diving", "vampire_squid_diving", "hemojelly_surface", "hemojelly_capture", "siphonophore_zap"};
    private static int index = Integer.getInteger("hemomancy.pelagic.reviewStartScene", 0), tick;
    private static boolean started, capturedZap;
    private static volatile boolean ready;
    private static final JsonArray results = new JsonArray();
    private static final List<Mob> actors = new ArrayList<>();
    private static final Map<UUID, Vec3> initial = new HashMap<>();
    private static BlockPos colonyPos;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.pelagic.ecologyClientReview")) return;
        var client = Minecraft.getInstance();
        client.options.pauseOnLostFocus = false;
        if (client.player == null || client.getSingleplayerServer() == null) return;
        try {
            if (index == SCENES.length) {
                Files.writeString(client.gameDirectory.toPath().resolve("pelagic-ecology-visuals.json"), new GsonBuilder().setPrettyPrinting().create().toJson(results));
                client.stop(); return;
            }
            String scene = SCENES[index];
            if (!started) {
                started = true; ready = false; capturedZap = false;
                client.options.renderDistance().set(8); client.options.gamma().set(.5); client.options.hideGui = true;
                var server = client.getSingleplayerServer(); var uuid = client.player.getUUID();
                server.execute(() -> setup(server.getPlayerList().getPlayer(uuid), scene));
                return;
            }
            if (!ready) return;
            tick++;
            if (tick == 60 && scene.startsWith("jar_")) {
                var jar=(SpecimenJarBlockEntity)client.level.getBlockEntity(new BlockPos(-416,42,2624));
                String expected = scene.substring(4).replace("_cloaked", "");
                LogUtils.getLogger().info("PELAGIC_JAR_CLIENT expected={} actual={}",expected,jar==null ? "missing jar" : jar.getSpecimenCopy().getString("id"));
                if(jar==null || !jar.getSpecimenCopy().getString("id").equals("hemomancy:"+expected)) throw new IllegalStateException("Client jar data mismatch: "+scene);
            }
            if (tick == 60 && scene.equals("ice_fish_beached")) {
                var fish = client.level.getEntity(actors.getFirst().getId());
                if (fish == null || fish.isInWater()) throw new IllegalStateException("Beached fish fixture is not dry");
            }
            if (directionalSwimmer(scene) && tick >= 50 && tick <= 75) {
                client.getSingleplayerServer().execute(() -> actors.getFirst().setDeltaMovement(
                        new Vec3(.06, scene.endsWith("_diving") ? -.025 : 0, .04)));
            }
            if (tick == 68 && directionalSwimmer(scene)) {
                var swimmer = (PelagicAnimal)client.level.getEntity(actors.getFirst().getId());
                if (swimmer == null || swimmer.swimmingPose().leanRadians(1) < .5F)
                    throw new IllegalStateException("Client swimmer did not tilt: " + scene + " client="
                            + (swimmer == null ? "missing" : "lean="+swimmer.swimmingPose().leanRadians(1)
                            + " pos="+swimmer.position()+" water="+swimmer.isInWater()+" age="+swimmer.tickCount)
                            + " serverLean="+((PelagicAnimal)actors.getFirst()).swimmingPose().leanRadians(1)
                            + " serverPos="+actors.getFirst().position()+" motion="+actors.getFirst().getDeltaMovement());
                LogUtils.getLogger().info("PELAGIC_SWIMMING_POSE scene={} lean={} heading={}", scene,
                        swimmer.swimmingPose().leanRadians(1), swimmer.swimmingPose().headingDegrees(1));
            }
            if ((tick == 55 || tick == 110) && scene.equals("hemojelly_capture"))
                client.getSingleplayerServer().execute(() -> {
                    var hunter = actors.getFirst();
                    var fish = EntityType.COD.create(hunter.level()); fish.setNoAi(true); fish.setNoGravity(true);
                    fish.setPos(hunter.getX(),hunter.getY()-.9,hunter.getZ()); hunter.level().addFreshEntity(fish); actors.add(fish);
                });
            if (tick == 65 && scene.equals("siphonophore_zap"))
                client.getSingleplayerServer().execute(() -> {
                    var hunter = actors.getFirst(); hunter.tickCount = 9;
                    var fish = EntityType.COD.create(hunter.level()); fish.setNoAi(true); fish.setNoGravity(true);
                    fish.setPos(hunter.getX(),hunter.getY()-1.5,hunter.getZ()); hunter.level().addFreshEntity(fish); actors.add(fish);
                });
            if (tick == 55 && scene.equals("hagfish_slime"))
                client.getSingleplayerServer().execute(() -> actors.getFirst().hurt(actors.getFirst().damageSources().generic(), 1));
            if (tick == 55 && scene.equals("vampire_squid_cloaked"))
                client.getSingleplayerServer().execute(() -> actors.getFirst().hurt(actors.getFirst().damageSources().generic(), 1));
            if (tick == 55 && scene.equals("tube_retracted"))
                client.getSingleplayerServer().execute(() -> ((PelagicColonyBlockEntity)client.getSingleplayerServer().overworld().getBlockEntity(colonyPos)).retract());
            if ((tick == 55 || tick == 80 || tick == 110) && scene.equals("tube_worm_counts_retracted"))
                client.getSingleplayerServer().execute(() -> {
                    for (int i = 0; i < 4; i++)
                        ((PelagicColonyBlockEntity)client.getSingleplayerServer().overworld().getBlockEntity(colonyPos.offset(i * 2, 0, 0))).retract();
                });
            if (scene.equals("siphonophore_zap") && !capturedZap
                    && client.level.getEntity(actors.getFirst().getId()) instanceof SiphonophoreEntity colony
                    && colony.actionTicks() > 0 && colony.actionTicks() <= 27) {
                capturedZap = true;
                Screenshot.grab(client.gameDirectory, "ecology-44-siphonophore_zap-active.png", client.getMainRenderTarget(),
                        message -> LogUtils.getLogger().info("PELAGIC_NATIVE_ZAP_SCREENSHOT {}", message.getString()));
            }
            if (tick == 70 || tick == 125) {
                var name = "ecology-" + String.format("%02d", index) + "-" + scene + (tick == 70 ? "-lit.png" : "-dark.png");
                Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), message -> LogUtils.getLogger().info("PELAGIC_ECOLOGY_SCREENSHOT {}", message.getString()));
            }
            if (tick == 80) {
                var server = client.getSingleplayerServer(); var uuid = client.player.getUUID();
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(uuid);
                    player.removeAllEffects();
                    if (scene.endsWith("clamped")) ((ChitonEntity)actors.getFirst()).clamp();
                    if (scene.equals("tube_retracted") && player.serverLevel().getBlockEntity(colonyPos) instanceof PelagicColonyBlockEntity colony) colony.retract();
                });
            }
            if (tick > 145) {
                var row = new JsonObject(); row.addProperty("scene", scene); row.addProperty("nativeClient", true);
                row.addProperty("darkViewHasNightVision", false); row.addProperty("fixture", true); results.add(row);
                index++; tick = 0; started = false;
            }
        } catch (Exception e) { LogUtils.getLogger().error("Ecology native review failed", e); client.stop(); }
    }
    private static boolean directionalSwimmer(String scene) {
        return scene.endsWith("_swimming") || scene.endsWith("_diving");
    }
    private static void setup(net.minecraft.server.level.ServerPlayer player, String scene) {
        var server = player.serverLevel().getServer();
        var level = server.overworld();
        if (player.serverLevel() != level) player.teleportTo(level, -416.5, 70, 2624.5, 0, 0);
        var source = player.createCommandSourceStack().withPermission(4);
        for (String cmd : new String[]{"gamemode spectator", "time set midnight", "weather clear", "gamerule doDaylightCycle false",
                "gamerule doMobSpawning false", "effect clear @s", "effect give @s minecraft:night_vision infinite 0 true"})
            server.getCommands().performPrefixedCommand(source, cmd);
        int depth = switch (scene) {
            case "siphonophore", "siphonophore_zap" -> 18;
            case "hemojelly_surface", "hemojelly_capture" -> 63;
            case "bloody_belly_comb_jelly" -> -8;
            case "comb_jelly_swimming", "comb_jelly_diving" -> -2;
            case "hagfish", "hagfish_slime", "bone_worm_colony" -> -24;
            case "giant_tube_worm_colony", "tube_retracted" -> -44;
            case "vampire_squid", "vampire_squid_cloaked", "vampire_squid_swimming", "vampire_squid_diving" -> -35;
            default -> 42;
        };
        if (scene.startsWith("bone_worm_")) depth = -24;
        if (scene.startsWith("tube_worm_")) depth = -44;
        var center = new BlockPos(-416, depth, 2624);
        actors.clear(); initial.clear(); colonyPos = null;
        for (var pos : BlockPos.betweenClosed(center.offset(-12,-6,-12), center.offset(12,7,12)))
            level.setBlock(pos, pos.getY() == center.getY()-6 ? Blocks.STONE.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
        if (scene.startsWith("hemojelly_"))
            for (var pos : BlockPos.betweenClosed(center.offset(-12,0,-12), center.offset(12,7,12)))
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        if (scene.contains("ice_fish")) {
            for (var pos : BlockPos.betweenClosed(center.offset(-12,4,-12), center.offset(12,4,12)))
                level.setBlock(pos, Blocks.ICE.defaultBlockState(), 2);
            server.getCommands().performPrefixedCommand(source, "fillbiome -428 36 2612 -404 49 2636 minecraft:frozen_ocean");
        }
        level.getEntitiesOfClass(Mob.class, new AABB(center).inflate(40)).forEach(Mob::discard);
        Vec3 focus = center.getCenter(); double distance = 2.2;
        String id = scene.replace("jar_", "").replace("_clamped", "").replace("_slime", "").replace("_cloaked", "").replace("_beached", "")
                .replace("_swimming", "").replace("_diving", "");
        if (scene.startsWith("hemojelly_")) id = "hemojelly";
        if (scene.equals("siphonophore_zap")) id = "siphonophore";
        if (id.equals("comb_jelly")) id = "bloody_belly_comb_jelly";
        if (directionalSwimmer(scene)) {
            var layer = id.equals("vampire_squid")
                    ? com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.CARRION
                    : com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.MIDNIGHT;
            String biome = com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicBiomes.key(layer).location().toString();
            server.getCommands().performPrefixedCommand(source, "fillbiome " + (center.getX()-12) + " " + (depth-6)
                    + " " + (center.getZ()-12) + " " + (center.getX()+12) + " " + (depth+7) + " " + (center.getZ()+12) + " " + biome);
        }
        if (scene.startsWith("tube_worm_counts")) {
            var block = BlockInit.giant_tube_worm_colony.get();
            for (int worms = 2; worms <= 5; worms++) {
                var pos = center.offset((worms - 2) * 2, 0, 0);
                level.setBlock(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
                var state = block.defaultBlockState().setValue(GiantTubeWormColonyBlock.WORMS, worms);
                level.setBlock(pos, state, 2);
                block.setPlacedBy(level, pos, state, player, new ItemStack(block));
            }
            colonyPos = center;
            focus = Vec3.atBottomCenterOf(center).add(3, .9, 0); distance = 6;
        } else if (scene.equals("bone_worm_counts")) {
            for (int worms = 2; worms <= 5; worms++) {
                var pos = center.offset((worms - 2) * 2, 0, 0);
                level.setBlock(pos.below(), Blocks.BONE_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos, BlockInit.bone_worm_colony.get().defaultBlockState().setValue(BoneWormColonyBlock.WORMS, worms), 3);
            }
            focus = Vec3.atBottomCenterOf(center).add(3, .25, 0); distance = 6;
        } else if (scene.startsWith("jar_")) {
            var mob = (Mob)BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(id)).create(level);
            if (scene.equals("jar_vampire_squid_cloaked")) mob.hurt(mob.damageSources().generic(), 1);
            var pos = center;
            level.setBlock(pos.below(), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            level.setBlock(pos, BlockInit.specimen_jar.get().defaultBlockState(), 3);
            ((SpecimenJarBlockEntity)level.getBlockEntity(pos)).setSpecimen(SpecimenJarData.captureEntity(mob));
            focus = Vec3.atBottomCenterOf(pos).add(0,.55,0); distance = 1.4;
        } else if (scene.contains("colony") || scene.equals("tidepool_anemone") || scene.equals("tube_retracted")) {
            if (scene.equals("tube_retracted")) id = "giant_tube_worm_colony";
            int worms = scene.startsWith("bone_worm_colony_") || scene.startsWith("tube_worm_colony_")
                    ? Integer.parseInt(scene.substring(scene.length() - 1)) : 2;
            if (scene.startsWith("bone_worm_colony_")) id = "bone_worm_colony";
            if (scene.startsWith("tube_worm_colony_")) id = "giant_tube_worm_colony";
            var block = BuiltInRegistries.BLOCK.get(Hemomancy.rloc(id));
            var state = block.defaultBlockState();
            if (block instanceof BoneWormColonyBlock) state = state.setValue(BoneWormColonyBlock.WORMS, worms);
            if (block instanceof GiantTubeWormColonyBlock) state = state.setValue(GiantTubeWormColonyBlock.WORMS, worms);
            level.setBlock(center.below(), id.equals("bone_worm_colony") ? Blocks.BONE_BLOCK.defaultBlockState() : id.contains("tube") ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(center, state, 2);
            block.setPlacedBy(level, center, state, player, new ItemStack(block));
            colonyPos = center;
            if (scene.equals("tube_retracted")) ((PelagicColonyBlockEntity)level.getBlockEntity(center)).retract();
            focus = Vec3.atBottomCenterOf(center).add(0,id.contains("tube") ? .9 : .25,0);
            distance = id.contains("tube") ? 2.2 : 1.5;
        } else {
            if (scene.equals("ice_fish_beached")) {
                for (var pos : BlockPos.betweenClosed(center.offset(-3,0,-3), center.offset(3,3,3))) {
                    boolean wall = Math.abs(pos.getX()-center.getX()) == 3 || Math.abs(pos.getZ()-center.getZ()) == 3
                            || pos.getY() == center.getY()+3;
                    level.setBlock(pos, wall ? Blocks.GLASS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
                }
                for (var pos : BlockPos.betweenClosed(center.offset(-3,-1,-3), center.offset(3,-1,3)))
                    level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            }
            boolean school = scene.equals("herring_school") || scene.equals("ice_fish_school");
            if (school) id = scene.equals("ice_fish_school") ? "ice_fish" : "pelagic_herring";
            SpawnGroupData group = null;
            for (int i=0; i<(school ? (id.equals("ice_fish") ? 6 : 8) : 1); i++) {
                var mob = (Mob)BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(id)).create(level);
                mob.setPersistenceRequired(); mob.setNoAi(!school && !directionalSwimmer(scene) && !scene.startsWith("hemojelly_") && !scene.equals("siphonophore_zap")); mob.setNoGravity(true);
                if (directionalSwimmer(scene)) mob.goalSelector.removeAllGoals(goal -> true);
                mob.moveTo(center.getX()+.5+(i%4)*.65, center.getY(), center.getZ()+.5+(i/4)*.7, -35, 0);
                if (scene.startsWith("hemojelly_")) mob.setPos(center.getX()+.5,center.getY()-.625,center.getZ()+.5);
                if (school) group = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.SPAWN_EGG, group);
                if (scene.equals("chiton_clamped")) ((ChitonEntity)mob).clamp();
                if (id.equals("chiton") || id.equals("hagfish")) {
                    level.setBlock(center.below(), Blocks.STONE.defaultBlockState(), 3);
                    if (id.equals("chiton")) level.setBlock(center, BlockInit.hematic_algal_crust.get().defaultBlockState(), 3);
                }
                level.addFreshEntity(mob); actors.add(mob); initial.put(mob.getUUID(), mob.position());
            }
            if (school) { focus = focus.add(1.5,0,0); distance = 7; }
            else {
                focus = focus.add(0, actors.getFirst().getBbHeight()*.35-.3,0);
                distance = switch (id) { case "siphonophore" -> 3.4; case "pyrosome", "hagfish" -> 2.2; case "vampire_squid" -> 1.9; default -> 1.45; };
                if (directionalSwimmer(scene)) distance = 3.4;
                if (id.equals("siphonophore")) focus = focus.add(0,-.7,0);
                if (id.equals("hemojelly")) { focus = center.getCenter().add(0,-1.3,0); distance = 3.5; }
            }
        }
        double elevation = scene.startsWith("chiton") || scene.equals("pyrosome") ? .65 : .23;
        if (scene.startsWith("bone_worm_")) elevation = .6;
        double cameraX=focus.x+distance*.75, cameraZ=focus.z+distance*.75, cameraY=focus.y+distance*elevation;
        if (scene.startsWith("tube_worm_")) {
            cameraX = focus.x + distance * .25;
            cameraZ = focus.z - distance;
            cameraY = focus.y + distance * .5;
        }
        // Look through a glass pane, not straight into the jar's solid corner post.
        if (scene.startsWith("jar_")) { cameraX=focus.x+distance*.15; cameraZ=focus.z+distance; }
        if (directionalSwimmer(scene)) { cameraX=focus.x-distance*.55; cameraZ=focus.z+distance; }
        float yaw=(float)Math.toDegrees(Math.atan2(cameraX-focus.x,focus.z-cameraZ));
        float pitch=(float)Math.toDegrees(Math.atan2(cameraY-focus.y,Math.hypot(cameraX-focus.x,cameraZ-focus.z)));
        cameraY -= player.getEyeHeight();
        player.teleportTo(cameraX,cameraY,cameraZ); player.setYRot(yaw); player.setXRot(pitch);
        server.getCommands().performPrefixedCommand(source,"tp @s " + cameraX + " " + cameraY + " " + cameraZ + " " + yaw + " " + pitch);
        LogUtils.getLogger().info("PELAGIC_ECOLOGY_REVIEW {}",scene); ready = true;
    }
}
