package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.player.tracks.AimLayers;
import com.vincenthuto.hemomancy.client.player.tracks.CastingTracks;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static org.junit.jupiter.api.Assertions.*;

class CastingPoseIntegrationTest {
    @Test void locomotionOwnsLegsWhileMovingOrUsingSpecialPostures() {
        assertEquals(1,CastingPlayerPose.stanceWeight(true,false,0,0));
        assertEquals(0,CastingPlayerPose.stanceWeight(true,false,.1F,0));
        assertEquals(0,CastingPlayerPose.stanceWeight(true,false,0,.5F));
        assertEquals(0,CastingPlayerPose.stanceWeight(false,false,0,0));
        assertEquals(0,CastingPlayerPose.stanceWeight(true,true,0,0));
    }
    @Test void differentPurposesDoNotEraseSchoolIdentity() {
        var distinct=new java.util.HashSet<CastingPose>();
        for(var school:EnumBloodTendency.values()) {
            var t=CastingTracks.base(school,EnumManipulationRank.MEDIOCRITAS);
            var layer=AimLayers.purpose(CastPurpose.PROJECTILE);
            distinct.add(AimLayerRules.apply(sample(t,CastPhase.RELEASE,2,0,false),layer.third().sample(2,CastingPose.ZERO),1));
        }
        assertEquals(8,distinct.size());
    }
    @Test void newQuickCastReplacesRecoveryWithoutAccumulating() {
        var t=CastingTracks.base(EnumBloodTendency.ANIMUS,EnumManipulationRank.HUMILIS);
        int release=phaseDuration(t,CastPhase.RELEASE,false),recovery=phaseDuration(t,CastPhase.RECOVERY,false);
        var playback=new CastingPlayback(1,CastPhase.RELEASE,100,100,0,0,0,release,recovery,10);
        assertEquals(CastPhase.RECOVERY,playback.phase(100+release+2));
        assertTrue(playback.update(2,CastPhase.RELEASE,103,103,0,0));
        assertEquals(0,playback.phaseTime(103,0));
        assertEquals(CastPhase.RELEASE,playback.phase(106));
        assertEquals(sample(t,CastPhase.RELEASE,3,0,false),sample(t,playback.phase(106),playback.phaseTime(106,0),0,false));
    }
    private static final CastingPose HOLD=CastingPose.pose(0,0,0,-60,-10,0,0,0), STRAIN=CastingPose.pose(0,0,0,-100,-50,0,0,0);
    /** Opening 8, release 6, recovery 12, cancel 10; the release and cancel start from the bare hold. */
    private static CastingTrack strainTrack() {
        var k=CastingTrack.Easing.LINEAR;
        return new CastingTrack.Builder()
            .opening(new CastingTrack.Keyframe<>(0,CastingPose.ZERO,k),new CastingTrack.Keyframe<>(8,HOLD,k)).hold(HOLD).strain(STRAIN,HandPose.ZERO)
            .release(new CastingTrack.Keyframe<>(0,HOLD,k),new CastingTrack.Keyframe<>(6,CastingPose.ZERO,k))
            .recovery(new CastingTrack.Keyframe<>(0,CastingPose.ZERO,k),new CastingTrack.Keyframe<>(12,CastingPose.ZERO,k))
            .cancel(new CastingTrack.Keyframe<>(0,HOLD,k),new CastingTrack.Keyframe<>(10,CastingPose.ZERO,k))
            .firstOpening(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(8,HandPose.ZERO,k)).firstHold(HandPose.ZERO)
            .firstRelease(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(6,HandPose.ZERO,k))
            .firstRecovery(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(12,HandPose.ZERO,k))
            .firstCancel(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(10,HandPose.ZERO,k)).build();
    }
    private static CastingDisplay display(CastingTrack t) {
        return new CastingDisplay(new CastingPlayback(7,CastPhase.OPENING,100,100,1,40),t,null,false);
    }
    @Test void strainFollowsChargeAndEasesBackOnDamageWithoutClockReset() {
        var t=strainTrack();
        var display=display(t);
        display.update(7,CastPhase.HOLD,100,110,10,40);
        assertEquals(HOLD.blend(STRAIN,.25F),display.pose(110,0));
        display.update(7,CastPhase.HOLD,100,120,20,40);
        assertEquals(.5F,display.strain(120,0));
        assertEquals(HOLD.blend(STRAIN,.5F),display.pose(120,0));
        // Damage drops the charge to .25; the pose eases back over several ticks instead of snapping.
        display.update(7,CastPhase.HOLD,100,130,10,40);
        assertEquals(.5F,display.strain(130,0));
        for(int tick=131;tick<=133;tick++)
            assertTrue(display.strain(tick,0)>display.playback.strain(tick,0)+.01F,"still easing at "+tick);
        assertEquals(display.playback.strain(134,0),display.strain(134,0));
        assertEquals(compose(t,null,CastPhase.HOLD,26,display.playback.strain(134,0),false),display.pose(134,0));
        assertEquals(34,display.playback.elapsed(134,0));
    }
    @Test void chargedReleaseLeavesFromTheLatchedStrain() {
        // The server's charged RELEASE carries held 0; the shown strain must not drop to the bare hold.
        var t=strainTrack();
        var display=display(t);
        display.update(7,CastPhase.HOLD,100,120,20,40);
        var held=display.pose(120,0);
        assertEquals(HOLD.blend(STRAIN,.5F),held);
        assertTrue(display.update(7,CastPhase.RELEASE,120,120,0,40));
        assertEquals(.5F,display.strain(120,0));
        CastingTrackRulesTest.assertClose(HOLD.blend(STRAIN,.5F),display.pose(120,0));
        // Past the crossfade the release still converges from the strain onto the authored keyframes.
        assertEquals(sample(t,CastPhase.RELEASE,3,.5F,false),display.pose(123,0));
        assertNotEquals(sample(t,CastPhase.RELEASE,3,0,false),display.pose(123,0));
        assertEquals(sample(t,CastPhase.RELEASE,6,0,false),display.pose(126,0));
        // A new cast starts from no strain.
        display.update(8,CastPhase.RELEASE,140,140,0,0);
        assertEquals(0,display.strain(140,0));
    }
    @Test void cancelDuringOpeningCrossfadesFromTheShownPose() {
        var t=strainTrack();
        var display=display(t);
        var opening=display.pose(104,0);
        assertEquals(sample(t,CastPhase.OPENING,4,0,false),opening);
        display.update(7,CastPhase.CANCEL,104,104,4,40);
        assertEquals(opening,display.pose(104,0));
        var mid=display.pose(105,.5F);
        assertNotEquals(opening,mid);
        assertNotEquals(sample(t,CastPhase.CANCEL,1.5F,0,false),mid);
        assertEquals(sample(t,CastPhase.CANCEL,3,0,false),display.pose(107,0));
        assertEquals(1,display.weight(107,0));
    }
    @Test void holdLoopStartsFromRestWhenTheOpeningEnds() {
        var k=CastingTrack.Easing.LINEAR;
        var t=new CastingTrack.Builder()
            .opening(new CastingTrack.Keyframe<>(0,CastingPose.ZERO,k),new CastingTrack.Keyframe<>(10,HOLD,k)).hold(HOLD)
            .loop(CastingPose.pose(0,0,0,-4,0,0,0,0),HandPose.hand(.02,0,0,0,0,0),48)
            .release(new CastingTrack.Keyframe<>(0,HOLD,k),new CastingTrack.Keyframe<>(6,CastingPose.ZERO,k))
            .recovery(new CastingTrack.Keyframe<>(0,CastingPose.ZERO,k),new CastingTrack.Keyframe<>(12,CastingPose.ZERO,k))
            .cancel(new CastingTrack.Keyframe<>(0,HOLD,k),new CastingTrack.Keyframe<>(10,CastingPose.ZERO,k))
            .firstOpening(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(10,HandPose.ZERO,k)).firstHold(HandPose.ZERO)
            .firstRelease(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(6,HandPose.ZERO,k))
            .firstRecovery(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(12,HandPose.ZERO,k))
            .firstCancel(new CastingTrack.Keyframe<>(0,HandPose.ZERO,k),new CastingTrack.Keyframe<>(10,HandPose.ZERO,k)).build();
        var display=new CastingDisplay(new CastingPlayback(3,CastPhase.OPENING,100,100,0,0),t,null,false);
        display.update(3,CastPhase.HOLD,100,106,0,0);
        assertEquals(CastPhase.HOLD,display.playback.phase(110));
        // The opening ends on the hold pose and the loop picks up from it, not from mid-swing.
        CastingTrackRulesTest.assertClose(HOLD,display.pose(110,0));
        CastingTrackRulesTest.assertClose(HOLD.offset(CastingPose.pose(0,0,0,-4,0,0,0,0),1),display.pose(122,0));
    }
    @Test void everyStyleTrackReturnsToRest() {
        for(var style:CastStyle.values()) {
            if(style==CastStyle.SCHOOL)continue;
            var t=CastingTracks.style(style);
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),style.name());
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),style.name());
            assertEquals(sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false),sample(t,CastPhase.RECOVERY,0,0,false),style.name());
        }
    }
}
