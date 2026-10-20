package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage.*;
import static org.junit.jupiter.api.Assertions.*;

class StillArtTracksTest {
    private static final int[] RELEASE={8,9,10,11,12},RECOVERY={14,16,18,20,24};
    private static final CastPhase[] PLAYED={CastPhase.OPENING,CastPhase.HOLD,CastPhase.RELEASE,CastPhase.RECOVERY,CastPhase.CANCEL};
    private static final float EPS=1e-5F;
    private interface Check { void at(CastPhase phase,float time); }

    private static float deg(double d) { return (float)Math.toRadians(d); }
    private static int span(CastingTrack t,CastPhase phase) { return phase==CastPhase.HOLD?48:phaseDuration(t,phase,false); }
    private static void sweep(CastingTrack t,Check check) {
        for(var phase:PLAYED)for(float time=0;time<=span(t,phase);time+=.5F)check.at(phase,time);
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
    private static void inBounds(CastingPose p,HandPose f,String id) {
        for(var arm:new CastingPose.Rotation[]{p.rightArm(),p.leftArm()}) {
            assertTrue(Math.abs(arm.x())<=deg(130)+EPS,id+" arm x "+arm);
            assertTrue(Math.abs(arm.z())<=deg(95)+EPS,id+" arm z "+arm);
        }
        assertTrue(Math.abs(p.pitch())<=deg(14)+EPS,id+" pitch");
        for(var h:new HandPose.Hand[]{f.lead(),f.support()})
            assertTrue(Math.abs(h.tx())<=.35F+EPS && Math.abs(h.ty())<=.25F+EPS && Math.abs(h.tz())<=.2F+EPS,id+" hand "+h);
    }

    @Test void everyStageHasBothViewsAndSettlesToRest() {
        for(var stage:EnumClarityStage.values()) {
            var t=StillArtTracks.get(stage); int i=stage.ordinal(); String id=stage.name();
            assertNotNull(t,id);
            assertSame(t,StillArtTracks.get(stage),id+" cached");
            assertEquals(2,phaseDuration(t,CastPhase.OPENING,false),id+" opening");
            assertEquals(RELEASE[i],phaseDuration(t,CastPhase.RELEASE,false),id+" release");
            assertEquals(RECOVERY[i],phaseDuration(t,CastPhase.RECOVERY,false),id+" settle");
            assertEquals(10,phaseDuration(t,CastPhase.CANCEL,false),id+" cancel");
            assertEquals(2,t.firstOpening().duration(),id+" first opening");
            assertEquals(RELEASE[i],t.firstRelease().duration(),id+" first release");
            assertEquals(RECOVERY[i],t.firstRecovery().duration(),id+" first settle");
            assertEquals(10,t.firstCancel().duration(),id+" first cancel");
            assertNull(t.strain(),id+" strain"); assertNull(t.loop(),id+" loop"); assertNull(t.on(),id+" on"); assertNull(t.off(),id+" off");
            int rel=RELEASE[i],rec=RECOVERY[i];
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.OPENING,0,0,false),id+" opening from rest");
            assertEquals(sample(t,CastPhase.OPENING,2,0,false),sample(t,CastPhase.HOLD,0,0,false),id+" opening->hold");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(sample(t,CastPhase.RELEASE,rel,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->settle");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,rec,0,false),id+" rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,10,0,false),id+" cancel rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.OPENING,0,0,false),id+" first opening from rest");
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,rel,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->settle");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,rec,0,false),id+" first rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,10,0,false),id+" first cancel rest");
            // The settle ends stiller than it began: near-zero velocity on the last half tick.
            assertTrue(distance(sample(t,CastPhase.RECOVERY,rec-.5F,0,false),CastingPose.ZERO)<deg(1),id+" settle velocity");
            assertTrue(distance(sampleFirst(t,CastPhase.RECOVERY,rec-.5F,0,false),HandPose.ZERO)<.01F,id+" first settle velocity");
            var frames=t.recovery().frames();
            assertTrue(distance(frames.get(frames.size()-2).pose(),CastingPose.ZERO)<=.05F*distance(frames.getFirst().pose(),CastingPose.ZERO),
                    id+" last settle keyframes nearly equal");
        }
    }
    @Test void stillArtsNeverClenchOrThrowTheHeadBack() {
        for(var stage:EnumClarityStage.values()) {
            var t=StillArtTracks.get(stage);
            sweep(t,(phase,time) -> assertTrue(sample(t,phase,time,0,false).head().x()>=-EPS,stage+" "+phase+" "+time+" head"));
            float last=-1;
            for(float time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time+=.5F) {
                float z=Math.abs(sample(t,CastPhase.RELEASE,time,0,false).rightArm().z());
                assertTrue(z>=last-EPS,stage+" lead arm closes at "+time);
                last=z;
            }
        }
        for(var ending:StillArtEnding.values())
            for(var frame:StillArtEndings.get(ending).third().frames())
                assertTrue(frame.pose().head().x()>=-EPS,ending+" head at "+frame.tick());
    }
    @Test void awakenedMovesOnlyTheMainArm() {
        var t=StillArtTracks.get(AWAKENED);
        sweep(t,(phase,time) -> {
            var p=sample(t,phase,time,0,false); String id=phase+" "+time;
            assertTrue(zero(p.leftArm()) && zero(p.body()) && zero(p.rightLeg()) && zero(p.leftLeg()),id+" "+p);
            assertEquals(0,p.pitch(),EPS,id);
            assertEquals(HandPose.Hand.ZERO,sampleFirst(t,phase,time,0,false).support(),id+" support hand");
        });
        assertFalse(FirstPersonHandRules.affectsOffHand(null,AWAKENED));
    }
    @Test void laterStagesUseBothArmsSymmetrically() {
        for(var stage:new EnumClarityStage[]{DISCERNING,VIGILANT,RESOLUTE,ENLIGHTENED}) {
            var t=StillArtTracks.get(stage);
            for(float time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time+=.5F) {
                var p=sample(t,CastPhase.RELEASE,time,0,false); var f=sampleFirst(t,CastPhase.RELEASE,time,0,false);
                assertEquals(p.rightArm(),p.leftArm().mirror(),stage+" symmetric at "+time);
                assertEquals(f.lead().ty(),f.support().ty(),EPS,stage+" first level at "+time);
                assertEquals(f.lead().tx(),-f.support().tx(),EPS,stage+" first symmetric at "+time);
            }
            var end=sample(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false);
            assertTrue(end.leftArm().x()<deg(-15),stage+" support arm lifted");
        }
    }
    @Test void vigilantAndAboveNarrowTheStanceAndBow() {
        for(var stage:new EnumClarityStage[]{VIGILANT,RESOLUTE,ENLIGHTENED}) {
            var t=StillArtTracks.get(stage);
            for(var phase:new CastPhase[]{CastPhase.HOLD,CastPhase.RELEASE})for(float time=0;time<=span(t,phase);time+=.5F) {
                var p=sample(t,phase,time,0,false);
                assertEquals(0,p.rightLeg().z(),EPS,stage+" right leg "+phase+" "+time);
                assertEquals(0,p.leftLeg().z(),EPS,stage+" left leg "+phase+" "+time);
            }
        }
        var vigilant=sample(StillArtTracks.get(VIGILANT),CastPhase.RELEASE,10,0,false);
        assertEquals(deg(4),vigilant.head().x(),EPS,"vigilant head bows 4");
        assertEquals(deg(-2),vigilant.pitch(),EPS,"vigilant straightens");
        var resolute=StillArtTracks.get(RESOLUTE);
        var peak=resolute.release().frames().get(resolute.release().frames().size()-2).pose();
        var rEnd=sample(resolute,CastPhase.RELEASE,11,0,false);
        assertEquals(deg(2),rEnd.rightArm().x()-peak.rightArm().x(),EPS,"resolute release lowers two degrees");
    }
    @Test void enlightenedIsStillAfterSettle() {
        var t=StillArtTracks.get(ENLIGHTENED);
        var end=sample(t,CastPhase.RELEASE,12,0,false);
        for(float time=8;time<=12;time+=.5F) {
            assertEquals(end,sample(t,CastPhase.RELEASE,time,0,false),"release still at "+time);
            assertEquals(sampleFirst(t,CastPhase.RELEASE,12,0,false),sampleFirst(t,CastPhase.RELEASE,time,0,false),"first still at "+time);
        }
        assertTrue(zero(end.body()) && end.pitch()==0,"enlightened body perfectly still");
        assertEquals(deg(-20),end.rightArm().x(),EPS); assertEquals(deg(-60),end.rightArm().z(),EPS);
        for(int time=24;time<=240;time++) {
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,time,0,false),"settled at "+time);
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,time,0,false),"first settled at "+time);
        }
    }
    @Test void everyEndingExistsAndSettlesByTheShortestRelease() {
        for(var ending:StillArtEnding.values()) {
            var layer=StillArtEndings.get(ending);
            assertNotNull(layer,ending.name());
            assertNotNull(layer.third(),ending+" third"); assertNotNull(layer.first(),ending+" first");
            assertTrue(layer.third().duration()<=12 && layer.first().duration()<=12,ending+" keyed on release ticks");
            // A release is at least 8 ticks; the ending must have arrived so recovery does not pop.
            assertEquals(layer.third().sample(8,CastingPose.ZERO),layer.third().last(CastingPose.ZERO),ending+" third settled by 8");
            assertEquals(layer.first().sample(8,HandPose.ZERO),layer.first().last(HandPose.ZERO),ending+" first settled by 8");
        }
        var step=StillArtEndings.get(StillArtEnding.STEP).third().last(CastingPose.ZERO);
        assertTrue(step.rightArm().x()>0,"step trails the arm back");
        assertEquals(0,step.head().x(),EPS,"step keeps the head level");
        var rebuke=StillArtEndings.get(StillArtEnding.REBUKE).third();
        assertEquals(deg(-90),rebuke.last(CastingPose.ZERO).rightArm().x(),EPS);
        assertEquals(0,rebuke.last(CastingPose.ZERO).rightArm().z(),EPS);
        for(float time=0;time<=12;time+=.5F)
            assertTrue(rebuke.sample(time,CastingPose.ZERO).rightArm().x()>=deg(-90)-EPS,"rebuke never overshoots at "+time);
        var sight=StillArtEndings.get(StillArtEnding.SIGHT).third();
        assertEquals(deg(-120),sight.last(CastingPose.ZERO).rightArm().x(),EPS);
        boolean left=false,right=false;
        for(float time=0;time<=8;time+=.5F) {
            float yaw=sight.sample(time,CastingPose.ZERO).head().y();
            if(yaw>deg(5))left=true;
            if(yaw<deg(-5) && left)right=true;
        }
        assertTrue(left && right,"sight scans left then right");
        var shear=StillArtEndings.get(StillArtEnding.SHEAR).third();
        assertTrue(shear.sample(3,CastingPose.ZERO).rightArm().y()>deg(30) && shear.last(CastingPose.ZERO).rightArm().y()<0,
                "shear cuts from outside to across the body");
        assertTrue(Math.abs(shear.last(CastingPose.ZERO).rightArm().x()-deg(-85))<deg(10),"shear stays at shoulder height");
    }
    @Test void everyStageAndEndingStaysInBoundsAndClearOfTheCrosshair() {
        for(var stage:EnumClarityStage.values()) {
            var t=StillArtTracks.get(stage);
            for(var ending:StillArtEnding.values()) {
                var aim=StillArtEndings.get(ending);
                sweep(t,(phase,time) -> {
                    String id=stage+" "+ending+" "+phase+" "+time;
                    var p=compose(t,aim,phase,time,0,false);
                    inBounds(p,composeFirst(t,aim,phase,time,0,false),id);
                    assertTrue(p.head().x()>=-EPS,id+" head");
                });
                int run=0;
                for(int time=0;time<=phaseDuration(t,CastPhase.RELEASE,false);time++) {
                    run=inside(composeFirst(t,aim,CastPhase.RELEASE,time,0,false))?run+1:0;
                    assertTrue(run<=2,stage+" "+ending+" crosses the crosshair at "+time);
                }
            }
            for(int time=0;time<=48;time++)assertFalse(inside(sampleFirst(t,CastPhase.HOLD,time,0,false)),stage+" hold "+time);
            var hold=sampleFirst(t,CastPhase.RELEASE,phaseDuration(t,CastPhase.RELEASE,false),0,false);
            if(stage!=RESOLUTE && stage!=ENLIGHTENED)
                assertTrue(hold.lead().ty()<=.12F+EPS && hold.support().ty()<=.12F+EPS,stage+" hands in the bottom third");
        }
    }
    @Test void holdHandsStayOnScreenInEveryStage() {
        for(var stage:EnumClarityStage.values())CastingTracksTest.assertHoldHandsOnScreen(StillArtTracks.get(stage),stage.name(),48);
    }
    @Test void unstainedPacketsResolveTheStageTrackAndEnding() {
        var id=UUID.randomUUID();
        for(var stage:EnumClarityStage.values())for(var ending:StillArtEnding.values()) {
            var packet=CastingAnimationPacket.stillArt(1,id,"still_pulse",1,stage,ending,CastPhase.RELEASE,0,0,InteractionHand.MAIN_HAND);
            assertSame(StillArtTracks.get(stage),CastingTracks.resolve(packet),stage+" track");
            assertSame(StillArtEndings.get(ending),CastingTracks.ending(packet),ending+" layer");
        }
        var harbinger=CastingAnimationPacket.harbinger(1,id,"blood_shot",1,
                com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS,null,
                com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.HUMILIS,null,
                com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle.SCHOOL,CastPhase.RELEASE,0,0,0,0,InteractionHand.MAIN_HAND);
        assertNull(CastingTracks.ending(harbinger));
    }
}
