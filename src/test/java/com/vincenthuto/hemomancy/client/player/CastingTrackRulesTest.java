package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import java.util.List;
import static com.vincenthuto.hemomancy.client.player.CastingPose.*;
import static com.vincenthuto.hemomancy.client.player.CastingTrack.*;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static org.junit.jupiter.api.Assertions.*;

class CastingTrackRulesTest {
    private static final CastingPose A=pose(0,0,0,-40,-10,0,0,0), B=pose(0,0,0,-80,-30,0,0,0), S=pose(0,0,0,-100,-50,0,0,0);
    private static final HandPose HA=HandPose.hand(.1,0,0,10,0,0), HB=HandPose.hand(.3,0,0,30,0,0);

    private static CastingTrack track(boolean strain,boolean loop,boolean passive) {
        var b=new CastingTrack.Builder()
            .opening(new Keyframe<>(0,CastingPose.ZERO,Easing.LINEAR),new Keyframe<>(6,A,Easing.LINEAR))
            .hold(A)
            .release(new Keyframe<>(0,A,Easing.LINEAR),new Keyframe<>(6,B,Easing.LINEAR))
            .recovery(new Keyframe<>(0,B,Easing.LINEAR),new Keyframe<>(12,CastingPose.ZERO,Easing.LINEAR))
            .cancel(new Keyframe<>(0,A,Easing.LINEAR),new Keyframe<>(8,CastingPose.ZERO,Easing.LINEAR))
            .firstOpening(new Keyframe<>(0,HandPose.ZERO,Easing.LINEAR),new Keyframe<>(6,HA,Easing.LINEAR))
            .firstHold(HA)
            .firstRelease(new Keyframe<>(0,HA,Easing.LINEAR),new Keyframe<>(6,HB,Easing.LINEAR))
            .firstRecovery(new Keyframe<>(0,HB,Easing.LINEAR),new Keyframe<>(12,HandPose.ZERO,Easing.LINEAR))
            .firstCancel(new Keyframe<>(0,HA,Easing.LINEAR),new Keyframe<>(8,HandPose.ZERO,Easing.LINEAR));
        if(strain)b.strain(S,HB);
        if(loop)b.loop(pose(0,0,0,-4,0,0,0,0),HandPose.hand(.02,0,0,0,0,0),48);
        if(passive)b.on(new Keyframe<>(0,CastingPose.ZERO,Easing.LINEAR),new Keyframe<>(8,B,Easing.LINEAR),new Keyframe<>(16,CastingPose.ZERO,Easing.LINEAR))
                .off(new Keyframe<>(0,CastingPose.ZERO,Easing.LINEAR),new Keyframe<>(6,S,Easing.LINEAR),new Keyframe<>(12,CastingPose.ZERO,Easing.LINEAR))
                .firstOn(new Keyframe<>(0,HandPose.ZERO,Easing.LINEAR),new Keyframe<>(16,HandPose.ZERO,Easing.LINEAR))
                .firstOff(new Keyframe<>(0,HandPose.ZERO,Easing.LINEAR),new Keyframe<>(12,HandPose.ZERO,Easing.LINEAR));
        return b.build();
    }

    static void assertClose(CastingPose a,CastingPose b) {
        for(var pair:new CastingPose.Rotation[][]{{a.head(),b.head()},{a.body(),b.body()},{a.rightArm(),b.rightArm()},
                {a.leftArm(),b.leftArm()},{a.rightLeg(),b.rightLeg()},{a.leftLeg(),b.leftLeg()}}) {
            assertEquals(pair[0].x(),pair[1].x(),1e-5); assertEquals(pair[0].y(),pair[1].y(),1e-5); assertEquals(pair[0].z(),pair[1].z(),1e-5);
        }
        assertEquals(a.pitch(),b.pitch(),1e-5);
    }

    @Test void easingShapes() {
        assertEquals(0f,ease(Easing.HOLD,.99f));
        assertEquals(1f,ease(Easing.HOLD,1f));
        assertEquals(.5f,ease(Easing.LINEAR,.5f));
        assertTrue(ease(Easing.EASE_OUT,.5f)>.5f);
        assertEquals(.5f,ease(Easing.SMOOTH,.5f),1e-6);
        assertEquals(0f,ease(Easing.SMOOTH,0f)); assertEquals(1f,ease(Easing.SMOOTH,1f));
    }

    @Test void segmentInterpolatesBetweenKeyframesAndClampsPastEnd() {
        var segment=new Segment<>(List.of(new Keyframe<>(0,A,Easing.LINEAR),new Keyframe<>(6,B,Easing.LINEAR)));
        assertEquals(6,segment.duration());
        assertEquals(A.blend(B,.5F),segment.sample(3,CastingPose.ZERO));
        assertEquals(B,segment.sample(9,CastingPose.ZERO));
        assertEquals(A,segment.sample(-2,CastingPose.ZERO));
    }

    @Test void holdEasingStepsInsteadOfInterpolating() {
        var segment=new Segment<>(List.of(new Keyframe<>(0,A,Easing.LINEAR),new Keyframe<>(4,B,Easing.HOLD)));
        assertEquals(A,segment.sample(3.9F,CastingPose.ZERO));
        assertEquals(B,segment.sample(4,CastingPose.ZERO));
    }

    @Test void holdBlendsTowardStrainByCharge() {
        var t=track(true,false,false);
        assertEquals(A,sample(t,CastPhase.HOLD,30,0,false));
        assertEquals(S,sample(t,CastPhase.HOLD,30,1,false));
        assertEquals(A.blend(S,.5F),sample(t,CastPhase.HOLD,30,.5F,false));
        assertEquals(HB,sampleFirst(t,CastPhase.HOLD,30,1,false));
    }

