package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.List;
import java.util.function.ToDoubleFunction;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static org.junit.jupiter.api.Assertions.*;

class PerfectusTracksTest {
    private static final EnumSet<CastStyle> STYLES=EnumSet.of(CastStyle.CRIMSON_CORONATION,CastStyle.SYNAPTIC_STORM,CastStyle.WHITE_VERDICT,
            CastStyle.FUNERAL_BELL,CastStyle.ABSOLUTE_STILLNESS,CastStyle.LODESTONE_COLLAPSE,CastStyle.ECLIPSE_WELL,CastStyle.PHOENIX_DEBT);
    private static final EnumSet<CastStyle> CHARGED=EnumSet.of(CastStyle.CRIMSON_CORONATION,CastStyle.SYNAPTIC_STORM,CastStyle.WHITE_VERDICT,
            CastStyle.FUNERAL_BELL,CastStyle.LODESTONE_COLLAPSE,CastStyle.ECLIPSE_WELL);
    private static final float[] CHARGES={0,.5F,1};
    private static final List<CastPhase> TIMED=List.of(CastPhase.OPENING,CastPhase.RELEASE,CastPhase.RECOVERY,CastPhase.CANCEL);
    private static final double EPS=1e-3;

    private static CastingTrack t(CastStyle s) { return PerfectusTracks.get(s); }
    private static double deg(float radians) { return Math.toDegrees(radians); }
    private static void close(CastingPose a,CastingPose b,String id) {
        var x=new CastingPose.Rotation[]{a.head(),a.body(),a.rightArm(),a.leftArm(),a.rightLeg(),a.leftLeg()};
        var y=new CastingPose.Rotation[]{b.head(),b.body(),b.rightArm(),b.leftArm(),b.rightLeg(),b.leftLeg()};
        for(int i=0;i<x.length;i++) {
            assertEquals(x[i].x(),y[i].x(),1e-5,id+" part "+i+" x"); assertEquals(x[i].y(),y[i].y(),1e-5,id+" part "+i+" y");
            assertEquals(x[i].z(),y[i].z(),1e-5,id+" part "+i+" z");
        }
        assertEquals(a.pitch(),b.pitch(),1e-5,id+" pitch");
    }
    private static void close(HandPose a,HandPose b,String id) {
        for(var pair:new HandPose.Hand[][]{{a.lead(),b.lead()},{a.support(),b.support()}}) {
            var p=pair[0]; var q=pair[1];
            assertArrayEquals(new float[]{p.tx(),p.ty(),p.tz(),p.rx(),p.ry(),p.rz()},new float[]{q.tx(),q.ty(),q.tz(),q.rx(),q.ry(),q.rz()},1e-5F,id);
        }
    }

    @Test void exactlyTheEightPerfectusStylesResolveAndAreCached() {
        for(var style:CastStyle.values()) {
            if(STYLES.contains(style)) { assertNotNull(t(style),style.name()); assertSame(t(style),t(style),style.name()); }
            else assertNull(t(style),style.name());
        }
        assertNull(PerfectusTracks.get(null));
    }

