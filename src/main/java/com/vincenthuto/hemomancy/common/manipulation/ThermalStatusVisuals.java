package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Ephemeral cosmetic state. No damage, effects, persistence or authority is derived from it. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class ThermalStatusVisuals {
    private record Key(UUID id,Form form) {}
    private record Status(Entity entity,Form form,double radius,int count,long until,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {}
    private static final Map<Key,Status> ACTIVE=new HashMap<>();
    private ThermalStatusVisuals() {}

    static void track(Entity entity,Form form,double radius,int ticks,int count) {
        if(form!=Form.FROZEN_VEINS && form!=Form.RIMEBOUND && form!=Form.BONE && form!=Form.CAUTERIZE)return;
        Key key=new Key(entity.getUUID(),form);
        if(ticks<=0)ACTIVE.remove(key);
        else ACTIVE.put(key,new Status(entity,form,radius,count,entity.level().getGameTime()+ticks,entity.level().dimension()));
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        var iterator=ACTIVE.entrySet().iterator();
        while(iterator.hasNext()) {
            Status state=iterator.next().getValue();
            Entity entity=state.entity;
            boolean thawed=entity instanceof LivingEntity living && switch(state.form) {
                case FROZEN_VEINS,RIMEBOUND -> living.getTicksFrozen()<=0;
                case BONE -> !living.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
                default -> false;
            };
            if(!entity.isAlive() || entity.level().getGameTime()>=state.until || thawed
                    || !entity.level().dimension().equals(state.dimension)) {
                iterator.remove();
                if(!entity.level().isClientSide) {
                    var at=entity.position();
                    PacketDistributor.sendToPlayersNear((net.minecraft.server.level.ServerLevel)entity.level(),null,
                            at.x,at.y,at.z,80,new ManipulationVisualPacket(state.form,entity.getId(),at,at,0,0,0));
                }
            }
        }
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if(!(event.getEntity() instanceof ServerPlayer observer))return;
        for(Status state:ACTIVE.values())if(state.entity==event.getTarget()) {
            int ticks=(int)Math.max(0,state.until-state.entity.level().getGameTime());
            if(ticks>0)PacketDistributor.sendToPlayer(observer,new ManipulationVisualPacket(state.form,state.entity.getId(),
                    state.entity.position(),state.entity.position(),(float)state.radius,ticks,state.count));
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) {ACTIVE.clear();}
}
