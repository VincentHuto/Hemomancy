package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.*;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;

@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class AntecedentEvents {
    private static final Map<net.minecraft.server.level.ServerPlayer,Boolean> reading=new WeakHashMap<>();
    private AntecedentEvents() {}
    @SubscribeEvent public static void vibration(VanillaGameEvent event) {
        if(!(event.getLevel() instanceof ServerLevel level) || !event.getVanillaEvent().is(GameEventTags.VIBRATIONS)) return;
        var cause=event.getCause();
        if(cause!=null && (cause.isSpectator() || cause.dampensVibrations() || cause.isSteppingCarefully() && event.getVanillaEvent().is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING))) return;
        for(var site:VigilSites.loaded(level)) site.disturbance(event.getEventPosition());
        for(var player:level.players()) if(player.distanceToSqr(event.getEventPosition())<=64
                && (AntecedentPlayback.analyzed(player.getMainHandItem()) || AntecedentPlayback.analyzed(player.getOffhandItem()))) {
            AntecedentKnowledge.record(player,ACOUSTIC_RESPONSE);
            PacketHandler.sendToPlayer(player,new AntecedentEffectPacket(0,BlockPos.containing(event.getEventPosition())));
        }
    }
    public static void accepted(ServerLevel level,BlockPos pos) {
        long now=level.getGameTime();
        for(var player:level.players()) {
            if(player.distanceToSqr(pos.getCenter())>256 || !ListeningScarItem.awake(new TalismanSocket(player).getStackInSlot(0))) continue;
            var data=player.getPersistentData();
            if(data.getLong("hemomancy:scar_pulse_until")>now) continue;
            data.putLong("hemomancy:scar_pulse_until",now+5);
            PacketHandler.sendToPlayer(player,new AntecedentEffectPacket(1,pos));
        }
    }
    @SubscribeEvent public static void record(PlayerInteractEvent.RightClickBlock event) {
        if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        for(var site:VigilSites.loaded(player.level())) {
            if(site.world(site.layout().record()).equals(event.getPos()) && player.level().getBlockEntity(event.getPos()) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern && !lectern.getBook().isEmpty())
                reading.put(player,false);
        }
    }
    @SubscribeEvent public static void recordClosed(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) || !reading.containsKey(player))return;
        if(player.containerMenu instanceof net.minecraft.world.inventory.LecternMenu)reading.put(player,true);
        else if(player.containerMenu==player.inventoryMenu) {
            if(reading.remove(player))AntecedentKnowledge.record(player,VIGIL_RECORD_READ);
        }
    }
    @SubscribeEvent public static void epilogue(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) || player.tickCount%200!=0 || player.containerMenu!=player.inventoryMenu) return;
        var research=HemoCapabilityAccess.antecedent(player);
        if(!research.complete() || research.has(EPILOGUE) || HemoCapabilityAccess.getPlayerDegreeNumber(player)<7
                || !HemoCapabilityAccess.getBloodVolume(player).map(volume->volume.isActive()).orElse(false)) return;
        if(player.getLastHurtByMob()!=null && player.tickCount-player.getLastHurtByMobTimestamp()<200) return;
        PacketHandler.sendToPlayer(player,new AntecedentEffectPacket(4,BlockPos.ZERO));
    }
}
