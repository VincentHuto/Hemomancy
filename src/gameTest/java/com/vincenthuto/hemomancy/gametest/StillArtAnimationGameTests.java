package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.*;

/** Still Arts publish one Unstained RELEASE per successful cast and nothing on refusal. */
@GameTestHolder("casting_animation_validation")
@PrefixGameTestTemplate(false)
public final class StillArtAnimationGameTests {
    @GameTest(template="empty") public static void successfulStillArtReleasesOnceAndRefusalsDoNot(GameTestHelper h) {
        var capture=player(h);var p=capture.player();var art=StillArtInit.silver_rebuke.get();
        try {
            prepare(p,art,100);
            long now=h.getLevel().getGameTime();
            art.performAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition());
            h.assertTrue(art.isOnCooldown(p),"Silver Rebuke fixture did not cast");
            h.assertTrue(capture.packets().size()==1,"Expected one Still Art release, got "+capture.packets().size());
            var packet=capture.packets().getFirst();
            h.assertTrue(packet.phase()==CastPhase.RELEASE && packet.lane()==CastLane.UNSTAINED
                    && packet.stage()==EnumClarityStage.AWAKENED && packet.ending()==StillArtEnding.REBUKE
                    && packet.started()==now && packet.school()==null && packet.rank()==null && packet.purpose()==null
                    && packet.manipulation().equals(art.getName()),"Still Art release carried the wrong presentation: "+packet);
            CastingAnimationManager.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(packet.equals(CastingAnimationManager.current(p)),"Manipulation lookup cancelled a Still Art session");
            art.performAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition());
            h.assertTrue(capture.packets().size()==1,"Cooldown refusal published another release");
        } finally { cleanup(p); }
        var unknown=player(h);
        try {
            prepare(unknown.player(),null,100);
            StillArtInit.lethean_mute.get().performAction(unknown.player(),h.getLevel(),ItemStack.EMPTY,unknown.player().blockPosition());
            h.assertTrue(unknown.packets().isEmpty(),"Unknown Still Art published a release");
        } finally { cleanup(unknown.player()); }
        var weak=player(h);
        try {
            var edge=StillArtInit.autoimmune_edge.get();
            prepare(weak.player(),edge,0);
            edge.performAction(weak.player(),h.getLevel(),ItemStack.EMPTY,weak.player().blockPosition());
            h.assertTrue(weak.packets().isEmpty() && !edge.isOnCooldown(weak.player()),"Too-low clarity published a release");
        } finally { cleanup(weak.player()); }
        h.succeed();
    }
    private static void prepare(ServerPlayer p,StillArt art,float clarity) {
        var progress=HemoCapabilityAccess.getUnstainedProgress(p);
        progress.setClarityUnlocked(true);progress.setClarity(clarity);
        if(art!=null)HemoCapabilityAccess.getKnownStillArts(p).learnArt(art);
    }
    private static void cleanup(ServerPlayer p) {
        CastingAnimationManager.logout(new PlayerEvent.PlayerLoggedOutEvent(p));
        p.discard();
    }
    private static Capture player(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(UUID.randomUUID(),"still-art-test"),false);
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
