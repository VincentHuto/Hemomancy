package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;

/** Server-clock playback. Heartbeats change charge, not the age of the choreography; the track supplies phase lengths. */
public final class CastingPlayback {
    private long sequence, started, updated;
    private CastPhase phase;
    private int held, required;
    private int opening, release, recovery, cancel;

    public CastingPlayback(long sequence, CastPhase phase, long started, long updated, int held, int required) {
        this(sequence, phase, started, updated, held, required, 6, 6, 14, 10);
    }

    public CastingPlayback(long sequence, CastPhase phase, long started, long updated, int held, int required,
                           int opening, int release, int recovery, int cancel) {
        this.sequence = sequence;
        this.phase = phase;
        this.started = started;
        this.updated = updated;
        this.held = held;
        this.required = required;
        durations(opening, release, recovery, cancel);
    }

    public void durations(int opening, int release, int recovery, int cancel) {
        this.opening = opening; this.release = release; this.recovery = recovery; this.cancel = cancel;
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
    /** Strain only follows a real charge; quick and continuous casts carry no requirement and release from the hold. */
    public float strain(long now, float partial) { return required <= 0 ? 0 : charge(now, partial); }
    public CastPhase phase(long now) {
        // The server flips to HOLD after 6 ticks; the track's opening still plays in full.
        if (phase.sustained()) return now - started < opening ? CastPhase.OPENING : CastPhase.HOLD;
        if (phase == CastPhase.RELEASE && now - started >= release) return CastPhase.RECOVERY;
        return phase;
    }
    /** Time into the shown phase: hold and recovery start their own clocks where the opening and release end. */
    public float phaseTime(long now, float partial) {
        return switch (phase(now)) {
            case HOLD -> Math.max(0, elapsed(now, partial) - opening);
            case RECOVERY -> elapsed(now, partial) - (phase == CastPhase.RELEASE ? release : 0);
            default -> elapsed(now, partial);
        };
    }
    public boolean expired(long now) {
        return phase.sustained() ? now - updated > 40 : now - started >= (phase == CastPhase.CANCEL ? cancel : release + recovery);
    }
    public long sequence() { return sequence; }
    /** The phase the last packet carried, before track timing splits it. */
    public CastPhase packetPhase() { return phase; }
}
