package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaSummonRules;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaSummoning;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaSummons;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.world.chum.ChumRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * CR-7: 500 mL projected into a broad ichor pool calls one Phlegethosoma, homed there and turned on its caller; a
 * second call is refused while it lives; a narrow pool is refused; the count fades like chum; a summoned one with
 * nothing to fight dives after a minute and leaves nothing; an older summons dives when its caller calls another.
 */
@GameTestHolder("phlegethosoma_validation")
@PrefixGameTestTemplate(false)
public final class PhlegethosomaSummonGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(7, 3, 7);

    private PhlegethosomaSummonGameTests() {
    }

    /** An ichor pool two deep on blackstone, {@code half} blocks either side of the middle. */
    private static void pool(GameTestHelper helper, int half) {
        for (int x = 1; x <= 13; x++) for (int z = 1; z <= 13; z++) {
            boolean inside = Math.abs(x - 7) <= half && Math.abs(z - 7) <= half;
            helper.setBlock(new BlockPos(x, 1, z), Blocks.BLACKSTONE);
            for (int y = 2; y <= 3; y++)
                helper.setBlock(new BlockPos(x, y, z), inside ? BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get() : Blocks.BLACKSTONE);
        }
    }

    private static void clear(GameTestHelper helper) {
        for (int x = 1; x <= 13; x++) for (int z = 1; z <= 13; z++) for (int y = 1; y <= 3; y++)
            helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
    }

    private static ServerPlayer caller(CapturedPlayer captured, GameTestHelper helper) {
        ServerPlayer player = captured.player;
        IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
        volume.setActive(true);
        volume.setMaxBloodVolume(5_000.0D);
        volume.setBloodVolume(1_000.0D);
        Vec3 bank = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 4, 7)));
        player.setPos(bank.x, bank.y, bank.z);
        return player;
    }

    private static double pour(ServerPlayer player, GameTestHelper helper, int ticks) {
        BlockPos pos = helper.absolutePos(MIDDLE);
        double paid = 0.0D;
        for (int i = 0; i < ticks; i++)
            paid += PhlegethosomaSummoning.pour(player, pos, Vec3.atCenterOf(pos)).orElse(-1.0D);
        return paid;
    }

    private static List<PhlegethosomaEntity> bosses(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(PhlegethosomaEntity.class,
                new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(24.0D));
    }

    private static double blood(ServerPlayer player) {
        return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
    }

    @GameTest(template = EMPTY, batch = "fluke_summon_call", timeoutTicks = 40)
    public static void fiveHundredMillilitresCallsOneAndASecondCallIsRefused(GameTestHelper helper) {
        pool(helper, 4);
        try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
            ServerPlayer player = caller(captured, helper);
            double paid = pour(player, helper, 249);
            helper.assertTrue(bosses(helper).isEmpty(), "the Phlegethosoma rose before 500 mL");
            paid += pour(player, helper, 1);
            helper.assertTrue(Math.abs(paid - 500.0D) < 1.0E-6D, "the call took " + paid + " mL");
            helper.assertTrue(Math.abs(1_000.0D - blood(player) - 500.0D) < 1.0E-6D, "the caller paid " + (1_000.0D - blood(player)));
            List<PhlegethosomaEntity> risen = bosses(helper);
            helper.assertTrue(risen.size() == 1, "500 mL called " + risen.size() + " Phlegethosomas");
            PhlegethosomaEntity boss = risen.get(0);
            helper.assertTrue(player.getUUID().equals(boss.summoner()) && boss.getTarget() == player,
                    "the summons is not turned on its caller");
            helper.assertTrue(boss.isPersistenceRequired(), "a summons may despawn like a wild animal");
            helper.assertTrue(PhlegethosomaSummoning.current(helper.getLevel(), player.getUUID()) == boss, "the caller's summons is not recorded");
            double before = blood(player);
            double again = pour(player, helper, 10);
            helper.assertTrue(again == 0.0D && blood(player) == before, "a second call was taken while the first lives");
            helper.assertTrue(bosses(helper).size() == 1, "a second Phlegethosoma rose");
            boss.discard();
            helper.assertTrue(PhlegethosomaSummons.get(helper.getLevel()).summonedBy(player.getUUID()) == null,
                    "a gone summons still holds the caller's place");
        }
        clear(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = "fluke_summon_narrow", timeoutTicks = 40)
    public static void aNarrowPoolOrAPoolOutsideTheBasinIsRefused(GameTestHelper helper) {
        pool(helper, 1);
        try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
            ServerPlayer player = caller(captured, helper);
            double paid = pour(player, helper, 300);
            helper.assertTrue(paid == 0.0D && blood(player) == 1_000.0D, "a three-wide pool took " + paid + " mL");
            helper.assertTrue(bosses(helper).isEmpty(), "a narrow pool called the Phlegethosoma");
            pool(helper, 4);
            BlockPos pos = helper.absolutePos(MIDDLE);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            helper.assertTrue(PhlegethosomaSummoning.tryProject(player, hit).isEmpty(),
                    "ichor outside the Nether basin is not a summoning pool");
        }
        clear(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = "fluke_summon_asleep", timeoutTicks = 40)
    public static void sleepingBloodCannotCallAndTheLiberPageOpensAtDegreeFive(GameTestHelper helper) {
        pool(helper, 4);
        try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
            ServerPlayer player = caller(captured, helper);
            HemoCapabilityAccess.getBloodVolume(player).setActive(false);
            BlockPos pos = helper.absolutePos(MIDDLE);
            for (int i = 0; i < 300; i++)
                helper.assertTrue(PhlegethosomaSummoning.pour(player, pos, Vec3.atCenterOf(pos)).isEmpty(), "sleeping blood poured");
            helper.assertTrue(blood(player) == 1_000.0D, "sleeping blood paid " + (1_000.0D - blood(player)) + " mL");
            helper.assertTrue(bosses(helper).isEmpty(), "sleeping blood called the Phlegethosoma");
            LiberKnowledgeHelper.unlockForDegree(player, 4);
            helper.assertTrue(!LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.PHLEGETHOSOMA), "the page opened before D5");
            LiberKnowledgeHelper.unlockForDegree(player, 5);
            helper.assertTrue(LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.PHLEGETHOSOMA), "the page did not open at D5");
        }
        clear(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = "fluke_summon_fade", timeoutTicks = 40)
    public static void aBrokenPourFadesLikeChum(GameTestHelper helper) {
        pool(helper, 4);
        ServerLevel level = helper.getLevel();
        Vec3 point = Vec3.atCenterOf(helper.absolutePos(MIDDLE));
        PhlegethosomaSummons data = PhlegethosomaSummons.get(level);
        try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
            ServerPlayer player = caller(captured, helper);
            pour(player, helper, 100);
            helper.assertTrue(Math.abs(data.counted(level, point) - 200.0D) < 1.0E-6D, "100 ticks counted " + data.counted(level, point));
            data.backdate(level, point, ChumRules.HALF_LIFE_TICKS);
            helper.assertTrue(Math.abs(data.counted(level, point) - 100.0D) < 1.0E-6D, "45 seconds left " + data.counted(level, point));
            pour(player, helper, 1);
            helper.assertTrue(Math.abs(data.counted(level, point) - 102.0D) < 1.0E-6D, "a resumed pour did not add to the faded count");
            helper.assertTrue(bosses(helper).isEmpty(), "a faded count called the Phlegethosoma");
        }
        clear(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = "fluke_summon_dive", timeoutTicks = 40)
    public static void aSummonsWithNothingToFightDivesAfterAMinuteAndLeavesNothing(GameTestHelper helper) {
        pool(helper, 4);
        PhlegethosomaEntity boss;
        UUID caller;
        try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
            ServerPlayer player = caller(captured, helper);
            caller = player.getUUID();
            pour(player, helper, 250);
            boss = bosses(helper).get(0);
        }
        boss.setTarget(null);
        int gone = -1;
        for (int tick = 1; tick <= PhlegethosomaSummonRules.DIVE_AFTER_TICKS + PhlegethosomaSummonRules.DEPART_TICKS + 20; tick++) {
            boss.tick();
            if (tick == PhlegethosomaSummonRules.DIVE_AFTER_TICKS - 20)
                helper.assertTrue(!boss.departing() && !boss.isRemoved(), "the summons left before a minute was up");
            if (boss.isRemoved()) {
                gone = tick;
                break;
            }
        }
        helper.assertTrue(gone > PhlegethosomaSummonRules.DIVE_AFTER_TICKS, "the summons was not gone after its dive: " + gone);
        AABB around = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(24.0D);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, around).isEmpty()
                && helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, around).isEmpty(), "the dive left loot behind");
        helper.assertTrue(PhlegethosomaSummons.get(helper.getLevel()).summonedBy(caller) == null, "a dived summons still blocks its caller");
        clear(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = "fluke_summon_replaced", timeoutTicks = 40)
    public static void anOlderSummonsDivesWhenItsCallerHasAnother(GameTestHelper helper) {
        pool(helper, 4);
        try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
            ServerPlayer player = caller(captured, helper);
            pour(player, helper, 250);
            PhlegethosomaEntity boss = bosses(helper).get(0);
            PhlegethosomaSummons.get(helper.getLevel()).record(player.getUUID(), UUID.randomUUID());
            for (int tick = 0; tick <= PhlegethosomaSummonRules.DEPART_TICKS && !boss.isRemoved(); tick++) boss.tick();
            helper.assertTrue(boss.isRemoved(), "a summons its caller no longer owns is still out");
        }
        clear(helper);
        helper.succeed();
    }
}
