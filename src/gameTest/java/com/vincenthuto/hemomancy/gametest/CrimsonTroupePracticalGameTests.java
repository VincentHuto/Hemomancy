package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.mission.circus.CircusPracticalController;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowJugglerRules;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerCommandMode;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerCrossbarCommands;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonFactory;
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
        HemoCapabilityAccess.getKnownSummons(pupil).learn(definition);
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
    @GameTest(template="empty", timeoutTicks=40) public static void aKilledThreatEndsAGuestActUnpaid(GameTestHelper h) {
        try (var e = exercise(h, "scarlet_mummer", EntityInit.circus_stilt_walker.get())) {
            CircusApprenticeshipProgress.completePractical(e.pupil, "scarlet_mummer");
            h.assertTrue(CircusPracticalController.begin(e.pupil, e.teacher, "scarlet_mummer", true), "guest act could not begin");
            var guests = h.getLevel().getEntitiesOfClass(Mob.class, e.teacher.getBoundingBox().inflate(8),
                    mob -> mob.isAlive() && mob.getPersistentData().hasUUID(CircusPracticalController.EXAM)
                            && e.pupil.getUUID().equals(mob.getPersistentData().getUUID(CircusPracticalController.EXAM)));
            h.assertTrue(guests.size() == 2, "the Mummer guest act brings two threats: " + guests.size());
            guests.forEach(mob -> { mob.setNoAi(true); mob.setNoGravity(true); });
            guests.get(0).discard(); e.observe();
            h.assertTrue(guests.get(1).isRemoved(), "the guest act did not end when a threat it needs was lost");
            h.assertTrue(CircusApprenticeshipProgress.state(e.pupil).getInt("guest.pending") == 0
                    && !CircusApprenticeshipProgress.state(e.pupil).contains("guest.day"), "an ended guest act paid");
        } h.succeed();
    }
    /** NQ-15-7: the volley keeps the old raw damage; the third dagger pins about 1.5 s and no dagger bleeds. */
    @GameTest(template="empty", timeoutTicks=40) public static void daggerVolleyKeepsItsDamageAndTheThirdDaggerPins(GameTestHelper h) throws Exception {
        Mob oldTarget = EntityType.PIG.create(h.getLevel()), newTarget = EntityType.PIG.create(h.getLevel());
        oldTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(80); oldTarget.setHealth(80);
        newTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(80); newTarget.setHealth(80);
        var shooter = EntityInit.marrow_spitter.get().create(h.getLevel());
        var old = new BloodShotEntity(h.getLevel(), shooter); old.setBaseDamage(5); old.setDeltaMovement(new Vec3(1.7,0,0));
        var oldHit = BloodShotEntity.class.getDeclaredMethod("onHitEntity", EntityHitResult.class); oldHit.setAccessible(true);
        oldHit.invoke(old, new EntityHitResult(oldTarget));
        var daggerHit = CircusKnifeProjectileEntity.class.getDeclaredMethod("onHitEntity", EntityHitResult.class); daggerHit.setAccessible(true);
        for (int tick = 0; tick <= 20; tick++) {
            if (!MarrowJugglerRules.throwTick(tick)) continue;
            h.assertTrue(!com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLock.isPinned(newTarget),
                    "the target was pinned before the volley's third dagger");
            var dagger = new CircusKnifeProjectileEntity(h.getLevel(), shooter, MarrowJugglerRules.daggerDamage(5));
            if (MarrowJugglerRules.pinsTarget(tick)) dagger.setPinDagger(); else dagger.setPuppetDagger();
            dagger.setDeltaMovement(new Vec3(1.7,0,0));
            newTarget.invulnerableTime = 0;
            daggerHit.invoke(dagger, new EntityHitResult(newTarget));
        }
        h.assertTrue(Math.abs(oldTarget.getHealth()-newTarget.getHealth()) < .001,
                "three dagger impacts changed the original raw damage budget: " + oldTarget.getHealth() + " vs " + newTarget.getHealth());
        h.assertTrue(!newTarget.hasEffect(EffectInit.blood_loss), "a Juggler dagger still causes Blood Loss");
        var pin = newTarget.getEffect(EffectInit.pinned);
        h.assertTrue(pin != null && pin.getDuration() == MarrowJugglerRules.PIN_TICKS, "the third dagger did not pin for 30 ticks: " + pin);
        h.assertTrue(newTarget.getArrowCount() == 0, "a dagger left a stuck arrow");
        h.succeed();
    }

    /** FX-26: a troupe knife and a plain blood needle leave no vanilla arrow stuck in what they hit. */
    @GameTest(template="empty", timeoutTicks=40) public static void needlesAndKnivesLeaveNoStuckArrow(GameTestHelper h) throws Exception {
        Mob target = EntityType.PIG.create(h.getLevel());
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(80); target.setHealth(80);
        target.setPos(h.absoluteVec(new Vec3(2, 2, 2))); h.getLevel().addFreshEntity(target);
        var thrower = EntityInit.circus_knife_thrower.get().create(h.getLevel());
        var knife = new CircusKnifeProjectileEntity(h.getLevel(), thrower, 2);
        knife.setDeltaMovement(new Vec3(1.7,0,0));
        var knifeHit = CircusKnifeProjectileEntity.class.getDeclaredMethod("onHitEntity", EntityHitResult.class); knifeHit.setAccessible(true);
        knifeHit.invoke(knife, new EntityHitResult(target));
        h.assertTrue(target.getArrowCount() == 0, "a troupe knife left " + target.getArrowCount() + " stuck arrows");
        var needle = new com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity(h.getLevel(), thrower);
        needle.setBaseDamage(2); needle.setDeltaMovement(new Vec3(1.7,0,0));
        var needleHit = com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity.class.getDeclaredMethod("onHitEntity", EntityHitResult.class);
        needleHit.setAccessible(true);
        target.invulnerableTime = 0;
        needleHit.invoke(needle, new EntityHitResult(target));
        h.assertTrue(target.getHealth() < 80, "the needle fixture did not land");
        h.assertTrue(target.getArrowCount() == 0, "a blood needle left " + target.getArrowCount() + " stuck arrows");
        target.discard();
        h.succeed();
    }

    private static Mob body(GameTestHelper h, ServerPlayer owner, UUID crossbar, String shape, Vec3 at) {
        var definition = PuppeteerSummonDefinitions.byName(shape).orElseThrow();
        HemoCapabilityAccess.getKnownSummons(owner).learn(definition);
        Mob body = PuppeteerSummonFactory.create(definition, h.getLevel(), owner, crossbar, 0).orElseThrow();
        body.setPos(at); body.setNoAi(true); body.setNoGravity(true); h.getLevel().addFreshEntity(body);
        return body;
    }
    private static ServerPlayer actor(GameTestHelper h, ItemStack crossbar, String selected) {
        ServerPlayer owner = StationUpgradeGameTests.claimant(h, 5);
        h.getLevel().addNewPlayer(owner); onlinePlayers(h).put(owner.getUUID(), owner);
        owner.setPos(h.absoluteVec(new Vec3(4, 2, 4)));
        MarionetteCrossbarItem.bindCrossbar(crossbar, owner); MarionetteCrossbarItem.addThread(crossbar, 200);
        owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
        MarionetteCrossbarItem.setSelectedSummonName(crossbar, selected);
        return owner;
    }

    /** D-38, NQ-15-9 A: with one Crossbar, the Pattern's Ring makes the Mummer and the Hound perform their acts at once. */
    @GameTest(template="empty", timeoutTicks=40) public static void ringPerformsEveryBodysActOnOneCrossbar(GameTestHelper h) {
        ItemStack crossbar = new ItemStack(ItemInit.marionette_crossbar.get());
        ServerPlayer owner = actor(h, crossbar, PuppeteerSummonDefinitions.RINGMASTER_PATTERN);
        UUID id = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
        Mob pattern = body(h, owner, id, PuppeteerSummonDefinitions.RINGMASTER_PATTERN, owner.position().add(1, 0, 0));
        Mob mummer = body(h, owner, id, PuppeteerSummonDefinitions.SCARLET_MUMMER, owner.position().add(3, 0, 0));
        Mob hound = body(h, owner, id, PuppeteerSummonDefinitions.SANGUINE_HOUND, owner.position().add(0, 0, 3));
        Mob threat = EntityType.ZOMBIE.create(h.getLevel());
        threat.setPos(owner.position().add(6, 0, 0)); threat.setNoAi(true); h.getLevel().addFreshEntity(threat);
        threat.setTarget(owner);
        try {
            h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActs.fire(owner, crossbar), "the Ring was refused");
            h.assertTrue(threat.getTarget() == mummer, "the Mummer's Spotlight did not take the threat: " + threat.getTarget());
            h.assertTrue(threat.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING), "the Hound's Scent did not mark the threat");
            int cost = com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetAct.RING.threadCost();
            h.assertTrue(MarionetteCrossbarItem.getThread(crossbar) == 200 - cost, "the Ring did not cost " + cost + " thread once: "
                    + MarionetteCrossbarItem.getThread(crossbar));
            h.assertTrue(!com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActs.fire(owner, crossbar)
                    && MarionetteCrossbarItem.getThread(crossbar) == 200 - cost, "a second Ring inside its cooldown was not refused");
        } finally {
            threat.discard(); pattern.discard(); mummer.discard(); hound.discard();
            com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActs.logout(owner);
            onlinePlayers(h).remove(owner.getUUID()); owner.discard();
        }
        h.succeed();
    }

    /** D-37, NQ-15-8 A: Trapeze swaps the owner and the Vulture; an act with nothing to do costs no thread. */
    @GameTest(template="empty", timeoutTicks=40) public static void trapezeSwapsOwnerAndVulture(GameTestHelper h) {
        ItemStack crossbar = new ItemStack(ItemInit.marionette_crossbar.get());
        ServerPlayer owner = actor(h, crossbar, PuppeteerSummonDefinitions.VEINWING_VULTURE);
        UUID id = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
        Vec3 ownerAt = owner.position();
        Mob vulture = body(h, owner, id, PuppeteerSummonDefinitions.VEINWING_VULTURE, ownerAt.add(4, 0, 0));
        Vec3 vultureAt = vulture.position();
        try {
            h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActs.fire(owner, crossbar), "Trapeze was refused");
            h.assertTrue(owner.position().distanceTo(vultureAt) < 0.01 && vulture.position().distanceTo(ownerAt) < 0.01,
                    "Trapeze did not swap: owner " + owner.position() + ", Vulture " + vulture.position());
            int cost = com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetAct.TRAPEZE.threadCost();
            h.assertTrue(MarionetteCrossbarItem.getThread(crossbar) == 200 - cost, "Trapeze cost the wrong thread");
            MarionetteCrossbarItem.setSelectedSummonName(crossbar, PuppeteerSummonDefinitions.SCARLET_MUMMER);
            Mob mummer = body(h, owner, id, PuppeteerSummonDefinitions.SCARLET_MUMMER, owner.position().add(2, 0, 0));
            h.assertTrue(!com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActs.fire(owner, crossbar)
                    && MarionetteCrossbarItem.getThread(crossbar) == 200 - cost, "a Spotlight with no threat spent thread");
            mummer.discard();
        } finally {
            vulture.discard();
            com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActs.logout(owner);
            onlinePlayers(h).remove(owner.getUUID()); owner.discard();
        }
        h.succeed();
    }
}
