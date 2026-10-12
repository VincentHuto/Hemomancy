package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.projectile.CloudEntityBlood;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodBindingManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodRushManip;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Balance pass 2, task 4: the Animus re-rank and the Ductilis command moves. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationBalancePass2RosterGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String ROOM = "combat_targeting_room";
    private static final String BATCH = "manipulation_balance_pass_2_roster";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void animusRosterSitsInItsNewRanks(GameTestHelper h) {
        Map<String, EnumManipulationRank> ranks = Map.of(
                "hematic_mortar", EnumManipulationRank.MEDIOCRITAS,
                "sanguine_halo", EnumManipulationRank.MEDIOCRITAS,
                "blood_lattice", EnumManipulationRank.MEDIOCRITAS,
                "summon_avatar", EnumManipulationRank.MEDIOCRITAS,
                "summon_avatar_arms", EnumManipulationRank.MEDIOCRITAS,
                "sanguine_tempest", EnumManipulationRank.MAGISTER,
                "summon_avatar_complete", EnumManipulationRank.MAGISTER,
                "sovereign_instinct", EnumManipulationRank.MAGISTER);
        ranks.forEach((name, rank) -> {
            BloodManipulation m = ManipulationInit.getByName(name);
            h.assertTrue(m.getRank() == rank, name + " is " + m.getRank() + ", expected " + rank);
        });
        schools(h, "sanguine_marionette", EnumBloodTendency.DUCTILIS, EnumBloodTendency.ANIMUS);
        schools(h, "sovereign_instinct", EnumBloodTendency.DUCTILIS, EnumBloodTendency.ANIMUS);
        schools(h, "sanguine_tempest", EnumBloodTendency.ANIMUS, EnumBloodTendency.DUCTILIS);
        schools(h, "crimson_tithe", EnumBloodTendency.MORTEM, EnumBloodTendency.ANIMUS);
        h.assertTrue(BloodBindingManip.durationTicks(BloodBindingManip.Mode.CHAIN) == 80, "Chain Binding is not 80 ticks");
        h.assertTrue(BloodBindingManip.durationTicks(BloodBindingManip.Mode.LATTICE) == 160, "Blood Lattice changed");
        h.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void mortarOpensAtDegreeOneAndTempestWaitsForMagister(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            var known = HemoCapabilityAccess.requireKnownManipulations(p);
            known.getKnownManips().put(ManipulationInit.blood_shot.get(), new ManipLevel(3, 50));
            known.getKnownManips().put(ManipulationInit.blood_cloud.get(), new ManipLevel(4, 185));
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(p);

            degree.setDegreeNumber(1);
            var mortar = KnownManipulationGrantHelper.checkMemoryGrant(p, ManipulationInit.hematic_mortar.get());
            h.assertTrue(mortar.status() == KnownManipulationGrantHelper.MemoryGrantStatus.GRANTED,
                    "D1 with Shot mastery 3 could not learn Mortar: " + mortar.status());

            degree.setDegreeNumber(3);
            var tempest = KnownManipulationGrantHelper.checkMemoryGrant(p, ManipulationInit.sanguine_tempest.get());
            h.assertTrue(tempest.status() == KnownManipulationGrantHelper.MemoryGrantStatus.RANK_TOO_LOW,
                    "D3 was not refused Tempest: " + tempest.status());

            degree.setDegreeNumber(5);
            tempest = KnownManipulationGrantHelper.checkMemoryGrant(p, ManipulationInit.sanguine_tempest.get());
            h.assertTrue(tempest.status() == KnownManipulationGrantHelper.MemoryGrantStatus.GRANTED,
                    "D5 with Cloud mastery 4 could not learn Tempest: " + tempest.status());
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void bloodRushIsAnEightSecondFirstLevelSurge(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            BloodManipulation rush = ManipulationInit.blood_rush.get();
            rush.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
            var effect = p.getEffect(EffectInit.blood_rush);
            h.assertTrue(effect != null, "Blood Rush applied no effect");
            h.assertTrue(effect.getAmplifier() == 0, "Blood Rush amplifier is " + effect.getAmplifier());
            h.assertTrue(effect.getDuration() == 160, "Blood Rush duration is " + effect.getDuration());
            h.assertTrue(BloodRushManip.EFFECT_DURATION_TICKS == 160 && BloodRushManip.EFFECT_AMPLIFIER == 0,
                    "Blood Rush constants drifted");
            h.assertTrue(rush.getCooldownTicks() == 160, "Blood Rush cooldown is " + rush.getCooldownTicks());
            h.succeed();
        } finally { p.discard(); }
    }

    // Clouds reach past a fixture's bounds, so the cloud test runs in a batch of its own.
    @GameTest(templateNamespace = "hemomancy", template = ROOM, batch = BATCH + "_cloud", timeoutTicks = 200)
    public static void aBasicCloudPressuresAHuskOnceOnFirstContact(GameTestHelper h) {
        whenLoaded(h, () -> cloudPressure(h));
    }

    private static void cloudPressure(GameTestHelper h) {
        ServerPlayer p = player(h);
        var husk = EntityType.HUSK.create(h.getLevel());
        husk.setNoAi(true);
        husk.setNoGravity(true);
        husk.setPos(h.absoluteVec(new Vec3(8.5, 2, 8.5)));
        h.getLevel().addFreshEntity(husk);
        var cloud = new CloudEntityBlood(EntityInit.blood_cloud.get(), h.getLevel(), p);
        BloodManipulation manip = ManipulationInit.blood_cloud.get();
        cloud.setDamageTendencies(manip.getTend(), manip.getSecondaryTend());
        cloud.configure(1.25D, 100, CloudEntityBlood.Mode.STATIC);
        cloud.setNoGravity(true);
        cloud.setPos(husk.position().add(0, 1.5, 0));
        // The carrier hands its cloud the cast's context; do the same without the carrier's flight.
        SchoolDamage.capture(cloud, SchoolDamage.context(manip, p));
        h.getLevel().addFreshEntity(cloud);
        float start = husk.getHealth();
        h.runAfterDelay(35, () -> {
            try {
                h.assertTrue(husk.getHealth() < start, "The cloud never struck the husk");
                h.assertTrue(SchoolStates.has(husk, SchoolState.PRESSURE), "The cloud's first contact applied no Pressure");
                h.assertTrue(SchoolStates.levels(husk, SchoolState.PRESSURE) == 1,
                        "Periodic cloud ticks stacked Pressure to " + SchoolStates.levels(husk, SchoolState.PRESSURE));
                h.succeed();
            } finally { cloud.discard(); husk.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void avatarDoesNotTurnVoidOrHunger(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            HemoCapabilityAccess.requireKnownManipulations(p).setActiveAvatarForm("summon_avatar");
            var sources = h.getLevel().damageSources();
            near(h, 3, avatarDamage(p, sources.generic(), 4), "The avatar no longer turns an ordinary blow");
            near(h, 4, avatarDamage(p, sources.fellOutOfWorld(), 4), "The avatar reduced void damage");
            near(h, 2, avatarDamage(p, sources.starve(), 2), "The avatar reduced starvation");
            h.succeed();
        } finally { p.discard(); }
    }

    /** A fake player keeps its spawn invulnerability, so drive the avatar's damage hook directly. */
    private static float avatarDamage(ServerPlayer p, DamageSource source, float amount) {
        var event = new LivingDamageEvent.Pre(p, new DamageContainer(source, amount));
        KnownManipulationEvents.onPlayerDamage(event);
        return event.getNewDamage();
    }

    /** Review focus 1: a save made while Marionette was an Animus Binding form. */
    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH)
    public static void anOldAnimusMarionetteAdoptsDuctilisAndStaysEquipped(GameTestHelper h) {
        BloodManipulation oldMarionette = new BloodManipulation("sanguine_marionette", 50, 0, 0,
                EnumManipulationType.CONTINUOUS, EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS,
                EnumVeinSections.LEGS);
        oldMarionette.setSecondaryTend(EnumBloodTendency.DUCTILIS);
        KnownManipulations original = new KnownManipulations();
        LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
        map.put(BloodManipulation.deserialize(ManipulationInit.blood_binding.get().serialize()), new ManipLevel(3, 60));
        map.put(BloodManipulation.deserialize(oldMarionette.serialize()), new ManipLevel(3, 60));
        original.setKnownManips(map);
        original.setEquippedManipNames(List.of("sanguine_marionette"));
        original.setSelectedManip(map.keySet().stream().skip(1).findFirst().orElseThrow());
        ListTag save = original.serializeNBT(h.getLevel().registryAccess());
        for (int i = 0; i < save.size(); i++) {
            CompoundTag tag = save.getCompound(i);
            if (tag.contains("familyMemoryVersion")) tag.putInt("familyMemoryVersion", 1);
        }

        KnownManipulations restored = new KnownManipulations();
        restored.deserializeNBT(h.getLevel().registryAccess(), save);
        ManipulationRetirementRules.sanitizeKnownManipulations(restored);

        BloodManipulation registered = ManipulationInit.sanguine_marionette.get();
        BloodManipulation marionette = named(restored, "sanguine_marionette");
        h.assertTrue(marionette.getTend() == EnumBloodTendency.DUCTILIS, "Old Marionette kept school " + marionette.getTend());
        h.assertTrue(marionette.getSecondaryTend() == EnumBloodTendency.ANIMUS,
                "Old Marionette kept secondary " + marionette.getSecondaryTend());
        h.assertTrue(marionette.getRank() == registered.getRank() && marionette.getCost() == registered.getCost(),
                "Old Marionette kept its saved rank or cost");
        h.assertTrue(restored.getSelectedManip().getTend() == EnumBloodTendency.DUCTILIS, "The selected copy kept Animus");
        h.assertTrue(restored.getEquippedManipNames().contains("sanguine_marionette"), "Old Marionette was unequipped");
        h.assertTrue(restored.isManipulationAvailable(marionette), "Old Marionette is no longer usable");
        h.assertTrue(restored.getManipLevel(named(restored, "blood_binding")).getCurrentLevel() == 3,
                "Blood Binding mastery was lost");
        h.succeed();
    }

    private static void whenLoaded(GameTestHelper h, Runnable body) {
        poll(h, body, 0);
    }

    private static void poll(GameTestHelper h, Runnable body, int attempt) {
        boolean loaded = true;
        for (Vec3 at : List.of(new Vec3(2.5, 2, 2.5), new Vec3(8.5, 2, 8.5)))
            loaded &= h.getLevel().areEntitiesLoaded(ChunkPos.asLong(BlockPos.containing(h.absoluteVec(at))));
        if (loaded || attempt >= 100) body.run();
        else h.runAfterDelay(1, () -> poll(h, body, attempt + 1));
    }

    private static BloodManipulation named(KnownManipulations known, String name) {
        return known.getManipList().stream().filter(m -> name.equals(m.getName())).findFirst().orElseThrow();
    }

    private static void schools(GameTestHelper h, String name, EnumBloodTendency primary, EnumBloodTendency secondary) {
        BloodManipulation m = ManipulationInit.getByName(name);
        h.assertTrue(m.getTend() == primary && m.getSecondaryTend() == secondary,
                name + " is " + m.getTend() + "/" + m.getSecondaryTend() + ", expected " + primary + "/" + secondary);
    }

    private static void near(GameTestHelper h, double expected, double actual, String message) {
        h.assertTrue(Math.abs(expected - actual) < 0.002, message + ": expected " + expected + ", got " + actual);
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "balance2r"), false);
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
        return p;
    }
}
