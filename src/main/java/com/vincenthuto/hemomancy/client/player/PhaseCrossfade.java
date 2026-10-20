package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.player.CastingTrack.Easing;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;

/**
 * Fades from the last shown value into a new phase or cast over {@link #TICKS}, so cancels from the opening, releases
 * from strain and casts replacing a recovery never pop. One instance per sampled value; time is game ticks plus partial.
 */
public final class PhaseCrossfade<P> {
    public static final int TICKS = 3;
    /** A value last shown longer ago than this was off screen when the phase changed; it is not faded from. */
    private static final float STALE = 2;
    public interface Mix<P> { P mix(P from, P to, float t); }
    private final Mix<P> mix;
    private long sequence;
    private CastPhase phase;
    private float changed, last;
    private P from, shown;

    public PhaseCrossfade(Mix<P> mix) { this.mix = mix; }
    public static float amount(float ticks, int duration) {
        return duration <= 0 ? 1 : CastingTrackRules.ease(Easing.SMOOTH, ticks/duration);
    }
    public static <P extends Blendable<P>> P blend(P previous, P next, float ticks, int duration) {
        return previous.blend(next, amount(ticks, duration));
    }
    public P apply(long sequence, CastPhase phase, float time, P target) {
        if (phase != this.phase || sequence != this.sequence) {
            from = shown != null && time - last <= STALE ? shown : null;
            changed = time; this.phase = phase; this.sequence = sequence;
        }
        float t = from == null ? 1 : amount(time - changed, TICKS);
        if (t >= 1) from = null;
        last = time;
        return shown = from == null ? target : mix.mix(from, target, t);
    }
}
