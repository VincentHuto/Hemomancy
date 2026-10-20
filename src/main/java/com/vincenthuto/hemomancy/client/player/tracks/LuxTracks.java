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
 * Lux: rise, part, proclaim. An orant: the arms lift forward together, then part sideways to shoulder height with
 * the face lifted; release opens them wider. Cancel lowers the arms first and lets the head come down last.
 * pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): negative head lifts the face, negative lean leans back.
 * Arm x applies before z: "raise" poses keep x at or past -90 with small z; sideways spread uses a small x and a large z.
 */
public final class LuxTracks {
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
    private LuxTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    /** Lead arm rises palm-forward with the head lifting; release lifts it a little higher. */
    private static CastingTrack humilis() {
        var hold=pose(-4,0,0,-100,-14,0,0,0); var fHold=hand(-.16,.16,-.08,-11,0,-6); var lower=pose(-4,0,0,-40,-6,0,0,0);
        return track(
                seq(lin(0,Z),p(4,pose(-1,0,0,-52,-6,0,0,0)),out(8,hold)),
                seq(lin(0,hold),out(2,pose(-6,0,0,-112,-18,0,0,0)),p(6,pose(-5,0,0,-107,-16,0,0,0))),12,lower,10,
                seq(hlin(0,HZ),h(4,hand(-.06,.06,-.03,-4,0,-2)),hout(8,fHold)),
                seq(hlin(0,fHold),hout(2,hand(-.17,.22,-.10,-14,0,-8)),h(6,hand(-.16,.20,-.09,-13,0,-7))),12,10)
            .cancel(lin(0,hold),p(5,lower),p(10,Z)).build();
    }

    /** Both arms rise forward together; release parts them out to the sides. */
    private static CastingTrack mediocritas() {
        var hold=pose(-5,0,0,-100,-14,-100,14,0); var lower=pose(-5,0,0,-38,-14,-38,14,0);
        var fHold=hands(-.10,.15,-.07,-11,0,-6,.10,.15,-.07,-11,0,6);
        return track(
                seq(lin(0,Z),p(4,pose(-1,0,0,-54,-6,-54,6,0)),out(9,hold)),
                seq(lin(0,hold),out(3,pose(-7,0,0,-38,-74,-38,74,0)),p(7,pose(-6,0,0,-36,-70,-36,70,0))),14,lower,11,
                seq(hlin(0,HZ),h(4,hands(-.04,.06,-.03,-5,0,-2,.04,.06,-.03,-5,0,2)),hout(9,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.02,.20,-.09,-14,0,-12,-.02,.20,-.09,-14,0,12)),
                        h(7,hands(.01,.19,-.08,-13,0,-11,-.01,.19,-.08,-13,0,11))),14,11)
            .cancel(lin(0,hold),p(6,lower),p(11,Z)).build();
    }

    /** Arms rise forward, then part into an orant near shoulder height; slight backward lean, planted stance, face up. */
    private static CastingTrack summa() {
        var hold=pose(-7,-3,0,-32,-76,-32,76,6); var lower=pose(-7,-2,0,-18,-30,-18,30,3);
        var fHold=hands(.06,.20,-.06,-10,0,-10,-.06,.20,-.06,-10,0,10);
        return track(
                seq(lin(0,Z),p(4,pose(-2,-1,0,-62,-14,-62,14,2)),out(10,hold)),
                seq(lin(0,hold),out(3,pose(-9,-4,0,-22,-88,-22,88,7)),p(8,pose(-8,-4,0,-23,-86,-23,86,7))),16,lower,12,
                seq(hlin(0,HZ),h(4,hands(0,.08,-.03,-4,0,-4,0,.08,-.03,-4,0,4)),hout(10,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.22,.22,-.04,-11,0,-18,-.22,.22,-.04,-11,0,18)),
                        h(8,hands(.20,.21,-.04,-10,0,-16,-.20,.21,-.04,-10,0,16))),16,12)
            .cancel(lin(0,hold),p(7,lower),p(12,Z)).build();
    }

    /** Charge lifts the parted arms toward level and tips the face further up; the hold breathes in a slow rise and fall. */
    private static CastingTrack magister() {
        var hold=pose(-8,-3,0,-34,-72,-34,72,6); var lower=pose(-8,-2,0,-18,-30,-18,30,3);
        var fHold=hands(.04,.16,-.06,-8,0,-10,-.04,.16,-.06,-8,0,10);
        return track(
                seq(lin(0,Z),p(4,pose(-2,-1,0,-62,-14,-62,14,2)),p(8,pose(-6,-2,0,-45,-58,-45,58,5)),out(11,hold)),
                seq(lin(0,hold),out(3,pose(-10,-4,0,-26,-82,-26,82,7)),p(9,pose(-9,-4,0,-27,-80,-27,80,7))),18,lower,13,
                seq(hlin(0,HZ),h(4,hands(0,.06,-.03,-3,0,-4,0,.06,-.03,-3,0,4)),
                        h(8,hands(.03,.12,-.05,-6,0,-8,-.03,.12,-.05,-6,0,8)),hout(11,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.20,.20,-.05,-10,0,-18,-.20,.20,-.05,-10,0,18)),
                        h(9,hands(.18,.19,-.05,-9,0,-16,-.18,.19,-.05,-9,0,16))),18,13)
            .strain(pose(-12,-5,0,-25,-84,-25,84,7),hands(.04,.21,-.08,-11,0,-12,-.04,.21,-.08,-11,0,12))
            .loop(pose(-1.5,-1,0,1,-2,1,2,0),hands(0,.006,0,0,0,0,0,.006,0,0,0,0),48)
            .cancel(lin(0,hold),p(7,lower),p(13,Z)).build();
    }

    /** Full cruciform (arms level at the sides, x -20, z 90 out), face up, perfectly still; release lifts it all 2 degrees. */
    private static CastingTrack perfectus() {
        var hold=pose(-8,-3,0,-20,-90,-20,90,6); var lower=pose(-8,-2,0,-15,-32,-15,32,3);
        var fHold=hands(.30,.18,-.04,-5,0,-20,-.30,.18,-.04,-5,0,20);
        return track(
                seq(lin(0,Z),p(4,pose(-2,-1,0,-60,-20,-60,20,2)),p(8,pose(-6,-2,0,-35,-65,-35,65,5)),out(12,hold)),
                seq(lin(0,hold),p(4,pose(-10,-5,0,-20,-92,-20,92,6)),p(10,pose(-9,-4,0,-20,-91,-20,91,6))),20,lower,14,
                seq(hlin(0,HZ),h(4,hands(.04,.06,-.02,-2,0,-6,-.04,.06,-.02,-2,0,6)),
                        h(8,hands(.20,.14,-.03,-4,0,-14,-.20,.14,-.03,-4,0,14)),hout(12,fHold)),
                seq(hlin(0,fHold),h(4,hands(.30,.20,-.04,-6,0,-20,-.30,.20,-.04,-6,0,20)),
                        h(10,hands(.30,.19,-.04,-5,0,-20,-.30,.19,-.04,-5,0,20))),20,14)
            .strain(pose(-11,-5,0,-20,-92,-20,92,7),hands(.32,.21,-.05,-6,0,-22,-.32,.21,-.05,-6,0,22))
            .cancel(lin(0,hold),p(8,lower),p(14,Z)).build();
    }
}
