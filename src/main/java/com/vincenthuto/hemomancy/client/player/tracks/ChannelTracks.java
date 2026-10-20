package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import java.util.EnumMap;
import static com.vincenthuto.hemomancy.client.player.CastingPose.degrees;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static com.vincenthuto.hemomancy.client.player.HandPose.hand;
import static com.vincenthuto.hemomancy.client.player.HandPose.hands;
import static com.vincenthuto.hemomancy.client.player.tracks.TrackAuthoring.*;

/**
 * Channels, ground work and Magister passives. Each channel's loop speaks its school's verb rather than the plain
 * Magister breath; passives draw inward on activation and throw outward on dismissal.
 * pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance): right z positive and left z negative are inward.
 */
public final class ChannelTracks {
    private static final EnumMap<CastStyle,CastingTrack> TRACKS=new EnumMap<>(CastStyle.class);
    private static final CastingPose REST=CastingPose.ZERO;
    private static final HandPose HREST=HandPose.ZERO;
    static {
        TRACKS.put(CastStyle.GROUND_TOUCH,groundTouch());
        TRACKS.put(CastStyle.DOWSING,dowsing());
        TRACKS.put(CastStyle.FURNACE_VEINS,furnaceVeins());
        TRACKS.put(CastStyle.LIVING_CIRCUIT,livingCircuit());
        TRACKS.put(CastStyle.IRON_CHOIR,ironChoir());
        TRACKS.put(CastStyle.CARRION_COMMUNION,carrionCommunion());
        TRACKS.put(CastStyle.PENUMBRAL_DRIFT,penumbralDrift());
        TRACKS.put(CastStyle.RADIANT_WAKE,radiantWake());
        TRACKS.put(CastStyle.BLACKHEARTED,blackhearted());
        TRACKS.put(CastStyle.SOVEREIGN_INSTINCT,sovereignInstinct());
    }
    private ChannelTracks() {}
    public static CastingTrack get(CastStyle style) { return style==null?null:TRACKS.get(style); }

    /** Lignum work: the body lowers from the waist, the lead hand presses toward the ground; the loop is a slow 2 degree press. */
    private static CastingTrack groundTouch() {
        var hold=pose(10,11,0,-32,-6,8,6,6); var end=pose(6,5,0,-10,-4,4,4,4);
        var fHold=hands(-.12,-.20,-.06,21,0,8,.04,-.12,.06,10,0,-4); var fEnd=hands(-.04,-.16,.06,20,0,4,.02,-.10,.06,8,0,-2);
        return track(seq(lin(0,REST),out(4,pose(4,4,0,-55,-4,4,3,2)),p(10,hold)),
                seq(lin(0,hold),out(3,pose(12,13,0,-24,-6,10,6,6)),p(8,end)),14,pose(2,1,0,-20,-4,4,4,3),10,
                seq(h(0,HREST),h(4,hands(-.08,-.08,-.10,10,0,4,.02,-.06,.02,4,0,-2)),h(10,fHold)),
                seq(hlin(0,fHold),hout(3,hands(-.12,-.23,-.04,25,0,8,.04,-.14,.06,12,0,-4)),h(8,fEnd)),14,10)
            .loop(pose(1,2,0,2,0,0,0,0),hands(0,-.012,-.006,1.5,0,0,0,0,0,0,0,0),56).build();
    }

    /** Vascular Dowsing: the lead arm rises, drives level like a rod and the head scans with it; nothing else moves. */
    private static CastingTrack dowsing() {
        var hold=pose(4,0,0,-90,0,0,0,0); var end=pose(6,0,0,-78,0,0,0,0);
        var fHold=hand(-.16,.02,-.12,-8,0,0); var fEnd=hand(-.14,-.04,-.10,4,0,0);
        var scan=new CastingPose(degrees(0,3,0),CastingPose.Rotation.ZERO,degrees(0,3,0),
                CastingPose.Rotation.ZERO,CastingPose.Rotation.ZERO,CastingPose.Rotation.ZERO,0);
        return track(seq(lin(0,REST),out(4,pose(-2,0,0,-112,-8,0,0,0)),p(9,hold)),
                seq(lin(0,hold),out(2,pose(8,0,0,-70,0,0,0,0)),lin(4,pose(8,0,0,-70,0,0,0,0)),p(7,end)),12,pose(2,0,0,-60,0,0,0,0),10,
                seq(h(0,HREST),hout(4,hand(-.10,.14,-.06,-18,0,0)),h(9,fHold)),
                seq(hlin(0,fHold),hout(2,hand(-.16,-.08,-.12,10,0,0)),hlin(4,hand(-.16,-.08,-.12,10,0,0)),h(7,fEnd)),12,10)
            .loop(scan,hand(.015,0,0,0,3,0),48).build();
    }

