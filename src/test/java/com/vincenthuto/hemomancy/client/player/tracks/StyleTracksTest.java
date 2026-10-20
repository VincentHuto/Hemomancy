package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingTrack;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Easing;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Keyframe;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Segment;
import com.vincenthuto.hemomancy.client.player.FirstPersonHandRules;
import com.vincenthuto.hemomancy.client.player.HandPose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle.*;
import static org.junit.jupiter.api.Assertions.*;

class StyleTracksTest {
    private static final EnumSet<CastStyle> STYLES=EnumSet.of(IRONHEART,UMBRAL_STEP,UMBRAL_REVERSAL,AVATAR,STAFF,
            CRIMSON_TITHE,UNCLOSING_EYE,BLOOM_OF_ROT,ENDLESS_HOUR);
    private static final EnumSet<CastStyle> SHORT=EnumSet.of(UMBRAL_STEP,UMBRAL_REVERSAL,STAFF);
    private static final float EPS=1e-4F;

    private static CastingTrack t(CastStyle s) { return StyleTracks.get(s); }
    private static float deg(float rad) { return (float)Math.toDegrees(rad); }
    private static int dur(CastingTrack t,CastPhase phase) { return phaseDuration(t,phase,false); }
    private static CastingPose hold(CastingTrack t) { return sample(t,CastPhase.HOLD,0,0,false); }
    private static CastingPose releaseEnd(CastingTrack t) { return t.release().last(CastingPose.ZERO); }
    private static boolean inside(HandPose f) {
        return FirstPersonHandRules.insideCrosshairZone(f.lead(),true) || FirstPersonHandRules.insideCrosshairZone(f.support(),false);
    }
    private static List<Segment<CastingPose>> third(CastingTrack t) {
        var list=new ArrayList<Segment<CastingPose>>();
        for(var s:java.util.Arrays.asList(t.opening(),t.hold(),t.strain(),t.release(),t.recovery(),t.cancel(),t.on(),t.off()))if(s!=null)list.add(s);
        return list;
    }
    private static List<Segment<HandPose>> first(CastingTrack t) {
        var list=new ArrayList<Segment<HandPose>>();
        for(var s:java.util.Arrays.asList(t.firstOpening(),t.firstHold(),t.firstStrain(),t.firstRelease(),t.firstRecovery(),
                t.firstCancel(),t.firstOn(),t.firstOff()))if(s!=null)list.add(s);
        return list;
    }
    private static void assertBounds(CastingPose p,String id) {
        for(var arm:new CastingPose.Rotation[]{p.rightArm(),p.leftArm()}) {
            assertTrue(Math.abs(deg(arm.x()))<=130+EPS,id+" arm x "+deg(arm.x()));
            assertTrue(Math.abs(deg(arm.z()))<=95+EPS,id+" arm z "+deg(arm.z()));
        }
        assertTrue(Math.abs(deg(p.pitch()))<=14+EPS,id+" pitch "+deg(p.pitch()));
    }
    private static void assertBounds(HandPose p,String id) {
        for(var h:new HandPose.Hand[]{p.lead(),p.support()}) {
            assertTrue(Math.abs(h.tx())<=.35F+EPS,id+" tx "+h.tx());
            assertTrue(Math.abs(h.ty())<=.25F+EPS,id+" ty "+h.ty());
            assertTrue(Math.abs(h.tz())<=.2F+EPS,id+" tz "+h.tz());
        }
    }
    private static int tickOf(Segment<CastingPose> s,java.util.function.Predicate<CastingPose> test) {
        for(var k:s.frames())if(test.test(k.pose()))return k.tick();
        return -1;
    }

    @Test void exactlyTheNineStylesResolveAndAreCached() {
        for(var style:CastStyle.values()) {
            if(STYLES.contains(style)) { assertNotNull(t(style),style.name()); assertSame(t(style),t(style),style.name()); }
            else assertNull(t(style),style.name());
        }
        assertNull(StyleTracks.get(null));
    }

    @Test void timingBudgetsAndBothViewsShareTheClock() {
        for(var style:STYLES) {
            var t=t(style); String id=style.name(); boolean sh=SHORT.contains(style);
            int o=dur(t,CastPhase.OPENING),r=dur(t,CastPhase.RELEASE),rc=dur(t,CastPhase.RECOVERY),c=dur(t,CastPhase.CANCEL);
            assertTrue(o>=8 && o<=(sh?10:12),id+" opening "+o);
            assertTrue(r>=6 && r<=(sh?8:10),id+" release "+r);
            assertTrue(rc>=12 && rc<=(sh?16:20),id+" recovery "+rc);
            assertTrue(c>=10 && c<=(sh?12:14),id+" cancel "+c);
            assertEquals(o,t.firstOpening().duration(),id+" first opening");
            assertEquals(r,t.firstRelease().duration(),id+" first release");
            assertEquals(rc,t.firstRecovery().duration(),id+" first recovery");
            assertEquals(c,t.firstCancel().duration(),id+" first cancel");
        }
    }

