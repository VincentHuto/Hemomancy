package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import java.util.EnumMap;
import static com.vincenthuto.hemomancy.client.player.CastingPose.degrees;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static com.vincenthuto.hemomancy.client.player.HandPose.hands;
import static com.vincenthuto.hemomancy.client.player.tracks.TrackAuthoring.*;

/**
 * The eight Perfectus iconics (spec §7, §10). The body takes one monumental shape and holds it dead still; the anatomy
 * around it does the grand motion. Strain is the only thing charge moves. Synaptic Storm alone carries a hold loop.
 * Arm x applies before z, so z near x −90 is pure roll: arms spread sideways at shoulder height use a small x and a large z.
 */
public final class PerfectusTracks {
    private static final EnumMap<CastStyle,CastingTrack> TRACKS=new EnumMap<>(CastStyle.class);
    static {
        TRACKS.put(CastStyle.CRIMSON_CORONATION,crimsonCoronation());
        TRACKS.put(CastStyle.SYNAPTIC_STORM,synapticStorm());
        TRACKS.put(CastStyle.WHITE_VERDICT,whiteVerdict());
        TRACKS.put(CastStyle.FUNERAL_BELL,funeralBell());
        TRACKS.put(CastStyle.ABSOLUTE_STILLNESS,absoluteStillness());
        TRACKS.put(CastStyle.LODESTONE_COLLAPSE,lodestoneCollapse());
        TRACKS.put(CastStyle.ECLIPSE_WELL,eclipseWell());
        TRACKS.put(CastStyle.PHOENIX_DEBT,phoenixDebt());
    }
    private PerfectusTracks() {}
    public static CastingTrack get(CastStyle style) { return style==null?null:TRACKS.get(style); }

    // Animus, charged. Contract to the sternum, rise into a crown held before the brow; release opens it to the cruciform guard
    // on one 2° heartbeat drop. Arms stop at x −128 (crown limit) rather than the sketch's −150: |arm x| is capped at 130.
    private static CastingTrack crimsonCoronation() {
        var crown=sym(-6,0,-118,22,6); var tight=sym(-10,0,-128,28,7);
        var beat=sym(0,2,-45,-70,7); var guard=sym(0,0,-45,-70,7);
        var fCrown=sym(-.10,.20,-.06,-27,0,18); var fTight=sym(-.15,.24,-.08,-34,0,24);
        var fBeat=sym(.30,.06,-.04,-5,0,-14); var fGuard=sym(.32,.04,-.02,-4,0,-12);
        return track(seq(lin(0,CastingPose.ZERO),p(5,sym(3,2,-60,30,3)),out(12,crown)),
                seq(lin(0,crown),out(4,beat),p(10,guard)),20,sym(3,2,-70,10,3),14,
                seq(hlin(0,HandPose.ZERO),h(5,sym(-.20,-.08,.06,9,0,10)),hout(12,fCrown)),
                seq(hlin(0,fCrown),hout(4,fBeat),h(10,fGuard)),20,14)
            .strain(tight,fTight).build();
    }

    // Ductilis, charged. Three staccato steps lock into a rigid symmetric hold; strain widens it; the hold carries the one
    // permitted tic (the loop is a sinusoid, so it reads as a 2-beat flutter rather than a true step). Release: one snap wider and back.
    private static CastingTrack synapticStorm() {
        var rigid=sym(0,0,-25,-60,6); var wide=sym(-2,0,-27,-68,7); var snap=sym(-4,-2,-30,-75,9);
        var fRigid=sym(-.24,.06,-.08,-6,0,-6); var fWide=sym(-.20,.08,-.10,-7,0,-10); var fSnap=sym(-.12,.10,-.12,-8,0,-16);
        return track(seq(lin(0,CastingPose.ZERO),step(4,pose(4,0,-6,-55,-20,-25,10,4)),step(8,pose(-3,0,5,-35,-45,-60,50,7)),step(12,rigid)),
                seq(lin(0,rigid),step(1,snap),step(3,rigid),lin(9,rigid)),18,rigid,13,
                seq(hlin(0,HandPose.ZERO),hstep(4,hands(-.10,-.05,0,0,0,10,.05,-.10,0,0,0,0)),
                        hstep(8,hands(-.18,.04,-.04,-4,0,-4,.22,.02,-.06,-6,0,8)),hstep(12,fRigid)),
                seq(hlin(0,fRigid),hstep(1,fSnap),hstep(2,fRigid),hlin(9,fRigid)),18,13)
            .strain(wide,fWide).loop(pose(0,0,0,0,-3,0,3,0),sym(.01,0,0,0,0,-2),12)
            // The circuit collapses: arms drop in two uneven steps, then rest.
            .cancel(lin(0,rigid),step(3,pose(3,0,4,-20,-35,-45,50,5)),step(7,pose(5,0,-2,-15,-15,-25,25,3)),p(13,CastingPose.ZERO))
            .firstCancel(hlin(0,fRigid),hstep(3,hands(-.05,-.06,0,3,0,-4,.16,.02,-.04,-4,0,6)),
                    hstep(7,hands(.02,-.12,.02,4,0,0,.04,-.08,0,1,0,2)),h(13,HandPose.ZERO))
            .build();
    }

