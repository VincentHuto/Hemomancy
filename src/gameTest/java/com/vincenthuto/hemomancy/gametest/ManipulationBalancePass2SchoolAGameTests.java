package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricBarrierManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineMagnetismManip;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Balance pass 2, task 5A: Ferric, Flammeus and Congeatio gap fills and tuning. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePass2SchoolAGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String ROOM = "combat_targeting_room";
    private static final String BATCH = "manipulation_balance_pass_2_school_a";
    /** The room is 32 wide; area spells are cast from its centre. */
    private static final Vec3 CENTRE = new Vec3(16.5, 2, 16.5);

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void schoolARegistryValues(GameTestHelper h) {
        h.assertTrue(ManipulationInit.suspended_updraft.get().getRank() == EnumManipulationRank.MEDIOCRITAS,
                "Suspended Updraft is not Mediocritas");
        h.assertTrue(ManipulationInit.expulsive_updraft.get().getRank() == EnumManipulationRank.MEDIOCRITAS,
                "Expulsive Updraft is not Mediocritas");
        h.assertTrue(ManipulationInit.pyretic_forge.get().getRank() == EnumManipulationRank.HUMILIS,
                "Pyretic Forge is not Humilis");
        near(h, 150, ManipulationInit.pyretic_forge.get().getCost(), "Pyretic Forge cost");
        near(h, 150, ManipulationInit.cauterizing_rebuke.get().getCost(), "Cauterizing Rebuke cost");
        near(h, 1200, ManipulationInit.phoenix_debt.get().getCost(), "Phoenix Debt cost");
        near(h, 350, ManipulationInit.ferric_rampart.get().getCost(), "Ferric Rampart cost");
        near(h, 450, ManipulationInit.ferric_spikes.get().getCost(), "Ferric Spikes cost");
        h.assertTrue(FerricBarrierManip.durationTicks(Kind.WALL) == 200, "Ferric Rampart does not last 10 s");
        h.assertTrue(FerricBarrierManip.durationTicks(Kind.SPIKE) == 120, "Ferric Spikes duration changed");
        h.succeed();
    }

    // ── Ferric ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_magnetism", timeoutTicks = 200)
    public static void magnetismCrushesWhatItPulledWhenThePillarExpires(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            Vec3 at = h.absoluteVec(CENTRE);
            var plain = husk(h, at.add(2, 0, 0));
            var distant = husk(h, at.add(0, 0, 6));
            var cow = EntityType.COW.create(h.getLevel());
            cow.setNoAi(true);
            cow.setPos(at.add(0, 0, -2));
            h.getLevel().addFreshEntity(cow);
            var magnetism = ManipulationInit.sanguine_magnetism.get();
            float mastery = (float) SkillPointHelper.getCrimsonMasteryMultiplier(p);
            try (var cast = SchoolDamage.cast(magnetism, p, 1)) {
                SanguineMagnetismManip.spawnMagneticPillar(p, h.getLevel(), at, 20);
            }
            h.runAfterDelay(10, () -> near(h, plain.getMaxHealth(), plain.getHealth(), "The pillar crushed before it expired"));
            h.runAfterDelay(30, () -> {
                try {
                    near(h, plain.getMaxHealth() - 4 * mastery * affinity(p, plain, magnetism), plain.getHealth(),
                            "Crush on a pulled husk");
                    h.assertTrue(SchoolStates.has(plain, SchoolState.LODESTONE), "The crush was not a direct Ferric hit");
                    near(h, distant.getMaxHealth(), distant.getHealth(), "The crush reached past three blocks");
                    near(h, cow.getMaxHealth(), cow.getHealth(), "The crush hit a creature the pillar never pulled");
                } finally { plain.discard(); distant.discard(); cow.discard(); }
                // A Lodestoned body conducts its crush onward, so it gets a pillar of its own.
                var lodestoned = husk(h, at.add(-2, 0, 0));
                SchoolStates.apply(p, lodestoned, SchoolState.LODESTONE, 400);
                try (var cast = SchoolDamage.cast(magnetism, p, 1)) {
                    SanguineMagnetismManip.spawnMagneticPillar(p, h.getLevel(), at, 20);
                }
                h.runAfterDelay(30, () -> {
                    try {
                        near(h, lodestoned.getMaxHealth() - 6 * mastery * affinity(p, lodestoned, magnetism),
                                lodestoned.getHealth(), "Crush on a Lodestoned husk");
                        h.succeed();
                    } finally { lodestoned.discard(); p.discard(); }
                });
            });
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_choir", timeoutTicks = 200)
    public static void ironChoirPulseFiresANeedleAtTheNearestHostile(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var hostile = husk(h, p.position().add(0, 0, 6));
            var cow = EntityType.COW.create(h.getLevel());
            cow.setNoAi(true);
            cow.setPos(p.position().add(3, 0, 0));
            h.getLevel().addFreshEntity(cow);
            var choir = ManipulationInit.iron_choir.get();
            try {
                choir.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
                List<BloodNeedleEntity> needles = needles(h, p);
                h.assertTrue(needles.size() == 1, "A choir pulse fired " + needles.size() + " needles");
                var needle = needles.getFirst();
                float mastery = (float) SkillPointHelper.getCrimsonMasteryMultiplier(p);
                near(h, 0.4 * mastery, needle.getBaseDamage(), "Choir needle base damage");
                Vec3 toHostile = hostile.getEyePosition().subtract(needle.position()).normalize();
                h.assertTrue(needle.getDeltaMovement().normalize().dot(toHostile) > 0.95,
                        "The needle was not aimed at the hostile husk");
                SchoolHitContext hit = SchoolDamage.projectileContext(needle);
                h.assertTrue(hit != null && hit.ability().getPath().equals("iron_choir")
                        && hit.kind() == SchoolHitContext.Kind.DIRECT, "The choir needle is not a direct Iron Choir hit: " + hit);
                needle.discard();
                hostile.discard();
                choir.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
                h.assertTrue(needles(h, p).isEmpty(), "The choir fired at a passive animal");
                h.succeed();
            } finally {
                choir.finishContinuousAction(p, false);
                needles(h, p).forEach(BloodNeedleEntity::discard);
                hostile.discard(); cow.discard(); p.discard();
            }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_rampart", timeoutTicks = 200)
    public static void ferricRampartLetsFriendlyShotsThrough(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h), ally = player(h);
            var scoreboard = h.getLevel().getScoreboard();
            var team = scoreboard.addPlayerTeam("rampart_" + p.getId());
            scoreboard.addPlayerToTeam(p.getScoreboardName(), team);
            scoreboard.addPlayerToTeam(ally.getScoreboardName(), team);
            var shooter = husk(h, h.absoluteVec(new Vec3(4.5, 2, 4.5)));
            FerricConstructEntity wall = EntityInit.iron_wall.get().create(h.getLevel());
            wall.setPos(h.absoluteVec(CENTRE));
            wall.configure(p, Kind.WALL, Direction.NORTH, FerricBarrierManip.durationTicks(Kind.WALL));
            h.getLevel().addFreshEntity(wall);
            Arrow own = arrow(h, wall, p, -1.0), allied = arrow(h, wall, ally, -0.5),
                    hostile = arrow(h, wall, shooter, 0.5), ownerless = arrow(h, wall, null, 1.0);
            // Let the wall and the arrows tick naturally: a friendly shot must actually cross the plate.
            h.runAfterDelay(5, () -> {
                try {
                    for (Arrow friendly : List.of(own, allied)) {
                        String who = friendly == own ? "caster's" : "ally's";
                        h.assertTrue(!friendly.isRemoved(), "The rampart destroyed an " + who + " shot");
                        h.assertTrue(friendly.getZ() > wall.getZ() + 1,
                                "The " + who + " shot did not cross the rampart: z " + (friendly.getZ() - wall.getZ()));
                        h.assertTrue(friendly.getDeltaMovement().z > 1.0,
                                "The " + who + " shot lost its forward velocity: " + friendly.getDeltaMovement());
                    }
                    h.assertTrue(hostile.isRemoved(), "The rampart let a hostile shot through");
                    h.assertTrue(ownerless.isRemoved(), "The rampart let an ownerless shot through");
                    h.succeed();
                } finally {
                    scoreboard.removePlayerTeam(team);
                    own.discard(); allied.discard(); hostile.discard(); ownerless.discard();
                    wall.discard(); shooter.discard(); p.discard(); ally.discard();
                }
            });
        });
    }

    // ── Congeatio (rampart melt) ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_glacial", timeoutTicks = 300)
    public static void glacialRampartMeltsAfterTwelveSeconds(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            p.setShiftKeyDown(true);
            ManipulationInit.glacial_rampart.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            BlockPos base = p.blockPosition();
            h.assertTrue(ice(h, base) >= 8, "The bastion did not rise");
            h.runAfterDelay(230, () -> h.assertTrue(ice(h, base) >= 8, "The bastion melted before twelve seconds"));
            h.runAfterDelay(246, () -> {
                long left = ice(h, base);
                p.discard();
                h.assertTrue(left == 0, left + " rampart blocks outlasted twelve seconds");
                h.succeed();
            });
        });
    }

    // ── Flammeus ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_expulsive", timeoutTicks = 200)
    public static void expulsiveUpdraftBurstsSearsAndThrowsOutward(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            p.setDeltaMovement(Vec3.ZERO);
            var target = husk(h, p.position().add(3, 0, 0));
            var expulsive = ManipulationInit.expulsive_updraft.get();
            try {
                expulsive.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                near(h, 0.6, p.getDeltaMovement().y, "Expulsive vertical launch");
                float expected = 3 * (float) SkillPointHelper.getCrimsonMasteryMultiplier(p) * affinity(p, target, expulsive);
                near(h, expected, target.getMaxHealth() - target.getHealth(), "Expulsive burst");
                var searing = target.getEffect(EffectInit.searing);
                h.assertTrue(searing != null && searing.getDuration() <= 60 && searing.getDuration() > 40,
                        "Expulsive burst did not sear for 60 ticks: " + searing);
                h.assertTrue(target.getDeltaMovement().x > 0.3, "Expulsive burst did not throw the husk outward");
                h.succeed();
            } finally { target.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_cauterize", timeoutTicks = 200)
    public static void cauterizingRebukePurgesCheaplyThenBurnsNeighbours(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var near = husk(h, p.position().add(2, 0, 0));
            var far = husk(h, p.position().add(0, 0, 7));
            var rebuke = ManipulationInit.cauterizing_rebuke.get();
            try {
                p.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
                p.addEffect(new MobEffectInstance(EffectInit.blood_loss, 200));
                float before = p.getHealth();
                rebuke.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                h.assertTrue(!p.hasEffect(MobEffects.POISON) && !p.hasEffect(EffectInit.blood_loss), "Rebuke did not purge");
                near(h, before - 2, p.getHealth(), "Two purged ailments cost one health each");
                float expected = 3 * (float) SkillPointHelper.getCrimsonMasteryMultiplier(p) * affinity(p, near, rebuke);
                near(h, expected, near.getMaxHealth() - near.getHealth(), "Rebuke burst");
                var searing = near.getEffect(EffectInit.searing);
                h.assertTrue(searing != null && searing.getDuration() <= 60, "Rebuke burst did not sear for 60 ticks: " + searing);
                near(h, far.getMaxHealth(), far.getHealth(), "Rebuke burst reached past four blocks");
                h.succeed();
            } finally { near.discard(); far.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void lumenSutureGrantsAbsorptionOneForTenSeconds(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            p.setHealth(10);
            ManipulationInit.lumen_suture.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            var absorption = p.getEffect(MobEffects.ABSORPTION);
            h.assertTrue(absorption != null && absorption.getAmplifier() == 0 && absorption.getDuration() == 200,
                    "Lumen Suture absorption: " + absorption);
            near(h, 12, p.getHealth(), "Lumen Suture heal");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_ignition", timeoutTicks = 200)
    public static void sanguineIgnitionSetsTargetsAlightForTwoSeconds(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var target = husk(h, p.position().add(3, 0, 0));
            try {
                ManipulationInit.sanguine_ignition.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                h.assertTrue(target.getRemainingFireTicks() > 30 && target.getRemainingFireTicks() <= 40,
                        "Ignition fire ticks: " + target.getRemainingFireTicks());
                h.succeed();
            } finally { target.discard(); p.discard(); }
        });
    }

    // ── Congeatio ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_rimebound", timeoutTicks = 200)
    public static void rimeboundSentenceDealsFourPlusEightCharge(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var sentence = ManipulationInit.rimebound_sentence.get();
            float mastery = (float) SkillPointHelper.getCrimsonMasteryMultiplier(p);
            var half = husk(h, p.position().add(0, 0, 4));
            try {
                p.lookAt(EntityAnchorArgument.Anchor.EYES, half.getEyePosition());
                sentence.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 35);
                near(h, half.getMaxHealth() - 8 * mastery * affinity(p, half, sentence), half.getHealth(), "Half-charge sentence");
            } finally { half.discard(); }
            var full = husk(h, p.position().add(0, 0, -4));
            try {
                p.lookAt(EntityAnchorArgument.Anchor.EYES, full.getEyePosition());
                sentence.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 70);
                near(h, full.getMaxHealth() - 12 * mastery * affinity(p, full, sentence), full.getHealth(), "Full-charge sentence");
                h.succeed();
            } finally { full.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_stillness", timeoutTicks = 200)
    public static void absoluteStillnessFreezesOnlyFullyRimedTargets(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var full = husk(h, p.position().add(3, 0, 0));
            var partial = husk(h, p.position().add(-3, 0, 0));
            SchoolStates.apply(p, full, SchoolState.RIME, 200, 3);
            SchoolStates.apply(p, partial, SchoolState.RIME, 200, 1);
            var stillness = ManipulationInit.absolute_stillness.get();
            try {
                h.assertTrue(SchoolStates.levels(full, SchoolState.RIME) == 3, "Fixture husk is not at three Rime");
                stillness.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
                float expected = 1.5F * (float) SkillPointHelper.getCrimsonMasteryMultiplier(p) * affinity(p, full, stillness);
                near(h, expected, full.getMaxHealth() - full.getHealth(), "Stillness freeze on three Rime");
                near(h, partial.getMaxHealth(), partial.getHealth(), "Stillness froze a husk below three Rime");
                h.succeed();
            } finally { full.discard(); partial.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_javelin", timeoutTicks = 200)
    public static void cruorJavelinDrivesThreeRimeIntoWetTargets(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            BlockPos pool = BlockPos.containing(CENTRE).north(5);
            h.setBlock(pool, Blocks.WATER);
            var wet = husk(h, h.absoluteVec(Vec3.atBottomCenterOf(pool)));
            var dry = husk(h, p.position().add(0, 0, 5));
            h.runAfterDelay(3, () -> {
                var javelin = ManipulationInit.cruor_javelin.get();
                try {
                    h.assertTrue(wet.isInWaterOrRain() && !dry.isInWaterOrRain(), "Fixture wetness is wrong");
                    p.lookAt(EntityAnchorArgument.Anchor.EYES, wet.getEyePosition());
                    javelin.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                    p.lookAt(EntityAnchorArgument.Anchor.EYES, dry.getEyePosition());
                    javelin.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                    h.assertTrue(SchoolStates.levels(wet, SchoolState.RIME) == 3,
                            "Wet husk Rime: " + SchoolStates.levels(wet, SchoolState.RIME));
                    h.assertTrue(SchoolStates.levels(dry, SchoolState.RIME) == 2,
                            "Dry husk Rime: " + SchoolStates.levels(dry, SchoolState.RIME));
                    h.succeed();
                } finally { h.setBlock(pool, Blocks.AIR); wet.discard(); dry.discard(); p.discard(); }
            });
        });
    }

    // ── helpers ──

    private static float affinity(ServerPlayer p, LivingEntity target, BloodManipulation m) {
        return TendencyAffinityRules.damageMultiplier(p, target, m.getTend(), m.getSecondaryTend());
    }

    private static long ice(GameTestHelper h, BlockPos base) {
        return BlockPos.betweenClosedStream(base.offset(-2, 0, -2), base.offset(2, 2, 2))
                .filter(pos -> h.getLevel().getBlockState(pos).is(BlockInit.frozen_cruor.get())).count();
    }

    private static List<BloodNeedleEntity> needles(GameTestHelper h, ServerPlayer p) {
        return h.getLevel().getEntitiesOfClass(BloodNeedleEntity.class, p.getBoundingBox().inflate(4),
                needle -> needle.getOwner() == p && !needle.isRemoved());
    }

    private static Arrow arrow(GameTestHelper h, FerricConstructEntity wall, LivingEntity owner, double lateral) {
        var arrow = new Arrow(h.getLevel(), wall.getX() + lateral, wall.getY() + 2.2, wall.getZ() - 3,
                new ItemStack(Items.ARROW), null);
        if (owner != null) arrow.setOwner(owner);
        arrow.setDeltaMovement(0, 0, 1.5);
        h.getLevel().addFreshEntity(arrow);
        return arrow;
    }

    /** Fixture entities added before their chunk section loads are invisible to area queries; start once it has. */
    private static void whenLoaded(GameTestHelper h, Runnable body) {
        poll(h, body, 0);
    }

    private static void poll(GameTestHelper h, Runnable body, int attempt) {
        boolean loaded = true;
        for (Vec3 at : List.of(new Vec3(2.5, 2, 2.5), new Vec3(29.5, 2, 2.5), new Vec3(2.5, 2, 29.5),
                new Vec3(29.5, 2, 29.5), CENTRE))
            loaded &= h.getLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(
                    BlockPos.containing(h.absoluteVec(at))));
        if (loaded || attempt >= 100) body.run();
        else h.runAfterDelay(1, () -> poll(h, body, attempt + 1));
    }

    private static LivingEntity husk(GameTestHelper h, Vec3 at) {
        var husk = EntityType.HUSK.create(h.getLevel());
        husk.setNoAi(true);
        husk.setNoGravity(true);
        husk.setPos(at);
        h.getLevel().addFreshEntity(husk);
        return husk;
    }

    private static void near(GameTestHelper h, double expected, double actual, String message) {
        h.assertTrue(Math.abs(expected - actual) < 0.002, message + ": expected " + expected + ", got " + actual);
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance2a"), false);
        ServerPlayer p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, p, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        };
        p.setPos(h.absoluteVec(new Vec3(2.5, 2, 2.5)));
        var blood = HemoCapabilityAccess.getBloodVolume(p);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        for (var tendency : EnumBloodTendency.values())
            HemoCapabilityAccess.getBloodTendency(p).setTendencyAlignment(tendency, 100);
        return p;
    }
}
