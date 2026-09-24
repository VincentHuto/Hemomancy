package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class NaeglerophaeonMembraneTest {
    private static NaeglerophaeonModel.Input pose(int phase,double yaw) {
        return new NaeglerophaeonModel.Input(123,71,yaw,.4,phase,50,
                phase==2||phase==3?new Vec3(4,-2,5):null,1.2,1.8,null,null,0);
    }
    @Test void geometryIsFiniteAndBudgeted() {
        for(int phase=0;phase<6;phase++) for(double yaw:new double[]{0,Math.PI,50}) {
            var mesh=NaeglerophaeonModel.build(pose(phase,yaw),0);
            assertEquals(13,mesh.centers().length);
            assertTrue(mesh.faces().size()<=4000,"quads="+mesh.faces().size());
            for(var q:mesh.faces()) for(var p:new Vec3[]{q.a(),q.b(),q.c(),q.d()}) assertTrue(Double.isFinite(p.lengthSqr()));
        }
    }
    @Test void tendrilsHaveSpecifiedArcLengths() {
        var mesh=NaeglerophaeonModel.build(pose(0,0),0);
        for(int limb=0;limb<13;limb++) {
            Vec3[] c=mesh.centers()[limb]; double length=0;
            for(int j=1;j<c.length;j++) length+=c[j].distanceTo(c[j-1]);
            assertEquals(limb==12?30:12,length,.12);
            if(limb<12) for(int j=8;j<c.length;j+=8) {
                double section=0; for(int k=j-7;k<=j;k++) section+=c[k].distanceTo(c[k-1]);
                assertEquals(4,section,.05);
            }
        }
    }
    @Test void pulsesHaveThreeSlotsAndOrderedTravel() {
        Map<Long,Double> progress=new HashMap<>();
        for(int tick=0;tick<5000;tick++) {
            var pulses=NaeglerophaeonModel.pulses(tick,71);
            assertTrue(pulses.size()<=3);
            Set<Integer> limbs=new HashSet<>();
            for(var p:pulses) {
                assertTrue(p.limb()<12); assertTrue(limbs.add(p.limb()));
                Double old=progress.put(p.event(),p.progress());
                if(old!=null) assertTrue(p.progress()>old);
                assertTrue(p.progress()>=0 && p.progress()<1);
            }
        }
    }
    @Test void everyPulseHasOneTerminalSparkTick() {
        Map<Long,Integer> sparks=new HashMap<>();
        for(int tick=0;tick<5000;tick++) for(var p:NaeglerophaeonModel.pulses(tick,71))
            if(p.spark()) sparks.merge(p.event(),1,Integer::sum);
        assertTrue(sparks.size()>100);
        sparks.values().forEach(n->assertEquals(1,n));
    }
    @Test void detailDecreasesAtBothDistances() {
        int near=NaeglerophaeonModel.build(pose(0,0),0).faces().size();
        int mid=NaeglerophaeonModel.build(pose(0,0),1).faces().size();
        int far=NaeglerophaeonModel.build(pose(0,0),2).faces().size();
        assertTrue(near>mid && mid>far);
    }
    @Test void gripTipMatchesSharedWeakPointWhileBossTurns() {
        for(double yaw:new double[]{0,Math.PI,50}) {
            var input=pose(3,yaw);
            var tail=NaeglerophaeonModel.build(input,0).centers()[12];
            var expected=com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules.gripPosition(input.victim(),1.8,(float)Math.toDegrees(input.victimYaw()));
            assertEquals(0,tail[tail.length-1].distanceTo(expected),1e-6);
            assertTrue(tail[tail.length-1].y<input.victim().y+1.2);
        }
    }
    @Test void historyResetsAfterTeleportOrTrackingGap() {
        var pose=new NaeglerophaeonPose(); pose.update(0,Vec3.ZERO); pose.update(1,new Vec3(.2,0,0));
        assertTrue(pose.offsets(new Vec3(.2,0,0))[1].x<0);
        pose.update(2,new Vec3(100,0,0));
        for(var offset:pose.offsets(new Vec3(100,0,0))) assertEquals(0,offset.lengthSqr());
    }
    @Test void framesRemainFiniteUnderReversals() {
        for(var tangent:new Vec3[]{new Vec3(0,1,0),new Vec3(0,-1,0),new Vec3(1,0,0)}) {
            var side=NaeglerophaeonModel.transportedSide(tangent,tangent);
            assertEquals(1,side.length(),1e-8); assertEquals(0,side.dot(tangent),1e-8);
        }
    }
    @Test void coreNormalsFaceOutward() {
        var faces=NaeglerophaeonModel.build(pose(0,0),0).faces();
        var center=new Vec3(0,.8,0);
        for(int i=0;i<72;i++) {
            var f=faces.get(i);
            var normal=f.b().subtract(f.a()).cross(f.c().subtract(f.a()));
            if(normal.lengthSqr()<1e-10) continue;
            var mid=f.a().add(f.b()).add(f.c()).add(f.d()).scale(.25);
            assertTrue(normal.dot(mid.subtract(center))>0);
        }
    }
    @Test void releaseStartsAtTheCapturedCurve() {
        var held=NaeglerophaeonModel.build(pose(3,0),0).centers()[12];
        var i=pose(5,0);
        var release=new NaeglerophaeonModel.Input(i.time(),i.seed(),i.yaw(),i.pitch(),5,0,null,0,1.8,null,held,1);
        var tail=NaeglerophaeonModel.build(release,0).centers()[12];
        for(int n=0;n<tail.length;n++) assertEquals(0,tail[n].distanceTo(held[n]),1e-8);
    }
    @Test void captureTailRetainsItsLengthWhenReachingForward() {
        var curve=NaeglerophaeonModel.build(pose(3,0),0).centers()[12];
        double length=0;
        for(int j=1;j<curve.length;j++) length+=curve[j].distanceTo(curve[j-1]);
        assertEquals(30,length,.8);
    }
}
