package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Segment;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import java.util.EnumMap;
import static com.vincenthuto.hemomancy.client.player.CastingPose.degrees;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static com.vincenthuto.hemomancy.client.player.HandPose.hand;
import static com.vincenthuto.hemomancy.client.player.HandPose.hands;
import static com.vincenthuto.hemomancy.client.player.tracks.TrackAuthoring.*;

/**
 * Unstained Still Arts: a figure standing in still water. Slow, symmetric, level; the arms only ever open, the head is
 * level or bowed, and the long settle ends at rest with zero velocity so the stillness is the memorable frame.
 * The server publishes RELEASE only, so the hold is a low readiness the release rises out of, keeping the first frame
 * close to rest. pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): positive head bows, negative lean straightens.
 */
public final class StillArtTracks {
    private static final CastingPose Z=CastingPose.ZERO;
    private static final HandPose HZ=HandPose.ZERO;
    private static final int OPENING=2,CANCEL=10;
    private static final EnumMap<EnumClarityStage,CastingTrack> TRACKS=new EnumMap<>(EnumClarityStage.class);
    static {
        TRACKS.put(EnumClarityStage.AWAKENED,awakened());
        TRACKS.put(EnumClarityStage.DISCERNING,discerning());
        TRACKS.put(EnumClarityStage.VIGILANT,vigilant());
        TRACKS.put(EnumClarityStage.RESOLUTE,resolute());
        TRACKS.put(EnumClarityStage.ENLIGHTENED,enlightened());
    }
    private StillArtTracks() {}
    public static CastingTrack get(EnumClarityStage stage) { return TRACKS.get(stage); }

    /** Lead arm alone lifts to a level open palm at chest height; the head stays level. */
    private static CastingTrack awakened() {
        var hold=pose(0,0,0,-20,-4,0,0,0); var fHold=hand(-.04,.03,-.02,-4,0,-2);
        return still(hold,seq(lin(0,hold),out(5,pose(0,0,0,-72,-8,0,0,0)),p(8,pose(0,0,0,-75,-10,0,0,0))),14,
                fHold,seq(hlin(0,fHold),hout(5,hand(-.26,.10,-.06,-14,0,-4)),h(8,hand(-.28,.11,-.06,-15,0,-4))));
    }

    /** Both arms lift to level open palms, shoulders square, head level. */
    private static CastingTrack discerning() {
        var hold=pose(0,0,0,-20,-6,-20,6,0); var fHold=sym(-.04,.03,-.02,-4,-2);
        return still(hold,seq(lin(0,hold),out(5,pose(0,0,0,-70,-18,-70,18,0)),p(9,pose(0,0,0,-72,-20,-72,20,0))),16,
                fHold,seq(hlin(0,fHold),hout(5,sym(-.14,.09,-.05,-12,-5)),h(9,sym(-.15,.10,-.05,-12,-6))));
    }

    /** Both arms lift while the body straightens, feet come together and the head bows. */
    private static CastingTrack vigilant() {
        var hold=pose(2,-1,0,-22,-8,-22,8,0); var fHold=sym(-.04,.04,-.02,-5,-2);
        return still(hold,seq(lin(0,hold),out(6,pose(4,-2,0,-78,-22,-78,22,0)),p(10,pose(4,-2,0,-80,-24,-80,24,0))),18,
                fHold,seq(hlin(0,fHold),hout(6,sym(-.15,.11,-.06,-14,-6)),h(10,sym(-.15,.12,-.06,-15,-6))));
    }

    /** Arms rise to shoulder height, forward and out, palms forward; the release ends in a single 2 degree lowering, a bell damping. */
    private static CastingTrack resolute() {
        var hold=yawed(2,-1,-22,10,-6,0); var fHold=sym(-.02,.05,-.02,-6,-3);
        return still(hold,seq(lin(0,hold),out(7,yawed(4,-2,-88,35,-6,0)),p(11,yawed(4,-2,-86,35,-6,0))),20,
                fHold,seq(hlin(0,fHold),hout(7,sym(.02,.17,-.05,-16,-10)),h(11,sym(.02,.15,-.05,-14,-10))));
    }

    /** A level open stance with the body perfectly still; the world moves around it. Arrives by tick 8, then nothing moves. */
    private static CastingTrack enlightened() {
        var hold=pose(0,0,0,-14,-14,-14,14,0); var open=pose(0,0,0,-20,-60,-20,60,0);
        var fHold=sym(-.02,.04,-.02,-5,-4); var fOpen=sym(.08,.10,-.04,-12,-18);
        return still(hold,seq(lin(0,hold),out(6,pose(0,0,0,-20,-57,-20,57,0)),p(8,open),lin(12,open)),24,
                fHold,seq(hlin(0,fHold),hout(6,sym(.07,.10,-.04,-12,-17)),h(8,fOpen),hlin(12,fOpen)));
    }

    /** Opening rises from rest in two ticks; the settle lands 99% of the way home two ticks early so it ends without velocity. */
    private static CastingTrack still(CastingPose hold,Segment<CastingPose> release,int settle,HandPose fHold,Segment<HandPose> fRelease) {
        var end=release.last(Z); var fEnd=fRelease.last(HZ);
        return track(seq(lin(0,Z),out(OPENING,hold)),release,settle,hold.blend(Z,.5F),CANCEL,
                seq(hlin(0,HZ),hout(OPENING,fHold)),fRelease,settle,CANCEL)
            .recovery(lin(0,end),p(settle-2,end.blend(Z,.99F)),p(settle,Z))
            .firstRecovery(hlin(0,fEnd),h(settle-2,fEnd.blend(HZ,.99F)),h(settle,HZ))
            .build();
    }
    /** Mirrored first-person hands, both level at ty: positive tx moves each hand outward, negative toward the centre. */
    private static HandPose sym(double tx,double ty,double tz,double rx,double rz) {
        return hands(tx,ty,tz,rx,0,rz, -tx,ty,tz,rx,0,-rz);
    }
    /** Both arms forward with arm yaw (positive right yaw swings the lead arm outward) so the spread stays at shoulder height. */
    private static CastingPose yawed(double head,double lean,double x,double yaw,double z,double stance) {
        return new CastingPose(degrees(head,0,0),CastingPose.Rotation.ZERO,degrees(x,yaw,z),degrees(x,-yaw,-z),
                degrees(0,0,stance),degrees(0,0,-stance),(float)Math.toRadians(lean));
    }
}
