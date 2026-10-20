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

class FerricTracksTest {
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
            var t=FerricTracks.get(rank); int i=rank.ordinal();
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
            var t=FerricTracks.get(rank); String id=rank.name();
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
        var t=FerricTracks.get(HUMILIS);
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
            var t=FerricTracks.get(rank);
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
            var t=FerricTracks.get(rank);
            assertNotEquals(Rotation.ZERO,hold(t).leftArm(),rank.name());
            assertNotEquals(HandPose.Hand.ZERO,sampleFirst(t,CastPhase.HOLD,0,0,false).support(),rank+" first");
        }
    }

    @Test void everyRankStaysWithinBounds() {
        for(var rank:EnumManipulationRank.values()) {
            var t=FerricTracks.get(rank);
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
            var t=FerricTracks.get(rank);
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

    @Test void ferricPlantsThenDrivesDownWithAHardStop() {
        for(var rank:new EnumManipulationRank[]{HUMILIS,MEDIOCRITAS,SUMMA,MAGISTER}) {
            var t=FerricTracks.get(rank);
            assertTrue(hold(t).rightArm().x()<=rad(-105),rank+" hammer raised high");
            assertTrue(sample(t,CastPhase.RELEASE,span(t,CastPhase.RELEASE),0,false).rightArm().x()>=rad(-65),rank+" driven to chest height");
        }
        for(var rank:EnumManipulationRank.values()) {
            List<Keyframe<CastingPose>> frames=FerricTracks.get(rank).release().frames();
            var last=frames.getLast(); var settle=frames.get(frames.size()-2);
            assertEquals(settle.pose(),last.pose(),rank+" release settles on a held pose");
            assertTrue(last.tick()-settle.tick()>=2,rank+" settle lasts two ticks");
        }
    }

    @Test void supportArmBracesAcrossTheBody() {
        for(var rank:new EnumManipulationRank[]{MEDIOCRITAS,SUMMA,MAGISTER}) {
            var arm=hold(FerricTracks.get(rank)).leftArm();
            assertEquals(rad(-80),arm.x(),rad(4),rank+" brace level");
            assertTrue(arm.z()<0 && Math.abs(arm.z())>=rad(30),rank+" brace inward "+Math.toDegrees(arm.z()));
        }
    }

    @Test void stancePlantsFromSumma() {
        for(var rank:new EnumManipulationRank[]{SUMMA,MAGISTER,PERFECTUS}) {
            var pose=hold(FerricTracks.get(rank));
            assertTrue(pose.rightLeg().z()>=rad(6) && pose.leftLeg().z()<=rad(-6),rank+" stance");
            assertTrue(pose.pitch()>0,rank+" lowered");
        }
    }

    @Test void summaAndAboveRecoilUpwardAfterTheStop() {
        for(var rank:new EnumManipulationRank[]{SUMMA,MAGISTER}) {
            List<Keyframe<CastingPose>> frames=FerricTracks.get(rank).release().frames();
            int stop=-1;
            for(int i=0;i<frames.size() && stop<0;i++)if(frames.get(i).pose().rightArm().x()>=rad(-65))stop=i;
            assertTrue(stop>0 && stop+2<frames.size(),rank+" stop keyframe");
            assertTrue(frames.get(stop+1).pose().rightArm().x()<frames.get(stop).pose().rightArm().x(),rank+" recoil rises");
            assertEquals(frames.get(stop).pose(),frames.get(stop+2).pose(),rank+" recoil returns to the stop");
            assertEquals(2,frames.get(stop+2).tick()-frames.get(stop).tick(),rank+" two-tick recoil");
        }
    }

    @Test void magisterStrainClimbsAndWidensMonotonically() {
        var t=FerricTracks.get(MAGISTER);
        assertNotNull(t.strain()); assertNotNull(t.firstStrain());
        var hold=hold(t); var strain=sample(t,CastPhase.HOLD,0,1,false);
        assertTrue(strain.rightArm().x()<hold.rightArm().x(),"hammer climbs with charge");
        assertTrue(strain.rightLeg().z()>hold.rightLeg().z(),"stance widens with charge");
        assertTrue(strain.head().x()>hold.head().x(),"head lowers with charge");
        assertTrue(sampleFirst(t,CastPhase.HOLD,0,1,false).lead().ty()>sampleFirst(t,CastPhase.HOLD,0,0,false).lead().ty(),"first lead hand climbs");
        for(var rank:new EnumManipulationRank[]{MAGISTER,PERFECTUS}) {
            var track=FerricTracks.get(rank);
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

    @Test void magisterLoopSettlesTheWeightSlowly() {
        var t=FerricTracks.get(MAGISTER);
        assertNotNull(t.loop());
        assertEquals(60,t.loop().period());
        for(float v:parts(t.loop().amplitude()))assertTrue(Math.abs(v)<=rad(4)+EPS,"loop amplitude "+Math.toDegrees(v));
        float[] first=parts(t.loop().firstAmplitude());
        for(int i=0;i<first.length;i++)assertTrue(Math.abs(first[i])<=(i%6<3 ? .01F : rad(2)),"first loop amplitude "+i);
        assertNotEquals(hold(t),sample(t,CastPhase.HOLD,15,0,false),"loop moves");
        assertNotEquals(sampleFirst(t,CastPhase.HOLD,0,0,false),sampleFirst(t,CastPhase.HOLD,15,0,false),"first loop moves");
        for(var rank:new EnumManipulationRank[]{HUMILIS,MEDIOCRITAS,SUMMA,PERFECTUS})assertNull(FerricTracks.get(rank).loop(),rank.name());
    }

    @Test void perfectusIsAStillMonumentalPlant() {
        var t=FerricTracks.get(PERFECTUS);
        assertNotNull(t.strain()); assertNull(t.loop());
        for(float charge:new float[]{0,.5F,1}) {
            var held=sample(t,CastPhase.HOLD,0,charge,false); var firstHeld=sampleFirst(t,CastPhase.HOLD,0,charge,false);
            for(int tick=1;tick<=240;tick++) {
                assertEquals(held,sample(t,CastPhase.HOLD,tick,charge,false),"still at "+tick);
                assertEquals(firstHeld,sampleFirst(t,CastPhase.HOLD,tick,charge,false),"first still at "+tick);
            }
        }
        var hold=hold(t);
        assertEquals(rad(-60),hold.rightArm().x(),rad(1)); assertEquals(rad(-60),hold.leftArm().x(),rad(1));
        assertEquals(rad(-45),hold.rightArm().z(),rad(1)); assertEquals(rad(45),hold.leftArm().z(),rad(1));
        assertEquals(rad(6),hold.pitch(),rad(.5));
        assertEquals(0F,hold.head().x(),rad(.5),"head level");
        float deepest=hold.pitch(); int held=0;
        for(int tick=0;tick<=span(t,CastPhase.RELEASE);tick++) {
            var pose=sample(t,CastPhase.RELEASE,tick,0,false);
            assertEquals(new CastingPose(pose.head(),pose.body(),pose.rightArm(),pose.leftArm(),pose.rightLeg(),pose.leftLeg(),hold.pitch()),hold,
                    "only the waist drops at "+tick);
            if(pose.pitch()>deepest+EPS) { deepest=pose.pitch(); held=1; } else if(Math.abs(pose.pitch()-deepest)<=EPS && deepest>hold.pitch())held++;
        }
        assertEquals(rad(8),deepest,rad(.1),"a single 2° drop");
        assertTrue(held>=2,"drop held for two ticks");
        assertEquals(hold,sample(t,CastPhase.RELEASE,span(t,CastPhase.RELEASE),0,false),"recovers to the plant");
    }

    @Test void cancelUnloadsDownwardAndNarrowsTheStance() {
        for(var rank:EnumManipulationRank.values()) {
            var t=FerricTracks.get(rank); var hold=hold(t);
            var via=t.cancel().frames().get(1).pose();
            assertTrue(via.rightArm().x()>hold.rightArm().x(),rank+" arms unload");
            assertTrue(via.rightLeg().z()<=hold.rightLeg().z(),rank+" stance narrows");
        }
    }
}
