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
 * Ductilis: snap, pause, snap. Every opening keyframe after rest is a HOLD step, a whole pose held 2-3 ticks and
 * never eased into; the head counter-jerks against each step. Release is one last step, cancel collapses in two.
 * pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): right z positive and left z negative are inward.
 */
public final class DuctilisTracks {
    private static final CastingPose Z=CastingPose.ZERO;
    private static final HandPose HZ=HandPose.ZERO;
    private static final EnumMap<EnumManipulationRank,CastingTrack> TRACKS=new EnumMap<>(EnumManipulationRank.class);
    static {
        TRACKS.put(EnumManipulationRank.HUMILIS,humilis());
        TRACKS.put(EnumManipulationRank.MEDIOCRITAS,mediocritas());
        TRACKS.put(EnumManipulationRank.SUMMA,summa());
        TRACKS.put(EnumManipulationRank.MAGISTER,magister());
        TRACKS.put(EnumManipulationRank.PERFECTUS,perfectus());
    }
    private DuctilisTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    /** Lead arm alone: half-raised and in, then raised and out, then a twitch to lock; release steps straight forward. */
    private static CastingTrack humilis() {
        var hold=pose(3,0,0,-102,-22,0,0,0); var fHold=hand(.03,.12,-.08,-9,0,-6);
        return collapse(track(
                seq(lin(0,Z),step(2,pose(5,0,0,-50,15,0,0,0)),step(5,pose(-4,0,0,-95,-25,0,0,0)),step(8,hold)),
                seq(lin(0,hold),step(2,pose(-2,0,0,-92,2,0,0,0)),step(6,pose(1,0,0,-90,0,0,0,0))),12,Z,10,
                seq(hlin(0,HZ),hstep(2,hand(-.08,-.05,.03,8,0,10)),hstep(5,hand(.07,.15,-.06,-12,0,-12)),hstep(8,fHold)),
                seq(hlin(0,fHold),hstep(2,hand(-.24,.09,-.16,-6,0,0)),hstep(6,hand(-.22,.08,-.14,-5,0,2))),12,10),
            hold,pose(2,0,0,-55,-12,0,0,0),pose(1,0,0,-18,-4,0,0,0),7,10,fHold).build();
    }

    /** Arms alternate: one thrown up, the other low and crossed; release snaps both forward, unevenly. */
    private static CastingTrack mediocritas() {
        var hold=pose(3,0,0,-104,-20,-42,-24,0); var fHold=hands(0,.15,-.08,-10,0,-8,.06,-.08,.02,6,0,-4);
        return collapse(track(
                seq(lin(0,Z),step(3,pose(5,0,0,-112,-14,-36,-28,0)),step(6,pose(-4,0,0,-38,28,-110,14,0)),step(9,hold)),
                seq(lin(0,hold),step(2,pose(-2,0,0,-94,-4,-80,16,0)),step(7,pose(1,0,0,-90,-2,-84,14,0))),14,Z,11,
                seq(hlin(0,HZ),hstep(3,hands(.02,.17,-.07,-12,0,-10,.05,-.10,.04,8,0,-6)),
                        hstep(6,hands(-.05,-.10,.04,8,0,6,.03,.17,-.07,-12,0,10)),hstep(9,fHold)),
                seq(hlin(0,fHold),hstep(2,hands(-.21,.08,-.15,-6,0,4,.17,.04,-.12,-5,0,-6)),
                        hstep(7,hands(-.20,.07,-.14,-5,0,2,.16,.03,-.11,-4,0,-4))),14,11),
            hold,pose(2,0,0,-60,-14,-20,-12,0),pose(1,0,0,-22,-5,-6,-4,0),7,11,fHold).build();
    }

    /** The torso joins: twist flips 8 degrees and the legs jump between a wide and a pinched stance on every step. */
    private static CastingTrack summa() {
        var hold=pose(-3,0,-8,-110,-18,-45,-28,9); var fHold=hands(.02,.18,-.09,-11,0,-6,.07,-.09,.03,6,0,-6);
        return collapse(track(
                seq(lin(0,Z),step(2,pose(4,0,8,-40,28,-108,10,-3)),step(4,pose(-4,0,-8,-112,-12,-38,-30,9)),
                        step(7,pose(5,0,8,-42,30,-114,14,-3)),step(10,hold)),
                seq(lin(0,hold),step(2,pose(-2,0,-5,-94,-6,-82,14,7)),step(8,pose(1,0,-3,-90,-3,-85,12,6))),16,Z,12,
                seq(hlin(0,HZ),hstep(2,hands(.05,-.10,.04,7,0,6,.03,.18,-.07,-12,0,6)),
                        hstep(4,hands(.03,.19,-.08,-12,0,-6,.06,-.11,.04,7,0,-6)),
                        hstep(7,hands(-.05,-.11,.05,8,0,6,.02,.19,-.08,-13,0,6)),hstep(10,fHold)),
                seq(hlin(0,fHold),hstep(2,hands(-.23,.09,-.16,-6,0,3,.19,.05,-.13,-5,0,-4)),
                        hstep(8,hands(-.22,.08,-.15,-5,0,2,.18,.04,-.12,-4,0,-3))),16,12),
            hold,pose(2,0,-3,-62,-12,-22,-14,4),pose(1,0,-1,-24,-4,-8,-5,2),7,12,fHold).build();
    }

