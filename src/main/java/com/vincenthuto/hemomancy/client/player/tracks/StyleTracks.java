package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import java.util.EnumMap;
import java.util.function.UnaryOperator;
import static com.vincenthuto.hemomancy.client.player.CastingPose.degrees;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static com.vincenthuto.hemomancy.client.player.HandPose.hands;
import static com.vincenthuto.hemomancy.client.player.tracks.TrackAuthoring.*;

/** The nine older bespoke styles re-authored as full tracks (spec §10 Tier A, last row). One body doing one thing per cast. */
public final class StyleTracks {
    private static final EnumMap<CastStyle,CastingTrack> TRACKS=new EnumMap<>(CastStyle.class);
    private static final CastingPose Z=CastingPose.ZERO;
    private static final HandPose HZ=HandPose.ZERO;
    private StyleTracks() {}

    /** Cached track for the nine re-authored styles; null for every other style. */
    public static CastingTrack get(CastStyle style) {
        return style==null?null:TRACKS.computeIfAbsent(style,StyleTracks::build);
    }
    private static CastingTrack build(CastStyle style) {
        return switch(style) {
            case IRONHEART -> ironheart();
            case UMBRAL_STEP -> umbral(false);
            case UMBRAL_REVERSAL -> umbral(true);
            case AVATAR -> avatar();
            case STAFF -> staff();
            case CRIMSON_TITHE -> crimsonTithe();
            case UNCLOSING_EYE -> unclosingEye();
            case BLOOM_OF_ROT -> bloomOfRot();
            case ENDLESS_HOUR -> endlessHour();
            default -> null;
        };
    }

    // Ironhearted (Ferric Magister, charged self): plant wide, lay the support forearm level across the chest, raise the
    // hammer arm high; charge lifts it higher. Release is a one-tick cock and a straight drive onto the brace that stops dead.
    private static CastingTrack ironheart() {
        CastingPose hold=y(pose(5,5,-4,-118,-8,-88,-8,10),0,38), strain=y(pose(8,8,-6,-128,-12,-90,-10,13),0,40);
        CastingPose impact=y(pose(7,9,4,-62,0,-86,-8,11),-15,38);
        HandPose fHold=hands(-.08,.16,.02,12,0,-8,.20,-.02,-.08,0,0,-28), fStrain=hands(-.10,.18,.02,12,0,-10,.22,-.03,-.09,0,0,-30);
        HandPose fImpact=hands(-.16,-.10,-.08,-18,0,6,.20,-.06,-.08,0,0,-28);
        return track(
            seq(lin(0,Z),p(5,y(pose(2,3,-2,-78,-6,-62,-6,6),0,20)),out(12,hold)),
            seq(lin(0,hold),out(1,y(pose(5,5,-4,-120,-8,-88,-8,10),0,38)),lin(4,impact),lin(8,impact)),
            18,y(pose(3,2,0,-70,-4,-50,-4,5),0,14),12,
            seq(hlin(0,HZ),h(5,hands(-.04,.10,.02,12,0,-4,.08,-.04,-.04,0,0,-10)),hout(12,fHold)),
            seq(hlin(0,fHold),hout(1,hands(-.09,.17,.02,13,0,-9,.20,-.02,-.08,0,0,-28)),hlin(4,fImpact),hlin(8,fImpact)),
            18,12)
            .strain(strain,fStrain)
            .recovery(lin(0,impact),p(5,y(pose(4,4,1,-50,0,-60,-6,8),-8,20)),p(18,Z))
            .firstRecovery(hlin(0,fImpact),h(5,hands(-.12,-.14,-.04,-8,0,4,.14,-.10,-.04,0,0,-16)),h(18,HZ))
            .build();
    }

