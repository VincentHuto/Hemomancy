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

/** Ferric (spec §7.7): plant, brace, drive. A smith at an anvil: the weight lowers, the lead arm rises and hammers down to a dead stop. */
public final class FerricTracks {
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
    private FerricTracks() {}
    public static CastingTrack get(EnumManipulationRank rank) { return TRACKS.get(rank); }

    // Humilis: the lead arm alone rises high, then drives straight down to chest height and stops dead.
    private static CastingTrack humilis() {
        CastingPose hold=f(5,0,-112,0,-10,0,0,0,0), stop=f(8,0,-60,-6,-8,0,0,0,0);
        HandPose fHold=hand(-.06,.2,-.04,-12,0,-6), fStop=hand(-.15,-.1,-.07,6,0,4);
        return track(
            seq(lin(0,Z),p(3,f(2,0,-72,0,-14,0,0,0,0)),out(8,hold)),
            seq(lin(0,hold),p(1,f(4,0,-118,0,-10,0,0,0,0)),lin(3,f(9,0,-56,-6,-8,0,0,0,0)),p(4,stop),lin(6,stop)),
            12,f(3,0,-40,0,-6,0,0,0,0),10,
            seq(hlin(0,HZ),h(3,hand(-.03,.08,-.02,-5,0,-3)),hout(8,fHold)),
            seq(hlin(0,fHold),h(1,hand(-.06,.23,-.03,-14,0,-6)),hlin(3,hand(-.16,-.12,-.08,7,0,4)),h(4,fStop),hlin(6,fStop)),
            12,10).build();
    }

    // Mediocritas: the support arm sets a level brace across the body first; the lead arm hammers down across it.
    private static CastingTrack mediocritas() {
        CastingPose hold=f(6,0,-114,-8,-10,-80,30,-38,0), stop=f(9,0,-60,-14,-6,-80,30,-38,0);
        HandPose fHold=hands(-.07,.2,-.04,-12,0,-6,.2,-.04,-.04,0,0,-16), fStop=hands(-.17,-.02,-.07,6,0,4,.2,-.05,-.04,0,0,-16);
        return track(
            seq(lin(0,Z),p(4,f(3,0,-70,-4,-12,-78,30,-38,0)),out(9,hold)),
            seq(lin(0,hold),p(2,f(5,0,-120,-8,-10,-80,30,-38,0)),lin(4,f(10,0,-56,-14,-6,-78,30,-36,0)),p(5,stop),lin(7,stop)),
            14,f(3,0,-45,-4,-6,-40,12,-18,0),11,
            seq(hlin(0,HZ),h(4,hands(-.03,.08,-.02,-5,0,-3,.18,-.03,-.03,0,0,-14)),hout(9,fHold)),
            seq(hlin(0,fHold),h(2,hands(-.07,.23,-.03,-14,0,-6,.2,-.04,-.04,0,0,-16)),
                hlin(4,hands(-.18,-.04,-.08,7,0,4,.2,-.07,-.04,1,0,-16)),h(5,fStop),hlin(7,fStop)),
            14,11).build();
    }

    // Summa: plant the stance and lower the waist, brace, raise; the strike stops dead and recoils upward for two ticks.
    private static CastingTrack summa() {
        CastingPose hold=f(7,5,-116,-8,-10,-82,32,-40,10), stop=f(11,6,-58,-14,-6,-80,32,-40,10);
        HandPose fHold=hands(-.07,.21,-.04,-13,0,-6,.21,-.05,-.04,0,0,-18), fStop=hands(-.18,-.03,-.08,7,0,4,.21,-.07,-.04,1,0,-18);
        return track(
            seq(lin(0,Z),p(3,f(4,4,-30,0,-8,-25,8,-10,10)),p(6,f(5,5,-80,-4,-12,-78,30,-38,10)),out(10,hold)),
            seq(lin(0,hold),p(2,f(6,5,-122,-8,-10,-82,32,-40,10)),lin(4,stop),out(5,f(10,6,-66,-14,-6,-82,32,-40,10)),p(6,stop),lin(8,stop)),
            16,f(3,2,-45,-4,-6,-40,12,-18,4),12,
            seq(hlin(0,HZ),h(3,hands(-.02,.03,-.01,-2,0,-2,.08,-.02,-.01,0,0,-6)),h(6,hands(-.05,.1,-.03,-6,0,-4,.2,-.05,-.04,0,0,-16)),hout(10,fHold)),
            seq(hlin(0,fHold),h(2,hands(-.07,.23,-.03,-14,0,-6,.21,-.05,-.04,0,0,-18)),hlin(4,fStop),
                hout(5,hands(-.17,.01,-.03,4,0,3,.21,-.05,-.03,0,0,-18)),h(6,fStop),hlin(8,fStop)),
            16,12).build();
    }

