package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.circus.*;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
public final class CrimsonTroupePracticalGameTests {
    private record Exercise(GameTestHelper helper, ServerPlayer pupil, CircusPerformerEntity teacher,
                            ItemStack crossbar, UUID id, Mob body, List<Mob> threats) implements AutoCloseable {
        boolean passed() { return CircusApprenticeshipProgress.practicalComplete(pupil, ((BoundPuppeteerSummon)body).hemomancy$getSummonName()); }
        void observe() { CircusPracticalController.tick(new PlayerTickEvent.Post(pupil)); }
        @Override public void close() {
            CircusPracticalController.cancel(pupil); threats.forEach(Mob::discard);
            helper.getLevel().getEntitiesOfClass(SanguineHoundEntity.class, teacher.getBoundingBox().inflate(12),
                    cur -> pupil.getUUID().equals(cur.curOwner())).forEach(Mob::discard);
            body.discard(); teacher.discard(); onlinePlayers(helper).remove(pupil.getUUID()); pupil.discard();
        }
    }
    @SuppressWarnings("unchecked") private static Map<UUID, ServerPlayer> onlinePlayers(GameTestHelper h) {
        try {
            var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true); return (Map<UUID, ServerPlayer>) field.get(h.getLevel().getServer().getPlayerList());
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static Exercise exercise(GameTestHelper h, String shape, EntityType<? extends CircusPerformerEntity> professor) {
        ServerPlayer pupil = StationUpgradeGameTests.claimant(h, 5);
        h.getLevel().addNewPlayer(pupil); onlinePlayers(h).put(pupil.getUUID(), pupil);
        var definition = PuppeteerSummonDefinitions.byName(shape).orElseThrow();
        HemoCapabilityAccess.requireKnownSummons(pupil).learn(definition);
        ItemStack crossbar = new ItemStack(ItemInit.marionette_crossbar.get());
        MarionetteCrossbarItem.bindCrossbar(crossbar, pupil); MarionetteCrossbarItem.addThread(crossbar, 200);
        pupil.setItemInHand(InteractionHand.MAIN_HAND, crossbar); MarionetteCrossbarItem.prepareSelectedSummon(crossbar, pupil, shape);
        UUID id = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
        var teacher = professor.create(h.getLevel()); teacher.setNoAi(true); teacher.setNoGravity(true);
        teacher.setPos(pupil.position().add(1, 0, 0)); h.getLevel().addFreshEntity(teacher);
        h.assertTrue(CircusPracticalController.begin(pupil, teacher, shape, false), "exercise could not begin: " + shape);
        var threats = h.getLevel().getEntitiesOfClass(Mob.class, teacher.getBoundingBox().inflate(8),
                mob -> mob.getPersistentData().hasUUID(CircusPracticalController.EXAM)
                        && pupil.getUUID().equals(mob.getPersistentData().getUUID(CircusPracticalController.EXAM)));
        threats.forEach(mob -> { mob.setNoAi(true); mob.setNoGravity(true); });
        var body = PuppeteerSummonFactory.create(definition, h.getLevel(), pupil, id, 0).orElseThrow();
        body.setPos(teacher.position().add(1, 0, 0)); body.setNoAi(true); body.setNoGravity(true); h.getLevel().addFreshEntity(body);
        return new Exercise(h, pupil, teacher, crossbar, id, body, threats);
    }
    private static void strike(Exercise e, Mob attacker, Mob target) {
        target.invulnerableTime = 0;
        e.helper.assertTrue(target.hurt(e.helper.getLevel().damageSources().mobAttack(attacker), 2), "fixture hit rejected");
    }
    @GameTest(template="empty", timeoutTicks=40) public static void vultureNeedsAerialHitAndMatchingRecall(GameTestHelper h) {
        try (var e = exercise(h, "veinwing_vulture", EntityInit.circus_acrobat.get())) {
            strike(e, e.body, e.threats.get(0)); CircusPracticalController.recall(e.pupil, e.id, "veinwing_vulture");
            h.assertTrue(!e.passed(), "ground-only Vulture passed");
            e.body.setOnGround(false); e.body.setPos(e.body.position().add(0, 2, 0)); e.observe();
            CircusPracticalController.recall(e.pupil, UUID.randomUUID(), "veinwing_vulture");
            h.assertTrue(!e.passed(), "wrong Crossbar recall passed");
            CircusPracticalController.recall(e.pupil, e.id, "veinwing_vulture"); h.assertTrue(e.passed(), "valid aerial pursuit did not pass");
        } h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40) public static void jugglerNeedsThreeOwnedHitsOnOneThreat(GameTestHelper h) {
        try (var e = exercise(h, "marrow_spitter", EntityInit.circus_knife_thrower.get())) {
            e.threats.get(0).hurt(h.getLevel().damageSources().playerAttack(e.pupil), 1);
            strike(e, e.body, e.threats.get(0)); strike(e, e.body, e.threats.get(1)); strike(e, e.body, e.threats.get(1));
            h.assertTrue(!e.passed(), "split volley or player strike passed");
            strike(e, e.body, e.threats.get(1)); h.assertTrue(e.passed(), "focused volley did not pass");
        } h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40) public static void hulkNeedsFiveSecondsOfGuardAndARealGuardStrike(GameTestHelper h) {
        try (var e = exercise(h, "gorebound_hulk", EntityInit.circus_strongman.get())) {
            PuppeteerCrossbarCommands.setMode(e.pupil, e.crossbar, PuppeteerCommandMode.GUARD);
            for (int i=0; i<99; i++) e.observe();
            strike(e, e.body, e.threats.get(0)); h.assertTrue(!e.passed(), "Hulk passed before five seconds");
            e.observe(); h.assertTrue(e.passed(), "guard exercise did not pass");
        } h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40) public static void mummerMustActuallyDivertTwoThreats(GameTestHelper h) {
        try (var e = exercise(h, "scarlet_mummer", EntityInit.circus_stilt_walker.get())) {
            e.observe(); h.assertTrue(!e.passed(), "idle Mummer passed");
            e.body.tickCount = ScarletMummerRules.PERFORMANCE_INTERVAL_TICKS - 1; h.getLevel().tickNonPassenger(e.body);
            h.assertTrue(((ScarletMummerEntity)e.body).isPerforming(), "Mummer did not perform");
            e.observe(); h.assertTrue(e.passed(), "two actual diversions did not pass");
        } h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40) public static void houndNeedsARuptureCurAndKeepsCursAllied(GameTestHelper h) {
        try (var e = exercise(h, "sanguine_hound", EntityInit.circus_beast_tamer.get())) {
            strike(e, e.body, e.threats.get(0)); h.assertTrue(!e.passed(), "unruptured Hound passed");
            ((SanguineHoundEntity)e.body).ruptureIntoCurs();
            var curs = h.getLevel().getEntitiesOfClass(SanguineHoundEntity.class, e.teacher.getBoundingBox().inflate(10), mob -> e.pupil.getUUID().equals(mob.curOwner()));
            h.assertTrue(curs.size() >= 2 && !curs.get(0).canAttack(curs.get(1)), "rupture curs are missing or target one another");
            strike(e, curs.get(0), e.threats.get(0));
            h.assertTrue(CircusApprenticeshipProgress.practicalComplete(e.pupil, "sanguine_hound"), "owned rupture Cur did not pass");
        } h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40) public static void bellowsMustCatchTwoThreatsInOneActualBreath(GameTestHelper h) {
        try (var e = exercise(h, "cinder_bellows", EntityInit.circus_fire_eater.get())) {
            e.body.setPos(e.teacher.position());
            BoundSummonBehavior.setFocusedTarget(e.body, e.threats.get(0));
            for (int i=0; i<30; i++) h.getLevel().tickNonPassenger(e.body);
            h.assertTrue(e.passed(), "actual breath cone did not hit both rehearsal threats: cycle=" + ((CinderBellowsEntity)e.body).getBreathCycle()
                    + ", health=" + e.threats.stream().map(Mob::getHealth).toList());
        } h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40) public static void mnemonistMustReplayThePupilsOwnAttack(GameTestHelper h) {
        try (var e = exercise(h, "mnemonist_puppet", EntityInit.circus_understudy.get())) {
            Mob target = e.threats.get(0); e.body.setTarget(target);
            strike(e, e.body, target); target.invulnerableTime = 0; e.body.tickCount = 39;
            h.getLevel().tickNonPassenger(e.body); h.assertTrue(!e.passed(), "puppet replayed its own hit for mastery");
            target.invulnerableTime = 0; target.hurt(h.getLevel().damageSources().playerAttack(e.pupil), 4);
            target.invulnerableTime = 0; e.body.tickCount = 79; h.getLevel().tickNonPassenger(e.body);
            h.assertTrue(e.passed(), "actual player-origin replay did not pass");
        } h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40) public static void daggerVolleyMatchesLegacyBloodShotImpactAndBleeding(GameTestHelper h) throws Exception {
        Mob oldTarget = EntityType.PIG.create(h.getLevel()), newTarget = EntityType.PIG.create(h.getLevel());
        oldTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(80); oldTarget.setHealth(80);
        newTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(80); newTarget.setHealth(80);
        var shooter = EntityInit.marrow_spitter.get().create(h.getLevel());
        var old = new BloodShotEntity(h.getLevel(), shooter); old.setBaseDamage(5); old.setDeltaMovement(new Vec3(1.7,0,0));
        var oldHit = BloodShotEntity.class.getDeclaredMethod("onHitEntity", EntityHitResult.class); oldHit.setAccessible(true);
        oldHit.invoke(old, new EntityHitResult(oldTarget));
        var daggerHit = CircusKnifeProjectileEntity.class.getDeclaredMethod("onHitEntity", EntityHitResult.class); daggerHit.setAccessible(true);
        for (int i=0; i<3; i++) {
            var dagger = new CircusKnifeProjectileEntity(h.getLevel(), shooter, MarrowJugglerRules.daggerDamage(5));
            dagger.setPuppetDagger(); dagger.setDeltaMovement(new Vec3(1.7,0,0));
            daggerHit.invoke(dagger, new EntityHitResult(newTarget));
        }
        h.assertTrue(Math.abs(oldTarget.getHealth()-newTarget.getHealth()) < .001,
                "three dagger impacts changed the original raw damage budget: " + oldTarget.getHealth() + " vs " + newTarget.getHealth());
        h.assertTrue(newTarget.hasEffect(EffectInit.blood_loss) && oldTarget.hasEffect(EffectInit.blood_loss), "daggers lost the original Blood Loss");
        h.succeed();
    }
}
