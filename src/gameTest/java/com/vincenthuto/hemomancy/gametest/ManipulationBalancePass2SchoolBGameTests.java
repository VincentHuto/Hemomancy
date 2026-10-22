package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseWellManip;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Balance pass 2, task 5B: Lux, Mortem, Tenebris and Ductilis gap fills and tuning. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePass2SchoolBGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String ROOM = "combat_targeting_room";
    private static final String BATCH = "manipulation_balance_pass_2_school_b";
    /** The room is 32 wide; area spells are cast from its centre. */
    private static final Vec3 CENTRE = new Vec3(16.5, 2, 16.5);

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void schoolBRegistryValues(GameTestHelper h) {
        h.assertTrue(ManipulationInit.insatiable_hunger.get().getRank() == EnumManipulationRank.HUMILIS,
                "Insatiable Hunger is not Humilis");
        h.assertTrue(ManipulationInit.worked_lignum.get().getRank() == EnumManipulationRank.MEDIOCRITAS,
                "Worked Lignum is not Mediocritas");
        h.assertTrue(ManipulationInit.black_veil_covenant.get().getCooldownTicks() == 900,
                "Black Veil Covenant cooldown: " + ManipulationInit.black_veil_covenant.get().getCooldownTicks());
        near(h, 175, ManipulationInit.hemolymphal_pulse.get().getCost(), "Hemolymphal Pulse cost");
        h.assertTrue(ManipulationInit.hemolymphal_pulse.get().getCooldownTicks() == 200,
                "Hemolymphal Pulse cooldown: " + ManipulationInit.hemolymphal_pulse.get().getCooldownTicks());
        near(h, 100, ManipulationInit.living_circuit.get().getCost(), "Living Circuit cost");
        h.assertTrue(EclipseWellManip.collapses(80) && !EclipseWellManip.collapses(79),
                "Only a fully charged Eclipse Well collapses");
        h.succeed();
    }

    // ── Lux ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_verdict", timeoutTicks = 200)
    public static void whiteVerdictDealsThreePlusNineCharge(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var verdict = ManipulationInit.white_verdict.get();
            float mastery = (float) SkillPointHelper.getCrimsonMasteryMultiplier(p);
            var half = husk(h, p.position().add(0, 0, 4));
            try {
                p.lookAt(EntityAnchorArgument.Anchor.EYES, half.getEyePosition());
                verdict.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 30);
                near(h, half.getMaxHealth() - 7.5 * mastery * affinity(p, half, verdict), half.getHealth(), "Half-charge verdict");
            } finally { half.discard(); }
            var full = husk(h, p.position().add(0, 0, -4));
            try {
                p.lookAt(EntityAnchorArgument.Anchor.EYES, full.getEyePosition());
                verdict.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 60);
                near(h, full.getMaxHealth() - 12 * mastery * affinity(p, full, verdict), full.getHealth(), "Full-charge verdict");
                h.succeed();
            } finally { full.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_wake", timeoutTicks = 200)
    public static void radiantWakeBurnsOnlyIlluminatedEnemies(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var lit = husk(h, p.position().add(3, 0, 0));
            var dark = husk(h, p.position().add(-3, 0, 0));
            var wake = ManipulationInit.radiant_wake.get();
            try {
                SchoolStates.apply(p, lit, SchoolState.ILLUMINATED, 200);
                float bite = (float) SkillPointHelper.getCrimsonMasteryMultiplier(p) * affinity(p, lit, wake);
                wake.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
                near(h, bite, lit.getMaxHealth() - lit.getHealth(), "Wake pulse on an Illuminated husk");
                near(h, dark.getMaxHealth(), dark.getHealth(), "Wake pulse burned a husk the light did not yet hold");
                h.assertTrue(SchoolStates.has(dark, SchoolState.ILLUMINATED), "Wake pulse did not Illuminate the second husk");
                wake.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
                near(h, bite, dark.getMaxHealth() - dark.getHealth(), "Second pulse on the newly Illuminated husk");
                h.succeed();
            } finally { wake.finishContinuousAction(p, false); lit.discard(); dark.discard(); p.discard(); }
        });
    }

    // ── Mortem ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_hunger", timeoutTicks = 200)
    public static void insatiableHungerBitesAndSeedsNecrosis(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var target = husk(h, p.position().add(0, 0, 4));
            var hunger = ManipulationInit.insatiable_hunger.get();
            try {
                hunger.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                float expected = 2 * (float) SkillPointHelper.getCrimsonMasteryMultiplier(p) * affinity(p, target, hunger);
                near(h, expected, target.getMaxHealth() - target.getHealth(), "Hunger bite");
                h.assertTrue(SchoolStates.has(target, SchoolState.NECROSIS), "Hunger bite did not seed Necrosis");
                h.assertTrue(target.hasEffect(EffectInit.insatiable_hunger), "Hunger curse missing");
                h.succeed();
            } finally { target.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_bloom", timeoutTicks = 200)
    public static void bloomOfRotStrikesEveryBodyItReaches(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var first = husk(h, p.position().add(3, 0, 0));
            var second = husk(h, p.position().add(0, 0, -5));
            var bloom = ManipulationInit.bloom_of_rot.get();
            try {
                bloom.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                float mastery = (float) SkillPointHelper.getCrimsonMasteryMultiplier(p);
                for (LivingEntity husk : List.of(first, second)) {
                    near(h, 3 * mastery * affinity(p, husk, bloom), husk.getMaxHealth() - husk.getHealth(), "Bloom damage");
                    h.assertTrue(SchoolStates.has(husk, SchoolState.NECROSIS), "Bloom did not infect a husk");
                }
                h.succeed();
            } finally { first.discard(); second.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_bloom_mastery", timeoutTicks = 200)
    public static void bloomOfRotAppliesCrimsonMasteryExactlyOnce(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var target = husk(h, p.position().add(3, 0, 0));
            var bloom = ManipulationInit.bloom_of_rot.get();
            try {
                HemoCapabilityAccess.getSkillProgress(p).setSkill(SkillPointInit.skill_crimson_mastery,
                        EnumSkillStates.UNLOCKED, 3);
                near(h, 1.45, SkillPointHelper.getCrimsonMasteryMultiplier(p), "Crimson Mastery 3 multiplier");
                bloom.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                // A literal 1.45: a second application of Crimson Mastery would show as 1.45 squared.
                near(h, 3 * 1.45 * affinity(p, target, bloom), target.getMaxHealth() - target.getHealth(),
                        "Bloom damage at Crimson Mastery 3");
                h.succeed();
            } finally { target.discard(); p.discard(); }
        });
    }

    // ── Tenebris ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_well", timeoutTicks = 200)
    public static void eclipseWellBitesObscuredBodiesEachSecond(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            h.getLevel().addNewPlayer(p);
            // Looking straight down sinks the well into the floor at the caster's feet.
            p.setXRot(90);
            p.setYRot(0);
            var inside = husk(h, p.position().add(3, 0, 0));
            Runnable cleanup = () -> { inside.discard(); p.discard(); };
            guarded(cleanup, () -> {
                var well = ManipulationInit.eclipse_well.get();
                float bite = EclipseWellManip.PULSE_DAMAGE * (float) SkillPointHelper.getCrimsonMasteryMultiplier(p)
                        * affinity(p, inside, well);
                well.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 80);
                h.runAfterDelay(15, () -> guarded(cleanup, () -> {
                    h.assertTrue(SchoolStates.has(inside, SchoolState.OBSCURED), "The well did not Obscure the husk");
                    near(h, inside.getMaxHealth(), inside.getHealth(), "The well bit before its first pulse");
                }));
                h.runAfterDelay(30, () -> guarded(cleanup,
                        () -> near(h, bite, inside.getMaxHealth() - inside.getHealth(), "One well pulse")));
                h.runAfterDelay(50, () -> {
                    try {
                        near(h, 2 * bite, inside.getMaxHealth() - inside.getHealth(), "Two well pulses");
                        h.succeed();
                    } finally { cleanup.run(); }
                });
            });
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_collapse", timeoutTicks = 200)
    public static void fullEclipseWellScattersNonBossEnemiesAtItsHeart(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(new Vec3(4.5, 2, 4.5)));
            h.getLevel().addNewPlayer(p);
            Vec3 centre = h.absoluteVec(CENTRE);
            List<Entity> bodies = new java.util.ArrayList<>();
            Runnable cleanup = () -> { bodies.forEach(Entity::discard); p.discard(); };
            var heart = husk(h, centre.add(0.5, 0, 0));
            bodies.add(heart);
            var rim = husk(h, centre.add(-3.2, 0, 0));
            bodies.add(rim);
            var cow = EntityType.COW.create(h.getLevel());
            bodies.add(cow);
            var guardian = EntityType.ELDER_GUARDIAN.create(h.getLevel());
            bodies.add(guardian);
            Vec3[] before = new Vec3[bodies.size()];
            guarded(cleanup, () -> {
                cow.setNoAi(true);
                cow.setPos(centre.add(0, 0, -0.8));
                h.getLevel().addFreshEntity(cow);
                guardian.setNoAi(true);
                guardian.setNoGravity(true);
                guardian.setPos(centre.add(0, 0, 1.5));
                h.getLevel().addFreshEntity(guardian);
                ManipulationReactiveEvents.createEclipseWell(h.getLevel(), centre, 3, 20, p.getUUID(), 0, true);
            });
            h.runAfterDelay(18, () -> guarded(cleanup, () -> {
                for (int i = 0; i < bodies.size(); i++) before[i] = bodies.get(i).position();
            }));
            h.runAfterDelay(25, () -> {
                try {
                    h.assertTrue(heart.position().distanceTo(before[0]) > 0.5,
                            "The collapse did not scatter the husk at its heart");
                    h.assertTrue(rim.position().distanceTo(before[1]) < 0.5, "The collapse scattered a husk beyond two blocks");
                    h.assertTrue(cow.position().distanceTo(before[2]) < 0.5, "The collapse scattered a passive animal");
                    h.assertTrue(guardian.position().distanceTo(before[3]) < 0.5, "The collapse scattered a boss");
                    h.succeed();
                } finally { cleanup.run(); }
            });
        });
    }

    // ── Ductilis ──

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_pulse", timeoutTicks = 200)
    public static void hemolymphalPulseRevealsOnlyHarmableBodies(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            h.getLevel().addNewPlayer(p);
            var hostile = husk(h, p.position().add(3, 0, 0));
            Wolf pet = EntityType.WOLF.create(h.getLevel());
            pet.setNoAi(true);
            pet.tame(p);
            pet.setPos(p.position().add(-3, 0, 0));
            h.getLevel().addFreshEntity(pet);
            try {
                ManipulationInit.hemolymphal_pulse.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                h.assertTrue(hostile.hasEffect(MobEffects.GLOWING), "The pulse did not reveal a hostile");
                h.assertTrue(!pet.hasEffect(MobEffects.GLOWING), "The pulse revealed the caster's own pet");
                h.assertTrue(!p.hasEffect(MobEffects.GLOWING), "The pulse revealed the caster");
                h.succeed();
            } finally { hostile.discard(); pet.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_circuit", timeoutTicks = 200)
    public static void livingCircuitArcsToTheNearestHostileWithoutState(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            var nearest = husk(h, p.position().add(2.5, 0, 0));
            var second = husk(h, p.position().add(0, 0, 3.5));
            var distant = husk(h, p.position().add(-6, 0, 0));
            var cow = EntityType.COW.create(h.getLevel());
            cow.setNoAi(true);
            cow.setPos(p.position().add(0, 0, -1.5));
            h.getLevel().addFreshEntity(cow);
            var circuit = ManipulationInit.living_circuit.get();
            try {
                circuit.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
                float expected = 2 * (float) SkillPointHelper.getCrimsonMasteryMultiplier(p) * affinity(p, nearest, circuit);
                near(h, expected, nearest.getMaxHealth() - nearest.getHealth(), "Circuit arc on the nearest hostile");
                h.assertTrue(!SchoolStates.has(nearest, SchoolState.DISRUPTED) && !nearest.hasEffect(EffectInit.conductive_mark),
                        "The circuit arc applied a school state");
                near(h, second.getMaxHealth(), second.getHealth(), "A solo circuit arced twice");
                near(h, distant.getMaxHealth(), distant.getHealth(), "The circuit arced past four blocks");
                near(h, cow.getMaxHealth(), cow.getHealth(), "The circuit arced into a passive animal");
                h.succeed();
            } finally {
                circuit.finishContinuousAction(p, false);
                nearest.discard(); second.discard(); distant.discard(); cow.discard(); p.discard();
            }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_circuit_ally", timeoutTicks = 200)
    public static void livingCircuitArcsAroundEachLinkedAlly(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h), ally = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            ally.setPos(p.position().add(6, 0, 0));
            var board = h.getLevel().getScoreboard();
            var team = board.addPlayerTeam("circuit_" + p.getId());
            board.addPlayerToTeam(p.getScoreboardName(), team);
            board.addPlayerToTeam(ally.getScoreboardName(), team);
            h.getLevel().addNewPlayer(p);
            h.getLevel().addNewPlayer(ally);
            var byAlly = husk(h, ally.position().add(3, 0, 0));
            var circuit = ManipulationInit.living_circuit.get();
            try {
                circuit.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
                float expected = 2 * (float) SkillPointHelper.getCrimsonMasteryMultiplier(p) * affinity(p, byAlly, circuit);
                near(h, expected, byAlly.getMaxHealth() - byAlly.getHealth(), "Circuit arc beside the linked ally");
                h.succeed();
            } finally {
                circuit.finishContinuousAction(p, false);
                board.removePlayerTeam(team);
                byAlly.discard(); p.discard(); ally.discard();
            }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_ripper", timeoutTicks = 200)
    public static void threadRipperUnravelsForeignSummonsButSparesBondedAnimals(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h), rival = player(h);
            p.setPos(h.absoluteVec(CENTRE));
            rival.setPos(h.absoluteVec(new Vec3(4.5, 2, 4.5)));
            h.getLevel().addNewPlayer(p);
            h.getLevel().addNewPlayer(rival);
            var server = h.getLevel().getServer();
            boolean pvp = server.isPvpAllowed();
            List<Mob> bodies = new java.util.ArrayList<>();
            try {
                var vex = EntityType.VEX.create(h.getLevel());
                h.assertTrue(rip(h, p, vex, bodies, true), "Thread Ripper did not unravel a healthy Vex");
                // Hemomancy summons owned by a harmable rival unravel.
                var thrall = EntityInit.blood_thrall.get().create(h.getLevel());
                thrall.setOwnerUUID(rival.getUUID());
                h.assertTrue(rip(h, p, thrall, bodies, true), "Thread Ripper spared a rival's Blood Thrall");
                var drudge = EntityInit.drudge.get().create(h.getLevel());
                drudge.setOwnerUUID(UUID.randomUUID());
                h.assertTrue(rip(h, p, drudge, bodies, true), "Thread Ripper spared an absent owner's Drudge while PvP is on");
                // Unravelling a rival's Drudge gives back the Blood Memory it carries instead of destroying it.
                var armed = EntityInit.drudge.get().create(h.getLevel());
                armed.setOwnerUUID(rival.getUUID());
                armed.setEquippedMemory(ManipulationInit.blood_shot.get());
                h.assertTrue(rip(h, p, armed, bodies, true), "Thread Ripper spared a rival's armed Drudge");
                var dropped = h.getLevel().getEntitiesOfClass(ItemEntity.class, armed.getBoundingBox().inflate(4),
                        item -> item.getItem().getItem() instanceof BloodMemoryItem memory && memory.getManip() != null
                                && memory.getManip().getName().equals(ManipulationInit.blood_shot.get().getName()));
                h.assertTrue(!dropped.isEmpty(), "Unravelled Drudge destroyed its equipped Blood Memory");
                dropped.forEach(Entity::discard);
                var guarded = EntityInit.blood_thrall.get().create(h.getLevel());
                guarded.setOwnerUUID(rival.getUUID());
                h.assertTrue(!rip(h, p, guarded, bodies, false), "Thread Ripper unravelled a rival's Blood Thrall while PvP is off");
                var own = EntityInit.blood_thrall.get().create(h.getLevel());
                own.setOwnerUUID(p.getUUID());
                h.assertTrue(!rip(h, p, own, bodies, true), "Thread Ripper unravelled the caster's own Blood Thrall");
                // Tamed animals and horses are bonded, not summoned: never unravelled, whoever owns them.
                h.assertTrue(!rip(h, p, wolf(h, rival.getUUID()), bodies, true), "Thread Ripper unravelled a rival's wolf");
                h.assertTrue(!rip(h, p, wolf(h, UUID.randomUUID()), bodies, true), "Thread Ripper unravelled an absent owner's wolf");
                h.assertTrue(!rip(h, p, wolf(h, p.getUUID()), bodies, true), "Thread Ripper unravelled the caster's own wolf");
                h.assertTrue(!rip(h, p, EntityType.WOLF.create(h.getLevel()), bodies, true), "Thread Ripper unravelled a wild wolf");
                var horse = EntityType.HORSE.create(h.getLevel());
                horse.setTamed(true);
                horse.setOwnerUUID(rival.getUUID());
                h.assertTrue(!rip(h, p, horse, bodies, true), "Thread Ripper unravelled a rival's horse");
                h.succeed();
            } finally {
                server.setPvpAllowed(pvp);
                bodies.forEach(Entity::discard);
                p.discard(); rival.discard();
            }
        });
    }

    // ── helpers ──

    /** Runs a step of a multi-tick test; if it fails, the fixture is torn down before the failure propagates. */
    private static void guarded(Runnable cleanup, Runnable step) {
        try {
            step.run();
        } catch (Throwable failure) {
            cleanup.run();
            throw failure;
        }
    }

    /** Places {@code body} in front of the caster, casts a full Thread Ripper at it, and reports whether it was unravelled. */
    private static boolean rip(GameTestHelper h, ServerPlayer p, Mob body, List<Mob> bodies, boolean pvp) {
        body.setNoAi(true);
        body.setNoGravity(true);
        body.setPos(p.position().add(0, 1, 4));
        h.getLevel().addFreshEntity(body);
        bodies.add(body);
        p.lookAt(EntityAnchorArgument.Anchor.EYES, body.getBoundingBox().getCenter());
        var server = h.getLevel().getServer();
        boolean previous = server.isPvpAllowed();
        server.setPvpAllowed(pvp);
        try {
            ManipulationInit.thread_ripper.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 30);
        } finally { server.setPvpAllowed(previous); }
        boolean removed = body.isRemoved();
        body.discard();
        return removed;
    }

    private static Wolf wolf(GameTestHelper h, UUID owner) {
        Wolf wolf = EntityType.WOLF.create(h.getLevel());
        wolf.setTame(true, false);
        wolf.setOwnerUUID(owner);
        return wolf;
    }

    private static float affinity(ServerPlayer p, LivingEntity target, BloodManipulation m) {
        return TendencyAffinityRules.damageMultiplier(p, target, m.getTend(), m.getSecondaryTend());
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
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance2b"), false);
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