    /** Flammeus channel: arms low and cocked, rocking from the hips like a bellows. */
    private static CastingTrack furnaceVeins() {
        var hold=pose(3,5,-4,-40,-28,-38,26,8); var end=pose(1,3,0,-78,-22,-76,20,8);
        var fHold=hands(.08,-.16,.06,20,0,-12,-.08,-.16,.06,20,0,12); var fEnd=hands(-.10,-.06,-.08,-6,0,2,.10,-.06,-.08,-6,0,-2);
        return track(seq(lin(0,REST),out(4,pose(1,1,-8,18,-22,14,20,5)),p(11,hold)),
                seq(lin(0,hold),out(3,pose(-3,8,2,-96,-10,-94,10,8)),p(8,end)),16,pose(-2,1,0,-30,-50,-30,50,4),12,
                seq(h(0,HREST),hout(4,hands(.14,-.10,.10,10,0,-16,-.14,-.10,.10,10,0,16)),h(11,fHold)),
                seq(hlin(0,fHold),hout(3,hands(-.18,-.02,-.14,-12,0,6,.18,-.02,-.14,-12,0,-6)),h(8,fEnd)),16,12)
            .loop(pose(-1.5,3,0,-2,0,-2,0,0),hands(0,.008,.012,1,0,0,0,.008,.012,1,0,0),52).build();
    }

    /** Ductilis channel: the hands close a circuit before the sternum and a current tics through it at period 12. */
    private static CastingTrack livingCircuit() {
        var hold=pose(2,2,0,-78,26,-78,-26,6); var end=pose(0,1,0,-60,-40,-60,40,6);
        var fHold=hands(-.24,-.06,.02,6,-8,14,.24,-.06,.02,6,8,-14); var fEnd=hands(.08,.02,-.04,-4,6,-10,-.08,.02,-.04,-4,-6,10);
        return track(seq(lin(0,REST),step(3,pose(4,0,4,-96,-20,-30,10,3)),step(6,pose(-3,1,-4,-40,16,-96,20,7)),
                        step(9,pose(3,2,0,-84,32,-84,-32,5)),p(11,hold)),
                seq(lin(0,hold),step(2,pose(-2,2,0,-100,-22,-100,22,6)),step(5,pose(-1,1,0,-68,-52,-68,52,6)),p(8,end)),16,
                pose(3,1,0,-50,12,-30,-6,3),12,
                seq(h(0,HREST),hstep(3,hands(-.12,.06,-.04,-8,0,6,0,-.04,0,0,0,0)),hstep(6,hands(-.04,-.06,0,4,0,2,.12,.06,-.04,-8,0,-6)),
                        hstep(9,hands(-.26,-.05,.02,6,-8,16,.26,-.05,.02,6,8,-16)),h(11,fHold)),
                seq(hlin(0,fHold),hstep(2,hands(-.18,.06,-.10,-10,0,4,.18,.06,-.10,-10,0,-4)),
                        hstep(5,hands(.12,.06,-.06,-6,10,-16,-.12,.06,-.06,-6,-10,16)),h(8,fEnd)),16,12)
            .loop(pose(0,0,2,0,3,0,3,0),hands(.010,0,0,0,0,2,.010,0,0,0,0,2),12).build();
    }

    /** Ferric channel: planted, arms level over the work; the weight settles 2 degrees and lifts again. */
    private static CastingTrack ironChoir() {
        var hold=pose(3,4,0,-74,-30,-74,30,11); var struck=pose(4,6,0,-56,-34,-56,34,12); var end=pose(3,5,0,-60,-32,-60,32,11);
        var fHold=hands(.10,-.08,-.04,2,0,-10,-.10,-.08,-.04,2,0,10);
        var fStruck=hands(.06,-.16,-.06,6,0,-8,-.06,-.16,-.06,6,0,8); var fEnd=hands(.07,-.14,-.05,4,0,-8,-.07,-.14,-.05,4,0,8);
        return track(seq(lin(0,REST),out(4,pose(-2,2,0,-106,-14,-106,14,6)),p(11,hold)),
                seq(lin(0,hold),out(2,pose(-1,3,0,-104,-18,-104,18,11)),p(4,struck),lin(6,struck),p(8,end)),16,
                pose(2,2,0,-40,-20,-40,20,5),12,
                seq(h(0,HREST),hout(4,hands(-.04,.14,-.04,-16,0,0,.04,.14,-.04,-16,0,0)),h(11,fHold)),
                seq(hlin(0,fHold),hout(2,hands(-.04,.15,-.05,-18,0,0,.04,.15,-.05,-18,0,0)),h(4,fStruck),hlin(6,fStruck),h(8,fEnd)),16,12)
            .loop(pose(1,2,0,2,0,2,0,1.5),hands(0,-.010,0,1,0,0,0,-.010,0,1,0,0),56).build();
    }

