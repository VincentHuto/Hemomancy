package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.BombardierState;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.PhlegethonticBombardier;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PhlegethonticBombardierGameTests {
    private static final String ROOM = "phlegethontic_test_room";

    @GameTest(batch = "phlegethontic_bombardier", template = ROOM, timeoutTicks = 20)
    public static void naturalSpawnUsesNetherMobPool(GameTestHelper helper) {
        helper.assertTrue(EntityInit.phlegethontic_bombardier.get().getCategory() == MobCategory.MONSTER,
                "Bombardier must use the Nether hostile cap instead of the Strider-filled creature cap");
        var basin = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(BiomeInit.PHLEGETHONTIC_BASIN).value();
        boolean registered = basin.getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                .anyMatch(entry -> entry.type == EntityInit.phlegethontic_bombardier.get());
        helper.assertTrue(registered, "Phlegethontic Basin mob settings must include Bombardiers");
        helper.succeed();
    }

    @GameTest(batch = "phlegethontic_bombardier", template = ROOM, timeoutTicks = 20)
    public static void spawnEggIsRegisteredForBombardier(GameTestHelper helper) {
        var egg = BuiltInRegistries.ITEM.get(Hemomancy.rloc("spawn_egg_phlegethontic_bombardier"));
        helper.assertTrue(egg instanceof DeferredSpawnEggItem,
                "Bombardier spawn egg must be registered as a DeferredSpawnEggItem");
        helper.assertTrue(DeferredSpawnEggItem.deferredOnlyById(EntityInit.phlegethontic_bombardier.get()) == egg,
                "Bombardier spawn egg must resolve back to the Bombardier entity type");
        helper.succeed();
    }

    @GameTest(batch = "phlegethontic_bombardier", template = ROOM, timeoutTicks = 20)
    public static void naturalSpawnGatesAcceptBasinOvergrowth(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(8, 4, 8));
        var basin = level.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(BiomeInit.PHLEGETHONTIC_BASIN);
        level.getChunk(center).fillBiomesFromNoise((x, y, z, sampler) -> basin,
                level.getChunkSource().randomState().sampler());
        level.setBlockAndUpdate(center.below(), BlockInit.escharian_overgrowth.get().defaultBlockState());
        level.setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(center.above(), Blocks.AIR.defaultBlockState());

        var type = EntityInit.phlegethontic_bombardier.get();
        helper.assertTrue(SpawnPlacements.hasPlacement(type), "Bombardier must have a registered spawn placement");
        helper.assertTrue(SpawnPlacements.getPlacementType(type) == SpawnPlacementTypes.ON_GROUND,
                "Bombardier natural spawning must use the ground placement type");
        helper.assertTrue(SpawnPlacements.isSpawnPositionOk(type, level, center),
                "Basin overgrowth must satisfy the Bombardier ground placement");
        helper.assertTrue(SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, center, level.random),
                "Basin overgrowth must satisfy the Bombardier NATURAL spawn predicate");
        helper.succeed();
    }

    @GameTest(batch = "phlegethontic_bombardier", template = ROOM, timeoutTicks = 20)
    public static void naturalSpawnAcceptsSafeGroundNearOvergrowth(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos spawn = helper.absolutePos(new BlockPos(8, 4, 8));
        BlockPos growth = helper.absolutePos(new BlockPos(13, 4, 8));
        var basin = level.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(BiomeInit.PHLEGETHONTIC_BASIN);
        level.getChunk(spawn).fillBiomesFromNoise((x, y, z, sampler) -> basin,
                level.getChunkSource().randomState().sampler());
        level.setBlockAndUpdate(spawn.below(), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(spawn, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(spawn.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(growth, BlockInit.escharian_overgrowth.get().defaultBlockState());

        var type = EntityInit.phlegethontic_bombardier.get();
        helper.assertTrue(SpawnPlacements.isSpawnPositionOk(type, level, spawn),
                "ordinary solid ground near a colony must satisfy the ground placement");
        helper.assertTrue(SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, spawn, level.random),
                "Bombardiers must naturally spawn on safe ground near Escharian Overgrowth");
        helper.succeed();
    }

    @GameTest(batch = "phlegethontic_bombardier", template = ROOM, timeoutTicks = 20)
    public static void naturalSpawnKeepsNearbyOvergrowthTerritory(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos spawn = helper.absolutePos(new BlockPos(8, 4, 8));
        BlockPos growth = helper.absolutePos(new BlockPos(13, 4, 8));
        level.setBlockAndUpdate(growth, BlockInit.escharian_overgrowth.get().defaultBlockState());

        var bombardier = EntityInit.phlegethontic_bombardier.get().create(level);
        helper.assertTrue(bombardier != null, "Bombardier entity creation failed");
        bombardier.moveTo(spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5, 0, 0);
        bombardier.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.NATURAL, null);

        helper.assertTrue(!bombardier.isRemoved(),
                "a natural Bombardier near Overgrowth must not discard itself during finalization");
        helper.assertTrue(bombardier.getTerritoryAnchor().filter(growth::equals).isPresent(),
                "a natural Bombardier must anchor to the nearby Overgrowth colony");
        helper.succeed();
    }

    @GameTest(batch = "phlegethontic_bombardier", template = ROOM, timeoutTicks = 30)
    public static void harmAlertsOnlyTheStoredOutcropping(GameTestHelper helper) {
        var first = helper.spawn(EntityInit.phlegethontic_bombardier.get(), new BlockPos(8, 4, 8));
        var same = helper.spawn(EntityInit.phlegethontic_bombardier.get(), new BlockPos(10, 4, 8));
        var separate = helper.spawn(EntityInit.phlegethontic_bombardier.get(), new BlockPos(16, 4, 8));
        Cow provoker = helper.spawn(EntityType.COW, new BlockPos(8, 4, 13));
        BlockPos firstAnchor = helper.absolutePos(new BlockPos(8, 3, 8));
        first.setTerritoryAnchor(firstAnchor);
        same.setTerritoryAnchor(firstAnchor);
        separate.setTerritoryAnchor(helper.absolutePos(new BlockPos(16, 3, 8)));
        helper.assertTrue(first.getTarget() == null && same.getTarget() == null && separate.getTarget() == null,
                "unprovoked bombardiers must remain neutral");
        first.hurt(helper.getLevel().damageSources().mobAttack(provoker), 1);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(same.getTarget() == provoker, "same-outcropping colony member must retaliate");
            helper.assertTrue(separate.getTarget() == null, "different outcropping must remain neutral");
            helper.succeed();
        });
    }

    @GameTest(batch = "phlegethontic_bombardier", template = ROOM, timeoutTicks = 80)
    public static void telegraphedSweepHitsFrontNotRearAndNeverChangesGrowth(GameTestHelper helper) {
        BlockPos support = new BlockPos(8, 3, 8);
        helper.setBlock(support, BlockInit.escharian_overgrowth.get());
        var attacker = helper.spawn(EntityInit.phlegethontic_bombardier.get(), new BlockPos(8, 4, 8));
        Cow front = helper.spawn(EntityType.COW, new BlockPos(8, 4, 14));
        Cow rear = helper.spawn(EntityType.COW, new BlockPos(8, 4, 5));
        front.setNoAi(true); rear.setNoAi(true);
        front.setNoGravity(true); rear.setNoGravity(true);
        attacker.setTerritoryAnchor(helper.absolutePos(support));
        attacker.hurt(helper.getLevel().damageSources().mobAttack(front), 1);
        attacker.setBombardierState(BombardierState.WINDUP);
        float frontHealth = front.getHealth(), rearHealth = rear.getHealth();
        var before = helper.getBlockState(support);
        helper.runAfterDelay(25, () -> helper.assertTrue(front.getHealth() == frontHealth,
                "wind-up cannot deal damage"));
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(front.getHealth() < frontHealth, "front target must be hit by the sweep");
            helper.assertTrue(rear.getHealth() == rearHealth, "rear target must not be hit");
            helper.assertTrue(helper.getBlockState(support).equals(before), "flame lifecycle changed Escharian Overgrowth");
            helper.assertTrue(attacker.getBombardierState() == BombardierState.COOLING,
                    "completed attack must enter cooling");
            helper.succeed();
        });
    }
}
