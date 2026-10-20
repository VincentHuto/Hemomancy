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
 * Animus: contract, then open (heartbeat). The arms squeeze inward to the sternum, then open into a guard.
 * pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): right z positive and left z negative are inward.
 * Arm z only reads as spread below x -90 (it rolls a straight-forward arm), so open guards keep x near -55..-70.
 */
public final class AnimusTracks {
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
    private AnimusTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    /** Lead arm pulls to the sternum with a head dip, then thrusts forward. Nothing else moves. */
    private static CastingTrack humilis() {
        var hold=pose(3,0,0,-60,28,0,0,0); var end=pose(-1,0,0,-95,-10,0,0,0);
        var fHold=hand(-.18,-.10,.04,10,-6,14); var fEnd=hand(-.26,.05,-.10,-10,-4,2);
        return track(seq(p(0,REST),out(3,pose(1,0,0,-30,14,0,0,0)),p(6,pose(3.5,0,0,-58,30,0,0,0)),p(8,hold)),
                seq(lin(0,hold),out(2,pose(-1.5,0,0,-102,-6,0,0,0)),p(6,end)),12,pose(4,0,0,-36,14,0,0,0),10,
                seq(h(0,HREST),h(4,hand(-.08,-.05,.02,5,-2,7)),h(8,fHold)),
                seq(hlin(0,fHold),hout(2,hand(-.29,.07,-.14,-13,-4,2)),h(6,fEnd)),12,10).build();
    }

    /** Both arms draw into a loose cup before the chest, then open outward to a guard. */
    private static CastingTrack mediocritas() {
        var hold=pose(3,0,0,-55,30,-55,-30,0); var end=pose(-1.5,0,0,-68,-40,-68,40,0);
        var fHold=hands(-.22,-.08,.02,10,-8,18,.22,-.08,.02,10,8,-18); var fEnd=hands(.02,.08,-.06,-8,8,-12,-.02,.08,-.06,-8,-8,12);
        return track(seq(p(0,REST),out(4,pose(1.5,0,0,-35,16,-35,-16,0)),p(9,hold)),
                seq(lin(0,hold),out(3,pose(-2,0,0,-72,-46,-72,46,0)),p(7,end)),14,pose(4.5,0,0,-32,14,-32,-14,0),11,
                seq(h(0,HREST),h(4,hands(-.10,-.04,0,4,0,8,.10,-.04,0,4,0,-8)),h(9,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.04,.10,-.08,-10,10,-14,-.04,.10,-.08,-10,-10,14)),h(7,fEnd)),14,11).build();
    }

    /** The legs plant and the waist folds 4 degrees into the squeeze, rocks 2 back on the open; recovery is a weaker second beat. */
    private static CastingTrack summa() {
        var hold=pose(3.5,4,0,-58,32,-58,-32,8); var end=pose(-2,-2,0,-60,-52,-60,52,8);
        var fHold=hands(-.24,-.09,.06,12,-10,20,.24,-.09,.06,12,10,-20); var fEnd=hands(.10,.10,-.08,-10,12,-16,-.10,.10,-.08,-10,-12,16);
        return track(seq(p(0,REST),out(4,pose(1.5,1.5,0,-38,18,-38,-18,3)),p(10,hold)),
                seq(lin(0,hold),out(3,pose(-3,-2.5,0,-66,-58,-66,58,8)),p(8,end)),16,pose(5,2,0,-34,14,-34,-14,5),12,
                seq(h(0,HREST),h(4,hands(-.10,-.04,.02,4,0,8,.10,-.04,.02,4,0,-8)),h(10,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.12,.12,-.10,-12,14,-18,-.12,.12,-.10,-12,-14,18)),h(8,fEnd)),16,12)
            .recovery(lin(0,end),p(4,pose(1.5,1.5,0,-48,6,-48,-6,6)),out(7,pose(-.5,-.5,0,-42,-20,-42,20,5)),p(16,REST))
            .firstRecovery(hlin(0,fEnd),h(4,hands(-.08,-.02,.04,4,-4,6,.08,-.02,.04,4,4,-6)),h(16,HREST)).build();
    }

    /** A slow two-beat squeeze; charge tightens the arms and drops the head, a channel breathes at chest height. */
    private static CastingTrack magister() {
        var hold=pose(3,3.5,0,-58,30,-58,-30,9); var end=pose(-3,-2,0,-55,-62,-55,62,9);
        var fHold=hands(-.23,-.08,.04,11,-9,18,.23,-.08,.04,11,9,-18); var fEnd=hands(.14,.11,-.10,-12,16,-20,-.14,.11,-.10,-12,-16,20);
        return track(seq(p(0,REST),out(3,pose(2,2,0,-42,22,-42,-22,4)),p(5,pose(1,1,0,-38,10,-38,-10,4)),
                        out(8,pose(3.5,4,0,-56,32,-56,-32,8)),p(11,hold)),
                seq(lin(0,hold),out(3,pose(-4,-3,0,-62,-70,-62,70,9)),p(9,end)),18,pose(5.5,2.5,0,-36,16,-36,-16,6),13,
                seq(h(0,HREST),h(3,hands(-.08,-.03,.02,4,0,8,.08,-.03,.02,4,0,-8)),h(5,hands(-.05,-.02,.01,2,0,4,.05,-.02,.01,2,0,-4)),
                        h(8,hands(-.24,-.09,.05,12,-10,20,.24,-.09,.05,12,10,-20)),h(11,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.16,.13,-.12,-14,18,-22,-.16,.13,-.12,-14,-18,22)),h(9,fEnd)),18,13)
            .strain(pose(9,6,0,-60,40,-60,-40,12),hands(-.28,-.11,.07,16,-10,26,.28,-.11,.07,16,10,-26))
            .loop(pose(-1.5,1,0,-3,-4,-3,4,0),hands(0,.008,-.006,1,0,-1.5,0,.008,-.006,1,0,1.5),48).build();
    }

    /** Iconic: a wide open guard held dead still; the heart anatomy beats. Release is a single 2 degree drop at the waist. */
    private static CastingTrack perfectus() {
        var hold=pose(0,0,0,-55,-72,-55,72,7); var end=pose(.5,2,0,-54,-73,-54,73,7);
        var fHold=hands(.22,.06,-.04,0,22,-28,-.22,.06,-.04,0,-22,28); var fEnd=hands(.22,.03,-.05,0,22,-28,-.22,.03,-.05,0,-22,28);
        return track(seq(p(0,REST),out(4,pose(1,1,0,-34,14,-34,-14,3)),p(12,hold)),
                seq(lin(0,hold),out(3,pose(1,2,0,-53,-74,-53,74,7)),p(10,end)),20,pose(4,1.5,0,-34,-36,-34,36,4),14,
                seq(h(0,HREST),h(4,hands(-.06,-.02,.02,0,0,4,.06,-.02,.02,0,0,-4)),h(12,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.23,.04,-.06,0,22,-28,-.23,.04,-.06,0,-22,28)),h(10,fEnd)),20,14)
            .strain(pose(1.5,1,0,-58,-78,-58,78,8),hands(.25,.07,-.05,0,24,-30,-.25,.07,-.05,0,-24,30)).build();
    }
}
