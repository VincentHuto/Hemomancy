package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("mob_drop_validation")
@PrefixGameTestTemplate(false)
public final class MobDropConsolidationGameTests {
    @GameTest(template = "pool")
    public static void marineSpawnPacksUseTheAquaticCategoryAndBoundedGroups(GameTestHelper h) {
        var type = EntityInit.hemojelly.get();
        h.assertTrue(type.getCategory() == MobCategory.WATER_CREATURE, "Hemojelly is still in the land creature cap");
        var biomes = h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        for (var key : java.util.List.of(net.minecraft.world.level.biome.Biomes.OCEAN,
                net.minecraft.world.level.biome.Biomes.WARM_OCEAN, net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN)) {
            var packs = biomes.getHolderOrThrow(key).value().getMobSettings().getMobs(MobCategory.WATER_CREATURE)
                    .unwrap().stream().filter(pack -> pack.type == type).toList();
            h.assertTrue(packs.size() == 1 && packs.getFirst().getWeight().asInt() == 4
                    && packs.getFirst().minCount == 1 && packs.getFirst().maxCount == 2, "Incorrect marine Hemojelly spawn pack");
        }
        for (var key : java.util.List.of(net.minecraft.world.level.biome.Biomes.COLD_OCEAN,
                net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN, net.minecraft.world.level.biome.Biomes.RIVER))
            h.assertTrue(biomes.getHolderOrThrow(key).value().getMobSettings().getMobs(MobCategory.WATER_CREATURE)
                    .unwrap().stream().noneMatch(pack -> pack.type == type), "Hemojelly leaked into a cold/freshwater habitat");
        var depths = biomes.get(com.vincenthuto.hemomancy.Hemomancy.rloc("mycelial_depths"));
        for (var category : MobCategory.values())
            h.assertTrue(depths.getMobSettings().getMobs(category).unwrap().stream().noneMatch(pack -> pack.type == type),
                    "Old Mycelial Depths land spawn remains");
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 200)
    public static void savedCaptureCannotResumeAgainstTheOldPrey(GameTestHelper h) {
        var pos = pool(h);
        h.startSequence().thenWaitUntil(() -> h.assertTrue(h.getLevel().areEntitiesLoaded(
                net.minecraft.world.level.ChunkPos.asLong(pos)), "Pool must be entity loaded"))
                .thenExecute(() -> {
                    var jelly = EntityInit.hemojelly.get().create(h.getLevel());
                    jelly.moveTo(pos.getX()+.5,pos.getY()-.625,pos.getZ()+.5); h.getLevel().addFreshEntity(jelly);
                    var fish = EntityType.COD.create(h.getLevel()); fish.setNoAi(true); fish.setNoGravity(true);
                    fish.moveTo(pos.getX()+.5,pos.getY()-1.5,pos.getZ()+.5); h.getLevel().addFreshEntity(fish);
                    h.startSequence().thenWaitUntil(() -> h.assertTrue(jelly.captureTicks() > 0, "Capture never began"))
                            .thenExecute(() -> {
                                var saved = new net.minecraft.nbt.CompoundTag(); jelly.saveWithoutId(saved); jelly.discard();
                                var restored = EntityInit.hemojelly.get().create(h.getLevel()); restored.load(saved);
                                h.getLevel().addFreshEntity(restored);
                                h.assertTrue(restored.captureTicks() == 0, "Capture target survived reload");
                                h.runAfterDelay(55, () -> {
                                    h.assertTrue(fish.tickCount >= 40 && fish.getHealth() == fish.getMaxHealth(), "Reload resumed or immediately repeated the bite");
                                    fish.discard(); restored.discard(); h.succeed();
                                });
                            });
                });
    }

    @GameTest(template = "pool", timeoutTicks = 200)
    public static void hemojellyFloatsAtSurfaceAndFeedsOnSmallFish(GameTestHelper h) {
        var pos = pool(h);
        h.startSequence().thenWaitUntil(() -> h.assertTrue(h.getLevel().areEntitiesLoaded(
                net.minecraft.world.level.ChunkPos.asLong(pos)), "Pool must be entity loaded"))
                .thenExecute(() -> {
        var jelly = EntityInit.hemojelly.get().create(h.getLevel());
        h.assertTrue(((Entity) jelly) instanceof WaterAnimal, "Hemojelly must use aquatic movement");
        jelly.moveTo(pos.getX()+.5, pos.getY()-.625, pos.getZ()+.5);
        h.getLevel().addFreshEntity(jelly);
        var fish = EntityType.COD.create(h.getLevel()); fish.setNoAi(true); fish.setNoGravity(true);
        fish.moveTo(pos.getX()+.5, pos.getY()-1.5, pos.getZ()+.5);
        h.getLevel().addFreshEntity(fish);
        h.runAfterDelay(65, () -> {
            h.assertTrue(fish.getHealth() < fish.getMaxHealth(), "Submerged prey was never captured and bitten: jellyTicks="+jelly.tickCount+" fishTicks="+fish.tickCount+" capture="+jelly.captureTicks()+" jelly="+jelly.position()+" fish="+fish.position()+" water="+jelly.isInWater()+" LOS="+jelly.hasLineOfSight(fish));
            h.assertTrue(Math.abs(jelly.getY()-(pos.getY()-.625)) < .35, "Bell left the water surface");
            fish.discard(); jelly.discard(); h.succeed();
        });
        });
    }

