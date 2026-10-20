package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Easing;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import org.junit.jupiter.api.Test;
import java.util.List;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static org.junit.jupiter.api.Assertions.*;

class GameplayTracksTest {
    static final List<CastStyle> STYLES=List.of(CastStyle.RIPOSTE,CastStyle.BLOOD_RUSH,CastStyle.DEADLY_GAZE,CastStyle.MARIONETTE);
    static final CastPhase[] TIMED={CastPhase.OPENING,CastPhase.RELEASE,CastPhase.RECOVERY,CastPhase.CANCEL};

    @Test void onlyTheGameplayStylesResolveAndAreCached() {
        for(var style:CastStyle.values()) {
            if(STYLES.contains(style)) { assertNotNull(GameplayTracks.get(style),style.name()); assertSame(GameplayTracks.get(style),GameplayTracks.get(style)); }
            else assertNull(GameplayTracks.get(style),style.name());
        }
        assertNull(GameplayTracks.get(null));
    }

    @Test void everyTrackHonoursTheSharedContract() {
        for(var style:STYLES) {
            var t=GameplayTracks.get(style);
            assertContract(t,style.name(),false);
            assertTiming(t,style.name(),6,style==CastStyle.RIPOSTE?6:10,
                    style==CastStyle.BLOOD_RUSH?8:12,style==CastStyle.BLOOD_RUSH?8:20);
        }
    }

    @Test void riposteGuardsThenSweepsOutwardWithTheParryArm() {
        var t=GameplayTracks.get(CastStyle.RIPOSTE);
        var guard=sample(t,CastPhase.HOLD,0,0,false);
        assertTrue(deg(guard.leftArm().x())<=-60,"support arm raised across the chest");
        assertTrue(deg(guard.leftArm().z())<-20,"support arm inward");
        assertTrue(deg(guard.rightArm().x())<=-80,"lead arm raised");
        assertTrue(phaseDuration(t,CastPhase.RELEASE,false)<=6);
        float start=deg(t.release().first(CastingPose.ZERO).rightArm().z());
        boolean swept=false;
        for(var k:t.release().frames())if(k.tick()>=2 && k.tick()<=6 && start-deg(k.pose().rightArm().z())>=25)swept=true;
        assertTrue(swept,"lead arm sweeps outward >= 25 degrees between ticks 2 and 6");
    }

    @Test void bloodRushPumpsBothArmsBackWithoutFightingTheDash() {
        var t=GameplayTracks.get(CastStyle.BLOOD_RUSH);
        boolean pumped=false;
        for(var phase:new CastPhase[]{CastPhase.OPENING,CastPhase.RELEASE})for(float time=0;time<=phaseDuration(t,phase,false);time+=.5F) {
            var p=sample(t,phase,time,0,false);
            pumped|=p.rightArm().x()>0 && p.leftArm().x()>0;
        }
        assertTrue(pumped,"both arms pump back once");
        for(var phase:TIMED)for(float time=0;time<=phaseDuration(t,phase,false);time+=.5F)
            assertTrue(Math.abs(deg(sample(t,phase,time,0,false).pitch()))<=6+1e-3,phase+" "+time);
        assertEquals(8,t.recovery().duration()); assertEquals(8,t.firstRecovery().duration());
    }

