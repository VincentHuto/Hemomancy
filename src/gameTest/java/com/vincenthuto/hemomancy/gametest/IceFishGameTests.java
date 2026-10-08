package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ice_fish_validation")
@PrefixGameTestTemplate(false)
public final class IceFishGameTests {
    @GameTest(template = "pool", timeoutTicks = 40, batch = "ice_fish_registration")
    public static void coldSpawnTablesEggAndPureAquaticProfileAreLoaded(GameTestHelper helper) {
        var type = EntityInit.ice_fish.get();
        helper.assertTrue(type.getCategory() == MobCategory.WATER_AMBIENT && type.is(EntityTypeTags.AQUATIC)
                && type.is(EntityTypeTags.CAN_BREATHE_UNDER_WATER) && type.is(EntityInit.SPECIMEN_JAR_CAPTURABLE),
                "Missing native aquatic category or capture tags");
        helper.assertTrue(ItemInit.spawn_egg_ice_fish.get().getType(new ItemStack(ItemInit.spawn_egg_ice_fish.get())) == type,
                "Ice Fish spawn egg points to the wrong entity");
        var profile = BloodProfileData.profile(type, false);
        helper.assertTrue(profile.tendencies().isEmpty() && !profile.requiresLivingSyringe()
                && profile.properties().contains(Hemomancy.rloc("blood_properties/aquatic")), "Ice Fish blood must remain tendency-free and aquatic");
        var biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        for (var key : java.util.List.of(Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.FROZEN_OCEAN,
                Biomes.DEEP_FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.ICE_SPIKES)) {
            var packs = biomes.getHolderOrThrow(key).value().getMobSettings().getMobs(MobCategory.WATER_AMBIENT)
                    .unwrap().stream().filter(pack -> pack.type == type).toList();
            helper.assertTrue(packs.size() == 1 && packs.getFirst().minCount == 3 && packs.getFirst().maxCount == 5,
                    "Missing or incorrect Ice Fish school in " + key.location());
        }
        for (var key : java.util.List.of(Biomes.OCEAN, Biomes.WARM_OCEAN, Biomes.RIVER))
            helper.assertTrue(biomes.getHolderOrThrow(key).value().getMobSettings().getMobs(MobCategory.WATER_AMBIENT)
                    .unwrap().stream().noneMatch(pack -> pack.type == type), "Ice Fish added to a warm habitat: " + key.location());
        helper.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40, batch = "ice_fish_spawn")
    public static void coldWaterSupportsSpawnsUnderIceAndRejectsWarmWater(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 6));
        pool(helper, pos);
        for (var biome : java.util.List.of(Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.FROZEN_OCEAN,
                Biomes.DEEP_FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.ICE_SPIKES)) {
            biome(helper, pos, biome.location().toString());
            helper.assertTrue(SpawnPlacements.checkSpawnRules(type(), helper.getLevel(), MobSpawnType.NATURAL, pos, helper.getLevel().random),
                    "Ice Fish rejected cold water in " + biome.location());
            helper.assertTrue(SpawnPlacements.isSpawnPositionOk(type(), helper.getLevel(), pos), "Native water placement rejected submerged fish");
        }
        biome(helper, pos, "minecraft:warm_ocean");
        helper.assertTrue(!SpawnPlacements.checkSpawnRules(type(), helper.getLevel(), MobSpawnType.NATURAL, pos, helper.getLevel().random),
                "Ice Fish accepted warm ocean water");
        biome(helper, pos, "minecraft:frozen_river");
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.assertTrue(!SpawnPlacements.checkSpawnRules(type(), helper.getLevel(), MobSpawnType.NATURAL, pos, helper.getLevel().random),
                "Ice Fish accepted a dry spawn");
        helper.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40, batch = "ice_fish_high_pool")
    public static void elevatedIceSpikesPoolsWorkButWaterloggedObstructionsDoNot(GameTestHelper helper) {
        var origin = helper.absolutePos(new BlockPos(6, 4, 6));
        var pos = new BlockPos(origin.getX(), 96, origin.getZ());
        pool(helper, pos); biome(helper, pos, "minecraft:ice_spikes");
        helper.assertTrue(SpawnPlacements.checkSpawnRules(type(), helper.getLevel(), MobSpawnType.NATURAL, pos, helper.getLevel().random)
                && SpawnPlacements.isSpawnPositionOk(type(), helper.getLevel(), pos), "A high Ice Spikes pool was blocked by a sea-level limit");
        helper.getLevel().setBlock(pos, Blocks.OAK_SLAB.defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true), 2);
        helper.assertTrue(!SpawnPlacements.checkSpawnRules(type(), helper.getLevel(), MobSpawnType.NATURAL, pos, helper.getLevel().random),
                "Waterlogged solid geometry should not count as swimming space");
        helper.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40, batch = "ice_fish_loot")
    public static void dropsExistingCleansingHemolymph(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 6));
        pool(helper, pos);
        Mob fish = fish(helper, pos);
        fish.hurt(fish.damageSources().generic(), 100);
        helper.runAfterDelay(25, () -> {
            int count = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(5),
                    item -> item.getItem().is(ItemInit.cleansing_hemolymph.get())).stream()
                    .mapToInt(item -> item.getItem().getCount()).sum();
            helper.assertTrue(count == 1, "Ice Fish must drop one existing Cleansing Hemolymph, got " + count);
            helper.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 80, batch = "ice_fish_school")
    public static void schoolsUsingFishNavigationAndSurvivesUnderIce(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 6));
        pool(helper, pos);
        var leader = (AbstractSchoolingFish) fish(helper, pos);
        var follower = (AbstractSchoolingFish) fish(helper, pos.east(2));
        follower.startFollowing(leader);
        var start = leader.position();
        helper.assertTrue(leader.getMaxSchoolSize() == 6, "Unexpected Ice Fish school size");
        helper.runAfterDelay(10, () -> helper.assertTrue(leader.getNavigation().moveTo(pos.getX()+3.5, pos.getY()+.5, pos.getZ()+.5, 1),
                "Ice Fish could not navigate submerged water"));
        helper.runAfterDelay(55, () -> {
            helper.assertTrue(leader.isAlive() && follower.isAlive() && leader.isInWater() && follower.isInWater(),
                    "Fish did not survive in submerged water under ice");
            helper.assertTrue(follower.isFollower(), "Ice Fish did not retain its school leader");
            helper.assertTrue(leader.position().distanceToSqr(start) > .1, "Ice Fish did not swim along its route");
            helper.assertTrue(leader.getTarget() == null && follower.getTarget() == null, "Ice Fish should be passive");
            helper.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 40, batch = "ice_fish_capture")
    public static void bucketDoesNotDeleteFishAndSpecimenJarPreservesIt(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(6, 4, 6)); pool(helper, pos);
        var fish = fish(helper, pos); fish.setNoAi(true);
        fish.setCustomName(Component.literal("Pale swimmer")); fish.setHealth(4);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        fish.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(fish.isAlive() && player.getMainHandItem().is(Items.WATER_BUCKET), "Unsupported bucket capture consumed the fish or bucket");
        var jar = SpecimenJarData.createStackWithSpecimen(SpecimenJarData.captureEntity(fish));
        var restored = (Mob) SpecimenJarData.releaseSpecimen(helper.getLevel(), pos.east(2), SpecimenJarData.getSpecimen(jar)).orElseThrow();
        helper.assertTrue(restored.getType() == fish.getType() && restored.getHealth() == 4
                && restored.isPersistenceRequired() && fish.getCustomName().equals(restored.getCustomName()), "Jar capture lost specimen identity or health");
        helper.assertTrue(SpecimenBestiaryDefinitions.isResearchSpecimen(Hemomancy.rloc("ice_fish"))
                && !SpecimenBestiaryDefinitions.createSurrenderReward(Hemomancy.rloc("ice_fish")).isEmpty(), "Living Bestiary does not accept Ice Fish");
        fish.discard(); restored.discard(); helper.succeed();
    }

    private static EntityType<?> type() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(Hemomancy.rloc("ice_fish"))
                .orElseThrow(() -> new AssertionError("Ice Fish entity is not registered"));
    }

    private static Mob fish(GameTestHelper helper, BlockPos pos) {
        Mob fish = (Mob) type().create(helper.getLevel());
        fish.setPos(Vec3.atCenterOf(pos)); fish.setPersistenceRequired();
        helper.getLevel().addFreshEntity(fish);
        return fish;
    }

    private static void pool(GameTestHelper helper, BlockPos pos) {
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-4,-3,-4), pos.offset(4,3,4)))
            helper.getLevel().setBlock(block, block.getY() == pos.getY()+3 ? Blocks.ICE.defaultBlockState()
                    : block.getY() == pos.getY()-3 ? Blocks.STONE.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
    }

    private static void biome(GameTestHelper helper, BlockPos pos, String biome) {
        var level = helper.getLevel();
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack()
                        .withLevel(level).withPermission(4).withSuppressedOutput(),
                "fillbiome " + (pos.getX()-4) + " " + Math.max(level.getMinBuildHeight(),pos.getY()-4) + " " + (pos.getZ()-4)
                        + " " + (pos.getX()+4) + " " + (pos.getY()+4) + " " + (pos.getZ()+4) + " " + biome);
    }
}
