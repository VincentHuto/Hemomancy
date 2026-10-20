package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.client.player.CastingTrack.*;
import java.util.List;

/** Shared authoring shorthand for school, bespoke and Still Art tracks. All angles in degrees, translations in blocks. */
public final class TrackAuthoring {
    private TrackAuthoring() {}
    // Third-person keyframes: p = smooth, out = ease-out (decelerate into the pose), step = hold then jump, lin = linear.
    public static Keyframe<CastingPose> p(int t,CastingPose pose) { return new Keyframe<>(t,pose,Easing.SMOOTH); }
    public static Keyframe<CastingPose> out(int t,CastingPose pose) { return new Keyframe<>(t,pose,Easing.EASE_OUT); }
    public static Keyframe<CastingPose> step(int t,CastingPose pose) { return new Keyframe<>(t,pose,Easing.HOLD); }
    public static Keyframe<CastingPose> lin(int t,CastingPose pose) { return new Keyframe<>(t,pose,Easing.LINEAR); }
    // First-person keyframes.
    public static Keyframe<HandPose> h(int t,HandPose pose) { return new Keyframe<>(t,pose,Easing.SMOOTH); }
    public static Keyframe<HandPose> hout(int t,HandPose pose) { return new Keyframe<>(t,pose,Easing.EASE_OUT); }
    public static Keyframe<HandPose> hstep(int t,HandPose pose) { return new Keyframe<>(t,pose,Easing.HOLD); }
    public static Keyframe<HandPose> hlin(int t,HandPose pose) { return new Keyframe<>(t,pose,Easing.LINEAR); }
    @SafeVarargs public static <P extends Blendable<P>> Segment<P> seq(Keyframe<P>... frames) { return new Segment<>(List.of(frames)); }

    /**
     * A builder with the common shape filled in: hold = opening's last pose, recovery eases from release's last pose to rest,
     * cancel passes through {@code cancelVia} at 30% then rests. First-person cancel folds the hold halfway to rest, then rests.
     * Callers may still override any segment or add strain/loop/on/off before build().
     */
    public static CastingTrack.Builder track(Segment<CastingPose> opening,Segment<CastingPose> release,int recovery,
                                             CastingPose cancelVia,int cancel,
                                             Segment<HandPose> firstOpening,Segment<HandPose> firstRelease,int firstRecovery,int firstCancel) {
        var hold=opening.last(CastingPose.ZERO); var firstHold=firstOpening.last(HandPose.ZERO);
        var builder=new CastingTrack.Builder()
            .opening(opening.frames().toArray(new Keyframe[0])).hold(hold)
            .release(release.frames().toArray(new Keyframe[0]))
            .recovery(lin(0,release.last(CastingPose.ZERO)),p(recovery,CastingPose.ZERO))
            .cancel(lin(0,hold),p(Math.max(1,Math.round(cancel*.3F)),cancelVia),p(cancel,CastingPose.ZERO))
            .firstOpening(firstOpening.frames().toArray(new Keyframe[0])).firstHold(firstHold)
            .firstRelease(firstRelease.frames().toArray(new Keyframe[0]))
            .firstRecovery(hlin(0,firstRelease.last(HandPose.ZERO)),h(firstRecovery,HandPose.ZERO))
            .firstCancel(hlin(0,firstHold),h(Math.max(1,Math.round(firstCancel*.3F)),firstHold.blend(HandPose.ZERO,.5F)),h(firstCancel,HandPose.ZERO));
        return builder;
    }
}
