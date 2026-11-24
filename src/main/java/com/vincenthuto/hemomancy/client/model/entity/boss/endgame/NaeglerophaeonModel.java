package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;

/** Pulse timing and the capture path, expressed relative to entity feet. */
public final class NaeglerophaeonModel {
    public record Input(double time, int seed, double yaw, double pitch, int phase, double phaseTicks,
                        Vec3 victim, double victimYaw, double victimHeight, Vec3[] releasedTail,
                        double releaseBlend, double hunting, NaeglerophaeonPose.TurnLag turns, boolean enraged) {
        public boolean drained() { return phase == NaeglerophaeonEntity.DRAINED; }
    }

    public record Pulse(int limb,double progress,long event,boolean spark) {}
    private static final Vec3 CORE=new Vec3(0,.8,0);
    private NaeglerophaeonModel() {}

    /** Three disjoint groups guarantee uniqueness and an upper bound of three active pulses. */
    public static List<Pulse> pulses(double time,int seed) {
        List<Pulse> result=new ArrayList<>(3);
        for(int slot=0;slot<3;slot++) {
            int period=61+slot*7;
            double clock=time+Math.floorMod(seed,197)+slot*19;
            long cycle=(long)Math.floor(clock/period);
            int duration=36+(int)Math.floorMod(cycle*7+slot*5,17);
            double age=clock-cycle*period;
            if(age<duration) result.add(new Pulse(slot*4+(int)Math.floorMod(cycle+seed,4),age/duration,cycle*3+slot,age>=duration-1));
        }
        return result;
    }
    static Vec3[] captureCurve(Input input,Vec3 root,int segments) {
        Vec3 grip=NaeglerophaeonCombatRules.gripPosition(input.victim,input.victimHeight,(float)Math.toDegrees(input.victimYaw));
        Vec3[] raw=new Vec3[193];
        double low=0,high=45;
        for(int iteration=0;iteration<11;iteration++) {
            double bend=(low+high)*.5;
            for(int j=0;j<raw.length;j++) {
                double t=j/(double)(raw.length-1);
                if(t<.8) raw[j]=bezier(root,orient(new Vec3(0,-4,-bend),input),input.victim.add(0,-3,4),grip,t/.8);
                else {
                    double theta=(1-(t-.8)/.2)*Math.PI*4;
                    raw[j]=new Vec3(Math.sin(theta)*.48,input.victimHeight*(.48+Math.sin(theta)*.12),Math.cos(theta)*.48)
                            .yRot((float)-input.victimYaw).add(input.victim);
                }
            }
            double length=0;
            for(int j=1;j<raw.length;j++) length+=raw[j].distanceTo(raw[j-1]);
            if(iteration==10) return resample(raw,length,segments);
            if(length<30) low=bend; else high=bend;
        }
        throw new IllegalStateException("Capture curve search did not complete");
    }
    private static Vec3 orient(Vec3 local,Input i) { return local.xRot((float)i.pitch).yRot((float)-i.yaw).add(CORE); }
    private static Vec3[] resample(Vec3[] raw,double length,int segments) {
        double[] distances=new double[raw.length];
        for(int j=1;j<raw.length;j++) distances[j]=distances[j-1]+raw[j].distanceTo(raw[j-1]);
        Vec3[] result=new Vec3[segments+1];
        double total=distances[distances.length-1];
        for(int j=0,index=1;j<=segments;j++) {
            double at=total*j/segments;
            while(index<raw.length-1 && distances[index]<at) index++;
            double span=distances[index]-distances[index-1];
            Vec3 p=raw[index-1].lerp(raw[index],span<1e-8?0:(at-distances[index-1])/span);
            result[j]=raw[0].add(p.subtract(raw[0]).scale(total<1e-8?0:length/total));
        }
        return result;
    }
    public static Vec3 sample(Vec3[] curve,double t) {
        double at=Math.clamp(t,0,1)*(curve.length-1); int i=Math.min(curve.length-2,(int)at);
        return curve[i].lerp(curve[i+1],at-i);
    }
    private static Vec3 bezier(Vec3 a,Vec3 b,Vec3 c,Vec3 d,double t) {
        double s=1-t; return a.scale(s*s*s).add(b.scale(3*s*s*t)).add(c.scale(3*s*t*t)).add(d.scale(t*t*t));
    }
}
