package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.manipulation.lux.RadiantWakeManip;
import io.netty.channel.embedded.EmbeddedChannel;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Live coverage for the balance pass in docs/MANIPULATION_BALANCE_PLAN.md. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePassGameTests {
    private static final String EMPTY = "bastion/mobs/empty";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void projectileBasesLandInTheStarterBand(GameTestHelper h) {
        whenLoaded(h, () -> projectileBasesLandInTheStarterBandBody(h));
    }

    private static void projectileBasesLandInTheStarterBandBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            ManipulationInit.blood_shot.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            var shots = h.getLevel().getEntitiesOfClass(BloodShotEntity.class, p.getBoundingBox().inflate(6),
                    shot -> shot.getOwner() == p);
            h.assertTrue(!shots.isEmpty(), "Blood Shot did not fire");
            near(h, 1.0, shots.getFirst().getBaseDamage(), "Blood Shot base damage");
            shots.forEach(net.minecraft.world.entity.Entity::discard);
            var lance = ManipulationInit.blood_needle_lance.get();
            lance.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), lance.getRequiredChargeTicks());
            var needles = h.getLevel().getEntitiesOfClass(BloodNeedleEntity.class, p.getBoundingBox().inflate(10),
                    needle -> needle.getOwner() == p);
            h.assertTrue(needles.size() == 3, "Lance did not fire three needles");
            needles.forEach(needle -> near(h, 0.75, needle.getBaseDamage(), "Lance needle base damage"));
            h.assertTrue(needles.stream().allMatch(BloodNeedleEntity::isForged), "Ferric lance needles were not forged");
            needles.forEach(net.minecraft.world.entity.Entity::discard);
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void movedFamiliesUseTheirNewSchools(GameTestHelper h) {
        whenLoaded(h, () -> movedFamiliesUseTheirNewSchoolsBody(h));
    }

    private static void movedFamiliesUseTheirNewSchoolsBody(GameTestHelper h) {
        for (var m : List.of(ManipulationInit.blood_needle.get(), ManipulationInit.blood_needle_fan.get(),
                ManipulationInit.blood_needle_lance.get()))
            h.assertTrue(m.getTend() == EnumBloodTendency.FERRIC && m.getSecondaryTend() == EnumBloodTendency.ANIMUS,
                    m.getName() + " is not Ferric / Animus");
        for (var m : List.of(ManipulationInit.hematic_rebuke.get(), ManipulationInit.hematic_impressment.get()))
            h.assertTrue(m.getTend() == EnumBloodTendency.DUCTILIS && m.getSecondaryTend() == EnumBloodTendency.ANIMUS,
                    m.getName() + " is not Ductilis / Animus");
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass")
    public static void savedCopiesAdoptTheirRehomedSchool(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            // An old save learned Blood Needle as Animus / Ferric with the old price.
            var stale = BloodManipulation.deserialize(ManipulationInit.blood_needle.get().serialize());
            stale.setTend(EnumBloodTendency.ANIMUS);
            stale.setSecondaryTend(EnumBloodTendency.FERRIC);
            stale.setCost(999);
            var known = HemoCapabilityAccess.requireKnownManipulations(p);
            known.getKnownManips().put(stale, new ManipLevel(1, 0));
            com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules.sanitizeKnownManipulations(known);
            h.assertTrue(stale.getTend() == EnumBloodTendency.FERRIC && stale.getSecondaryTend() == EnumBloodTendency.ANIMUS,
                    "Saved copy kept its old school");
            near(h, ManipulationInit.blood_needle.get().getCost(), stale.getCost(), "Saved copy kept its old price");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void umbralRendNeedsAndConsumesObscured(GameTestHelper h) {
        whenLoaded(h, () -> umbralRendNeedsAndConsumesObscuredBody(h));
    }

    private static void umbralRendNeedsAndConsumesObscuredBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var rend = ManipulationInit.umbral_rend.get();
            select(p, rend);
            h.assertTrue(!rend.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                    "Rend accepted a target with no shadow on it");
            near(h, 2000, blood(p), "Rejected Rend spent blood");
            SchoolStates.apply(p, cow, SchoolState.OBSCURED, 100);
            h.assertTrue(rend.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Rend rejected an Obscured target");
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, rend, 5) + 4, 100 - cow.getHealth(), "Rend damage plus Obscured payoff");
            h.assertTrue(!SchoolStates.has(cow, SchoolState.OBSCURED), "Rend did not consume Obscured");
            h.assertTrue(cow.hasEffect(EffectInit.blood_loss), "Rend did not open a wound");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void lodestoneCollapseCashesInLodestone(GameTestHelper h) {
        whenLoaded(h, () -> lodestoneCollapseCashesInLodestoneBody(h));
    }

    private static void lodestoneCollapseCashesInLodestoneBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var collapse = ManipulationInit.lodestone_collapse.get();
            select(p, collapse);
            p.setXRot(90);
            SchoolStates.apply(p, cow, SchoolState.LODESTONE, 100);
            h.assertTrue(collapse.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(),
                    collapse.getRequiredChargeTicks()), "Full Collapse rejected");
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, collapse, 10) + 4, 100 - cow.getHealth(), "Collapse damage plus Lodestone payoff");
            h.assertTrue(!SchoolStates.has(cow, SchoolState.LODESTONE), "Collapse did not consume Lodestone");
            h.assertTrue(cow.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "Full Collapse did not pin");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void searingLashLaysALongSearing(GameTestHelper h) {
        whenLoaded(h, () -> searingLashLaysALongSearingBody(h));
    }

    private static void searingLashLaysALongSearingBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var lash = ManipulationInit.searing_lash.get();
            select(p, lash);
            h.assertTrue(lash.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Lash rejected an aimed target");
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, lash, 4), 100 - cow.getHealth(), "Lash damage");
            h.assertTrue(SchoolStates.has(cow, SchoolState.SEARING), "Lash did not lay Searing");
            h.assertTrue(cow.getEffect(EffectInit.searing).getDuration() >= 100, "Lash Searing is too short to prime Vitric fully");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void radiantWakeMendsIlluminatesAndGlides(GameTestHelper h) {
        whenLoaded(h, () -> radiantWakeMendsIlluminatesAndGlidesBody(h));
    }

    private static void radiantWakeMendsIlluminatesAndGlidesBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            p.setHealth(10);
            ManipulationInit.radiant_wake.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0);
            near(h, 11, p.getHealth(), "Wake did not mend the caster");
            h.assertTrue(SchoolStates.has(cow, SchoolState.ILLUMINATED), "Wake did not Illuminate the enemy");
            p.setOnGround(false);
            p.setDeltaMovement(0, -1, 0);
            ManipulationInit.radiant_wake.get().tickContinuousAction(p, h.getLevel());
            h.assertTrue(p.getDeltaMovement().y >= RadiantWakeManip.MAX_FALL_SPEED - 1.0E-6, "Wake did not slow the fall");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void funeralBellTollsAndSpendsAfflictions(GameTestHelper h) {
        whenLoaded(h, () -> funeralBellTollsAndSpendsAfflictionsBody(h));
    }

    private static void funeralBellTollsAndSpendsAfflictionsBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var bell = ManipulationInit.funeral_bell.get();
            select(p, bell);
            cow.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            h.assertTrue(bell.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 80), "Full Bell rejected");
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, bell, 7 + 2 * 2), 100 - cow.getHealth(), "Bell toll with two afflictions");
            h.assertTrue(!cow.hasEffect(MobEffects.WITHER) && !cow.hasEffect(MobEffects.POISON), "Bell did not spend the afflictions");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void hemorrhageSeedsNecrosis(GameTestHelper h) {
        whenLoaded(h, () -> hemorrhageSeedsNecrosisBody(h));
    }

    private static void hemorrhageSeedsNecrosisBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 40);
        try {
            ManipulationInit.hemorrhage.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            h.assertTrue(cow.hasEffect(EffectInit.blood_loss), "Hemorrhage did not open a wound");
            h.assertTrue(SchoolStates.has(cow, SchoolState.NECROSIS), "Hemorrhage did not seed Necrosis");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void glacialGraspFrostsDryTargets(GameTestHelper h) {
        whenLoaded(h, () -> glacialGraspFrostsDryTargetsBody(h));
    }

    private static void glacialGraspFrostsDryTargetsBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var grasp = ManipulationInit.glacial_grasp.get();
            grasp.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, grasp, 2), 100 - cow.getHealth(), "Dry Grasp damage");
            h.assertTrue(SchoolStates.levels(cow, SchoolState.RIME) == 1, "Dry Grasp did not add one Rime level");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void penumbralDriftBreaksCoverAsAnAmbush(GameTestHelper h) {
        whenLoaded(h, () -> penumbralDriftBreaksCoverAsAnAmbushBody(h));
    }

    private static void penumbralDriftBreaksCoverAsAnAmbushBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            select(p, ManipulationInit.penumbral_drift.get());
            ManipulationChannelManager.start(p);
            h.assertTrue(ManipulationChannelManager.isChanneling(p.getUUID()), "Drift did not start");
            var hit = new LivingDamageEvent.Pre(cow, new DamageContainer(p.damageSources().playerAttack(p), 4));
            ManipulationReactiveEvents.onFinalDamage(hit);
            near(h, 4 * ManipulationReactiveEvents.PENUMBRAL_AMBUSH_MULTIPLIER, hit.getNewDamage(), "Ambush multiplier");
            h.assertTrue(SchoolStates.has(cow, SchoolState.OBSCURED), "Ambush did not leave the victim Obscured");
            h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()), "Attacking did not end the drift");
            h.succeed();
        } finally { ManipulationChannelManager.stop(p, false); cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void cruorJavelinRimesDryTargets(GameTestHelper h) {
        whenLoaded(h, () -> cruorJavelinRimesDryTargetsBody(h));
    }

    private static void cruorJavelinRimesDryTargetsBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var javelin = ManipulationInit.cruor_javelin.get();
            select(p, javelin);
            h.assertTrue(javelin.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Javelin rejected an aimed target");
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, javelin, 4), 100 - cow.getHealth(), "Javelin damage");
            h.assertTrue(SchoolStates.levels(cow, SchoolState.RIME) == 2, "Javelin did not drive two Rime levels");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void fruitingContagionSpreadsFromCarriers(GameTestHelper h) {
        whenLoaded(h, () -> fruitingContagionSpreadsFromCarriersBody(h));
    }

    private static void fruitingContagionSpreadsFromCarriersBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity carrier = cow(h, p, 100);
        LivingEntity neighbour = cow(h, p, 100);
        neighbour.setPos(carrier.position().add(2, 0, 0));
        try {
            var contagion = ManipulationInit.fruiting_contagion.get();
            select(p, contagion);
            h.assertTrue(!contagion.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                    "Contagion fired with no rot nearby");
            near(h, 2000, blood(p), "Rejected Contagion spent blood");
            SchoolStates.apply(p, carrier, SchoolState.NECROSIS, 160);
            h.assertTrue(contagion.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Contagion rejected a carrier");
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, carrier, contagion, 3), 100 - carrier.getHealth(), "Carrier damage");
            h.assertTrue(carrier.hasEffect(MobEffects.WITHER), "Carrier was not withered");
            h.assertTrue(SchoolStates.has(neighbour, SchoolState.NECROSIS), "Rot did not spread to the neighbour");
            h.succeed();
        } finally { carrier.discard(); neighbour.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void forgedLancesPinAndOtherNeedlesDoNot(GameTestHelper h) {
        whenLoaded(h, () -> forgedLancesPinAndOtherNeedlesDoNotBody(h));
    }

    private static void forgedLancesPinAndOtherNeedlesDoNotBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity pinned = cow(h, p, 100);
        LivingEntity grazed = cow(h, p, 100);
        grazed.setPos(pinned.position().add(2, 0, 0));
        var lance = new ProbeNeedle(h.getLevel(), p);
        var plain = new ProbeNeedle(h.getLevel(), p);
        try {
            lance.setForged(true);
            lance.configurePiercing((byte) 3);
            lance.setDeltaMovement(0, 0, 3);
            lance.hit(pinned);
            var slow = pinned.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            h.assertTrue(slow != null && slow.getAmplifier() == com.vincenthuto.hemomancy.common.manipulation.ferric.ForgedNeedleRules.LANCE_PIN_AMPLIFIER,
                    "A forged lance did not pin its target");
            plain.setForged(true);
            plain.setDeltaMovement(0, 0, 3);
            plain.hit(grazed);
            h.assertTrue(!grazed.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "An ordinary forged needle pinned its target");
            var tag = new net.minecraft.nbt.CompoundTag();
            lance.addAdditionalSaveData(tag);
            var loaded = new ProbeNeedle(h.getLevel(), p);
            loaded.readAdditionalSaveData(tag);
            h.assertTrue(loaded.isForged(), "Forged state was lost on reload");
            loaded.discard();
            h.succeed();
        } finally { lance.discard(); plain.discard(); pinned.discard(); grazed.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "manipulation_balance_pass", timeoutTicks = 200)
    public static void hematicRebukeDisruptsTheCommandedBody(GameTestHelper h) {
        whenLoaded(h, () -> hematicRebukeDisruptsTheCommandedBodyBody(h));
    }

    private static void hematicRebukeDisruptsTheCommandedBodyBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        var zombie = EntityType.ZOMBIE.create(h.getLevel());
        zombie.setPos(p.position().add(0, 0, 3));
        zombie.setNoAi(true);
        h.getLevel().addFreshEntity(zombie);
        try {
            h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager.rebuke(p, zombie), "Rebuke was refused");
            h.assertTrue(SchoolStates.has(zombie, SchoolState.DISRUPTED), "Rebuke did not interrupt the body's nerves");
            h.succeed();
        } finally { zombie.discard(); p.discard(); }
    }

    private static final class ProbeNeedle extends BloodNeedleEntity {
        ProbeNeedle(net.minecraft.world.level.Level level, ServerPlayer player) { super(level, player); }
        void hit(net.minecraft.world.entity.Entity target) { super.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target)); }
    }

    /** Fixture entities added before their chunk section loads are invisible to area queries; start once it has. */
    private static void whenLoaded(GameTestHelper h, Runnable body) {
        poll(h, body, 0);
    }

    private static void poll(GameTestHelper h, Runnable body, int attempt) {
        boolean loaded = true;
        for (Vec3 at : List.of(new Vec3(2.5, 2, 2.5), new Vec3(2.5, 2, 5.5), new Vec3(4.5, 2, 5.5)))
            loaded &= h.getLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(
                    net.minecraft.core.BlockPos.containing(h.absoluteVec(at))));
        if (loaded || attempt >= 100) body.run();
        else h.runAfterDelay(1, () -> poll(h, body, attempt + 1));
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
        return HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
    }

    private static void near(GameTestHelper h, double expected, double actual, String message) {
        h.assertTrue(Math.abs(expected - actual) < 0.002, message + ": expected " + expected + ", got " + actual);
    }

    private static void select(ServerPlayer p, BloodManipulation m) {
        var known = HemoCapabilityAccess.requireKnownManipulations(p);
        known.getKnownManips().put(m, new ManipLevel(4, 185));
        known.setEquippedManipNames(List.of(m.getName()));
        known.setSelectedManip(m);
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance"), false);
        ServerPlayer p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, p, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        };
        p.setPos(h.absoluteVec(new Vec3(2.5, 2, 2.5)));
        var blood = HemoCapabilityAccess.requireBloodVolume(p);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        for (var tendency : EnumBloodTendency.values())
            HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(tendency, 100);
        return p;
    }
}
