package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.player.CastingTrack.Easing;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Segment;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;

/** Samples authored tracks by phase. Pure maths: charge drives strain, loops ride the hold, passives use on/off. */
public final class CastingTrackRules {
    private CastingTrackRules() {}

    public static float ease(Easing easing, float value) {
        float t = Math.clamp(value, 0, 1);
        return switch (easing) {
            case LINEAR -> t;
            case SMOOTH -> t*t*(3-2*t);
            case EASE_OUT -> 1-(1-t)*(1-t);
            case HOLD -> t >= 1 ? 1 : 0;
        };
    }

    public static CastingPose sample(CastingTrack track, CastPhase phase, float time, float charge, boolean passive) {
        return sample(track.opening(), track.hold(), track.strain(), track.release(), track.recovery(), track.cancel(),
                track.on(), track.off(), track.loop() == null ? null : track.loop().amplitude(),
                track.loop() == null ? 0 : track.loop().period(), phase, time, charge, passive, CastingPose.ZERO);
    }

    public static HandPose sampleFirst(CastingTrack track, CastPhase phase, float time, float charge, boolean passive) {
        return sample(track.firstOpening(), track.firstHold(), track.firstStrain(), track.firstRelease(), track.firstRecovery(),
                track.firstCancel(), track.firstOn(), track.firstOff(), track.loop() == null ? null : track.loop().firstAmplitude(),
                track.loop() == null ? 0 : track.loop().period(), phase, time, charge, passive, HandPose.ZERO);
    }

    /** Track sample with the aim layer applied; aim may be null. */
    public static CastingPose compose(CastingTrack track, AimLayer aim, CastPhase phase, float time, float charge, boolean passive) {
        var pose = sample(track, phase, time, charge, passive);
        float w = aimWeight(track, aim, phase, time, passive);
        return w <= 0 ? pose : AimLayerRules.apply(pose, aim.third().sample(aimTime(phase, time), CastingPose.ZERO), w);
    }
    public static HandPose composeFirst(CastingTrack track, AimLayer aim, CastPhase phase, float time, float charge, boolean passive) {
        var pose = sampleFirst(track, phase, time, charge, passive);
        float w = aimWeight(track, aim, phase, time, passive);
        return w <= 0 ? pose : AimLayerRules.applyFirst(pose, aim.first().sample(aimTime(phase, time), HandPose.ZERO), w);
    }
    private static float aimWeight(CastingTrack track, AimLayer aim, CastPhase phase, float time, boolean passive) {
        return aim == null || passive ? 0 : AimLayerRules.weight(phase, time,
                phaseDuration(track, CastPhase.RELEASE, false), phaseDuration(track, CastPhase.RECOVERY, false));
    }
    /** Recovery keeps the ending's final frame while the aim weight fades. */
    private static float aimTime(CastPhase phase, float time) { return phase == CastPhase.RELEASE ? time : Float.MAX_VALUE; }

    public static int phaseDuration(CastingTrack track, CastPhase phase, boolean passive) {
        return switch (phase) {
            case OPENING -> track.opening().duration();
            case HOLD -> 0;
            case RELEASE -> passive && track.on() != null ? track.on().duration() : track.release().duration();
            case RECOVERY -> passive && track.on() != null ? 0 : track.recovery().duration();
            case CANCEL -> passive && track.off() != null ? track.off().duration() : track.cancel().duration();
        };
    }

    /** Ticks at the end of recovery and cancel over which the residual blend fades back to vanilla. */
    public static final int FADE = 4;
    /**
     * Blend weight against vanilla: ramps in over the opening's first ticks, then stays full so authored recovery and
     * passive off beats play at their real amplitude, fading only across the last {@link #FADE} ticks.
     */
    public static float weight(CastPhase phase, float time, int recovery, int cancel) {
        return switch (phase) {
            case OPENING -> Math.clamp((time+1)/3, 0, 1);
            case RECOVERY -> fade(time, recovery);
            case CANCEL -> fade(time, cancel);
            default -> 1;
        };
    }
    private static float fade(float time, int duration) {
        return duration <= 0 ? 0 : Math.clamp((duration-time)/Math.min(FADE, duration), 0, 1);
    }

    private static <P extends Blendable<P>> P sample(Segment<P> opening, Segment<P> hold, Segment<P> strain, Segment<P> release,
            Segment<P> recovery, Segment<P> cancel, Segment<P> on, Segment<P> off, P amplitude, int period,
            CastPhase phase, float time, float charge, boolean passive, P zero) {
        boolean gesture = passive && on != null && off != null;
        float c = Math.clamp(charge, 0, 1);
        return switch (phase) {
            case OPENING -> opening.sample(time, zero);
            case HOLD -> {
                P pose = hold.sample(time, zero);
                if (strain != null) pose = pose.blend(strain.sample(time, zero), c);
                // HOLD time starts where the opening ends (CastingPlayback.phaseTime), so the loop starts from rest.
                if (amplitude != null && period > 0) pose = pose.offset(amplitude, (float)Math.sin(Math.PI*2*time/period));
                yield pose;
            }
            case RELEASE -> {
                if (gesture) yield on.sample(time, zero);
                P pose = release.sample(time, zero);
                if (strain != null && c > 0) {
                    // Release leaves from the strained pose and converges on the authored keyframes.
                    P delta = strain.sample(0, zero).offset(hold.sample(0, zero), -1);
                    float remaining = release.duration() <= 0 ? 0 : 1 - Math.clamp(time/release.duration(), 0, 1);
                    pose = pose.offset(delta, c*remaining);
                }
                yield pose;
            }
            case RECOVERY -> gesture ? zero : recovery.sample(time, zero);
            case CANCEL -> gesture ? off.sample(time, zero) : cancel.sample(time, zero);
        };
    }
}