    @Test void segmentsAreContinuousAndReturnToRestInBothViews() {
        for(var s:STYLES) {
            var t=t(s); String id=s.name();
            assertEquals(CastingPose.ZERO,t.opening().first(CastingPose.ZERO),id+" opening from rest");
            assertEquals(HandPose.ZERO,t.firstOpening().first(HandPose.ZERO),id+" first opening from rest");
            int opening=phaseDuration(t,CastPhase.OPENING,false),release=phaseDuration(t,CastPhase.RELEASE,false);
            assertEquals(sample(t,CastPhase.OPENING,opening,0,false),sample(t,CastPhase.HOLD,0,0,false),id+" opening->hold");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(sample(t,CastPhase.RELEASE,release,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.CANCEL,0,0,false),id+" hold->cancel");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" cancel rest");
            assertEquals(sampleFirst(t,CastPhase.OPENING,opening,0,false),sampleFirst(t,CastPhase.HOLD,0,0,false),id+" first opening->hold");
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,release,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.CANCEL,0,0,false),id+" first hold->cancel");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,phaseDuration(t,CastPhase.RECOVERY,false),0,false),id+" first rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,phaseDuration(t,CastPhase.CANCEL,false),0,false),id+" first cancel rest");
            if(t.strain()!=null) for(float c:CHARGES) {
                close(sample(t,CastPhase.HOLD,0,c,false),sample(t,CastPhase.RELEASE,0,c,false),id+" strained release start "+c);
                close(sampleFirst(t,CastPhase.HOLD,0,c,false),sampleFirst(t,CastPhase.RELEASE,0,c,false),id+" first strained release start "+c);
            }
        }
    }

    @Test void timingIsPerfectusAndBothViewsShareTheClock() {
        for(var s:STYLES) {
            var t=t(s); String id=s.name();
            int o=t.opening().duration(),r=t.release().duration(),v=t.recovery().duration(),c=t.cancel().duration();
            assertTrue(o>=10 && o<=12,id+" opening "+o); assertTrue(r>=8 && r<=10,id+" release "+r);
            assertTrue(v>=16 && v<=20,id+" recovery "+v); assertTrue(c>=12 && c<=14,id+" cancel "+c);
            assertEquals(o,t.firstOpening().duration(),id); assertEquals(r,t.firstRelease().duration(),id);
            assertEquals(v,t.firstRecovery().duration(),id); assertEquals(c,t.firstCancel().duration(),id);
        }
    }

    @Test void everySampleStaysInsideBodyAndFrameBounds() {
        for(var s:STYLES) {
            var t=t(s);
            for(float c:CHARGES) {
                for(var phase:TIMED) {
                    int d=phaseDuration(t,phase,false);
                    for(float time=0;time<=d;time+=.25F) bounds(s+" "+phase+" "+time+" c"+c,sample(t,phase,time,c,false),sampleFirst(t,phase,time,c,false));
                }
                for(float time=0;time<=60;time+=.25F) bounds(s+" HOLD "+time+" c"+c,sample(t,CastPhase.HOLD,time,c,false),sampleFirst(t,CastPhase.HOLD,time,c,false));
            }
            if(t.on()!=null) for(var phase:List.of(CastPhase.RELEASE,CastPhase.CANCEL)) {
                int d=phaseDuration(t,phase,true);
                for(float time=0;time<=d;time+=.25F) bounds(s+" passive "+phase+" "+time,sample(t,phase,time,0,true),sampleFirst(t,phase,time,0,true));
            }
        }
    }
    private static void bounds(String id,CastingPose p,HandPose h) {
        for(var arm:new CastingPose.Rotation[]{p.rightArm(),p.leftArm()}) {
            assertTrue(Math.abs(deg(arm.x()))<=130+EPS,id+" arm x "+deg(arm.x()));
            assertTrue(Math.abs(deg(arm.z()))<=95+EPS,id+" arm z "+deg(arm.z()));
        }
        assertTrue(Math.abs(deg(p.pitch()))<=14+EPS,id+" pitch "+deg(p.pitch()));
        for(var hand:new HandPose.Hand[]{h.lead(),h.support()}) {
            assertTrue(Math.abs(hand.tx())<=.35F+1e-6,id+" tx "+hand.tx());
            assertTrue(Math.abs(hand.ty())<=.25F+1e-6,id+" ty "+hand.ty());
            assertTrue(Math.abs(hand.tz())<=.2F+1e-6,id+" tz "+hand.tz());
        }
    }

    @Test void handsKeepClearOfTheCrosshair() {
        for(var s:STYLES) {
            var t=t(s);
            for(float c:CHARGES) {
                for(float time=0;time<=60;time+=.5F) {
                    var h=sampleFirst(t,CastPhase.HOLD,time,c,false);
                    assertFalse(FirstPersonHandRules.insideCrosshairZone(h.lead(),true),s+" hold lead "+time);
                    assertFalse(FirstPersonHandRules.insideCrosshairZone(h.support(),false),s+" hold support "+time);
                }
                int run=0;
                for(int tick=0;tick<=phaseDuration(t,CastPhase.RELEASE,false);tick++) {
                    var h=sampleFirst(t,CastPhase.RELEASE,tick,c,false);
                    boolean inside=FirstPersonHandRules.insideCrosshairZone(h.lead(),true)||FirstPersonHandRules.insideCrosshairZone(h.support(),false);
                    run=inside?run+1:0;
                    assertTrue(run<=2,s+" release crosses the crosshair for "+run+" ticks");
                }
            }
        }
    }

    @Test void perfectusHoldIsIconicAndStill() {
        for(var s:STYLES) {
            if(s==CastStyle.SYNAPTIC_STORM) continue;
            var t=t(s);
            assertNull(t.loop(),s.name());
            for(float c:CHARGES) {
                var body=sample(t,CastPhase.HOLD,0,c,false); var hands=sampleFirst(t,CastPhase.HOLD,0,c,false);
                assertNotEquals(CastingPose.ZERO,body,s.name());
                for(int time=0;time<=240;time++) {
                    assertEquals(body,sample(t,CastPhase.HOLD,time,c,false),s+" hold moved at "+time+" c"+c);
                    assertEquals(body,sample(t,CastPhase.HOLD,time+.37F,c,false),s+" hold moved at "+time+".37 c"+c);
                    assertEquals(hands,sampleFirst(t,CastPhase.HOLD,time,c,false),s+" first hold moved at "+time+" c"+c);
                }
            }
        }
    }

    @Test void synapticStormIsTheOnlyHoldWithATic() {
        var t=t(CastStyle.SYNAPTIC_STORM);
        assertNotNull(t.loop()); assertEquals(12,t.loop().period());
        var a=t.loop().amplitude();
        for(var r:new CastingPose.Rotation[]{a.head(),a.body(),a.rightArm(),a.leftArm(),a.rightLeg(),a.leftLeg()})
            for(float v:new float[]{r.x(),r.y(),r.z()}) assertTrue(Math.abs(deg(v))<=4+EPS);
        assertTrue(Math.abs(deg(a.pitch()))<=4+EPS);
        for(var hand:new HandPose.Hand[]{t.loop().firstAmplitude().lead(),t.loop().firstAmplitude().support()})
            for(float v:new float[]{hand.rx(),hand.ry(),hand.rz()}) assertTrue(Math.abs(deg(v))<=4+EPS);
        var still=sample(t,CastPhase.HOLD,0,0,false); boolean moved=false;
        for(int time=0;time<=120;time++) {
            var p=sample(t,CastPhase.HOLD,time,0,false);
            moved|=!p.equals(still);
            assertEquals(p.rightArm().x(),still.rightArm().x(),1e-6,"tic only spreads the arms");
            close(p,sample(t,CastPhase.HOLD,time+12,0,false),"period "+time);
        }
        assertTrue(moved);
        // Rigid and symmetric between tics.
        assertEquals(-25,deg(still.rightArm().x()),EPS); assertEquals(-60,deg(still.rightArm().z()),EPS);
        assertEquals(still.rightArm().x(),still.leftArm().x(),1e-6); assertEquals(-still.rightArm().z(),still.leftArm().z(),1e-6);
        // Release: nothing until the 1-tick snap wider, then back to the rigid hold.
        assertEquals(still,sample(t,CastPhase.RELEASE,.5F,0,false));
        assertEquals(-75,deg(sample(t,CastPhase.RELEASE,1,0,false).rightArm().z()),EPS,"snap spreads wider");
        assertEquals(75,deg(sample(t,CastPhase.RELEASE,1,0,false).leftArm().z()),EPS);
        assertEquals(still,sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false));
    }

    @Test void chargedStylesStrainMonotonically() {
        for(var s:CHARGED) assertNotNull(t(s).strain(),s.name());
        rising(CastStyle.CRIMSON_CORONATION,p -> -p.rightArm().x(),"crown rises");
        rising(CastStyle.CRIMSON_CORONATION,p -> p.rightArm().z(),"crown tightens");
        rising(CastStyle.CRIMSON_CORONATION,p -> -p.head().x(),"head rises");
        rising(CastStyle.SYNAPTIC_STORM,p -> -p.rightArm().z(),"right widens");
        rising(CastStyle.SYNAPTIC_STORM,p -> p.leftArm().z(),"left widens");
        rising(CastStyle.WHITE_VERDICT,p -> -p.head().x(),"head rises");
        rising(CastStyle.FUNERAL_BELL,CastingPose::pitch,"bow deepens");
        rising(CastStyle.LODESTONE_COLLAPSE,p -> -p.rightArm().x(),"arms lift");
        rising(CastStyle.ECLIPSE_WELL,p -> p.head().y(),"counter-rotation deepens");
        var white=sample(t(CastStyle.WHITE_VERDICT),CastPhase.HOLD,0,1,false);
        assertTrue(deg(white.head().x())<=-8,"verdict head up at least 8");
        assertEquals(14,deg(sample(t(CastStyle.FUNERAL_BELL),CastPhase.HOLD,0,1,false).pitch()),EPS);
        var lode=t(CastStyle.LODESTONE_COLLAPSE);
        assertEquals(-10,deg(sample(lode,CastPhase.HOLD,0,1,false).rightArm().x()-sample(lode,CastPhase.HOLD,0,0,false).rightArm().x()),EPS);
    }
    private static void rising(CastStyle s,ToDoubleFunction<CastingPose> f,String what) {
        double prev=Double.NEGATIVE_INFINITY;
        for(float c=0;c<=1.0001F;c+=.25F) {
            double v=f.applyAsDouble(sample(t(s),CastPhase.HOLD,30,c,false));
            assertTrue(v>prev,s+" "+what+" at charge "+c);
            prev=v;
        }
    }

    @Test void crimsonCoronationCrownsThenDropsIntoTheGuard() {
        var t=t(CastStyle.CRIMSON_CORONATION);
        var crown=sample(t,CastPhase.HOLD,0,0,false);
        assertTrue(deg(crown.rightArm().x())<=-115 && deg(crown.leftArm().x())<=-115,"arms raised to a crown");
        assertTrue(crown.rightArm().z()>0 && crown.leftArm().z()<0,"crown closes inward");
        var guard=sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false);
        assertEquals(-45,deg(guard.rightArm().x()),EPS); assertEquals(-70,deg(guard.rightArm().z()),EPS); assertEquals(70,deg(guard.leftArm().z()),EPS);
        assertEquals(2,maxPitch(t,0),.05); assertEquals(0,deg(guard.pitch()),EPS);
    }

    @Test void whiteVerdictLiftsTheUpperBodyTwoDegrees() {
        var t=t(CastStyle.WHITE_VERDICT);
        var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(-20,deg(hold.rightArm().x()),EPS); assertEquals(-90,deg(hold.rightArm().z()),EPS); assertEquals(90,deg(hold.leftArm().z()),EPS);
        assertEquals(-8,deg(hold.head().x()),EPS);
        double min=0;
        for(float time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time+=.25F) {
            double pitch=deg(sample(t,CastPhase.RELEASE,time,0,false).pitch());
            assertTrue(pitch<=EPS,"verdict never bows during release"); min=Math.min(min,pitch);
        }
        assertEquals(-2,min,.05);
    }

    @Test void funeralBellSwingsUprightOnceAndReBows() {
        var t=t(CastStyle.FUNERAL_BELL);
        var bow=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(10,deg(bow.pitch()),EPS); assertEquals(12,deg(bow.head().x()),EPS); assertEquals(-20,deg(bow.rightArm().x()),EPS);
        double min=Double.MAX_VALUE;
        for(float time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time+=.25F) min=Math.min(min,deg(sample(t,CastPhase.RELEASE,time,0,false).pitch()));
        assertTrue(min<=1,"bell swings near upright: "+min);
        assertEquals(10,deg(sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false).pitch()),EPS);
    }

    @Test void absoluteStillnessHasNoLoopAndFractures() {
        var t=t(CastStyle.ABSOLUTE_STILLNESS);
        assertNull(t.loop()); assertNull(t.strain());
        var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(-70,deg(hold.rightArm().x()),EPS); assertEquals(-70,deg(hold.leftArm().x()),EPS);
        assertEquals(-40,deg(hold.rightArm().y()),EPS,"right forearm yaws across the chest"); assertEquals(40,deg(hold.leftArm().y()),EPS);
        assertTrue(hold.head().x()>0,"head bowed");
        assertEquals(3,t.cancel().frames().get(1).tick(),"fracture takes three ticks");
        double total=Math.abs(hold.rightArm().x()), dropped=Math.abs(sample(t,CastPhase.CANCEL,3,0,false).rightArm().x()-hold.rightArm().x());
        assertTrue(dropped>=.75*total,"fracture carries most of the fall");
        // Release: one 1-tick settle of at most 1°, then nothing.
        var settled=sample(t,CastPhase.RELEASE,1,0,false);
        assertEquals(hold,sample(t,CastPhase.RELEASE,.5F,0,false));
        assertEquals(1,deg(settled.pitch()-hold.pitch()),EPS);
        assertEquals(settled,sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false));
    }

    @Test void lodestoneCollapseHoldsItsDropForTwoTicks() {
        var t=t(CastStyle.LODESTONE_COLLAPSE);
        var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(-60,deg(hold.rightArm().x()),EPS); assertEquals(-45,deg(hold.rightArm().z()),EPS); assertEquals(6,deg(hold.pitch()),EPS);
        assertEquals(10,deg(hold.rightLeg().z()),EPS);
        var frames=t.release().frames(); boolean held=false;
        for(int i=1;i<frames.size();i++)
            held|=frames.get(i).pose().equals(frames.get(i-1).pose()) && frames.get(i).tick()-frames.get(i-1).tick()==2;
        assertTrue(held,"two identical consecutive keyframes two ticks apart");
        var firstFrames=t.firstRelease().frames(); boolean firstHeld=false;
        for(int i=1;i<firstFrames.size();i++)
            firstHeld|=firstFrames.get(i).pose().equals(firstFrames.get(i-1).pose()) && firstFrames.get(i).tick()-firstFrames.get(i-1).tick()==2;
        assertTrue(firstHeld,"first person holds the drop too");
    }

    @Test void eclipseWellReleasesOnlyTheHead() {
        var t=t(CastStyle.ECLIPSE_WELL);
        var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(-20,deg(hold.body().y()),EPS); assertTrue(hold.head().y()>0,"head counter-rotated toward the target");
        assertEquals(-70,deg(hold.rightArm().x()),EPS); assertEquals(-40,deg(hold.rightArm().y()),EPS,"lead arm yaws across the chest");
        for(float c:CHARGES) for(float time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time+=.25F) {
            var p=sample(t,CastPhase.RELEASE,time,c,false); var still=sample(t,CastPhase.HOLD,0,c,false);
            close(new CastingPose(still.head(),p.body(),p.rightArm(),p.leftArm(),p.rightLeg(),p.leftLeg(),p.pitch()),still,"eclipse "+time+" c"+c);
        }
        assertEquals(hold.head(),sample(t,CastPhase.RELEASE,.5F,0,false).head());
        var turned=sample(t,CastPhase.RELEASE,1,0,false);
        assertEquals(2,Math.abs(deg(turned.head().y()-hold.head().y())),EPS);
        assertEquals(turned,sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false));
    }

    @Test void phoenixDebtFoldsOnAndThrowsOpenOff() {
        var t=t(CastStyle.PHOENIX_DEBT);
        assertNull(t.loop()); assertNull(t.strain());
        assertEquals(16,t.on().duration()); assertEquals(12,t.off().duration());
        assertEquals(16,t.firstOn().duration()); assertEquals(12,t.firstOff().duration());
        assertEquals(16,phaseDuration(t,CastPhase.RELEASE,true)); assertEquals(12,phaseDuration(t,CastPhase.CANCEL,true));
        assertEquals(0,phaseDuration(t,CastPhase.RECOVERY,true));
        for(var seg:List.of(t.on(),t.off())) { assertEquals(CastingPose.ZERO,seg.first(CastingPose.ZERO)); assertEquals(CastingPose.ZERO,seg.last(CastingPose.ZERO)); }
        for(var seg:List.of(t.firstOn(),t.firstOff())) { assertEquals(HandPose.ZERO,seg.first(HandPose.ZERO)); assertEquals(HandPose.ZERO,seg.last(HandPose.ZERO)); }
        assertNotEquals(t.on(),t.off()); assertNotEquals(t.firstOn(),t.firstOff());
        boolean folded=false,thrown=false;
        for(float time=0;time<=16;time+=.25F) {
            var p=sample(t,CastPhase.RELEASE,time,0,true);
            folded|=deg(p.rightArm().y())<=-30 && deg(p.leftArm().y())>=30 && deg(p.rightArm().x())<=-50 && deg(p.leftArm().x())<=-50;
        }
        for(float time=0;time<=12;time+=.25F) {
            var p=sample(t,CastPhase.CANCEL,time,0,true);
            thrown|=deg(p.rightArm().z())<=-60 && deg(p.leftArm().z())>=60;
        }
        assertTrue(folded,"on folds both arms across the chest"); assertTrue(thrown,"off throws both arms open");
        assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,3,0,true));
        // Manual-activation hold: planted, arms low and wide, head up.
        var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(-45,deg(hold.rightArm().x()),EPS); assertEquals(-55,deg(hold.rightArm().z()),EPS); assertEquals(55,deg(hold.leftArm().z()),EPS);
        assertEquals(-6,deg(hold.head().x()),EPS);
    }

    @Test void mirroringKeepsEveryIconicSymmetricWhereAuthoredSymmetric() {
        for(var s:STYLES) {
            if(s==CastStyle.ECLIPSE_WELL || s==CastStyle.SYNAPTIC_STORM) continue;
            var hold=sample(t(s),CastPhase.HOLD,0,1,false);
            close(hold,hold.mirror(),s+" symmetric hold");
            close(sampleFirst(t(s),CastPhase.HOLD,0,1,false),sampleFirst(t(s),CastPhase.HOLD,0,1,false).mirror(),s+" symmetric first hold");
        }
    }

    private static double maxPitch(CastingTrack t,float charge) {
        double max=-Double.MAX_VALUE;
        for(float time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time+=.25F) max=Math.max(max,deg(sample(t,CastPhase.RELEASE,time,charge,false).pitch()));
        return max;
    }
}