    /** Mortem channel: bowed, both arms hooked inward; the loop is a slow 3 degree haul toward the body. */
    private static CastingTrack carrionCommunion() {
        var hold=pose(9,9,0,-72,22,-72,-22,7); var end=pose(5,4,0,-26,8,-26,-8,6);
        var fHold=hands(-.16,-.14,-.04,16,0,12,.16,-.14,-.04,16,0,-12); var fEnd=hands(-.04,-.22,.10,26,0,4,.04,-.22,.10,26,0,-4);
        return track(seq(lin(0,REST),out(5,pose(6,6,0,-90,0,-90,0,5)),p(11,hold)),
                seq(lin(0,hold),out(3,pose(8,11,0,-90,8,-90,-8,7)),p(8,end)),16,pose(1,0,0,-40,6,-40,-6,3),12,
                seq(h(0,HREST),hout(5,hands(-.10,-.04,-.14,4,0,4,.10,-.04,-.14,4,0,-4)),h(11,fHold)),
                seq(hlin(0,fHold),hout(3,hands(-.12,-.06,-.16,6,0,6,.12,-.06,-.16,6,0,-6)),h(8,fEnd)),16,12)
            .loop(pose(1,-1.5,0,3,0,3,0,0),hands(0,-.008,.012,2,0,0,0,-.008,.012,2,0,0),60).build();
    }

