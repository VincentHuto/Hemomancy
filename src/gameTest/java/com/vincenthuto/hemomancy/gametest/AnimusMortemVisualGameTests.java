package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import net.minecraft.world.effect.*;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnimusMortemVisualGameTests {
    @GameTest(templateNamespace="minecraft",template="bastion/mobs/empty",batch="animus_mortem_visuals")
    public static void observersReceiveActiveStatusesCuresAndChannelStops(GameTestHelper helper) {
        verify(helper.getLevel(),helper.absoluteVec(new Vec3(2,3,2)));helper.succeed();
    }
    static void verify(ServerLevel level,Vec3 at) {
        var known=new java.util.LinkedHashMap<BloodManipulation,ManipLevel>();
        known.put(ManipulationInit.blood_shot.get(),new ManipLevel(2,25));
        var snapshot=new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket(
                known,ManipulationInit.blood_shot.get(),List.of(),com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation.BLANK,
                "",net.minecraft.core.BlockPos.ZERO,List.of(),List.of());
        var before=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        var after=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket.encode(before,snapshot);
            known.clear();known.put(ManipulationInit.hemorrhage.get(),new ManipLevel(4,185));
            com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket.encode(after,snapshot);
            require(io.netty.buffer.ByteBufUtil.equals(before,after),"Pending knowledge packet changed after server map mutation");
        } finally {before.release();after.release();}

        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"blood-observer"),false);
        var observer=new ServerPlayer(level.getServer(),level,cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);var channel=new EmbeddedChannel(connection);
        var packets=new ArrayList<ManipulationVisualPacket>();var flows=new ArrayList<BloodFlowPacket>();
        new ServerGamePacketListenerImpl(level.getServer(),connection,observer,cookie) {
            @Override public void send(Packet<?> packet) {
                if(packet instanceof ClientboundCustomPayloadPacket custom) {
                    if(custom.payload() instanceof ManipulationVisualPacket cue)packets.add(cue);
                    if(custom.payload() instanceof BloodFlowPacket flow)flows.add(flow);
                }
            }
        };
        observer.setPos(at);var players=players(level);players.add(observer);
        var target=EntityType.HUSK.create(level);target.setPos(at.add(0,0,3));target.setNoAi(true);level.addFreshEntity(target);
        var caster=EntityType.HUSK.create(level);caster.setPos(at);caster.setNoAi(true);level.addFreshEntity(caster);
        try {
            target.addEffect(new MobEffectInstance(EffectInit.blood_loss,180));
            target.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger,160));
            target.addEffect(new MobEffectInstance(EffectInit.grave_debt,140));
            target.addEffect(new MobEffectInstance(MobEffects.POISON,120));
            MortemStatusVisuals.infect(target,120);packets.clear();
            MortemStatusVisuals.tracking(new PlayerEvent.StartTracking(observer,target));
            for(Form form:List.of(Form.WOUND,Form.HUNGER,Form.GRAVE,Form.ROT_INFECTION))
                require(packets.stream().anyMatch(p->p.form()==form&&p.entityId()==target.getId()&&p.ticks()>0),"Late observer missed "+form);
            packets.clear();target.removeAllEffects();ManipulationStatusVisuals.tick(null);MortemStatusVisuals.tick(null);
            for(Form form:List.of(Form.WOUND,Form.HUNGER,Form.GRAVE,Form.ROT_INFECTION))
                require(packets.stream().anyMatch(p->p.form()==form&&p.ticks()==0),"Cure did not retire "+form);
            packets.clear();MortemStatusVisuals.tracking(new PlayerEvent.StartTracking(observer,target));
            require(packets.isEmpty(),"Cured statuses replayed to new observer");
            BloodFlowVisuals.connect(caster,target,BloodFlowPacket.Style.COMMUNION);
            require(flows.stream().anyMatch(p->!p.stop()&&p.start() instanceof com.vincenthuto.hutoslib.common.tendril.TendrilAnchor.Entity e&&e.entityId()==target.getId()),"Communion did not drain from the victim");
            ManipulationVisuals.endChannel(caster,"carrion_communion");
            require(flows.stream().anyMatch(p->p.stop()&&p.owner()==caster.getId()),"Channel cancellation did not stop drain");
            flows.clear();BloodFlowVisuals.connect(caster,target,BloodFlowPacket.Style.EXTRACTION);
            require(flows.stream().anyMatch(p->p.start() instanceof com.vincenthuto.hutoslib.common.tendril.TendrilAnchor.Point),"Execution source depends on a surviving corpse");
            target.addEffect(new MobEffectInstance(MobEffects.WITHER,100));MortemStatusVisuals.infect(target,100);
            target.discard();packets.clear();MortemStatusVisuals.tick(null);
            require(packets.stream().anyMatch(p->p.form()==Form.ROT_INFECTION&&p.ticks()==0),"Source loss left infection");
            org.slf4j.LoggerFactory.getLogger(AnimusMortemVisualGameTests.class).info("ANIMUS_MORTEM_OBSERVER_CHECK PASSED");
        } finally {players.remove(observer);caster.discard();target.discard();observer.discard();channel.finishAndReleaseAll();}
    }
    private static void require(boolean condition,String failure){if(!condition)throw new AssertionError(failure);}
    @SuppressWarnings("unchecked") private static List<ServerPlayer> players(ServerLevel level) {
        try {
            var field=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");field.setAccessible(true);
            return (List<ServerPlayer>)field.get(level.getServer().getPlayerList());
        } catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot register blood packet observer",error);}
    }
}
