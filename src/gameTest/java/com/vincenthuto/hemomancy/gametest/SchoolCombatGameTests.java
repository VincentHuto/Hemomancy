package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.*;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SchoolCombatGameTests {
    private static final String ARENA = "ductilis_arena";

    private static <T extends Mob> T body(GameTestHelper h, EntityType<T> type, int x) {
        T mob = h.spawn(type, new BlockPos(x, 3, 3));
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        mob.setHealth(100);
        mob.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20);
        return mob;
    }

    private static SchoolHitContext hit(LivingEntity owner, String ability, EnumBloodTendency school) {
        return SchoolHitContext.direct(Hemomancy.rloc(ability), school, null, owner);
    }

    private static void strike(LivingEntity target, LivingEntity owner, SchoolHitContext hit, float damage) {
        target.invulnerableTime = 0;
        target.hurt(HemoDamageTypes.attributed(target.damageSources().mobAttack(owner), hit, owner), damage);
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void lodestoneAmplifiesOnlyFerricKnockbackAndRespectsResistance(GameTestHelper h) {
        var caster = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 6);
        SchoolStates.apply(caster, target, SchoolState.LODESTONE, 160);
        target.setDeltaMovement(0, 0, 0);
        strike(target, caster, hit(caster, "ferric_probe", EnumBloodTendency.FERRIC), 1);
        h.assertTrue(Math.abs(target.getDeltaMovement().horizontalDistance() - .5) < .001, "Ferric hit did not add 25 percent knockback");
        target.setDeltaMovement(0, 0, 0);
        strike(target, caster, hit(caster, "lux_probe", EnumBloodTendency.LUX), 1);
        h.assertTrue(Math.abs(target.getDeltaMovement().horizontalDistance() - .4) < .001, "Ferric knockback leaked into a different same-tick hit");
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(.5);
        target.setDeltaMovement(0, 0, 0);
        strike(target, caster, hit(caster, "ferric_probe", EnumBloodTendency.FERRIC), 1);
        h.assertTrue(Math.abs(target.getDeltaMovement().horizontalDistance() - .25) < .001, "Lodestone bypassed knockback resistance");
        caster.discard(); target.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void ownedPillarStrengthensMarkedPullAndRedirectsFerricHits(GameTestHelper h) {
        var caster = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.HUSK, 6);
        var pillar = new com.vincenthuto.hemomancy.common.entity.summon.EntityIronPillar(
                com.vincenthuto.hemomancy.common.init.EntityInit.iron_pillar.get(), h.getLevel(), caster);
        pillar.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(4.5, 3, 3.5)));
        pillar.setNoGravity(true);
        pillar.setMagnetic(160);
        h.getLevel().addFreshEntity(pillar);
        target.setDeltaMovement(0, 0, 0);
        pillar.tick();
        double ordinary = target.getDeltaMovement().length();
        h.assertTrue(ordinary > 0, "Fixture did not produce native pillar pull");
        SchoolStates.apply(caster, target, SchoolState.LODESTONE, 160);
        target.setDeltaMovement(0, 0, 0);
        pillar.tick();
        h.assertTrue(Math.abs(target.getDeltaMovement().length() / ordinary - 1.5) < .001, "Marked pull was not 1.5 times stronger");
        target.setDeltaMovement(0, 0, 0);
        strike(target, caster, hit(caster, "ferric_probe", EnumBloodTendency.FERRIC), 1);
        h.assertTrue(target.getDeltaMovement().x < 0, "Ferric displacement did not point toward the owned pillar");
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        target.setDeltaMovement(0, 0, 0);
        pillar.tick();
        h.assertTrue(target.getDeltaMovement().lengthSqr() == 0, "Magnetic pull bypassed full knockback resistance");
        pillar.discard(); caster.discard(); target.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void delayedZoneApplicationsKeepTheirCastOrigin(GameTestHelper h) {
        var caster = DuctilisGameTests.player(h);
        var target = body(h, EntityType.HUSK, 6);
        var beacon = com.vincenthuto.hemomancy.common.init.ManipulationInit.hematic_beacon.get();
        var well = com.vincenthuto.hemomancy.common.init.ManipulationInit.eclipse_well.get();
        SchoolHitContext beaconHit = SchoolDamage.context(beacon, caster);
        SchoolHitContext wellHit = SchoolDamage.context(well, caster).withCharge(.5f);
        try (var scope = SchoolDamage.scope(beaconHit, caster)) {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.createHematicBeacon(
                    h.getLevel(), target.position(), 3, 40, caster.getUUID());
        }
        try (var scope = SchoolDamage.scope(wellHit, caster)) {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.createEclipseWell(
                    h.getLevel(), target.position(), 3, 40, caster.getUUID());
        }
        caster.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.living_blade.get()));
        h.runAfterDelay(25, () -> {
            try {
                var illumination = SchoolStates.data(target).get(SchoolState.ILLUMINATED);
                var obscurity = SchoolStates.data(target).get(SchoolState.OBSCURED);
                h.assertTrue(illumination != null && illumination.origin.equals(beaconHit), "Beacon refresh lost its cast identity");
                h.assertTrue(obscurity != null && obscurity.origin.equals(wellHit), "Well refresh lost its cast identity or charge");
                h.succeed();
            } finally { target.discard(); caster.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void hostileStateCastBreaksVeiledButExistingZoneRefreshDoesNot(GameTestHelper h) {
        var caster = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.HUSK, 6);
        SchoolStates.apply(caster, caster, SchoolState.VEILED, 120);
        var cast = hit(caster, "hemorrhage", EnumBloodTendency.MORTEM);
        try (var scope = SchoolDamage.scope(cast, caster)) {
            SchoolStates.apply(caster, target, SchoolState.NECROSIS, 120);
        }
        h.assertTrue(!SchoolStates.has(caster, SchoolState.VEILED), "Offensive non-damaging cast retained concealment");
        SchoolStates.apply(caster, caster, SchoolState.VEILED, 120);
        SchoolStates.apply(hit(caster, "eclipse_well", EnumBloodTendency.TENEBRIS), caster, target, SchoolState.OBSCURED, 25, 1);
        h.assertTrue(SchoolStates.has(caster, SchoolState.VEILED), "Existing darkness zone repeatedly broke concealment");
        caster.discard(); target.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void registeredSourceInventoryUsesAuthoredAssignments(GameTestHelper h) {
        for (var holder : com.vincenthuto.hemomancy.common.init.ManipulationInit.MANIPS.getEntries()) {
            var manipulation = holder.get();
            var context = SchoolDamage.context(manipulation, null);
            h.assertTrue(context.primary() == manipulation.getTend() && context.secondary() == manipulation.getSecondaryTend(),
                    "Registry school changed for " + manipulation.getName());
            Hemomancy.LOGGER.info("SCHOOL_SOURCE {} {} {} {} entity={} drudge={}",
                    manipulation.getName(), manipulation.getTend(), manipulation.getSecondaryTend(), context.role(),
                    com.vincenthuto.hemomancy.common.manipulation.EntityManipulationEffects.isSupported(manipulation),
                    manipulation.getDrudgeAction().filter(action -> action != com.vincenthuto.hemomancy.common.manipulation.DrudgeAction.DRUDGE_UNSUPPORTED).isPresent());
        }
        h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void drudgeJoltDoesNotInterruptAnImmuneTargetOrCastWhileDisrupted(GameTestHelper h) {
        var caster = body(h, EntityType.PIG, 2);
        var target = body(h, EntityType.HUSK, 6);
        var action = com.vincenthuto.hemomancy.common.init.ManipulationInit.synaptic_jolt.get().getDrudgeAction().orElseThrow();
        try {
            target.setInvulnerable(true);
            action.execute(caster, h.getLevel(), caster.blockPosition(), 7);
            h.assertTrue(!SchoolStates.has(target, SchoolState.DISRUPTED), "Immune Drudge target was interrupted");
            target.setInvulnerable(false);
            SchoolStates.apply(null, caster, SchoolState.DISRUPTED, 10);
            h.assertTrue(!action.execute(caster, h.getLevel(), caster.blockPosition(), 7)
                    && target.getHealth() == 100, "Disrupted Drudge started another cast");
            SchoolStates.clear(caster, SchoolState.DISRUPTED);
            action.execute(caster, h.getLevel(), caster.blockPosition(), 7);
            h.assertTrue(target.getHealth() < 100 && SchoolStates.has(target, SchoolState.DISRUPTED), "Permitted Drudge hit lost its damage or state");
            h.succeed();
        } finally { caster.discard(); target.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void necrosisExpiryIsIndependentOfAnEarlierConductivePayoff(GameTestHelper h) {
        var caster = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.HUSK, 6);
        var relay = body(h, EntityType.HUSK, 8);
        var context = SchoolHitContext.direct(Hemomancy.rloc("necrosis_arc_probe"), EnumBloodTendency.MORTEM,
                EnumBloodTendency.DUCTILIS, caster).withApplication(10, 1);
        SchoolStates.apply(caster, target, SchoolState.LODESTONE, 160);
        strike(target, caster, context, 2);
        h.assertTrue(SchoolStates.data(target).hasPaid(context.rootAttack()), "Fixture did not fire the conductive payoff");
        target.invulnerableTime = 0;
        target.hurt(h.getLevel().damageSources().mobAttack(caster), 8);
        float wounded = target.getHealth();
        float debt = SchoolStates.data(target).get(SchoolState.NECROSIS).stored;
        h.assertTrue(debt > 0, "Fixture did not bank the subsequent health damage");
        h.runAfterDelay(9, () -> target.invulnerableTime = 0);
        h.runAfterDelay(13, () -> {
            try {
                h.assertTrue(Math.abs(target.getHealth() - (wounded - debt)) < .001,
                        "Earlier arc swallowed the later Necrosis expiry");
                h.succeed();
            } finally { caster.discard(); target.discard(); relay.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void periodicKillsKeepMorphlingRewardsWithoutSpreadingAnotherState(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        var victim = body(h, EntityType.COW, 6);
        var nearby = body(h, EntityType.HUSK, 8);
        var specimen = com.vincenthuto.hemomancy.common.init.ItemInit.morphling_gravecap.get().getDefaultInstance();
        var maturity = new net.minecraft.nbt.CompoundTag();
        maturity.putFloat("EnzymePower", 100);
        specimen.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(maturity));
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireEquippedMorphling(player).setEquippedMorphling(specimen);
        try {
            var periodic = hit(player, "periodic_probe", EnumBloodTendency.MORTEM).child(SchoolHitContext.Kind.PERIODIC);
            victim.hurt(SchoolDamage.attributed(h.getLevel().damageSources().magic(), periodic, player), 200);
            var equipped = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireEquippedMorphling(player).getEquippedMorphling();
            h.assertTrue(com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem.getHusbandryProgress(equipped) >= 2,
                    "Periodic kill erased Gravecap's non-damaging reward");
            h.assertTrue(!SchoolStates.has(nearby, SchoolState.NECROSIS), "Periodic kill started another offensive chain");
            h.succeed();
        } finally { player.discard(); victim.discard(); nearby.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=45)
    public static void shieldsBlockBuildupConsumptionAndRetaliation(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for (int tick = 0; tick < 61; tick++) player.tick();
        var attacker = body(h, EntityType.COW, 2);
        attacker.setPos(player.position().add(0, 0, 2));
        player.setYRot(0);
        player.setXRot(0);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
        player.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
        for (int tick = 0; tick < 6; tick++) player.doTick();
        h.assertTrue(player.isBlocking(), "Fixture did not raise its shield");
        SchoolStates.apply(attacker, player, SchoolState.PRESSURE, 120);
        player.addEffect(new MobEffectInstance(EffectInit.iron_retort, 100));
        com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.armCoronation(player, 1, 1);
        h.runAfterDelay(6, () -> {
            try {
                float health = player.getHealth(), attackerHealth = attacker.getHealth();
                strike(player, attacker, hit(attacker, "blood_aneurysm", EnumBloodTendency.ANIMUS), 2);
                h.assertTrue(player.getHealth() == health && SchoolStates.levels(player, SchoolState.PRESSURE) == 1,
                        "Shielded rupture consumed pressure or damaged health");
                h.assertTrue(player.hasEffect(EffectInit.iron_retort) && attacker.getHealth() == attackerHealth,
                        "Shielded hit consumed or fired Iron Retort");
                strike(player, attacker, hit(attacker, "cold_probe", EnumBloodTendency.CONGEATIO), 2);
                h.assertTrue(!player.hasEffect(EffectInit.rime), "Shielded hit built Rime");
                h.assertTrue(h.getLevel().getEntitiesOfClass(
                        com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity.class,
                        player.getBoundingBox().inflate(3), needle -> needle.getOwner() == player).isEmpty(),
                        "Shielded hit spent a Coronation lance");
                h.succeed();
            } finally { player.discard(); attacker.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void projectileKeepsLaunchSchoolAndNativeAttributionAfterWeaponSwitch(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        var target = body(h, EntityType.COW, 6);
        var weapon = new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.living_crossbow.get());
        var bolt = new com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity(h.getLevel(), player, weapon);
        try {
            h.getLevel().addFreshEntity(bolt);
            var launch = SchoolDamage.projectileContext(bolt);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.living_torch.get()));
            var source = SchoolDamage.wrap(target, h.getLevel().damageSources().arrow(bolt, player));
            h.assertTrue(source instanceof SchoolDamageSource school && school.context().equals(launch)
                    && source.getDirectEntity() == bolt && source.getEntity() == player,
                    "Projectile attribution changed after launch");
            target.hurt(source, 2);
            h.assertTrue(target.hasEffect(EffectInit.conductive_mark) && !target.hasEffect(EffectInit.searing)
                    && !target.hasEffect(EffectInit.disrupted), "Crossbow hit inherited the switched torch or hard control");
            var restored = SchoolHitContext.load(launch.save());
            h.assertTrue(launch.equals(restored), "Projectile context did not round-trip through NBT");
            h.succeed();
        } finally { bolt.discard(); target.discard(); player.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void mixedAffinityIsAppliedOnceAndHeldWeaponsDoNotReschoolSpells(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        var target = body(h, com.vincenthuto.hemomancy.common.init.EntityInit.will.get(), 6);
        target.setSchool(EnumBloodTendency.MORTEM);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        try {
            com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodTendency(player).orElseThrow()
                    .setTendencyAlignment(EnumBloodTendency.ANIMUS, 100);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.living_torch.get()));
            var context = SchoolHitContext.direct(Hemomancy.rloc("affinity_probe"), EnumBloodTendency.ANIMUS,
                    EnumBloodTendency.LUX, player);
            float multiplier = com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules.damageMultiplier(
                    player, target, context.primary(), context.secondary());
            h.assertTrue(multiplier > 1, "Fixture did not exercise a non-neutral affinity");
            target.hurt(SchoolDamage.attributed(h.getLevel().damageSources().magic(), context, player), 4);
            h.assertTrue(Math.abs(100 - target.getHealth() - 4 * multiplier) < .001,
                    "Damage was adjusted zero or multiple times");
            h.assertTrue(!target.hasEffect(EffectInit.searing) && !target.hasEffect(EffectInit.illuminated),
                    "Held weapon or secondary metadata applied an extra school");
            h.succeed();
        } finally { target.discard(); player.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void authoredCounterattacksGetAffinityButFixedStateDamageDoesNot(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        var target = body(h, com.vincenthuto.hemomancy.common.init.EntityInit.will.get(), 6);
        target.setSchool(EnumBloodTendency.MORTEM);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodTendency(player).orElseThrow()
                .setTendencyAlignment(EnumBloodTendency.ANIMUS, 100);
        var context = SchoolHitContext.direct(Hemomancy.rloc("crimson_coronation"), EnumBloodTendency.ANIMUS,
                null, player).child(SchoolHitContext.Kind.REACTION);
        float multiplier = com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules.damageMultiplier(player, target, context.primary(), context.secondary());
        h.assertTrue(multiplier > 1, "Fixture did not exercise affinity");
        strike(target, player, context, 4);
        h.assertTrue(Math.abs(100 - target.getHealth() - 4 * multiplier) < .001, "Authored counterattack lost or doubled affinity");
        h.assertTrue(!target.hasEffect(EffectInit.sanguine_pressure), "Counterattack built a new state");
        float before = target.getHealth();
        target.invulnerableTime = 0;
        target.hurt(HemoDamageTypes.stateDamage(h.getLevel(), HemoDamageTypes.DECAY_RELEASE, context.newAttack().child(SchoolHitContext.Kind.REACTION), player), 2);
        h.assertTrue(Math.abs(before - target.getHealth() - 2) < .001, "Fixed Decay release received affinity again");
        target.discard(); player.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void postDamageMorphlingSurvivalChecksDoNotSubtractTheHitTwice(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for (int tick = 0; tick < 61; tick++) player.tick();
        var specimen = com.vincenthuto.hemomancy.common.init.ItemInit.morphling_winter_shroud.get().getDefaultInstance();
        var maturity = new net.minecraft.nbt.CompoundTag();
        maturity.putFloat("EnzymePower", 100);
        specimen.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(maturity));
        com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem.setLastAbilityTick(specimen, "TunMolt", -100000);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireEquippedMorphling(player).setEquippedMorphling(specimen);
        player.removeAllEffects();
        player.setHealth(20);
        player.hurt(h.getLevel().damageSources().magic(), 10);
        h.assertTrue(player.getHealth() == 10 && !player.hasEffect(MobEffects.INVISIBILITY), "Nonlethal hit triggered a low-health survival response early");
        player.invulnerableTime = 0;
        var periodic = hit(null, "searing_probe", EnumBloodTendency.FLAMMEUS).child(SchoolHitContext.Kind.PERIODIC);
        player.hurt(SchoolDamage.attributed(h.getLevel().damageSources().magic(), periodic, null), 4);
        h.assertTrue(player.getHealth() == 6 && player.hasEffect(MobEffects.INVISIBILITY),
                "Periodic damage erased Winter Shroud's non-offensive survival response: health=" + player.getHealth() + " effects=" + player.getActiveEffects());
        player.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void lumenlaceRescueKeepsLethalThresholdAndPeriodicProtection(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for (int tick = 0; tick < 61; tick++) player.tick();
        var specimen = com.vincenthuto.hemomancy.common.init.ItemInit.morphling_lumenlace.get().getDefaultInstance();
        var maturity = new net.minecraft.nbt.CompoundTag();
        maturity.putFloat("EnzymePower", 100);
        specimen.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(maturity));
        com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem.setLastAbilityTick(specimen, "InkMantleReprieve", -100000);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireEquippedMorphling(player).setEquippedMorphling(specimen);
        com.vincenthuto.hemomancy.common.event.LastRiteHelper.arm(player, com.vincenthuto.hemomancy.common.event.LastRiteHelper.INK_MANTLE_ID);
        var blood = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireBloodVolume(player);
        blood.setActive(true); blood.setBloodVolume(1000);
        player.removeAllEffects(); player.setHealth(20);
        player.hurt(h.getLevel().damageSources().magic(), 10);
        h.assertTrue(player.getHealth() == 10 && blood.getBloodVolume() == 950, "Nonlethal hit spent Ink Mantle early: health=" + player.getHealth() + " blood=" + blood.getBloodVolume());
        player.invulnerableTime = 0;
        var periodic = hit(null, "searing_probe", EnumBloodTendency.FLAMMEUS).child(SchoolHitContext.Kind.PERIODIC);
        player.hurt(SchoolDamage.attributed(h.getLevel().damageSources().magic(), periodic, null), 10);
        h.assertTrue(player.isAlive() && player.getHealth() == 8 && blood.getBloodVolume() == 400,
                "Lethal periodic damage bypassed Ink Mantle or changed its payment: health=" + player.getHealth() + " blood=" + blood.getBloodVolume());
        player.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void rimeHasExactMovementPenaltyAndFullSentenceOverridesTheBuildWindow(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 6);
        double movement = target.getAttributeValue(Attributes.MOVEMENT_SPEED);
        SchoolStates.apply(owner, target, SchoolState.RIME, 120);
        SchoolStates.apply(owner, target, SchoolState.RIME, 120);
        h.assertTrue(SchoolStates.levels(target, SchoolState.RIME) == 1, "Rime ignored its ten-tick build window");
        strike(target, owner, hit(owner, "rimebound_sentence", EnumBloodTendency.CONGEATIO).withApplication(120, 3), 2);
        h.assertTrue(SchoolStates.levels(target, SchoolState.RIME) == 3, "Full Sentence failed to establish maximum Rime");
        h.runAfterDelay(2, () -> {
            h.assertTrue(Math.abs(target.getAttributeValue(Attributes.MOVEMENT_SPEED) - movement * .55) < .0001,
                    "Rime movement penalty was not exactly 45 percent");
            h.assertTrue(!com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(target),
                    "Rime blocked attacks or casting");
            target.removeEffect(EffectInit.rime);
            h.assertTrue(Math.abs(target.getAttributeValue(Attributes.MOVEMENT_SPEED) - movement) < .0001,
                    "Cleansed Rime retained its movement modifier");
            h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=70)
    public static void savedMetersAndOwnersResumeWithoutAnImmediateExtraBurn(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 6);
        SchoolStates.apply(owner, target, SchoolState.NECROSIS, 160);
        SchoolStates.apply(owner, target, SchoolState.SEARING, 80);
        SchoolStates.data(target).get(SchoolState.NECROSIS).stored = 3;
        var saved = target.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        var context = SchoolStates.data(target).get(SchoolState.SEARING).origin;
        target.discard();
        h.runAfterDelay(25, () -> {
            var restored = EntityType.COW.create(h.getLevel());
            restored.load(saved);
            h.getLevel().addFreshEntity(restored);
            h.assertTrue(SchoolStates.data(restored).get(SchoolState.NECROSIS).stored == 3,
                    "Save/reload erased the bank");
            h.assertTrue(SchoolStates.data(restored).get(SchoolState.SEARING).origin.equals(context),
                    "Save/reload changed school ownership");
            h.runAfterDelay(5, () -> h.assertTrue(restored.getHealth() == 100, "Unloaded time produced an immediate extra burn"));
            h.runAfterDelay(22, () -> {
                try {
                    h.assertTrue(restored.getHealth() == 99, "Restored burn did not resume its single stream");
                    h.succeed();
                } finally { restored.discard(); }
            });
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=45)
    public static void combustionAndVerdictHaveOnePayoffAndRespectCounters(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 6);
        SchoolStates.apply(owner, target, SchoolState.SEARING, 80);
        h.runAfterDelay(25, () -> {
            float before = target.getHealth();
            strike(target, owner, hit(owner, "vitric_combustion", EnumBloodTendency.FLAMMEUS), 2);
            h.assertTrue(before - target.getHealth() == 5 && !target.hasEffect(EffectInit.searing),
                    "Combustion did not spend the remaining three ticks");
            SchoolStates.apply(owner, target, SchoolState.VEILED, 120);
            SchoolStates.apply(owner, target, SchoolState.ILLUMINATED, 120);
            before = target.getHealth();
            strike(target, owner, hit(owner, "white_verdict", EnumBloodTendency.LUX), 4);
            h.assertTrue(before - target.getHealth() == 6 && !target.hasEffect(EffectInit.illuminated),
                    "Verdict applied both bonuses or reapplied exposure");
            target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100));
            h.assertTrue(!SchoolStates.apply(owner, target, SchoolState.SEARING, 80), "Fire Resistance admitted Searing");
            h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void summonedMeleeRetainsItsSchoolButStartsANewRootForEachAttack(GameTestHelper h) {
        var caster = body(h, EntityType.COW, 2);
        var summon = body(h, EntityType.COW, 4);
        var target = body(h, EntityType.COW, 6);
        SchoolDamage.capture(summon, hit(caster, "conjuration", EnumBloodTendency.ANIMUS));
        target.hurt(summon.damageSources().mobAttack(summon), 2);
        h.runAfterDelay(12, () -> {
            target.invulnerableTime = 0;
            target.hurt(summon.damageSources().mobAttack(summon), 2);
            h.assertTrue(SchoolStates.levels(target, SchoolState.PRESSURE) == 2,
                    "Separate summoned melee attacks reused their spawn root");
            h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_contracts", timeoutTicks=40)
    public static void livingCircuitWaitsForConfirmedMeleeDamage(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        var target = body(h, EntityType.COW, 6);
        try {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.armLivingCircuit(player);
            target.setInvulnerable(true);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                    new net.neoforged.neoforge.event.entity.player.AttackEntityEvent(player, target));
            target.hurt(player.damageSources().playerAttack(player), 2);
            h.assertTrue(!target.hasEffect(EffectInit.conductive_mark), "Immune swing applied or spent the armed circuit");
            target.setInvulnerable(false);
            target.hurt(player.damageSources().playerAttack(player), 2);
            h.assertTrue(target.hasEffect(EffectInit.conductive_mark), "Confirmed melee did not spend the retained circuit");
            h.succeed();
        } finally { player.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=40)
    public static void confirmedDamageBuildsOncePerRootAndRupturesOnlyPreexistingPressure(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var victim = body(h, EntityType.COW, 5);
        var root = hit(owner, "blood_needle", EnumBloodTendency.ANIMUS);
        strike(victim, owner, root, 1);
        strike(victim, owner, root, 1);
        h.assertTrue(SchoolStates.levels(victim, SchoolState.PRESSURE) == 1, "One root built pressure twice");
        strike(victim, owner, hit(owner, "blood_shot", EnumBloodTendency.ANIMUS), 1);
        strike(victim, owner, hit(owner, "blood_shot", EnumBloodTendency.ANIMUS), 1);
        float health = victim.getHealth();
        strike(victim, owner, hit(owner, "blood_aneurysm", EnumBloodTendency.ANIMUS), 2);
        h.assertTrue(Math.abs(health - victim.getHealth() - 8) < .001, "Aneurysm did not add six rupture damage");
        h.assertTrue(!victim.hasEffect(EffectInit.sanguine_pressure), "Rupture reapplied pressure");
        var skeleton = body(h, EntityType.SKELETON, 8);
        strike(skeleton, owner, hit(owner, "blood_shot", EnumBloodTendency.ANIMUS), 2);
        h.assertTrue(skeleton.getHealth() < 100 && !skeleton.hasEffect(EffectInit.sanguine_pressure), "Bloodless immunity erased damage or admitted pressure");
        h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=40)
    public static void absorptionCountsButInvulnerabilityDoesNotBuildOrConsume(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var victim = body(h, EntityType.COW, 5);
        victim.setAbsorptionAmount(5);
        strike(victim, owner, hit(owner, "blood_needle", EnumBloodTendency.ANIMUS), 1);
        h.assertTrue(victim.getHealth() == 100 && SchoolStates.levels(victim, SchoolState.PRESSURE) == 1,
                "Absorption damage did not confirm state application");
        victim.setInvulnerable(true);
        strike(victim, owner, hit(owner, "blood_aneurysm", EnumBloodTendency.ANIMUS), 2);
        h.assertTrue(SchoolStates.levels(victim, SchoolState.PRESSURE) == 1, "Immune hit consumed pressure");
        victim.setInvulnerable(false);
        var periodic = hit(owner, "blood_cloud", EnumBloodTendency.ANIMUS).child(SchoolHitContext.Kind.PERIODIC);
        strike(victim, owner, periodic, 1);
        h.assertTrue(SchoolStates.levels(victim, SchoolState.PRESSURE) == 1, "Periodic damage built pressure");
        h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=40)
    public static void attributedDamageRetainsNativeArmorAndEntities(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var plain = body(h, EntityType.COW, 5);
        var attributed = body(h, EntityType.COW, 8);
        plain.getAttribute(Attributes.ARMOR).setBaseValue(12);
        attributed.getAttribute(Attributes.ARMOR).setBaseValue(12);
        var context = hit(owner, "contract_probe", EnumBloodTendency.LUX).child(SchoolHitContext.Kind.REACTION);
        var nativeSource = h.getLevel().damageSources().mobAttack(owner);
        var source = HemoDamageTypes.attributed(nativeSource, context, owner);
        h.assertTrue(source.typeHolder().equals(nativeSource.typeHolder()) && source.getEntity() == owner
                && source.getDirectEntity() == owner, "Attribution changed native source fields");
        plain.hurt(nativeSource, 10);
        attributed.hurt(source, 10);
        h.assertTrue(Math.abs(plain.getHealth() - attributed.getHealth()) < .001, "Wrapping changed armor mitigation");
        h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=45)
    public static void luxDoesNotConsumeVanillaGlowingAndOwnPayoffWins(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 5);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200));
        strike(target, owner, hit(owner, "prismatic_reproof", EnumBloodTendency.LUX), 2);
        h.assertTrue(target.getHealth() == 98, "Vanilla glow qualified for Lux payoff");
        SchoolStates.apply(owner, target, SchoolState.ILLUMINATED, 120);
        strike(target, owner, hit(owner, "prismatic_reproof", EnumBloodTendency.LUX), 2);
        h.assertTrue(target.getHealth() == 94 && !target.hasEffect(EffectInit.illuminated), "Reproof did not consume exposure for four damage");
        h.assertTrue(target.hasEffect(MobEffects.GLOWING), "School consumption removed vanilla glow");
        SchoolStates.apply(owner, target, SchoolState.RIME, 120, 3);
        var ferric = hit(owner, "ferric_probe", EnumBloodTendency.FERRIC);
        strike(target, owner, ferric, 1);
        h.assertTrue(target.getHealth() == 89 && !target.hasEffect(EffectInit.rime), "Ferric shatter did not add four damage");
        strike(target, owner, ferric, 1);
        h.assertTrue(target.getHealth() == 88, "Same root repeated the shatter payoff");
        h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=45)
    public static void necrosisBanksHealthOnlyAndCleansingSafelyDropsDebt(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 5);
        SchoolStates.apply(owner, target, SchoolState.NECROSIS, 15);
        target.setAbsorptionAmount(4);
        strike(target, owner, hit(owner, "test", EnumBloodTendency.LUX), 12);
        h.assertTrue(SchoolStates.data(target).get(SchoolState.NECROSIS).stored == 2, "Debt counted absorption");
        target.heal(4);
        h.assertTrue(target.getHealth() == 95, "Necrosis healing suppression was not 25 percent");
        target.removeEffect(EffectInit.necrosis);
        h.assertTrue(SchoolStates.data(target).get(SchoolState.NECROSIS) == null, "Cleanse retained debt");
        h.runAfterDelay(20, () -> {
            h.assertTrue(target.getHealth() == 95, "Cleansed debt released damage");
            h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=45)
    public static void naturalNecrosisExpiryReleasesStoredDamageOnce(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 5);
        SchoolStates.apply(owner, target, SchoolState.NECROSIS, 25);
        strike(target, owner, hit(owner, "test", EnumBloodTendency.LUX), 8);
        h.runAfterDelay(30, () -> {
            h.assertTrue(target.getHealth() == 90, "Natural expiry did not release the two damage bank");
            h.assertTrue(!target.hasEffect(EffectInit.necrosis), "Natural expiry retained state");
            h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=110)
    public static void searingRefreshKeepsOneTickStreamAndThermalOppositionHasNoBurst(GameTestHelper h) {
        var owner = body(h, EntityType.COW, 2);
        var target = body(h, EntityType.COW, 5);
        SchoolStates.apply(owner, target, SchoolState.SEARING, 80);
        h.runAfterDelay(10, () -> SchoolStates.apply(owner, target, SchoolState.SEARING, 80));
        h.runAfterDelay(85, () -> {
            h.assertTrue(target.getHealth() == 96, "Refresh stacked or reset the searing tick stream");
            SchoolStates.apply(owner, target, SchoolState.RIME, 120);
            h.assertTrue(!target.hasEffect(EffectInit.searing) && !target.hasEffect(EffectInit.rime), "Cold did not spend itself extinguishing heat");
            SchoolStates.apply(owner, target, SchoolState.RIME, 120, 3);
            SchoolStates.apply(owner, target, SchoolState.SEARING, 80);
            h.assertTrue(SchoolStates.levels(target, SchoolState.RIME) == 2 && !target.hasEffect(EffectInit.searing), "Heat did not remove one Rime level");
            h.assertTrue(target.getHealth() == 96, "Thermal opposition created damage");
            h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=ARENA, batch="school_combat", timeoutTicks=40)
    public static void obscuredBrainAndOrdinaryTargetsRespectCloseDefenseAndExposure(GameTestHelper h) {
        var observer = body(h, EntityType.SKELETON, 2);
        var target = body(h, EntityType.COW, 10);
        observer.setTarget(target);
        SchoolStates.apply(target, observer, SchoolState.OBSCURED, 120);
        var brain = body(h, EntityType.PIGLIN, 3);
        brain.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
        SchoolStates.apply(target, brain, SchoolState.OBSCURED, 120);
        h.runAfterDelay(2, () -> {
            h.assertTrue(observer.getTarget() == null && brain.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty(),
                    "Obscured did not clear both targeting systems");
            observer.setTarget(target);
            h.assertTrue(observer.getTarget() == null, "Distant target was immediately reacquired");
            SchoolStates.apply(observer, target, SchoolState.ILLUMINATED, 120);
            observer.setTarget(target);
            h.assertTrue(observer.getTarget() == target, "Illuminated target stayed concealed");
            target.removeEffect(EffectInit.illuminated);
            target.setPos(observer.position().add(2, 0, 0));
            observer.setTarget(target);
            h.assertTrue(observer.getTarget() == target, "Obscured prevented close defense");
            h.succeed();
        });
    }
}
