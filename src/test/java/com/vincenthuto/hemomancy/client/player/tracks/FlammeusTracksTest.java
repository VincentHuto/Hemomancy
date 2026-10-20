package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.FirstPersonHandRules;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static org.junit.jupiter.api.Assertions.*;

class FlammeusTracksTest {
    private static final int[] OPENING={8,9,10,11,12},RELEASE={6,7,8,9,10},RECOVERY={12,14,16,18,20},CANCEL={10,11,12,13,14};
    private static final CastPhase[] PHASES=CastPhase.values();
    private static final float EPS=1e-5F;

    private static CastingTrack track(EnumManipulationRank rank) { return FlammeusTracks.get(rank); }
    private static float deg(double d) { return (float)Math.toRadians(d); }
    private static int span(CastingTrack t,CastPhase phase) { return phase==CastPhase.HOLD?96:phaseDuration(t,phase,false); }
    private static CastingPose hold(CastingTrack t) { return sample(t,CastPhase.HOLD,0,0,false); }
    private static CastingPose releaseEnd(CastingTrack t) { return sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false); }
    private static CastingPose.Rotation[] parts(CastingPose p) {
        return new CastingPose.Rotation[]{p.head(),p.body(),p.rightArm(),p.leftArm(),p.rightLeg(),p.leftLeg()};
    }
    private static float distance(CastingPose a,CastingPose b) {
        float d=Math.abs(a.pitch()-b.pitch()); var pa=parts(a); var pb=parts(b);
        for(int i=0;i<pa.length;i++)d+=Math.abs(pa[i].x()-pb[i].x())+Math.abs(pa[i].y()-pb[i].y())+Math.abs(pa[i].z()-pb[i].z());
        return d;
    }
    private static float distance(CastingPose.Rotation a,CastingPose.Rotation b) {
        return Math.abs(a.x()-b.x())+Math.abs(a.y()-b.y())+Math.abs(a.z()-b.z());
    }
    private static float distance(HandPose.Hand a,HandPose.Hand b) {
        return Math.abs(a.tx()-b.tx())+Math.abs(a.ty()-b.ty())+Math.abs(a.tz()-b.tz())+Math.abs(a.rx()-b.rx())+Math.abs(a.ry()-b.ry())+Math.abs(a.rz()-b.rz());
    }
    private static float distance(HandPose a,HandPose b) { return distance(a.lead(),b.lead())+distance(a.support(),b.support()); }
    private static void assertZero(CastingPose.Rotation r,String id) {
        assertTrue(Math.abs(r.x())<EPS && Math.abs(r.y())<EPS && Math.abs(r.z())<EPS,id+" "+r);
    }
    private static void assertZero(HandPose.Hand h,String id) { assertTrue(distance(h,HandPose.Hand.ZERO)<EPS,id+" "+h); }
    private static boolean inside(HandPose f) {
        return FirstPersonHandRules.insideCrosshairZone(f.lead(),true) || FirstPersonHandRules.insideCrosshairZone(f.support(),false);
    }

    @Test void timingScalesWithRank() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); int i=rank.ordinal(); String id=rank.name();
            assertEquals(OPENING[i],phaseDuration(t,CastPhase.OPENING,false),id+" opening");
            assertEquals(RELEASE[i],phaseDuration(t,CastPhase.RELEASE,false),id+" release");
            assertEquals(RECOVERY[i],phaseDuration(t,CastPhase.RECOVERY,false),id+" recovery");
            assertEquals(CANCEL[i],phaseDuration(t,CastPhase.CANCEL,false),id+" cancel");
            assertEquals(OPENING[i],t.firstOpening().duration(),id+" first opening");
            assertEquals(RELEASE[i],t.firstRelease().duration(),id+" first release");
            assertEquals(RECOVERY[i],t.firstRecovery().duration(),id+" first recovery");
            assertEquals(CANCEL[i],t.firstCancel().duration(),id+" first cancel");
        }
    }

    @Test void phasesJoinAndReturnToRest() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); String id=rank.name();
            int opening=phaseDuration(t,CastPhase.OPENING,false),release=phaseDuration(t,CastPhase.RELEASE,false);
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.OPENING,0,0,false),id+" opening start");
            assertEquals(sample(t,CastPhase.OPENING,opening,0,false),sample(t,CastPhase.HOLD,0,0,false),id+" opening->hold");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(sample(t,CastPhase.RELEASE,release,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" cancel rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.OPENING,0,0,false),id+" first opening start");
            assertEquals(sampleFirst(t,CastPhase.OPENING,opening,0,false),sampleFirst(t,CastPhase.HOLD,0,0,false),id+" first opening->hold");
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,release,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" first rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" first cancel rest");
        }
    }

    @Test void everySampleStaysInBounds() {
        for(var rank:EnumManipulationRank.values())for(var phase:PHASES)for(float c:new float[]{0,1}) {
            var t=track(rank);
            for(int i=0;i<=span(t,phase)*2;i++) {
                float time=i*.5F; String id=rank+" "+phase+" "+time+" charge "+c;
                var p=sample(t,phase,time,c,false);
                assertTrue(Math.abs(p.rightArm().x())<=deg(130) && Math.abs(p.leftArm().x())<=deg(130),id+" arm x");
                assertTrue(Math.abs(p.rightArm().z())<=deg(95) && Math.abs(p.leftArm().z())<=deg(95),id+" arm z");
                assertTrue(Math.abs(p.pitch())<=deg(14)+EPS,id+" pitch");
                var f=sampleFirst(t,phase,time,c,false);
                for(var h:new HandPose.Hand[]{f.lead(),f.support()})
                    assertTrue(Math.abs(h.tx())<=.35F+EPS && Math.abs(h.ty())<=.25F+EPS && Math.abs(h.tz())<=.2F+EPS,id+" first "+h);
            }
        }
    }

    @Test void humilisMovesOnlyTheLeadArmAndHead() {
        var t=track(HUMILIS);
        for(var phase:PHASES)for(int i=0;i<=span(t,phase)*2;i++) {
            float time=i*.5F; String id=phase+" "+time;
            var p=sample(t,phase,time,0,false);
            assertZero(p.leftArm(),id+" left arm"); assertZero(p.body(),id+" body");
            assertZero(p.rightLeg(),id+" right leg"); assertZero(p.leftLeg(),id+" left leg");
            assertEquals(0,p.pitch(),EPS,id+" pitch");
            assertZero(sampleFirst(t,phase,time,0,false).support(),id+" support hand");
        }
    }

    @Test void higherRanksUseBothArmsAndMediocritasKeepsItsFeet() {
        for(var rank:EnumManipulationRank.values()) {
            if(rank==HUMILIS)continue;
            var t=track(rank);
            assertTrue(distance(hold(t).leftArm(),CastingPose.Rotation.ZERO)>deg(10),rank+" support arm");
            assertTrue(distance(sampleFirst(t,CastPhase.HOLD,0,0,false).support(),HandPose.Hand.ZERO)>.05F,rank+" support hand");
        }
        var t=track(MEDIOCRITAS);
        for(var phase:PHASES)for(int i=0;i<=span(t,phase)*2;i++) {
            var p=sample(t,phase,i*.5F,0,false); String id=phase+" "+i*.5F;
            assertEquals(0,p.pitch(),EPS,id+" pitch"); assertZero(p.rightLeg(),id+" right leg"); assertZero(p.leftLeg(),id+" left leg");
        }
    }

    @Test void firstPersonHandsKeepTheCrosshairClear() {
        for(var rank:EnumManipulationRank.values())for(float c:new float[]{0,1}) {
            var t=track(rank);
            for(int i=0;i<=96;i++)assertFalse(inside(sampleFirst(t,CastPhase.HOLD,i,c,false)),rank+" hold "+i);
            int run=0;
            for(int i=0;i<=phaseDuration(t,CastPhase.RELEASE,false);i++) {
                run=inside(sampleFirst(t,CastPhase.RELEASE,i,c,false))?run+1:0;
                assertTrue(run<=2,rank+" release crosses the crosshair for "+run+" ticks");
            }
        }
    }

    @Test void magisterStrainsWithChargeAndBreathes() {
        var t=track(MAGISTER);
        assertNotNull(t.strain()); assertNotNull(t.loop());
        assertEquals(48,t.loop().period());
        var rest=sample(t,CastPhase.HOLD,0,0,false); var firstRest=sampleFirst(t,CastPhase.HOLD,0,0,false);
        float last=-1,firstLast=-1;
        for(int i=0;i<=10;i++) {
            float c=i/10F;
            float d=distance(sample(t,CastPhase.HOLD,0,c,false),rest),fd=distance(sampleFirst(t,CastPhase.HOLD,0,c,false),firstRest);
            assertTrue(d>=last-EPS && fd>=firstLast-EPS,"strain grows with charge at "+c);
            last=d; firstLast=fd;
        }
        assertTrue(last>deg(10),"strain is visible");
        var a=t.loop().amplitude();
        for(var r:parts(a))assertTrue(Math.abs(r.x())<=deg(4)+EPS && Math.abs(r.y())<=deg(4)+EPS && Math.abs(r.z())<=deg(4)+EPS,"loop amplitude "+r);
        assertTrue(Math.abs(a.pitch())<=deg(4)+EPS,"loop pitch");
        for(var h:new HandPose.Hand[]{t.loop().firstAmplitude().lead(),t.loop().firstAmplitude().support()})
            assertTrue(Math.abs(h.tx())<=.02F && Math.abs(h.ty())<=.02F && Math.abs(h.tz())<=.02F
                    && Math.abs(h.rx())<=deg(2) && Math.abs(h.ry())<=deg(2) && Math.abs(h.rz())<=deg(2),"first loop amplitude "+h);
        assertTrue(distance(sample(t,CastPhase.HOLD,12,0,false),rest)>deg(2),"loop moves");
        assertNotEquals(firstRest,sampleFirst(t,CastPhase.HOLD,12,0,false),"first loop moves");
    }

    @Test void perfectusIsStillAndReleasesWithOneHardPitch() {
        var t=track(PERFECTUS);
        assertNotNull(t.strain()); assertNull(t.loop());
        for(float c:new float[]{0,.5F,1}) {
            var still=sample(t,CastPhase.HOLD,0,c,false); var firstStill=sampleFirst(t,CastPhase.HOLD,0,c,false);
            for(int i=1;i<=240;i++) {
                assertEquals(still,sample(t,CastPhase.HOLD,i,c,false),"hold "+i+" charge "+c);
                assertEquals(firstStill,sampleFirst(t,CastPhase.HOLD,i,c,false),"first hold "+i+" charge "+c);
            }
        }
        var hold=hold(t); var end=releaseEnd(t);
        assertTrue(end.pitch()>hold.pitch(),"release pitches the body forward");
        assertTrue(end.pitch()-hold.pitch()<=deg(4)+EPS,"a single 4 degree pitch at most");
        assertEquals(hold.body().y(),end.body().y(),EPS,"no twist");
        for(int i=0;i<=phaseDuration(t,CastPhase.RELEASE,false);i++) {
            var p=sample(t,CastPhase.RELEASE,i,0,false);
            assertTrue(distance(p.rightArm(),hold.rightArm())<=deg(5) && distance(p.leftArm(),hold.leftArm())<=deg(5),"arms stay low and wide at "+i);
        }
    }

    @Test void armCoilsBackThenDrivesForward() {
        for(var rank:new EnumManipulationRank[]{HUMILIS,MEDIOCRITAS,SUMMA,MAGISTER}) {
            var t=track(rank);
            assertTrue(hold(t).rightArm().x()>deg(-70),rank+" lead arm is cocked, not raised forward");
            assertTrue(releaseEnd(t).rightArm().x()<=deg(-95),rank+" lead arm drives forward");
        }
    }

    @Test void torsoUnwindsThroughTheThrow() {
        for(var rank:new EnumManipulationRank[]{MEDIOCRITAS,SUMMA,MAGISTER}) {
            var t=track(rank);
            assertTrue(hold(t).body().y()<0 && releaseEnd(t).body().y()>0,rank+" twist changes sign");
        }
    }

    @Test void headRecoilsOnRelease() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); float held=hold(t).head().x(),least=held;
            for(int i=0;i<=phaseDuration(t,CastPhase.RELEASE,false);i++)least=Math.min(least,sample(t,CastPhase.RELEASE,i,0,false).head().x());
            assertTrue(least<held-deg(2),rank+" head recoils "+Math.toDegrees(held-least));
        }
    }
}
