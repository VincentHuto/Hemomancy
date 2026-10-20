package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.client.player.CastingPose.Rotation;
import com.vincenthuto.hemomancy.client.player.CastingTrack.Keyframe;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import java.util.List;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static org.junit.jupiter.api.Assertions.*;

class TenebrisTracksTest {
    private static final CastPhase[] PHASES=CastPhase.values();
    private static final float EPS=1e-5F;

    private static float rad(double degrees) { return (float)Math.toRadians(degrees); }
    private static int span(CastingTrack t,CastPhase phase) { return phase==CastPhase.HOLD ? 240 : phaseDuration(t,phase,false); }
    private static CastingPose hold(CastingTrack t) { return sample(t,CastPhase.HOLD,0,0,false); }
    private static float[] parts(CastingPose p) {
        var r=new Rotation[]{p.head(),p.body(),p.rightArm(),p.leftArm(),p.rightLeg(),p.leftLeg()};
        float[] out=new float[19];
        for(int i=0;i<6;i++) { out[i*3]=r[i].x(); out[i*3+1]=r[i].y(); out[i*3+2]=r[i].z(); }
        out[18]=p.pitch();
        return out;
    }
    private static float[] parts(HandPose p) {
        var a=p.lead(); var b=p.support();
        return new float[]{a.tx(),a.ty(),a.tz(),a.rx(),a.ry(),a.rz(),b.tx(),b.ty(),b.tz(),b.rx(),b.ry(),b.rz()};
    }
    private static double distance(float[] a,float[] b) { double d=0; for(int i=0;i<a.length;i++)d+=Math.abs(a[i]-b[i]); return d; }

    @Test void timingScalesWithRank() {
        int[] opening={8,9,10,11,12}, release={6,7,8,9,10}, recovery={12,14,16,18,20}, cancel={10,11,12,13,14};
        for(var rank:EnumManipulationRank.values()) {
            var t=TenebrisTracks.get(rank); int i=rank.ordinal();
            assertEquals(opening[i],t.opening().duration(),rank+" opening");
            assertEquals(release[i],t.release().duration(),rank+" release");
            assertEquals(recovery[i],t.recovery().duration(),rank+" recovery");
            assertEquals(cancel[i],t.cancel().duration(),rank+" cancel");
            assertEquals(opening[i],t.firstOpening().duration(),rank+" first opening");
            assertEquals(release[i],t.firstRelease().duration(),rank+" first release");
            assertEquals(recovery[i],t.firstRecovery().duration(),rank+" first recovery");
            assertEquals(cancel[i],t.firstCancel().duration(),rank+" first cancel");
        }
    }