    @GameTest(template = "pool", timeoutTicks = 200)
    public static void hemojellyReleasesRemovedPreyAndCannotBiteThroughStone(GameTestHelper h) {
        var pos = pool(h);
        h.startSequence().thenWaitUntil(() -> h.assertTrue(h.getLevel().areEntitiesLoaded(
                net.minecraft.world.level.ChunkPos.asLong(pos)), "Pool must be entity loaded"))
                .thenExecute(() -> {
        var jelly = EntityInit.hemojelly.get().create(h.getLevel());
        jelly.moveTo(pos.getX()+.5, pos.getY()-.625, pos.getZ()+.5); h.getLevel().addFreshEntity(jelly);
        var fish = EntityType.COD.create(h.getLevel()); fish.setNoAi(true); fish.setNoGravity(true);
        fish.moveTo(pos.getX()+.5, pos.getY()-1.5, pos.getZ()+.5); h.getLevel().addFreshEntity(fish);
        h.runAfterDelay(20, () -> {
            fish.discard();
            h.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            var second = EntityType.COD.create(h.getLevel()); second.setNoAi(true); second.setNoGravity(true);
            second.moveTo(pos.getX()+.5, pos.getY()-2.5, pos.getZ()+.5); h.getLevel().addFreshEntity(second);
            h.runAfterDelay(55, () -> {
                h.assertTrue(second.tickCount >= 40 && jelly.tickCount >= 60, "Release control must receive entity ticks");
                h.assertTrue(second.getHealth() == second.getMaxHealth(), "Capture crossed an obstruction");
                second.discard(); jelly.discard(); h.succeed();
            });
        });
        });
    }

    @GameTest(template = "pool", timeoutTicks = 200)
    public static void siphonophoreKeepsItsBoundedFeedingCooldown(GameTestHelper h) {
        var pos = pool(h);
        h.startSequence().thenWaitUntil(() -> h.assertTrue(h.getLevel().areEntitiesLoaded(
                net.minecraft.world.level.ChunkPos.asLong(pos)), "Pool must be entity loaded"))
                .thenExecute(() -> {
        var colony = EntityInit.siphonophore.get().create(h.getLevel());
        colony.moveTo(Vec3.atCenterOf(pos.below(2))); h.getLevel().addFreshEntity(colony);
        var fish = EntityType.COD.create(h.getLevel()); fish.setNoAi(true); fish.setNoGravity(true);
        fish.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20); fish.setHealth(20);
        fish.moveTo(Vec3.atCenterOf(pos.below(3))); h.getLevel().addFreshEntity(fish);
        h.runAfterDelay(55, () -> {
            h.assertTrue(fish.getHealth() == 16, "Siphonophore must inflict one four-damage bite within its cooldown: health="+fish.getHealth()+" colonyTicks="+colony.tickCount+" fishTicks="+fish.tickCount+" colony="+colony.position()+" fish="+fish.position()+" LOS="+colony.hasLineOfSight(fish));
            fish.discard(); colony.discard(); h.succeed();
        });
        });
    }

    @GameTest(template = "pool")
    public static void foodRecipesAndNutritionLoad(GameTestHelper h) {
        for (var pair : java.util.List.of(new ItemStack[]{new ItemStack(ItemInit.raw_pelagic_herring.get()), new ItemStack(ItemInit.cooked_pelagic_herring.get())},
                new ItemStack[]{new ItemStack(ItemInit.raw_venison.get()), new ItemStack(ItemInit.cooked_venison.get())})) {
            var input = new SingleRecipeInput(pair[0]);
            for (var type : java.util.List.of(RecipeType.SMELTING, RecipeType.SMOKING)) {
                var recipe = h.getLevel().getRecipeManager().getRecipeFor(type, input, h.getLevel()).orElseThrow().value();
                h.assertTrue(recipe.getResultItem(h.getLevel().registryAccess()).is(pair[1].getItem()), "Wrong cooked food");
            }
            h.assertTrue(pair[1].get(net.minecraft.core.component.DataComponents.FOOD).nutrition()
                    > pair[0].get(net.minecraft.core.component.DataComponents.FOOD).nutrition(), "Cooking must improve nutrition");
        }
        h.succeed();
    }

    private static BlockPos pool(GameTestHelper h) {
        var origin = h.absolutePos(BlockPos.ZERO);
        var pos = new BlockPos(1_000_008 + (origin.getX() & 65535) * 4, 20,
                1_000_008 + (origin.getZ() & 65535) * 4);
        var chunk = new net.minecraft.world.level.ChunkPos(pos);
        var owner = java.util.UUID.randomUUID();
        var ticket = net.minecraft.server.level.TicketType.create("mob_drop_fixture", java.util.Comparator.comparing(java.util.UUID::toString));
        h.getLevel().getChunkSource().addRegionTicket(ticket, chunk, 2, owner, true);
        Runnable cleanup = () -> {
            h.getLevel().getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(pos).inflate(8)).forEach(Mob::discard);
            h.getLevel().getChunkSource().removeRegionTicket(ticket, chunk, 2, owner, true);
        };
        h.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo info) { }
            public void testPassed(GameTestInfo info, GameTestRunner runner) { cleanup.run(); }
            public void testFailed(GameTestInfo info, GameTestRunner runner) { cleanup.run(); }
            public void testAddedForRerun(GameTestInfo old, GameTestInfo next, GameTestRunner runner) { }
        });
        for (var p : BlockPos.betweenClosed(pos.offset(-4,-6,-4),pos.offset(4,2,4))) {
            boolean wall = Math.abs(p.getX()-pos.getX())==4 || Math.abs(p.getZ()-pos.getZ())==4 || p.getY()==pos.getY()-6;
            h.getLevel().setBlockAndUpdate(p, (wall ? Blocks.STONE : p.getY()<pos.getY() ? Blocks.WATER : Blocks.AIR).defaultBlockState());
        }
        return pos;
    }
}

