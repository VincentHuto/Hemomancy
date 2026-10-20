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

/** Congeatio: close, decelerate, lock. Every part stops on the same tick; the hold is absolute, so no rank loops. */
public final class CongeatioTracks {
    private static final EnumMap<EnumManipulationRank,CastingTrack> TRACKS=new EnumMap<>(EnumManipulationRank.class);
    static {
        // pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): positive right z / negative left z is inward.
        // Each row: gather, hold, snap, settle, fracture; then the matching first-person hands; then strain.
        TRACKS.put(EnumManipulationRank.HUMILIS,congeal(8,6,14,10,
                pose(1,0,0,-44,-10,0,0,0),pose(3,0,0,-75,20,0,0,0),pose(2,0,0,-85,12,0,0,0),pose(4,0,0,-73,19,0,0,0),pose(3,0,0,4,0,0,0,0),
                hand(-.12,.01,-.05,-6,0,6),hand(-.28,.04,-.1,-12,0,18),hand(-.21,.07,-.13,-18,0,10),hand(-.27,.03,-.1,-11,0,17),hand(-.06,-.2,.02,10,0,2),
                null,null));
        TRACKS.put(EnumManipulationRank.MEDIOCRITAS,congeal(9,7,16,11,
                pose(2,0,0,-48,-8,-48,8,0),pose(4,0,0,-80,35,-80,-35,0),pose(3,0,0,-90,26,-90,-26,0),pose(5,0,0,-78,34,-78,-34,0),pose(4,0,0,4,-2,4,2,0),
                both(-.12,.01,-.05,-6,6),both(-.27,.04,-.1,-12,18),both(-.2,.07,-.13,-18,10),both(-.26,.03,-.1,-11,17),both(-.06,-.2,.02,10,2),
                null,null));
        // Summa: the waist and feet lock with the arms; recovery thaws linearly.
        TRACKS.put(EnumManipulationRank.SUMMA,congeal(10,8,18,12,
                pose(2,1,0,-50,-10,-50,10,5),pose(5,3,0,-85,42,-85,-42,9),pose(4,3,0,-95,32,-95,-32,9),pose(6,3,0,-83,41,-83,-41,9),pose(5,3,0,4,-3,4,3,9),
                both(-.13,.02,-.06,-7,6),both(-.3,.06,-.12,-14,22),both(-.22,.09,-.15,-20,12),both(-.29,.05,-.12,-13,21),both(-.06,-.21,.02,10,2),
                null,null));
        // Magister: charge tightens the cross and lowers the head; the snap runs against the strain, so it grows with charge.
        TRACKS.put(EnumManipulationRank.MAGISTER,congeal(11,9,19,13,
                pose(2,1,0,-52,-10,-52,10,5),pose(5,3,0,-88,40,-88,-40,9),pose(3,3,0,-97,30,-97,-30,9),pose(6,3,0,-86,39,-86,-39,9),pose(5,3,0,4,-3,4,3,9),
                both(-.13,.02,-.06,-7,6),both(-.3,.07,-.12,-14,22),both(-.22,.1,-.15,-18,12),both(-.29,.06,-.12,-13,21),both(-.06,-.21,.02,10,2),
                pose(9,3,0,-88,55,-88,-55,9),both(-.34,.05,-.11,-10,32)));
        // Perfectus: arms half-raised and crossed, head bowed, utterly still; the release is a one-tick, one-degree settle.
        TRACKS.put(EnumManipulationRank.PERFECTUS,congeal(12,10,20,14,
                pose(5,2,0,-36,-6,-36,6,5),pose(10,4,0,-60,45,-60,-45,8),pose(10,4,0,-61,44,-61,-44,8),pose(11,4,0,-59,45,-59,-45,8),pose(10,4,0,4,-3,4,3,8),
                both(-.15,.05,-.04,-5,10),both(-.33,.12,-.08,-10,30),both(-.32,.125,-.085,-11,29),both(-.33,.115,-.08,-10,30),both(-.06,-.2,.02,10,2),
                pose(12,4,0,-60,50,-60,-50,8),both(-.34,.12,-.08,-16,34)));
    }
    private CongeatioTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    /**
     * Opening gathers (arms open slightly), then one ease-out span closes every part onto the hold together.
     * Release: a 1-tick micro-snap, an ease-out settle, then stillness. Recovery thaws linearly. Cancel fractures:
     * the arms drop in one 3-tick move while head, waist and feet follow to rest.
     */
    private static CastingTrack congeal(int opening,int release,int recovery,int cancel,
            CastingPose gather,CastingPose hold,CastingPose snap,CastingPose settle,CastingPose fracture,
            HandPose fGather,HandPose fHold,HandPose fSnap,HandPose fSettle,HandPose fDrop,CastingPose strain,HandPose fStrain) {
        int g=opening/2-1;
        var b=track(seq(lin(0,CastingPose.ZERO),lin(g,gather),out(opening,hold)),
                seq(lin(0,hold),lin(1,snap),out(3,settle),lin(release,settle)),recovery,fracture,cancel,
                seq(hlin(0,HandPose.ZERO),hlin(g,fGather),hout(opening,fHold)),
                seq(hlin(0,fHold),hlin(1,fSnap),hout(3,fSettle),hlin(release,fSettle)),recovery,cancel)
            .recovery(lin(0,settle),lin(recovery,CastingPose.ZERO))
            .cancel(lin(0,hold),lin(3,fracture),p(cancel,CastingPose.ZERO))
            .firstRecovery(hlin(0,fSettle),hlin(recovery,HandPose.ZERO))
            .firstCancel(hlin(0,fHold),hlin(3,fDrop),h(cancel,HandPose.ZERO));
        if(strain!=null)b.strain(strain,fStrain);
        return b.build();
    }
    /** Mirrored hands: the support hand gets the reflected translation and roll of the lead. */
    private static HandPose both(double tx,double ty,double tz,double rx,double rz) {
        return hands(tx,ty,tz,rx,0,rz,-tx,ty,tz,rx,0,-rz);
    }
}
