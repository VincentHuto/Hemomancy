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
public final class ManipulationTargetingGameTests {
    private static final String EMPTY = "bastion/mobs/empty";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void aimedRayStopsAtWallAndGlassButPassesOpening(GameTestHelper h) {
        ServerPlayer p = player(h);
        var target = EntityType.ZOMBIE.create(h.getLevel());
        target.setPos(p.position().add(0, 0, 4));
        h.getLevel().addFreshEntity(target);
        try {
            for (var block : List.of(net.minecraft.world.level.block.Blocks.STONE, net.minecraft.world.level.block.Blocks.GLASS)) {
                h.setBlock(2, 3, 4, block);
                h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.DeadlyGazeManip.rayTraceEntities(p, 12, e -> e == target) == null, "Ray crossed " + block);
            }
            for (var block : List.of(net.minecraft.world.level.block.Blocks.AIR, net.minecraft.world.level.block.Blocks.STONE_SLAB)) {
                h.setBlock(2, 3, 4, block);
                h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.DeadlyGazeManip.rayTraceEntities(p, 12, e -> e == target) != null, "Ray failed through opening/above slab");
            }
            h.setBlock(2, 3, 4, net.minecraft.world.level.block.Blocks.GLASS_PANE);
            target.setPos(p.position().add(0, 0, 2.4));
            h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.DeadlyGazeManip.rayTraceEntities(p, 12, e -> e == target) == null, "Hitbox padding reached through thin cover");
            h.setBlock(2, 3, 4, net.minecraft.world.level.block.Blocks.AIR);
            target.setPos(p.position().add(0, 0, -3));
            h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.DeadlyGazeManip.rayTraceEntities(p, 12, e -> e == target) == null, "Ray selected behind caster");
            h.succeed();
        } finally { h.setBlock(2, 3, 4, net.minecraft.world.level.block.Blocks.AIR); target.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void hostileSelectionProtectsOwnedCreaturesAndPvpDisabledPlayers(GameTestHelper h) {
        ServerPlayer p = player(h), other = player(h);
        var wolf = EntityType.WOLF.create(h.getLevel());
        wolf.tame(p);
        wolf.setPos(p.position().add(1, 0, 0));
        other.setPos(p.position().add(0, 0, 1));
        h.getLevel().addFreshEntity(wolf);
        h.getLevel().addNewPlayer(other);
        boolean pvp = h.getLevel().getServer().isPvpAllowed();
        h.getLevel().getServer().setPvpAllowed(false);
        try {
            var targets = ManipulationCombatHelper.hostileTargets(p, h.getLevel(), 5);
            h.assertTrue(!targets.contains(wolf), "Caster's wolf was hostile");
            h.assertTrue(!targets.contains(other), "PvP-disabled player was hostile");
            h.succeed();
        } finally { h.getLevel().getServer().setPvpAllowed(pvp); wolf.discard(); other.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void flareSkipsProtectedTargetsWithoutRevealingThem(GameTestHelper h) {
        ServerPlayer p = player(h), other = player(h);
        var wolf = EntityType.WOLF.create(h.getLevel());
        var hostile = EntityType.HUSK.create(h.getLevel());
        wolf.tame(p);
        other.setPos(p.position().add(0, 0, 2));
        wolf.setPos(p.position().add(0, 0, 3));
        hostile.setPos(p.position().add(0, 0, 5));
        h.getLevel().addNewPlayer(other); h.getLevel().addFreshEntity(wolf); h.getLevel().addFreshEntity(hostile);
        boolean pvp = h.getLevel().getServer().isPvpAllowed();
        h.getLevel().getServer().setPvpAllowed(false);
        try {
            var flare = ManipulationInit.hematic_flare.get(); select(p, flare);
            h.assertTrue(flare.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Valid flare rejected");
            h.assertTrue(hostile.hasEffect(MobEffects.GLOWING) && hostile.getHealth() < 20, "Hostile was not revealed and hit");
            h.assertTrue(!other.hasEffect(MobEffects.GLOWING) && other.getHealth() == 20, "Protected player affected");
            h.assertTrue(!wolf.hasEffect(MobEffects.GLOWING) && wolf.getHealth() == wolf.getMaxHealth(), "Owned wolf affected");
            other.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 80));
            wolf.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 80));
            var bell = ManipulationInit.funeral_bell.get();
            bell.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), bell.getRequiredChargeTicks());
            h.assertTrue(other.getHealth() == 20 && !other.hasEffect(MobEffects.WITHER), "Bell harmed protected player");
            h.assertTrue(wolf.getHealth() == wolf.getMaxHealth() && !wolf.hasEffect(MobEffects.WITHER), "Bell harmed owned wolf");
            h.assertTrue(!ManipulationCombatHelper.hurt(bell, p, other, h.getLevel(), 4), "Shared damage bypassed PvP");
            h.succeed();
        } finally { h.getLevel().getServer().setPvpAllowed(pvp); p.discard(); other.discard(); wolf.discard(); hostile.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void beneficialSelectionSkipsCloserOpponent(GameTestHelper h) {
        ServerPlayer p = player(h), ally = player(h), enemy = player(h);
        var scoreboard = h.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("heal_" + p.getId());
        scoreboard.addPlayerToTeam(p.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(ally.getScoreboardName(), team);
        ally.setPos(p.position().add(0, 0, 3));
        enemy.setPos(p.position().add(0, 0, 1));
        h.getLevel().addNewPlayer(p); h.getLevel().addNewPlayer(ally); h.getLevel().addNewPlayer(enemy);
        ally.setHealth(10); enemy.setHealth(1);
        try {
            ManipulationInit.lumen_suture.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            near(h, 12, ally.getHealth(), "Ally did not receive suture");
            near(h, 1, enemy.getHealth(), "Opponent stole suture");
            ManipulationInit.living_circuit.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
            h.assertTrue(ally.hasEffect(MobEffects.DIG_SPEED), "Circuit missed ally");
            h.assertTrue(!enemy.hasEffect(MobEffects.DIG_SPEED), "Circuit buffed enemy");
            ManipulationReactiveEvents.createHematicBeacon(h.getLevel(), p.position(), 6, 160, p.getUUID());
            near(h, 12.4, ally.getHealth(), "Beacon missed ally");
            near(h, 1, enemy.getHealth(), "Beacon healed opponent");
            h.succeed();
        } finally { scoreboard.removePlayerTeam(team); p.discard(); ally.discard(); enemy.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void invalidForgeAndCommandSpendNothing(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (var m : List.of(ManipulationInit.hematic_flare.get(), ManipulationInit.pyretic_forge.get(), ManipulationInit.hematic_rebuke.get(), ManipulationInit.hematic_impressment.get(), ManipulationInit.exsanguinate.get())) {
                select(p, m);
                h.assertTrue(!m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Invalid cast accepted: " + m.getName());
                near(h, 2000, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Invalid cast spent blood");
                near(h, 185, HemoCapabilityAccess.requireKnownManipulations(p).getKnownManips().get(m).getXp(), "Invalid cast earned mastery");
                h.assertTrue(!BloodManipulation.isAnyManipOnCooldown(p), "Invalid cast started cooldown");
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void exactCostCanPayForAValidCast(GameTestHelper h) {
        for (int delta : new int[]{-1, 0, 1}) {
            ServerPlayer p = player(h);
            try {
                var m = ManipulationInit.blood_shot.get(); select(p, m);
                double cost = ManipulationCostLedger.collect(p, m, 1).effectiveCost();
                HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(cost + delta);
                boolean cast = m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), m.getRequiredChargeTicks());
                h.assertTrue(cast == (delta >= 0), "Incorrect exact-cost eligibility, delta=" + delta);
                near(h, delta >= 0 ? delta : cost - 1, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Incorrect payment");
            } finally { p.discard(); }
        }
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void blackheartedRequiresActiveBloodAndAlignment(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            select(p, ManipulationInit.blackhearted.get());
            HemoCapabilityAccess.requireKnownManipulations(p).togglePassive("blackhearted");
            HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(EnumBloodTendency.MORTEM, 0);
            var damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().wither(), 4));
            BodyIdiomEvents.onIncomingDamage(damage);
            near(h, 4, damage.getAmount(), "Blackhearted ignored alignment");
            HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(EnumBloodTendency.MORTEM, 100);
            HemoCapabilityAccess.requireBloodVolume(p).setActive(false);
            damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().wither(), 4));
            BodyIdiomEvents.onIncomingDamage(damage);
            near(h, 4, damage.getAmount(), "Blackhearted worked with inactive blood");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void phoenixRequiresReadinessAndOnlyPaysOnce(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var phoenix = ManipulationInit.phoenix_debt.get(); select(p, phoenix);
            HemoCapabilityAccess.requireKnownManipulations(p).togglePassive("phoenix_debt");
            HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(EnumBloodTendency.FLAMMEUS, 0);
            ManipulationReactiveEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(!com.vincenthuto.hemomancy.common.event.LastRiteHelper.hasArmedSource(p), "Under-aligned Phoenix armed");
            HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(EnumBloodTendency.FLAMMEUS, 100);
            ManipulationReactiveEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            double cost = ManipulationCostLedger.collect(p, phoenix, 1).effectiveCost();
            HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(cost);
            p.setHealth(5);
            var damage = new LivingDamageEvent.Pre(p, new DamageContainer(p.damageSources().generic(), 30));
            ManipulationReactiveEvents.onFinalDamage(damage);
            near(h, 0, damage.getNewDamage(), "Phoenix failed valid lethal trigger");
            near(h, 0, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Phoenix payment");
            HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(cost);
            damage = new LivingDamageEvent.Pre(p, new DamageContainer(p.damageSources().generic(), 30));
            ManipulationReactiveEvents.onFinalDamage(damage);
            near(h, 30, damage.getNewDamage(), "Phoenix fired twice");
            near(h, cost, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Blocked Phoenix paid again");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void sovereignControlSurvivesAnAiTargetChange(GameTestHelper h) {
        ServerPlayer p = player(h);
        h.getLevel().addNewPlayer(p);
        var mobs = new java.util.ArrayList<net.minecraft.world.entity.monster.Zombie>();
        try {
            select(p, ManipulationInit.sovereign_instinct.get());
            HemoCapabilityAccess.requireKnownManipulations(p).togglePassive("sovereign_instinct");
            for (int i=0; i<4; i++) {
                var mob = EntityType.ZOMBIE.create(h.getLevel());
                mob.setPos(p.position().add(i-2, 0, 2)); mob.setTarget(p);
                h.getLevel().addFreshEntity(mob); mobs.add(mob);
            }
            var damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().mobAttack(mobs.get(0)), 1));
            ManipulationReactiveEvents.onIncomingDamage(damage);
            h.assertTrue(mobs.get(0).getTarget() != p && mobs.get(0).getTarget() != null, "Sovereign did not redirect");
            mobs.get(0).setTarget(p);
            HematicCommandManager.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, h.getLevel()));
            h.assertTrue(mobs.get(0).getTarget() != p && mobs.get(0).getTarget() != null, "AI immediately erased Sovereign control");
            h.succeed();
        } finally { mobs.forEach(net.minecraft.world.entity.Entity::discard); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void hostilePayloadsLeaveAlliedHealthStatusesAndMotionUntouched(GameTestHelper h) {
        for (String name : List.of("blood_aneurysm", "activation_potential", "prismatic_reproof", "sanguine_ignition",
                "vitric_combustion", "scalding_updraft", "soaring_updraft", "suspended_updraft", "expulsive_updraft",
                "cryogenic_pulse", "osseous_bloom", "blood_eclipse", "bloom_of_rot", "hemorrhage")) {
            ServerPlayer p = player(h);
            var ally = EntityType.COW.create(h.getLevel());
            var enemy = EntityType.COW.create(h.getLevel());
            var board = h.getLevel().getScoreboard(); var team = board.addPlayerTeam("area_" + p.getId());
            board.addPlayerToTeam(p.getScoreboardName(), team); board.addPlayerToTeam(ally.getScoreboardName(), team);
            ally.setPos(p.position().add(1, 0, 2)); enemy.setPos(p.position().add(0, 0, 3));
            h.getLevel().addFreshEntity(ally); h.getLevel().addFreshEntity(enemy);
            h.setBlock(2, 3, 7, net.minecraft.world.level.block.Blocks.STONE);
            try {
                var m = ManipulationInit.getByName(name);
                m.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), m.getRequiredChargeTicks());
                near(h, ally.getMaxHealth(), ally.getHealth(), "Allied health: " + name);
                h.assertTrue(ally.getActiveEffects().isEmpty() && !ally.isOnFire(), "Allied status/fire: " + name);
                near(h, 0, ally.getDeltaMovement().length(), "Allied knockback: " + name);
                h.assertTrue(enemy.getHealth() < enemy.getMaxHealth() || !enemy.getActiveEffects().isEmpty() || enemy.isOnFire(), "No hostile payload: " + name);
                if (name.equals("bloom_of_rot")) h.assertTrue(p.hasEffect(MobEffects.POISON), "Bloom lost its intentional self-poison");
            } finally { board.removePlayerTeam(team); ally.discard(); enemy.discard(); p.discard(); h.setBlock(2, 3, 7, net.minecraft.world.level.block.Blocks.AIR); }
        }
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void teleportRejectsUnsafeOrBrightDestinationBeforePayment(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            for (String name : List.of("umbral_step", "umbral_reversal")) {
                var m = ManipulationInit.getByName(name); select(p, m);
                h.assertTrue(!m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Unsafe teleport accepted: " + name);
                near(h, 2000, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Failed teleport payment");
                h.assertTrue(!BloodManipulation.isAnyManipOnCooldown(p), "Failed teleport cooldown");
                near(h, 185, HemoCapabilityAccess.requireKnownManipulations(p).getKnownManips().get(m).getXp(), "Failed teleport mastery");
            }
            var pos = p.blockPosition().offset(0, 0, 4);
            h.getLevel().setBlockAndUpdate(pos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            h.assertTrue(ManipulationCombatHelper.safeLanding(p, pos), "Open supported landing rejected");
            h.getLevel().setBlockAndUpdate(pos.above(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            h.assertTrue(!ManipulationCombatHelper.safeLanding(p, pos), "Head collision accepted");
            h.getLevel().setBlockAndUpdate(pos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            h.getLevel().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
            h.assertTrue(!ManipulationCombatHelper.safeLanding(p, pos), "Lava landing accepted");
            h.getLevel().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            h.getLevel().setBlockAndUpdate(pos.below(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void blackheartedRuptureCooldownAndRecoveryRemainBounded(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            select(p, ManipulationInit.blackhearted.get()); HemoCapabilityAccess.requireKnownManipulations(p).togglePassive("blackhearted");
            var damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().wither(), 100));
            BodyIdiomEvents.onIncomingDamage(damage);
            var state = HemoCapabilityAccess.getPowerGuardrails(p);
            near(h, 0, state.getNecroticSaturation(), "Rupture did not empty saturation");
            h.assertTrue(state.getBlackheartedCooldownUntil() > h.getLevel().getGameTime(), "Rupture did not start refractory period");
            damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().wither(), 4));
            BodyIdiomEvents.onIncomingDamage(damage); near(h, 4, damage.getAmount(), "Conversion continued during refractory period");
            state.setBlackheartedCooldownUntil(h.getLevel().getGameTime());
            damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().wither(), 4));
            BodyIdiomEvents.onIncomingDamage(damage); near(h, 1.4, damage.getAmount(), "Conversion did not recover");
            near(h, 2.6, state.getNecroticSaturation(), "Recovered saturation incorrect");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void umbralStepChecksTheFinalFallbackAndChargesValidLandingOnce(GameTestHelper h) {
        ServerPlayer p = player(h);
        var m = ManipulationInit.umbral_step.get(); select(p, m);
        var hit = h.absolutePos(new net.minecraft.core.BlockPos(2, 3, 6));
        var top = hit.above(); var fallback = hit.north();
        h.getLevel().setBlockAndUpdate(hit, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(top, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(fallback.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        com.vincenthuto.hemomancy.common.manipulation.tenebris.BlackVeilCovenantManager.addVeil(h.getLevel(), top, .6, 40, p.getUUID());
        try {
            h.assertTrue(!com.vincenthuto.hemomancy.common.manipulation.tenebris.BlackVeilCovenantManager.isDarkEnough(h.getLevel(), fallback, 7), "Fixture fallback must be bright");
            h.assertTrue(!m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Bright fallback borrowed darkness from original candidate");
            near(h, 2000, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Invalid fallback spent blood");
            h.getLevel().setBlockAndUpdate(top, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            double cost = ManipulationCostLedger.collect(p, m, 1).effectiveCost();
            h.assertTrue(m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Safe dark step rejected");
            near(h, 2000-cost, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Valid step charged incorrectly");
            h.assertTrue(p.blockPosition().equals(top), "Step used a different landing");
            h.succeed();
        } finally {
            for (var pos : List.of(hit, top, fallback.below())) h.getLevel().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            p.discard();
        }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting", timeoutTicks = 120)
    public static void sovereignControlExpiresAndRestoresTargeting(GameTestHelper h) {
        ServerPlayer p = player(h); p.setNoGravity(true); p.getFoodData().setFoodLevel(10); h.getLevel().addNewPlayer(p);
        var mob = EntityType.ZOMBIE.create(h.getLevel()); var target = EntityType.ZOMBIE.create(h.getLevel());
        mob.setNoAi(true); target.setNoAi(true); mob.setNoGravity(true); target.setNoGravity(true);
        mob.setPos(p.position().add(0,0,2)); target.setPos(p.position().add(0,0,3));
        h.getLevel().addFreshEntity(mob); h.getLevel().addFreshEntity(target);
        h.assertTrue(HematicCommandManager.redirect(p, mob, target, 100), "Redirect rejected");
        h.runAfterDelay(101, () -> {
            try {
                HematicCommandManager.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, h.getLevel()));
                h.assertTrue(mob.getTarget() == null, "Expired redirect retained target");
                mob.setTarget(p);
                HematicCommandManager.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, h.getLevel()));
                h.assertTrue(mob.getTarget() == p, "Expired control still overwrote AI");
                h.succeed();
            } finally { mob.discard(); target.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_targeting")
    public static void soloSupportAndValidForgeHaveExplicitOutcomes(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            select(p, ManipulationInit.living_circuit.get());
            ManipulationChannelManager.start(p);
            h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()), "Solo Circuit started an empty channel");
            near(h, 2000, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Solo Circuit spent blood");
            p.setHealth(10);
            ManipulationInit.lumen_suture.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            near(h, 12, p.getHealth(), "Suture self fallback failed");
            var forge = ManipulationInit.pyretic_forge.get(); select(p, forge);
            var input = new ItemStack(net.minecraft.world.item.Items.RAW_IRON, 8);
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, input);
            double cost = ManipulationCostLedger.collect(p, forge, 1).effectiveCost();
            h.assertTrue(forge.tryPerformAction(p, h.getLevel(), input, p.blockPosition(), 0), "Valid Forge input rejected");
            near(h, 2000-cost, HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(), "Forge payment");
            h.assertTrue(p.getMainHandItem().is(net.minecraft.world.item.Items.IRON_INGOT) && p.getMainHandItem().getCount() == 8, "Forge did not smelt the inputs");
            h.succeed();
        } finally { ManipulationChannelManager.stop(p, false); p.discard(); }
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
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"cast_" + UUID.randomUUID().toString().substring(0, 8)),false);
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
