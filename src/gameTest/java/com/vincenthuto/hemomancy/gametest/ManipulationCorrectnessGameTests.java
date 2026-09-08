package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationCorrectnessGameTests {
    private static final String EMPTY = "bastion/mobs/empty";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void exsanguinateOnlyRefundsAnExecutedBloodVictim(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (boolean bloodless : List.of(false, true)) {
                LivingEntity target = bloodless ? EntityType.SKELETON.create(h.getLevel()) : EntityType.ZOMBIE.create(h.getLevel());
                target.setPos(p.position().add(0, 0, 2));
                target.setHealth(4);
                target.setInvulnerable(!bloodless);
                h.getLevel().addFreshEntity(target);
                try {
                    ManipulationInit.exsanguinate.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                    near(h, 2000, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Rejected/non-blood victim refunded blood");
                } finally { target.discard(); }
            }
            LivingEntity victim = EntityType.ZOMBIE.create(h.getLevel());
            victim.setPos(p.position().add(0, 0, 2));
            victim.setHealth(1);
            h.getLevel().addFreshEntity(victim);
            try {
                ManipulationInit.exsanguinate.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                h.assertTrue(!victim.isAlive(), "Valid execution failed");
                near(h, 2600, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Valid execution did not refund once");
                ManipulationInit.exsanguinate.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                near(h, 2600, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Dead victim refunded twice");
            } finally { victim.discard(); }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void fixedPayloadsRejectIncompleteChargeBeforePayment(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (BloodManipulation m : List.of(ManipulationInit.hematic_mortar.get(), ManipulationInit.blood_needle_lance.get(),
                    ManipulationInit.ironhearted.get(), ManipulationInit.thread_ripper.get())) {
                select(p, m);
                for (float ticks : new float[]{0, 1, m.getRequiredChargeTicks() / 2F, m.getRequiredChargeTicks() - 1}) {
                    boolean cast = m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), ticks);
                    h.assertTrue(!cast, m.getName() + " accepted incomplete charge " + ticks);
                    near(h, 2000, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Incomplete charge spent blood");
                    h.assertTrue(!BloodManipulation.isAnyManipOnCooldown(p), "Incomplete charge started cooldown");
                }
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void bloodRushHasABoundedSharedCadence(GameTestHelper h) {
        var effect = EffectInit.blood_rush.get();
        for (int duration : new int[]{20, 100, 250}) {
            int pulses = 0;
            for (int remaining = duration; remaining > 0; remaining--) {
                if (effect.shouldApplyEffectTickThisTick(remaining, 1)) pulses++;
            }
            near(h, duration / 20, pulses, "Blood Rush pulse budget for " + duration);
        }
        ServerPlayer p = player(h);
        try {
            p.setHealth(10);
            effect.applyEffectTick(p, 2);
            near(h, 10.5, p.getHealth(), "Weapon amplifier changed shared healing amount");
            p.setHealth(p.getMaxHealth());
            effect.applyEffectTick(p, 1);
            near(h, p.getMaxHealth(), p.getHealth(), "Healing exceeded max HP");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void channelStartHonorsCooldownAndWardDepletion(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var ward = ManipulationInit.sanguine_ward.get();
            select(p, ward);
            ManipulationChannelManager.start(p);
            h.assertTrue(ManipulationChannelManager.isChanneling(p.getUUID()), "Ward did not start");
            double paid = HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
            ManipulationChannelManager.start(p);
            near(h, paid, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Duplicate START paid again");
            ManipulationChannelManager.stop(p);
            ManipulationChannelManager.start(p);
            h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()), "Channel restarted during cooldown");
            near(h, paid, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Blocked restart spent blood");
            var damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().generic(), 10));
            ManipulationReactiveEvents.onIncomingDamage(damage);
            near(h, 4, damage.getAmount(), "Initial Ward pool changed");
            ManipulationReactiveEvents.refreshSanguineWard(p);
            damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().generic(), 10));
            ManipulationReactiveEvents.onIncomingDamage(damage);
            near(h, 8, damage.getAmount(), "Depleted Ward incorrectly re-granted initial six points");
            h.succeed();
        } finally { ManipulationChannelManager.stop(p, false); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void endlessHourDefersAndSettlesOnce(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            ManipulationInit.endless_hour.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            p.removeEffect(MobEffects.ABSORPTION);
            p.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            p.setAbsorptionAmount(0);
            p.setHealth(5);
            var damage = new LivingDamageEvent.Pre(p, new DamageContainer(p.damageSources().generic(), 10));
            NeoForge.EVENT_BUS.post(damage);
            near(h, 4, damage.getNewDamage(), "Endless Hour did not preserve one HP");
            near(h, 6, p.getPersistentData().getFloat("hemomancy:endless_hour_deferred"), "Wrong deferred damage");
            p.setHealth(1);
            damage = new LivingDamageEvent.Pre(p, new DamageContainer(p.damageSources().generic(), 3));
            NeoForge.EVENT_BUS.post(damage);
            near(h, 0, damage.getNewDamage(), "Second hit crossed survival floor");
            p.setHealth(20);
            p.setAbsorptionAmount(20);
            p.getPersistentData().putLong("hemomancy:endless_hour_expiry", h.getLevel().getGameTime());
            EndlessHourManip.tickEndlessHour(p);
            near(h, 11, p.getHealth(), "Debt was lost or mitigated again");
            EndlessHourManip.tickEndlessHour(p);
            near(h, 11, p.getHealth(), "Debt collected twice");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void osseousBloomCapsFinalBossDamage(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (boolean trained : List.of(false, true)) {
                if (trained) {
                    HemoCapabilityAccess.requireSkillProgress(p).setSkill(
                            com.vincenthuto.hemomancy.common.init.SkillPointInit.skill_crimson_mastery,
                            com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates.UNLOCKED, 3);
                    h.assertTrue(com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(p) > 1, "Mastery fixture did not increase damage");
                }
                for (int health : new int[]{20, 100, 1000}) {
                    var boss = EntityType.WITHER.create(h.getLevel());
                    boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
                    boss.getAttribute(Attributes.ARMOR).setBaseValue(0);
                    boss.setHealth(health);
                    boss.setNoAi(true);
                    boss.setPos(p.position().add(0, 0, 2));
                    h.getLevel().addFreshEntity(boss);
                    try {
                        var bloom = ManipulationInit.osseous_bloom.get();
                        bloom.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                        float raw = health * .25F * (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(p);
                        float adjusted = TendencyAffinityRules.adjustManipulationDamage(p, boss, bloom, raw);
                        near(h, Math.min(12, adjusted), health - boss.getHealth(), "Final boss damage, health=" + health + ", trained=" + trained);
                    } finally { boss.discard(); }
                }
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void bloodLossBaseAmplifierDrainsBlood(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (int amplifier=0; amplifier<3; amplifier++) {
                var blood=HemoCapabilityAccess.requireBloodVolume(p);
                blood.setBloodVolume(2000);
                for (int i=0;i<20;i++) EffectInit.blood_loss.get().applyEffectTick(p, amplifier);
                near(h, 2000-10*(amplifier+1), blood.getBloodVolume(), "Blood Loss tier " + amplifier);
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness", timeoutTicks = 180)
    public static void beaconPulseActuallyHeals(GameTestHelper h) {
        ServerPlayer p = player(h);
        h.getLevel().addNewPlayer(p);
        p.setNoGravity(true);
        p.getFoodData().setFoodLevel(10);
        p.setHealth(10);
        ManipulationReactiveEvents.createHematicBeacon(h.getLevel(), p.position(), 3, 160, p.getUUID());
        near(h, 10.4, p.getHealth(), "Beacon pulse did not heal its 0.4 HP share");
        h.runAfterDelay(160, () -> {
            try {
                near(h, 13.2, p.getHealth(), "Eight-second Beacon total");
                h.succeed();
            } finally { p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void funeralBellScalesEveryStatusBonusAndPayment(GameTestHelper h) {
        var statuses = List.of(MobEffects.POISON, EffectInit.blood_loss, EffectInit.grave_debt, MobEffects.WITHER);
        for (int count = 0; count <= 4; count++) {
            for (int ticks : new int[]{1, 40, 80}) {
                ServerPlayer p = player(h);
                var target = EntityType.COW.create(h.getLevel());
                target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
                target.setHealth(100);
                target.setPos(p.position().add(0, 0, 1));
                h.getLevel().addFreshEntity(target);
                try {
                    for (int i = 0; i < count; i++) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(statuses.get(i), 1));
                    var bell = ManipulationInit.funeral_bell.get();
                    select(p, bell);
                    double cost = ManipulationCostLedger.collect(p, bell, 1).effectiveCost();
                    h.assertTrue(bell.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), ticks), "Bell release rejected");
                    near(h, 2000 - cost * ticks / 80, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Bell charge payment");
                    float raw = new float[]{4, 6, 8, 10, 12}[count] * ticks / 80;
                    near(h, TendencyAffinityRules.adjustManipulationDamage(p, target, bell, raw), 100 - target.getHealth(), "Bell damage at " + ticks + " ticks, statuses=" + count);
                    if (count > 0) near(h, Math.round(120F * ticks / 80), target.getEffect(MobEffects.WITHER).getDuration(), "Bell wither duration");
                } finally { target.discard(); p.discard(); }
            }
        }
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void fixedProjectilesStillFireAtFullCharge(GameTestHelper h) {
        for (BloodManipulation m : List.of(ManipulationInit.hematic_mortar.get(), ManipulationInit.blood_needle_lance.get())) {
            ServerPlayer p = player(h);
            try {
                select(p, m);
                double cost = ManipulationCostLedger.collect(p, m, 1).effectiveCost();
                h.assertTrue(m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), m.getRequiredChargeTicks()), "Full cast rejected: " + m.getName());
                near(h, 2000 - cost, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Full cast payment");
                var projectiles = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,
                        p.getBoundingBox().inflate(5), projectile -> projectile.getOwner() == p);
                near(h, m == ManipulationInit.hematic_mortar.get() ? 1 : 3, projectiles.size(), "Full projectile count");
                projectiles.forEach(net.minecraft.world.entity.Entity::discard);
            } finally { p.discard(); }
        }
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void anotherChannelStopsOnSelectionDeathAndDimension(GameTestHelper h) {
        for (int reason = 0; reason < 3; reason++) {
            ServerPlayer p = player(h);
            try {
                select(p, ManipulationInit.penumbral_drift.get());
                ManipulationChannelManager.start(p);
                h.assertTrue(ManipulationChannelManager.isChanneling(p.getUUID()), "Drift failed to start");
                if (reason == 0) HemoCapabilityAccess.requireKnownManipulations(p).setSelectedManip(ManipulationInit.blood_shot.get());
                if (reason == 1) p.setHealth(0);
                if (reason == 2) NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(p, net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.level.Level.NETHER));
                ManipulationChannelManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
                h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()), "Drift channel survived interruption " + reason);
            } finally { ManipulationChannelManager.stop(p, false); p.discard(); }
        }
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void endlessHourCannotEraseDebtByRecastOrLogout(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var hour = ManipulationInit.endless_hour.get();
            hour.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            p.setHealth(5);
            p.setAbsorptionAmount(2);
            var damage = new LivingDamageEvent.Pre(p, new DamageContainer(p.damageSources().generic(), 10));
            NeoForge.EVENT_BUS.post(damage);
            near(h, 6, damage.getNewDamage(), "Absorption was deferred instead of consumed normally");
            near(h, 4, p.getPersistentData().getFloat("hemomancy:endless_hour_deferred"), "Absorption changed owed health");
            double bloodBeforeRecast = HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
            h.assertTrue(!hour.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Recast accepted");
            near(h, bloodBeforeRecast, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Blocked recast charged blood");
            hour.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            near(h, 4, p.getPersistentData().getFloat("hemomancy:endless_hour_deferred"), "Direct action erased debt");
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(p, net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.level.Level.NETHER));
            near(h, 4, p.getPersistentData().getFloat("hemomancy:endless_hour_deferred"), "Dimension event lost debt");
            p.setHealth(20);
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
            near(h, 16, p.getHealth(), "Logout did not settle debt before save");
            h.assertTrue(!p.getPersistentData().contains("hemomancy:endless_hour_expiry"), "Logout retained hour");
            EndlessHourManip.tickEndlessHour(p);
            near(h, 16, p.getHealth(), "Logout repayment repeated");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void osseousBloomRetainsOrdinaryHealthScaling(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (int health : new int[]{20, 100, 1000}) {
                var target = EntityType.COW.create(h.getLevel());
                target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
                target.setHealth(health);
                target.setPos(p.position().add(0, 0, 2));
                h.getLevel().addFreshEntity(target);
                try {
                    var bloom = ManipulationInit.osseous_bloom.get();
                    bloom.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                    near(h, TendencyAffinityRules.adjustManipulationDamage(p, target, bloom, health * .25F), health - target.getHealth(), "Ordinary HP scaling at " + health);
                } finally { target.discard(); }
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void effectsRespectDurationAndMobDamageImmunity(GameTestHelper h) {
        var healed = EntityType.COW.create(h.getLevel());
        healed.setNoAi(true);
        healed.setNoGravity(true);
        healed.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        healed.setHealth(10);
        healed.addEffect(new net.minecraft.world.effect.MobEffectInstance(EffectInit.blood_rush, 250, 1));
        for (int tick = 1; tick <= 250; tick++) {
            healed.tick();
            if (tick == 20) near(h, 10.5, healed.getHealth(), "One-second Rush healing");
            if (tick == 100) near(h, 12.5, healed.getHealth(), "Five-second Rush healing");
        }
        near(h, 16, healed.getHealth(), "Full Rush duration healing");
        for (int amplifier = 0; amplifier < 3; amplifier++) {
            var mob = EntityType.COW.create(h.getLevel());
            mob.setNoAi(true);
            mob.setNoGravity(true);
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(EffectInit.blood_loss, 20, amplifier));
            for (int tick = 0; tick < 20; tick++) mob.tick();
            near(h, mob.getMaxHealth() - 1, mob.getHealth(), "Mob Blood Loss immunity-frame budget, amplifier=" + amplifier);
        }
        var bloodless = EntityType.SKELETON.create(h.getLevel());
        EffectInit.blood_loss.get().applyEffectTick(bloodless, 2);
        near(h, bloodless.getMaxHealth(), bloodless.getHealth(), "Bloodless mob took bleed damage");
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void endlessHourRunsThroughActualDamageAndDeath(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (int i = 0; i < 60; i++) p.tick(); // Let the normal spawn protection expire.
            HemoCapabilityAccess.requireBloodVolume(p).setActive(false);
            ManipulationInit.endless_hour.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            p.removeEffect(MobEffects.ABSORPTION);
            p.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            p.setAbsorptionAmount(0);
            p.setHealth(5);
            p.hurt(p.damageSources().generic(), 10);
            near(h, 1, p.getHealth(), "Real lethal hit crossed floor");
            near(h, 6, p.getPersistentData().getFloat("hemomancy:endless_hour_deferred"), "Real hit debt");
            p.invulnerableTime = 0;
            p.hurt(p.damageSources().generic(), 3);
            near(h, 1, p.getHealth(), "Second real hit crossed floor");
            near(h, 9, p.getPersistentData().getFloat("hemomancy:endless_hour_deferred"), "Real hits did not accumulate");
            p.getPersistentData().putLong("hemomancy:endless_hour_expiry", h.getLevel().getGameTime());
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(!p.isAlive(), "Unpayable debt did not kill");
            h.assertTrue(!p.getPersistentData().contains("hemomancy:endless_hour_deferred"), "Death retained debt");
            EndlessHourManip.tickEndlessHour(p);
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness", timeoutTicks = 40)
    public static void channelUpkeepPaysOncePerSecond(GameTestHelper h) {
        ServerPlayer p = player(h);
        select(p, ManipulationInit.sanguine_ward.get());
        ManipulationChannelManager.start(p);
        double afterStart = HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
        double pulseCost = 2000 - afterStart;
        h.assertTrue(pulseCost > 0, "Initial pulse was unpaid");
        h.runAfterDelay(19, () -> {
            ManipulationChannelManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            near(h, afterStart, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Early upkeep charge");
        });
        h.runAfterDelay(21, () -> {
            try {
                ManipulationChannelManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
                near(h, afterStart - pulseCost, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Upkeep did not pay one pulse");
                ManipulationChannelManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
                near(h, afterStart - pulseCost, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Same-tick upkeep paid twice");
                h.succeed();
            } finally { ManipulationChannelManager.stop(p, false); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_correctness")
    public static void endlessHourTransfersOnEndReturnButNotDeath(GameTestHelper h) {
        for (boolean death : List.of(false, true)) {
            ServerPlayer original = player(h);
            ServerPlayer replacement = player(h);
            try {
                ManipulationInit.endless_hour.get().getAction(original, h.getLevel(), ItemStack.EMPTY, original.blockPosition());
                EndlessHourManip.accumulateDeferredDamage(original, 7);
                long expiry = original.getPersistentData().getLong("hemomancy:endless_hour_expiry");
                NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(replacement, original, death));
                near(h, death ? 0 : 7, replacement.getPersistentData().getFloat("hemomancy:endless_hour_deferred"), "Clone debt transfer, death=" + death);
                near(h, death ? 0 : expiry, replacement.getPersistentData().getLong("hemomancy:endless_hour_expiry"), "Clone deadline transfer");
                h.assertTrue(!original.getPersistentData().contains("hemomancy:endless_hour_deferred"), "Old player retained collectible debt");
            } finally { original.discard(); replacement.discard(); }
        }
        h.succeed();
    }

    private static void near(GameTestHelper h, double expected, double actual, String message) {
        h.assertTrue(Math.abs(expected-actual)<0.002, message + ": expected " + expected + ", got " + actual);
    }

    private static void select(ServerPlayer p, BloodManipulation m) {
        var known=HemoCapabilityAccess.requireKnownManipulations(p);
        known.getKnownManips().put(m,new ManipLevel(4,185));
        String baseline=com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.baselineId(m.getName());
        known.getKnownManips().put(ManipulationInit.getByName(baseline),new ManipLevel(4,185));
        known.setEquippedManipNames(List.of(m.getName()));
        known.setSelectedManip(m);
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"correctness"),false);
        ServerPlayer p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        };
        p.setPos(h.absoluteVec(new Vec3(2.5,2,2.5)));
        var blood=HemoCapabilityAccess.requireBloodVolume(p);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        for (var tendency:EnumBloodTendency.values())
            HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(tendency,100);
        return p;
    }
}
