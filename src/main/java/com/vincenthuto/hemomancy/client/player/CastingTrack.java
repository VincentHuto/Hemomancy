package com.vincenthuto.hemomancy.client.player;

import java.util.ArrayList;
import java.util.List;

/** Authored casting choreography: eased keyframe segments for both views. Null segments are absent. */
public record CastingTrack(Segment<CastingPose> opening, Segment<CastingPose> hold, Segment<CastingPose> strain,
                           Segment<CastingPose> release, Segment<CastingPose> recovery, Segment<CastingPose> cancel,
                           Segment<CastingPose> on, Segment<CastingPose> off, Loop loop,
                           Segment<HandPose> firstOpening, Segment<HandPose> firstHold, Segment<HandPose> firstStrain,
                           Segment<HandPose> firstRelease, Segment<HandPose> firstRecovery, Segment<HandPose> firstCancel,
                           Segment<HandPose> firstOn, Segment<HandPose> firstOff) {
    public enum Easing { LINEAR, SMOOTH, EASE_OUT, HOLD }
    public record Keyframe<P extends Blendable<P>>(int tick, P pose, Easing easing) {}
    public record Loop(CastingPose amplitude, HandPose firstAmplitude, int period) {}
    public record Segment<P extends Blendable<P>>(List<Keyframe<P>> frames) {
        public int duration() { return frames.isEmpty() ? 0 : frames.getLast().tick(); }
        public P sample(float time, P zero) {
            if (frames.isEmpty()) return zero;
            var first = frames.getFirst();
            if (time <= first.tick()) return first.pose();
            for (int i = 1; i < frames.size(); i++) {
                var next = frames.get(i);
                if (time <= next.tick()) {
                    var prev = frames.get(i-1);
                    float span = next.tick() - prev.tick();
                    float t = span <= 0 ? 1 : (time - prev.tick()) / span;
                    return prev.pose().blend(next.pose(), CastingTrackRules.ease(next.easing(), t));
                }
            }
            return frames.getLast().pose();
        }
        public P last(P zero) { return frames.isEmpty() ? zero : frames.getLast().pose(); }
        public P first(P zero) { return frames.isEmpty() ? zero : frames.getFirst().pose(); }
    }

    public static final class Builder {
        private Segment<CastingPose> opening, hold, strain, release, recovery, cancel, on, off;
        private Segment<HandPose> firstOpening, firstHold, firstStrain, firstRelease, firstRecovery, firstCancel, firstOn, firstOff;
        private Loop loop;
        @SafeVarargs private static <P extends Blendable<P>> Segment<P> seg(Keyframe<P>... frames) { return new Segment<>(List.of(frames)); }
        private static <P extends Blendable<P>> Segment<P> still(P pose) { return new Segment<>(List.of(new Keyframe<>(0,pose,Easing.LINEAR))); }
        @SafeVarargs public final Builder opening(Keyframe<CastingPose>... f) { opening=seg(f); return this; }
        public Builder hold(CastingPose p) { hold=still(p); return this; }
        public Builder strain(CastingPose p, HandPose h) { strain=still(p); firstStrain=still(h); return this; }
        public Builder loop(CastingPose amplitude, HandPose firstAmplitude, int period) { loop=new Loop(amplitude,firstAmplitude,period); return this; }
        @SafeVarargs public final Builder release(Keyframe<CastingPose>... f) { release=seg(f); return this; }
        @SafeVarargs public final Builder recovery(Keyframe<CastingPose>... f) { recovery=seg(f); return this; }
        @SafeVarargs public final Builder cancel(Keyframe<CastingPose>... f) { cancel=seg(f); return this; }
        @SafeVarargs public final Builder on(Keyframe<CastingPose>... f) { on=seg(f); return this; }
        @SafeVarargs public final Builder off(Keyframe<CastingPose>... f) { off=seg(f); return this; }
        @SafeVarargs public final Builder firstOpening(Keyframe<HandPose>... f) { firstOpening=seg(f); return this; }
        public Builder firstHold(HandPose p) { firstHold=still(p); return this; }
        @SafeVarargs public final Builder firstRelease(Keyframe<HandPose>... f) { firstRelease=seg(f); return this; }
        @SafeVarargs public final Builder firstRecovery(Keyframe<HandPose>... f) { firstRecovery=seg(f); return this; }
        @SafeVarargs public final Builder firstCancel(Keyframe<HandPose>... f) { firstCancel=seg(f); return this; }
        @SafeVarargs public final Builder firstOn(Keyframe<HandPose>... f) { firstOn=seg(f); return this; }
        @SafeVarargs public final Builder firstOff(Keyframe<HandPose>... f) { firstOff=seg(f); return this; }
        public CastingTrack build() {
            var missing = new ArrayList<String>();
            if (opening==null) missing.add("opening"); if (hold==null) missing.add("hold");
            if (release==null) missing.add("release"); if (recovery==null) missing.add("recovery"); if (cancel==null) missing.add("cancel");
            if (firstOpening==null) missing.add("firstOpening"); if (firstHold==null) missing.add("firstHold");
            if (firstRelease==null) missing.add("firstRelease"); if (firstRecovery==null) missing.add("firstRecovery"); if (firstCancel==null) missing.add("firstCancel");
            if (!missing.isEmpty()) throw new IllegalStateException("Track is missing "+missing);
            return new CastingTrack(opening,hold,strain,release,recovery,cancel,on,off,loop,
                    firstOpening,firstHold,firstStrain,firstRelease,firstRecovery,firstCancel,firstOn,firstOff);
        }
    }
}
