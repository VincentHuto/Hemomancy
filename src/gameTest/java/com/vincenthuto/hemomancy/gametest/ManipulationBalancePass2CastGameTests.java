package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.HemoMilestone;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.projectile.CloudEntityBlood;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.network.CastingChargePacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.List;
import java.util.UUID;

/** Balance pass 2, task 3a: the charged-cast floor, charge-scaled rewards, the server charge clamp and Crimson Mastery. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePass2CastGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String ARENA = "ductilis_arena";
    private static final String BATCH = "manipulation_balance_pass_2_cast";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_floor")
    public static void chargedCastsBelowAQuarterAreRefusedBeforePayment(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var storm = ManipulationInit.synaptic_storm.get();
            select(p, storm);
            double cost = ManipulationCostLedger.collect(p, storm, 1).effectiveCost();
            h.assertTrue(!storm.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 6),
                    "A 0.1-charge Storm was accepted");
            near(h, 2000, blood(p), "A refused 0.1-charge Storm spent blood");
            h.assertTrue(storm.getRemainingCooldownTicks(p) == 0L, "A refused 0.1-charge Storm started its cooldown");
            h.assertTrue(storm.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 15),
                    "A quarter-charge Storm was refused");
            near(h, 2000 - cost * 0.25, blood(p), "A quarter-charge Storm pays a quarter of its cost");
            h.succeed();
        } finally { p.discard(); }
    }

    // Review focus 3: QUICK casts never meet the floor, and spells that already demand full charge still cast at full charge.
    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_unaffected")
    public static void quickAndFullChargeCastsIgnoreTheFloor(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var shot = ManipulationInit.blood_shot.get();
            select(p, shot);
            h.assertTrue(shot.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                    "A QUICK Blood Shot with chargeTicks 0 was refused");
            h.assertTrue(blood(p) < 2000, "The QUICK Blood Shot cast for free");
            for (BloodManipulation m : List.of(ManipulationInit.blood_needle_lance.get(), ManipulationInit.hematic_mortar.get())) {
                select(p, m);
                double before = blood(p);
                h.assertTrue(m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), m.getRequiredChargeTicks()),
                        "A full-charge " + m.getName() + " was refused");
                h.assertTrue(blood(p) < before, "A full-charge " + m.getName() + " cast for free");
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_rewards")
    public static void partialChargeScalesAlignmentSkillPointsAndMastery(GameTestHelper h) {
        ServerPlayer partial = player(h);
        ServerPlayer full = player(h);
        try {
            var storm = ManipulationInit.synaptic_storm.get();
            float amount = HemoServerConfig.TENDENCY_SHIFT_ON_CAST_ENABLED.get()
                    ? HemoServerConfig.TENDENCY_SHIFT_ON_MANIP_USE.get().floatValue() : 0;
            Reward low = cast(h, partial, storm, 18);
            Reward high = cast(h, full, storm, 60);
            near(h, amount * 0.3, low.alignment, "A 0.3-charge Storm's Ductilis alignment gain");
            near(h, amount, high.alignment, "A full Storm's Ductilis alignment gain");
            near(h, 0, low.masteryUses, "A 0.3-charge Storm earned a mastery use");
            near(h, 1, high.masteryUses, "A full Storm's mastery use");
            // PERFECTUS pays 5 points; 0.3 of that rounds to 2. A cast below half charge is not a manipulation use,
            // so only the full caster advances the use milestones (First Manipulation pays its own points).
            h.assertTrue(low.uses == 0, "A 0.3-charge Storm counted toward the manipulation-use milestones");
            h.assertTrue(high.uses == 1, "A full Storm did not count toward the manipulation-use milestones");
            h.assertTrue(high.skillPoints - low.skillPoints == 3 + HemoMilestone.FIRST_MANIPULATION.getSkillPointReward(),
                    "Skill points did not scale with charge: partial " + low.skillPoints + ", full " + high.skillPoints);
            h.succeed();
        } finally { partial.discard(); full.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_clamp", timeoutTicks = 100)
    public static void serverClampsReportedChargeToTheObservedHold(GameTestHelper h) {
        ServerPlayer p = player(h);
        boolean handedOff = false;
        try {
            var storm = ManipulationInit.synaptic_storm.get();
            select(p, storm);
            double cost = ManipulationCostLedger.collect(p, storm, 1).effectiveCost();
            var context = new ServerPayloadContext(p.connection, UseManipKeyPacket.TYPE.id());
            UseManipKeyPacket.handle(new UseManipKeyPacket(60), context);
            near(h, 2000, blood(p), "A full-charge claim with no observed key-down spent blood");
            CastingChargePacket.handle(new CastingChargePacket(storm.getName(), 1), context);
            long start = h.getLevel().getGameTime();
            UseManipKeyPacket.handle(new UseManipKeyPacket(60), context);
            near(h, 2000, blood(p), "A full-charge claim in the key-down's own tick was credited");
            h.runAfterDelay(20, () -> {
                try {
                    long elapsed = h.getLevel().getGameTime() - start;
                    long allowed = ManipulationCastingRules.allowedChargeTicks(elapsed);
                    h.assertTrue(allowed < 60, "Fixture no longer over-claims beyond the tolerance: allowed " + allowed);
                    UseManipKeyPacket.handle(new UseManipKeyPacket(60), context);
                    near(h, 2000 - cost * allowed / 60.0, blood(p),
                            "A 60-tick claim after " + elapsed + " observed ticks is clamped to " + allowed);
                    h.assertTrue(storm.getRemainingCooldownTicks(p) > 0, "The clamped Storm did not cast");
                    h.succeed();
                } finally { p.discard(); }
            });
            handedOff = true;
        } finally {
            if (!handedOff) p.discard();
        }
    }

    @GameTest(templateNamespace = "hemomancy", template = ARENA, batch = BATCH + "_aneurysm")
    public static void aneurysmPressureBonusScalesWithCharge(GameTestHelper h) {
        ServerPlayer p = arenaPlayer(h);
        try {
            var aneurysm = ManipulationInit.blood_aneurysm.get();
            int half = aneurysm.getRequiredChargeTicks() / 2;
            float charge = (float) half / aneurysm.getRequiredChargeTicks();
            LivingEntity pressured = husk(h, new Vec3(4.5, 2, 2.5));
            for (int i = 0; i < 3; i++) SchoolStates.apply(p, pressured, SchoolState.PRESSURE, 200);
            h.assertTrue(SchoolStates.levels(pressured, SchoolState.PRESSURE) == 3, "Pressure fixture did not reach 3 stacks");
            aneurysm.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), half);
            float withPressure = pressured.getMaxHealth() - pressured.getHealth();
            pressured.discard();
            LivingEntity plain = husk(h, new Vec3(4.5, 2, 2.5));
            aneurysm.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), half);
            float withoutPressure = plain.getMaxHealth() - plain.getHealth();
            plain.discard();
            near(h, 2 * 3 * charge, withPressure - withoutPressure, "Half-charge Aneurysm Pressure bonus");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "hemomancy", template = ARENA, batch = BATCH + "_mastery")
    public static void crimsonMasteryScalesSynapticStorm(GameTestHelper h) {
        ServerPlayer p = arenaPlayer(h);
        try {
            var storm = ManipulationInit.synaptic_storm.get();
            LivingEntity untrained = husk(h, new Vec3(4.5, 2, 2.5));
            storm.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 60);
            float base = untrained.getMaxHealth() - untrained.getHealth();
            untrained.discard();
            h.assertTrue(base > 0, "The untrained Storm missed its victim");
            HemoCapabilityAccess.getSkillProgress(p).setSkill(SkillPointInit.skill_crimson_mastery, EnumSkillStates.UNLOCKED, 3);
            near(h, 1.45, SkillPointHelper.getCrimsonMasteryMultiplier(p), "Crimson Mastery 3 multiplier");
            LivingEntity trained = husk(h, new Vec3(4.5, 2, 2.5));
            storm.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 60);
            float mastered = trained.getMaxHealth() - trained.getHealth();
            trained.discard();
            near(h, base * 1.45, mastered, "Crimson Mastery 3 Storm damage (untrained " + base + ")");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "hemomancy", template = ARENA, batch = BATCH + "_cloud", timeoutTicks = 40)
    public static void bloodCloudKeepsItsCastMastery(GameTestHelper h) {
        var cloud = new CloudEntityBlood(EntityInit.blood_cloud.get(), h.getLevel());
        cloud.setDamageMultiplier(1.45F);
        CompoundTag saved = cloud.saveWithoutId(new CompoundTag());
        var reloaded = new CloudEntityBlood(EntityInit.blood_cloud.get(), h.getLevel());
        reloaded.load(saved);
        near(h, 1.45, reloaded.getDamageMultiplier(), "Reloaded cloud's Crimson Mastery");
        LivingEntity victim = husk(h, new Vec3(6.5, 2, 6.5));
        reloaded.setPos(victim.position().add(0, 2, 0));
        h.getLevel().addFreshEntity(reloaded);
        h.runAfterDelay(3, () -> {
            float taken = victim.getMaxHealth() - victim.getHealth();
            reloaded.discard();
            victim.discard();
            near(h, 2 * 1.45, taken, "Cloud tick damage at Crimson Mastery 1.45");
            h.succeed();
        });
    }

    private record Reward(double alignment, double masteryUses, int skillPoints, int uses) { }

    private static Reward cast(GameTestHelper h, ServerPlayer p, BloodManipulation m, float ticks) {
        select(p, m);
        var tendency = HemoCapabilityAccess.getBloodTendency(p);
        var known = HemoCapabilityAccess.getKnownManipulations(p);
        var skills = HemoCapabilityAccess.getSkillProgress(p);
        float alignment = tendency.getAlignmentByTendency(m.getTend());
        double xp = known.getManipLevel(m).getXp();
        int points = skills.getSkillPoints();
        int uses = skills.getTotalManipulationUses();
        h.assertTrue(m.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), ticks),
                m.getName() + " refused at " + ticks + " ticks");
        return new Reward(tendency.getAlignmentByTendency(m.getTend()) - alignment,
                known.getManipLevel(m).getXp() - xp, skills.getSkillPoints() - points,
                skills.getTotalManipulationUses() - uses);
    }

    private static LivingEntity husk(GameTestHelper h, Vec3 at) {
        var husk = EntityType.HUSK.create(h.getLevel());
        husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        husk.getAttribute(Attributes.ARMOR).setBaseValue(0);
        husk.setHealth(100);
        husk.setNoAi(true);
        husk.setNoGravity(true);
        husk.setPos(h.absoluteVec(at));
        h.getLevel().addFreshEntity(husk);
        return husk;
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
        // Family forms are only available once their baseline is mastered far enough.
        known.getKnownManips().put(ManipulationInit.getByName(ManipulationFamilyRegistry.baselineId(m.getName())), new ManipLevel(4, 185));
        known.setEquippedManipNames(List.of(m.getName()));
        known.setSelectedManip(m);
    }

    private static ServerPlayer arenaPlayer(GameTestHelper h) {
        ServerPlayer p = player(h);
        p.setPos(h.absoluteVec(new Vec3(2, 2, 2)));
        h.getLevel().addNewPlayer(p);
        return p;
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance2c"), false);
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