    // Umbral Step (Tenebris Mediocritas, quick travel): turn away with the lead arm cocked out and the support trailing,
    // then both arms cross in front (the silhouette narrows) and only afterwards does the body untwist. Reversal is the
    // exact mirror: the same departure taken from the other shoulder.
    private static CastingTrack umbral(boolean reversal) {
        UnaryOperator<CastingPose> m=reversal?CastingPose::mirror:p -> p;
        UnaryOperator<HandPose> fm=reversal?HandPose::mirror:p -> p;
        CastingPose turn=m.apply(y(pose(1,2,-9,-58,-8,10,10,3),12,0)), hold=m.apply(y(pose(0,3,-16,-76,-12,18,16,6),22,0));
        CastingPose cross=m.apply(y(pose(-2,4,-16,-96,20,-90,-20,6),-32,32)), untwist=m.apply(y(pose(-1,2,6,-90,16,-82,-16,5),-24,22));
        CastingPose end=m.apply(y(pose(0,1,0,-82,22,-58,-10,3),-10,8));
        HandPose fTurn=fm.apply(hands(.06,.02,.02,0,10,-8,-.06,-.10,.02,6,0,6)), fHold=fm.apply(hands(.12,.06,.04,0,16,-14,-.08,-.18,.05,10,0,10));
        HandPose fCross=fm.apply(hands(-.30,.08,-.06,-6,-20,12,.28,.02,-.04,-4,18,-10));
        HandPose fUntwist=fm.apply(hands(-.26,.03,-.03,-4,-20,6,.22,-.02,-.02,0,12,-6));
        HandPose fEnd=fm.apply(hands(-.28,.02,-.02,-4,-20,4,.12,-.06,0,0,6,-2));
        return track(
            seq(lin(0,Z),p(4,turn),out(8,hold)),
            seq(lin(0,hold),out(2,cross),p(4,untwist),p(6,end)),
            12,m.apply(pose(0,1,0,-60,-16,8,8,3)),10,
            seq(hlin(0,HZ),h(4,fTurn),hout(8,fHold)),
            seq(hlin(0,fHold),hout(2,fCross),h(4,fUntwist),h(6,fEnd)),
            12,10).build();
    }

    // Summon Avatar (Animus passive toggles): on draws both arms into the chest so the avatar settles into the body;
    // off throws them open past the shoulders. The manual track is an Animus contract-then-open guard.
    private static CastingTrack avatar() {
        CastingPose guard=pose(2,2,0,-88,-35,-88,35,7), open=pose(-3,-1,0,-98,-68,-98,68,9);
        HandPose fGuard=hands(-.10,.04,-.04,0,0,-10,.10,.04,-.04,0,0,10), fOpen=hands(.10,.10,-.10,-6,0,-18,-.10,.10,-.10,-6,0,18);
        return track(
            seq(lin(0,Z),p(5,pose(3,3,0,-70,18,-70,-18,4)),out(10,guard)),
            seq(lin(0,guard),out(3,pose(4,4,0,-76,12,-76,-12,7)),p(8,open)),
            16,pose(3,2,0,-55,-10,-55,10,4),10,
            seq(hlin(0,HZ),h(5,hands(-.16,-.08,.06,10,0,10,.16,-.08,.06,10,0,-10)),hout(10,fGuard)),
            seq(hlin(0,fGuard),hout(3,hands(-.18,-.04,.04,6,0,6,.18,-.04,.04,6,0,-6)),h(8,fOpen)),
            16,10)
            .on(lin(0,Z),p(6,pose(4,4,0,-72,26,-72,-26,4)),p(10,pose(3,2,0,-48,10,-48,-10,3)),p(16,Z))
            .off(lin(0,Z),p(4,pose(3,2,0,-64,14,-64,-14,3)),out(7,pose(-4,-2,0,-104,-72,-104,72,7)),p(12,Z))
            .firstOn(hlin(0,HZ),h(6,hands(-.20,-.06,.06,10,0,12,.20,-.06,.06,10,0,-12)),
                h(10,hands(-.12,-.16,.10,16,0,6,.12,-.16,.10,16,0,-6)),h(16,HZ))
            .firstOff(hlin(0,HZ),h(4,hands(-.12,-.06,.04,6,0,6,.12,-.06,.04,6,0,-6)),
                hout(7,hands(.16,.10,-.14,-8,0,-22,-.16,.10,-.14,-8,0,22)),h(12,HZ))
            .build();
    }