    /** Wider steps with a waist jerk; charge exaggerates the split and the hold keeps a quick two-step tic. */
    private static CastingTrack magister() {
        var hold=pose(-4,-2,-9,-114,-20,-46,-30,10); var fHold=hands(.02,.19,-.10,-10,0,-7,.08,-.10,.04,6,0,-7);
        return collapse(track(
                seq(lin(0,Z),step(2,pose(4,3,9,-44,30,-116,12,-3)),step(5,pose(-5,-2,-9,-118,-16,-40,-32,10)),
                        step(8,pose(5,3,9,-46,32,-118,14,-3)),step(11,hold)),
                seq(lin(0,hold),step(2,pose(-3,2,-6,-96,-6,-84,18,9)),step(9,pose(1,1,-3,-91,-3,-86,14,7))),18,Z,13,
                seq(hlin(0,HZ),hstep(2,hands(.06,-.11,.05,7,0,7,.03,.20,-.08,-11,0,7)),
                        hstep(5,hands(.03,.20,-.09,-11,0,-7,.07,-.12,.05,7,0,-7)),
                        hstep(8,hands(-.05,-.12,.05,8,0,7,.02,.21,-.09,-12,0,7)),hstep(11,fHold)),
                seq(hlin(0,fHold),hstep(2,hands(-.24,.10,-.17,-6,0,3,.20,.05,-.14,-4,0,-4)),
                        hstep(9,hands(-.22,.09,-.15,-4,0,2,.18,.04,-.12,-4,0,-3))),18,13),
            hold,pose(2,1,-4,-64,-14,-24,-16,5),pose(1,0,-1,-24,-4,-8,-5,2),8,13,fHold)
            .strain(pose(-6,-3,-11,-124,-28,-38,-36,12),hands(.04,.23,-.12,-11,0,-10,.10,-.14,.06,8,0,-10))
            .loop(pose(3,0,3,-4,4,4,-4,0),hands(.008,.008,0,1,0,1.5,.008,-.008,0,-1,0,-1.5),12)
            .build();
    }

    /** Steps snap into a rigid symmetric frame (arms x -90, z 60 out) and stay; release is one 1-tick snap wider and back. */
    private static CastingTrack perfectus() {
        var hold=pose(-2,0,0,-90,-60,-90,60,5); var fHold=hands(-.12,.12,-.08,-12,0,0,.12,.12,-.08,-12,0,0);
        return collapse(track(
                seq(lin(0,Z),step(3,pose(4,0,6,-50,-28,-50,28,9)),step(6,pose(-4,0,-6,-98,-66,-98,66,-3)),
                        step(9,pose(3,0,3,-82,-52,-82,52,8)),step(12,hold)),
                seq(lin(0,hold),step(3,pose(-3,-1.5,0,-92,-76,-92,76,7)),step(4,hold),lin(10,hold)),20,Z,14,
                seq(hlin(0,HZ),hstep(3,hands(-.03,.03,-.02,-3,0,0,.03,.03,-.02,-3,0,0)),
                        hstep(6,hands(-.14,.15,-.10,-14,0,0,.14,.15,-.10,-14,0,0)),
                        hstep(9,hands(-.09,.09,-.06,-9,0,0,.09,.09,-.06,-9,0,0)),hstep(12,fHold)),
                seq(hlin(0,fHold),hstep(3,hands(-.02,.13,-.08,-12,0,-6,.02,.13,-.08,-12,0,6)),hstep(4,fHold),hlin(10,fHold)),20,14),
            hold,pose(0,0,2,-50,-38,-58,30,3),pose(1,0,-1,-22,-14,-14,10,1),7,14,fHold)
            .strain(pose(-3,0,0,-94,-64,-94,64,6),hands(-.13,.14,-.09,-14,0,0,.13,.14,-.09,-14,0,0))
            .build();
    }

    /** The circuit collapses: two uneven HOLD drops (tick 3 and {@code at}), then a limp settle to rest. */
    private static CastingTrack.Builder collapse(CastingTrack.Builder b,CastingPose hold,CastingPose first,CastingPose second,
                                                 int at,int cancel,HandPose fHold) {
        return b.cancel(lin(0,hold),step(3,first),step(at,second),p(cancel,Z))
                .firstCancel(hlin(0,fHold),hstep(3,fHold.blend(HZ,.5F)),hstep(at,fHold.blend(HZ,.85F)),h(cancel,HZ));
    }
}
