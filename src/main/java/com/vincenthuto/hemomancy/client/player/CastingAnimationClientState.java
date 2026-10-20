package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.player.tracks.AimLayers;
import com.vincenthuto.hemomancy.client.player.tracks.CastingTracks;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringClientState;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastLane;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CastingAnimationClientState {
    private static final Map<UUID,Animation> ACTIVE = new HashMap<>();
    private static final CastingSequenceGuard ORDER = new CastingSequenceGuard();
    private static ClientLevel level;
    private static Animation preview;
    private static long previewUntil;
    private CastingAnimationClientState() {}
    private static void levelCheck() {
        var current=Minecraft.getInstance().level;
        if(level!=current) { ACTIVE.clear(); ORDER.clear(); preview=null; level=current; OutpouringClientState.clear(); }
    }
    public static void accept(CastingAnimationPacket packet) {
        levelCheck();
        if(level==null)return;
        if(!ORDER.accept(packet.playerId(),packet.sequence(),packet.phase(),packet.started(),packet.updated(),
                packet.held(),packet.required(),level.getGameTime()))return;
        OutpouringClientState.accept(packet);
        var old=ACTIVE.get(packet.playerId());
        if(old==null) ACTIVE.put(packet.playerId(),new Animation(packet));
        else if(old.display.update(packet.sequence(),packet.phase(),packet.started(),packet.updated(),packet.held(),packet.required()))
            old.retarget(packet);
    }
    public static void untrack(UUID player) { ACTIVE.remove(player); }
    public static void tick() {
        levelCheck();
        if(level==null)return;
        long now=level.getGameTime();
        ACTIVE.entrySet().removeIf(entry -> {
            var a=entry.getValue();
            var entity=level.getEntity(a.packet.entityId());
            if(entity!=null)a.seenEntity=true;
            return a.playback.expired(now) || a.seenEntity && entity==null || entity!=null
                    && (!entity.isAlive() || !entity.getUUID().equals(entry.getKey()));
        });
        if(preview!=null && now>=previewUntil)preview=null;
    }
    public static Animation animation(Player player) {
        levelCheck();
        if(player==null || level==null || !player.isAlive())return null;
        if(preview!=null && preview.packet.playerId().equals(player.getUUID()))return preview;
        var animation=ACTIVE.get(player.getUUID());
        return animation!=null && !animation.playback.expired(level.getGameTime())?animation:null;
    }
    public static void preview(CastingAnimationPacket packet) {
        levelCheck();
        preview=packet==null?null:new Animation(packet);
        if(packet!=null)OutpouringClientState.accept(packet);
        previewUntil=level==null?0:level.getGameTime()+160;
    }
    static boolean passive(String manipulation) {
        var m=ManipulationInit.getByName(manipulation);
        return m!=null && m.getType()==EnumManipulationType.PASSIVE;
    }
    public static final class Animation {
        private CastingAnimationPacket packet;
        private CastingTrack track;
        private AimLayer aim;
        private boolean passive, seenEntity;
        private final CastingDisplay display;
        public final CastingPlayback playback;
        Animation(CastingAnimationPacket p) {
            packet=p; resolve(p);
            display=new CastingDisplay(new CastingPlayback(p.sequence(),p.phase(),p.started(),p.updated(),p.held(),p.required()),track,aim,passive);
            playback=display.playback;
        }
        private void resolve(CastingAnimationPacket p) {
            track=CastingTracks.resolve(p);
            if(p.lane()==CastLane.UNSTAINED) {
                // Still Arts are quick casts: the stage track carries the body, the ending owns the lead arm and head.
                aim=CastingTracks.ending(p);
                passive=false;
                return;
            }
            // Bespoke tracks author their own lead arm and head; only school bases take the purpose ending.
            aim=p.purpose()==null||CastingTracks.bespoke(p.style())?null:AimLayers.purpose(p.purpose());
            passive=passive(p.manipulation());
        }
        void retarget(CastingAnimationPacket p) {
            boolean changed=CastingTracks.presentationChanged(packet,p);
            packet=p;
            if(changed) { resolve(p); display.track(track,aim,passive); }
        }
        public CastingAnimationPacket packet() { return packet; }
        public CastingTrack track() { return track; }
        public long now() { return Minecraft.getInstance().level.getGameTime(); }
        public CastingPose pose(float partial) { return display.pose(now(),partial); }
        public HandPose handPose(float partial) { return display.handPose(now(),partial); }
        /** Blend against vanilla: full through the authored beats, fading over the last ticks of recovery or cancel. */
        public float weight(float partial) { return display.weight(now(),partial); }
    }
}
