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

/**
 * Flammeus: coil, then expel. A thrower's silhouette: the arm cocks out and back, then drives through to the front
 * while the head recoils from the heat. pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance).
 */
public final class FlammeusTracks {
    private static final EnumMap<EnumManipulationRank,CastingTrack> TRACKS=new EnumMap<>(EnumManipulationRank.class);
    private static final CastingPose REST=CastingPose.ZERO;
    private static final HandPose HREST=HandPose.ZERO;
    static {
        TRACKS.put(EnumManipulationRank.HUMILIS,humilis());
        TRACKS.put(EnumManipulationRank.MEDIOCRITAS,mediocritas());
        TRACKS.put(EnumManipulationRank.SUMMA,summa());
        TRACKS.put(EnumManipulationRank.MAGISTER,magister());
        TRACKS.put(EnumManipulationRank.PERFECTUS,perfectus());
    }
    private FlammeusTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    /** Lead arm cocks out to the side, then snaps through to the front; the head flinches back. No torso. */
    private static CastingTrack humilis() {
        var hold=pose(2,0,0,-40,-55,0,0,0); var end=pose(-3,0,0,-100,-8,0,0,0);
        var fHold=hand(.30,-.04,.12,8,20,-18); var fEnd=hand(-.27,-.07,-.14,-12,-6,4);
        return track(seq(p(0,REST),out(3,pose(1,0,0,-25,-25,0,0,0)),p(8,hold)),
                seq(lin(0,hold),out(2,pose(-4,0,0,-104,-6,0,0,0)),p(6,end)),12,pose(1,0,0,-22,-58,0,0,0),10,
                seq(h(0,HREST),h(3,hand(.10,0,.04,2,6,-6)),h(8,fHold)),
                seq(hlin(0,fHold),hout(2,hand(-.30,-.08,-.18,-14,-8,6)),h(6,fEnd)),12,10).build();
    }

    /** The torso winds back while the support arm braces low across the belly; release untwists, the lead drives, the support opens. */
    private static CastingTrack mediocritas() {
        var hold=pose(3,0,-7,-38,-58,-32,-26,0); var end=pose(-3,0,4,-100,-10,-55,26,0);
        var fHold=hands(.30,-.04,.12,8,20,-18,.20,-.06,-.04,6,-8,12); var fEnd=hands(-.27,-.07,-.14,-12,-6,4,.24,-.04,-.12,-8,6,-4);
        return track(seq(p(0,REST),out(3,pose(1,0,-2,-25,-22,-15,-10,0)),p(9,hold)),
                seq(lin(0,hold),out(2,pose(-4,0,5,-106,-8,-58,30,0)),p(7,end)),14,pose(1,0,-2,-22,-58,-18,40,0),11,
                seq(h(0,HREST),h(3,hands(.10,0,.04,2,6,-6,.04,-.02,0,0,0,0)),h(9,fHold)),
                seq(hlin(0,fHold),hout(2,hands(-.30,-.08,-.18,-14,-8,6,.26,-.04,-.14,-10,6,-4)),h(7,fEnd)),14,11).build();
    }

    /** Both arms cock wide over a planted stance with the waist drawn back; the drive pitches 5 degrees forward under a 3-tick head recoil. */
    private static CastingTrack summa() {
        var hold=pose(3,-2.5,-10,-36,-60,-36,60,9); var end=pose(-2.5,4.5,5,-98,-12,-96,12,9);
        var fHold=hands(.32,-.04,.14,8,22,-20,-.32,-.04,.14,8,-22,20); var fEnd=hands(-.26,-.05,-.15,-12,-8,4,.26,-.05,-.15,-12,8,-4);
        return track(seq(p(0,REST),out(3,pose(1,-1,-3,-25,-25,-25,25,3)),p(10,hold)),
                seq(lin(0,hold),out(2,pose(-5,5,6,-104,-10,-100,10,9)),p(5,pose(-2,5,5,-100,-12,-98,12,9)),p(8,end)),16,
                pose(1,0,-3,-22,-64,-22,64,5),12,
                seq(h(0,HREST),h(3,hands(.10,0,.04,2,6,-6,-.10,0,.04,2,-6,6)),h(10,fHold)),
                seq(hlin(0,fHold),hout(2,hands(-.30,-.06,-.19,-16,-10,6,.30,-.06,-.19,-16,10,-6)),h(8,fEnd)),16,12).build();
    }

    /** A two-stage coil; charge shakes the cocked arms wider and lowers the head, a channel rocks slowly at the waist. */
    private static CastingTrack magister() {
        var hold=pose(3,-3,-11,-38,-62,-38,62,10); var end=pose(-2.5,5,5,-100,-12,-98,12,10);
        var fHold=hands(.31,-.05,.14,8,22,-20,-.31,-.05,.14,8,-22,20); var fEnd=hands(-.27,-.05,-.15,-12,-8,4,.27,-.05,-.15,-12,8,-4);
        return track(seq(p(0,REST),out(3,pose(1,-1,-3,-25,-25,-25,25,3)),p(7,pose(2,-2,-8,-34,-52,-34,52,7)),out(11,hold)),
                seq(lin(0,hold),out(3,pose(-5,6,6,-108,-10,-104,10,10)),p(6,pose(-2,5.5,5,-102,-12,-100,12,10)),p(9,end)),18,
                pose(1,0,-3,-24,-68,-24,68,6),13,
                seq(h(0,HREST),h(3,hands(.10,0,.04,2,6,-6,-.10,0,.04,2,-6,6)),h(11,fHold)),
                seq(hlin(0,fHold),hout(3,hands(-.32,-.06,-.19,-16,-10,6,.32,-.06,-.19,-16,10,-6)),h(9,fEnd)),18,13)
            .strain(pose(7,-4,-12,-40,-72,-40,72,12),hands(.34,-.07,.16,10,26,-24,-.34,-.07,.16,10,-26,24))
            .loop(pose(0,1.5,3,0,-2,0,2,0),hands(0,.006,0,0,1.5,-1,0,.006,0,0,-1.5,1),48).build();
    }

    /** Iconic: planted, arms low and wide, head lifted inside the inferno and held still. Release is one hard 4 degree pitch. */
    private static CastingTrack perfectus() {
        var hold=pose(-6,0,0,-45,-55,-45,55,10); var end=pose(-8.5,4,0,-46,-56,-46,56,10);
        var fHold=hands(.18,-.18,-.06,3,10,-12,-.18,-.18,-.06,3,-10,12); var fEnd=hands(.19,-.22,-.10,3,10,-12,-.19,-.22,-.10,3,-10,12);
        return track(seq(p(0,REST),out(4,pose(-1,0,0,-25,-20,-25,20,5)),p(12,hold)),
                seq(lin(0,hold),out(2,pose(-9,4,0,-47,-57,-47,57,10)),p(10,end)),20,pose(-2,0,0,-24,-66,-24,66,6),14,
                seq(h(0,HREST),h(4,hands(.06,-.04,0,0,0,-4,-.06,-.04,0,0,0,4)),h(12,fHold)),
                seq(hlin(0,fHold),hout(2,hands(.20,-.23,-.12,4,10,-12,-.20,-.23,-.12,4,-10,12)),h(10,fEnd)),20,14)
            .strain(pose(-7,-1,0,-48,-60,-48,60,11),hands(.20,-.19,-.07,3,12,-14,-.20,-.19,-.07,3,-12,14)).build();
    }
}
