package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.entity.weave.ThreadAttacks;
import com.vincenthuto.hemomancy.common.entity.weave.ThreadHazardEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity.WeaveThread;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("somatic_loom_validation")
@PrefixGameTestTemplate(false)
public final class SomaticLoomWeavingGameTests {
    private static final String AXONAL = "memory_weaving/memory_axonal_transduction";

    /** A loom stocked for Axonal Transduction (4 Ductilis, 600 mL, ganglion catalyst) and a weaver beside it. */
    private record Setup(BlockPos pos, SomaticLoomBlockEntity loom, FakePlayer weaver) {}

    /** Axonal Transduction is a PERFECTUS memory, so its fixture loom is Cortical (stage 2). */
    private static Setup setup(GameTestHelper helper, int ductilis) {
        return setup(helper, AXONAL, ItemInit.naeglerophaeon_ganglion.get(), ductilis, 2);
    }

    private static Setup setup(GameTestHelper helper, String recipe, net.minecraft.world.item.Item catalyst, int ductilis, int stage) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var loom = (SomaticLoomBlock) BlockInit.somatic_loom.get();
        level.setBlock(pos, loom.defaultBlockState().setValue(SomaticLoomBlock.STAGE, stage), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(catalyst), null);
        CompoundTag tag = te.saveWithoutMetadata(level.registryAccess());
        CompoundTag stock = new CompoundTag();
        stock.putInt(EnumBloodTendency.DUCTILIS.name(), ductilis);
        tag.put("storedEnzymes", stock);
        te.loadWithComponents(tag, level.registryAccess());
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc(recipe)), recipe + " should be selectable at stage " + stage);
        var weaver = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "loom_weaver"));
        weaver.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        HemoCapabilityAccess.getBloodVolume(weaver).ifPresent(volume -> {
            volume.setActive(true);
            volume.setBloodVolume(volume.getMaxBloodVolume());
        });
        return new Setup(pos, te, weaver);
    }

    private static void pay(GameTestHelper helper, Setup s) {
        helper.assertTrue(s.loom.tryChargeRitualBlood(s.weaver, 600, false) && s.loom.isWeaving(),
                "Paying the full blood cost should begin the weave");
    }

    private static void tick(GameTestHelper helper, Setup s, int ticks) {
        for (int i = 0; i < ticks; i++)
            SomaticLoomBlockEntity.serverTick(helper.getLevel(), s.pos, helper.getLevel().getBlockState(s.pos), s.loom);
    }

    private static long count(Setup s, WeaveThread.State state) {
        return s.loom.getWeaveThreads().stream().filter(t -> t.state() == state).count();
    }

    @GameTest(template = "empty")
    public static void payingSpinsTheEnzymesIntoThePirns(GameTestHelper helper) {
        Setup s = setup(helper, 6);
        pay(helper, s);
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 2, "Payment draws exactly the recipe's 4");
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 4, "The drawn enzyme is wound onto the pirn");
        helper.assertTrue(s.loom.getWeaveThreads().size() == 4 && count(s, WeaveThread.State.HELD) == 4,
                "One held thread per enzyme");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void threadsTearLooseOnTheEchoLoopUpToTheCap(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        helper.assertTrue(count(s, WeaveThread.State.LOOSE) == 1, "One thread frays per loop");
        tick(helper, s, SomaticWeaveRules.LOOP_TICKS * 5);
        // Axonal Transduction is PERFECTUS: three loose at most.
        helper.assertTrue(count(s, WeaveThread.State.LOOSE) == 3, "Loose threads stop at the rank's cap");
        helper.assertTrue(s.loom.looseThreadEntities(helper.getLevel()).size() == 3, "Every loose thread is a live entity");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slackThreadsHookHomeAndTheClimaxCrystallizes(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        helper.assertTrue(LoomWeaveTestSupport.weaveAll(helper.getLevel(), s.pos, s.loom, s.weaver),
                "The weave should finish");
        helper.assertFalse(s.loom.isWeaving(), "The loom returns to idle after the climax");
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 0, "Every spun enzyme is woven in");
        boolean delivered = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(s.pos).inflate(3)).stream()
                .anyMatch(item -> item.getItem().is(ItemInit.memory_axonal_transduction.get()));
        helper.assertTrue(delivered, "The finished memory should crystallize");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tautThreadsRefuseTheHook(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        helper.assertFalse(thread.tryHook(s.weaver), "A taut thread cannot be woven");
        thread.addStrain(1f);
        helper.assertTrue(thread.isSlack() && thread.tryHook(s.weaver), "A slack thread hooks home");
        helper.assertTrue(count(s, WeaveThread.State.WOVEN) == 1, "The hooked thread is woven");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void leavingTheAreaPausesAndRecallsLooseThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        long before = s.loom.getWeaveTicks();
        s.weaver.moveTo(s.pos.getX() + 40, s.pos.getY(), s.pos.getZ());
        tick(helper, s, 5);
        helper.assertTrue(s.loom.isPaused() && count(s, WeaveThread.State.LOOSE) == 0
                && s.loom.looseThreadEntities(helper.getLevel()).isEmpty(), "Loose threads retreat into the echo");
        helper.assertTrue(s.loom.getWeaveTicks() == before, "A paused weave does not advance its loop");
        s.weaver.moveTo(s.pos.getX() + 0.5, s.pos.getY(), s.pos.getZ() - 2.5);
        tick(helper, s, SomaticWeaveRules.LOOP_TICKS);
        helper.assertTrue(!s.loom.isPaused() && count(s, WeaveThread.State.LOOSE) >= 1, "Returning resumes the weave");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aMidWeaveSaveRestoresWovenAndHeldThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        thread.addStrain(1f);
        thread.tryHook(s.weaver);
        CompoundTag saved = s.loom.saveWithoutMetadata(helper.getLevel().registryAccess());
        s.loom.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(s.loom.isWeaving() && count(s, WeaveThread.State.WOVEN) == 1
                && s.loom.getWeaver().equals(s.weaver.getUUID()), "Woven threads and the weaver survive a reload");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void preWitnessedSavesMigrateTheirOrbsToThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        CompoundTag tag = s.loom.saveWithoutMetadata(helper.getLevel().registryAccess());
        ListTag orbs = new ListTag();
        for (int i = 0; i < 4; i++) {
            CompoundTag orb = new CompoundTag();
            orb.putString("tendency", EnumBloodTendency.DUCTILIS.name());
            orb.putInt("enzymeCost", 1);
            orb.putBoolean("completed", i == 0);
            orbs.add(orb);
        }
        tag.put("ritualOrbs", orbs);
        tag.putInt("craftPhase", SomaticLoomBlockEntity.PHASE_WEAVING);
        tag.putDouble("ritualBloodCharged", 600);
        tag.remove("weaveThreads");
        s.loom.loadWithComponents(tag, helper.getLevel().registryAccess());
        helper.assertTrue(s.loom.isWeaving() && count(s, WeaveThread.State.WOVEN) == 1
                && count(s, WeaveThread.State.HELD) == 3, "Completed orbs become woven threads, the rest held");
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 3, "Unwoven orbs keep their enzyme on the pirn");
        // A player present in the level (not only on the fixture's side) is what an orphaned weave can find.
        var adopter = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "loom_adopter"));
        adopter.moveTo(s.pos.getX() + 0.5, s.pos.getY(), s.pos.getZ() - 2.5);
        helper.getLevel().addNewPlayer(adopter);
        try {
            tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
            helper.assertTrue(adopter.getUUID().equals(s.loom.getWeaver()), "The nearest player takes up an orphaned weave");
        } finally {
            helper.getLevel().removePlayerImmediately(adopter, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void orphanedThreadsUnravelWhenTheLoomIsGone(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        helper.getLevel().removeBlock(s.pos, false);
        thread.tick();
        helper.assertTrue(thread.isRemoved(), "A thread without its loom unravels");
        s.weaver.discard();
        helper.succeed();
    }

    private static FrayedThreadEntity looseThread(GameTestHelper helper, Setup s) {
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        return s.loom.looseThreadEntities(helper.getLevel()).getFirst();
    }

    @GameTest(template = "empty")
    public static void tendencyHitsApplyTheirSchoolsHarm(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.moveTo(thread.getX() + 1, thread.getY(), thread.getZ());

        ThreadAttacks.onBoltHit(EnumBloodTendency.CONGEATIO, thread, victim, 1f);
        helper.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
                && victim.getTicksFrozen() > 0, "Congeatio slows and freezes");

        victim.invulnerableTime = 0;   // the previous hit's i-frames would swallow this one
        float before = victim.getHealth();
        ThreadAttacks.onBoltHit(EnumBloodTendency.LUX, thread, victim, 1f);
        helper.assertTrue(victim.getHealth() < before && victim.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING),
                "Lux lances burn and mark");

        ThreadAttacks.onBoltHit(EnumBloodTendency.FLAMMEUS, thread, victim, 1f);
        helper.assertTrue(victim.getRemainingFireTicks() > 0, "Flammeus ignites");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void animusBoltsDrainTheWeaversBlood(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        double before = HemoCapabilityAccess.getBloodVolume(s.weaver).orElseThrow().getBloodVolume();
        ThreadAttacks.onBoltHit(EnumBloodTendency.ANIMUS, thread, s.weaver, 1f);
        double after = HemoCapabilityAccess.getBloodVolume(s.weaver).orElseThrow().getBloodVolume();
        helper.assertTrue(after < before, "Animus drains blood volume");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void areaAttacksLandOnTheWeaver(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        var level = helper.getLevel();
        // Area harm finds only entities that are in the level, and lands only on the weaver, so the weaver is added.
        var sickened = s.weaver;
        sickened.moveTo(thread.getX() + 2, thread.getY() - 1, thread.getZ());
        level.addNewPlayer(sickened);
        try {
            ThreadHazardEntity miasma = ThreadHazardEntity.spawn(level, thread, sickened.position(), EnumBloodTendency.MORTEM, 2.5f, 80, 1f);
            for (int i = 0; i < 21; i++) miasma.tick();
            helper.assertTrue(sickened.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION)
                    && sickened.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Mortem miasma sickens");
        } finally {
            level.removePlayerImmediately(sickened, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.moveTo(thread.getX() + 2, thread.getY() - 1, thread.getZ());

        int spikesBefore = level.getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike.class,
                new AABB(victim.blockPosition()).inflate(5)).size();
        ThreadAttacks.spikes(thread, victim, 1f);
        int spikesAfter = level.getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike.class,
                new AABB(victim.blockPosition()).inflate(5)).size();
        helper.assertTrue(spikesAfter > spikesBefore && victim.getDeltaMovement().y > 0, "Ferric spikes erupt and launch");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lowRankThreadsBlackOutInsteadOfKilling(GameTestHelper helper) {
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Setup s = setupMortalBloodShot(helper, victim);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        victim.setHealth(6.0F);
        victim.hurt(thread.damageSources().mobAttack(thread), 20.0F);
        helper.assertTrue(victim.isAlive() && victim.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                "A HUMILIS weave floors the weaver");
        tick(helper, s, 1);
        helper.assertTrue(s.loom.isPaused(), "Blacking out pauses the weave");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void otherAttackersAreNotFloored(GameTestHelper helper) {
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.setHealth(6.0F);
        victim.getPersistentData().putLong("hemomancy:weave_harm_until", helper.getLevel().getGameTime() + 100);
        victim.getPersistentData().putBoolean("hemomancy:weave_lethal", false);
        var zombie = new net.minecraft.world.entity.monster.Zombie(helper.getLevel());
        victim.hurt(helper.getLevel().damageSources().mobAttack(zombie), 20.0F);
        helper.assertTrue(victim.isDeadOrDying(), "A zombie near a low-rank weave still kills");
        helper.succeed();
    }

    /** A HUMILIS Blood Shot weave whose weaver can be hurt: a mock player, not an invulnerable FakePlayer. */
    private static Setup setupMortalBloodShot(GameTestHelper helper, net.minecraft.world.entity.player.Player weaver) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
        te.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc("memory_weaving/memory_blood_shot")), "Blood Shot should be selectable");
        weaver.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        HemoCapabilityAccess.getBloodVolume(weaver).ifPresent(volume -> {
            volume.setActive(true);
            volume.setBloodVolume(volume.getMaxBloodVolume());
        });
        helper.assertTrue(te.tryChargeRitualBlood(weaver, 50, false) && te.isWeaving(), "Blood Shot weave should begin");
        // Active blood makes the damage hooks treat the mock as a ServerPlayer; the weave no longer needs it.
        HemoCapabilityAccess.getBloodVolume(weaver).ifPresent(volume -> volume.setActive(false));
        SomaticLoomBlockEntity.serverTick(level, pos, level.getBlockState(pos), te);
        for (int i = 0; i < SomaticWeaveRules.FRAY_TICK; i++) SomaticLoomBlockEntity.serverTick(level, pos, level.getBlockState(pos), te);
        return new Setup(pos, te, null);
    }

    @GameTest(template = "empty")
    public static void lavaStillKillsDuringALowRankWeave(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Setup s = setupMortalBloodShot(helper, weaver);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        ThreadAttacks.onBoltHit(EnumBloodTendency.FLAMMEUS, thread, weaver, 1f);
        weaver.invulnerableTime = 0;
        weaver.setHealth(6.0F);
        weaver.hurt(helper.getLevel().damageSources().lava(), 20.0F);
        helper.assertTrue(weaver.isDeadOrDying(), "Lava is not the weave's harm and must not be floored");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theFireAThreadLitIsFloored(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Setup s = setupMortalBloodShot(helper, weaver);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        ThreadAttacks.onBoltHit(EnumBloodTendency.FLAMMEUS, thread, weaver, 1f);
        weaver.invulnerableTime = 0;
        weaver.setHealth(6.0F);
        weaver.hurt(helper.getLevel().damageSources().onFire(), 20.0F);
        helper.assertTrue(weaver.isAlive() && weaver.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                "Burning lit by a low-rank thread stops at the floor");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aMissedAttackOpensNoHarmWindow(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Setup s = setupMortalBloodShot(helper, weaver);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        // Animus only fires a bolt; until the bolt lands nothing has harmed the weaver.
        ThreadAttacks.perform(thread, weaver);
        helper.assertTrue(!weaver.getPersistentData().contains("hemomancy:weave_harm_until"),
                "Firing a bolt must not open the harm window before it lands");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void firstThreadHarmIsFlooredWithoutAWindow(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Setup s = setupMortalBloodShot(helper, weaver);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        weaver.getPersistentData().remove("hemomancy:weave_harm_until");
        weaver.setHealth(6.0F);
        weaver.hurt(thread.damageSources().mobAttack(thread), 20.0F);
        helper.assertTrue(weaver.isAlive() && weaver.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                "A low-rank thread's own blow is floored even as the first harm");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ferricSpikesAndTheirLaunchAreFloored(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Setup s = setupMortalBloodShot(helper, weaver);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        ThreadAttacks.spikes(thread, weaver, 1f);
        var spike = new com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike(
                com.vincenthuto.hemomancy.common.init.EntityInit.iron_spike.get(), helper.getLevel());
        var orphanSpikeHit = new net.minecraft.world.damagesource.DamageSource(
                helper.getLevel().damageSources().generic().typeHolder(), spike, null);
        weaver.setHealth(6.0F);
        weaver.hurt(orphanSpikeHit, 20.0F);
        helper.assertTrue(weaver.isAlive() && weaver.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                "A spike whose thread is gone is still the weave's harm");
        // Blacking out spends the window, so a fresh launch opens the next one.
        ThreadAttacks.spikes(thread, weaver, 1f);
        weaver.invulnerableTime = 0;
        weaver.setHealth(6.0F);
        weaver.hurt(helper.getLevel().damageSources().fall(), 20.0F);
        helper.assertTrue(weaver.isAlive(), "Landing from a Ferric launch is floored");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void helpersAreNotHuntedByBoltsOrHazards(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Setup s = setupMortalBloodShot(helper, weaver);
        var level = helper.getLevel();
        FrayedThreadEntity thread = s.loom.looseThreadEntities(level).getFirst();
        var ally = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "loom_ally"));
        ally.moveTo(thread.getX() + 2, thread.getY() - 1, thread.getZ());
        level.addNewPlayer(ally);
        try {
            var bolt = new com.vincenthuto.hemomancy.common.entity.weave.ThreadBoltEntity(level, thread, EnumBloodTendency.ANIMUS, 1f);
            helper.assertTrue(bolt.mayStrike(weaver) && !bolt.mayStrike(ally), "Bolts strike only the weaver");
            ThreadHazardEntity miasma = ThreadHazardEntity.spawn(level, thread, ally.position(), EnumBloodTendency.MORTEM, 2.5f, 80, 1f);
            for (int i = 0; i < 21; i++) miasma.tick();
            helper.assertTrue(!ally.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Hazards spare helpers");
            ThreadAttacks.markWeaveHarm(ally, thread);
            helper.assertTrue(!ally.getPersistentData().contains("hemomancy:weave_loom"), "Helpers carry no weave floor");
        } finally {
            level.removePlayerImmediately(ally, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void staffStrikesMustBeMeleeAndRecover(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        var ally = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ally.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_staff.get()));
        var arrow = new net.minecraft.world.entity.projectile.Arrow(helper.getLevel(), ally, new ItemStack(net.minecraft.world.item.Items.ARROW), null);
        thread.hurt(helper.getLevel().damageSources().arrow(arrow, ally), 4.0F);
        helper.assertTrue(thread.strain() == 0F, "Only a staff blow in hand strains a thread, not a shot");
        thread.hurt(helper.getLevel().damageSources().playerAttack(ally), 4.0F);
        float once = thread.strain();
        thread.hurt(helper.getLevel().damageSources().playerAttack(ally), 4.0F);
        helper.assertTrue(once > 0F && thread.strain() == once, "Strikes recover before they strain again");
        for (int i = 0; i < SomaticWeaveRules.STRIKE_COOLDOWN_TICKS; i++) thread.tick();
        thread.hurt(helper.getLevel().damageSources().playerAttack(ally), 4.0F);
        helper.assertTrue(thread.strain() > once, "A recovered strike strains again");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aLaggingResyncNeverReplaysTheSameBeat(GameTestHelper helper) {
        Setup s = setup(helper, 8);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.LOOP_TICKS + 50);
        s.loom.markEchoLoopFired(1);
        var registries = helper.getLevel().registryAccess();
        CompoundTag behind = s.loom.saveWithoutMetadata(registries);
        // A client that ran ahead of a lagging server is pulled back a few ticks within the same loop.
        behind.putLong("weaveTicks", SomaticWeaveRules.LOOP_TICKS + 46);
        s.loom.loadWithComponents(behind, registries);
        helper.assertTrue(s.loom.echoLastLoopFired() == 1, "A resync within the beat keeps it fired");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void echoBookkeepingResetsForANewWeave(GameTestHelper helper) {
        Setup s = setup(helper, 8);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.LOOP_TICKS * 2);
        s.loom.markEchoClimaxFired();
        s.loom.markEchoLoopFired(2);
        var registries = helper.getLevel().registryAccess();
        CompoundTag fresh = s.loom.saveWithoutMetadata(registries);
        fresh.putLong("weaveTicks", 5);
        s.loom.loadWithComponents(fresh, registries);
        helper.assertTrue(!s.loom.echoClimaxFired() && s.loom.echoLastLoopFired() < 0,
                "A weave that restarts must replay its loop and climax casts");
        s.weaver.discard();
        helper.succeed();
    }

    private static final String LIVING_CIRCUIT = "memory_weaving/memory_living_circuit";

    @GameTest(template = "empty")
    public static void upgradeRitesWaitForAPaidWeave(GameTestHelper helper) {
        Setup s = setup(helper, 6);
        var level = helper.getLevel();
        helper.assertTrue(s.loom.readyForUpgradeRite(level, net.minecraft.core.Direction.NORTH),
                "A chosen but unpaid weave does not hold the loom from a rite");
        pay(helper, s);
        helper.assertTrue(!s.loom.readyForUpgradeRite(level, net.minecraft.core.Direction.NORTH),
                "A paid weave must finish or be cancelled before the loom is upgraded");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aRiteHeldLoomRefusesEdits(GameTestHelper helper) {
        Setup s = setup(helper, 6);
        s.loom.setRiteLocked(true);
        helper.assertTrue(!s.loom.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null)
                && !s.loom.selectRecipe(null, Hemomancy.rloc(AXONAL))
                && !s.loom.tryChargeRitualBlood(s.weaver, 600, false)
                && !s.loom.removeItem(s.weaver, false) && !s.loom.isWeaving(),
                "A loom held by a rite takes no inputs, choices or blood");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void completingAWeaveRecordsLoomPractice(GameTestHelper helper) {
        Setup s = setup(helper, LIVING_CIRCUIT, net.minecraft.world.item.Items.REDSTONE_LAMP, 3, 1);
        var progress = HemoCapabilityAccess.stationUpgrades(s.weaver);
        helper.assertTrue(s.loom.tryChargeRitualBlood(s.weaver, 450, false) && s.loom.isWeaving(), "Living Circuit weave begins");
        helper.assertTrue(LoomWeaveTestSupport.weaveAll(helper.getLevel(), s.pos, s.loom, s.weaver), "The weave completes");
        helper.assertTrue(progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE)
                && progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE_GANGLIONIC),
                "A Magister weave on a Ganglionic loom proves both loom practices");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void baseLoomWeavesDoNotProveCorticalPractice(GameTestHelper helper) {
        var fake = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "base_weaver"));
        HemoCapabilityAccess.getBloodVolume(fake).ifPresent(volume -> {
            volume.setActive(true);
            volume.setBloodVolume(volume.getMaxBloodVolume());
        });
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
        te.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc("memory_weaving/memory_blood_shot")), "Blood Shot selectable");
        fake.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        helper.assertTrue(te.tryChargeRitualBlood(fake, 50, false), "Blood Shot weave begins");
        helper.assertTrue(LoomWeaveTestSupport.weaveAll(level, pos, te, fake), "The weave completes");
        var progress = HemoCapabilityAccess.stationUpgrades(fake);
        helper.assertTrue(progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE)
                && !progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE_GANGLIONIC),
                "A Humilis weave proves only the first loom practice");
        fake.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void refinedMemoriesNeedAnUpgradedLoom(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var block = (SomaticLoomBlock) BlockInit.somatic_loom.get();
        int[][] cases = {{0, 0, 0}, {1, 1, 0}, {2, 1, 1}};   // stage, Magister allowed, Perfectus allowed
        for (int[] c : cases) {
            level.setBlock(pos, block.defaultBlockState().setValue(SomaticLoomBlock.STAGE, c[0]), 3);
            var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
            te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
            te.addItem(null, new ItemStack(net.minecraft.world.item.Items.REDSTONE_LAMP), null);
            helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc(LIVING_CIRCUIT)) == (c[1] == 1),
                    "Stage " + c[0] + " Magister selection");
            level.removeBlock(pos, false);
            level.setBlock(pos, block.defaultBlockState().setValue(SomaticLoomBlock.STAGE, c[0]), 3);
            te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
            te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
            te.addItem(null, new ItemStack(ItemInit.naeglerophaeon_ganglion.get()), null);
            helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc(AXONAL)) == (c[2] == 1),
                    "Stage " + c[0] + " Perfectus selection");
            level.removeBlock(pos, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aLoomBroughtBackToBaseRefusesItsRefinedWeave(GameTestHelper helper) {
        Setup s = setup(helper, LIVING_CIRCUIT, net.minecraft.world.item.Items.REDSTONE_LAMP, 3, 1);
        var level = helper.getLevel();
        level.setBlock(s.pos, level.getBlockState(s.pos).setValue(SomaticLoomBlock.STAGE, 0), 3);
        s.loom.refreshRecipe();
        helper.assertTrue(!s.loom.tryChargeRitualBlood(s.weaver, 450, false) && !s.loom.isWeaving()
                && s.loom.getCurRecipe() == null, "A base loom will not take blood for a Magister weave");
        s.weaver.discard();
        helper.succeed();
    }

    /** Pays a Living Circuit weave on a loom at `stage` and returns its first loose thread. */
    private static FrayedThreadEntity looseCircuitThread(GameTestHelper helper, Setup s) {
        helper.assertTrue(s.loom.tryChargeRitualBlood(s.weaver, 450, false) && s.loom.isWeaving(), "Living Circuit weave begins");
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        return s.loom.looseThreadEntities(helper.getLevel()).getFirst();
    }

    @GameTest(template = "empty")
    public static void ganglionicFeelersCatchSlackThreadsNearTheEcho(GameTestHelper helper) {
        Setup s = setup(helper, LIVING_CIRCUIT, net.minecraft.world.item.Items.REDSTONE_LAMP, 3, 1);
        FrayedThreadEntity thread = looseCircuitThread(helper, s);
        var anchor = s.loom.echoAnchor();
        thread.moveTo(anchor.x + 1.5, anchor.y + 1, anchor.z);
        tick(helper, s, 1);
        helper.assertTrue(!thread.isRemoved() && count(s, WeaveThread.State.WOVEN) == 0, "Feelers never take a taut thread");
        thread.addStrain(1f);
        tick(helper, s, 1);
        helper.assertTrue(count(s, WeaveThread.State.WOVEN) == 1, "Ganglionic feelers take a slack thread near the echo");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void baseLoomsHaveNoFeelers(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
        te.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc("memory_weaving/memory_blood_shot")), "Blood Shot selectable");
        var weaver = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "base_weaver"));
        weaver.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        HemoCapabilityAccess.getBloodVolume(weaver).ifPresent(volume -> { volume.setActive(true); volume.setBloodVolume(volume.getMaxBloodVolume()); });
        helper.assertTrue(te.tryChargeRitualBlood(weaver, 50, false), "Blood Shot weave begins");
        Setup s = new Setup(pos, te, weaver);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = te.looseThreadEntities(level).getFirst();
        thread.addStrain(1f);
        var anchor = te.echoAnchor();
        thread.moveTo(anchor.x + 1, anchor.y + 1, anchor.z);
        tick(helper, s, 1);
        helper.assertTrue(count(s, WeaveThread.State.WOVEN) == 0, "A base loom waits for the staff");
        weaver.discard();
        helper.succeed();
    }

    /** Faces `player` at `target` from `distance` blocks along -z and uses a Living Staff. */
    private static void staffAt(GameTestHelper helper, FakePlayer player, FrayedThreadEntity target, double distance) {
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_staff.get()));
        var at = target.getBoundingBox().getCenter();
        player.moveTo(at.x, at.y - player.getEyeHeight(), at.z - distance, 0F, 0F);
        player.setYRot(0F);
        player.setXRot(0F);
        player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
                .use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    @GameTest(template = "empty")
    public static void aStaffHooksASlackThreadAtItsLoomsReach(GameTestHelper helper) {
        Setup s = setup(helper, LIVING_CIRCUIT, net.minecraft.world.item.Items.REDSTONE_LAMP, 3, 1);
        FrayedThreadEntity thread = looseCircuitThread(helper, s);
        // Far from the echo so the feelers cannot take it first.
        var anchor = s.loom.echoAnchor();
        thread.moveTo(anchor.x, anchor.y + 1, anchor.z - 7.5);
        thread.addStrain(1f);
        var ally = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "far_hooker"));
        staffAt(helper, ally, thread, SomaticWeaveRules.hookReach(1) + 1.5);
        helper.assertTrue(count(s, WeaveThread.State.WOVEN) == 0, "Beyond the Ganglionic reach the staff misses");
        staffAt(helper, ally, thread, SomaticWeaveRules.hookReach(1) - 1.0);
        if (count(s, WeaveThread.State.WOVEN) != 1) {
            var seen = FrayedThreadEntity.lookedAtSlackThread(ally);
            helper.fail("Within the Ganglionic reach the staff hooks it home (looked-at=" + (seen == null ? "none" : seen == thread ? "ours" : "other@" + seen.blockPosition())
                    + ", removed=" + thread.isRemoved() + ", slack=" + thread.isSlack() + ", dist=" + ally.getEyePosition().distanceTo(thread.position())
                    + ", weaving=" + s.loom.isWeaving() + ", stage=" + s.loom.upgradeTier() + ")");
        }
        ally.discard();
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aCorticalWeaveCarriesTheWeaversChamberTheme(GameTestHelper helper) {
        var chambers = com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.get(helper.getLevel().getServer());
        Setup s = setup(helper, 8);
        pay(helper, s);
        helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.THEME_WILL_DEFAULT.equals(s.loom.getWeaverTheme()),
                "A weaver without a Chamber weaves under the default sky");
        helper.assertTrue(chambers.skyThemeOf(UUID.randomUUID()).equals(com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.THEME_WILL_DEFAULT),
                "Reading a stranger's theme falls back to the default");
        s.loom.cancelRitual(null);

        chambers.setSkyThemeOverride(s.weaver, com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION);
        // A cancel keeps the blank and catalyst; the recipe is chosen again.
        helper.assertTrue(s.loom.selectRecipe(null, Hemomancy.rloc(AXONAL)), "Axonal reselected");
        pay(helper, s);
        helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION.equals(s.loom.getWeaverTheme()),
                "The dome takes the weaver's own Chamber sky");
        var registries = helper.getLevel().registryAccess();
        s.loom.loadWithComponents(s.loom.saveWithoutMetadata(registries), registries);
        helper.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION.equals(s.loom.getWeaverTheme()),
                "The theme survives a reload");
        chambers.clearSkyThemeOverride(s.weaver);
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cyclingStepsPastMemoriesTheLoomCannotWeave(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(net.minecraft.world.item.Items.BLUE_ICE), null);
        var cycler = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "loom_cycler"));
        // Blue Ice opens Absolute Stillness (Perfectus) first, then three memories a base loom can weave.
        java.util.Set<String> reached = new java.util.HashSet<>();
        for (int click = 0; click < 8; click++) {
            te.chooseNextRecipe(cycler);
            if (te.getCurRecipe() != null)
                reached.add(BuiltInRegistries.ITEM.getKey(te.getCurRecipe().getResultItem(level.registryAccess()).getItem()).getPath());
        }
        helper.assertTrue(reached.containsAll(java.util.List.of("memory_cryogenic_pulse", "memory_glacial_grasp", "memory_glacial_rampart"))
                && !reached.contains("memory_absolute_stillness"),
                "Cycling reaches every memory the loom can weave and never readies a refused one: " + reached);
        cycler.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aHelperStrainsAndHooksTheWeaversThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        var ally = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ally.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_staff.get()));
        float before = thread.strain();
        thread.hurt(helper.getLevel().damageSources().playerAttack(ally), 4.0F);
        helper.assertTrue(thread.strain() > before, "A helper's staff hit strains the thread");
        thread.addStrain(1f);
        helper.assertTrue(thread.tryHook(ally), "A helper may hook a slack thread home");
        s.weaver.discard();
        helper.succeed();
    }
}
