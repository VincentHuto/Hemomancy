package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import java.util.EnumMap;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static com.vincenthuto.hemomancy.client.player.HandPose.hands;
import static com.vincenthuto.hemomancy.client.player.tracks.TrackAuthoring.*;

/**
 * Bespoke tracks whose shape is fixed by gameplay: the Riposte parry arm, the Blood Rush dash, the Deadly Gaze stare
 * and the Marionette crossbar. pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): right z positive and
 * left z negative are inward.
 */
public final class GameplayTracks {
    private static final EnumMap<CastStyle,CastingTrack> TRACKS=new EnumMap<>(CastStyle.class);
    private static final CastingPose REST=CastingPose.ZERO;
    private static final HandPose HREST=HandPose.ZERO;
    static {
        TRACKS.put(CastStyle.RIPOSTE,riposte());
        TRACKS.put(CastStyle.BLOOD_RUSH,bloodRush());
        TRACKS.put(CastStyle.DEADLY_GAZE,deadlyGaze());
        TRACKS.put(CastStyle.MARIONETTE,marionette());
    }
    private GameplayTracks() {}
    public static CastingTrack get(CastStyle style) { return style==null?null:TRACKS.get(style); }

    /**
     * Guard: the support forearm across the chest, the lead arm raised over it. Release follows the Riposte arm:
     * emerge 0-2 (a tighter cock inward), sweep 2-6 outward; the strike (6-8) carries into recovery.
     */
    private static CastingTrack riposte() {
        var guard=pose(2,2,0,-108,24,-70,-36,4); var sweep=pose(-1,1,0,-82,-40,-62,-18,4);
        var fGuard=hands(-.20,.10,-.04,-18,-6,22,.20,-.06,.02,12,8,-24); var fSweep=hands(.18,.04,-.10,-8,10,-16,.10,-.08,.02,10,4,-14);
        return track(seq(p(0,REST),out(4,pose(1,1,0,-70,14,-45,-22,2)),p(9,guard)),
                seq(lin(0,guard),out(2,pose(2,2,0,-110,30,-72,-38,4)),out(6,sweep)),14,pose(3,1,0,-60,10,-40,-18,2),10,
                seq(h(0,HREST),h(4,hands(-.10,.04,-.02,-8,-2,10,.10,-.03,.01,6,4,-12)),h(9,fGuard)),
                seq(hlin(0,fGuard),hout(2,hands(-.22,.11,-.05,-20,-8,26,.21,-.06,.02,12,8,-25)),hout(6,fSweep)),14,10)
            .recovery(lin(0,sweep),out(2,pose(-1.5,1,0,-80,-46,-58,-14,4)),p(14,REST))
            .firstRecovery(hlin(0,fSweep),hout(2,hands(.21,.03,-.11,-8,11,-18,.08,-.08,.02,8,3,-10)),h(14,HREST)).build();
    }

    /** Both arms pump back once with a 6 degree lean into the dash; recovery is 8 ticks so the dash owns the body. */
    private static CastingTrack bloodRush() {
        var coil=pose(2,3,0,-34,-6,-30,6,0); var pumped=pose(-1,6,0,38,-10,34,10,0); var end=pose(0,4,0,14,-6,12,6,0);
        var fCoil=hands(-.08,-.06,-.04,-10,0,6,.08,-.06,-.04,-10,0,-6); var fPump=hands(.08,-.20,.14,24,0,-8,-.08,-.20,.14,24,0,8);
        var fEnd=hands(.05,-.12,.08,14,0,-4,-.05,-.12,.08,14,0,4);
        return track(seq(p(0,REST),out(3,pose(1,2,0,-20,-4,-18,4,0)),p(8,coil)),
                seq(lin(0,coil),out(2,pumped),p(6,end)),8,pose(1,1,0,-14,-2,-12,2,0),10,
                seq(h(0,HREST),h(3,hands(-.04,-.03,-.02,-5,0,3,.04,-.03,-.02,-5,0,-3)),h(8,fCoil)),
                seq(hlin(0,fCoil),hout(2,fPump),h(6,fEnd)),8,10).build();
    }

    /** Arms never move: the head counter-jerks onto the target, bows further with charge, and snaps up in one tick. */
    private static CastingTrack deadlyGaze() {
        var hold=head(6,0); var snap=head(-3,0); var end=head(2,0);
        return track(seq(lin(0,REST),step(3,head(2.5,4)),step(6,head(5,-3)),out(8,hold)),
                seq(lin(0,hold),step(1,snap),lin(4,snap),p(7,end)),12,head(3,0),10,
                seq(h(0,HREST),h(8,HREST)),seq(hlin(0,HREST),h(7,HREST)),12,10)
            .strain(head(13,0),HREST).build();
    }

    /**
     * Puppeteer: both hands raised at chest height, the carriage fingers-down; the loop lifts one hand as the other
     * drops so the crossbar walks. First-person hands stay high and barely roll so the held bar reads.
     */
    private static CastingTrack marionette() {
        var hold=pose(4,1,0,-80,-15,-80,15,3); var end=pose(1,0,0,-62,-12,-62,12,3);
        var fHold=hands(-.10,.06,-.04,-6,0,4,.12,.04,-.02,-6,0,-4); var fEnd=hands(-.06,.02,-.02,-2,0,2,.08,0,0,-2,0,-2);
        return track(seq(lin(0,REST),step(3,pose(2,0,0,-62,-10,-30,6,1)),step(6,pose(3,1,0,-38,-6,-70,14,2)),p(10,hold)),
                seq(lin(0,hold),step(2,pose(1,1,0,-96,-14,-96,14,3)),step(5,pose(0,0,0,-74,-32,-74,32,3)),p(8,end)),14,pose(3,0,0,-50,-10,-50,10,2),11,
                seq(h(0,HREST),hstep(3,hands(-.05,.02,-.02,-3,0,2,.04,0,0,-1,0,-1)),hstep(6,hands(-.06,.03,-.02,-3,0,2,.10,.03,-.02,-5,0,-3)),h(10,fHold)),
                seq(hlin(0,fHold),hstep(2,hands(-.10,.12,-.06,-10,0,4,.12,.10,-.04,-10,0,-4)),hstep(5,hands(.04,.08,-.04,-6,0,-6,-.02,.06,-.02,-6,0,6)),h(8,fEnd)),14,11)
            .loop(pose(0,0,0,-3,0,3,0,0),hands(0,.012,0,-1,0,0,0,-.012,0,1,0,0),48).build();
    }

    /** Head-only pose: x is the nod (positive down), y the explicit yaw; body, arms and legs at rest. */
    private static CastingPose head(double x,double y) {
        return new CastingPose(CastingPose.degrees(x,y,0),CastingPose.Rotation.ZERO,CastingPose.Rotation.ZERO,
                CastingPose.Rotation.ZERO,CastingPose.Rotation.ZERO,CastingPose.Rotation.ZERO,0);
    }
}
