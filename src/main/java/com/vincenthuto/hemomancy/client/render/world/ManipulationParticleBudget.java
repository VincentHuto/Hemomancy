package com.vincenthuto.hemomancy.client.render.world;

import java.util.HashMap;
import java.util.Map;

final class ManipulationParticleBudget {
    private long tick=Long.MIN_VALUE;
    private int used;
    private final Map<Integer,Long> hits=new HashMap<>();

    int take(long now,int requested,int setting) {
        if(tick!=now) {
            tick=now;used=0;
            hits.values().removeIf(until->until<=now);
        }
        int count=setting==2?0:setting==1?(requested+1)/2:requested;
        count=Math.max(0,Math.min(count,48-used));
        used+=count;
        return count;
    }

    boolean hit(int entityId,long now) {
        if(hits.getOrDefault(entityId,Long.MIN_VALUE)>now)return false;
        hits.put(entityId,now+5);
        return true;
    }

    void clear() {tick=Long.MIN_VALUE;used=0;hits.clear();}
}