    @Test void everyRankIsContinuousAndReturnsToRest() {
        for(var rank:EnumManipulationRank.values()) {
            var t=TenebrisTracks.get(rank); String id=rank.name();
            int opening=span(t,CastPhase.OPENING), release=span(t,CastPhase.RELEASE);
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.OPENING,0,0,false),id+" opening start");
            assertEquals(sample(t,CastPhase.OPENING,opening,0,false),sample(t,CastPhase.HOLD,0,0,false),id+" opening->hold");
            assertEquals(sample(t,CastPhase.HOLD,0,0,false),sample(t,CastPhase.RELEASE,0,0,false),id+" hold->release");
            assertEquals(sample(t,CastPhase.RELEASE,release,0,false),sample(t,CastPhase.RECOVERY,0,0,false),id+" release->recovery");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RECOVERY,span(t,CastPhase.RECOVERY),0,false),id+" recovery rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,span(t,CastPhase.CANCEL),0,false),id+" cancel rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.OPENING,0,0,false),id+" first opening start");
            assertEquals(sampleFirst(t,CastPhase.OPENING,opening,0,false),sampleFirst(t,CastPhase.HOLD,0,0,false),id+" first opening->hold");
            assertEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.RELEASE,0,0,false),id+" first hold->release");
            assertEquals(sampleFirst(t,CastPhase.RELEASE,release,0,false),sampleFirst(t,CastPhase.RECOVERY,0,0,false),id+" first release->recovery");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RECOVERY,span(t,CastPhase.RECOVERY),0,false),id+" first recovery rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,span(t,CastPhase.CANCEL),0,false),id+" first cancel rest");
        }
    }

    @Test void humilisMovesOnlyTheLeadArmAndHead() {
        var t=TenebrisTracks.get(HUMILIS);
        for(var phase:PHASES)for(int tick=0;tick<=span(t,phase);tick++)for(float charge:new float[]{0,1}) {
            var pose=sample(t,phase,tick,charge,false); String id=phase+" "+tick+" "+charge;
            assertEquals(Rotation.ZERO,pose.leftArm(),id+" support arm");
            assertEquals(Rotation.ZERO,pose.body(),id+" body");
            assertEquals(Rotation.ZERO,pose.rightLeg(),id+" right leg");
            assertEquals(Rotation.ZERO,pose.leftLeg(),id+" left leg");
            assertEquals(0F,pose.pitch(),id+" pitch");
            assertEquals(HandPose.Hand.ZERO,sampleFirst(t,phase,tick,charge,false).support(),id+" first support");
        }
    }

    @Test void legsAndWaistWaitForSumma() {
        for(var rank:new EnumManipulationRank[]{HUMILIS,MEDIOCRITAS}) {
            var t=TenebrisTracks.get(rank);
            for(var phase:PHASES)for(int tick=0;tick<=span(t,phase);tick++) {
                var pose=sample(t,phase,tick,1,false);
                assertEquals(Rotation.ZERO,pose.rightLeg(),rank+" "+phase+" "+tick);
                assertEquals(Rotation.ZERO,pose.leftLeg(),rank+" "+phase+" "+tick);
                assertEquals(0F,pose.pitch(),rank+" "+phase+" "+tick);
            }
        }
    }

    @Test void mediocritasAndAboveUseBothArms() {
        for(var rank:new EnumManipulationRank[]{MEDIOCRITAS,SUMMA,MAGISTER,PERFECTUS}) {
            var t=TenebrisTracks.get(rank);
            assertNotEquals(Rotation.ZERO,hold(t).leftArm(),rank.name());
            assertNotEquals(HandPose.Hand.ZERO,sampleFirst(t,CastPhase.HOLD,0,0,false).support(),rank+" first");
        }
    }

    @Test void everyRankStaysWithinBounds() {
        for(var rank:EnumManipulationRank.values()) {
            var t=TenebrisTracks.get(rank);
            for(var phase:PHASES)for(int tick=0;tick<=span(t,phase);tick++)for(float charge:new float[]{0,.5F,1}) {
                String id=rank+" "+phase+" "+tick+" "+charge;
                var pose=sample(t,phase,tick,charge,false);
                for(var arm:new Rotation[]{pose.rightArm(),pose.leftArm()}) {
                    assertTrue(Math.abs(arm.x())<=rad(130)+EPS,id+" arm x "+Math.toDegrees(arm.x()));
                    assertTrue(Math.abs(arm.z())<=rad(95)+EPS,id+" arm z "+Math.toDegrees(arm.z()));
                }
                assertTrue(Math.abs(pose.pitch())<=rad(14)+EPS,id+" pitch");
                var first=sampleFirst(t,phase,tick,charge,false);
                for(var hand:new HandPose.Hand[]{first.lead(),first.support()}) {
                    assertTrue(Math.abs(hand.tx())<=.35F+EPS,id+" tx "+hand);
                    assertTrue(Math.abs(hand.ty())<=.25F+EPS,id+" ty "+hand);
                    assertTrue(Math.abs(hand.tz())<=.2F+EPS,id+" tz "+hand);
                }
            }
        }
    }

    @Test void handsKeepClearOfTheCrosshair() {
        for(var rank:EnumManipulationRank.values()) {
            var t=TenebrisTracks.get(rank);
            for(int tick=0;tick<=240;tick++)for(float charge:new float[]{0,1}) {
                var hold=sampleFirst(t,CastPhase.HOLD,tick,charge,false);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(hold.lead(),true),rank+" hold lead "+tick);
                assertFalse(FirstPersonHandRules.insideCrosshairZone(hold.support(),false),rank+" hold support "+tick);
            }
            int lead=0, support=0;
            for(int tick=0;tick<=span(t,CastPhase.RELEASE);tick++) {
                var release=sampleFirst(t,CastPhase.RELEASE,tick,0,false);
                lead=FirstPersonHandRules.insideCrosshairZone(release.lead(),true) ? lead+1 : 0;
                support=FirstPersonHandRules.insideCrosshairZone(release.support(),false) ? support+1 : 0;
                assertTrue(lead<=2 && support<=2,rank+" release crosses the crosshair too long at "+tick);
            }
        }
    }

    @Test void tenebrisTurnsAwayAndCrosses() {
        for(var rank:EnumManipulationRank.values()) {
            var pose=hold(TenebrisTracks.get(rank));
            assertTrue(pose.head().y()>0,rank+" head counter-turns toward the target");
            assertTrue(pose.rightArm().z()>0 && pose.rightArm().x()<=rad(-90),rank+" lead arm crosses the face");
            if(rank!=HUMILIS) {
                assertTrue(pose.body().y()<0,rank+" torso turned away");
                // Live captures (2026-10-19): 12-20 degrees read as a shoulder turn from a front-quarter camera, not a figure facing away.
                assertTrue(pose.body().y()<=rad(-25)+EPS && pose.body().y()>=rad(-30)-EPS,rank+" torso turned 25-30 degrees away");
                assertTrue(pose.leftArm().x()>0,rank+" support arm trails behind");
            }
        }
    }

    @Test void tenebrisUnfoldsLate() {
        for(var rank:new EnumManipulationRank[]{HUMILIS,MEDIOCRITAS,SUMMA,MAGISTER}) {
            var t=TenebrisTracks.get(rank); var hold=hold(t);
            List<Keyframe<CastingPose>> frames=t.release().frames();
            int arm=-1, turn=-1;
            for(var frame:frames) {
                var pose=frame.pose();
                if(arm<0 && pose.rightArm().z()<0)arm=frame.tick();
                boolean returning=rank==HUMILIS ? pose.head().y()<hold.head().y() : Math.abs(pose.body().y())<Math.abs(hold.body().y());
                if(turn<0 && returning)turn=frame.tick();
            }
            assertTrue(arm>0,rank+" lead arm sweeps to the opposite side");
            assertTrue(turn>arm,rank+" the turn returns after the arm ("+arm+" vs "+turn+")");
        }
    }

    @Test void summaAndAboveOvershootOnAnOffsetStance() {
        for(var rank:new EnumManipulationRank[]{SUMMA,MAGISTER}) {
            var t=TenebrisTracks.get(rank); boolean overshoot=false;
            for(int tick=0;tick<=span(t,CastPhase.RELEASE);tick++)overshoot|=sample(t,CastPhase.RELEASE,tick,0,false).body().y()>0;
            assertTrue(overshoot,rank+" twist overshoots");
            assertEquals(0F,sample(t,CastPhase.RELEASE,span(t,CastPhase.RELEASE),0,false).body().y(),rank+" twist settles");
        }
        for(var rank:new EnumManipulationRank[]{SUMMA,MAGISTER,PERFECTUS}) {
            var pose=hold(TenebrisTracks.get(rank));
            assertNotEquals(pose.rightLeg().z(),-pose.leftLeg().z(),rank+" offset stance");
            assertTrue(Math.abs(pose.pitch())<=rad(14),rank+" lean");
        }
    }

    @Test void magisterStrainDeepensTheTurnMonotonically() {
        var t=TenebrisTracks.get(MAGISTER);
        assertNotNull(t.strain()); assertNotNull(t.firstStrain());
        var hold=hold(t); var strain=sample(t,CastPhase.HOLD,0,1,false);
        assertTrue(strain.body().y()<hold.body().y(),"twist deepens with charge");
        assertTrue(strain.head().y()>hold.head().y(),"head counter-rotation grows with charge");
        for(var rank:new EnumManipulationRank[]{MAGISTER,PERFECTUS}) {
            var track=TenebrisTracks.get(rank);
            float[] base=parts(hold(track)), firstBase=parts(sampleFirst(track,CastPhase.HOLD,0,0,false));
            double previous=0, firstPrevious=0;
            for(int i=1;i<=10;i++) {
                float c=i/10F;
                double d=distance(parts(sample(track,CastPhase.HOLD,0,c,false)),base);
                double fd=distance(parts(sampleFirst(track,CastPhase.HOLD,0,c,false)),firstBase);
                assertTrue(d>previous,rank+" strain grows at "+c);
                assertTrue(fd>firstPrevious,rank+" first strain grows at "+c);
                previous=d; firstPrevious=fd;
            }
        }
    }

    @Test void magisterLoopSwaysSlowly() {
        var t=TenebrisTracks.get(MAGISTER);
        assertNotNull(t.loop());
        assertEquals(56,t.loop().period());
        for(float v:parts(t.loop().amplitude()))assertTrue(Math.abs(v)<=rad(4)+EPS,"loop amplitude "+Math.toDegrees(v));
        float[] first=parts(t.loop().firstAmplitude());
        for(int i=0;i<first.length;i++)assertTrue(Math.abs(first[i])<=(i%6<3 ? .02F : rad(2)),"first loop amplitude "+i);
        assertNotEquals(hold(t),sample(t,CastPhase.HOLD,14,0,false),"loop moves");
        var drift=sampleFirst(t,CastPhase.HOLD,14,0,false); var still=sampleFirst(t,CastPhase.HOLD,0,0,false);
        assertTrue(Math.signum(drift.lead().tx()-still.lead().tx())!=Math.signum(drift.support().tx()-still.support().tx()),"hands drift apart");
        for(var rank:new EnumManipulationRank[]{HUMILIS,MEDIOCRITAS,SUMMA,PERFECTUS})assertNull(TenebrisTracks.get(rank).loop(),rank.name());
    }

    @Test void perfectusIsStillAndOnlyTheHeadTurns() {
        var t=TenebrisTracks.get(PERFECTUS);
        assertNotNull(t.strain()); assertNull(t.loop());
        for(float charge:new float[]{0,.5F,1}) {
            var held=sample(t,CastPhase.HOLD,0,charge,false); var firstHeld=sampleFirst(t,CastPhase.HOLD,0,charge,false);
            for(int tick=1;tick<=240;tick++) {
                assertEquals(held,sample(t,CastPhase.HOLD,tick,charge,false),"still at "+tick);
                assertEquals(firstHeld,sampleFirst(t,CastPhase.HOLD,tick,charge,false),"first still at "+tick);
            }
            assertEquals(rad(-28),held.body().y(),"torso held 28° away at charge "+charge);
        }
        var hold=hold(t); int turned=0; float widest=0;
        for(int tick=0;tick<=span(t,CastPhase.RELEASE);tick++) {
            var pose=sample(t,CastPhase.RELEASE,tick,0,false);
            assertEquals(new CastingPose(hold.head(),pose.body(),pose.rightArm(),pose.leftArm(),pose.rightLeg(),pose.leftLeg(),pose.pitch()),hold,
                    "only the head moves at "+tick);
            float turn=Math.abs(pose.head().y()-hold.head().y());
            if(turn>EPS)turned++;
            widest=Math.max(widest,turn);
        }
        assertEquals(1,turned,"a single-tick head turn");
        assertEquals(rad(2),widest,rad(.5),"about 2°");
    }

    @Test void cancelFoldsIntoAlignmentBeforeTheArmsLower() {
        for(var rank:EnumManipulationRank.values()) {
            var t=TenebrisTracks.get(rank); int aligned=-1, lowered=-1;
            for(int tick=0;tick<=span(t,CastPhase.CANCEL);tick++) {
                var pose=sample(t,CastPhase.CANCEL,tick,0,false);
                if(aligned<0 && pose.body().y()==0)aligned=tick;
                if(lowered<0 && pose.rightArm().equals(Rotation.ZERO) && pose.leftArm().equals(Rotation.ZERO))lowered=tick;
            }
            assertTrue(aligned>=0 && lowered>aligned,rank+" untwists ("+aligned+") before the arms lower ("+lowered+")");
        }
    }
}