    // Lux, charged. Rise in front, part into the full level cruciform with the head lifted 8°; the head keeps climbing with
    // charge. Release lifts the whole upper body 2° back and settles.
    private static CastingTrack whiteVerdict() {
        var orant=sym(-8,0,-20,-90,6); var exalt=sym(-18,0,-22,-90,6); var lift=sym(-10,-2,-24,-92,6);
        var fOrant=sym(.33,.10,-.04,-2,0,-20); var fExalt=sym(.33,.16,-.06,-4,0,-24); var fLift=sym(.33,.14,-.06,-3,0,-22);
        return track(seq(lin(0,CastingPose.ZERO),p(5,sym(-3,0,-95,-25,3)),out(12,orant)),
                seq(lin(0,orant),out(3,lift),p(10,orant)),20,sym(-7,0,-15,-40,3),14,
                seq(hlin(0,HandPose.ZERO),h(5,sym(-.08,.08,-.06,-3,0,-4)),hout(12,fOrant)),
                seq(hlin(0,fOrant),hout(3,fLift),h(10,fOrant)),20,14)
            .strain(exalt,fExalt).build();
    }

    // Mortem, charged. Bowed 10°, head down 12°, arms hanging slightly forward; strain deepens the bow to 14°. Release swings
    // the body upright once like a bell (arms lag forward), then back into the bow; recovery rises from it.
    private static CastingTrack funeralBell() {
        var bow=sym(12,10,-20,-6,5); var deep=sym(15,14,-24,-4,6); var swing=sym(2,0,-34,-8,5);
        var fBow=sym(-.06,-.20,.02,15,0,-8); var fDeep=sym(-.08,-.24,.04,25,0,-4); var fSwing=sym(-.08,-.08,-.06,0,0,-10);
        return track(seq(lin(0,CastingPose.ZERO),p(5,sym(5,4,-30,-10,3)),out(12,bow)),
                seq(lin(0,bow),out(4,swing),p(10,bow)),20,sym(3,1,-8,-3,3),14,
                seq(hlin(0,HandPose.ZERO),h(5,sym(-.04,-.10,-.04,0,0,4)),hout(12,fBow)),
                seq(hlin(0,fBow),hout(4,fSwing),h(10,fBow)),20,14)
            .strain(deep,fDeep).build();
    }

    // Congeatio, continuous. One ease-out contraction into arms half-raised and crossed, head bowed; every part stops on the
    // same tick and nothing moves again (no loop). Release is a 1-tick 1° settle; recovery thaws linearly; cancel fractures.
    private static CastingTrack absoluteStillness() {
        var locked=turned(10,0,0,3,-70,-40,10,-70,40,-10,6); var settled=turned(11,0,0,4,-70,-40,10,-70,40,-10,6); var broken=pose(4,1,0,-12,8,-12,-8,2);
        var fLocked=sym(-.28,.04,-.04,-6,-14,24); var fSettled=sym(-.28,.03,-.04,-5,-14,24);
        return track(seq(lin(0,CastingPose.ZERO),out(12,locked)),
                seq(lin(0,locked),step(1,settled),lin(8,settled)),18,locked,13,
                seq(hlin(0,HandPose.ZERO),hout(12,fLocked)),
                seq(hlin(0,fLocked),hstep(1,fSettled),hlin(8,fSettled)),18,13)
            .recovery(lin(0,settled),lin(18,CastingPose.ZERO))
            .firstRecovery(hlin(0,fSettled),hlin(18,HandPose.ZERO))
            .cancel(lin(0,locked),lin(3,broken),p(13,CastingPose.ZERO))
            .firstCancel(hlin(0,fLocked),hlin(3,sym(-.06,-.14,.02,8,0,4)),h(13,HandPose.ZERO))
            .build();
    }

    // Ferric, charged. Plant the legs, then lower into the monumental stance: arms low and level, body down 6°. Strain lifts the
    // arms 10° against the weight. Release drops everything, holds the drop 2 ticks, and braces.
    private static CastingTrack lodestoneCollapse() {
        var plant=sym(0,6,-60,-45,10); var heave=sym(-2,6,-70,-42,11); var drop=sym(2,8,-50,-45,10); var brace=sym(1,7,-55,-45,10);
        var fPlant=sym(.30,-.12,0,0,0,-4); var fHeave=sym(.30,-.06,-.02,-4,0,-4);
        var fDrop=sym(.30,-.18,.02,4,0,-4); var fBrace=sym(.30,-.15,.01,2,0,-4);
        return track(seq(lin(0,CastingPose.ZERO),p(5,sym(2,3,-30,-25,10)),out(12,plant)),
                seq(lin(0,plant),out(3,drop),lin(5,drop),p(9,brace)),20,sym(2,3,-25,-30,4),14,
                seq(hlin(0,HandPose.ZERO),h(5,sym(.12,-.06,0,0,0,-2)),hout(12,fPlant)),
                seq(hlin(0,fPlant),hout(3,fDrop),hlin(5,fDrop),h(9,fBrace)),20,14)
            .strain(heave,fHeave).build();
    }

