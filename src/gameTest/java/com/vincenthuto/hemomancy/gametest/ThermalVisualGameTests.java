package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
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
public final class ThermalVisualGameTests {
    @GameTest(templateNamespace="minecraft",template="bastion/mobs/empty",batch="thermal_visuals")
    public static void castsAndLateObserversReceiveDistinctCuesAndThawRemoval(GameTestHelper helper) {
        verify(helper.getLevel(),helper.absoluteVec(new Vec3(2,3,2)));helper.succeed();
    }
    @GameTest(templateNamespace="minecraft",template="bastion/mobs/empty",batch="thermal_visuals")
    public static void temporaryCruorStillExpiresWithoutChangingTheWallFootprint(GameTestHelper helper) {
        var level=helper.getLevel();var pos=helper.absolutePos(new net.minecraft.core.BlockPos(1,2,1));
        var neighbor=pos.east();
        level.setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(neighbor,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        require(com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager.place(level,pos,
                com.vincenthuto.hemomancy.common.init.BlockInit.frozen_cruor.get().defaultBlockState(),4),"Cruor placement failed");
        helper.runAfterDelay(8,()->{
            require(level.getBlockState(pos).isAir(),"Temporary cruor did not expire");
            require(level.getBlockState(neighbor).is(net.minecraft.world.level.block.Blocks.STONE),"Expiry changed adjacent collision");
            helper.succeed();
        });
    }
    static void verify(ServerLevel level,Vec3 at) {
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"thermal-observer"),false);
        var observer=new ServerPlayer(level.getServer(),level,cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);var channel=new EmbeddedChannel(connection);
        var packets=new ArrayList<ManipulationVisualPacket>();
        new ServerGamePacketListenerImpl(level.getServer(),connection,observer,cookie) {
            @Override public void send(Packet<?> packet) {
                if(packet instanceof ClientboundCustomPayloadPacket custom && custom.payload() instanceof ManipulationVisualPacket cue)packets.add(cue);
            }
        };
        observer.setPos(at);var players=players(level);players.add(observer);
        var target=EntityType.HUSK.create(level);target.setPos(at.add(0,0,3));target.setNoAi(true);level.addFreshEntity(target);
        try {
            com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip.settleDebt(observer);
            require(packets.stream().noneMatch(p->p.form()==Form.HOUR_BREAK),"Inactive Hour emitted settlement fragments");
            packets.clear();
            ManipulationInit.sanguine_ignition.get().getAction(observer,level,ItemStack.EMPTY,observer.blockPosition());
            require(packets.stream().anyMatch(p->p.form()==Form.IGNITION),"Ignition still shares Furnace's channel cue");
            packets.clear();target.invulnerableTime=0;
            ManipulationInit.cryogenic_pulse.get().getAction(observer,level,ItemStack.EMPTY,observer.blockPosition());
            require(packets.stream().anyMatch(p->p.form()==Form.CRYOGENIC_PULSE),"Cryogenic Pulse lost its pressure front");
            require(packets.stream().anyMatch(p->p.form()==Form.BONE && p.entityId()==target.getId()),"Cold impact was not attached to the affected target");
            target.setTicksFrozen(80);packets.clear();
            ManipulationVisuals.attached(target,Form.FROZEN_VEINS,.6,80,1);
            packets.clear();
            ThermalStatusVisuals.tracking(new PlayerEvent.StartTracking(observer,target));
            require(packets.stream().anyMatch(p->p.form()==Form.FROZEN_VEINS && p.ticks()>0),"Late observer missed active frost");
            packets.clear();target.setTicksFrozen(0);ThermalStatusVisuals.tick(null);
            require(packets.stream().anyMatch(p->p.form()==Form.FROZEN_VEINS && p.ticks()==0),"Thaw did not remove the remote frost cue");
            packets.clear();ManipulationVisuals.endChannel(observer,"furnace_veins");
            require(packets.stream().anyMatch(p->p.form()==Form.FURNACE && p.ticks()==0),"Channel cancellation did not retire Furnace");
            packets.clear();
            var caster=EntityType.HUSK.create(level);caster.setPos(at.add(1,0,0));caster.setTarget(target);
            EntityManipulationEffects.cast(ManipulationInit.vitric_combustion.get(),ManipulationCastContext.forWill(caster,target,ItemStack.EMPTY));
            require(packets.stream().anyMatch(p->p.form()==Form.GLASS),"NPC Vitric still emits only generic particles");
            caster.discard();
            packets.clear();target.setTicksFrozen(100);
            ManipulationVisuals.attached(target,Form.FROZEN_VEINS,.6,100,1);
            target.discard();packets.clear();ThermalStatusVisuals.tick(null);
            require(packets.stream().anyMatch(p->p.form()==Form.FROZEN_VEINS && p.ticks()==0),"Source loss left active frost");
            packets.clear();ThermalStatusVisuals.tracking(new PlayerEvent.StartTracking(observer,target));
            require(packets.isEmpty(),"Removed frost was replayed to a new observer");
        } finally {players.remove(observer);target.discard();observer.discard();channel.finishAndReleaseAll();}
    }
    private static void require(boolean condition,String failure){if(!condition)throw new AssertionError(failure);}
    @SuppressWarnings("unchecked") private static List<ServerPlayer> players(ServerLevel level) {
        try {
            var field=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");field.setAccessible(true);
            return (List<ServerPlayer>)field.get(level.getServer().getPlayerList());
        } catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot register thermal packet observer",error);}
    }
}
