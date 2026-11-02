package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.mob.monster.ErythromyceliumEruptusEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Eruptus's states: its fuse, its one-way eruption and what a reload lands on. */
@GameTestHolder("eruptus_validation")
@PrefixGameTestTemplate(false)
public final class EruptusStateGameTests {
    private static ErythromyceliumEruptusEntity corpse(GameTestHelper h) {
        var eruptus = EntityInit.erythromycelium_eruptus.get().create(h.getLevel());
        eruptus.setNoAi(true);
        eruptus.setPersistenceRequired();
        Vec3 at = h.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 2, 1)));
        eruptus.setPos(at.x, at.y, at.z);
        h.getLevel().addFreshEntity(eruptus);
        return eruptus;
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void theFuseEndsInAStandingGrowthWithNoLeftoverSwelling(GameTestHelper h) {
        var eruptus = corpse(h);
        h.assertTrue(eruptus.getBbHeight() == 0.5F, "A corpse should lie flat, not stand three blocks tall");
        eruptus.setSwellDir(1);
        h.startSequence()
                .thenWaitUntil(() -> h.assertTrue(eruptus.isErupted(), "The full fuse should erupt it"))
                .thenExecute(() -> h.assertTrue(eruptus.getBbHeight() == 3.0F, "The growth should take its full height"))
                .thenIdle(ErythromyceliumEruptusEntity.ERUPT_TICKS)
                .thenExecute(() -> {
                    h.assertTrue(eruptus.eruption(1.0F) == 1.0F, "The eruption should have finished");
                    h.assertTrue(eruptus.getSwelling(1.0F) == 0.0F, "A standing growth should keep no swelling");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void swellingBacksOffUntilItBursts(GameTestHelper h) {
        var eruptus = corpse(h);
        eruptus.setSwellDir(1);
        h.startSequence()
                .thenIdle(10)
                .thenExecute(() -> {
                    h.assertTrue(eruptus.getSwelling(1.0F) > 0.0F, "It should be swelling");
                    eruptus.setSwellDir(-1);
                })
                .thenIdle(15)
                .thenExecute(() -> {
                    h.assertFalse(eruptus.isErupted(), "A fuse that backed off should not erupt");
                    h.assertTrue(eruptus.getSwelling(1.0F) == 0.0F, "The swelling should have gone down");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void aReloadKeepsTheStateAndNeverReplaysTheEruption(GameTestHelper h) {
        var eruptus = corpse(h);
        var corpseCopy = EntityInit.erythromycelium_eruptus.get().create(h.getLevel());
        corpseCopy.load(eruptus.saveWithoutId(new CompoundTag()));
        h.assertFalse(corpseCopy.isErupted(), "A saved corpse should load as a corpse");
        eruptus.setSwellDir(1);
        h.startSequence()
                .thenWaitUntil(() -> h.assertTrue(eruptus.isErupted(), "The full fuse should erupt it"))
                .thenExecute(() -> {
                    var copy = EntityInit.erythromycelium_eruptus.get().create(h.getLevel());
                    copy.load(eruptus.saveWithoutId(new CompoundTag()));
                    h.assertTrue(copy.isErupted(), "A saved growth should not load as a corpse");
                    h.assertTrue(copy.eruption(0.0F) == 1.0F, "A reloaded growth should already be standing");
                    h.assertTrue(copy.getBbHeight() == 3.0F, "A reloaded growth should take its full height");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theStandingGrowthLungesAtItsTargetAndHurtsIt(GameTestHelper h) {
        // A floor to land on, and the small template's shell cleared: the standing eye sits above it.
        for (int x = -1; x <= 9; x++)
            for (int z = -1; z <= 3; z++) {
                h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                for (int y = 2; y <= 7; y++) h.setBlock(new BlockPos(x, y, z), Blocks.AIR);
            }
        var eruptus = corpse(h);
        eruptus.setNoAi(false);
        var pig = h.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(6, 2, 1));
        h.assertTrue(eruptus.getMaxHealth() == 30.0F, "The raised health should be registered");
        eruptus.setTarget(pig);
        eruptus.setSwellDir(1);
        h.startSequence()
                .thenWaitUntil(() -> h.assertTrue(eruptus.lungeAge(0.0F) >= 0.0F, "It should start a lunge in range"))
                .thenExecute(() -> h.assertTrue(pig.getHealth() == pig.getMaxHealth(), "The wind-up should warn before it hurts"))
                .thenWaitUntil(() -> h.assertTrue(pig.getHealth() == pig.getMaxHealth() - 6.0F, "The lunge should land for its attack damage"))
                .thenSucceed();
    }
}
