package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingPose.Rotation;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Easing;
import com.vincenthuto.hemomancy.client.player.FirstPersonHandRules;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static org.junit.jupiter.api.Assertions.*;

class CongeatioTracksTest {
    private static final CastPhase[] TIMED={CastPhase.OPENING,CastPhase.RELEASE,CastPhase.RECOVERY,CastPhase.CANCEL};
    private static final float[] CHARGES={0,.5F,1};
    private static final float EPS=1e-4F;

    private static CastingTrack t(EnumManipulationRank rank) { return CongeatioTracks.get(rank); }
    private static float deg(double d) { return (float)Math.toRadians(d); }
    private static int dur(CastingTrack t,CastPhase phase) { return phaseDuration(t,phase,false); }
    private static int span(CastingTrack t,CastPhase phase) { return phase==CastPhase.HOLD?240:dur(t,phase); }
    private static Rotation[] parts(CastingPose p) { return new Rotation[]{p.head(),p.body(),p.rightArm(),p.leftArm(),p.rightLeg(),p.leftLeg()}; }
    /** Largest single-component difference, radians. */
    static float diff(CastingPose a,CastingPose b) {
        float m=Math.abs(a.pitch()-b.pitch());
        var pa=parts(a); var pb=parts(b);
        for(int i=0;i<6;i++)m=Math.max(m,Math.max(Math.abs(pa[i].x()-pb[i].x()),Math.max(Math.abs(pa[i].y()-pb[i].y()),Math.abs(pa[i].z()-pb[i].z()))));
        return m;
    }
    static float diff(HandPose.Hand a,HandPose.Hand b) {
        return Math.max(Math.max(Math.max(Math.abs(a.tx()-b.tx()),Math.abs(a.ty()-b.ty())),Math.max(Math.abs(a.tz()-b.tz()),Math.abs(a.rx()-b.rx()))),
                Math.max(Math.abs(a.ry()-b.ry()),Math.abs(a.rz()-b.rz())));
    }
    static float diff(HandPose a,HandPose b) { return Math.max(diff(a.lead(),b.lead()),diff(a.support(),b.support())); }

    @Test void timingScalesWithRank() {
        int[] min={8,6,12,10},max={12,10,20,14},prev={0,0,0,0};
        for(var rank:EnumManipulationRank.values()) {
            var t=t(rank);
            for(int i=0;i<TIMED.length;i++) {
                int d=dur(t,TIMED[i]);
                assertTrue(d>=min[i]&&d<=max[i],rank+" "+TIMED[i]+" "+d);
                assertTrue(d>=prev[i],rank+" "+TIMED[i]+" shrinks");
                prev[i]=d;
            }
            assertEquals(t.opening().duration(),t.firstOpening().duration(),rank+" first opening");
            assertEquals(t.release().duration(),t.firstRelease().duration(),rank+" first release");
            assertEquals(t.recovery().duration(),t.firstRecovery().duration(),rank+" first recovery");
            assertEquals(t.cancel().duration(),t.firstCancel().duration(),rank+" first cancel");
        }
        var low=t(HUMILIS);
        var high=t(PERFECTUS);
        assertArrayEquals(new int[]{8,6,10},new int[]{dur(low,CastPhase.OPENING),dur(low,CastPhase.RELEASE),dur(low,CastPhase.CANCEL)});
        assertArrayEquals(new int[]{12,10,20,14},new int[]{dur(high,CastPhase.OPENING),dur(high,CastPhase.RELEASE),dur(high,CastPhase.RECOVERY),dur(high,CastPhase.CANCEL)});
    }

