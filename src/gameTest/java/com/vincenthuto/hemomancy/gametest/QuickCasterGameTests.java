package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UpdateCurrentManipPacket;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.UUID;

/** Quick Caster: radial and cycle-key selection of a QUICK manipulation casts it through the R-key server path. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class QuickCasterGameTests {
    private static final String EMPTY = "bastion/mobs/empty";
    private static final String BATCH = "quick_caster";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_radial")
    public static void radialSelectionCastsOnlyWhileTheSkillIsEnabled(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            BloodManipulation shot = ManipulationInit.blood_shot.get();
            BloodManipulation cloud = ManipulationInit.blood_cloud.get();
            equip(p, shot, cloud);
            var skills = HemoCapabilityAccess.requireSkillProgress(p);
            skills.setSkill(SkillPointInit.skill_quick_caster, EnumSkillStates.UNLOCKED, 1);

            radial(p, shot);
            h.assertTrue(selected(p) == shot, "Radial selection did not select Blood Shot");
            h.assertTrue(shot.getRemainingCooldownTicks(p) > 0, "Quick Caster did not cast Blood Shot on selection");
            h.assertTrue(blood(p) < 2000, "Quick Caster's cast was free");

            skills.toggleEnabled(SkillPointInit.skill_quick_caster);
            double before = blood(p);
            radial(p, cloud);
            h.assertTrue(selected(p) == cloud, "Radial selection did not select Blood Cloud");
            h.assertTrue(cloud.getRemainingCooldownTicks(p) == 0L, "A disabled Quick Caster still cast on selection");
            h.assertTrue(blood(p) == before, "A disabled Quick Caster still spent blood");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_cycle")
    public static void cycleKeySelectionCasts(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            BloodManipulation shot = ManipulationInit.blood_shot.get();
            BloodManipulation cloud = ManipulationInit.blood_cloud.get();
            equip(p, shot, cloud);
            HemoCapabilityAccess.requireKnownManipulations(p).setSelectedManip(shot);
            HemoCapabilityAccess.requireSkillProgress(p).setSkill(SkillPointInit.skill_quick_caster, EnumSkillStates.UNLOCKED, 1);

            ChangeSelectedManipPacket.handle(new ChangeSelectedManipPacket(),
                    new ServerPayloadContext(p.connection, ChangeSelectedManipPacket.TYPE.id()));
            h.assertTrue(selected(p) == cloud, "The cycle key did not advance to Blood Cloud");
            h.assertTrue(cloud.getRemainingCooldownTicks(p) > 0, "Quick Caster did not cast on a cycle-key selection");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_cooldown")
    public static void coolingSelectionIsOnlySelected(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            BloodManipulation shot = ManipulationInit.blood_shot.get();
            equip(p, shot);
            HemoCapabilityAccess.requireSkillProgress(p).setSkill(SkillPointInit.skill_quick_caster, EnumSkillStates.UNLOCKED, 1);
            radial(p, shot);
            double afterFirst = blood(p);
            long cooldown = shot.getRemainingCooldownTicks(p);
            h.assertTrue(cooldown > 0, "Fixture: first selection did not cast");

            radial(p, shot);
            h.assertTrue(blood(p) == afterFirst, "Reselecting a cooling manipulation spent blood");
            h.assertTrue(shot.getRemainingCooldownTicks(p) <= cooldown, "Reselecting a cooling manipulation restarted its cooldown");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = BATCH + "_excluded")
    public static void chargedSelectionWaitsForTheCastKey(GameTestHelper h) {
        ServerPlayer p = player(h);
        try {
            BloodManipulation storm = ManipulationInit.synaptic_storm.get();
            equip(p, storm);
            HemoCapabilityAccess.requireSkillProgress(p).setSkill(SkillPointInit.skill_quick_caster, EnumSkillStates.UNLOCKED, 1);
            radial(p, storm);
            h.assertTrue(selected(p) == storm, "Radial selection did not select Synaptic Storm");
            h.assertTrue(storm.getRemainingCooldownTicks(p) == 0L && blood(p) == 2000,
                    "Quick Caster cast a CHARGED manipulation on selection");
            h.succeed();
        } finally { p.discard(); }
    }

    private static void radial(ServerPlayer p, BloodManipulation m) {
        int index = HemoCapabilityAccess.requireKnownManipulations(p).getManipList().indexOf(m);
        UpdateCurrentManipPacket.Handler.handle(new UpdateCurrentManipPacket(index),
                new ServerPayloadContext(p.connection, UpdateCurrentManipPacket.TYPE.id()));
    }

    private static BloodManipulation selected(ServerPlayer p) {
        return HemoCapabilityAccess.requireKnownManipulations(p).getSelectedManip();
    }

    private static double blood(ServerPlayer p) {
        return HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
    }

    private static void equip(ServerPlayer p, BloodManipulation... manips) {
        var known = HemoCapabilityAccess.requireKnownManipulations(p);
        for (BloodManipulation m : manips) known.getKnownManips().put(m, new ManipLevel(4, 185));
        known.setEquippedManipNames(java.util.Arrays.stream(manips).map(BloodManipulation::getName).toList());
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "quickcaster"), false);
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
