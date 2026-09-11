package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.network.particle.NerveLinkPacket;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** Remaining lifetimes, counts and moving endpoints for observers entering an ongoing cast. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class FerricDuctilisStatusVisuals {
    private record Key(UUID target, ManipulationVisuals.Form form) {}
    private record Status(Entity entity, ServerLevel level, ManipulationVisuals.Form form, double radius, int count, long until) {}
    private record LinkKey(UUID source, UUID target) {}
    private record Link(LivingEntity source, LivingEntity target, ServerLevel level, long until) {}
    private static final Map<Key,Status> STATES=new HashMap<>();
    private static final Map<LinkKey,Link> LINKS=new HashMap<>();
    private FerricDuctilisStatusVisuals() {}

    static void track(Entity entity, ManipulationVisuals.Form form, double radius, int ticks, int count) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (switch(form) {case MARK, RETORT, PARALYSIS, CIRCUIT, WARD, CHOIR, IRON_HEART, MENDING, FERRIC_CONJURE -> false; default -> true;}) return;
        var key=new Key(entity.getUUID(),form);
        if(ticks<=0 || count<=0) STATES.remove(key);
        else STATES.put(key,new Status(entity,level,form,radius,count,level.getGameTime()+ticks));
    }

    public static void link(LivingEntity source, LivingEntity target, int ticks) {
        if(!(source.level() instanceof ServerLevel level))return;
        var link=new Link(source,target,level,level.getGameTime()+ticks);
        LINKS.put(new LinkKey(source.getUUID(),target.getUUID()),link);
        sendLink(link,ticks);
    }

    private static NerveLinkPacket packet(Link link,int ticks) {
        return new NerveLinkPacket(new TendrilAnchor.Entity(link.source.getId(),TendrilAnchor.AnchorPoint.CENTER,Vec3.ZERO),
                new TendrilAnchor.Entity(link.target.getId(),TendrilAnchor.AnchorPoint.CENTER,Vec3.ZERO),ticks);
    }
    private static void sendLink(Link link,int ticks) {
        var center=link.source.position().lerp(link.target.position(),.5);
        PacketDistributor.sendToPlayersNear(link.level,null,center.x,center.y,center.z,
                64+link.source.distanceTo(link.target)*.5,packet(link,ticks));
    }
    private static ManipulationVisualPacket packet(Status state,int ticks) {
        return new ManipulationVisualPacket(state.form,state.entity.getId(),state.entity.position(),state.entity.position(),
                (float)state.radius,ticks,ticks<=0?0:state.count);
    }

    @SubscribeEvent public static void added(MobEffectEvent.Added event) {
        var effect=event.getEffectInstance();
        var form=effect.getEffect().value()==EffectInit.paralysis.get()?ManipulationVisuals.Form.PARALYSIS:
                effect.getEffect().value()==EffectInit.conductive_mark.get()?ManipulationVisuals.Form.MARK:
                effect.getEffect().value()==EffectInit.iron_retort.get()?ManipulationVisuals.Form.RETORT:null;
        if(form!=null)ManipulationVisuals.attached(event.getEntity(),form,1,effect.getDuration(),1);
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if(!(event.getEntity() instanceof ServerPlayer observer))return;
        // Effects may have been restored from entity NBT before this observer existed.
        if(event.getTarget() instanceof LivingEntity living) {
            for(var effect:living.getActiveEffects()) {
                var form=effect.getEffect().value()==EffectInit.paralysis.get()?ManipulationVisuals.Form.PARALYSIS:
                        effect.getEffect().value()==EffectInit.conductive_mark.get()?ManipulationVisuals.Form.MARK:
                        effect.getEffect().value()==EffectInit.iron_retort.get()?ManipulationVisuals.Form.RETORT:null;
                if(form!=null)PacketHandler.sendToPlayer(observer,new ManipulationVisualPacket(form,living.getId(),living.position(),living.position(),1,effect.getDuration(),1));
            }
        }
        for(var state:STATES.values())if(state.entity==event.getTarget()) {
            int remaining=(int)(state.until-state.level.getGameTime());
            if(remaining>0)PacketHandler.sendToPlayer(observer,packet(state,remaining));
        }
        for(var link:LINKS.values())if(link.source==event.getTarget() || link.target==event.getTarget()) {
            int remaining=(int)(link.until-link.level.getGameTime());
            if(remaining>0)PacketHandler.sendToPlayer(observer,packet(link,remaining));
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        var states=STATES.values().iterator();
        while(states.hasNext()) {
            var state=states.next();
            if(!state.entity.isAlive() || state.entity.level()!=state.level || state.level.getGameTime()>=state.until) {
                states.remove();
                var at=state.entity.position();
                PacketDistributor.sendToPlayersNear(state.level,null,at.x,at.y,at.z,80,packet(state,0));
            }
        }
        var links=LINKS.values().iterator();
        while(links.hasNext()) {
            var link=links.next();
            if(!link.source.isAlive() || !link.target.isAlive() || link.source.level()!=link.level || link.target.level()!=link.level
                    || link.level.getGameTime()>=link.until) { links.remove(); sendLink(link,0); }
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) {STATES.clear();LINKS.clear();}
}
