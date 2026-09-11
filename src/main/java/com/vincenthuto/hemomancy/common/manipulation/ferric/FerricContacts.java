package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.*;

/** Five adjacent blades share one ten-tick contact interval without writing victim save data. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class FerricContacts {
    private record Hit(UUID owner,UUID victim) {}
    private static final Map<ServerLevel,Map<Hit,Long>> HITS=new WeakHashMap<>();
    private FerricContacts() {}
    public static boolean claim(ServerLevel level,Player owner,LivingEntity victim) {
        var hits=HITS.computeIfAbsent(level,key->new HashMap<>());
        var key=new Hit(owner.getUUID(),victim.getUUID());
        long now=level.getGameTime();
        if(!FerricConstructShapes.canContact(now,hits.getOrDefault(key,Long.MIN_VALUE)))return false;
        hits.put(key,now);return true;
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if(event.getLevel() instanceof ServerLevel level) {
            var hits=HITS.get(level);
            if(hits!=null) {
                hits.values().removeIf(last->level.getGameTime()-last>=10);
                if(hits.isEmpty())HITS.remove(level);
            }
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){HITS.clear();}
}