    // Conjure Staff and the Living Staff forms (Ferric/Animus Mediocritas, quick conjuration): the support hand closes on
    // the lead wrist, draws the haft out along the forearm, and the lead arm swings down onto the weapon's carrying line.
    private static CastingTrack staff() {
        CastingPose hold=y(pose(5,3,-4,-94,14,-74,-34,3),-8,30), carry=pose(1,1,0,-70,-15,-10,4,2);
        HandPose fHold=hands(-.14,.04,-.06,-8,0,6,.20,.02,-.04,0,0,-14), fCarry=hands(-.02,-.05,0,0,0,5,0,-.04,0,0,0,0);
        return track(
            seq(lin(0,Z),p(4,y(pose(3,2,-2,-80,6,-58,-26,2),0,22)),out(8,hold)),
            seq(lin(0,hold),out(2,y(pose(3,2,2,-88,2,-52,18,3),-4,0)),p(4,pose(1,1,3,-64,-18,-24,10,2)),p(6,carry)),
            12,pose(3,1,0,-60,4,-40,-10,1),10,
            seq(hlin(0,HZ),h(4,hands(-.06,.02,-.04,-4,0,4,.10,-.02,-.02,0,0,-8)),hout(8,fHold)),
            seq(hlin(0,fHold),hout(2,hands(-.10,.02,-.04,-6,0,6,-.04,-.08,.02,0,0,10)),
                h(4,hands(-.01,-.07,.02,4,0,4,-.02,-.10,.02,0,0,4)),h(6,fCarry)),
            12,10).build();
    }

    // Crimson Tithe (Animus Summa Saint canon): bow forward and reach, hook both arms out in a wide sweep, haul them to the
    // hips as the body rocks back; the arms then hang a beat before rest.
    private static CastingTrack crimsonTithe() {
        CastingPose reach=pose(8,7,-3,-90,-38,-86,36,7), end=pose(2,-1,1,-26,8,-24,-8,6), hang=pose(2,1,0,-6,0,-6,0,5);
        HandPose fReach=hands(-.16,.02,-.14,-14,0,6,.16,.02,-.14,-14,0,-6), fEnd=hands(-.08,-.22,.14,24,0,8,.08,-.22,.14,24,0,-8);
        HandPose fHang=hands(-.02,-.25,.08,10,0,2,.02,-.25,.08,10,0,-2);
        return track(
            seq(lin(0,Z),p(5,pose(4,4,-1,-66,-18,-58,22,4)),out(10,reach)),
            seq(lin(0,reach),out(3,pose(9,8,-4,-96,-62,-92,58,7)),p(6,pose(3,-3,2,-34,14,-30,-14,7)),p(8,end)),
            18,pose(1,-2,0,-48,-12,-44,12,4),12,
            seq(hlin(0,HZ),h(5,hands(-.08,-.02,-.08,-8,0,4,.08,-.02,-.08,-8,0,-4)),hout(10,fReach)),
            seq(hlin(0,fReach),hout(3,hands(.08,.04,-.18,-14,0,-16,-.08,.04,-.18,-14,0,16)),
                h(6,hands(-.10,-.20,.12,20,0,10,.10,-.20,.12,20,0,-10)),h(8,fEnd)),
            18,12)
            .recovery(lin(0,end),p(4,hang),lin(6,hang),p(18,Z))
            .firstRecovery(hlin(0,fEnd),h(4,fHang),hlin(6,fHang),h(18,HZ))
            .build();
    }

    // Unclosing Eye (Lux Summa Saint canon): an orant rise, a further lift, then the arms part level into the cruciform
    // with the head up and the body leaning slightly back. The cruciform keeps a small x so z carries the arms out sideways.
    private static CastingTrack unclosingEye() {
        CastingPose orant=pose(-5,-2,0,-102,-36,-102,36,6);
        HandPose fOrant=hands(-.06,.18,-.04,-9,0,-10,.06,.18,-.04,-9,0,10);
        return track(
            seq(lin(0,Z),p(5,pose(-3,-1,0,-72,-16,-72,16,3)),out(10,orant)),
            seq(lin(0,orant),out(3,pose(-7,-3,0,-114,-56,-114,56,6)),p(8,pose(-8,-3,0,-20,-90,-20,90,6))),
            16,pose(-5,-1,0,-58,-18,-58,18,3),12,
            seq(hlin(0,HZ),h(5,hands(-.04,.10,-.02,-6,0,-4,.04,.10,-.02,-6,0,4)),hout(10,fOrant)),
            seq(hlin(0,fOrant),hout(3,hands(-.02,.24,-.06,-12,0,-16,.02,.24,-.06,-12,0,16)),
                h(8,hands(.20,.16,-.02,-8,0,-28,-.20,.16,-.02,-8,0,28))),
            16,12).build();
    }

