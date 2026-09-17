package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@GameTestHolder("casting_animation_validation")
@PrefixGameTestTemplate(false)
public final class CastingAnimationGameTests {
    @GameTest(template="empty") public static void everyActiveRegistryEntryHasPresentation(GameTestHelper h) {
        for(var manipulation:ManipulationInit.MANIPS_TYPE_REGISTRY) {
            if(ManipulationRetirementRules.isRetiredManipulation(manipulation))continue;
            h.assertTrue(CastPresentation.hasExplicit(manipulation.getName()),"Missing presentation: "+manipulation.getName());
            h.assertTrue(!manipulation.serialize().contains("cast_purpose"),"Presentation polluted saved memories");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void onlyAcceptedQuickCastReleasesAndGameplayRemainsImmediate(GameTestHelper h) {
        var capture=player(h); var p=capture.player(); var m=ManipulationInit.blood_shot.get();
        try {
            prepare(p,m);
            var volume=HemoCapabilityAccess.requireBloodVolume(p);
            double before=volume.getBloodVolume(); long now=h.getLevel().getGameTime();
            h.assertTrue(m.tryPerformAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),0),"Valid quick cast rejected");
            h.assertTrue(volume.getBloodVolume()<before && m.isOnCooldown(p),"Cast payment or cooldown was delayed");
            h.assertTrue(capture.packets().size()==1 && capture.packets().getFirst().phase()==CastPhase.RELEASE
                    && capture.packets().getFirst().started()==now,"Quick cast did not release on its gameplay tick");
            h.assertTrue(!m.tryPerformAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),0),"Cooldown did not reject cast");
            h.assertTrue(capture.packets().size()==1,"Rejected cast emitted another release");
        } finally { cleanup(p); }
        h.succeed();
    }
    @GameTest(template="empty") public static void chargeCancellationCannotCancelAnotherManipulation(GameTestHelper h) {
        var capture=player(h);var p=capture.player();var m=ManipulationInit.blood_needle.get();
        try {
            prepare(p,m); double before=HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
            CastingAnimationManager.charge(p,m.getName(),1);
            var first=CastingAnimationManager.current(p);
            h.assertTrue(first!=null && first.phase()==CastPhase.OPENING,"Charge never opened");
            CastingAnimationManager.charge(p,"ironhearted",0);
            h.assertTrue(CastingAnimationManager.current(p).phase()==CastPhase.OPENING,"Stale cancellation killed another cast");
            CastingAnimationManager.charge(p,m.getName(),0);
            h.assertTrue(CastingAnimationManager.current(p).phase()==CastPhase.CANCEL,"Release did not cancel preview");
            h.assertTrue(HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume()==before && !m.isOnCooldown(p),
                    "Presentation request spent blood or started cooldown");
        } finally { cleanup(p); }
        h.succeed();
    }
    @GameTest(template="empty") public static void lateTrackingGetsHoldButNeverReplaysRelease(GameTestHelper h) {
        var caster=player(h);var observer=player(h);var m=ManipulationInit.blood_needle.get();
        try {
            prepare(caster.player(),m);
            CastingAnimationManager.charge(caster.player(),m.getName(),1);
            CastingAnimationManager.tracking(new PlayerEvent.StartTracking(observer.player(),caster.player()));
            h.assertTrue(observer.packets().size()==1 && observer.packets().getFirst().sequence()
                    ==CastingAnimationManager.current(caster.player()).sequence(),"Late observer lost active session");
            CastingAnimationManager.release(caster.player(),m);
            observer.packets().clear();
            CastingAnimationManager.tracking(new PlayerEvent.StartTracking(observer.player(),caster.player()));
            h.assertTrue(observer.packets().isEmpty(),"Late observer replayed an old release");
        } finally { cleanup(caster.player());cleanup(observer.player()); }
        h.succeed();
    }
    @GameTest(template="empty") public static void acceptedChargeReleasesSameSequenceAndRejectedAttemptCancels(GameTestHelper h) {
        var capture=player(h);var p=capture.player();var m=ManipulationInit.blood_needle.get();
        try {
            prepare(p,m);CastingAnimationManager.charge(p,m.getName(),1);
            long sequence=CastingAnimationManager.current(p).sequence();
            h.assertTrue(m.tryPerformAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),m.getRequiredChargeTicks()),"Charged cast failed");
            CastingAnimationManager.finishChargeAttempt(p);
            var release=CastingAnimationManager.current(p);
            h.assertTrue(release.sequence()==sequence && release.phase()==CastPhase.RELEASE,"Accepted charge split its cast sequence");
        } finally { cleanup(p); }
        var rejected=player(h).player();
        try {
            prepare(rejected,m);CastingAnimationManager.charge(rejected,m.getName(),1);
            h.assertTrue(CastingAnimationManager.current(rejected).phase()==CastPhase.OPENING,"Rejected fixture did not open charge");
            CastingAnimationManager.finishChargeAttempt(rejected);
            h.assertTrue(CastingAnimationManager.current(rejected).phase()==CastPhase.CANCEL,"Rejected attempt left a charge loop");
        } finally { cleanup(rejected); }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=70) public static void damageChargeUpdateKeepsClockAndSelectionChangeCancels(GameTestHelper h) {
        var capture=player(h);var p=capture.player();var m=ManipulationInit.blood_needle.get();
        prepare(p,m);CastingAnimationManager.charge(p,m.getName(),8);
        var first=CastingAnimationManager.current(p);
        h.assertTrue(first!=null,"Charge did not start");
        h.runAfterDelay(4,() -> {
            try {
                CastingAnimationManager.charge(p,m.getName(),2);
                var updated=CastingAnimationManager.current(p);
                h.assertTrue(updated.sequence()==first.sequence() && updated.started()==first.started() && updated.held()==2,
                        "Charge damage restarted opening or retained old intensity");
                HemoCapabilityAccess.requireKnownManipulations(p).setSelectedManip(ManipulationInit.blood_shot.get());
                CastingAnimationManager.tick(new PlayerTickEvent.Post(p));
                h.assertTrue(CastingAnimationManager.current(p).phase()==CastPhase.CANCEL,"Selection change left a charging pose");
                h.succeed();
            } finally { cleanup(p); }
        });
    }
    @GameTest(template="empty",timeoutTicks=80) public static void continuousPulseDoesNotRestartAnimation(GameTestHelper h) {
        var capture=player(h);var p=capture.player();var m=ManipulationInit.sanguine_ward.get();
        prepare(p,m);ManipulationChannelManager.start(p);
        var first=CastingAnimationManager.current(p);
        h.assertTrue(first!=null,"Channel failed to start");
        h.runAfterDelay(22,() -> {
            try {
                ManipulationChannelManager.onPlayerTick(new PlayerTickEvent.Post(p));
                CastingAnimationManager.tick(new PlayerTickEvent.Post(p));
                var updated=CastingAnimationManager.current(p);
                h.assertTrue(updated.sequence()==first.sequence() && updated.started()==first.started(),"Channel pulse restarted animation");
                ManipulationChannelManager.stop(p,false);
                h.assertTrue(CastingAnimationManager.current(p).phase()==CastPhase.CANCEL,"Interrupted channel released instead of cancelling");
                h.succeed();
            } finally { cleanup(p); }
        });
    }
    @GameTest(template="empty") public static void allSchoolRankPhasePacketsRoundTrip(GameTestHelper h) {
        var id=UUID.randomUUID();
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values())for(var phase:CastPhase.values()) {
            var packet=new CastingAnimationPacket(42,id,"blood_shot",73,school,EnumBloodTendency.MORTEM,rank,
                    CastPurpose.AREA,CastStyle.SCHOOL,phase,12345,12350,5,40,net.minecraft.world.InteractionHand.OFF_HAND);
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                CastingAnimationPacket.STREAM_CODEC.encode(buffer,packet);
                h.assertTrue(packet.equals(CastingAnimationPacket.STREAM_CODEC.decode(buffer)),"Lost casting packet state");
            } finally { buffer.release(); }
        }
        h.succeed();
    }
    private static void prepare(ServerPlayer p,BloodManipulation m) {
        var volume=HemoCapabilityAccess.requireBloodVolume(p);volume.setActive(true);volume.fill(100000);
        HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(m.getTend(),100);
        var known=HemoCapabilityAccess.requireKnownManipulations(p);
        known.getKnownManips().put(m,new ManipLevel(0,0));known.setSelectedManip(m);known.setEquippedManipNames(List.of(m.getName()));
    }
    private static void cleanup(ServerPlayer p) {
        ManipulationChannelManager.stop(p,false);
        CastingAnimationManager.logout(new PlayerEvent.PlayerLoggedOutEvent(p));
        p.discard();
    }
    private static Capture player(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(UUID.randomUUID(),"casting-test"),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        var packets=new ArrayList<CastingAnimationPacket>();
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket payload
                        && payload.payload() instanceof CastingAnimationPacket animation)packets.add(animation);
            }
        };
        p.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,2,1))));
        h.getLevel().addNewPlayer(p);
        return new Capture(p,packets);
    }
    private record Capture(ServerPlayer player,List<CastingAnimationPacket> packets) {}
}