    /** Tenebris channel: turned away, the lead arm crossing low and the support arm trailing; a slow 3 degree sway. */
    private static CastingTrack penumbralDrift() {
        var hold=pose(2,2,-12,-62,30,20,12,6); var end=pose(0,1,0,-76,-34,-44,16,5);
        var fHold=hands(-.26,-.06,.02,4,12,18,-.04,-.20,.08,20,0,4); var fEnd=hands(.16,.02,-.04,-6,-10,-12,.12,-.06,-.02,4,0,-8);
        return track(seq(lin(0,REST),out(4,pose(1,1,-6,-40,20,10,6,3)),p(11,hold)),
                seq(lin(0,hold),out(3,pose(0,2,-10,-86,-30,-20,10,6)),p(5,pose(-1,1,4,-82,-36,-40,14,6)),p(8,end)),16,
                pose(1,1,0,-54,24,12,10,3),12,
                seq(h(0,HREST),h(4,hands(-.14,-.03,.01,4,6,10,-.02,-.10,.04,10,0,2)),h(11,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.10,.02,-.06,-6,-10,-12,.10,-.12,.02,10,0,-6)),
                        h(5,hands(.20,.04,-.06,-8,-14,-16,.16,-.06,-.02,4,0,-10)),h(8,fEnd)),16,12)
            .loop(pose(0,0,3,-1.5,0,1.5,0,0),hands(.012,0,0,0,2,0,-.012,0,0,0,-2,0),52).build();
    }

    /** Lux channel: arms low, open and symmetric, palms forward, head lifted; the whole figure breathes upward 2 degrees. */
    private static CastingTrack radiantWake() {
        var hold=pose(-4,-2,0,-45,-40,-45,40,4); var end=pose(-3,-2,0,-60,-50,-60,50,4);
        var fHold=hands(.10,-.02,-.04,-2,0,-10,-.10,-.02,-.04,-2,0,10); var fEnd=hands(.15,.03,-.05,-2,0,-14,-.15,.03,-.05,-2,0,14);
        return track(seq(lin(0,REST),out(5,pose(-2,-1,0,-30,-15,-30,15,2)),p(11,hold)),
                seq(lin(0,hold),out(3,pose(-6,-3,0,-70,-55,-70,55,5)),p(8,end)),16,pose(-1,-1,0,-25,-20,-25,20,2),12,
                seq(h(0,HREST),h(5,hands(.04,-.06,-.02,-1,0,-4,-.04,-.06,-.02,-1,0,4)),h(11,fHold)),
                seq(hlin(0,fHold),hout(3,hands(.18,.06,-.06,-3,0,-16,-.18,.06,-.06,-3,0,16)),h(8,fEnd)),16,12)
            .loop(pose(-1,-1,0,-2,-1,-2,1,0),hands(0,.012,0,0,0,0,0,.012,0,0,0,0),44).build();
    }

    /** Mortem passive: on reaches out and hauls the blackened heart into the chest; off throws the arms wide open. */
    private static CastingTrack blackhearted() {
        var hold=pose(8,6,0,-62,30,-62,-30,6); var end=pose(6,4,0,-40,20,-40,-20,5);
        var hauled=pose(9,7,0,-62,34,-62,-34,6); var thrown=pose(-3,-2,0,-50,-62,-50,62,6);
        var fHold=hands(-.20,-.10,.06,12,-6,18,.20,-.10,.06,12,6,-18); var fEnd=hands(-.12,-.14,.06,14,-4,10,.12,-.14,.06,14,4,-10);
        var fHauled=hands(-.22,-.10,.08,14,-8,20,.22,-.10,.08,14,8,-20); var fThrown=hands(.24,.06,-.06,-10,14,-24,-.24,.06,-.06,-10,-14,24);
        return track(seq(lin(0,REST),out(4,pose(4,3,0,-78,0,-78,0,4)),p(10,hold)),
                seq(lin(0,hold),out(3,pose(10,8,0,-56,36,-56,-36,7)),p(8,end)),16,pose(2,1,0,-30,6,-30,-6,3),12,
                seq(h(0,HREST),hout(4,hands(-.06,.02,-.10,-6,0,4,.06,.02,-.10,-6,0,-4)),h(10,fHold)),
                seq(hlin(0,fHold),hout(3,hands(-.24,-.12,.10,16,-8,22,.24,-.12,.10,16,8,-22)),h(8,fEnd)),16,12)
            .on(lin(0,REST),out(4,pose(4,4,0,-80,-4,-80,4,4)),p(9,hauled),lin(11,hauled),p(16,REST))
            .off(lin(0,REST),out(3,thrown),lin(5,thrown),p(12,REST))
            .firstOn(hlin(0,HREST),hout(4,hands(-.06,.02,-.10,-6,0,4,.06,.02,-.10,-6,0,-4)),h(9,fHauled),hlin(11,fHauled),h(16,HREST))
            .firstOff(hlin(0,HREST),hout(3,fThrown),hlin(5,fThrown),h(12,HREST)).build();
    }

    /** Ductilis passive: on clenches inward in three staccato steps; off snaps open in two. */
    private static CastingTrack sovereignInstinct() {
        var hold=pose(5,2,0,-66,34,-66,-34,5); var end=pose(0,0,0,-52,-36,-52,36,5);
        var clench=pose(7,3,0,-68,38,-68,-38,6);
        var fHold=hands(-.22,-.08,.06,12,-8,18,.22,-.08,.06,12,8,-18); var fEnd=hands(.12,.04,-.04,-6,8,-14,-.12,.04,-.04,-6,-8,14);
        var fClench=hands(-.24,-.10,.08,14,-8,20,.24,-.10,.08,14,8,-20);
        return track(seq(lin(0,REST),step(3,pose(3,0,0,-64,28,-20,4,2)),step(6,pose(4,1,0,-60,30,-64,-28,4)),p(10,hold)),
                seq(lin(0,hold),step(2,pose(-2,0,0,-96,-20,-96,20,6)),step(5,pose(-1,0,0,-62,-48,-62,48,6)),p(8,end)),16,
                pose(3,0,0,-40,12,-34,-6,2),12,
                seq(h(0,HREST),hstep(3,hands(-.16,-.06,.04,8,-6,14,0,0,0,0,0,0)),hstep(6,hands(-.18,-.07,.05,10,-6,16,.18,-.07,.05,10,6,-16)),h(10,fHold)),
                seq(hlin(0,fHold),hstep(2,hands(-.16,.08,-.10,-10,0,4,.16,.08,-.10,-10,0,-4)),
                        hstep(5,hands(.16,.06,-.06,-8,10,-18,-.16,.06,-.06,-8,-10,18)),h(8,fEnd)),16,12)
            .on(lin(0,REST),step(3,pose(3,0,0,-62,30,-20,4,2)),step(6,pose(5,1,0,-62,32,-62,-32,4)),step(9,clench),lin(12,clench),p(16,REST))
            .off(lin(0,REST),step(2,pose(-2,0,0,-50,-56,-50,56,5)),step(5,pose(-3,-1,0,-46,-66,-46,66,6)),p(12,REST))
            .firstOn(hlin(0,HREST),hstep(3,hands(-.16,-.06,.04,8,-6,14,0,0,0,0,0,0)),hstep(6,hands(-.20,-.08,.06,12,-8,18,.20,-.08,.06,12,8,-18)),
                    hstep(9,fClench),hlin(12,fClench),h(16,HREST))
            .firstOff(hlin(0,HREST),hstep(2,hands(.18,.06,-.06,-8,10,-18,-.18,.06,-.06,-8,-10,18)),
                    hstep(5,hands(.24,.08,-.08,-10,14,-24,-.24,.08,-.08,-10,-14,24)),h(12,HREST)).build();
    }
}
