package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;

/**
 * What the client shows for one player's cast: the server-clock playback sampled through the track, with the displayed
 * strain (eased drops, latched through a charged release) and phase crossfades. Pure; time is game ticks.
 */
public final class CastingDisplay {
    public final CastingPlayback playback;
    private CastingTrack track;
    private AimLayer aim;
    private boolean passive;
    private final StrainDisplay strain = new StrainDisplay();
    private final PhaseCrossfade<CastingPose> third = new PhaseCrossfade<>(CastingPose::blend);
    private final PhaseCrossfade<HandPose> first = new PhaseCrossfade<>(HandPose::blend);
    private final PhaseCrossfade<Float> fade = new PhaseCrossfade<>((a,b,t) -> a+(b-a)*t);

    public CastingDisplay(CastingPlayback playback, CastingTrack track, AimLayer aim, boolean passive) {
        this.playback = playback;
        track(track, aim, passive);
    }
    /** Swaps the track (the presentation changed, see CastingTracks.presentationChanged); the playback takes the new phase lengths. */
    public void track(CastingTrack track, AimLayer aim, boolean passive) {
        this.track = track; this.aim = aim; this.passive = passive;
        playback.durations(duration(CastPhase.OPENING), duration(CastPhase.RELEASE), duration(CastPhase.RECOVERY), duration(CastPhase.CANCEL));
    }
    public CastingTrack track() { return track; }
    public boolean update(long sequence, CastPhase phase, long started, long updated, int held, int required) {
        boolean same = sequence == playback.sequence();
        CastPhase previous = playback.packetPhase();
        if (!playback.update(sequence, phase, started, updated, held, required)) return false;
        if (!same) strain.reset();
        else if (StrainDisplay.latches(true, previous, phase)) strain.latch();
        else if (phase.sustained()) strain.unlatch();
        return true;
    }
    public float strain(long now, float partial) { return strain.sample(playback.strain(now, partial), now + partial); }
    public CastingPose pose(long now, float partial) {
        CastPhase phase = playback.phase(now);
        return third.apply(playback.sequence(), phase, now + partial,
                CastingTrackRules.compose(track, aim, phase, playback.phaseTime(now, partial), strain(now, partial), passive));
    }
    public HandPose handPose(long now, float partial) {
        CastPhase phase = playback.phase(now);
        return first.apply(playback.sequence(), phase, now + partial,
                CastingTrackRules.composeFirst(track, aim, phase, playback.phaseTime(now, partial), strain(now, partial), passive));
    }
    public float weight(long now, float partial) {
        CastPhase phase = playback.phase(now);
        return fade.apply(playback.sequence(), phase, now + partial, CastingTrackRules.weight(phase, playback.phaseTime(now, partial),
                duration(CastPhase.RECOVERY), duration(CastPhase.CANCEL)));
    }
    private int duration(CastPhase phase) { return CastingTrackRules.phaseDuration(track, phase, passive); }
}
