package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import java.util.EnumMap;
import static com.vincenthuto.hemomancy.client.player.CastingPose.degrees;
import static com.vincenthuto.hemomancy.client.player.HandPose.hand;
import static com.vincenthuto.hemomancy.client.player.HandPose.hands;
import static com.vincenthuto.hemomancy.client.player.tracks.TrackAuthoring.*;

/** Tenebris (spec §7.8): turn away, cross, unfold late. A figure facing the wrong way: the arm sweeps open first, the body follows. */
public final class TenebrisTracks {
    private static final EnumMap<EnumManipulationRank,CastingTrack> TRACKS=new EnumMap<>(EnumManipulationRank.class);
    private static final CastingPose Z=CastingPose.ZERO;
    private static final HandPose HZ=HandPose.ZERO;
    static {
        TRACKS.put(EnumManipulationRank.HUMILIS,humilis());
        TRACKS.put(EnumManipulationRank.MEDIOCRITAS,mediocritas());
        TRACKS.put(EnumManipulationRank.SUMMA,summa());
        TRACKS.put(EnumManipulationRank.MAGISTER,magister());
        TRACKS.put(EnumManipulationRank.PERFECTUS,perfectus());
    }
    private TenebrisTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    // Humilis: no torso; the head turns toward the target while the lead arm crosses the face, sweeps open, and the head follows late.
    private static CastingTrack humilis() {
        CastingPose hold=f(2,10,0,0,-98,-30,35,0,0,0,0,0), open=f(0,0,0,0,-84,20,-30,0,0,0,0,0);
        HandPose fHold=hand(-.3,.12,-.06,-12,24,16), fOpen=hand(.18,.02,-.06,-4,-16,-10);
        return track(
            seq(lin(0,Z),p(3,f(1,4,0,0,-55,8,-10,0,0,0,0,0)),out(8,hold)),
            seq(lin(0,hold),out(2,f(1,10,0,0,-88,22,-30,0,0,0,0,0)),p(4,open),lin(6,open)),
            12,f(0,0,0,0,-70,-20,24,0,0,0,0,0),10,
            seq(hlin(0,HZ),h(3,hand(.08,.06,-.02,-6,-10,-6)),hout(8,fHold)),
            seq(hlin(0,fHold),hout(2,hand(.22,.04,-.08,-6,-20,-14)),h(4,fOpen),hlin(6,fOpen)),
            12,10).build();
    }

    // Mediocritas: the torso turns away, the support arm trails behind low; on release the trailing arm leads the unfold.
    private static CastingTrack mediocritas() {
        CastingPose hold=f(2,12,0,-25,-100,-32,40,20,0,15,0,0), open=f(0,0,0,0,-86,18,-30,-72,-4,14,0,0);
        HandPose fHold=hands(-.3,.12,-.06,-12,24,16,-.05,-.22,.04,20,0,0), fOpen=hands(.18,-.04,-.06,-4,-14,-10,.26,.08,-.05,-10,-8,-6);
        return track(
            seq(lin(0,Z),p(3,f(1,4,0,-8,-50,8,-10,8,0,6,0,0)),p(6,f(2,9,0,-19,-88,-22,30,16,0,12,0,0)),out(9,hold)),
            seq(lin(0,hold),out(2,f(2,12,0,-25,-96,-26,30,-55,-12,-8,0,0)),out(4,f(1,12,0,-25,-88,20,-32,-74,-4,14,0,0)),p(6,open),lin(7,open)),
            14,f(0,0,0,0,-60,-18,22,8,0,8,0,0),11,
            seq(hlin(0,HZ),h(3,hands(.08,.06,-.02,-6,-10,-6,0,-.08,.02,8,0,0)),hout(9,fHold)),
            seq(hlin(0,fHold),hout(2,hands(-.26,.1,-.06,-10,20,14,.2,.02,-.04,-8,0,0)),
                hout(4,hands(.2,-.04,-.08,-6,-18,-12,.28,.1,-.06,-12,-10,-8)),h(6,fOpen),hlin(7,fOpen)),
            14,11).build();
    }

    // Summa: a deeper turn on an offset stance; the unfold carries the torso past square before it settles.
    private static CastingTrack summa() {
        CastingPose hold=f(3,15,3,-27,-104,-34,45,22,0,18,8,2), open=f(0,0,0,0,-86,18,-32,-72,-4,14,6,2);
        HandPose fHold=hands(-.3,.12,-.06,-12,24,16,.28,.04,-.05,-10,-20,-14), fOpen=hands(.18,-.03,-.06,-4,-14,-10,-.1,-.03,-.05,-4,12,8);
        return track(
            seq(lin(0,Z),p(3,f(1,5,1,-9,-52,8,-10,8,0,6,4,1)),p(7,f(2,11,2,-21,-92,-24,34,18,0,14,7,2)),out(10,hold)),
            seq(lin(0,hold),out(2,f(3,15,3,-27,-98,-28,32,-58,-12,-8,8,2)),out(4,f(2,15,3,-27,-90,22,-36,-76,-4,16,8,2)),
                p(6,f(0,-2,1,4,-88,20,-34,-74,-4,16,6,2)),p(8,open)),
            16,f(0,0,1,0,-62,-18,24,8,0,8,4,1),12,
            seq(hlin(0,HZ),h(3,hands(.08,.06,-.02,-6,-10,-6,-.04,-.04,.02,4,0,0)),h(7,hands(-.2,.1,-.05,-10,18,12,.18,0,-.04,-6,-12,-8)),hout(10,fHold)),
            seq(hlin(0,fHold),hout(2,hands(-.24,.1,-.06,-10,18,12,.3,.06,-.06,-12,-22,-16)),
                hout(4,hands(.2,-.02,-.08,-6,-18,-12,-.12,-.02,-.06,-4,14,10)),h(6,fOpen),hlin(8,fOpen)),
            16,12).build();
    }

