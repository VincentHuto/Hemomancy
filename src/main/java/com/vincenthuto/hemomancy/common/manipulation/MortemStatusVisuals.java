package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.server.level.ServerPlayer;
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

/** Bloom provenance lasts for this server session; ordinary wounds reconstruct from real effects. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class MortemStatusVisuals {
    private record Infection(LivingEntity target,long until,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {}
    private static final Map<java.util.UUID,Infection> INFECTIONS=new HashMap<>();
    private MortemStatusVisuals() {}
    public static void infect(LivingEntity target,int ticks) {
        if(target.level().isClientSide)return;
        INFECTIONS.put(target.getUUID(),new Infection(target,target.level().getGameTime()+ticks,target.level().dimension()));
        ManipulationVisuals.attached(target,Form.ROT_INFECTION,1,ticks,1);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        var entries=INFECTIONS.values().iterator();
        while(entries.hasNext()) {
            var infection=entries.next();var target=infection.target;
            if(!target.isAlive()||target.level().getGameTime()>=infection.until||!target.level().dimension().equals(infection.dimension)
                    || !target.hasEffect(EffectInit.necrosis)) {
                entries.remove();ManipulationVisuals.attached(target,Form.ROT_INFECTION,1,0,0);
            }
        }
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if(!(event.getEntity() instanceof ServerPlayer observer)||!(event.getTarget() instanceof LivingEntity target))return;
        var wound=target.getEffect(EffectInit.blood_loss);if(wound!=null)send(observer,target,Form.WOUND,wound.getDuration());
        var hunger=target.getEffect(EffectInit.insatiable_hunger);if(hunger!=null)send(observer,target,Form.HUNGER,hunger.getDuration());
        var grave=target.getEffect(EffectInit.grave_debt);if(grave!=null)send(observer,target,Form.GRAVE,grave.getDuration());
        var infection=INFECTIONS.get(target.getUUID());if(infection!=null)send(observer,target,Form.ROT_INFECTION,(int)(infection.until-target.level().getGameTime()));
    }
    private static void send(ServerPlayer observer,LivingEntity target,Form form,int ticks) {
        if(ticks>0)PacketDistributor.sendToPlayer(observer,new ManipulationVisualPacket(form,target.getId(),target.position(),target.position(),1,ticks,1));
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){INFECTIONS.clear();}
}
