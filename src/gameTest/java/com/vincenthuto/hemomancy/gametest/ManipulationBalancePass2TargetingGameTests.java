package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastContext;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.BlackVeilCovenantManager;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Balance pass 2, task 2: targeting, payment-before-validation and the shared boss predicate. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePass2TargetingGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String ROOM = "combat_targeting_room";
    private static final String BATCH = "manipulation_balance_pass_2_targeting";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void bossPredicateCoversVanillaBosses(GameTestHelper h) {
        var level = h.getLevel();
        h.assertTrue(ManipulationReactiveEvents.isBoss(EntityType.WARDEN.create(level)), "A Warden is not a boss");
        h.assertTrue(ManipulationReactiveEvents.isBoss(EntityType.ELDER_GUARDIAN.create(level)), "An Elder Guardian is not a boss");
        h.assertTrue(ManipulationReactiveEvents.isBoss(EntityType.WITHER.create(level)), "The Wither stopped being a boss");
        h.assertTrue(!ManipulationReactiveEvents.isBoss(EntityType.ZOMBIE.create(level)), "A zombie counted as a boss");
        h.assertTrue(!ManipulationReactiveEvents.isBoss(EntityType.RAVAGER.create(level)), "A ravager counted as a boss");
        h.succeed();
    }

    // Area spells reach past a fixture's bounds, so every area-sensitive test runs in a batch of its own.
    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_exsanguinate", timeoutTicks = 200)
    public static void exsanguinateRefundsTwoHundredFifty(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            var victim = EntityType.ZOMBIE.create(h.getLevel());
            victim.setNoAi(true);
            victim.setPos(p.position().add(0, 0, 2));
            victim.setHealth(4);
            h.getLevel().addFreshEntity(victim);
            try {
                var exsanguinate = ManipulationInit.exsanguinate.get();
                select(p, exsanguinate);
                double cost = ManipulationCostLedger.collect(p, exsanguinate, 1).effectiveCost();
                h.assertTrue(exsanguinate.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                        "Exsanguinate refused a weakened zombie");
                h.assertTrue(!victim.isAlive(), "Exsanguinate did not execute the zombie");
                near(h, 2000 - cost + 250 + 25, blood(p), "Exsanguinate refund (250) plus the separately attributed 25 mL kill reward");
                h.succeed();
            } finally { victim.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_exsanguinate_boss", timeoutTicks = 200)
    public static void exsanguinateCannotExecuteABoss(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            var warden = EntityType.WARDEN.create(h.getLevel());
            warden.setNoAi(true);
            warden.setPos(p.position().add(0, 0, 3));
            warden.setHealth(100);
            h.getLevel().addFreshEntity(warden);
            try {
                ManipulationInit.exsanguinate.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                float taken = 100 - warden.getHealth();
                float mastery = (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(p);
                float cap = 12 * mastery; // the cap holds after affinity too
                h.assertTrue(taken > 0, "Exsanguinate did not touch the Warden");
                h.assertTrue(taken <= cap + 0.002F, "Exsanguinate drained a boss past the cap: " + taken + " > " + cap);
                h.assertTrue(warden.isAlive(), "Exsanguinate executed a boss");
                h.succeed();
            } finally { warden.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_exsanguinate_affinity", timeoutTicks = 200)
    public static void exsanguinateBossCapHoldsAfterAffinity(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            var exsanguinate = ManipulationInit.exsanguinate.get();
            var vesper = com.vincenthuto.hemomancy.common.init.EntityInit.vesper_evening_star.get().create(h.getLevel());
            // An Animus stance opposes Mortem, so the school pipeline multiplies the drain by the caster's affinity.
            var tag = vesper.saveWithoutId(new net.minecraft.nbt.CompoundTag());
            tag.putInt("ActiveTendency", com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS.ordinal());
            vesper.load(tag);
            vesper.setNoAi(true);
            vesper.setPos(p.position().add(0, 0, 3));
            vesper.setHealth(vesper.getMaxHealth() * 0.2F);
            h.getLevel().addFreshEntity(vesper);
            try {
                float affinity = com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules
                        .manipulationDamageMultiplier(p, vesper, exsanguinate);
                h.assertTrue(affinity > 1.0F, "Fixture does not raise Exsanguinate's affinity: " + affinity);
                float before = vesper.getHealth();
                exsanguinate.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                float taken = before - vesper.getHealth();
                float cap = 12 * (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper
                        .getCrimsonMasteryMultiplier(p);
                h.assertTrue(taken > 0, "Exsanguinate did not touch Vesper");
                h.assertTrue(taken <= cap + 0.002F,
                        "Affinity lifted Exsanguinate past the boss cap: " + taken + " > " + cap);
                h.succeed();
            } finally { vesper.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_bloom", timeoutTicks = 200)
    public static void osseousBloomCapsBossesAndChargesNothingWhenEmpty(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            var bloom = ManipulationInit.osseous_bloom.get();
            select(p, bloom);
            h.assertTrue(!bloom.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Bloom cast on an empty radius");
            near(h, 2000, blood(p), "An empty bloom spent blood");
            h.assertTrue(bloom.getRemainingCooldownTicks(p) == 0L, "An empty bloom started its cooldown");
            var warden = EntityType.WARDEN.create(h.getLevel());
            warden.setNoAi(true);
            warden.setPos(p.position().add(0, 0, 3));
            h.getLevel().addFreshEntity(warden);
            try {
                float before = warden.getHealth();
                h.assertTrue(bloom.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Bloom refused a Warden");
                float taken = before - warden.getHealth();
                h.assertTrue(taken > 0 && taken <= 12.002F, "Warden took " + taken + " from Osseous Bloom (cap 12)");
                h.succeed();
            } finally { warden.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_validation", timeoutTicks = 200, skyAccess = true)
    public static void targetlessCastsSpendNothing(GameTestHelper h) {
        whenLoaded(h, () -> {
            for (var m : List.of(ManipulationInit.hemorrhage.get(), ManipulationInit.grave_debt.get(),
                    ManipulationInit.insatiable_hunger.get(), ManipulationInit.blood_aneurysm.get(),
                    ManipulationInit.vital_effusion.get(), ManipulationInit.rimebound_sentence.get(),
                    ManipulationInit.glacial_rampart.get())) {
                ServerPlayer p = player(h);
                // Looking at open sky (the fixture has no barrier ceiling): no surface, no growth, no aimed vessel.
                p.setXRot(-90);
                try {
                    select(p, m);
                    h.assertTrue(!m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), m.getRequiredChargeTicks()),
                            "Targetless cast accepted: " + m.getName());
                    near(h, 2000, blood(p), "Targetless cast spent blood: " + m.getName());
                    h.assertTrue(m.getRemainingCooldownTicks(p) == 0L, "Targetless cast started cooldown: " + m.getName());
                } finally { p.discard(); }
            }
            // A cow is no fight: the aneurysm still refuses it before payment.
            ServerPlayer p = player(h);
            LivingEntity cow = cow(h, p, 20);
            try {
                var aneurysm = ManipulationInit.blood_aneurysm.get();
                select(p, aneurysm);
                h.assertTrue(!aneurysm.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), aneurysm.getRequiredChargeTicks()),
                        "Aneurysm accepted a passive cow");
                near(h, 2000, blood(p), "Aneurysm on a cow spent blood");
                near(h, 20, cow.getHealth(), "Aneurysm ruptured a passive cow");
                h.succeed();
            } finally { cow.discard(); p.discard(); }
        });
    }

    /** Review focus 2: mob and Will casts never pass through the player's canPerformAction. */
    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_mobcast", timeoutTicks = 200)
    public static void mobCastAneurysmAndHemorrhageStillWork(GameTestHelper h) {
        whenLoaded(h, () -> {
            var caster = EntityType.HUSK.create(h.getLevel());
            caster.setNoAi(true);
            caster.setPos(h.absoluteVec(new Vec3(2.5, 2, 2.5)));
            h.getLevel().addFreshEntity(caster);
            var target = EntityType.COW.create(h.getLevel());
            target.setNoAi(true);
            target.setPos(caster.position().add(0, 0, 3));
            h.getLevel().addFreshEntity(target);
            try {
                h.assertTrue(ManipulationInit.hemorrhage.get().castFromEntity(ManipulationCastContext.forWill(caster, target, ItemStack.EMPTY)),
                        "Mob Hemorrhage failed");
                h.assertTrue(SchoolStates.has(target, SchoolState.NECROSIS), "Mob Hemorrhage left no Necrosis");
                float before = target.getHealth();
                h.assertTrue(ManipulationInit.blood_aneurysm.get().castFromEntity(ManipulationCastContext.forWill(caster, target, ItemStack.EMPTY)),
                        "Mob Aneurysm failed");
                h.assertTrue(target.getHealth() < before, "Mob Aneurysm did no damage");
                h.succeed();
            } finally { caster.discard(); target.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_halo", timeoutTicks = 200)
    public static void sanguineHaloOnlyFiresAtHostiles(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            h.getLevel().addNewPlayer(p);
            var server = h.getLevel().getServer();
            boolean pvp = server.isPvpAllowed();
            List<Entity> spawned = new ArrayList<>();
            try {
                LivingEntity cow = cow(h, p, 20);
                spawned.add(cow);
                var wolf = EntityType.WOLF.create(h.getLevel());
                wolf.setTame(true, false);
                wolf.setOwnerUUID(p.getUUID());
                wolf.setNoAi(true);
                wolf.setPos(p.position().add(2, 0, 0));
                h.getLevel().addFreshEntity(wolf);
                spawned.add(wolf);
                var villager = EntityType.VILLAGER.create(h.getLevel());
                villager.setNoAi(true);
                villager.setPos(p.position().add(-2, 0, 0));
                h.getLevel().addFreshEntity(villager);
                spawned.add(villager);

                BloodShotEntity idle = haloShot(h, p);
                h.assertTrue(idle.isOrbitingFor(p.getUUID()), "Halo fired at a cow, a pet or a villager");

                var zombie = EntityType.HUSK.create(h.getLevel());
                zombie.setNoAi(true);
                zombie.setPos(p.position().add(0, 0, 7));
                h.getLevel().addFreshEntity(zombie);
                spawned.add(zombie);
                BloodShotEntity loosed = haloShot(h, p);
                h.assertTrue(!loosed.isOrbitingFor(p.getUUID()), "Halo ignored a hostile");
                Vec3 toZombie = zombie.getEyePosition().subtract(loosed.position()).normalize();
                h.assertTrue(loosed.getDeltaMovement().normalize().dot(toZombie) > 0.95D, "Halo fired at something other than the hostile");

                // Review focus 4: hostile players stay valid; tamed pets and villagers never are.
                server.setPvpAllowed(true);
                ServerPlayer rival = player(h);
                try {
                    h.assertTrue(ManipulationCombatHelper.isHostileTarget(p, rival), "A harmable player is not a hostile target");
                } finally { rival.discard(); }
                h.assertTrue(!ManipulationCombatHelper.isHostileTarget(p, wolf), "A tamed pet is a hostile target");
                h.assertTrue(!ManipulationCombatHelper.isHostileTarget(p, villager), "A villager is a hostile target");
                h.assertTrue(!ManipulationCombatHelper.isHostileTarget(p, cow), "A cow is a hostile target");
                h.succeed();
            } finally {
                server.setPvpAllowed(pvp);
                spawned.forEach(Entity::discard);
                p.discard();
            }
        });
    }

    private static BloodShotEntity haloShot(GameTestHelper h, ServerPlayer p) {
        BloodShotEntity shot = BloodShotEntity.fromManipulation(h.getLevel(), p, ItemStack.EMPTY);
        shot.configureOrbit(p, 0);
        // Slot zero looses on its first orbit tick; drive it directly so the result does not depend on chunk ticking.
        // The level, not tick(), advances tickCount.
        shot.tickCount = 1;
        shot.tick();
        return shot;
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_enchant", timeoutTicks = 200)
    public static void bloodShotIgnoresWeaponEnchantments(GameTestHelper h) {
        whenLoaded(h, () -> {
            var sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.SHARPNESS), 5);
            float sharp = shotDamage(h, sword);
            float bare = shotDamage(h, ItemStack.EMPTY);
            h.assertTrue(bare > 0, "Empty-hand Blood Shot did no damage");
            h.assertTrue(Math.abs(sharp - bare) <= 1.5F, "Sharpness rode the Blood Shot: " + sharp + " vs " + bare);
            h.succeed();
        });
    }

    private static float shotDamage(GameTestHelper h, ItemStack held) {
        ServerPlayer p = player(h);
        p.setItemInHand(InteractionHand.MAIN_HAND, held.copy());
        p.setYRot(0);
        p.setXRot(20);
        LivingEntity cow = cow(h, p, 100);
        cow.setPos(p.position().add(0, 0, 2.5));
        try {
            ManipulationInit.blood_shot.get().getAction(p, h.getLevel(), held, p.blockPosition(), 0);
            var shots = h.getLevel().getEntitiesOfClass(BloodShotEntity.class, p.getBoundingBox().inflate(4));
            h.assertTrue(!shots.isEmpty(), "No Blood Shot was fired");
            for (int t = 0; t < 6 && cow.getHealth() >= 100; t++) for (var shot : shots) if (!shot.isRemoved()) shot.tick();
            shots.forEach(Entity::discard);
            return 100 - cow.getHealth();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "hemomancy", template = "ductilis_arena", batch = BATCH + "_spikes", timeoutTicks = 60)
    public static void ferricSpikeContactAppliesLodestone(GameTestHelper h) {
        ServerPlayer owner = player(h);
        Vec3 origin = h.absoluteVec(new Vec3(10, 3, 8));
        FerricConstructEntity spike = EntityInit.iron_spike.get().create(h.getLevel());
        spike.setPos(origin);
        spike.configure(owner, FerricConstructShapes.Kind.SPIKE, Direction.NORTH, 120);
        h.getLevel().addFreshEntity(spike);
        var target = EntityType.HUSK.create(h.getLevel());
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(origin);
        h.getLevel().addFreshEntity(target);
        float health = target.getHealth();
        h.runAfterDelay(8, () -> {
            try {
                target.setPos(origin);
                spike.tick();
                h.assertTrue(target.getHealth() < health, "Spike contact did no damage");
                h.assertTrue(SchoolStates.has(target, SchoolState.LODESTONE), "Spike contact did not apply Lodestone");
                h.succeed();
            } finally { spike.discard(); target.discard(); owner.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_lodestone", timeoutTicks = 200)
    public static void lodestoneCollapseNeedsLineOfSight(GameTestHelper h) {
        // The caster looks south at a pillar; one husk hides behind it, another stands in the open.
        BlockPos pillar = new BlockPos(2, 3, 6);
        h.setBlock(pillar, Blocks.STONE);
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setYRot(0);
            p.setXRot(0);
            var hidden = husk(h, h.absoluteVec(new Vec3(2.5, 2, 7.5)));
            var open = husk(h, h.absoluteVec(new Vec3(5.5, 2, 4.5)));
            try {
                var collapse = ManipulationInit.lodestone_collapse.get();
                collapse.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), collapse.getRequiredChargeTicks());
                h.assertTrue(open.getHealth() < open.getMaxHealth(), "Collapse missed the husk in the open");
                near(h, hidden.getMaxHealth(), hidden.getHealth(), "Collapse hurt a husk behind stone");
                near(h, 0, hidden.getDeltaMovement().length(), "Collapse pulled a husk through stone");
                h.succeed();
            } finally { hidden.discard(); open.discard(); p.discard(); h.setBlock(pillar, Blocks.AIR); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_rain", timeoutTicks = 200, skyAccess = true)
    public static void glacialGraspTreatsRainAsWet(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setYRot(0);
            p.setXRot(0);
            var husk = husk(h, h.absoluteVec(new Vec3(2.5, 2, 5.5)));
            var level = h.getLevel();
            float rain = level.getRainLevel(1.0F);
            try {
                h.assertTrue(!GlacialGraspManip.isWet(husk), "Dry fixture husk already counts as wet");
                // Rain only for this synchronous step, so no other fixture ever sees the weather change.
                level.setRainLevel(1.0F);
                BlockPos head = husk.blockPosition().above();
                h.assertTrue(level.isRainingAt(head), "Fixture cannot be rained on: raining=" + level.isRaining()
                        + " sky=" + level.canSeeSky(head) + " precipitation=" + level.getBiome(head).value().getPrecipitationAt(head));
                h.assertTrue(GlacialGraspManip.isWet(husk), "A husk in the rain is not wet");
                ManipulationInit.glacial_grasp.get().getAction(p, level, ItemStack.EMPTY, p.blockPosition());
                near(h, husk.getMaxHealth(), husk.getHealth(), "Rain-soaked husk took dry frost damage");
                h.assertTrue(SchoolStates.levels(husk, SchoolState.RIME) == 2, "Rain-soaked husk was not frozen deeply: "
                        + SchoolStates.levels(husk, SchoolState.RIME));
                h.succeed();
            } finally { level.setRainLevel(rain); husk.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_beacon", timeoutTicks = 200)
    public static void hematicBeaconIlluminatesHostilePlayers(GameTestHelper h) {
        whenLoaded(h, () -> {
            var server = h.getLevel().getServer();
            boolean pvp = server.isPvpAllowed();
            ServerPlayer owner = player(h);
            ServerPlayer rival = player(h);
            rival.setPos(owner.position().add(2, 0, 0));
            h.getLevel().addNewPlayer(owner);
            h.getLevel().addNewPlayer(rival);
            try {
                server.setPvpAllowed(true);
                ManipulationReactiveEvents.createHematicBeacon(h.getLevel(), owner.position(), 6, 40, owner.getUUID());
                h.assertTrue(SchoolStates.has(rival, SchoolState.ILLUMINATED), "Beacon did not illuminate a hostile player");
                h.assertTrue(!SchoolStates.has(owner, SchoolState.ILLUMINATED), "Beacon illuminated its own caster");
                h.succeed();
            } finally { server.setPvpAllowed(pvp); rival.discard(); owner.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void scaldingUpdraftNeedsFooting(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var updraft = ManipulationInit.scalding_updraft.get();
            select(p, updraft);
            p.setOnGround(false);
            h.assertTrue(!updraft.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Airborne updraft accepted");
            near(h, 2000, blood(p), "Airborne updraft spent blood");
            h.assertTrue(updraft.getRemainingCooldownTicks(p) == 0L, "Airborne updraft started cooldown");
            p.setOnGround(true);
            double cost = ManipulationCostLedger.collect(p, updraft, 1).effectiveCost();
            h.assertTrue(updraft.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Grounded updraft refused");
            near(h, 2000 - cost, blood(p), "Grounded updraft payment");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_flames", timeoutTicks = 200)
    public static void adventurersCannotConjureFlames(GameTestHelper h) {
        BlockPos floor = new BlockPos(2, 1, 2);
        BlockPos flame = floor.above();
        h.setBlock(floor, Blocks.STONE);
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            p.setXRot(90);
            try {
                var conjure = ManipulationInit.crimson_flame_conjuration.get();
                select(p, conjure);
                p.setGameMode(GameType.ADVENTURE);
                h.assertTrue(!conjure.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Adventurer conjured flames");
                near(h, 2000, blood(p), "Refused conjuring spent blood");
                h.assertTrue(!h.getBlockState(flame).is(BlockInit.crimson_flames.get()), "Adventurer placed crimson flames");
                p.setGameMode(GameType.SURVIVAL);
                h.assertTrue(conjure.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Survival conjuring refused");
                h.assertTrue(h.getBlockState(flame).is(BlockInit.crimson_flames.get()), "Survival conjuring placed no flames");
                h.succeed();
            } finally { p.discard(); h.setBlock(flame, Blocks.AIR); h.setBlock(floor, Blocks.AIR); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void bloodBindingSyncsABoundPlayer(GameTestHelper h) {
        ServerPlayer bound = player(h);
        try {
            bound.addEffect(new MobEffectInstance(EffectInit.blood_binding, 100, 0));
            bound.setDeltaMovement(0.4, 0, 0.4);
            bound.hurtMarked = false;
            EffectInit.blood_binding.value().applyEffectTick(bound, 0);
            near(h, 0, bound.getDeltaMovement().length(), "Binding left the player moving");
            h.assertTrue(bound.hurtMarked, "Binding did not send the frozen velocity to the bound player");
            h.succeed();
        } finally { bound.discard(); }
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_sovereign", timeoutTicks = 200)
    public static void sovereignInstinctRespectsCommandLimits(GameTestHelper h) {
        whenLoaded(h, () -> {
            ServerPlayer p = player(h);
            h.getLevel().addNewPlayer(p);
            List<Mob> mobs = new ArrayList<>();
            try {
                select(p, ManipulationInit.sovereign_instinct.get());
                HemoCapabilityAccess.getKnownManipulations(p).togglePassive("sovereign_instinct");
                for (int i = 0; i < 3; i++) {
                    var zombie = EntityType.ZOMBIE.create(h.getLevel());
                    zombie.setPos(p.position().add(i - 2, 0, 2));
                    mobs.add(zombie);
                }
                var ravager = EntityType.RAVAGER.create(h.getLevel());
                ravager.setPos(p.position().add(3, 0, 3));
                mobs.add(ravager);
                for (Mob mob : mobs) { h.getLevel().addFreshEntity(mob); mob.setTarget(p); }
                h.assertTrue(ravager.getMaxHealth() > 80, "Ravager fixture is commandable");
                var damage = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().mobAttack(mobs.get(0)), 1));
                ManipulationReactiveEvents.onIncomingDamage(damage);
                h.assertTrue(mobs.get(0).getTarget() != p && mobs.get(0).getTarget() != null, "Sovereign did not redirect a zombie");
                h.assertTrue(ravager.getTarget() == p, "Sovereign redirected a ravager past the command limit");
                h.succeed();
            } finally { mobs.forEach(Entity::discard); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void coronationAndCovenantRefuseRefresh(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var coronation = ManipulationInit.crimson_coronation.get();
            select(p, coronation);
            ManipulationReactiveEvents.armCoronation(p, 3, 1.0F);
            h.assertTrue(!coronation.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), coronation.getRequiredChargeTicks()),
                    "Coronation refreshed a standing crown");
            near(h, 2000, blood(p), "Refused coronation spent blood");
            h.assertTrue(coronation.getRemainingCooldownTicks(p) == 0L, "Refused coronation started cooldown");

            var covenant = ManipulationInit.black_veil_covenant.get();
            select(p, covenant);
            BlackVeilCovenantManager.addVeil(h.getLevel(), p.blockPosition(), 2, 20, p.getUUID());
            h.assertTrue(!covenant.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                    "Covenant stacked a second veil");
            near(h, 2000, blood(p), "Refused covenant spent blood");
            h.assertTrue(covenant.getRemainingCooldownTicks(p) == 0L, "Refused covenant started cooldown");
        } finally { p.discard(); }
        // An uncrowned caster still crowns themselves.
        ServerPlayer fresh = player(h);
        try {
            var coronation = ManipulationInit.crimson_coronation.get();
            select(fresh, coronation);
            h.assertTrue(coronation.tryPerformAction(fresh, h.getLevel(), ItemStack.EMPTY, fresh.blockPosition(), coronation.getRequiredChargeTicks()),
                    "Uncrowned coronation refused");
            h.assertTrue(ManipulationReactiveEvents.coronationActive(fresh), "Coronation did not crown");
            h.succeed();
        } finally { fresh.discard(); }
    }

    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_drudge", timeoutTicks = 200)
    public static void activationPotentialDrudgeShocksHostiles(GameTestHelper h) {
        whenLoaded(h, () -> {
            var drudge = EntityType.COW.create(h.getLevel());
            drudge.setNoAi(true);
            drudge.setPos(h.absoluteVec(new Vec3(4.5, 2, 4.5)));
            h.getLevel().addFreshEntity(drudge);
            var close = husk(h, drudge.position().add(3, 0, 0));
            var far = husk(h, drudge.position().add(0, 0, 9));
            var bystander = EntityType.COW.create(h.getLevel());
            bystander.setNoAi(true);
            bystander.setPos(drudge.position().add(-2, 0, 0));
            h.getLevel().addFreshEntity(bystander);
            try {
                var potential = ManipulationInit.activation_potential.get();
                h.assertTrue("Shocks hostiles near the Drudge".equals(potential.getDrudgeDescription().orElse("")),
                        "Drudge description: " + potential.getDrudgeDescription().orElse(""));
                h.assertTrue(potential.getDrudgeAction().orElseThrow().execute(drudge, h.getLevel(), drudge.blockPosition(), 10),
                        "Drudge shock found no hostile");
                h.assertTrue(SchoolStates.has(close, SchoolState.DISRUPTED), "Nearby hostile was not Disrupted");
                h.assertTrue(close.getHealth() < close.getMaxHealth(), "Nearby hostile was not shocked");
                near(h, far.getMaxHealth(), far.getHealth(), "Shock reached past five blocks");
                h.assertTrue(!SchoolStates.has(far, SchoolState.DISRUPTED), "Distant hostile was Disrupted");
                near(h, bystander.getMaxHealth(), bystander.getHealth(), "Shock hit a passive animal");
                h.succeed();
            } finally { drudge.discard(); close.discard(); far.discard(); bystander.discard(); }
        });
    }

    /** Fixture entities added before their chunk section loads are invisible to area queries; start once it has. */
    private static void whenLoaded(GameTestHelper h, Runnable body) {
        poll(h, body, 0);
    }

    private static void poll(GameTestHelper h, Runnable body, int attempt) {
        boolean loaded = true;
        for (Vec3 at : List.of(new Vec3(2.5, 2, 2.5), new Vec3(2.5, 2, 8.5), new Vec3(8.5, 2, 8.5), new Vec3(13.5, 2, 4.5)))
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

    private static LivingEntity cow(GameTestHelper h, ServerPlayer p, int health) {
        var cow = EntityType.COW.create(h.getLevel());
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        cow.setHealth(health);
        cow.setNoAi(true);
        cow.setPos(p.position().add(0, 0, 3));
        h.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static double blood(ServerPlayer p) {
        return HemoCapabilityAccess.getBloodVolume(p).getBloodVolume();
    }

    private static void near(GameTestHelper h, double expected, double actual, String message) {
        h.assertTrue(Math.abs(expected - actual) < 0.002, message + ": expected " + expected + ", got " + actual);
    }

    private static void select(ServerPlayer p, BloodManipulation m) {
        var known = HemoCapabilityAccess.getKnownManipulations(p);
        known.getKnownManips().put(m, new ManipLevel(4, 185));
        known.setEquippedManipNames(List.of(m.getName()));
        known.setSelectedManip(m);
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance2t"), false);
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