    // Magister: as Summa; charging deepens the turn and the head's counter-turn; a slow sway while held.
    private static CastingTrack magister() {
        CastingPose hold=f(3,16,3,-27,-104,-34,45,22,0,18,9,3), open=f(0,0,0,0,-86,18,-34,-74,-4,14,7,3);
        HandPose fHold=hands(-.3,.12,-.06,-12,24,16,.28,.04,-.05,-10,-20,-14), fOpen=hands(.18,-.03,-.06,-4,-14,-10,-.1,-.03,-.05,-4,12,8);
        return track(
            seq(lin(0,Z),p(3,f(1,5,1,-9,-52,8,-10,8,0,6,4,1)),p(7,f(2,12,2,-21,-94,-26,36,18,0,14,8,3)),out(11,hold)),
            seq(lin(0,hold),out(2,f(3,16,3,-27,-98,-28,32,-60,-12,-8,9,3)),out(4,f(2,16,3,-27,-90,22,-38,-78,-4,16,9,3)),
                p(7,f(0,-2,1,5,-88,20,-36,-76,-4,16,7,3)),p(9,open)),
            18,f(0,0,1,0,-62,-18,24,8,0,8,4,1),13,
            seq(hlin(0,HZ),h(3,hands(.08,.06,-.02,-6,-10,-6,-.04,-.04,.02,4,0,0)),h(7,hands(-.22,.1,-.05,-10,18,12,.2,.02,-.04,-6,-14,-10)),hout(11,fHold)),
            seq(hlin(0,fHold),hout(2,hands(-.24,.1,-.06,-10,18,12,.3,.06,-.06,-12,-22,-16)),
                hout(4,hands(.2,-.02,-.08,-6,-18,-12,-.12,-.02,-.06,-4,14,10)),h(7,fOpen),hlin(9,fOpen)),
            18,13)
            // The strained lead hand stays no higher than the hold: centred under the crosshair, it must not rise into it.
            .strain(f(4,22,4,-30,-108,-38,48,26,0,20,10,3),hands(-.33,.12,-.07,-14,28,18,.31,.05,-.06,-12,-24,-16))
            .loop(f(0,-1.5,0,3,-1.5,1.5,0,1.5,0,0,0,0),hands(.012,.004,0,0,1,0,-.012,.004,0,0,-1,0),56)
            .build();
    }

    // Perfectus: still, turned 28° away, one arm crossing, head toward the target; the release is a single 1-tick 2° head turn.
    private static CastingTrack perfectus() {
        CastingPose hold=f(2,14,0,-28,-102,-34,42,18,0,16,8,2), glance=f(2,16,0,-28,-102,-34,42,18,0,16,8,2);
        HandPose fHold=hands(-.28,.1,-.06,-12,24,16,.24,-.02,-.05,-8,-18,-12);
        return track(
            seq(lin(0,Z),p(4,f(1,5,0,-10,-55,8,-10,8,0,6,4,1)),p(8,f(2,11,0,-22,-94,-26,36,15,0,13,7,2)),out(12,hold)),
            seq(lin(0,hold),lin(1,glance),lin(2,hold),lin(10,hold)),
            20,f(0,0,0,0,-62,-18,24,8,0,8,4,1),14,
            seq(hlin(0,HZ),h(4,hands(.08,.06,-.02,-6,-10,-6,-.04,-.04,.02,4,0,0)),h(8,hands(-.2,.09,-.05,-10,18,12,.18,-.01,-.04,-6,-14,-10)),hout(12,fHold)),
            seq(hlin(0,fHold),hlin(10,fHold)),
            20,14)
            .strain(f(3,18,0,-28,-106,-38,44,18,0,16,8,2),hands(-.3,.11,-.06,-14,26,18,.26,-.02,-.05,-9,-20,-13))
            .build();
    }

    /** head (pitch, yaw toward the target), lean, twist, lead arm (x, yaw, z), support arm (x, yaw, z), right/left leg spread; degrees. */
    private static CastingPose f(double hx,double hy,double lean,double twist,double rx,double ry,double rz,
                                 double lx,double ly,double lz,double rLeg,double lLeg) {
        return new CastingPose(degrees(hx,hy,0),degrees(0,twist,0),degrees(rx,ry,rz),degrees(lx,ly,lz),
                degrees(0,0,rLeg),degrees(0,0,-lLeg),(float)Math.toRadians(lean));
    }
}
