package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.AimLayer;
import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack.*;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import java.util.EnumMap;
import java.util.List;
import static com.vincenthuto.hemomancy.client.player.CastingPose.degrees;
import static com.vincenthuto.hemomancy.client.player.HandPose.hand;

/**
 * Still Art endings: the Unstained aim layer. Only the lead arm and head (third) and lead hand (first) are read.
 * Keyed on release ticks; every ending starts from the stages' low readiness and has arrived by tick 8 (the shortest
 * release), so recovery fades from the same frame whatever the stage. Nothing overshoots and the head never lifts.
 */
public final class StillArtEndings {
    private static final EnumMap<StillArtEnding,AimLayer> ENDINGS=new EnumMap<>(StillArtEnding.class);
    private static final CastingPose READY=lead(0,0,-20,0,-6);
    private static final HandPose F_READY=hand(-.04,.03,-.02,-4,0,-2);
    static {
        // Rebuke: the palm pushes forward slowly to the aim and stops there.
        arrive(StillArtEnding.REBUKE,lead(0,0,-90,0,0),hand(-.30,.12,-.12,-12,0,0));
        // Hush: the palm turns down and the arm settles 10 degrees below level.
        put(StillArtEnding.HUSH,
            third(k(0,READY,Easing.LINEAR),k(4,lead(2,0,-72,0,-4),Easing.SMOOTH),k(8,lead(3,0,-62,0,0),Easing.SMOOTH),k(12,lead(3,0,-62,0,0),Easing.LINEAR)),
            first(k(0,F_READY,Easing.LINEAR),k(4,hand(-.18,.08,-.05,-10,0,-2),Easing.SMOOTH),k(8,hand(-.18,.01,-.05,8,0,0),Easing.SMOOTH),k(12,hand(-.18,.01,-.05,8,0,0),Easing.LINEAR)));
        // Stillness: the arm stops level and holds.
        arrive(StillArtEnding.STILLNESS,lead(0,0,-80,0,-8),hand(-.18,.10,-.05,-12,0,-4));
        // Sight: the lead hand rises to the brow while the head scans left, then right, and returns level.
        put(StillArtEnding.SIGHT,
            third(k(0,READY,Easing.LINEAR),k(3,lead(2,6,-95,0,10),Easing.SMOOTH),k(6,lead(2,-6,-118,0,15),Easing.SMOOTH),
                    k(8,lead(2,0,-120,0,15),Easing.SMOOTH),k(12,lead(2,0,-120,0,15),Easing.LINEAR)),
            first(k(0,F_READY,Easing.LINEAR),k(4,hand(-.30,.24,-.08,-24,0,0),Easing.SMOOTH),k(6,hand(-.20,.24,-.08,-24,-6,0),Easing.SMOOTH),
                    k(8,hand(-.30,.24,-.08,-24,0,0),Easing.SMOOTH),k(12,hand(-.30,.24,-.08,-24,0,0),Easing.LINEAR)));
        // Shear: one slow horizontal cut at shoulder height, from outside the body to just across it (arm yaw, not roll).
        put(StillArtEnding.SHEAR,
            third(k(0,READY,Easing.LINEAR),k(3,lead(0,0,-85,40,-4),Easing.SMOOTH),k(8,lead(0,0,-85,-10,0),Easing.SMOOTH),k(12,lead(0,0,-85,-10,0),Easing.LINEAR)),
            first(k(0,F_READY,Easing.LINEAR),k(3,hand(.05,.10,-.06,-10,0,-6),Easing.SMOOTH),k(8,hand(-.33,.10,-.06,-10,8,0),Easing.SMOOTH),k(12,hand(-.33,.10,-.06,-10,8,0),Easing.LINEAR)));
        // Step: the arm trails back 15 degrees with the head level.
        arrive(StillArtEnding.STEP,lead(0,0,15,0,-8),hand(.20,-.20,.10,10,0,-6));
        // Intercession: the arm opens outward and up, palm up.
        arrive(StillArtEnding.INTERCESSION,lead(0,0,-40,0,-50),hand(.20,.20,-.04,-18,0,-20));
    }
    private StillArtEndings() {}
    public static AimLayer get(StillArtEnding ending) { return ending==null?null:ENDINGS.get(ending); }

    /** Slow eased arrival from readiness by tick 8, then held through the longest release. */
    private static void arrive(StillArtEnding ending,CastingPose to,HandPose fTo) {
        put(ending,third(k(0,READY,Easing.LINEAR),k(8,to,Easing.EASE_OUT),k(12,to,Easing.LINEAR)),
                first(k(0,F_READY,Easing.LINEAR),k(8,fTo,Easing.EASE_OUT),k(12,fTo,Easing.LINEAR)));
    }
    /** Head (pitch, positive bows; yaw) and lead arm (x, yaw, z; positive yaw swings the right arm outward); degrees. */
    private static CastingPose lead(double headX,double headY,double x,double yaw,double z) {
        return new CastingPose(degrees(headX,headY,0),CastingPose.Rotation.ZERO,degrees(x,yaw,z),CastingPose.Rotation.ZERO,
                CastingPose.Rotation.ZERO,CastingPose.Rotation.ZERO,0);
    }
    private static void put(StillArtEnding e,Segment<CastingPose> third,Segment<HandPose> first) { ENDINGS.put(e,new AimLayer(third,first)); }
    private static <P extends com.vincenthuto.hemomancy.client.player.Blendable<P>> Keyframe<P> k(int t,P pose,Easing e) { return new Keyframe<>(t,pose,e); }
    @SafeVarargs private static Segment<CastingPose> third(Keyframe<CastingPose>... f) { return new Segment<>(List.of(f)); }
    @SafeVarargs private static Segment<HandPose> first(Keyframe<HandPose>... f) { return new Segment<>(List.of(f)); }
}
