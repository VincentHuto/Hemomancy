package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("choir_keeper_validation")
@PrefixGameTestTemplate(false)
public final class ChoirKeeperGameTests {
    @GameTest(template = "empty", timeoutTicks = 100, batch = "choir_keeper")
    public static void switchesToAnotherEyeDuringFlight(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos flower = helper.absolutePos(new BlockPos(3, 4, 3));
        level.setBlockAndUpdate(flower.below(2), Blocks.END_STONE.defaultBlockState());
        level.setBlockAndUpdate(flower.below(), Blocks.CHORUS_PLANT.defaultBlockState());
        level.setBlockAndUpdate(flower, Blocks.CHORUS_FLOWER.defaultBlockState());
        BlockPos seat = flower.above();

        ChoirKeeperEntity keeper = EntityInit.choir_keeper.get().create(level);
        if (keeper == null) throw new AssertionError("Choir Keeper could not be created");
        keeper.setPos(Vec3.atBottomCenterOf(seat));
        keeper.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        level.addFreshEntity(keeper);

        Vec3 firstPos = Vec3.atBottomCenterOf(seat.east(3)).add(0.0D, 0.25D, 0.0D);
        ItemEntity firstEye = new ItemEntity(level, firstPos.x, firstPos.y, firstPos.z,
                new ItemStack(Items.ENDER_EYE));
        firstEye.setNoGravity(true);
        firstEye.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(firstEye);
        Vec3 secondPos = Vec3.atBottomCenterOf(seat.south(4)).add(0.0D, 0.25D, 0.0D);
        ItemEntity secondEye = new ItemEntity(level, secondPos.x, secondPos.y, secondPos.z,
                new ItemStack(Items.ENDER_EYE));
        secondEye.setNoGravity(true);
        secondEye.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(secondEye);

        helper.runAfterDelay(6, () -> {
            helper.assertTrue(keeper.isFlying() && firstEye.isAlive(),
                    "Keeper did not begin chasing the nearer eye");
            firstEye.discard();
        });
        helper.runAfterDelay(65, () -> {
            helper.assertTrue(!secondEye.isAlive(),
                    "Keeper returned to its perch instead of switching to the remaining eye");
            long pearls = level.getEntitiesOfClass(ItemEntity.class,
                    new AABB(seat).inflate(12.0D), item -> item.getItem().is(Items.ENDER_PEARL)).size();
            helper.assertTrue(pearls == 1, "Expected one pearl from the second eye, got " + pearls);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80, batch = "choir_keeper")
    public static void reservesOneFlowerPerKeeper(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos flower = helper.absolutePos(new BlockPos(3, 4, 3));
        level.setBlockAndUpdate(flower.below(2), Blocks.END_STONE.defaultBlockState());
        level.setBlockAndUpdate(flower.below(), Blocks.CHORUS_PLANT.defaultBlockState());
        level.setBlockAndUpdate(flower, Blocks.CHORUS_FLOWER.defaultBlockState());
        BlockPos seat = flower.above();
        var type = EntityInit.choir_keeper.get();

        ChoirKeeperEntity first = type.create(level);
        if (first == null) throw new AssertionError("First Choir Keeper could not be created");
        first.setPos(Vec3.atBottomCenterOf(seat));
        first.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        level.addFreshEntity(first);
        helper.assertTrue(!ChoirKeeperEntity.canSpawnHere(type, level, MobSpawnType.NATURAL,
                seat, RandomSource.create()), "Occupied flower allowed another natural spawn");

        ChoirKeeperEntity duplicate = type.create(level);
        if (duplicate == null) throw new AssertionError("Second Choir Keeper could not be created");
        duplicate.setPos(Vec3.atBottomCenterOf(seat));
        duplicate.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        helper.assertTrue(!duplicate.isAlive(), "Duplicate keeper remained on the occupied flower");

        BlockPos otherFlower = flower.east(4);
        level.setBlockAndUpdate(otherFlower.below(2), Blocks.END_STONE.defaultBlockState());
        level.setBlockAndUpdate(otherFlower.below(), Blocks.CHORUS_PLANT.defaultBlockState());
        level.setBlockAndUpdate(otherFlower, Blocks.CHORUS_FLOWER.defaultBlockState());
        BlockPos otherSeat = otherFlower.above();
        ChoirKeeperEntity second = type.create(level);
        if (second == null) throw new AssertionError("Third Choir Keeper could not be created");
        second.setPos(Vec3.atBottomCenterOf(seat));
        second.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        level.addFreshEntity(second);
        helper.assertTrue(second.blockPosition().equals(otherSeat),
                "Second keeper did not claim the free flower");
        CompoundTag oldSave = new CompoundTag();
        second.addAdditionalSaveData(oldSave);
        oldSave.putLong("Perch", seat.asLong());
        second.readAdditionalSaveData(oldSave);
        second.setPos(Vec3.atBottomCenterOf(seat));
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(first.isAlive() && second.isAlive(), "A keeper vanished after claiming its flower");
            helper.assertTrue(first.blockPosition().equals(seat) && second.blockPosition().equals(otherSeat),
                    "A duplicate saved perch was not reassigned to the free flower");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80, batch = "choir_keeper")
    public static void spawnsOnlyAtClearChorusPerch(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos flower = helper.absolutePos(new BlockPos(3, 4, 3));
        level.setBlockAndUpdate(flower.below(2), Blocks.END_STONE.defaultBlockState());
        level.setBlockAndUpdate(flower.below(), Blocks.CHORUS_PLANT.defaultBlockState());
        level.setBlockAndUpdate(flower, Blocks.CHORUS_FLOWER.defaultBlockState());
        BlockPos seat = flower.above();
        var type = EntityInit.choir_keeper.get();
        helper.assertTrue(!ChoirKeeperEntity.canSpawnHere(type, level, MobSpawnType.NATURAL,
                flower, RandomSource.create()), "Choir Keeper accepted a spawn inside a chorus flower");
        level.setBlockAndUpdate(seat.above(), Blocks.END_STONE.defaultBlockState());
        helper.assertTrue(!ChoirKeeperEntity.canSpawnHere(type, level, MobSpawnType.NATURAL,
                seat, RandomSource.create()), "Choir Keeper accepted an obstructed perch");
        level.setBlockAndUpdate(seat.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(seat.east(), Blocks.END_STONE.defaultBlockState());
        helper.assertTrue(!ChoirKeeperEntity.canSpawnHere(type, level, MobSpawnType.NATURAL,
                seat, RandomSource.create()), "Choir Keeper accepted a perch clipping an adjacent block");
        level.setBlockAndUpdate(seat.east(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(ChoirKeeperEntity.canSpawnHere(type, level, MobSpawnType.NATURAL,
                seat, RandomSource.create()), "Clear perch was rejected");

        ChoirKeeperEntity keeper = type.create(level);
        if (keeper == null) throw new AssertionError("Choir Keeper could not be created");
        keeper.setPos(Vec3.atBottomCenterOf(seat));
        keeper.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        level.addFreshEntity(keeper);
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(keeper.isAlive(), "Choir Keeper vanished after spawning");
            helper.assertTrue(level.noCollision(keeper.getBoundingBox()),
                    "Choir Keeper is inside a block after spawning");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 220, batch = "choir_keeper")
    public static void fliesAroundBlocksToReachEye(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos flower = helper.absolutePos(new BlockPos(3, 4, 3));
        level.setBlockAndUpdate(flower.below(2), Blocks.END_STONE.defaultBlockState());
        level.setBlockAndUpdate(flower.below(), Blocks.CHORUS_PLANT.defaultBlockState());
        level.setBlockAndUpdate(flower, Blocks.CHORUS_FLOWER.defaultBlockState());
        BlockPos seat = flower.above();
        for (int y = 0; y < 4; y++) {
            level.setBlockAndUpdate(seat.east(2).above(y), Blocks.END_STONE.defaultBlockState());
        }

        ChoirKeeperEntity keeper = EntityInit.choir_keeper.get().create(level);
        if (keeper == null) throw new AssertionError("Choir Keeper could not be created");
        keeper.setPos(Vec3.atBottomCenterOf(seat));
        keeper.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        level.addFreshEntity(keeper);
        Vec3 lure = Vec3.atBottomCenterOf(seat.east(4)).add(0.0D, 0.25D, 0.0D);
        ItemEntity eye = new ItemEntity(level, lure.x, lure.y, lure.z, new ItemStack(Items.ENDER_EYE));
        eye.setNoGravity(true);
        eye.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(eye);

        helper.runAfterDelay(190, () -> {
            helper.assertTrue(!eye.isAlive(), "Choir Keeper could not fly around the wall; keeper="
                    + keeper.position() + ", eye=" + eye.position() + ", flying=" + keeper.isFlying());
            helper.assertTrue(level.noCollision(keeper.getBoundingBox()),
                    "Choir Keeper remained inside a block after flight");
            helper.succeed();
        });
    }

    @GameTest(template = "empty",
            timeoutTicks = 150, batch = "choir_keeper")
    public static void catchesEyeAndReturnsToChorusPerch(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(3, 1, 3));
        level.setBlockAndUpdate(base, Blocks.END_STONE.defaultBlockState());
        level.setBlockAndUpdate(base.above(), Blocks.CHORUS_PLANT.defaultBlockState());
        level.setBlockAndUpdate(base.above(2), Blocks.CHORUS_PLANT.defaultBlockState());
        level.setBlockAndUpdate(base.above(3), Blocks.CHORUS_FLOWER.defaultBlockState());
        BlockPos seat = base.above(4);
        if (!ChoirKeeperEntity.canSpawnHere(EntityInit.choir_keeper.get(), level,
                MobSpawnType.NATURAL, seat, RandomSource.create())) {
            throw new AssertionError("Chorus tree perch was rejected for spawning");
        }

        ChoirKeeperEntity keeper = EntityInit.choir_keeper.get().create(level);
        if (keeper == null) throw new AssertionError("Choir Keeper could not be created");
        keeper.setPos(Vec3.atBottomCenterOf(seat));
        keeper.finalizeSpawn(level, level.getCurrentDifficultyAt(seat), MobSpawnType.NATURAL, null);
        level.addFreshEntity(keeper);

        Vec3 lure = Vec3.atBottomCenterOf(seat).add(3.0D, 0.25D, 0.0D);
        ItemEntity eye = new ItemEntity(level, lure.x, lure.y, lure.z, new ItemStack(Items.ENDER_EYE));
        eye.setNoGravity(true);
        eye.setPickUpDelay(120);
        level.addFreshEntity(eye);

        helper.runAfterDelay(15, () -> {
            helper.assertTrue(keeper.isFlying(), "Choir Keeper ignored the thrown eye; keeper="
                    + keeper.position() + ", eye=" + eye.position());
            helper.assertTrue(!ChoirKeeperEntity.canSpawnHere(EntityInit.choir_keeper.get(), level,
                            MobSpawnType.NATURAL, seat, RandomSource.create()),
                    "Keeper's flower became available while it chased the eye");
        });

        helper.runAfterDelay(100, () -> {
            helper.assertTrue(!eye.isAlive(), "Choir Keeper did not break the eye; keeper="
                    + keeper.position() + ", flying=" + keeper.isFlying()
                    + ", eye=" + eye.position() + ", eyeTicks=" + eye.tickCount);
            long pearls = level.getEntitiesOfClass(ItemEntity.class,
                    new AABB(seat).inflate(12.0D), item -> item.getItem().is(Items.ENDER_PEARL)).size();
            helper.assertTrue(pearls == 1, "Expected exactly one pearl, got " + pearls);
            helper.assertTrue(!keeper.isFlying()
                            && keeper.position().distanceToSqr(Vec3.atBottomCenterOf(seat)) <= 0.5D,
                    "Choir Keeper did not return to its chorus perch: " + keeper.position());
            helper.succeed();
        });
    }
}
