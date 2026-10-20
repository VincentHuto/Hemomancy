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

class LuxTracksTest {
    private static final int[] OPENING={8,9,10,11,12},RELEASE={6,7,8,9,10},RECOVERY={12,14,16,18,20},CANCEL={10,11,12,13,14};
    private static final float[] CHARGES={0,.5F,1};
    private static final float EPS=1e-5F;
    private interface Check { void at(CastPhase phase,float time,float charge); }

    private static CastingTrack track(EnumManipulationRank rank) { return LuxTracks.get(rank); }
    private static float deg(double d) { return (float)Math.toRadians(d); }
    private static int span(CastingTrack t,CastPhase phase) { return phase==CastPhase.HOLD?96:phaseDuration(t,phase,false); }
    private static CastingPose hold(CastingTrack t) { return sample(t,CastPhase.HOLD,0,0,false); }
    private static CastingPose releaseEnd(CastingTrack t) { return sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false); }
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
    /** Arm x applies before z: how far the hand hangs below the shoulder (1 hanging, 0 level, negative raised). */
    private static float drop(CastingPose.Rotation arm) { return (float)(Math.cos(arm.x())*Math.cos(arm.z())); }
    /** How far the arm reaches out to the side (0 forward or hanging, 1 straight out). */
    private static float side(CastingPose.Rotation arm) { return (float)Math.abs(Math.cos(arm.x())*Math.sin(arm.z())); }
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
    @Test void phasesJoinAndEndAtRest() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); String id=rank.name();
            int open=phaseDuration(t,CastPhase.OPENING,false),rel=phaseDuration(t,CastPhase.RELEASE,false);
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.OPENING,0,0,false),id+" opening from rest");
            assertEquals(sample(t,CastPhase.OPENING,open,0,false),hold(t),id+" opening->hold");
            assertEquals(hold(t),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(releaseEnd(t),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
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
    @Test void mediocritasUsesBothArmsButNotTheBody() {
        var t=track(MEDIOCRITAS);
        sweep(t,(phase,time,c) -> {
            var p=sample(t,phase,time,c,false);
            assertTrue(zero(p.rightLeg()) && zero(p.leftLeg()) && zero(p.body()) && Math.abs(p.pitch())<EPS,phase+" "+time+" "+p);
        });
        for(var rank:new EnumManipulationRank[]{MEDIOCRITAS,SUMMA,MAGISTER,PERFECTUS})
            assertTrue(Math.abs(hold(track(rank)).leftArm().x())+Math.abs(hold(track(rank)).leftArm().z())>deg(60),rank+" support arm");
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
    @Test void armsRiseThenPartWithTheHeadLifted() {
        for(var rank:EnumManipulationRank.values()) {
            var h=hold(track(rank)); var end=releaseEnd(track(rank)); var arm=h.rightArm();
            assertTrue(h.head().x()<0,rank+" head lifted");
            assertTrue(drop(arm)<=.3F,rank+" arms risen to the shoulder "+drop(arm));
            if(rank==HUMILIS || rank==MEDIOCRITAS)
                assertTrue(arm.x()<=deg(-90)+EPS && Math.abs(arm.z())<=deg(20),rank+" forward raise "+arm);
            else assertTrue(arm.x()>=deg(-40) && arm.x()<=deg(-15) && arm.z()<=deg(-60) && arm.z()>=deg(-90)-EPS,rank+" sideways orant "+arm);
            if(rank==HUMILIS)assertTrue(end.rightArm().x()<arm.x(),"release lifts");
            if(rank!=HUMILIS) {
                assertEquals(arm.x(),h.leftArm().x(),EPS,rank+" symmetric x");
                assertEquals(arm.z(),-h.leftArm().z(),EPS,rank+" symmetric z");
                assertEquals(end.rightArm().x(),end.leftArm().x(),EPS,rank+" symmetric release x");
                assertEquals(end.rightArm().z(),-end.leftArm().z(),EPS,rank+" symmetric release z");
            }
            if(rank==MEDIOCRITAS || rank==SUMMA || rank==MAGISTER) {
                assertTrue(end.rightArm().z()<arm.z(),rank+" release parts outward");
                assertTrue(side(end.rightArm())>side(arm),rank+" release reaches wider");
                assertTrue(drop(end.rightArm())<=.3F,rank+" release stays near the shoulder");
            }
            if(rank==SUMMA || rank==MAGISTER || rank==PERFECTUS)
                assertTrue(h.pitch()<0 && h.pitch()>=deg(-14),rank+" leans back");
        }
    }
    @Test void perfectusIsCruciformAndReleaseLiftsTwoDegrees() {
        var t=track(PERFECTUS); var h=hold(t); var end=releaseEnd(t);
        assertEquals(deg(-20),h.rightArm().x(),EPS); assertEquals(deg(-90),h.rightArm().z(),EPS);
        assertEquals(deg(-20),h.leftArm().x(),EPS); assertEquals(deg(90),h.leftArm().z(),EPS);
        assertTrue(side(h.rightArm())>.9F && Math.abs(drop(h.rightArm()))<EPS,"arms level at the sides");
        assertTrue(end.head().x()<h.head().x() || end.pitch()<h.pitch(),"release lifts the upper body");
        for(float time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time+=.5F) {
            var p=sample(t,CastPhase.RELEASE,time,0,false);
            assertTrue(Math.abs(p.pitch()-h.pitch())<=deg(2)+EPS && Math.abs(p.head().x()-h.head().x())<=deg(2)+EPS
                    && Math.abs(p.rightArm().x()-h.rightArm().x())<=deg(2)+EPS
                    && Math.abs(p.rightArm().z()-h.rightArm().z())<=deg(2)+EPS,"two degrees at most at "+time);
        }
    }
    @Test void cancelLowersArmsBeforeTheHead() {
        for(var rank:EnumManipulationRank.values()) {
            var t=track(rank); var h=hold(t); var frames=t.cancel().frames();
            assertEquals(3,frames.size(),rank.name());
            var mid=frames.get(1).pose();
            assertTrue(drop(mid.rightArm())>.6F,rank+" arms lowered "+drop(mid.rightArm()));
            assertTrue(mid.head().x()<=.75F*h.head().x(),rank+" head still lifted");
        }
    }
    @Test void magisterClimbsWithChargeAndBreathes() {
        var t=track(MAGISTER);
        assertNotNull(t.strain());
        float last=-1,fLast=-1; var h=hold(t); var fh=sampleFirst(t,CastPhase.HOLD,0,0,false);
        for(int i=0;i<=10;i++) {
            float d=distance(sample(t,CastPhase.HOLD,0,i/10F,false),h),fd=distance(sampleFirst(t,CastPhase.HOLD,0,i/10F,false),fh);
            if(i>0) { assertTrue(d>last,"strain "+i); assertTrue(fd>fLast,"first strain "+i); }
            last=d; fLast=fd;
        }
        var full=sample(t,CastPhase.HOLD,0,1,false);
        assertTrue(drop(full.rightArm())<drop(h.rightArm()) && full.head().x()<h.head().x(),"arms and head climb");
        var loop=t.loop();
        assertNotNull(loop);
        assertEquals(48,loop.period());
        for(var r:parts(loop.amplitude()))
            assertTrue(Math.abs(r.x())<=deg(4)+EPS && Math.abs(r.y())<=deg(4)+EPS && Math.abs(r.z())<=deg(4)+EPS,"loop "+r);
        assertTrue(Math.abs(loop.amplitude().pitch())<=deg(4)+EPS);
        for(var hand:new HandPose.Hand[]{loop.firstAmplitude().lead(),loop.firstAmplitude().support()})
            assertTrue(Math.abs(hand.tx())<=.02F && Math.abs(hand.ty())<=.02F && Math.abs(hand.tz())<=.02F
                    && Math.abs(hand.rx())<=deg(2) && Math.abs(hand.ry())<=deg(2) && Math.abs(hand.rz())<=deg(2),"first loop "+hand);
        assertTrue(distance(sample(t,CastPhase.HOLD,12,0,false),h)>deg(1),"loop moves");
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
    }
}