    @Test void releaseStartsFromTheStrainedPoseAndEndsAuthored() {
        var t=track(true,false,false);
        assertClose(S,sample(t,CastPhase.RELEASE,0,1,false));
        assertEquals(B,sample(t,CastPhase.RELEASE,6,1,false));
        assertEquals(A,sample(t,CastPhase.RELEASE,0,0,false));
    }

    @Test void loopOffsetsHoldWithinAmplitudeAndPeriod() {
        var t=track(false,true,false);
        boolean moved=false;
        for(int tick=0;tick<=200;tick++) {
            var p=sample(t,CastPhase.HOLD,tick,0,false);
            float dx=Math.abs(p.rightArm().x()-A.rightArm().x());
            assertTrue(dx<=Math.toRadians(4)+1e-6,"tick "+tick);
            moved|=dx>1e-4;
            assertClose(p,sample(t,CastPhase.HOLD,tick+48,0,false));
        }
        assertTrue(moved);
        assertEquals(A,sample(track(false,false,false),CastPhase.HOLD,17,0,false));
    }

    @Test void passiveUsesOnAndOffSegmentsAndRestsAfterwards() {
        var t=track(false,false,true);
        assertEquals(B,sample(t,CastPhase.RELEASE,8,0,true));
        assertEquals(S,sample(t,CastPhase.CANCEL,6,0,true));
        assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,3,0,true));
        assertEquals(16,phaseDuration(t,CastPhase.RELEASE,true));
        assertEquals(12,phaseDuration(t,CastPhase.CANCEL,true));
        assertEquals(0,phaseDuration(t,CastPhase.RECOVERY,true));
    }

    @Test void phaseDurationsComeFromSegments() {
        var t=track(false,false,false);
        assertEquals(6,phaseDuration(t,CastPhase.OPENING,false));
        assertEquals(6,phaseDuration(t,CastPhase.RELEASE,false));
        assertEquals(12,phaseDuration(t,CastPhase.RECOVERY,false));
        assertEquals(8,phaseDuration(t,CastPhase.CANCEL,false));
        assertEquals(0,phaseDuration(t,CastPhase.HOLD,false));
    }

    @Test void recoveryAndCancelEndAtZero() {
        var t=track(true,true,false);
        assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,22,1,false));
        assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,18,1,false));
        assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,22,1,false));
        assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,18,1,false));
    }

    @Test void handPoseMirrorSwapsHandsAndNegatesLateral() {
        var p=new HandPose(HandPose.hand(.1,.2,.3,10,20,30).lead(),HandPose.hand(.4,.5,.6,40,50,60).lead());
        var m=p.mirror();
        assertEquals(p.support().ty(),m.lead().ty());
        assertEquals(-p.support().tx(),m.lead().tx());
        assertEquals(-p.support().ry(),m.lead().ry(),1e-6);
        assertEquals(-p.support().rz(),m.lead().rz(),1e-6);
        assertEquals(p.support().rx(),m.lead().rx(),1e-6);
        assertEquals(p,m.mirror());
    }

    @Test void poseBlendIncludesPitchAndKeepsBodyXClear() {
        var p=pose(0,10,0,0,0,0,0,0);
        assertEquals((float)Math.toRadians(10),p.pitch(),1e-6);
        assertEquals(0f,p.body().x());
        assertEquals((float)Math.toRadians(5),CastingPose.ZERO.blend(p,.5F).pitch(),1e-6);
        assertEquals(p.pitch(),p.mirror().pitch());
    }

    @Test void toModelFlipsOnlyTheArmRollSign() {
        var authored=pose(3,4,5,-60,-40,-60,40,7);
        var model=authored.toModel();
        assertEquals(authored.rightArm().x(),model.rightArm().x()); assertEquals(-authored.rightArm().z(),model.rightArm().z());
        assertEquals(authored.leftArm().x(),model.leftArm().x()); assertEquals(-authored.leftArm().z(),model.leftArm().z());
        assertEquals(authored.head(),model.head()); assertEquals(authored.body(),model.body());
        assertEquals(authored.rightLeg(),model.rightLeg()); assertEquals(authored.leftLeg(),model.leftLeg());
        assertEquals(authored.pitch(),model.pitch());
        assertEquals(CastingPose.ZERO,CastingPose.ZERO.toModel());
    }

    @Test void fadeOnlyCoversTheLastTicksOfRecoveryAndCancel() {
        // Authored recovery and passive off beats play at full amplitude; only the residual blend fades.
        assertEquals(1,weight(CastPhase.RECOVERY,14-6,14,10));
        assertEquals(1,weight(CastPhase.RECOVERY,14-4,14,10));
        assertEquals(.5F,weight(CastPhase.RECOVERY,14-2,14,10),1e-6);
        assertEquals(0,weight(CastPhase.RECOVERY,14,14,10));
        assertEquals(1,weight(CastPhase.CANCEL,7,14,12));
        assertEquals(0,weight(CastPhase.CANCEL,12,14,12));
        assertEquals(0,weight(CastPhase.RECOVERY,0,0,10));
        assertEquals(1,weight(CastPhase.HOLD,50,14,10));
        assertEquals(1,weight(CastPhase.RELEASE,0,14,10));
        assertEquals(1/3F,weight(CastPhase.OPENING,0,14,10),1e-6);
        assertEquals(1,weight(CastPhase.OPENING,2,14,10));
    }
}