    @Test void deadlyGazeIsLedByTheHeadAndLeavesArmsAndHandsAlone() {
        var t=GameplayTracks.get(CastStyle.DEADLY_GAZE);
        assertNotNull(t.strain());
        for(float charge:new float[]{0,.5F,1}) {
            for(var phase:TIMED)for(float time=0;time<=phaseDuration(t,phase,false);time+=.5F) {
                var p=sample(t,phase,time,charge,false);
                assertEquals(CastingPose.Rotation.ZERO,p.rightArm(),phase+" "+time); assertEquals(CastingPose.Rotation.ZERO,p.leftArm(),phase+" "+time);
                assertEquals(HandPose.ZERO,sampleFirst(t,phase,time,charge,false),phase+" first "+time);
            }
            var hold=sample(t,CastPhase.HOLD,5,charge,false);
            assertEquals(CastingPose.Rotation.ZERO,hold.rightArm()); assertEquals(CastingPose.Rotation.ZERO,hold.leftArm());
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.HOLD,5,charge,false));
        }
        float hold=sample(t,CastPhase.HOLD,0,0,false).head().x(), strained=sample(t,CastPhase.HOLD,0,1,false).head().x();
        assertTrue(hold>0,"head locks down on the target");
        assertTrue(strained>hold,"head tilts further with charge");
        assertStrainMonotonic(t,"DEADLY_GAZE");
        var snap=t.release().frames().get(1);
        assertEquals(1,snap.tick()); assertEquals(Easing.HOLD,snap.easing());
        assertNotEquals(t.release().frames().get(0).pose().head(),snap.pose().head());
    }

    @Test void marionetteHoldsTheCrossbarHighAndAlternatesTheLift() {
        var t=GameplayTracks.get(CastStyle.MARIONETTE);
        var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(-80,deg(hold.rightArm().x()),4); assertEquals(-80,deg(hold.leftArm().x()),4);
        assertEquals(15,Math.abs(deg(hold.rightArm().z())),3); assertEquals(15,Math.abs(deg(hold.leftArm().z())),3);
        assertTrue(deg(hold.head().x())>0 && deg(hold.head().x())<=8,"fingers-down carriage");
        var loop=t.loop();
        assertNotNull(loop); assertEquals(48,loop.period());
        assertEquals(loop.amplitude().rightArm().x(),-loop.amplitude().leftArm().x(),1e-6);
        assertEquals(3,Math.abs(deg(loop.amplitude().rightArm().x())),1e-3);
        assertLoopMoves(t,"MARIONETTE");
        for(var phase:TIMED)for(float time=0;time<=phaseDuration(t,phase,false);time+=.5F)assertHandsHigh(sampleFirst(t,phase,time,0,false),phase+" "+time);
        for(int tick=0;tick<=96;tick++)assertHandsHigh(sampleFirst(t,CastPhase.HOLD,tick,0,false),"hold "+tick);
    }

    private static void assertHandsHigh(HandPose h,String id) {
        assertTrue(h.lead().ty()>-.05F && h.support().ty()>-.05F,id);
    }

    // Shared contract helpers, also used by ChannelTracksTest.

    static float deg(float radians) { return (float)Math.toDegrees(radians); }

    static void assertTiming(CastingTrack t,String id,int releaseMin,int releaseMax,int recoveryMin,int recoveryMax) {
        int opening=t.opening().duration(),release=t.release().duration(),recovery=t.recovery().duration(),cancel=t.cancel().duration();
        assertTrue(opening>=8 && opening<=12,id+" opening "+opening);
        assertTrue(release>=releaseMin && release<=releaseMax,id+" release "+release);
        assertTrue(recovery>=recoveryMin && recovery<=recoveryMax,id+" recovery "+recovery);
        assertTrue(cancel>=10 && cancel<=14,id+" cancel "+cancel);
        // Both views share one clock.
        assertEquals(opening,t.firstOpening().duration(),id+" first opening");
        assertEquals(release,t.firstRelease().duration(),id+" first release");
        assertEquals(recovery,t.firstRecovery().duration(),id+" first recovery");
        assertEquals(cancel,t.firstCancel().duration(),id+" first cancel");
    }

    static void assertContract(CastingTrack t,String id,boolean passive) {
        int opening=phaseDuration(t,CastPhase.OPENING,false),release=phaseDuration(t,CastPhase.RELEASE,false);
        assertEquals(CastingPose.ZERO,sample(t,CastPhase.OPENING,0,0,false),id+" opening from rest");
        assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.OPENING,0,0,false),id+" first opening from rest");
        assertEquals(sample(t,CastPhase.OPENING,opening,0,false),sample(t,CastPhase.HOLD,0,0,false),id+" opening->hold");
        assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
        assertEquals(sample(t,CastPhase.RELEASE,release,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
        assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" recovery rest");
        assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" cancel rest");
        assertEquals(sampleFirst(t,CastPhase.OPENING,opening,0,false),sampleFirst(t,CastPhase.HOLD,0,0,false),id+" first opening->hold");
        assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
        assertEquals(sampleFirst(t,CastPhase.RELEASE,release,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
        assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" first recovery rest");
        assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" first cancel rest");
        for(float charge:new float[]{0,1}) {
            for(var phase:TIMED)for(float time=0;time<=phaseDuration(t,phase,false);time+=.5F) {
                assertBounds(sample(t,phase,time,charge,false),id+" "+phase+" "+time+" c"+charge);
                assertHandBounds(sampleFirst(t,phase,time,charge,false),id+" first "+phase+" "+time+" c"+charge);
            }
            int span=t.loop()==null?24:t.loop().period()*2;
            for(float time=0;time<=span;time+=.5F) {
                assertBounds(sample(t,CastPhase.HOLD,time,charge,false),id+" hold "+time+" c"+charge);
                var hands=sampleFirst(t,CastPhase.HOLD,time,charge,false);
                assertHandBounds(hands,id+" first hold "+time+" c"+charge);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(hands.lead(),true),id+" lead in crosshair at hold "+time);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(hands.support(),false),id+" support in crosshair at hold "+time);
            }
            int run=0;
            for(int tick=0;tick<=release;tick++) {
                var hands=sampleFirst(t,CastPhase.RELEASE,tick,charge,false);
                run=FirstPersonHandRules.insideCrosshairZone(hands.lead(),true)||FirstPersonHandRules.insideCrosshairZone(hands.support(),false)?run+1:0;
                assertTrue(run<=2,id+" release crosses the crosshair for "+run+" ticks");
            }
        }
        if(passive)for(var phase:new CastPhase[]{CastPhase.RELEASE,CastPhase.CANCEL})for(float time=0;time<=phaseDuration(t,phase,true);time+=.5F) {
            assertBounds(sample(t,phase,time,0,true),id+" passive "+phase+" "+time);
            assertHandBounds(sampleFirst(t,phase,time,0,true),id+" passive first "+phase+" "+time);
        }
    }

    static void assertBounds(CastingPose p,String id) {
        for(var arm:new CastingPose.Rotation[]{p.rightArm(),p.leftArm()}) {
            assertTrue(Math.abs(deg(arm.x()))<=130+1e-3,id+" arm x "+deg(arm.x()));
            assertTrue(Math.abs(deg(arm.z()))<=95+1e-3,id+" arm z "+deg(arm.z()));
        }
        assertTrue(Math.abs(deg(p.pitch()))<=14+1e-3,id+" pitch "+deg(p.pitch()));
    }

    static void assertHandBounds(HandPose h,String id) {
        for(var hand:new HandPose.Hand[]{h.lead(),h.support()}) {
            assertTrue(Math.abs(hand.tx())<=.35F+1e-6,id+" tx "+hand.tx());
            assertTrue(Math.abs(hand.ty())<=.25F+1e-6,id+" ty "+hand.ty());
            assertTrue(Math.abs(hand.tz())<=.2F+1e-6,id+" tz "+hand.tz());
        }
    }

    static void assertStrainMonotonic(CastingTrack t,String id) {
        var previous=sample(t,CastPhase.HOLD,0,0,false); var target=sample(t,CastPhase.HOLD,0,1,false);
        for(int i=1;i<=10;i++) {
            var p=sample(t,CastPhase.HOLD,0,i/10F,false);
            assertTrue(Math.signum(target.head().x()-previous.head().x())*(p.head().x()-previous.head().x())>=-1e-6,id+" strain head at "+i);
            assertTrue(Math.signum(target.pitch()-previous.pitch())*(p.pitch()-previous.pitch())>=-1e-6,id+" strain pitch at "+i);
            assertTrue(Math.signum(target.rightArm().x()-previous.rightArm().x())*(p.rightArm().x()-previous.rightArm().x())>=-1e-6,id+" strain arm at "+i);
            previous=p;
        }
    }

    /** The loop moves the hold, keeps every amplitude component within 4 degrees, and repeats on its period. */
    static void assertLoopMoves(CastingTrack t,String id) {
        var loop=t.loop(); var a=loop.amplitude();
        for(var r:new CastingPose.Rotation[]{a.head(),a.body(),a.rightArm(),a.leftArm(),a.rightLeg(),a.leftLeg()})
            for(float v:new float[]{r.x(),r.y(),r.z()})assertTrue(Math.abs(deg(v))<=4+1e-3,id+" loop amplitude "+deg(v));
        assertTrue(Math.abs(deg(a.pitch()))<=4+1e-3,id+" loop pitch");
        assertNotEquals(CastingPose.ZERO,a,id+" loop amplitude");
        assertNotEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.HOLD,loop.period()/4F,0,false),id+" loop moves");
        assertNotEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.HOLD,loop.period()/4F,0,false),id+" first loop moves");
    }
}
