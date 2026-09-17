package com.vincenthuto.hemomancy.client.player;

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
        if(level!=current) { ACTIVE.clear(); ORDER.clear(); preview=null; level=current; }
    }
    public static void accept(CastingAnimationPacket packet) {
        levelCheck();
        if(level==null)return;
        if(!ORDER.accept(packet.playerId(),packet.sequence(),packet.phase(),packet.started(),packet.updated(),
                packet.held(),packet.required(),level.getGameTime()))return;
        var old=ACTIVE.get(packet.playerId());
        if(old==null) ACTIVE.put(packet.playerId(),new Animation(packet));
        else if(old.playback.update(packet.sequence(),packet.phase(),packet.started(),packet.updated(),packet.held(),packet.required()))
            old.packet=packet;
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
        previewUntil=level==null?0:level.getGameTime()+160;
    }
    public static final class Animation {
        private CastingAnimationPacket packet;
        private boolean seenEntity;
        public final CastingPlayback playback;
        Animation(CastingAnimationPacket p) {
            packet=p;
            playback=new CastingPlayback(p.sequence(),p.phase(),p.started(),p.updated(),p.held(),p.required());
        }
        public CastingAnimationPacket packet() { return packet; }
        public long now() { return Minecraft.getInstance().level.getGameTime(); }
        public CastingPose pose(float partial,boolean firstPerson) {
            var phase=playback.phase(now());
            float t=playback.phaseTime(now(),partial);
            var clip=CastingClips.get(packet.school(),packet.rank());
            var pose=firstPerson?clip.firstPerson(phase,t):clip.sample(phase,t);
            return CastingPoseModifiers.apply(pose,packet.purpose(),packet.style(),phase,t,firstPerson);
        }
        public float weight(float partial) {
            float t=playback.phaseTime(now(),partial);
            return switch(playback.phase(now())) {
                case OPENING -> Math.clamp((t+1)/3,0,1);
                case RECOVERY -> Math.clamp(1-t/14,0,1);
                case CANCEL -> Math.clamp(1-t/10,0,1);
                default -> 1;
            };
        }
    }
}
