package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("mnemonic_whale_validation")
@PrefixGameTestTemplate(false)
public final class MnemonicWhaleGameTests {
    private MnemonicWhaleGameTests() {
    }

    @GameTest(template = "pool")
    public static void reefLoadsBothPreySpawnSources(GameTestHelper helper) {
        var reef = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME)
                .get(ResourceLocation.parse("hemomancy:erythrocoral_reef"));
        helper.assertTrue(reef != null, "Reef biome did not load");
        var spawns = reef.getMobSettings().getMobs(MobCategory.WATER_CREATURE).unwrap();
        helper.assertTrue(spawns.stream().anyMatch(spawn -> spawn.type == EntityType.SQUID),
                "Reef lost its natural squid prey");
        helper.assertTrue(spawns.stream().anyMatch(spawn -> spawn.type == EntityInit.prism_cuttle.get()
                        && spawn.getWeight().asInt() == 14 && spawn.minCount == 1 && spawn.maxCount == 3),
                "Reef did not receive the existing cuttle spawn modifier");
        helper.succeed();
    }

    @GameTest(template = "pool")
    public static void ambergrisPickupTeachesTheFeedingPage(GameTestHelper helper) {
        var page = ResourceLocation.parse("hemomancy:libersanguinium/the_infection/pages/mnemonic_ambergris");
        helper.assertTrue(com.vincenthuto.hutoslib.common.book.knowledge.BookEntryRegistry.entriesForItem(
                        ResourceLocation.parse("hemomancy:mnemonic_ambergris")).contains(page),
                "Ambergris pickup does not unlock its feeding guidance");
        helper.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 240)
    public static void huntsSquidAndDropsOneAmbergris(GameTestHelper helper) {
        hunt(helper, EntityType.SQUID);
    }

    @GameTest(template = "pool", timeoutTicks = 240)
    public static void huntsGlowSquidAndDropsOneAmbergris(GameTestHelper helper) {
        hunt(helper, EntityType.GLOW_SQUID);
    }

    @GameTest(template = "pool", timeoutTicks = 240)
    public static void huntsPrismCuttleAndDropsOneAmbergris(GameTestHelper helper) {
        hunt(helper, EntityInit.prism_cuttle.get());
    }

    @GameTest(template = "pool", timeoutTicks = 240)
    public static void huntsInShallowWaterWithoutForcedDiving(GameTestHelper helper) {
        pool(helper, poolBaseY(helper) + 12);
        MnemonicWhaleEntity whale = (MnemonicWhaleEntity) prey(helper,
                EntityInit.mnemonic_whale.get(), 5, 17, 12, false);
        Mob prey = prey(helper, EntityType.SQUID, 12, 17, 12);
        assertMeal(helper, whale, prey);
    }

    @GameTest(template = "pool", timeoutTicks = 100)
    public static void strongerPreyNeedsMultipleBitesBeforeReward(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        bottleCooldown(helper, whale);
        Mob prey = prey(helper, EntityType.SQUID, 5, 5, 12);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(24);
        prey.setHealth(24);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(prey.getHealth() == 12, "First bite was not 12 damage"))
                .thenExecute(() -> helper.assertTrue(ambergris(helper) == 0, "Nonlethal bite rewarded ambergris"))
                .thenIdle(15)
                .thenExecute(() -> helper.assertTrue(prey.isAlive(), "Whale ignored its 20-tick bite interval"))
                .thenWaitUntil(() -> helper.assertTrue(!prey.isAlive() && ambergris(helper) == 1,
                        "Lethal second bite did not reward exactly one ambergris"))
                .thenSucceed();
    }

    @GameTest(template = "pool", timeoutTicks = 120)
    public static void abandonsPreyThatLeavesTheWater(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        bottleCooldown(helper, whale);
        Mob prey = prey(helper, EntityType.SQUID, 18, 5, 12);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(whale.getTarget() == prey, "Whale did not acquire submerged prey");
            prey.setPos(prey.getX(), helper.getLevel().getSeaLevel() + 5, prey.getZ());
        });
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(!prey.isInWater() && whale.getTarget() == null && ambergris(helper) == 0,
                    "Whale continued hunting prey that left the water");
            helper.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 240)
    public static void huntsLeashedSquid(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        Mob prey = prey(helper, EntityType.SQUID, 12, 5, 12);
        BlockPos fence = helper.absolutePos(new BlockPos(12, poolBaseY(helper) + 4, 12));
        helper.getLevel().setBlockAndUpdate(fence, Blocks.OAK_FENCE.defaultBlockState());
        helper.assertTrue(prey.canBeLeashed(), "Squid cannot be transported on a lead");
        prey.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(helper.getLevel(), fence), true);
        assertMeal(helper, whale, prey);
    }

    @GameTest(template = "pool", timeoutTicks = 240)
    public static void huntsCuttleReleasedByBreakingSpecimenJar(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        PrismCuttleEntity captured = EntityInit.prism_cuttle.get().create(helper.getLevel());
        helper.assertTrue(captured != null, "Cuttle creation failed");
        captured.setNoAi(true);
        BlockPos jarPos = helper.absolutePos(new BlockPos(12, poolBaseY(helper) + 5, 12));
        helper.getLevel().setBlockAndUpdate(jarPos, BlockInit.specimen_jar.get().defaultBlockState());
        SpecimenJarBlockEntity jar = (SpecimenJarBlockEntity) helper.getLevel().getBlockEntity(jarPos);
        jar.setSpecimen(SpecimenJarData.captureEntity(captured));
        helper.getLevel().destroyBlock(jarPos, true);
        var released = helper.getLevel().getEntitiesOfClass(PrismCuttleEntity.class, bounds(helper));
        helper.assertTrue(released.size() == 1, "Breaking the occupied jar did not release exactly one cuttle");
        assertMeal(helper, whale, released.getFirst());
    }

    @GameTest(template = "pool", timeoutTicks = 360)
    public static void consecutiveMealsDoNotResetBottleCooldown(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 3));
        whale.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getInventory().countItem(ItemInit.mnemonic_ambergris.get()) == 1,
                "Initial bottle sampling did not establish a cooldown");
        Mob first = prey(helper, EntityType.SQUID, 7, 5, 12);
        helper.runAfterDelay(90, () -> {
            helper.assertTrue(!first.isAlive() && ambergris(helper) == 1, "First meal failed during bottle cooldown");
            Mob second = prey(helper, EntityInit.prism_cuttle.get(),
                    whale.blockPosition().getX() - helper.absolutePos(BlockPos.ZERO).getX(),
                    whale.blockPosition().getY() - helper.absolutePos(BlockPos.ZERO).getY() - poolBaseY(helper),
                    whale.blockPosition().getZ() - helper.absolutePos(BlockPos.ZERO).getZ());
            helper.runAfterDelay(180, () -> {
                helper.assertTrue(!second.isAlive() && ambergris(helper) == 2,
                        "Consecutive meals were throttled or duplicated: drops=" + ambergris(helper)
                                + ", whale=" + whale.position() + ", prey=" + second.position()
                                + ", health=" + second.getHealth() + ", wet=" + second.isInWater()
                                + ", target=" + whale.getTarget() + ", navigation=" + whale.getNavigation().isDone());
                whale.interact(player, InteractionHand.MAIN_HAND);
                helper.assertTrue(player.getInventory().countItem(ItemInit.mnemonic_ambergris.get()) == 1
                                && player.getMainHandItem().getCount() == 2,
                        "Feeding cleared the existing bottle cooldown");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void rejectedBitesAndOtherKillersGiveNoAmbergris(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        whale.setNoAi(true);
        bottleCooldown(helper, whale);
        Mob prey = prey(helper, EntityType.SQUID, 5, 5, 12);
        prey.setInvulnerable(true);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(!whale.doHurtTarget(prey) && prey.isAlive() && ambergris(helper) == 0,
                    "Rejected damage yielded ambergris");
            prey.setInvulnerable(false);
            prey.hurt(helper.getLevel().damageSources().generic(), 100);
            helper.assertTrue(!prey.isAlive(), "Control damage did not kill prey");
            helper.assertTrue(!whale.doHurtTarget(prey) && ambergris(helper) == 0,
                    "Another killer or a dead prey yielded ambergris");
            helper.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void successfulBiteRewardsOnceAboveWhaleAndKeepsPreyLoot(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        whale.setNoAi(true);
        bottleCooldown(helper, whale);
        Mob prey = prey(helper, EntityType.SQUID, 5, 5, 12);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(whale.doHurtTarget(prey) && !prey.isAlive(), "Bite did not kill squid");
            helper.assertTrue(ambergris(helper) == 1, "Lethal bite did not yield exactly one ambergris");
            var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds(helper));
            ItemEntity reward = drops.stream().filter(item -> item.getItem().is(ItemInit.mnemonic_ambergris.get()))
                    .findFirst().orElseThrow();
            helper.assertTrue(reward.getY() >= whale.getBoundingBox().maxY,
                    "Feeding reward spawned below the whale's back");
            helper.assertTrue(drops.stream().anyMatch(item -> item.getItem().is(Items.INK_SAC)),
                    "Whale consumption suppressed ordinary squid loot");
            helper.assertTrue(!whale.doHurtTarget(prey) && ambergris(helper) == 1,
                    "A repeated bite duplicated a dead prey's reward");
            helper.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 120)
    public static void ignoresUnrelatedCreaturesAndDryPrey(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        bottleCooldown(helper, whale);
        Mob fish = prey(helper, EntityType.TROPICAL_FISH, 6, 5, 12);
        Mob drySquid = prey(helper, EntityType.SQUID, 7, 11, 12);
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(fish.isAlive() && whale.getTarget() == null && ambergris(helper) == 0,
                    "Whale hunted unrelated creatures or dry prey");
            helper.assertTrue(!whale.doHurtTarget(fish) && !whale.doHurtTarget(drySquid),
                    "Whale accepted an unrelated or dry bite target");
            helper.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 240)
    public static void cannotBiteThroughWallsAndAbandonsUnreachablePrey(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        bottleCooldown(helper, whale);
        Mob prey = prey(helper, EntityType.SQUID, 7, 5, 12);
        whale.setPos(helper.absolutePos(BlockPos.ZERO).getX() + 4.25D, whale.getY(), whale.getZ());
        prey.setPos(helper.absolutePos(BlockPos.ZERO).getX() + 7.2D, prey.getY(), prey.getZ());
        for (BlockPos pos : BlockPos.betweenClosed(6, 1, 0, 6, 9, 23)) {
            helper.setBlock(pos.offset(0, poolBaseY(helper), 0), Blocks.GLASS_PANE);
        }
        helper.runAfterDelay(3, () -> helper.assertTrue(!whale.doHurtTarget(prey),
                "Whale bit prey through the wall"));
        helper.startSequence().thenIdle(40).thenWaitUntil(() -> {
            helper.assertTrue(prey.isAlive() && ambergris(helper) == 0,
                    "Unreachable prey was consumed through a wall");
            helper.assertTrue(whale.getTarget() == null, "Whale kept an unreachable hunt target: whale="
                    + whale.position() + ", prey=" + prey.position() + ", target=" + whale.getTarget()
                    + ", path=" + whale.getNavigation().getPath());
        }).thenSucceed();
    }

    @GameTest(template = "pool", timeoutTicks = 400)
    public static void abandonsRemovedPreyAndResumesCruising(GameTestHelper helper) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        bottleCooldown(helper, whale);
        Mob prey = prey(helper, EntityType.SQUID, 18, 5, 12);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(whale.getTarget() == prey, "Whale did not acquire available prey: whale="
                    + whale.position() + ", prey=" + prey.position() + ", wet=" + prey.isInWater()
                    + ", target=" + whale.getTarget());
            prey.discard();
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(prey.isRemoved() && whale.getTarget() == null, "Removed prey remained targeted");
            helper.assertTrue(whale.goalSelector.getAvailableGoals().stream().anyMatch(goal ->
                            goal.isRunning() && goal.getGoal().getClass().getSimpleName().equals("MnemonicWhaleCruiseGoal")),
                    "Whale did not resume cruising after losing prey");
        });
    }

    private static void hunt(GameTestHelper helper, EntityType<? extends Mob> type) {
        pool(helper);
        MnemonicWhaleEntity whale = whale(helper);
        bottleCooldown(helper, whale);
        Mob prey = prey(helper, type, 12, 5, 12);
        assertMeal(helper, whale, prey);
    }

    private static void assertMeal(GameTestHelper helper, MnemonicWhaleEntity whale, LivingEntity prey) {
        bottleCooldown(helper, whale);
        helper.succeedWhen(() -> {
            helper.assertTrue(!prey.isAlive(), "Whale did not pursue and consume supplied prey: whale="
                    + whale.position() + ", prey=" + prey.position() + ", target=" + whale.getTarget()
                    + ", navigation=" + whale.getNavigation().isDone());
            helper.assertTrue(ambergris(helper) == 1, "Meal did not produce exactly one ambergris");
            helper.assertTrue(whale.getX() > helper.absolutePos(new BlockPos(7, 0, 0)).getX(),
                    "Whale killed distant prey without pursuing it");
        });
    }

    private static MnemonicWhaleEntity whale(GameTestHelper helper) {
        return (MnemonicWhaleEntity) prey(helper, EntityInit.mnemonic_whale.get(), 5, 5, 12, false);
    }

    private static Mob prey(GameTestHelper helper, EntityType<? extends Mob> type, int x, int y, int z) {
        return prey(helper, type, x, y, z, true);
    }

    private static Mob prey(GameTestHelper helper, EntityType<? extends Mob> type, int x, int y, int z,
                            boolean noAi) {
        Mob entity = type.create(helper.getLevel());
        helper.assertTrue(entity != null, "Entity creation failed");
        entity.setNoAi(noAi);
        entity.setNoGravity(noAi);
        entity.setPersistenceRequired();
        entity.moveTo(helper.absolutePos(new BlockPos(x, y + poolBaseY(helper), z)).getCenter());
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static void bottleCooldown(GameTestHelper helper, MnemonicWhaleEntity whale) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        whale.interact(player, InteractionHand.MAIN_HAND);
    }

    private static int ambergris(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds(helper)).stream()
                .filter(item -> item.getItem().is(ItemInit.mnemonic_ambergris.get()))
                .mapToInt(item -> item.getItem().getCount()).sum();
    }

    private static AABB bounds(GameTestHelper helper) {
        return AABB.encapsulatingFullBlocks(helper.absolutePos(BlockPos.ZERO),
                helper.absolutePos(new BlockPos(23, 139, 23)));
    }

    private static void pool(GameTestHelper helper) {
        pool(helper, poolBaseY(helper));
    }

    private static int poolBaseY(GameTestHelper helper) {
        return helper.getLevel().getSeaLevel() - 20 - helper.absolutePos(BlockPos.ZERO).getY();
    }

    private static void pool(GameTestHelper helper, int baseY) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 23, 10, 23)) {
            boolean wall = pos.getX() == 0 || pos.getX() == 23 || pos.getZ() == 0 || pos.getZ() == 23
                    || pos.getY() == 0 || pos.getY() == 10;
            helper.setBlock(pos.offset(0, baseY, 0), wall ? Blocks.STONE : Blocks.WATER);
        }
    }
}