    // Magister: as Summa, wider and lower; charging climbs the hammer arm and widens the plant; a slow settling of weight while held.
    private static CastingTrack magister() {
        CastingPose hold=f(8,6,-118,-8,-10,-82,32,-40,11), stop=f(12,7,-58,-16,-6,-80,32,-40,11);
        HandPose fHold=hands(-.07,.19,-.04,-10,0,-6,.21,-.05,-.04,0,0,-18), fStop=hands(-.18,-.03,-.08,6,0,4,.21,-.07,-.04,1,0,-18);
        return track(
            seq(lin(0,Z),p(3,f(4,5,-30,0,-8,-25,8,-10,11)),p(7,f(6,6,-82,-4,-12,-80,32,-40,11)),out(11,hold)),
            seq(lin(0,hold),p(2,f(7,6,-122,-8,-10,-82,32,-40,11)),lin(5,stop),out(6,f(11,7,-67,-16,-6,-82,32,-40,11)),p(7,stop),lin(9,stop)),
            18,f(3,2,-45,-4,-6,-40,12,-18,4),13,
            seq(hlin(0,HZ),h(3,hands(-.02,.03,-.01,-2,0,-2,.08,-.02,-.01,0,0,-6)),h(7,hands(-.05,.1,-.03,-5,0,-4,.2,-.05,-.04,0,0,-16)),hout(11,fHold)),
            seq(hlin(0,fHold),h(2,hands(-.07,.21,-.03,-12,0,-6,.21,-.05,-.04,0,0,-18)),hlin(5,fStop),
                hout(6,hands(-.17,.01,-.03,3,0,3,.21,-.05,-.03,0,0,-18)),h(7,fStop),hlin(9,fStop)),
            18,13)
            .strain(f(12,7,-126,-8,-10,-82,32,-40,14),hands(-.07,.23,-.03,-13,0,-6,.21,-.06,-.04,0,0,-18))
            .loop(f(1.5,2,2,0,0,2,0,0,1),hands(0,-.005,0,0,0,0,0,-.005,0,0,0,0),60)
            .build();
    }

    // Perfectus: a monumental plant, arms low and level, still while held; the release is one 2° drop of the whole body, held 2 ticks.
    private static CastingTrack perfectus() {
        CastingPose hold=f(0,6,-60,0,-45,-60,0,45,12), drop=f(0,8,-60,0,-45,-60,0,45,12);
        HandPose fHold=hands(.12,-.08,-.02,6,0,0,-.12,-.08,-.02,6,0,0), fDrop=hands(.12,-.092,-.02,8,0,0,-.12,-.092,-.02,8,0,0);
        return track(
            seq(lin(0,Z),p(5,f(-3,1,-92,0,-22,-92,0,22,5)),out(12,hold)),
            seq(lin(0,hold),lin(1,drop),lin(3,drop),p(7,hold),lin(10,hold)),
            20,f(2,2,-35,0,-25,-35,0,25,5),14,
            seq(hlin(0,HZ),h(5,hands(.04,.06,-.04,-10,0,0,-.04,.06,-.04,-10,0,0)),hout(12,fHold)),
            seq(hlin(0,fHold),hlin(1,fDrop),hlin(3,fDrop),h(7,fHold),hlin(10,fHold)),
            20,14)
            .strain(f(2,6,-56,0,-52,-56,0,52,12),hands(.15,-.1,-.02,8,0,0,-.15,-.1,-.02,8,0,0))
            .build();
    }

    /** head pitch, lean, lead arm (x, yaw, z), support arm (x, yaw, z), stance; degrees. Ferric never twists. Arm yaw carries the arm across the body. */
    private static CastingPose f(double head,double lean,double rx,double ry,double rz,double lx,double ly,double lz,double stance) {
        return new CastingPose(degrees(head,0,0),CastingPose.Rotation.ZERO,degrees(rx,ry,rz),degrees(lx,ly,lz),
                degrees(0,0,stance),degrees(0,0,-stance),(float)Math.toRadians(lean));
    }
}