    @Test void segmentsJoinAndEndAtRest() {
        for(var rank:EnumManipulationRank.values()) {
            var t=t(rank);
            String id=rank.name();
            int opening=dur(t,CastPhase.OPENING),release=dur(t,CastPhase.RELEASE),recovery=dur(t,CastPhase.RECOVERY),cancel=dur(t,CastPhase.CANCEL);
            assertEquals(CastingPose.ZERO,t.opening().first(CastingPose.ZERO),id+" opening starts at rest");
            assertEquals(HandPose.ZERO,t.firstOpening().first(HandPose.ZERO),id+" first opening starts at rest");
            assertEquals(sample(t,CastPhase.OPENING,opening,0,false),sample(t,CastPhase.HOLD,0,0,false),id+" opening->hold");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(sample(t,CastPhase.RELEASE,release,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.CANCEL,0,0,false),id+" hold->cancel");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,recovery,0,false),id+" rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,cancel,0,false),id+" cancel rest");
            assertEquals(sampleFirst(t,CastPhase.OPENING,opening,0,false),sampleFirst(t,CastPhase.HOLD,0,0,false),id+" first opening->hold");
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,release,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,recovery,0,false),id+" first rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,cancel,0,false),id+" first cancel rest");
            for(float c:CHARGES) {
                assertTrue(diff(sample(t,CastPhase.HOLD,0,c,false),sample(t,CastPhase.RELEASE,0,c,false))<EPS,id+" strained release "+c);
                assertTrue(diff(sampleFirst(t,CastPhase.HOLD,0,c,false),sampleFirst(t,CastPhase.RELEASE,0,c,false))<EPS,id+" first strained release "+c);
            }
        }
    }

    @Test void staysInsideTheBodyAndTheFrame() {
        for(var rank:EnumManipulationRank.values()) {
            var t=t(rank);
            for(var phase:CastPhase.values())for(float c:CHARGES)for(float time=0;time<=span(t,phase);time+=.25F) {
                var pose=sample(t,phase,time,c,false);
                String id=rank+" "+phase+" "+time+" "+c;
                for(var arm:new Rotation[]{pose.rightArm(),pose.leftArm()}) {
                    assertTrue(Math.abs(arm.x())<=deg(130)+EPS,id+" arm x");
                    assertTrue(Math.abs(arm.z())<=deg(95)+EPS,id+" arm z");
                }
                assertTrue(Math.abs(pose.pitch())<=deg(14)+EPS,id+" pitch");
                var hands=sampleFirst(t,phase,time,c,false);
                for(var hand:new HandPose.Hand[]{hands.lead(),hands.support()}) {
                    assertTrue(Math.abs(hand.tx())<=.35F+EPS,id+" tx");
                    assertTrue(Math.abs(hand.ty())<=.25F+EPS,id+" ty");
                    assertTrue(Math.abs(hand.tz())<=.2F+EPS,id+" tz");
                }
            }
        }
    }

    @Test void humilisMovesOnlyTheLeadArmAndHead() {
        var t=t(HUMILIS);
        for(var phase:CastPhase.values())for(float c:new float[]{0,1})for(float time=0;time<=span(t,phase);time+=.25F) {
            var pose=sample(t,phase,time,c,false);
            String id=phase+" "+time+" "+c;
            assertEquals(Rotation.ZERO,pose.leftArm(),id+" left arm");
            assertEquals(Rotation.ZERO,pose.body(),id+" body");
            assertEquals(Rotation.ZERO,pose.rightLeg(),id+" right leg");
            assertEquals(Rotation.ZERO,pose.leftLeg(),id+" left leg");
            assertEquals(0F,pose.pitch(),0F,id+" pitch");
            assertEquals(HandPose.Hand.ZERO,sampleFirst(t,phase,time,c,false).support(),id+" support hand");
        }
    }

    @Test void mediocritasAndAboveUseBothArms() {
        for(var rank:EnumManipulationRank.values()) {
            if(rank==HUMILIS)continue;
            var t=t(rank);
            assertNotEquals(Rotation.ZERO,t.hold().first(CastingPose.ZERO).leftArm(),rank.name());
            assertNotEquals(HandPose.Hand.ZERO,t.firstHold().first(HandPose.ZERO).support(),rank+" first");
        }
    }

    @Test void firstPersonHandsKeepTheCrosshairClear() {
        for(var rank:EnumManipulationRank.values())for(float c:CHARGES) {
            var t=t(rank);
            for(int tick=0;tick<=240;tick++) {
                var hands=sampleFirst(t,CastPhase.HOLD,tick,c,false);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(hands.lead(),true),rank+" hold lead "+tick);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(hands.support(),false),rank+" hold support "+tick);
            }
            int run=0;
            for(int tick=0;tick<=dur(t,CastPhase.RELEASE);tick++) {
                var hands=sampleFirst(t,CastPhase.RELEASE,tick,c,false);
                boolean inside=FirstPersonHandRules.insideCrosshairZone(hands.lead(),true)||FirstPersonHandRules.insideCrosshairZone(hands.support(),false);
                run=inside?run+1:0;
                assertTrue(run<=2,rank+" release crosses the crosshair at "+tick);
            }
        }
    }

    @Test void openingLocksEveryPartOnTheSameTick() {
        for(var rank:EnumManipulationRank.values()) {
            var t=t(rank);
            int d=dur(t,CastPhase.OPENING);
            var hold=t.hold().first(CastingPose.ZERO);
            var start=t.opening().first(CastingPose.ZERO);
            var before=sample(t,CastPhase.OPENING,d-1,0,false);
            assertEquals(Easing.EASE_OUT,t.opening().frames().getLast().easing(),rank.name());
            assertEquals(Easing.EASE_OUT,t.firstOpening().frames().getLast().easing(),rank+" first");
            assertEquals(hold,sample(t,CastPhase.OPENING,d,0,false),rank+" locked");
            assertTrue(diff(before,hold)>0,rank+" still moving a tick early");
            var h=parts(hold);
            var b=parts(before);
            var s=parts(start);
            for(int i=0;i<6;i++)if(!h[i].equals(s[i]))assertNotEquals(h[i],b[i],rank+" part "+i+" finished early");
            if(hold.pitch()!=start.pitch())assertNotEquals(hold.pitch(),before.pitch(),rank+" pitch finished early");
        }
    }

    @Test void releaseHoldsThenMicroSnapsAndSettles() {
        for(var rank:EnumManipulationRank.values()) {
            var t=t(rank);
            var frames=t.release().frames();
            assertEquals(t.hold().first(CastingPose.ZERO),frames.get(0).pose(),rank+" begins at the hold");
            assertEquals(1,frames.get(1).tick(),rank+" snap is one tick");
            assertNotEquals(frames.get(0).pose(),frames.get(1).pose(),rank+" snap");
            assertNotEquals(frames.get(1).pose(),frames.get(2).pose(),rank+" settle");
            var first=t.firstRelease().frames();
            assertEquals(t.firstHold().first(HandPose.ZERO),first.get(0).pose(),rank+" first begins at the hold");
            assertEquals(1,first.get(1).tick(),rank+" first snap is one tick");
            assertNotEquals(first.get(0).pose(),first.get(1).pose(),rank+" first snap");
            assertNotEquals(first.get(1).pose(),first.get(2).pose(),rank+" first settle");
        }
    }

    @Test void holdIsAbsoluteAtEveryRank() {
        for(var rank:EnumManipulationRank.values())assertNull(t(rank).loop(),rank.name());
    }

    @Test void summaAndAboveThawLinearlyAndSlowly() {
        for(var rank:new EnumManipulationRank[]{SUMMA,MAGISTER,PERFECTUS}) {
            var t=t(rank);
            assertTrue(dur(t,CastPhase.RECOVERY)>=16,rank.name());
            for(var k:t.recovery().frames())assertEquals(Easing.LINEAR,k.easing(),rank.name());
            for(var k:t.firstRecovery().frames())assertEquals(Easing.LINEAR,k.easing(),rank+" first");
        }
    }

    @Test void cancelFracturesInOneThreeTickDrop() {
        for(var rank:EnumManipulationRank.values()) {
            var t=t(rank);
            var drop=t.cancel().frames().get(1);
            assertEquals(3,drop.tick(),rank.name());
            for(var arm:new Rotation[]{drop.pose().rightArm(),drop.pose().leftArm()}) {
                assertTrue(Math.abs(arm.x())<=deg(6),rank+" arms dropped");
                assertTrue(Math.abs(arm.z())<=deg(6),rank+" arms dropped");
            }
            assertEquals(3,t.firstCancel().frames().get(1).tick(),rank+" first");
        }
    }

    @Test void strainGrowsMonotonicallyWithCharge() {
        for(var rank:new EnumManipulationRank[]{MAGISTER,PERFECTUS}) {
            var t=t(rank);
            assertNotNull(t.strain(),rank.name());
            assertNotNull(t.firstStrain(),rank+" first");
            float prev=0,prevFirst=0;
            for(int i=1;i<=10;i++) {
                float c=i/10F;
                float d=diff(sample(t,CastPhase.HOLD,0,c,false),sample(t,CastPhase.HOLD,0,0,false));
                float f=diff(sampleFirst(t,CastPhase.HOLD,0,c,false),sampleFirst(t,CastPhase.HOLD,0,0,false));
                assertTrue(d+1e-7F>=prev,rank+" strain "+c);
                assertTrue(f+1e-7F>=prevFirst,rank+" first strain "+c);
                prev=d;
                prevFirst=f;
            }
            assertTrue(prev>EPS&&prevFirst>EPS,rank+" strain moves");
        }
    }

    @Test void magisterStrainTightensAndHardensTheSnap() {
        var t=t(MAGISTER);
        var hold=t.hold().first(CastingPose.ZERO);
        var strain=t.strain().first(CastingPose.ZERO);
        assertTrue(strain.rightArm().z()>hold.rightArm().z(),"right arm crosses further");
        assertTrue(strain.leftArm().z()<hold.leftArm().z(),"left arm crosses further");
        assertTrue(strain.head().x()>hold.head().x(),"head lowers");
        float relaxed=diff(sample(t,CastPhase.RELEASE,1,0,false),sample(t,CastPhase.RELEASE,0,0,false));
        float strained=diff(sample(t,CastPhase.RELEASE,1,1,false),sample(t,CastPhase.RELEASE,0,1,false));
        assertTrue(strained>relaxed,"snap grows with charge");
    }

    @Test void perfectusIsCrossedBowedAndUtterlyStill() {
        var t=t(PERFECTUS);
        var hold=t.hold().first(CastingPose.ZERO);
        assertTrue(hold.rightArm().x()<=deg(-45)&&hold.rightArm().x()>=deg(-75),"half-raised");
        assertTrue(hold.rightArm().z()>0&&hold.leftArm().z()<0,"crossed");
        assertTrue(hold.head().x()>0,"bowed");
        for(float c:CHARGES)for(int tick=0;tick<=240;tick++) {
            assertEquals(sample(t,CastPhase.HOLD,0,c,false),sample(t,CastPhase.HOLD,tick,c,false),"still at "+tick);
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,c,false),sampleFirst(t,CastPhase.HOLD,tick,c,false),"first still at "+tick);
        }
        for(float time=0;time<=dur(t,CastPhase.RELEASE);time+=.25F)
            assertTrue(diff(sample(t,CastPhase.RELEASE,time,0,false),hold)<=deg(2)+EPS,"release is small at "+time);
    }
}