    @Test void everyStyleIsContinuousAndReturnsToRest() {
        for(var style:STYLES) {
            var t=t(style); String id=style.name();
            int o=dur(t,CastPhase.OPENING),r=dur(t,CastPhase.RELEASE);
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.OPENING,0,0,false),id+" opening start");
            assertEquals(sample(t,CastPhase.OPENING,o,0,false),hold(t),id+" opening->hold");
            assertEquals(hold(t),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(hold(t),sample(t,CastPhase.CANCEL,0,0,false),id+" hold->cancel");
            assertEquals(sample(t,CastPhase.RELEASE,r,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,dur(t,CastPhase.RECOVERY),0,false),id+" rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,dur(t,CastPhase.CANCEL),0,false),id+" cancel rest");
            var fHold=sampleFirst(t,CastPhase.HOLD,0,0,false);
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.OPENING,0,0,false),id+" first opening start");
            assertEquals(sampleFirst(t,CastPhase.OPENING,o,0,false),fHold,id+" first opening->hold");
            assertEquals(fHold,sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
            assertEquals(fHold,sampleFirst(t,CastPhase.CANCEL,0,0,false),id+" first hold->cancel");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,r,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,dur(t,CastPhase.RECOVERY),0,false),id+" first rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,dur(t,CastPhase.CANCEL),0,false),id+" first cancel rest");
            assertNotEquals(CastingPose.ZERO,hold(t),id+" hold");
            assertNotEquals(HandPose.ZERO,fHold,id+" first hold");
        }
    }

    @Test void everyKeyframeAndSampledReleaseStaysInBounds() {
        for(var style:STYLES) {
            var t=t(style); String id=style.name();
            for(var s:third(t))for(var k:s.frames())assertBounds(k.pose(),id+" t"+k.tick());
            for(var s:first(t))for(var k:s.frames())assertBounds(k.pose(),id+" first t"+k.tick());
            for(float c:new float[]{0,.5F,1})for(int tick=0;tick<=dur(t,CastPhase.RELEASE);tick++) {
                assertBounds(sample(t,CastPhase.RELEASE,tick,c,false),id+" release "+tick+" charge "+c);
                assertBounds(sampleFirst(t,CastPhase.RELEASE,tick,c,false),id+" first release "+tick+" charge "+c);
            }
        }
    }

    @Test void handsStayOutOfTheCrosshair() {
        for(var style:STYLES) {
            var t=t(style); String id=style.name();
            for(float c:new float[]{0,.5F,1}) {
                for(int tick=0;tick<=96;tick++)assertFalse(inside(sampleFirst(t,CastPhase.HOLD,tick,c,false)),id+" hold "+tick);
                int run=0;
                for(int tick=0;tick<=dur(t,CastPhase.RELEASE);tick++) {
                    run=inside(sampleFirst(t,CastPhase.RELEASE,tick,c,false))?run+1:0;
                    assertTrue(run<=2,id+" release crosses the crosshair at "+tick);
                }
            }
        }
    }

    @Test void ironheartPlantsBracesAndHammersToADeadStop() {
        var t=t(IRONHEART); var hold=hold(t);
        assertTrue(deg(hold.rightLeg().z())>=9,"planted stance");
        assertTrue(deg(hold.leftArm().x())<=-80 && deg(hold.leftArm().x())>=-100,"brace is level");
        assertTrue(deg(hold.leftArm().y())>=25,"brace lies across the body");
        assertTrue(deg(hold.rightArm().x())<=-110,"hammer raised");
        var rel=t.release().frames(); var fRel=t.firstRelease().frames();
        assertEquals(rel.get(rel.size()-2).pose(),rel.getLast().pose(),"hard stop");
        assertEquals(fRel.get(fRel.size()-2).pose(),fRel.getLast().pose(),"first hard stop");
        assertTrue(deg(releaseEnd(t).rightArm().x())>deg(hold.rightArm().x())+40,"hammer drives down");
        assertEquals(sample(t,CastPhase.RELEASE,rel.get(rel.size()-2).tick(),0,false),sample(t,CastPhase.RELEASE,rel.getLast().tick(),0,false));
        assertTrue(t.strain().first(CastingPose.ZERO).rightArm().x()<hold.rightArm().x(),"strain raises the hammer");
        float prevX=Float.MAX_VALUE,prevY=-Float.MAX_VALUE;
        for(int i=0;i<=10;i++) {
            float c=i/10F;
            float x=sample(t,CastPhase.HOLD,20,c,false).rightArm().x(), y=sampleFirst(t,CastPhase.HOLD,20,c,false).lead().ty();
            assertTrue(x<=prevX+1e-6F,"strain monotonic at "+c); assertTrue(y>=prevY-1e-6F,"first strain monotonic at "+c);
            prevX=x; prevY=y;
        }
        assertTrue(deg(sample(t,CastPhase.HOLD,20,1,false).rightLeg().z())>=deg(hold.rightLeg().z()),"strain keeps the plant");
    }

    @Test void umbralDeparturesMirrorAndCrossBeforeTheBodyUntwists() {
        var step=t(UMBRAL_STEP); var rev=t(UMBRAL_REVERSAL);
        assertEquals(dur(step,CastPhase.RELEASE),dur(rev,CastPhase.RELEASE));
        assertTrue(Math.abs(hold(step).body().y())>0);
        for(int tick=0;tick<=dur(step,CastPhase.RELEASE);tick++)
            assertEquals(sample(step,CastPhase.RELEASE,tick,0,false).body().y(),-sample(rev,CastPhase.RELEASE,tick,0,false).body().y(),1e-6,"tick "+tick);
        for(var style:new CastStyle[]{UMBRAL_STEP,UMBRAL_REVERSAL}) {
            var t=t(style); var hold=hold(t); String id=style.name();
            assertTrue(hold.rightArm().z()<0 && hold.leftArm().z()>0,id+" hold is open");
            int cross=tickOf(t.release(),p -> p.rightArm().z()>0 && p.leftArm().z()<0);
            float twist=Math.abs(hold.body().y());
            int untwist=tickOf(t.release(),p -> Math.abs(p.body().y())<twist/2);
            assertTrue(cross>0,id+" arms cross");
            assertTrue(untwist>0,id+" body returns");
            assertTrue(cross<untwist,id+" crosses at "+cross+" before untwisting at "+untwist);
        }
    }

    @Test void avatarOnSettlesInwardAndOffPushesOutward() {
        var t=t(AVATAR);
        assertNotNull(t.on()); assertNotNull(t.off()); assertNotNull(t.firstOn()); assertNotNull(t.firstOff());
        assertEquals(16,phaseDuration(t,CastPhase.RELEASE,true)); assertEquals(12,phaseDuration(t,CastPhase.CANCEL,true));
        assertEquals(0,phaseDuration(t,CastPhase.RECOVERY,true));
        assertEquals(List.of(0,6,10,16),t.on().frames().stream().map(Keyframe::tick).toList());
        assertEquals(16,t.firstOn().duration()); assertEquals(12,t.off().duration()); assertEquals(12,t.firstOff().duration());
        for(var s:List.of(t.on(),t.off())) { assertEquals(CastingPose.ZERO,s.first(null)); assertEquals(CastingPose.ZERO,s.last(null)); }
        for(var s:List.of(t.firstOn(),t.firstOff())) { assertEquals(HandPose.ZERO,s.first(null)); assertEquals(HandPose.ZERO,s.last(null)); }
        var settle=t.on().sample(10,CastingPose.ZERO);
        assertTrue(deg(settle.rightArm().z())>=0 && deg(settle.rightArm().z())<=15,"on settles inward");
        assertTrue(deg(settle.leftArm().z())<=0 && deg(settle.leftArm().z())>=-15,"on settles inward");
        var drawn=t.on().sample(6,CastingPose.ZERO);
        assertTrue(drawn.rightArm().z()>0 && drawn.leftArm().z()<0,"on draws inward");
        var push=t.off().frames().stream().map(Keyframe::pose).min((a,b) -> Float.compare(a.rightArm().z(),b.rightArm().z())).orElseThrow();
        assertTrue(deg(push.rightArm().z())<=-60 && deg(push.leftArm().z())>=60,"off throws outward");
        assertNotEquals(sample(t,CastPhase.RELEASE,8,0,true),sample(t,CastPhase.CANCEL,6,0,true));
        assertNotEquals(sampleFirst(t,CastPhase.RELEASE,8,0,true),sampleFirst(t,CastPhase.CANCEL,6,0,true));
        assertTrue(sampleFirst(t,CastPhase.RELEASE,10,0,true).lead().tx()<0,"first on draws in");
        assertTrue(sampleFirst(t,CastPhase.CANCEL,7,0,true).lead().tx()>0,"first off pushes out");
    }

    @Test void staffEndsOnTheCarryingLineWithTheWeaponInHand() {
        var t=t(STAFF); var end=releaseEnd(t);
        assertEquals(-70,deg(end.rightArm().x()),1); assertEquals(-15,deg(end.rightArm().z()),1);
        var lead=t.firstRelease().last(HandPose.ZERO).lead();
        assertTrue(Math.abs(lead.tx())<=.05F && Math.abs(lead.ty())<=.08F,"lead hand at the item position "+lead);
    }

    @Test void crimsonTitheReachesSweepsWideAndHauls() {
        var t=t(CRIMSON_TITHE); var hold=hold(t);
        assertTrue(deg(hold.pitch())>=6 && deg(hold.head().x())>=6,"reaching taker leans and looks down");
        boolean wide=t.release().frames().stream().map(Keyframe::pose).anyMatch(p ->
                Math.abs(deg(p.rightArm().z()))>=Math.abs(deg(hold.rightArm().z()))+15
                && Math.abs(deg(p.leftArm().z()))>=Math.abs(deg(hold.leftArm().z()))+15);
        assertTrue(wide,"wide hook");
        var end=releaseEnd(t);
        assertTrue(deg(end.rightArm().x())>=-40 && deg(end.leftArm().x())>=-40,"hauled to the hips");
        assertTrue(end.pitch()<hold.pitch(),"the haul rocks the body back");
    }

    @Test void unclosingEyeRisesSymmetricallyToTheCruciform() {
        var t=t(UNCLOSING_EYE); var end=releaseEnd(t);
        assertEquals(-20,deg(end.rightArm().x()),3); assertEquals(-20,deg(end.leftArm().x()),3);
        assertEquals(-90,deg(end.rightArm().z()),.5); assertEquals(90,deg(end.leftArm().z()),.5);
        assertTrue(deg(end.head().x())<=-6,"head up");
        assertTrue(t.release().frames().stream().anyMatch(k -> deg(k.pose().rightArm().x())<=-105),"rises before it parts");
        for(var s:List.of(t.opening(),t.release()))for(var k:s.frames()) {
            var p=k.pose();
            assertEquals(p.rightArm().x(),p.leftArm().x(),1e-6); assertEquals(p.rightArm().z(),-p.leftArm().z(),1e-6);
            assertEquals(0,p.body().y(),1e-6);
        }
    }

    @Test void bloomOfRotBowsAndOpensLow() {
        var t=t(BLOOM_OF_ROT); var hold=hold(t); var end=releaseEnd(t);
        assertTrue(deg(hold.pitch())>=8 && deg(hold.head().x())>=8,"bowed");
        assertTrue(deg(end.pitch())>=8,"stays bowed");
        for(var arm:new CastingPose.Rotation[]{end.rightArm(),end.leftArm()})assertEquals(-35,deg(arm.x()),3,"low");
        assertEquals(-70,deg(end.rightArm().z()),3,"open"); assertEquals(70,deg(end.leftArm().z()),3,"open");
    }

    @Test void endlessHourLocksEveryPartOnOneTickAndThawsLinearly() {
        var t=t(ENDLESS_HOUR); var hold=hold(t); int o=dur(t,CastPhase.OPENING);
        float[] target=parts(hold);
        for(int tick=1;tick<o;tick++) {
            float[] now=parts(sample(t,CastPhase.OPENING,tick,0,false)); float ratio=Float.NaN;
            for(int i=0;i<target.length;i++) {
                if(target[i]==0) { assertEquals(0,now[i],1e-6); continue; }
                float r=now[i]/target[i];
                assertTrue(r<1-1e-4F,"part "+i+" arrived early at "+tick);
                if(Float.isNaN(ratio))ratio=r; else assertEquals(ratio,r,1e-4,"part "+i+" out of step at "+tick);
            }
        }
        assertEquals(hold,sample(t,CastPhase.OPENING,o,0,false));
        int rc=dur(t,CastPhase.RECOVERY);
        assertTrue(rc>=16,"slow thaw");
        assertTrue(t.recovery().frames().stream().allMatch(k -> k.easing()==Easing.LINEAR),"linear thaw");
        var snap=t.recovery().first(CastingPose.ZERO);
        assertEquals(snap.rightArm().x()/2,sample(t,CastPhase.RECOVERY,rc/2F,0,false).rightArm().x(),1e-5);
        var rel=t.release().frames();
        assertEquals(rel.get(rel.size()-2).pose(),rel.getLast().pose(),"the snap holds");
        assertNull(t.loop(),"the hold is absolute");
    }

    private static float[] parts(CastingPose p) {
        var r=new CastingPose.Rotation[]{p.head(),p.body(),p.rightArm(),p.leftArm(),p.rightLeg(),p.leftLeg()};
        float[] out=new float[r.length*3+1];
        for(int i=0;i<r.length;i++) { out[i*3]=r[i].x(); out[i*3+1]=r[i].y(); out[i*3+2]=r[i].z(); }
        out[out.length-1]=p.pitch();
        return out;
    }
}
