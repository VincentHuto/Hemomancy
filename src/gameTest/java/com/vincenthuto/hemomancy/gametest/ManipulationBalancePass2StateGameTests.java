package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolCombatEvents;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.BlackVeilCovenantManager;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Balance pass 2, task 1: state lifetimes and reactive-event ordering. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePass2StateGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String BATCH = "manipulation_balance_pass_2_state";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH, timeoutTicks = 400)
    public static void searingStopsBurningWhenItExpires(GameTestHelper h) {
        whenLoaded(h, () -> searingStopsBurningWhenItExpiresBody(h));
    }

    private static void searingStopsBurningWhenItExpiresBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        h.assertTrue(SchoolStates.apply(p, cow, SchoolState.SEARING, 80), "Searing was not applied");
        // Fixture chunks are not always entity-ticking, so drive the state tick (and hit grace) each game tick.
        for (int t = 1; t <= 140; t++) h.runAfterDelay(t, () -> {
            cow.invulnerableTime = 0;
            SchoolCombatEvents.tick(new EntityTickEvent.Pre(cow));
        });
        float[] afterExpiry = new float[1];
        h.runAfterDelay(100, () -> {
            afterExpiry[0] = cow.getHealth();
            h.assertTrue(afterExpiry[0] < 100, "Searing never burned");
            h.assertTrue(!SchoolStates.has(cow, SchoolState.SEARING), "Searing effect outlived its duration");
            h.assertTrue(SchoolStates.data(cow).get(SchoolState.SEARING) == null, "Searing meter outlived its effect");
        });
        h.runAfterDelay(141, () -> {
            try {
                near(h, afterExpiry[0], cow.getHealth(), "Searing kept burning after it expired");
                h.succeed();
            } finally { cow.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH, timeoutTicks = 200)
    public static void penumbralDriftMeleeLandsAsAnAmbush(GameTestHelper h) {
        whenLoaded(h, () -> penumbralDriftMeleeLandsAsAnAmbushBody(h));
    }

    private static void penumbralDriftMeleeLandsAsAnAmbushBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity open = cow(h, p, 100);
        LivingEntity ambushed = cow(h, p, 100);
        ambushed.setPos(open.position().add(2, 0, 0));
        try {
            p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10);
            select(p, ManipulationInit.penumbral_drift.get());
            p.attack(open);
            float baseline = 100 - open.getHealth();
            h.assertTrue(baseline > 0, "Baseline swing did no damage");
            ManipulationChannelManager.start(p);
            h.assertTrue(ManipulationChannelManager.isChanneling(p.getUUID()), "Drift did not start");
            p.attack(ambushed);
            near(h, baseline * ManipulationReactiveEvents.PENUMBRAL_AMBUSH_MULTIPLIER, 100 - ambushed.getHealth(),
                    "Melee swing out of the drift");
            h.assertTrue(SchoolStates.has(ambushed, SchoolState.OBSCURED), "Melee ambush did not leave the victim Obscured");
            h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()), "Attacking did not end the drift");
            h.succeed();
        } finally { ManipulationChannelManager.stop(p, false); open.discard(); ambushed.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH, timeoutTicks = 200)
    public static void gloamLacerationTreatsAVeilAsCover(GameTestHelper h) {
        h.setBlock(new BlockPos(2, 2, 2), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15));
        whenLoaded(h, () -> whenLit(h, () -> gloamLacerationTreatsAVeilAsCoverBody(h), 0));
    }

    private static void whenLit(GameTestHelper h, Runnable body, int attempt) {
        if (attempt >= 40 || !BlackVeilCovenantManager.isDarkEnough(h.getLevel(), BlockPos.containing(h.absoluteVec(new Vec3(2.5, 2, 2.5))), 7))
            body.run();
        else h.runAfterDelay(1, () -> whenLit(h, body, attempt + 1));
    }

    private static void gloamLacerationTreatsAVeilAsCoverBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            h.assertTrue(!BlackVeilCovenantManager.isDarkEnough(h.getLevel(), p.blockPosition(), 7), "Fixture is not lit");
            h.assertTrue(SchoolStates.apply(p, p, SchoolState.VEILED, 100), "Caster was not Veiled");
            var gloam = ManipulationInit.gloam_laceration.get();
            gloam.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, gloam,
                    (float) (6 * SkillPointHelper.getCrimsonMasteryMultiplier(p))), 100 - cow.getHealth(), "Veiled Gloam damage");
            h.assertTrue(cow.hasEffect(EffectInit.blood_loss), "Gloam did not open a wound");
            // Illumination strips the Veil: the same caster in the same light no longer ambushes.
            cow.discard();
            LivingEntity revealed = cow(h, p, 100);
            try {
                h.assertTrue(SchoolStates.apply(p, p, SchoolState.VEILED, 100), "Caster was not re-Veiled");
                h.assertTrue(SchoolStates.apply(p, p, SchoolState.ILLUMINATED, 100), "Caster was not Illuminated");
                gloam.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
                near(h, TendencyAffinityRules.adjustManipulationDamage(p, revealed, gloam,
                        (float) (3.5 * SkillPointHelper.getCrimsonMasteryMultiplier(p))), 100 - revealed.getHealth(),
                        "An Illuminated Veil still ambushed");
            } finally { revealed.discard(); }
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH, timeoutTicks = 200)
    public static void gloamLacerationDoesNotWoundPets(GameTestHelper h) {
        whenLoaded(h, () -> gloamLacerationDoesNotWoundPetsBody(h));
    }

    private static void gloamLacerationDoesNotWoundPetsBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        var wolf = EntityType.WOLF.create(h.getLevel());
        wolf.setTame(true, false);
        wolf.setOwnerUUID(p.getUUID());
        wolf.setNoAi(true);
        wolf.setPos(p.position().add(0, 0, 3));
        h.getLevel().addFreshEntity(wolf);
        try {
            float health = wolf.getHealth();
            ManipulationInit.gloam_laceration.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            h.assertTrue(!wolf.hasEffect(EffectInit.blood_loss), "Gloam wounded the caster's own wolf");
            near(h, health, wolf.getHealth(), "Gloam hurt the caster's own wolf");
            h.succeed();
        } finally { wolf.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH, timeoutTicks = 260)
    public static void sanguineWardRecastResumesALapsedPool(GameTestHelper h) {
        ServerPlayer quick = player(h);
        ServerPlayer patient = player(h);
        for (ServerPlayer p : List.of(quick, patient)) {
            ManipulationReactiveEvents.refreshSanguineWard(p);
            var hit = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().generic(), 4));
            ManipulationReactiveEvents.onIncomingDamage(hit);
            near(h, 0, hit.getAmount(), "Ward did not absorb the hit");
            near(h, 2, ManipulationReactiveEvents.sanguineWardPool(p), "Ward pool after absorbing four");
        }
        // The ward lapses 25 ticks after its last refresh.
        h.runAfterDelay(30, () -> {
            near(h, 0, ManipulationReactiveEvents.sanguineWardPool(quick), "Ward did not lapse");
            ManipulationReactiveEvents.refreshSanguineWard(quick);
            near(h, 3, ManipulationReactiveEvents.sanguineWardPool(quick), "Quick recast refilled the ward");
        });
        h.runAfterDelay(25 + ManipulationReactiveEvents.SANGUINE_WARD_RESUME_TICKS + 20, () -> {
            try {
                ManipulationReactiveEvents.refreshSanguineWard(patient);
                near(h, ManipulationReactiveEvents.SANGUINE_WARD_POOL, ManipulationReactiveEvents.sanguineWardPool(patient),
                        "A long-lapsed ward did not start fresh");
                h.succeed();
            } finally { quick.discard(); patient.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void endlessHourSettlementAnswersToATotem(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            ManipulationInit.endless_hour.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            p.setHealth(5);
            EndlessHourManip.accumulateDeferredDamage(p, 30);
            p.getPersistentData().putLong("hemomancy:endless_hour_expiry", h.getLevel().getGameTime());
            EndlessHourManip.tickEndlessHour(p);
            h.assertTrue(p.isAlive(), "A totem did not answer the hour's lethal debt");
            h.assertTrue(p.getMainHandItem().isEmpty(), "The totem was not consumed");
            h.assertTrue(!p.getPersistentData().contains("hemomancy:endless_hour_deferred"), "Debt survived settlement");
            settlementCannotBeRefusedByBastion(h);
            h.succeed();
        } finally { p.discard(); }
    }

    /** Sheolic Bastion zeroes every ordinary hit, but the hour's debt was already mitigated when deferred. */
    private static void settlementCannotBeRefusedByBastion(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            p.getPersistentData().putLong("hemomancy:sheolic_bastion_until", h.getLevel().getGameTime() + 200);
            h.assertTrue(com.vincenthuto.hemomancy.common.armor.ability.SheolicBastionBloodlustArmorAbilityHandler.isBastionActive(p),
                    "Bastion fixture is not active");
            var ordinary = new LivingDamageEvent.Pre(p, new DamageContainer(p.damageSources().generic(), 5));
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(ordinary);
            near(h, 0, ordinary.getNewDamage(), "Bastion did not refuse an ordinary hit");
            ManipulationInit.endless_hour.get().getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            EndlessHourManip.accumulateDeferredDamage(p, 6);
            p.getPersistentData().putLong("hemomancy:endless_hour_expiry", h.getLevel().getGameTime());
            EndlessHourManip.tickEndlessHour(p);
            near(h, 14, p.getHealth(), "Bastion refused the hour's debt");
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void phoenixDebtNeedsBloodForItsPrice(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var phoenix = ManipulationInit.phoenix_debt.get();
            var known = HemoCapabilityAccess.requireKnownManipulations(p);
            known.getKnownManips().put(phoenix, new ManipLevel(4, 185));
            known.setEquippedManipNames(List.of(phoenix.getName()));
            known.togglePassive(phoenix.getName());
            LastRiteHelper.arm(p, LastRiteHelper.PHOENIX_DEBT_ID);
            var blood = HemoCapabilityAccess.requireBloodVolume(p);
            double price = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger
                    .collect(p, phoenix).effectiveCost();
            h.assertTrue(price > 500, "Fixture price is too low to test an unaffordable debt: " + price);
            blood.setBloodVolume(price);
            h.assertTrue(ManipulationReactiveEvents.phoenixDebtReady(p), "Phoenix Debt was not ready at its full price");
            // A dead section refuses the trigger, so the debt must not show ready through it either.
            var veins = HemoCapabilityAccess.requireVascularSystem(p).getVascularSystem();
            Float sectionHealth = veins.get(phoenix.getSection());
            veins.put(phoenix.getSection(), 0F);
            h.assertTrue(!ManipulationReactiveEvents.phoenixDebtReady(p), "Phoenix Debt showed ready through a dead section");
            veins.put(phoenix.getSection(), sectionHealth == null ? 100F : sectionHealth);
            h.assertTrue(ManipulationReactiveEvents.phoenixDebtReady(p), "Phoenix Debt stayed unready after its section recovered");
            blood.setBloodVolume(500);
            h.assertTrue(!ManipulationReactiveEvents.phoenixDebtReady(p), "Phoenix Debt showed ready without its price");
            var lethal = new LivingDamageEvent.Pre(p, new DamageContainer(p.damageSources().generic(), 50));
            ManipulationReactiveEvents.onFinalDamage(lethal);
            near(h, 50, lethal.getNewDamage(), "An unaffordable Phoenix Debt still cancelled lethal damage");
            near(h, 20, p.getHealth(), "An unaffordable Phoenix Debt healed the caster");
            h.assertTrue(LastRiteHelper.PHOENIX_DEBT_ID.equals(LastRiteHelper.getArmedSource(p))
                    && LastRiteHelper.canFire(p, LastRiteHelper.PHOENIX_DEBT_ID), "An unaffordable Phoenix Debt spent the Last Rite");
            h.succeed();
        } finally { p.discard(); }
    }

    // Bell and Carrion reach past a fixture's bounds, so each runs in a batch of its own.
    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_bell", timeoutTicks = 200)
    public static void funeralBellPartialTollLeavesTheRestOwed(GameTestHelper h) {
        whenLoaded(h, () -> funeralBellPartialTollLeavesTheRestOwedBody(h));
    }

    private static void funeralBellPartialTollLeavesTheRestOwedBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var bell = ManipulationInit.funeral_bell.get();
            h.assertTrue(SchoolStates.apply(p, cow, SchoolState.NECROSIS, 160), "Necrosis was not applied");
            SchoolStates.data(cow).get(SchoolState.NECROSIS).stored = 6;
            bell.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), bell.getRequiredChargeTicks() / 2f);
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, bell, 7 * 0.5f) + 3, 100 - cow.getHealth(),
                    "Half Bell toll plus half the debt");
            var debt = SchoolStates.data(cow).get(SchoolState.NECROSIS);
            h.assertTrue(debt != null && SchoolStates.has(cow, SchoolState.NECROSIS), "Half Bell erased the unpaid debt");
            near(h, 3, debt.stored, "Debt left owed after a half toll");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_hunger", timeoutTicks = 200)
    public static void insatiableHungerCountsAsAnAffliction(GameTestHelper h) {
        whenLoaded(h, () -> insatiableHungerCountsAsAnAfflictionBody(h));
    }

    private static void insatiableHungerCountsAsAnAfflictionBody(GameTestHelper h) {
        ServerPlayer p = player(h);
        LivingEntity cow = cow(h, p, 100);
        try {
            var carrion = ManipulationInit.carrion_communion.get();
            h.assertTrue(!carrion.canContinueChannel(p, h.getLevel()), "Carrion found an unafflicted target");
            cow.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger, 200, 0));
            h.assertTrue(carrion.canContinueChannel(p, h.getLevel()), "Carrion ignored an Insatiable Hunger target");
            var bell = ManipulationInit.funeral_bell.get();
            bell.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), bell.getRequiredChargeTicks());
            near(h, TendencyAffinityRules.adjustManipulationDamage(p, cow, bell, 9), 100 - cow.getHealth(),
                    "Full Bell toll with Insatiable Hunger");
            h.assertTrue(!cow.hasEffect(EffectInit.insatiable_hunger), "Bell did not spend Insatiable Hunger");
            h.succeed();
        } finally { cow.discard(); p.discard(); }
    }

    /** Fixture entities added before their chunk section loads are invisible to area queries; start once it has. */
    private static void whenLoaded(GameTestHelper h, Runnable body) {
        poll(h, body, 0);
    }

    private static void poll(GameTestHelper h, Runnable body, int attempt) {
        boolean loaded = true;
        for (Vec3 at : List.of(new Vec3(2.5, 2, 2.5), new Vec3(2.5, 2, 5.5), new Vec3(4.5, 2, 5.5)))
            loaded &= h.getLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(
                    BlockPos.containing(h.absoluteVec(at))));
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
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance2"), false);
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