    // Tenebris, charged. Turned 20° away, lead arm crossing, support arm trailing low behind, head counter-rotated toward the
    // target; strain moves only the head. Release is a 1-tick 2° head turn; nothing else on the body moves.
    private static CastingTrack eclipseWell() {
        var veiled=turned(2,10,-20,0,-70,-40,10,15,0,10,4); var watching=turned(2,16,-20,0,-70,-40,10,15,0,10,4);
        var turn=turned(2,12,-20,0,-70,-40,10,15,0,10,4);
        var fVeiled=hands(-.30,.06,-.04,-8,-12,18,.28,-.12,0,6,10,-12);
        var fWatching=hands(-.32,.07,-.05,-9,-14,20,.30,-.13,0,6,12,-12);
        var fTurn=hands(-.30,.06,-.04,-8,-16,20,.28,-.12,0,6,10,-12);
        return track(seq(lin(0,CastingPose.ZERO),p(5,turned(1,4,-10,0,-50,-20,10,5,0,6,2)),out(12,veiled)),
                seq(lin(0,veiled),step(1,turn),lin(9,turn)),18,veiled,14,
                seq(hlin(0,HandPose.ZERO),h(5,hands(-.16,0,0,-4,-6,8,.14,-.06,0,2,4,-6)),hout(12,fVeiled)),
                seq(hlin(0,fVeiled),hstep(1,fTurn),hlin(9,fTurn)),18,14)
            .strain(watching,fWatching)
            // Folds into alignment: the body untwists before the arms lower.
            .cancel(lin(0,veiled),p(5,turned(1,0,0,0,-70,-40,10,5,0,6,2)),p(14,CastingPose.ZERO))
            .build();
    }

    // Flammeus, passive. Manual activation: planted, arms low and wide, head up, with one hard 4° forward pitch on release.
    // On folds both arms across the chest (the debt settles in); off throws them open.
    private static CastingTrack phoenixDebt() {
        var inferno=sym(-6,0,-45,-55,9); var expel=sym(-4,4,-52,-55,9);
        var fold=turned(5,0,0,2,-65,-40,15,-65,40,-15,4); var thrown=sym(-8,-2,-70,-75,6);
        var fInferno=sym(.22,-.14,0,8,0,-20); var fExpel=sym(.22,-.25,.02,6,0,-20);
        var fFold=sym(-.12,-.18,.06,4,12,20); var fThrown=sym(.32,.05,-.10,-10,0,-25);
        return track(seq(lin(0,CastingPose.ZERO),p(4,sym(2,2,-30,-20,6)),out(10,inferno)),
                seq(lin(0,inferno),out(2,expel),p(9,inferno)),18,sym(-3,0,-30,-70,5),12,
                seq(hlin(0,HandPose.ZERO),h(4,sym(.06,-.08,.02,4,0,-8)),hout(10,fInferno)),
                seq(hlin(0,fInferno),hout(2,fExpel),h(9,fInferno)),18,12)
            .on(lin(0,CastingPose.ZERO),p(6,fold),lin(10,fold),p(16,CastingPose.ZERO))
            .off(lin(0,CastingPose.ZERO),out(4,thrown),p(12,CastingPose.ZERO))
            .firstOn(hlin(0,HandPose.ZERO),h(6,fFold),hlin(10,fFold),h(16,HandPose.ZERO))
            .firstOff(hlin(0,HandPose.ZERO),hout(4,fThrown),h(12,HandPose.ZERO))
            .build();
    }

    /** Symmetric body: right arm as given, left arm mirrored (positive right z is inward). */
    private static CastingPose sym(double head,double lean,double rightX,double rightZ,double stance) {
        return pose(head,lean,0,rightX,rightZ,rightX,-rightZ,stance);
    }
    /** Symmetric hands: support mirrors the lead. */
    private static HandPose sym(double tx,double ty,double tz,double rx,double ry,double rz) {
        var lead=HandPose.hand(tx,ty,tz,rx,ry,rz).lead();
        return new HandPose(lead,lead.mirror());
    }
    /**
     * Explicit head yaw (no automatic counter-yaw) and arm yaw. Arms that cross the chest need yaw toward the centre
     * (right negative, left positive): at a moderate x, z alone only rolls the arm.
     */
    private static CastingPose turned(double headX,double headY,double twist,double lean,double rightX,double rightYaw,double rightZ,
                                      double leftX,double leftYaw,double leftZ,double stance) {
        return new CastingPose(degrees(headX,headY,0),degrees(0,twist,0),degrees(rightX,rightYaw,rightZ),degrees(leftX,leftYaw,leftZ),
                degrees(0,0,stance),degrees(0,0,-stance),(float)Math.toRadians(lean));
    }
}
