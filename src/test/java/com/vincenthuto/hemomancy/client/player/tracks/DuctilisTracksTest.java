package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Easing;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Keyframe;
import com.vincenthuto.hemomancy.client.player.FirstPersonHandRules;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static org.junit.jupiter.api.Assertions.*;

class DuctilisTracksTest {
    private static final int[] OPENING={8,9,10,11,12},RELEASE={6,7,8,9,10},RECOVERY={12,14,16,18,20},CANCEL={10,11,12,13,14};
    private static final float[] CHARGES={0,.5F,1};
    private static final float EPS=1e-5F;
    private interface Check { void at(CastPhase phase,float time,float charge); }

    private static CastingTrack track(EnumManipulationRank rank) { return DuctilisTracks.get(rank); }
    private static float deg(double d) { return (float)Math.toRadians(d); }
    private static int span(CastingTrack t,CastPhase phase) { return phase==CastPhase.HOLD?96:phaseDuration(t,phase,false); }
    private static CastingPose hold(CastingTrack t) { return sample(t,CastPhase.HOLD,0,0,false); }
    /** Every half tick of every phase at every charge; HOLD covers two minutes of loop. */
    private static void sweep(CastingTrack t,Check check) {
        for(var phase:CastPhase.values())for(float time=0;time<=span(t,phase);time+=.5F)for(float c:CHARGES)check.at(phase,time,c);
    }
    private static CastingPose.Rotation[] parts(CastingPose p) {
        return new CastingPose.Rotation[]{p.head(),p.body(),p.rightArm(),p.leftArm(),p.rightLeg(),p.leftLeg()};
    }
    private static float distance(CastingPose a,CastingPose b) {
        float d=Math.abs(a.pitch()-b.pitch()); var pa=parts(a); var pb=parts(b);
        for(int i=0;i<pa.length;i++)d+=Math.abs(pa[i].x()-pb[i].x())+Math.abs(pa[i].y()-pb[i].y())+Math.abs(pa[i].z()-pb[i].z());
        return d;
    }
    private static float distance(HandPose.Hand a,HandPose.Hand b) {
        return Math.abs(a.tx()-b.tx())+Math.abs(a.ty()-b.ty())+Math.abs(a.tz()-b.tz())+Math.abs(a.rx()-b.rx())+Math.abs(a.ry()-b.ry())+Math.abs(a.rz()-b.rz());
    }
    private static float distance(HandPose a,HandPose b) { return distance(a.lead(),b.lead())+distance(a.support(),b.support()); }
    private static boolean zero(CastingPose.Rotation r) { return Math.abs(r.x())<EPS && Math.abs(r.y())<EPS && Math.abs(r.z())<EPS; }
    private static boolean inside(HandPose f) {
        return FirstPersonHandRules.insideCrosshairZone(f.lead(),true) || FirstPersonHandRules.insideCrosshairZone(f.support(),false);
    }
    private static <P extends com.vincenthuto.hemomancy.client.player.Blendable<P>> List<Keyframe<P>> steps(List<Keyframe<P>> frames) {
        var out=new ArrayList<Keyframe<P>>();
        for(var k:frames)if(k.easing()==Easing.HOLD)out.add(k);
        return out;
    }
    /** Distance of the right arm from straight forward (x -90, z 0). */
    private static float offForward(CastingPose.Rotation arm) { return Math.abs(arm.x()+deg(90))+Math.abs(arm.z()); }

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
    @Test void phasesJoinAndEndAtRest() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); String id=rank.name();
            int open=phaseDuration(t,CastPhase.OPENING,false),rel=phaseDuration(t,CastPhase.RELEASE,false);
            assertEquals(sample(t,CastPhase.OPENING,open,0,false),hold(t),id+" opening->hold");
            assertEquals(hold(t),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(sample(t,CastPhase.RELEASE,rel,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" cancel rest");
            assertEquals(sampleFirst(t,CastPhase.OPENING,open,0,false),sampleFirst(t,CastPhase.HOLD,0,0,false),id+" first opening->hold");
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,rel,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" first rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" first cancel rest");
        }
    }
    @Test void humilisMovesOnlyTheLeadArmAndHead() {
        var t=track(HUMILIS);
        sweep(t,(phase,time,c) -> {
            var p=sample(t,phase,time,c,false); String id=phase+" "+time+" "+c;
            assertTrue(zero(p.leftArm()) && zero(p.body()) && zero(p.rightLeg()) && zero(p.leftLeg()),id+" "+p);
            assertEquals(0,p.pitch(),EPS,id);
            assertTrue(distance(sampleFirst(t,phase,time,c,false).support(),HandPose.Hand.ZERO)<EPS,id+" support hand");
        });
    }
    @Test void bothArmsFromMediocritasAndLegsOnlyFromSumma() {
        for(var rank:new EnumManipulationRank[]{MEDIOCRITAS,SUMMA,MAGISTER,PERFECTUS})
            assertTrue(Math.abs(hold(track(rank)).leftArm().x())>deg(20),rank+" support arm");
        var t=track(MEDIOCRITAS);
        sweep(t,(phase,time,c) -> {
            var p=sample(t,phase,time,c,false);
            assertTrue(zero(p.rightLeg()) && zero(p.leftLeg()) && zero(p.body()) && Math.abs(p.pitch())<EPS,phase+" "+time+" "+p);
        });
        for(var rank:new EnumManipulationRank[]{SUMMA,MAGISTER,PERFECTUS})assertNotEquals(0,hold(track(rank)).rightLeg().z(),rank+" stance");
    }
    @Test void everySampleStaysInBounds() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank);
            sweep(t,(phase,time,c) -> {
                var p=sample(t,phase,time,c,false); String id=rank+" "+phase+" "+time+" "+c;
                for(var arm:new CastingPose.Rotation[]{p.rightArm(),p.leftArm()}) {
                    assertTrue(Math.abs(arm.x())<=deg(130)+EPS,id+" arm x "+arm);
                    assertTrue(Math.abs(arm.z())<=deg(95)+EPS,id+" arm z "+arm);
                }
                assertTrue(Math.abs(p.pitch())<=deg(14)+EPS,id+" pitch");
                var f=sampleFirst(t,phase,time,c,false);
                for(var h:new HandPose.Hand[]{f.lead(),f.support()})
                    assertTrue(Math.abs(h.tx())<=.35F+EPS && Math.abs(h.ty())<=.25F+EPS && Math.abs(h.tz())<=.2F+EPS,id+" hand "+h);
            });
        }
    }
    @Test void handsKeepClearOfTheCrosshair() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank);
            for(float c:CHARGES) {
                for(int time=0;time<=96;time++)assertFalse(inside(sampleFirst(t,CastPhase.HOLD,time,c,false)),rank+" hold "+time);
                int run=0;
                for(int time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time++) {
                    run=inside(sampleFirst(t,CastPhase.RELEASE,time,c,false))?run+1:0;
                    assertTrue(run<=2,rank+" release crosses the crosshair at "+time);
                }
            }
        }
    }
    @Test void openingIsStaccatoSteps() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); var frames=t.opening().frames(); String id=rank.name();
            assertEquals(0,frames.getFirst().tick(),id);
            assertEquals(CastingPose.ZERO,frames.getFirst().pose(),id);
            int count=steps(frames).size();
            assertTrue(count>=(rank==HUMILIS?1:2),id+" steps "+count);
            assertEquals(frames.size()-1,count,id+" no eased keyframes between steps");
            assertEquals(t.firstOpening().frames().size()-1,steps(t.firstOpening().frames()).size(),id+" first-person steps");
            for(int i=1;i<frames.size();i++) {
                var prev=frames.get(i-1); var k=frames.get(i);
                assertTrue(k.tick()-prev.tick()>=2,id+" step at "+k.tick()+" held under 2 ticks");
                assertEquals(prev.pose(),sample(t,CastPhase.OPENING,k.tick()-.5F,0,false),id+" before "+k.tick());
                assertEquals(k.pose(),sample(t,CastPhase.OPENING,k.tick(),0,false),id+" at "+k.tick());
            }
        }
    }
    @Test void headCounterJerksBetweenSteps() {
        for(var rank:EnumManipulationRank.values()) {
            var s=steps(track(rank).opening().frames());
            for(int i=1;i<s.size();i++) {
                var a=s.get(i-1).pose().head(); var b=s.get(i).pose().head();
                assertTrue(a.x()*b.x()<0 || a.y()*b.y()<0,rank+" head step "+i);
            }
        }
    }
    @Test void summaAndAboveTwistAndStanceFlipEachStep() {
        for(var rank:new EnumManipulationRank[]{SUMMA,MAGISTER,PERFECTUS}) {
            var s=steps(track(rank).opening().frames());
            for(int i=1;i<s.size();i++) {
                float a=s.get(i-1).pose().body().y(),b=s.get(i).pose().body().y();
                assertTrue(a*b<=0 && Math.abs(a)+Math.abs(b)>EPS,rank+" twist step "+i);
                assertTrue(Math.abs(s.get(i).pose().rightLeg().z()-s.get(i-1).pose().rightLeg().z())>=deg(2.5),rank+" stance step "+i);
            }
            for(int i=2;i<s.size();i++) {
                float d1=s.get(i-1).pose().rightLeg().z()-s.get(i-2).pose().rightLeg().z(),d2=s.get(i).pose().rightLeg().z()-s.get(i-1).pose().rightLeg().z();
                assertTrue(d1*d2<0,rank+" legs swap at step "+i);
            }
        }
    }
    @Test void releaseIsAFinalStepForward() {
        for(var rank:new EnumManipulationRank[]{HUMILIS,MEDIOCRITAS,SUMMA,MAGISTER}) {
            var t=track(rank); var h=hold(t); var s=steps(t.release().frames());
            assertFalse(s.isEmpty(),rank.name());
            var forward=s.getFirst().pose();
            assertTrue(offForward(forward.rightArm())<offForward(h.rightArm()),rank+" lead arm");
            if(rank!=HUMILIS)assertTrue(offForward(forward.leftArm().mirror())<offForward(h.leftArm().mirror()),rank+" support arm");
            assertEquals(h,sample(t,CastPhase.RELEASE,s.getFirst().tick()-.5F,0,false),rank+" no easing into the step");
        }
    }
    @Test void perfectusReleaseIsOneTickSnapWiderAndBack() {
        var t=track(PERFECTUS); var h=hold(t); var fh=sampleFirst(t,CastPhase.HOLD,0,0,false); int changed=0,fChanged=0;
        for(int time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time++) {
            var p=sample(t,CastPhase.RELEASE,time,0,false);
            if(!p.equals(h)) {
                changed++;
                assertTrue(Math.abs(p.rightArm().z())>Math.abs(h.rightArm().z()),"wider at "+time);
            }
            if(!sampleFirst(t,CastPhase.RELEASE,time,0,false).equals(fh))fChanged++;
            assertTrue(Math.abs(p.pitch()-h.pitch())<=deg(2)+EPS && Math.abs(p.body().y()-h.body().y())<=deg(2)+EPS
                    && Math.abs(p.head().x()-h.head().x())<=deg(2)+EPS,"body moves at most two degrees at "+time);
        }
        assertEquals(1,changed);
        assertEquals(1,fChanged);
    }
    @Test void cancelCollapsesInTwoUnevenSteps() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); var s=steps(t.cancel().frames()); var h=hold(t);
            assertEquals(2,s.size(),rank.name());
            float x0=Math.abs(h.rightArm().x()),x1=Math.abs(s.get(0).pose().rightArm().x()),x2=Math.abs(s.get(1).pose().rightArm().x());
            assertTrue(x0>x1 && x1>x2 && x2>0,rank+" arms drop");
            assertTrue(Math.abs((x0-x1)-(x1-x2))>deg(3),rank+" uneven drops");
        }
    }
    @Test void magisterStrainsWithChargeAndTics() {
        var t=track(MAGISTER);
        assertNotNull(t.strain());
        float last=-1,fLast=-1; var h=hold(t); var fh=sampleFirst(t,CastPhase.HOLD,0,0,false);
        for(int i=0;i<=10;i++) {
            float d=distance(sample(t,CastPhase.HOLD,0,i/10F,false),h),fd=distance(sampleFirst(t,CastPhase.HOLD,0,i/10F,false),fh);
            if(i>0) { assertTrue(d>last,"strain "+i); assertTrue(fd>fLast,"first strain "+i); }
            last=d; fLast=fd;
        }
        var loop=t.loop();
        assertNotNull(loop);
        assertEquals(12,loop.period());
        for(var r:parts(loop.amplitude()))
            assertTrue(Math.abs(r.x())<=deg(4)+EPS && Math.abs(r.y())<=deg(4)+EPS && Math.abs(r.z())<=deg(4)+EPS,"loop "+r);
        assertTrue(Math.abs(loop.amplitude().pitch())<=deg(4)+EPS);
        for(var hand:new HandPose.Hand[]{loop.firstAmplitude().lead(),loop.firstAmplitude().support()})
            assertTrue(Math.abs(hand.tx())<=.02F && Math.abs(hand.ty())<=.02F && Math.abs(hand.tz())<=.02F
                    && Math.abs(hand.rx())<=deg(2) && Math.abs(hand.ry())<=deg(2) && Math.abs(hand.rz())<=deg(2),"first loop "+hand);
        assertTrue(distance(sample(t,CastPhase.HOLD,3,0,false),h)>deg(1),"loop moves");
    }
    @Test void perfectusHoldIsStill() {
        var t=track(PERFECTUS);
        assertNotNull(t.strain());
        assertNull(t.loop());
        for(float c:CHARGES) {
            var p=sample(t,CastPhase.HOLD,0,c,false); var f=sampleFirst(t,CastPhase.HOLD,0,c,false);
            for(int time=0;time<=240;time++) {
                assertEquals(p,sample(t,CastPhase.HOLD,time,c,false),"hold "+time+" "+c);
                assertEquals(f,sampleFirst(t,CastPhase.HOLD,time,c,false),"first hold "+time+" "+c);
            }
        }
        var h=hold(t);
        assertEquals(h.rightArm().x(),h.leftArm().x(),EPS);
        assertEquals(h.rightArm().z(),-h.leftArm().z(),EPS);
        assertEquals(0,h.body().y(),EPS);
    }
}