    // Bloom of Rot (Mortem Summa Saint canon): stoop and cup both hands low, then open them outward and down like a
    // flower going over, still bowed; a shallow x lets z open the arms outward instead of folding them.
    private static CastingTrack bloomOfRot() {
        CastingPose cup=pose(10,9,0,-60,8,-60,-8,8), bloom=pose(11,10,0,-35,-70,-35,70,10);
        HandPose fCup=hands(-.12,-.18,-.04,16,0,10,.12,-.18,-.04,16,0,-10), fBloom=hands(.18,-.22,-.04,8,0,-26,-.18,-.22,-.04,8,0,26);
        return track(
            seq(lin(0,Z),p(5,pose(6,5,0,-44,-12,-44,12,5)),out(10,cup)),
            seq(lin(0,cup),out(3,pose(12,11,0,-44,-58,-44,58,9)),p(8,bloom)),
            18,pose(4,2,0,-36,-10,-36,10,4),12,
            seq(hlin(0,HZ),h(5,hands(-.06,-.10,-.02,8,0,4,.06,-.10,-.02,8,0,-4)),hout(10,fCup)),
            seq(hlin(0,fCup),hout(3,hands(.10,-.22,-.06,12,0,-18,-.10,-.22,-.06,12,0,18)),h(8,fBloom)),
            18,12)
            .recovery(lin(0,bloom),p(5,pose(7,6,0,-20,-32,-20,32,7)),p(18,Z))
            .firstRecovery(hlin(0,fBloom),h(5,hands(.08,-.25,0,6,0,-10,-.08,-.25,0,6,0,10)),h(18,HZ))
            .build();
    }

    // Endless Hour (Congeatio Summa Saint canon): one decelerating close in which every part arrives on the same tick,
    // a slow tightening, a one-tick snap that then holds, and a slow linear thaw.
    private static CastingTrack endlessHour() {
        CastingPose lock=pose(6,3,0,-78,34,-78,-34,9), snap=pose(5,3,0,-90,30,-90,-30,9);
        HandPose fLock=hands(-.24,-.02,-.02,0,0,18,.24,-.02,-.02,0,0,-18), fSnap=hands(-.22,-.02,-.04,-2,0,14,.22,-.02,-.04,-2,0,-14);
        return track(
            seq(lin(0,Z),out(10,lock)),
            seq(lin(0,lock),out(4,pose(7,4,0,-84,42,-84,-42,9)),lin(5,snap),step(8,snap)),
            18,Z,10,
            seq(hlin(0,HZ),hout(10,fLock)),
            seq(hlin(0,fLock),hout(4,hands(-.28,-.03,-.03,0,0,22,.28,-.03,-.03,0,0,-22)),hlin(5,fSnap),hstep(8,fSnap)),
            18,10)
            .recovery(lin(0,snap),lin(18,Z))
            .firstRecovery(hlin(0,fSnap),hlin(18,HZ))
            .cancel(lin(0,lock),out(3,pose(3,1,0,-24,6,-24,-6,5)),p(10,Z))
            .firstCancel(hlin(0,fLock),hout(3,hands(-.08,-.18,0,8,0,4,.08,-.18,0,8,0,-4)),h(10,HZ))
            .build();
    }

    /** Adds arm yaw (degrees, vanilla sign: right negative and left positive swing the arm across the chest), which pose() leaves at zero. */
    private static CastingPose y(CastingPose p,double rightYaw,double leftYaw) {
        var r=p.rightArm(); var l=p.leftArm();
        return new CastingPose(p.head(),p.body(),degrees(Math.toDegrees(r.x()),rightYaw,Math.toDegrees(r.z())),
                degrees(Math.toDegrees(l.x()),leftYaw,Math.toDegrees(l.z())),p.rightLeg(),p.leftLeg(),p.pitch());
    }
}
