package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.*;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FerricDuctilisObserverGameTests {
    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ferric_visuals")
    public static void lateObserversSeeRemainingNervesPoolsAndMovingLinksAndTheirRemoval(GameTestHelper h) {
        var level=h.getLevel();var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"ferric_viewer"),false);
        var observer=new ServerPlayer(level.getServer(),level,cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);var channel=new EmbeddedChannel(connection);
        var packets=new ArrayList<CustomPacketPayload>();
        new ServerGamePacketListenerImpl(level.getServer(),connection,observer,cookie) {
            @Override public void send(Packet<?> packet) {
                if(packet instanceof ClientboundCustomPayloadPacket custom)packets.add(custom.payload());
            }
        };
        observer.setPos(h.absoluteVec(new Vec3(2,3,2)));var players=players(level);players.add(observer);
        var target=EntityType.HUSK.create(level);target.setPos(observer.position().add(0,0,3));target.setNoAi(true);level.addFreshEntity(target);
        try {
            target.addEffect(new MobEffectInstance(EffectInit.conductive_mark,60));
            com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.apply(target,12);
            FerricDuctilisStatusVisuals.link(observer,target,25);packets.clear();
            FerricDuctilisStatusVisuals.tracking(new PlayerEvent.StartTracking(observer,target));
            h.assertTrue(has(packets,Form.MARK,60)&&has(packets,Form.PARALYSIS,12),"Late tracker lost active mark/paralysis");
            h.assertTrue(packets.stream().anyMatch(p->p instanceof NerveLinkPacket link&&link.ticks()==25),"Late tracker lost the moving connection");
            packets.clear();target.removeEffect(EffectInit.conductive_mark);target.removeEffect(EffectInit.paralysis);ManipulationStatusVisuals.tick(null);
            h.assertTrue(has(packets,Form.MARK,0)&&has(packets,Form.PARALYSIS,0),"Dispel did not remove remote nerve states");
            packets.clear();FerricDuctilisStatusVisuals.tracking(new PlayerEvent.StartTracking(observer,target));
            h.assertTrue(packets.stream().noneMatch(p->p instanceof ManipulationVisualPacket cue&&(cue.form()==Form.MARK||cue.form()==Form.PARALYSIS)),"Dispelled nerves replayed to a new observer");
            ManipulationVisuals.attached(observer,Form.WARD,1,25,8);ManipulationVisuals.attached(observer,Form.WARD,1,25,3);packets.clear();
            FerricDuctilisStatusVisuals.tracking(new PlayerEvent.StartTracking(observer,observer));
            h.assertTrue(packets.stream().anyMatch(p->p instanceof ManipulationVisualPacket cue&&cue.form()==Form.WARD&&cue.count()==3),"Late tracker received the old protection pool");
            packets.clear();ManipulationVisuals.endChannel(observer,"sanguine_ward");
            h.assertTrue(has(packets,Form.WARD,0),"Stopping a channel left the electrical sheath");
            packets.clear();target.discard();FerricDuctilisStatusVisuals.tick(null);
            h.assertTrue(packets.stream().anyMatch(p->p instanceof NerveLinkPacket link&&link.ticks()==0),"Source loss did not remove the moving connection");
            h.succeed();
        } finally {players.remove(observer);target.discard();observer.discard();channel.finishAndReleaseAll();}
    }
    private static boolean has(List<CustomPacketPayload> packets,Form form,int ticks) {
        return packets.stream().anyMatch(p->p instanceof ManipulationVisualPacket cue&&cue.form()==form&&cue.ticks()==ticks);
    }
    @SuppressWarnings("unchecked") private static List<ServerPlayer> players(ServerLevel level) {
        try {
            var field=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");field.setAccessible(true);
            return (List<ServerPlayer>)field.get(level.getServer().getPlayerList());
        } catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot register disposable packet observer",error);}
    }
}
