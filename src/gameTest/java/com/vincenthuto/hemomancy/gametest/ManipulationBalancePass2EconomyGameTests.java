package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonEvents;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Balance pass 2, task 3b: preset swap lockout, vein strain and clotting, kill/cast tendency shifts. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePass2EconomyGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String BATCH = "manipulation_balance_pass_2_economy";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_preset", timeoutTicks = 260)
    public static void presetApplyLocksOutFurtherAppliesAndCoolsNewEquips(GameTestHelper h) {
        ServerPlayer p = player(h);
        boolean handedOff = false;
        try {
            var shot = ManipulationInit.blood_shot.get();
            var hemorrhage = ManipulationInit.hemorrhage.get();
            var rush = ManipulationInit.blood_rush.get();
            var avatar = ManipulationInit.summon_avatar.get();
            var known = HemoCapabilityAccess.getKnownManipulations(p);
            for (BloodManipulation m : List.of(shot, hemorrhage, rush, avatar)) know(p, m, 0);
            known.setEquippedManipNames(List.of(shot.getName()));
            known.setSelectedManip(shot);
            known.setLoadout(0, ManipulationLoadout.of("Lockout A", hemorrhage.getName(),
                    List.of(shot.getName(), hemorrhage.getName(), avatar.getName()), 0));
            known.setLoadout(1, ManipulationLoadout.of("Lockout B", rush.getName(), List.of(rush.getName()), 1));

            staleLockout(p, h.getLevel().getGameTime() + 1_000_000L);
            h.assertTrue(SynapticLoadoutActionPacket.applyLockoutRemaining(p) == 0L,
                    "A lockout stamped on another world's clock still binds");
            h.assertTrue(SynapticLoadoutActionPacket.applyLoadout(p, 0), "The first pattern apply was refused");
            h.assertTrue(known.getEquippedManipNames().contains(hemorrhage.getName()), "Pattern A did not equip Hemorrhage");
            long fresh = hemorrhage.getRemainingCooldownTicks(p);
            h.assertTrue(fresh > 0 && fresh <= SynapticLoadoutActionPacket.NEWLY_EQUIPPED_COOLDOWN_TICKS,
                    "Newly equipped Hemorrhage cooldown was " + fresh);
            h.assertTrue(hemorrhage.isOnCooldown(p), "Newly equipped Hemorrhage is castable");
            h.assertTrue(shot.getRemainingCooldownTicks(p) == 0L, "Already-equipped Blood Shot was put on cooldown");
            h.assertTrue(known.getEquippedManipNames().contains(avatar.getName()), "Pattern A did not equip the avatar");
            h.assertTrue(avatar.getRemainingCooldownTicks(p) == 0L,
                    "A PASSIVE avatar toggle got a swap cooldown it never checks");
            h.assertTrue(SynapticLoadoutActionPacket.applyLockoutRemaining(p) == SynapticLoadoutActionPacket.APPLY_LOCKOUT_TICKS,
                    "Lockout was " + SynapticLoadoutActionPacket.applyLockoutRemaining(p));

            h.assertTrue(!SynapticLoadoutActionPacket.applyLoadout(p, 1), "A second apply within the lockout was accepted");
            h.assertTrue(!known.getEquippedManipNames().contains(rush.getName()), "The refused apply still equipped Blood Rush");

            h.runAfterDelay(SynapticLoadoutActionPacket.APPLY_LOCKOUT_TICKS + 1, () -> {
                try {
                    h.assertTrue(SynapticLoadoutActionPacket.applyLoadout(p, 1), "Apply after the lockout was refused");
                    h.assertTrue(known.getEquippedManipNames().contains(rush.getName()), "Pattern B did not equip Blood Rush");
                    h.succeed();
                } finally { clearProjectiles(h, p); p.discard(); }
            });
            handedOff = true;
        } finally {
            if (!handedOff) p.discard();
        }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_dead")
    public static void deadSectionRefusesManipulationsWithoutPayment(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var shot = ManipulationInit.blood_shot.get();
            know(p, shot, 0);
            setSection(p, EnumVeinSections.HEAD, 0F);
            h.assertTrue(!shot.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                    "Blood Shot cast through a DEAD head");
            near(h, 2000, blood(p), "A refused DEAD-head Blood Shot spent blood");
            h.assertTrue(shot.getRemainingCooldownTicks(p) == 0L, "A refused DEAD-head Blood Shot started its cooldown");
            setSection(p, EnumVeinSections.HEAD, 100F);
            h.assertTrue(shot.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                    "Blood Shot refused once the head recovered");
            h.succeed();
        } finally { clearProjectiles(h, p); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_clotted")
    public static void clottedSectionAddsAQuarterToTheCost(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var shot = ManipulationInit.blood_shot.get();
            know(p, shot, 0);
            double healthy = ManipulationCostLedger.collect(p, shot, 1).effectiveCost();
            if (!BloodMoonEvents.isBloodMoonActive(h.getLevel()))
                near(h, 100, healthy, "Untrained Blood Shot base cost");
            setSection(p, EnumVeinSections.HEAD, 10F);
            var clotted = ManipulationCostLedger.collect(p, shot, 1);
            h.assertTrue(clotted.contribution("clotted_vein").map(c -> Math.abs(c.multiplier() - 1.25) < 1.0E-6).orElse(false),
                    "No 1.25 'Clotted vein' cost term");
            h.assertTrue(shot.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                    "Blood Shot refused through a CLOTTED head");
            near(h, 2000 - healthy * 1.25, blood(p), "CLOTTED-head Blood Shot payment (healthy cost " + healthy + ")");
            h.succeed();
        } finally { clearProjectiles(h, p); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_strain")
    public static void veinStrainScalesWithRank(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            float perRank = HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.get()
                    ? HemoServerConfig.VASCULAR_MANIP_STRAIN.get().floatValue() : 0F;
            var shot = ManipulationInit.blood_shot.get();
            know(p, shot, 0);
            h.assertTrue(shot.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "Blood Shot refused");
            near(h, 100 - perRank, head(p), "HUMILIS Blood Shot head strain");
            setSection(p, EnumVeinSections.HEAD, 100F);
            // Strain only: a real Storm cast would arc into concurrently running tests within 18 blocks.
            var storm = ManipulationInit.synaptic_storm.get();
            h.assertTrue(storm.getRank() == com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.PERFECTUS
                    && storm.getSection() == EnumVeinSections.HEAD, "Fixture: Synaptic Storm is no longer a PERFECTUS head power");
            com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents.applyManipStrain(p, storm);
            near(h, 100 - 3 * perRank, head(p), "PERFECTUS Synaptic Storm head strain");
            h.succeed();
        } finally { clearProjectiles(h, p); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_kills")
    public static void onlyProfiledKillsShiftTendency(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            float base = HemoServerConfig.TENDENCY_SHIFT_ON_KILL_ENABLED.get()
                    ? HemoServerConfig.TENDENCY_SHIFT_AMOUNT.get().floatValue() : 0F;
            Mob giant = mob(h, EntityType.GIANT);
            h.assertTrue(BloodProfileData.profile(EntityType.GIANT, false).tendencies().isEmpty(),
                    "Fixture: the Giant gained a blood profile");
            h.assertTrue(!giant.isInvertedHealAndHarm(), "Fixture: the Giant is undead");
            Map<EnumBloodTendency, Float> before = alignments(p);
            giant.die(p.damageSources().playerAttack(p));
            giant.discard();
            for (var tendency : EnumBloodTendency.values())
                near(h, before.get(tendency), alignments(p).get(tendency), "Unprofiled Giant kill shifted " + tendency);

            // A profiled mob no predicate claims still teaches its profile (the Ravager's blood is Ferric).
            h.assertTrue(BloodProfileData.profile(EntityType.RAVAGER, false).tendencies().equals(List.of(EnumBloodTendency.FERRIC)),
                    "Fixture: the Ravager's blood profile changed");
            Mob ravager = mob(h, EntityType.RAVAGER);
            before = alignments(p);
            ravager.die(p.damageSources().playerAttack(p));
            ravager.discard();
            near(h, before.get(EnumBloodTendency.FERRIC) + base, alignments(p).get(EnumBloodTendency.FERRIC), "Ravager kill Ferric shift");
            near(h, before.get(EnumBloodTendency.ANIMUS), alignments(p).get(EnumBloodTendency.ANIMUS), "Ravager kill shifted Animus");
            h.succeed();
        } finally { clearProjectiles(h, p); p.discard(); }
    }

    /**
     * Blood Shot projectiles outlive their test (200 ticks) and can fly into concurrently running tests' fixtures;
     * remove this test player's projectiles in the tick they were fired.
     */
    private static void clearProjectiles(GameTestHelper h, ServerPlayer p) {
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,
                        p.getBoundingBox().inflate(16), projectile -> projectile.getOwner() == p)
                .forEach(net.minecraft.world.entity.Entity::discard);
    }

    @SuppressWarnings("unchecked")
    private static void staleLockout(ServerPlayer p, long until) {
        try {
            var field = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.SynapticLoadoutLockout.class
                    .getDeclaredField("APPLY_LOCKOUT_UNTIL");
            field.setAccessible(true);
            ((Map<UUID, Long>) field.get(null)).put(p.getUUID(), until);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Mob mob(GameTestHelper h, EntityType<? extends Mob> type) {
        Mob mob = type.create(h.getLevel());
        mob.setNoAi(true);
        mob.setPos(h.absoluteVec(new Vec3(4.5, 2, 4.5)));
        h.getLevel().addFreshEntity(mob);
        return mob;
    }

    private static Map<EnumBloodTendency, Float> alignments(ServerPlayer p) {
        var tendency = HemoCapabilityAccess.getBloodTendency(p);
        Map<EnumBloodTendency, Float> values = new EnumMap<>(EnumBloodTendency.class);
        for (var t : EnumBloodTendency.values()) values.put(t, tendency.getAlignmentByTendency(t));
        return values;
    }

    private static void setSection(ServerPlayer p, EnumVeinSections section, float health) {
        HemoCapabilityAccess.getVascularSystem(p).getVascularSystem().put(section, health);
    }

    private static float head(ServerPlayer p) {
        return HemoCapabilityAccess.getVascularSystem(p).getHealthBySection(EnumVeinSections.HEAD);
    }

    private static void know(ServerPlayer p, BloodManipulation m, int level) {
        var known = HemoCapabilityAccess.getKnownManipulations(p).getKnownManips();
        known.put(m, new ManipLevel(level, 0));
        // Family forms are only available once their baseline is mastered far enough.
        var baseline = ManipulationInit.getByName(ManipulationFamilyRegistry.baselineId(m.getName()));
        if (baseline != null && baseline != m) known.put(baseline, new ManipLevel(4, 185));
    }

    private static double blood(ServerPlayer p) {
        return HemoCapabilityAccess.getBloodVolume(p).getBloodVolume();
    }

    private static void near(GameTestHelper h, double expected, double actual, String message) {
        h.assertTrue(Math.abs(expected - actual) < 0.002, message + ": expected " + expected + ", got " + actual);
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance2e"), false);
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
