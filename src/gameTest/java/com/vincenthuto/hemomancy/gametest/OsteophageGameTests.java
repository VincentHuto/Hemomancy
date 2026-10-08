package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("osteophage_validation")
@PrefixGameTestTemplate(false)
public final class OsteophageGameTests {
    @GameTest(template = "empty", timeoutTicks = 40, batch = "osteophage_spawn_table")
    public static void loadedSpawnTableAddsBirdOnlyToSoulSandValley(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        var type = type();
        var valley = biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY);
        var forest = biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST);
        var entries = valley.getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.AMBIENT).unwrap();
        helper.assertTrue(entries.stream().anyMatch(entry -> entry.type == type && entry.minCount == 1 && entry.maxCount == 1),
                "Loaded Soul Sand Valley biome lacks the Osteophage spawn entry");
        helper.assertTrue(forest.getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.AMBIENT).unwrap().stream()
                .noneMatch(entry -> entry.type == type), "Osteophage leaked into Crimson Forest spawn table");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 350, batch = "osteophage_repeated_hunt")
    public static void makesSeveralPassesAgainstHealthySkeleton(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 6, 5));
        Mob bird = bird(helper, seat);
        Skeleton skeleton = EntityType.SKELETON.create(helper.getLevel());
        protectFromFixtureSun(skeleton);
        skeleton.setPos(Vec3.atBottomCenterOf(seat.east(5).below(3)));
        skeleton.setNoAi(true); skeleton.setNoGravity(true);
        helper.getLevel().addFreshEntity(skeleton);
        helper.runAfterDelay(55, () -> helper.assertTrue(skeleton.getHealth() > 0 && skeleton.getHealth() < 20,
                "First hunting pass should wound a healthy skeleton"));
        helper.runAfterDelay(300, () -> {
            helper.assertTrue(!skeleton.isAlive() && bird.isAlive(), "Osteophage did not complete repeated hunting passes");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "osteophage_loot")
    public static void playerKillDropsSharedVenousPinions(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 5, 5));
        Mob bird = bird(helper, seat);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        bird.hurt(bird.damageSources().playerAttack(player), 100);
        helper.runAfterDelay(25, () -> {
            int count = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(seat).inflate(6),
                    item -> item.getItem().is(com.vincenthuto.hemomancy.common.init.ItemInit.venous_pinion.get()))
                    .stream().mapToInt(item -> item.getItem().getCount()).sum();
            helper.assertTrue(count >= 1 && count <= 2, "Player kill did not drop 1-2 shared Venous Pinions");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 120, batch = "osteophage_pickup")
    public static void respectsPickupDelayAndPrefersBonesToLivingPrey(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 6, 5));
        Mob bird = bird(helper, seat);
        ItemEntity bones = item(helper, seat.east(4).below(2), Items.BONE, 2);
        bones.setPickUpDelay(40);
        helper.runAfterDelay(30, () -> helper.assertTrue(bones.getItem().getCount() == 2,
                "Osteophage ignored the dropped item's pickup delay"));
        helper.runAfterDelay(42, () -> {
            Skeleton skeleton = EntityType.SKELETON.create(helper.getLevel());
            protectFromFixtureSun(skeleton);
            skeleton.setNoAi(true); skeleton.setNoGravity(true);
            skeleton.setPos(Vec3.atBottomCenterOf(seat.south(3).below(2)));
            helper.getLevel().addFreshEntity(skeleton);
            helper.runAfterDelay(50, () -> {
                helper.assertTrue(bones.getItem().getCount() == 1 && skeleton.getHealth() == 20,
                        "Bone-first feeding failed: bones=" + bones.getItem().getCount() + ", skeletonHealth="
                                + skeleton.getHealth() + ", fireTicks=" + skeleton.getRemainingFireTicks()
                                + ", bird=" + bird.position() + ", target=" + bird.getTarget());
                helper.succeed();
            });
        });
    }
    @GameTest(template = "empty", timeoutTicks = 40, batch = "osteophage_spawn")
    public static void spawnRulesRequireValleyBoneAndClearance(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 5, 5));
        var level = helper.getLevel();
        var type = type();
        helper.assertTrue(!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, seat, RandomSource.create()),
                "Osteophage spawned outside Soul Sand Valley");
        var source = level.getServer().createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
        level.getServer().getCommands().performPrefixedCommand(source,
                "fillbiome " + (seat.getX()-12) + " " + Math.max(level.getMinBuildHeight(), seat.getY()-12) + " " + (seat.getZ()-12)
                        + " " + (seat.getX()+12) + " " + (seat.getY()+12) + " " + (seat.getZ()+12) + " minecraft:soul_sand_valley");
        helper.assertTrue(SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, seat, RandomSource.create()),
                "Clear valley bone perch did not allow natural spawning");
        level.setBlockAndUpdate(seat.above(), Blocks.NETHERRACK.defaultBlockState());
        helper.assertTrue(!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, seat, RandomSource.create()),
                "Obstructed bone perch allowed a spawn");
        level.setBlockAndUpdate(seat.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(seat.below(), Blocks.SOUL_SAND.defaultBlockState());
        helper.assertTrue(!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, seat, RandomSource.create()),
                "Ordinary soul sand was treated as a fossil perch");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 110, batch = "osteophage_passive")
    public static void ignoresPlayersAndBoneMeal(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 5, 5));
        Mob bird = bird(helper, seat);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(seat.east(2)));
        ItemEntity meal = item(helper, seat.east(3), Items.BONE_MEAL, 2);
        helper.runAfterDelay(90, () -> {
            helper.assertTrue(bird.getTarget() == null && player.getHealth() == player.getMaxHealth(),
                    "Osteophage attacked a player");
            helper.assertTrue(meal.isAlive() && meal.getItem().getCount() == 2, "Bird ate bone meal instead of bone items");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200, batch = "osteophage_navigation")
    public static void savedBirdClimbsOverWallWithoutSnappingToItsPerch(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(3, 4, 5));
        Mob bird = bird(helper, seat);
        CompoundTag saved = new CompoundTag(); bird.addAdditionalSaveData(saved);
        bird.setPos(Vec3.atBottomCenterOf(seat.east(8)));
        bird.readAdditionalSaveData(saved);
        for (BlockPos pos : BlockPos.betweenClosed(seat.offset(4, 0, -4), seat.offset(4, 3, 4)))
            helper.getLevel().setBlockAndUpdate(pos, Blocks.NETHERRACK.defaultBlockState());
        helper.runAfterDelay(1, () -> helper.assertTrue(bird.distanceToSqr(Vec3.atBottomCenterOf(seat)) > 20,
                "Loading an airborne bird teleported it onto the saved perch"));
        helper.runAfterDelay(160, () -> {
            helper.assertTrue(bird.position().distanceToSqr(Vec3.atBottomCenterOf(seat)) < .1,
                    "Osteophage could not climb over the wall and land on its saved bone perch");
            helper.succeed();
        });
    }
    @GameTest(template = "empty", timeoutTicks = 120, batch = "osteophage_perches")
    public static void returnsToBonePerchAfterEatingOneOfferedBone(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 5, 5));
        Mob bird = bird(helper, seat);
        ItemEntity bones = item(helper, seat.east(4).below(2), Items.BONE, 3);
        bird.hurt(bird.damageSources().generic(), 3);
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(bones.isAlive() && bones.getItem().getCount() == 2,
                    "Offering must consume one bone, preserving the rest of the stack");
            helper.assertTrue(bird.position().distanceToSqr(Vec3.atBottomCenterOf(seat)) < .1,
                    "Fed Osteophage did not return to its bone perch");
            helper.assertTrue(bird.getHealth() == 15, "One bone should restore two health");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 260, batch = "osteophage_hunting")
    public static void swoopsOnSkeletonAndThenScavengesItsBones(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 5, 5));
        Mob bird = bird(helper, seat);
        bird.hurt(bird.damageSources().generic(), 3);
        Skeleton skeleton = EntityType.SKELETON.create(helper.getLevel());
        protectFromFixtureSun(skeleton);
        CompoundTag loot = new CompoundTag(); skeleton.addAdditionalSaveData(loot);
        loot.putString("DeathLootTable", "osteophage_validation:one_bone");
        skeleton.readAdditionalSaveData(loot);
        skeleton.setPos(Vec3.atBottomCenterOf(seat.east(5).below(3)));
        skeleton.setNoAi(true);
        skeleton.setNoGravity(true);
        skeleton.setHealth(4);
        helper.getLevel().addFreshEntity(skeleton);
        helper.runAfterDelay(120, () -> {
            helper.assertTrue(!skeleton.isAlive(), "Osteophage did not swoop down and kill its skeleton prey");
        });
        helper.runAfterDelay(220, () -> {
            var remains = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(seat).inflate(12), item -> item.getItem().is(Items.BONE));
            helper.assertTrue(remains.isEmpty(), "Osteophage ignored the bone actually dropped by its prey");
            helper.assertTrue(bird.isAlive(), "Hunting Osteophage vanished");
            helper.assertTrue(bird.getHealth() == 15, "Bird did not restore health from its prey's guaranteed bone drop");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 120, batch = "osteophage_perches")
    public static void abandonsBrokenBoneAndFindsAnotherPerch(GameTestHelper helper) {
        BlockPos seat = bonePerch(helper, new BlockPos(5, 5, 5));
        BlockPos alternate = bonePerch(helper, new BlockPos(10, 5, 5));
        Mob bird = bird(helper, seat);
        helper.getLevel().setBlockAndUpdate(seat.below(), Blocks.AIR.defaultBlockState());
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(bird.position().distanceToSqr(Vec3.atBottomCenterOf(alternate)) < .1,
                    "Osteophage stayed on a broken fossil instead of finding another bone perch");
            helper.succeed();
        });
    }

    private static Mob bird(GameTestHelper helper, BlockPos seat) {
        Mob bird = (Mob) type().create(helper.getLevel());
        bird.setPos(Vec3.atBottomCenterOf(seat));
        bird.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        bird.setPersistenceRequired();
        helper.getLevel().addFreshEntity(bird);
        return bird;
    }

    private static EntityType<?> type() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(Hemomancy.rloc("osteophage"))
                .orElseThrow(() -> new AssertionError("Osteophage entity is not registered"));
    }

    private static void protectFromFixtureSun(Skeleton skeleton) {
        // GameTests run in an exposed Overworld arena; Nether prey do not burn in sunlight.
        skeleton.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
        skeleton.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0);
    }

    private static BlockPos bonePerch(GameTestHelper helper, BlockPos relativeSeat) {
        BlockPos seat = helper.absolutePos(relativeSeat);
        helper.getLevel().setBlockAndUpdate(seat.below(), Blocks.BONE_BLOCK.defaultBlockState());
        return seat;
    }

    private static ItemEntity item(GameTestHelper helper, BlockPos pos, net.minecraft.world.item.Item item, int count) {
        Vec3 location = Vec3.atBottomCenterOf(pos).add(0, .15, 0);
        ItemEntity entity = new ItemEntity(helper.getLevel(), location.x, location.y, location.z, new ItemStack(item, count));
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPickUpDelay(0);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }
}
