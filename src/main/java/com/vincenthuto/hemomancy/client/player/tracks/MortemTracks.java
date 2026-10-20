package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import java.util.EnumMap;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static com.vincenthuto.hemomancy.client.player.HandPose.hand;
import static com.vincenthuto.hemomancy.client.player.HandPose.hands;
import static com.vincenthuto.hemomancy.client.player.tracks.TrackAuthoring.*;

/** Mortem: reach, hook, haul. A hunched taker; rank widens the reach and deepens the bow, never the verb. */
public final class MortemTracks {
    private static final EnumMap<EnumManipulationRank,CastingTrack> TRACKS=new EnumMap<>(EnumManipulationRank.class);
    static {
        // pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): positive right z / negative left z is inward.
        // Humilis keeps the torso still, so its hunch lives in the bowed head.
        TRACKS.put(EnumManipulationRank.HUMILIS,reach(4,8,2,6,12,10,
                pose(6,0,0,-56,-6,0,0,0),pose(9,0,0,-84,4,0,0,0),pose(11,0,0,-90,14,0,0,0),pose(4,0,0,-28,-6,0,0,0),pose(-2,0,0,-46,2,0,0,0),
                hand(-.14,.02,-.09,-12,5,6),hand(-.26,.05,-.15,-20,8,12),hand(-.3,.03,-.17,-6,10,28),hand(-.14,-.2,.14,30,0,18)).build());
        // Mediocritas: the support arm hooks across the body; both haul to the hips.
        TRACKS.put(EnumManipulationRank.MEDIOCRITAS,reach(5,9,2,7,14,11,
                pose(6,2,0,-62,-6,-58,-18,0),pose(8,3,0,-88,4,-82,-30,0),pose(10,5,0,-94,14,-88,-38,0),pose(5,1,0,-30,-6,-28,6,0),pose(-2,-1,0,-50,2,-46,-14,0),
                hands(-.15,.03,-.09,-12,5,6,.14,.02,-.08,-11,-5,-6),hands(-.25,.05,-.15,-20,8,12,.23,.03,-.14,-18,-8,-10),
                hands(-.29,.03,-.17,-6,10,28,.27,.01,-.16,-4,-10,-26),hands(-.13,-.21,.13,30,0,18,.11,-.22,.13,30,0,-18)).build());
        // Summa: the waist bows and the feet spread; the hands are dragged past the bottom of the frame.
        var summaHaul=pose(6,3,0,-26,-8,-24,8,7);
        TRACKS.put(EnumManipulationRank.SUMMA,hang(reach(6,10,2,8,16,12,
                pose(7,5,0,-72,-6,-68,-20,5),pose(9,7,0,-96,6,-90,-32,7),pose(11,9,0,-102,16,-96,-42,7),summaHaul,pose(-3,-2,0,-54,3,-50,-16,4),
                hands(-.22,.05,-.13,-16,6,6,.2,.03,-.12,-15,-6,-6),hands(-.3,.06,-.18,-21,8,12,.28,.04,-.17,-19,-8,-12),
                hands(-.33,.04,-.19,-8,10,30,.31,.02,-.19,-6,-10,-28),hands(-.1,-.24,.16,34,0,20,.08,-.24,.16,34,0,-20)),
                summaHaul,4,pose(5,2,0,4,-4,4,4,5),pose(3,1,0,4,-4,4,4,4),16).build());
        // Magister: charge deepens the bow and curls the arms inward; held channels rock in a slow haul.
        var magisterHaul=pose(6,3,0,-28,-8,-26,8,8);
        TRACKS.put(EnumManipulationRank.MAGISTER,hang(reach(7,11,3,9,18,13,
                pose(7,5,0,-75,-6,-70,-22,6),pose(9,6,0,-96,8,-90,-34,8),pose(11,9,0,-102,18,-96,-44,8),magisterHaul,pose(-3,-2,0,-54,4,-50,-18,5),
                hands(-.22,.05,-.13,-16,6,6,.2,.03,-.12,-15,-6,-6),hands(-.3,.06,-.18,-19,8,12,.28,.04,-.17,-17,-8,-12),
                hands(-.32,.04,-.19,-8,10,30,.3,.02,-.19,-6,-10,-28),hands(-.1,-.22,.16,34,0,20,.08,-.22,.16,34,0,-20)),
                magisterHaul,5,pose(5,2,0,4,-4,4,4,6),pose(3,1,0,4,-4,4,4,5),18)
            .strain(pose(13,11,0,-92,22,-86,-46,8),hands(-.32,.02,-.16,-23,14,34,.3,0,-.15,-20,-14,-32))
            .loop(pose(1,2,0,-3,0,-3,0,0),hands(0,-.01,-.01,2,0,2,0,-.01,-.01,2,0,-2),60)
            .build());
        TRACKS.put(EnumManipulationRank.PERFECTUS,perfectus());
    }
    private MortemTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    /** Reach (decelerating into the hold), hook (a short further grasp inward), haul (yanked back to the hip). */
    private static CastingTrack.Builder reach(int midTick,int opening,int hookTick,int release,int recovery,int cancel,
            CastingPose mid,CastingPose hold,CastingPose hook,CastingPose haul,CastingPose cancelVia,
            HandPose fMid,HandPose fHold,HandPose fHook,HandPose fHaul) {
        return track(seq(lin(0,CastingPose.ZERO),lin(midTick,mid),out(opening,hold)),
                seq(lin(0,hold),p(hookTick,hook),out(release,haul)),recovery,cancelVia,cancel,
                seq(hlin(0,HandPose.ZERO),hlin(midTick,fMid),hout(opening,fHold)),
                seq(hlin(0,fHold),h(hookTick,fHook),hout(release,fHaul)),recovery,cancel);
    }
    /** Summa and above: the arms fall to the sides and hang two ticks while the bow lets go, then everything returns. */
    private static CastingTrack.Builder hang(CastingTrack.Builder b,CastingPose haul,int fall,CastingPose fallen,CastingPose hung,int recovery) {
        return b.recovery(lin(0,haul),p(fall,fallen),lin(fall+2,hung),p(recovery,CastingPose.ZERO));
    }
    /** Iconic: bowed with the arms hanging slightly forward; the cadaver anatomy performs the extraction. */
    private static CastingTrack perfectus() {
        var hold=pose(12,10,0,-25,4,-25,-4,6);
        var bow=pose(13,12,0,-27,6,-27,-6,6);
        var fHold=hands(-.1,-.2,-.05,4,0,-8,.1,-.2,-.05,4,0,8);
        var fCurl=hands(-.12,-.22,-.02,30,0,18,.12,-.22,-.02,30,0,-18);
        return track(seq(lin(0,CastingPose.ZERO),lin(7,pose(9,7,0,-18,0,-18,0,4)),out(12,hold)),
                seq(lin(0,hold),p(4,bow),lin(10,bow)),20,pose(-3,-2,0,-12,2,-12,-2,3),14,
                seq(hlin(0,HandPose.ZERO),hlin(7,hands(-.07,-.14,-.03,-7,0,-5,.07,-.14,-.03,-7,0,5)),hout(12,fHold)),
                seq(hlin(0,fHold),h(4,fCurl),hlin(10,fCurl)),20,14)
            .strain(pose(13,11,0,-28,10,-28,-10,6),hands(-.12,-.21,-.06,0,0,4,.12,-.21,-.06,0,0,-4))
            .recovery(lin(0,bow),p(6,pose(6,4,0,4,-4,4,4,4)),lin(8,pose(4,2,0,4,-4,4,4,3)),p(20,CastingPose.ZERO))
            .build();
    }
}
