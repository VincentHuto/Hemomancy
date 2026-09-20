package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import java.util.*;
import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;

public final class AntecedentPlayback {
    private final Map<UUID,AntecedentObservation> witnesses = new HashMap<>();
    private final Map<UUID,AntecedentObservation> archiveWitnesses=new HashMap<>(), housingWitnesses=new HashMap<>();
    private final Map<UUID,Integer> listeners = new HashMap<>();
    private final Map<UUID,AntecedentObservation> controlledWitnesses=new HashMap<>();
    public void clear() { witnesses.clear(); listeners.clear(); archiveWitnesses.clear(); housingWitnesses.clear(); controlledWitnesses.clear(); }
    public static boolean analyzed(ItemStack sample) { return AhaematicSample.readable(sample) && BloodSampleData.identified(sample); }
    public void tick(ClairaudiographBlockEntity machine) {
        if (!(machine.getLevel() instanceof ServerLevel level) || machine.program()!=AncientRecordings.SEVERED) return;
        int elapsed=machine.elapsed();
        var center=machine.getBlockPos().getCenter();
        var present=new HashSet<UUID>();
        boolean alchemist=!level.getEntitiesOfClass(HarbingerAlchemistEntity.class,new AABB(machine.getBlockPos()).inflate(8),npc->npc.isAlive() && npc.distanceToSqr(center)<=64).isEmpty();
        for (var player:level.players()) {
            if(player.isSpectator() || !player.isAlive() || player.distanceToSqr(center)>64) continue;
            var id=player.getUUID(); present.add(id);
            if(elapsed>=860 && elapsed<1100) listeners.merge(id,1,Integer::sum);
            if(elapsed==1100 && listeners.getOrDefault(id,0)>=240) AntecedentKnowledge.record(player,SEVERED_RECORD_HEARD);
            boolean sample=analyzed(machine.inventory.getStackInSlot(0)) || analyzed(player.getMainHandItem()) || analyzed(player.getOffhandItem());
            if(elapsed==1260 && sample)
                com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(player,new com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket(6,machine.getBlockPos()));
            if(machine.program().unresolved(elapsed) && elapsed%10==0 && sample)
                com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(player,new com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket(2,machine.getBlockPos()));
            boolean loaded=java.util.stream.Stream.of(player.getMainHandItem(),player.getOffhandItem()).anyMatch(stack->stack.is(com.vincenthuto.hemomancy.common.init.ItemInit.listening_scar.get()) && ListeningScarItem.state(stack)==1);
            if(housingWitnesses.computeIfAbsent(id,ignored->new AntecedentObservation()).tick(elapsed,loaded)) ListeningScarItem.awaken(player);
            boolean archive=false;
            for(var site:VigilSites.loaded(level)) if(site.getBlockPos().distSqr(machine.getBlockPos())<=64 && player.distanceToSqr(site.getBlockPos().getCenter())<=64) {
                archive=true;
                if(machine.program().unresolved(elapsed)) {
                    site.signal(elapsed);
                    if(elapsed%10==0) com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(player,new com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket(5,site.getBlockPos()));
                }
            }
            if(archiveWitnesses.computeIfAbsent(id,ignored->new AntecedentObservation()).tick(elapsed,archive)) AntecedentKnowledge.record(player,ARCHIVE_RESPONSE);
            if(elapsed==1440) com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(player,new com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket(3,net.minecraft.core.BlockPos.containing(player.position().subtract(player.getLookAngle().scale(2)))));

            if(witnesses.computeIfAbsent(id,ignored->new AntecedentObservation()).tick(elapsed,sample)) AntecedentKnowledge.record(player,SAMPLE_RESPONSE);
            if(controlledWitnesses.computeIfAbsent(id,ignored->new AntecedentObservation()).tick(elapsed,sample && alchemist && HemoCapabilityAccess.antecedent(player).has(REPLAY_REQUESTED)))
                AntecedentKnowledge.record(player,CONTROLLED_REPLAY);
        }
        listeners.keySet().retainAll(present); witnesses.keySet().retainAll(present); archiveWitnesses.keySet().retainAll(present); housingWitnesses.keySet().retainAll(present); controlledWitnesses.keySet().retainAll(present);
    }
}
