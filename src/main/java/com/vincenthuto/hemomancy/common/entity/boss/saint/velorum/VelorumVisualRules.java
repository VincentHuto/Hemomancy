package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** Independent presentation envelopes; server attack clocks and hit tests remain authoritative. */
public final class VelorumVisualRules {
    public static final int FROST_INTERVAL=100, VEIL_INTERVAL=160, DRAIN_INTERVAL=80;
    public static final int FROST_RELEASE=18, VEIL_RELEASE=22, DRAIN_RELEASE=20, STILL_GUARD=40;
    public static final int FROST_WARNING=24, VEIL_WARNING=20, DRAIN_WARNING=16;
    public static final int FROST=1, VEIL=2, DRAIN=3, GUARD_CAST=4, REST=5, MELEE=6;
    public static final int STILLNESS=7, STILLNESS_BREAK=8;
    public static final int BURST_TICKS=32, VEIL_TICKS=60, REST_TICKS=40;
    public record Phase(float frostUntil,float frostRelease,float veilUntil,float veilRelease,
                        float drainUntil,float drainRelease,float stillGuard) {}
    public record Pose(float gather,float frost,float veil,float silence,float stillGuard) {}
    private static final BossMoveTimeline FROST_MOTION=new BossMoveTimeline(8,16,FROST_RELEASE);
    private VelorumVisualRules() {}
    public static BossMoveTimeline frost() { return FROST_MOTION; }
    public static float warning(float until,int duration) {
        return until>0 && until<=duration?HemoEasing.smoothstep(1-until/duration):0;
    }
    private static float release(float remaining,int duration) {
        return remaining>0?HemoEasing.pulse(Math.max(0,duration-remaining),3,duration):0;
    }
    public static Pose pose(Phase phase) {
        float gather=warning(phase.frostUntil(),frost().windup()+frost().active());
        if(phase.frostRelease()>0)gather=1-HemoEasing.smoothstep((FROST_RELEASE-phase.frostRelease())/6);
        float veil=warning(phase.veilUntil(),VEIL_WARNING)*.45F+release(phase.veilRelease(),VEIL_RELEASE);
        float drain=warning(phase.drainUntil(),DRAIN_WARNING)*.4F+release(phase.drainRelease(),DRAIN_RELEASE);
        float stillGuard=phase.stillGuard()>0?HemoEasing.smoothstep((STILL_GUARD-phase.stillGuard())/6)*HemoEasing.smoothstep(phase.stillGuard()/10):0;
        return new Pose(gather,release(phase.frostRelease(),FROST_RELEASE),veil,drain,stillGuard);
    }
    public static Phase phase(int visual,boolean stillGuard,float remaining) {
        return new Phase(100,visual==1?remaining:0,160,visual==2?remaining:0,80,visual==3?remaining:0,stillGuard?24:0);
    }
    public static float fade(float age,int life) {
        return age<0 || age>=life?0:1-HemoEasing.smoothstep((age-3)/(life-3));
    }
}
