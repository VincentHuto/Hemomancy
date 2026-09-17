package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;

/** Server-clock playback. Heartbeats change charge, not the age of the choreography. */
public final class CastingPlayback {
    private long sequence, started, updated;
    private CastPhase phase;
    private int held, required;

    public CastingPlayback(long sequence, CastPhase phase, long started, long updated, int held, int required) {
        this.sequence = sequence;
        this.phase = phase;
        this.started = started;
        this.updated = updated;
        this.held = held;
        this.required = required;
    }

    public boolean update(long nextSequence, CastPhase next, long start, long at, int ticks, int duration) {
        if (nextSequence < sequence || (nextSequence == sequence && (at < updated
                || !phase.sustained() && next.sustained()))) return false;
        sequence = nextSequence;
        phase = next;
        started = start;
        updated = at;
        held = ticks;
        required = duration;
        return true;
    }

    public float elapsed(long now, float partial) { return Math.max(0, now - started + partial); }
    public float charge(long now, float partial) {
        return required <= 0 ? 1 : Math.clamp((held + (phase.sustained() ? now - updated + partial : 0)) / required, 0, 1);
    }
    public CastPhase phase(long now) {
        if (phase == CastPhase.OPENING && now - started >= 6) return CastPhase.HOLD;
        if (phase == CastPhase.RELEASE && now - started >= 6) return CastPhase.RECOVERY;
        return phase;
    }
    public float phaseTime(long now, float partial) {
        return elapsed(now, partial) - (phase == CastPhase.RELEASE && phase(now) == CastPhase.RECOVERY ? 6 : 0);
    }
    public boolean expired(long now) {
        return phase.sustained() ? now - updated > 40 : now - started >= (phase == CastPhase.CANCEL ? 10 : 20);
    }
    public long sequence() { return sequence; }
}
