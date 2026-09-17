package com.vincenthuto.hemomancy.common.manipulation.animation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Transient presentation sessions. This class never spends blood, executes powers, or changes movement. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class CastingAnimationManager {
    private static final Map<UUID,CastingAnimationPacket> ACTIVE = new HashMap<>();
    private static long sequence;
    private CastingAnimationManager() {}

    public static CastingAnimationPacket current(ServerPlayer player) { return ACTIVE.get(player.getUUID()); }

    public static void release(ServerPlayer player,BloodManipulation manipulation) {
        publish(player,create(player,manipulation,CastPhase.RELEASE,0));
    }
    public static void passive(ServerPlayer player,BloodManipulation manipulation,boolean active) {
        publish(player,create(player,manipulation,active?CastPhase.RELEASE:CastPhase.CANCEL,0));
    }
    public static void channelStart(ServerPlayer player,BloodManipulation manipulation) {
        publish(player,create(player,manipulation,CastPhase.OPENING,0));
    }
    public static void channelStop(ServerPlayer player,boolean released) {
        var old=current(player);
        if(old!=null && old.phase().sustained()) finish(player,old,released?CastPhase.RELEASE:CastPhase.CANCEL);
    }
    public static void finishChargeAttempt(ServerPlayer player) {
        var old=current(player);
        if(old==null || !old.phase().sustained())return;
        var manipulation=ManipulationInit.getByName(old.manipulation());
        // Successful execution has already published RELEASE using this sequence.
        if(manipulation!=null && manipulation.getType()==EnumManipulationType.CHARGED)finish(player,old,CastPhase.CANCEL);
    }
    public static void charge(ServerPlayer player,String id,int held) {
        var old=current(player);
        if(held==0) {
            if(old!=null && old.manipulation().equals(id) && old.phase().sustained()) finish(player,old,CastPhase.CANCEL);
            return;
        }
        BloodManipulation manipulation=ManipulationInit.getByName(id);
        if(held<0 || manipulation==null || manipulation.getType()!=EnumManipulationType.CHARGED
                || held>manipulation.getRequiredChargeTicks() || !canPresent(player,manipulation)
                || manipulation.isOnCooldown(player)) return;
        long now=player.level().getGameTime();
        if(old!=null && old.manipulation().equals(id) && old.phase().sustained()) {
            if(now-old.updated()<2) return;
            publish(player,old.at(now-old.started()<6?CastPhase.OPENING:CastPhase.HOLD,old.started(),now,held));
        } else publish(player,create(player,manipulation,CastPhase.OPENING,held));
    }
    private static boolean canPresent(ServerPlayer player,BloodManipulation manipulation) {
        var known=HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
        return player.isAlive() && !ManipulationRetirementRules.isRetiredManipulation(manipulation)
                && !com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(player)
                && known!=null && known.getSelectedManip()!=null
                && known.getSelectedManip().getName().equals(manipulation.getName())
                && known.isManipEquipped(manipulation) && known.isManipulationAvailable(manipulation)
                && HemoCapabilityAccess.requireBloodVolume(player).isActive()
                && !HemoCapabilityAccess.getUnstainedProgress(player).map(UnstainedAccessRules::blocksKnownBloodPowerUse).orElse(false)
                && HemoCapabilityAccess.getBloodTendency(player)
                    .map(t -> t.getAlignmentByTendency(manipulation.getTend())>=manipulation.getAlignLevel()).orElse(false);
    }
    private static CastingAnimationPacket create(ServerPlayer player,BloodManipulation manipulation,CastPhase phase,int held) {
        long now=player.level().getGameTime();
        var old=current(player);
        long id=old!=null && old.phase().sustained() && old.manipulation().equals(manipulation.getName())
                ?old.sequence():++sequence;
        var profile=manipulation.getCastPresentation();
        return new CastingAnimationPacket(player.getId(),player.getUUID(),manipulation.getName(),id,
                manipulation.getTend(),manipulation.getSecondaryTend(),manipulation.getRank(),profile.purpose(),profile.style(),
                phase,now,now,held,manipulation.getRequiredChargeTicks(),InteractionHand.MAIN_HAND);
    }
    private static void publish(ServerPlayer player,CastingAnimationPacket packet) {
        ACTIVE.put(player.getUUID(),packet);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,packet);
    }
    private static void finish(ServerPlayer player,CastingAnimationPacket old,CastPhase phase) {
        long now=player.level().getGameTime();
        publish(player,old.at(phase,now,now,old.held()));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        var packet=current(player);
        if(packet==null)return;
        long now=player.level().getGameTime();
        if(!player.isAlive()) { ACTIVE.remove(player.getUUID()); return; }
        if(!packet.phase().sustained()) {
            if(now-packet.started()>=20) ACTIVE.remove(player.getUUID());
            return;
        }
        var manipulation=ManipulationInit.getByName(packet.manipulation());
        if(manipulation==null || !canPresent(player,manipulation)) { finish(player,packet,CastPhase.CANCEL); return; }
        if(manipulation.getType()==EnumManipulationType.CHARGED) {
            if(now-packet.updated()>12)finish(player,packet,CastPhase.CANCEL);
        } else if(!ManipulationChannelManager.isChanneling(player.getUUID())) finish(player,packet,CastPhase.CANCEL);
        else if(now-packet.updated()>=20)publish(player,packet.at(CastPhase.HOLD,packet.started(),now,0));
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if(event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer caster) {
            var packet=current(caster);
            // Late observers enter the loop at its age, never replay an already released cast.
            if(packet!=null && packet.phase().sustained())PacketDistributor.sendToPlayer(observer,packet);
        }
    }
    private static void clear(PlayerEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            var packet=current(player);
            if(packet!=null)finish(player,packet,CastPhase.CANCEL);
            ACTIVE.remove(player.getUUID());
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) { clear(e); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e) { clear(e); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { clear(e); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e) { ACTIVE.clear(); sequence=0; }
}
