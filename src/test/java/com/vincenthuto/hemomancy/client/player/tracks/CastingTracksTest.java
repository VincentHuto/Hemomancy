package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import net.minecraft.world.InteractionHand;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static org.junit.jupiter.api.Assertions.*;

class CastingTracksTest {
    @Test void everySchoolAndRankResolvesBothViews() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values()) {
            var t=CastingTracks.base(school,rank);
            assertNotNull(t,school+" "+rank);
            assertNotEquals(CastingPose.ZERO,sample(t,CastPhase.HOLD,8,0,false),school+" "+rank);
            assertNotEquals(HandPose.ZERO,sampleFirst(t,CastPhase.HOLD,8,0,false),school+" "+rank+" first");
        }
    }
    @Test void everyTrackIsContinuousAndReturnsToRest() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values()) {
            var t=CastingTracks.base(school,rank); String id=school+" "+rank;
            int opening=phaseDuration(t,CastPhase.OPENING,false),release=phaseDuration(t,CastPhase.RELEASE,false);
            assertEquals(sample(t,CastPhase.OPENING,opening,0,false),sample(t,CastPhase.HOLD,0,0,false),id+" opening->hold");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(sample(t,CastPhase.RELEASE,release,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" cancel rest");
            assertEquals(sampleFirst(t,CastPhase.OPENING,opening,0,false),sampleFirst(t,CastPhase.HOLD,0,0,false),id+" first opening->hold");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,release,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" first rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" first cancel rest");
        }
    }
    @Test void everyNonSchoolStyleResolvesAndSchoolDoesNot() {
        for(var style:CastStyle.values()) {
            if(style==CastStyle.SCHOOL)assertNull(CastingTracks.style(style));
            else assertNotNull(CastingTracks.style(style),style.name());
        }
    }
    @Test void mirroringExchangesLimbsAndReversesTwist() {
        var pose=sample(CastingTracks.base(EnumBloodTendency.TENEBRIS,EnumManipulationRank.SUMMA),CastPhase.RELEASE,2,0,false);
        assertEquals(pose,pose.mirror().mirror());
        assertEquals(-pose.body().y(),pose.mirror().body().y());
        assertEquals(pose.rightArm().x(),pose.mirror().leftArm().x());
    }

    private static final CastPhase[] PHASES={CastPhase.OPENING,CastPhase.HOLD,CastPhase.RELEASE,CastPhase.RECOVERY,CastPhase.CANCEL};
    @Test void humilisMovesOnlyTheMainArmAndKeepsTorsoStill() {
        for(var school:EnumBloodTendency.values()) {
            var t=CastingTracks.base(school,EnumManipulationRank.HUMILIS);
            for(var phase:PHASES)for(float tick=0;tick<=40;tick+=.5F) {
                var p=sample(t,phase,tick,1,false); String id=school+" "+phase+" "+tick;
                assertEquals(CastingPose.Rotation.ZERO,p.leftArm(),id); assertEquals(CastingPose.Rotation.ZERO,p.body(),id);
                assertEquals(0,p.pitch(),id); assertEquals(CastingPose.Rotation.ZERO,p.rightLeg(),id); assertEquals(CastingPose.Rotation.ZERO,p.leftLeg(),id);
                assertEquals(HandPose.Hand.ZERO,sampleFirst(t,phase,tick,1,false).support(),id+" support hand");
            }
        }
    }
    @Test void perfectusHoldsAreStillFor240Ticks() {
        for(var school:EnumBloodTendency.values()) {
            var t=CastingTracks.base(school,EnumManipulationRank.PERFECTUS);
            var still=sample(t,CastPhase.HOLD,0,1,false); var stillFirst=sampleFirst(t,CastPhase.HOLD,0,1,false);
            for(int tick=0;tick<=240;tick++) {
                assertEquals(still,sample(t,CastPhase.HOLD,tick,1,false),school+" "+tick);
                assertEquals(stillFirst,sampleFirst(t,CastPhase.HOLD,tick,1,false),school+" first "+tick);
            }
        }
    }
    @Test void everyBaseRespectsRotationPitchAndHandBounds() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values()) {
            var t=CastingTracks.base(school,rank);
            for(var phase:PHASES)for(float tick=0;tick<=40;tick+=.5F)for(float c:new float[]{0,.5F,1}) {
                var p=sample(t,phase,tick,c,false); String id=school+" "+rank+" "+phase+" "+tick;
                for(var arm:new CastingPose.Rotation[]{p.rightArm(),p.leftArm()}) {
                    assertTrue(Math.abs(arm.x())<=Math.toRadians(130)+1e-4,id+" arm x"); assertTrue(Math.abs(arm.z())<=Math.toRadians(95)+1e-4,id+" arm z");
                }
                assertTrue(Math.abs(p.pitch())<=Math.toRadians(14)+1e-4,id+" pitch");
                var h=sampleFirst(t,phase,tick,c,false);
                for(var hand:new HandPose.Hand[]{h.lead(),h.support()}) {
                    assertTrue(Math.abs(hand.tx())<=.35F+1e-4 && Math.abs(hand.ty())<=.25F+1e-4 && Math.abs(hand.tz())<=.2F+1e-4,id+" hand "+hand);
                }
            }
        }
    }
    @Test void holdHandsStayOutOfTheCrosshairZoneInEveryBase() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values()) {
            var t=CastingTracks.base(school,rank);
            for(int tick=0;tick<=96;tick++) {
                var h=sampleFirst(t,CastPhase.HOLD,tick,1,false);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(h.lead(),true),school+" "+rank+" lead "+tick);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(h.support(),false),school+" "+rank+" support "+tick);
            }
            int run=0;
            for(int tick=0;tick<=phaseDuration(t,CastPhase.RELEASE,false);tick++) {
                var h=sampleFirst(t,CastPhase.RELEASE,tick,1,false);
                run=FirstPersonHandRules.insideCrosshairZone(h.lead(),true)||FirstPersonHandRules.insideCrosshairZone(h.support(),false)?run+1:0;
                assertTrue(run<=2,school+" "+rank+" release crosshair run");
            }
        }
    }

    /** Every posed (non-ZERO) hand of a hold, at no charge and full strain, reads on screen. Shared with the Still Art test. */
    static void assertHoldHandsOnScreen(CastingTrack t,String id,int ticks) {
        for(float c:new float[]{0,1})for(int tick=0;tick<=ticks;tick++) {
            var h=sampleFirst(t,CastPhase.HOLD,tick,c,false);
            if(!h.lead().equals(HandPose.Hand.ZERO))assertTrue(FirstPersonHandRules.onScreen(h.lead(),true),id+" lead off screen charge "+c+" tick "+tick+" "+h.lead());
            if(!h.support().equals(HandPose.Hand.ZERO))assertTrue(FirstPersonHandRules.onScreen(h.support(),false),id+" support off screen charge "+c+" tick "+tick+" "+h.support());
        }
    }
    @Test void holdHandsStayOnScreenInEveryBase() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values())assertHoldHandsOnScreen(CastingTracks.base(school,rank),school+" "+rank,96);
    }
    @Test void holdHandsStayOnScreenInEveryBespokeStyle() {
        for(var style:CastStyle.values())if(style!=CastStyle.SCHOOL)assertHoldHandsOnScreen(CastingTracks.style(style),style.name(),96);
    }
    @Test void passiveOnAndOffGesturesKeepClearOfTheCrosshair() {
        for(var style:CastStyle.values()) {
            var t=CastingTracks.style(style);
            if(t==null || t.firstOn()==null)continue;
            for(var phase:new CastPhase[]{CastPhase.RELEASE,CastPhase.CANCEL}) {
                int run=0;
                for(int tick=0;tick<=phaseDuration(t,phase,true);tick++) {
                    var h=sampleFirst(t,phase,tick,0,true);
                    run=FirstPersonHandRules.insideCrosshairZone(h.lead(),true)||FirstPersonHandRules.insideCrosshairZone(h.support(),false)?run+1:0;
                    assertTrue(run<=2,style+" passive "+phase+" covers the crosshair at "+tick);
                }
            }
        }
    }
    @Test void presentationChangesOnEveryTrackOrAimInputButNotOnPlaybackFields() {
        var id=UUID.randomUUID();
        var base=CastingAnimationPacket.harbinger(1,id,"review",5,EnumBloodTendency.LUX,null,
                EnumManipulationRank.SUMMA,CastPurpose.AREA,CastStyle.SCHOOL,
                CastPhase.OPENING,0,0,0,0,InteractionHand.MAIN_HAND);
        // The 2026-10-19 fixture reused one id across schools: only the manipulation was compared, so the track went stale.
        assertTrue(CastingTracks.presentationChanged(base,with(base,"review",EnumBloodTendency.ANIMUS,base.rank(),base.purpose(),base.style())),"school");
        assertTrue(CastingTracks.presentationChanged(base,with(base,"review",base.school(),EnumManipulationRank.PERFECTUS,base.purpose(),base.style())),"rank");
        assertTrue(CastingTracks.presentationChanged(base,with(base,"review",base.school(),base.rank(),base.purpose(),CastStyle.WHITE_VERDICT)),"style");
        assertTrue(CastingTracks.presentationChanged(base,with(base,"review",base.school(),base.rank(),
                CastPurpose.values()[(base.purpose().ordinal()+1)%CastPurpose.values().length],base.style())),"purpose");
        assertTrue(CastingTracks.presentationChanged(base,with(base,"other",base.school(),base.rank(),base.purpose(),base.style())),"manipulation");
        var still=CastingAnimationPacket.stillArt(1,id,"review",5,
                EnumClarityStage.AWAKENED,
                StillArtEnding.values()[0],CastPhase.RELEASE,0,0,InteractionHand.MAIN_HAND);
        assertTrue(CastingTracks.presentationChanged(base,still),"lane");
        var stage=CastingAnimationPacket.stillArt(1,id,"review",5,
                EnumClarityStage.ENLIGHTENED,
                StillArtEnding.values()[0],CastPhase.RELEASE,0,0,InteractionHand.MAIN_HAND);
        assertTrue(CastingTracks.presentationChanged(still,stage),"stage");
        var ending=CastingAnimationPacket.stillArt(1,id,"review",5,
                EnumClarityStage.AWAKENED,
                StillArtEnding.values()[1],CastPhase.RELEASE,0,0,InteractionHand.MAIN_HAND);
        assertTrue(CastingTracks.presentationChanged(still,ending),"ending");
        // Phase, sequence, clock and charge updates keep the resolved track.
        var later=CastingAnimationPacket.harbinger(1,id,"review",6,EnumBloodTendency.LUX,null,
                EnumManipulationRank.SUMMA,base.purpose(),CastStyle.SCHOOL,CastPhase.HOLD,4,9,20,40,InteractionHand.OFF_HAND);
        assertFalse(CastingTracks.presentationChanged(base,later),"playback fields only");
        assertFalse(CastingTracks.presentationChanged(base,base),"same packet");
    }
    private static CastingAnimationPacket with(CastingAnimationPacket p,
            String manipulation,EnumBloodTendency school,EnumManipulationRank rank,com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose purpose,CastStyle style) {
        return CastingAnimationPacket.harbinger(p.entityId(),p.playerId(),manipulation,p.sequence(),school,p.secondary(),
                rank,purpose,style,p.phase(),p.started(),p.updated(),p.held(),p.required(),p.hand());
    }
    @Test void noLegacyClipsRemain() {
        assertFalse(new java.io.File("src/main/java/com/vincenthuto/hemomancy/client/player/CastingClips.java").exists());
        assertFalse(new java.io.File("src/main/java/com/vincenthuto/hemomancy/client/player/tracks/LegacyClipTracks.java").exists());
    }
}
