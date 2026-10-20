package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;

/**
 * The strain the client shows. Rising charge is followed at once; a drop (damage) eases back over {@link #DROP_TICKS}
 * whatever its size; a charged release keeps the strain it left from, because the server's RELEASE carries no charge.
 * Pure: time is game ticks plus partial.
 */
public final class StrainDisplay {
    public static final float DROP_TICKS = 4;
    private float shown, from, dropStart = Float.NaN;
    private boolean latched;

    /** A sustained cast ending in the same sequence keeps the strain it showed. */
    public static boolean latches(boolean sameSequence, CastPhase previous, CastPhase next) {
        return sameSequence && previous.sustained() && !next.sustained();
    }
    public float sample(float target, float time) {
        if (latched) return shown;
        if (target >= shown) { shown = target; dropStart = Float.NaN; return shown; }
        if (Float.isNaN(dropStart)) { from = shown; dropStart = time; }
        float t = Math.clamp((time - dropStart)/DROP_TICKS, 0, 1);
        if (t >= 1) dropStart = Float.NaN;
        return shown = t >= 1 ? target : from + (target - from)*t;
    }
    public void latch() { latched = true; }
    public void unlatch() { latched = false; }
    /** A new cast starts unstrained. */
    public void reset() { shown = 0; latched = false; dropStart = Float.NaN; }
}
