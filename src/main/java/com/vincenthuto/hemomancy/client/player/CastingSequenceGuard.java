package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import java.util.LinkedHashMap;
import java.util.UUID;

/** Keep terminal ordering after a pose expires, without keeping player entities or unbounded history. */
public final class CastingSequenceGuard {
    private record Entry(CastingPlayback playback, long seen, CastPhase phase, long updated) {}
    private final LinkedHashMap<UUID,Entry> entries=new LinkedHashMap<>();
    public boolean accept(UUID player,long sequence,CastPhase phase,long started,long updated,int held,int required,long now) {
        entries.entrySet().removeIf(e -> now-e.getValue().seen()>1200);
        var old=entries.get(player);
        if(old!=null) {
            if(!phase.sustained() && old.playback().sequence()==sequence && old.phase()==phase && updated<=old.updated())return false;
            if(!old.playback().update(sequence,phase,started,updated,held,required))return false;
        }
        var playback=old==null?new CastingPlayback(sequence,phase,started,updated,held,required):old.playback();
        entries.remove(player);
        entries.put(player,new Entry(playback,now,phase,updated));
        if(entries.size()>1024)entries.pollFirstEntry();
        return true;
    }
    public void clear() { entries.clear(); }
}
