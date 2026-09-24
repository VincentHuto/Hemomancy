package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;

/** Arc-length sampled, continuous swimmer geometry. All returned points are relative to entity feet. */
public final class NaeglerophaeonModel {
    public record Input(double time, int seed, double yaw, double pitch, int phase, double phaseTicks,
            Vec3 victim, double victimYaw, double victimHeight, Vec3[] history, Vec3[] releasedTail,
            double releaseBlend, double hunting, NaeglerophaeonPose.TurnLag turns) {
        public Input(double time, int seed, double yaw, double pitch, int phase, double phaseTicks,
                Vec3 victim, double victimYaw, double victimHeight, Vec3[] history, Vec3[] releasedTail,
                double releaseBlend, double hunting) {
            this(time,seed,yaw,pitch,phase,phaseTicks,victim,victimYaw,victimHeight,history,releasedTail,
                    releaseBlend,hunting,NaeglerophaeonPose.TurnLag.ZERO);
        }
        public Input(double time, int seed, double yaw, double pitch, int phase, double phaseTicks,
                Vec3 victim, double victimYaw, double victimHeight, Vec3[] history, Vec3[] releasedTail,
                double releaseBlend) {
            this(time,seed,yaw,pitch,phase,phaseTicks,victim,victimYaw,victimHeight,history,releasedTail,
                    releaseBlend,0,NaeglerophaeonPose.TurnLag.ZERO);
        }
    }
    public record Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, float u0,float v0,float u1,float v1,int color,float alpha,boolean glow) {}
    public record Mesh(List<Quad> faces, Vec3[][] centers) {}
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
    public static Mesh build(Input input,int lod) {
        int segments=lod==0?24:lod==1?12:6, sides=lod==0?6:lod==1?4:3;
        List<Quad> faces=new ArrayList<>(4000);
        Vec3[][] centers=new Vec3[13][];
        var pulses=pulses(input.time,input.seed);
        double charge=input.phase==NaeglerophaeonEntity.GRAB?Math.min(1,input.phaseTicks/100)
                : input.phase==NaeglerophaeonEntity.DISCHARGE?1
                : input.phase==NaeglerophaeonEntity.CHARGE?Math.min(1,input.phaseTicks/20)*.7:0;
        sphere(faces,CORE,new Vec3(1.05,.86,1.1),12- lod*3,6-lod,charge>.7?0xFFD898:charge>.3?0x9C2941:0x310B20,charge>.3);
        for(int i=0;i<3;i++) sphere(faces,orient(new Vec3(Math.cos(i*2.1)*.48,Math.sin(i*2.1)*.4,-.2),input),
                new Vec3(.65,.62,.7),6,3,0x430D25,false);
        for(int i=0;i<7;i++) {
            double angle=i*2.39996;
            Vec3 at=orient(new Vec3(Math.cos(angle)*(.48+i*.045),Math.sin(angle)*.61,.95+Math.sin(i)*.12),input);
            orb(faces,at,.22+(i%3)*.075,charge,lod,false);
        }
        for(int limb=0;limb<13;limb++) {
            boolean tail=limb==12;
            int count=tail?segments*2:segments;
            Vec3[] raw=new Vec3[97];
            double angle=limb*Math.PI*2/12 + Math.sin(limb)*.07;
            double stroke=Math.sin(input.time*.075);
            for(int j=0;j<raw.length;j++) {
                double t=j/(double)(raw.length-1);
                double spread=tail?.45:1.05+Math.sin(t*Math.PI)*(.8+stroke*.22);
                double curl=angle+t*(tail?2:2.6)+Math.sin(input.time*.035+limb)*.15;
                Vec3 local=new Vec3(Math.cos(curl)*spread+Math.sin(t*8-input.time*.045+limb)*t*.5,
                        Math.sin(curl)*spread+Math.cos(t*7-input.time*.04+limb)*t*.45,
                        tail?-30*t:-11.2*t+Math.sin(t*5)*.65);
                if(tail) local=local.multiply(t, t, 1);
                raw[j]=orient(local,input);
                if(input.history!=null && input.history.length>1) {
                    int index=Math.min(input.history.length-1,(int)(t*(tail?60:24)));
                    raw[j]=raw[j].add(input.history[index].scale(t*.4));
                }
            }
            centers[limb]=resample(raw,tail?30:12,count);
            if(tail && input.victim!=null && (input.phase==NaeglerophaeonEntity.CAPTURE || input.phase==NaeglerophaeonEntity.GRAB)) {
                double blend=input.phase==NaeglerophaeonEntity.GRAB?1:smooth(input.phaseTicks/20);
                Vec3[] reaching=captureCurve(input,centers[limb][0],count);
                for(int j=0;j<=count;j++) centers[limb][j]=centers[limb][j].lerp(reaching[j],blend);
            }
            if(tail && input.releasedTail!=null && input.releaseBlend>0) {
                for(int j=0;j<=count;j++) centers[limb][j]=centers[limb][j].lerp(sample(input.releasedTail,j/(double)count),input.releaseBlend);
            }
            tube(faces,centers[limb],sides,tail?.22:.19,tail?0x780B28:0xFFF3DE);
            if(tail) {
                for(double at:new double[]{4,8,12,16,20,24,28,30}) orb(faces,sample(centers[limb],at/30),.13,.8,lod,true);
            } else for(int node=1;node<=3;node++) {
                double light=.08;
                for(Pulse p:pulses) if(p.limb==limb) light=Math.max(light,Math.max(0,1-Math.abs(p.progress-node/3.0)*9));
                orb(faces,sample(centers[limb],node/3.0),.16,light,lod,false);
            }
        }
        for(int limb=0;limb<12;limb++) {
            Vec3[] a=centers[limb], b=centers[(limb+1)%12];
            int webs=lod==0?7:lod==1?4:2;
            for(int j=0;j<webs;j++) {
                double t0=j/(double)webs*3.5/12,t1=(j+1)/(double)webs*3.5/12;
                Vec3 p=sample(a,t0), q=sample(a,t1), r=sample(b,t1), s=sample(b,t0);
                Vec3 m0=p.lerp(s,.5).add(0,-.12,0),m1=q.lerp(r,.5).add(0,-.12,0);
                faces.add(quad(p,q,m1,m0,0xBA193F,.28F,false));
                faces.add(quad(m0,m1,r,s,0xBA193F,.28F,false));
            }
        }
        return new Mesh(faces,centers);
    }
    private static Vec3[] captureCurve(Input input,Vec3 root,int segments) {
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
    public static Vec3[] resample(Vec3[] raw,double length,int segments) {
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
    private static void tube(List<Quad> faces,Vec3[] curve,int sides,double width,int color) {
        Vec3[] previous=null; Vec3 side=null;
        for(int j=0;j<curve.length;j++) {
            Vec3 tangent=curve[Math.min(curve.length-1,j+1)].subtract(curve[Math.max(0,j-1)]).normalize();
            if(tangent.lengthSqr()<1e-8) tangent=new Vec3(0,0,1);
            side=transportedSide(tangent,side); Vec3 up=tangent.cross(side).normalize();
            Vec3[] ring=new Vec3[sides]; double radius=width*(1-j/(double)(curve.length-1)*.9);
            for(int k=0;k<sides;k++) ring[k]=curve[j].add(side.scale(Math.cos(k*Math.PI*2/sides)*radius)).add(up.scale(Math.sin(k*Math.PI*2/sides)*radius));
            if(previous!=null) for(int k=0;k<sides;k++) faces.add(new Quad(previous[k],previous[(k+1)%sides],ring[(k+1)%sides],ring[k],k/(float)sides,(j-1F)/(curve.length-1),(k+1F)/sides,j/(float)(curve.length-1),color,1,false));
            previous=ring;
        }
    }
    private static void orb(List<Quad> faces,Vec3 center,double radius,double brightness,int lod,boolean white) {
        sphere(faces,center,new Vec3(radius,radius,radius),lod==0?6:4,lod==0?3:2,white?0xFFC844:0xB91F3B,false,.4F);
        double glowRadius=radius*(.62+brightness*.16);
        int color=white?0xFFFFDF:brightness>.4?0xFFF4AF:0xFFA52D;
        sphere(faces,center,new Vec3(glowRadius,glowRadius,glowRadius),4,2,color,true);
    }
    private static void sphere(List<Quad> faces,Vec3 center,Vec3 radius,int sides,int rows,int color,boolean glow) {
        sphere(faces,center,radius,sides,rows,color,glow,1);
    }
    private static void sphere(List<Quad> faces,Vec3 center,Vec3 radius,int sides,int rows,int color,boolean glow,float alpha) {
        Vec3[][] points=new Vec3[rows+1][sides];
        for(int j=0;j<=rows;j++) for(int k=0;k<sides;k++) {
            double p=Math.PI*j/rows,a=k*Math.PI*2/sides;
            points[j][k]=center.add(Math.sin(p)*Math.cos(a)*radius.x,Math.cos(p)*radius.y,Math.sin(p)*Math.sin(a)*radius.z);
        }
        for(int j=0;j<rows;j++) for(int k=0;k<sides;k++) faces.add(quad(points[j][k],points[j][(k+1)%sides],points[j+1][(k+1)%sides],points[j+1][k],color,alpha,glow));
    }
    private static Quad quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,int color,float alpha,boolean glow) { return new Quad(a,b,c,d,0,0,1,1,color,alpha,glow); }
    public static Vec3 transportedSide(Vec3 tangent,Vec3 previous) {
        Vec3 side=previous==null?Vec3.ZERO:previous.subtract(tangent.scale(previous.dot(tangent)));
        if(side.lengthSqr()<1e-8) side=tangent.cross(Math.abs(tangent.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0));
        return side.normalize();
    }
    public static Vec3 bezier(Vec3 a,Vec3 b,Vec3 c,Vec3 d,double t) {
        double s=1-t; return a.scale(s*s*s).add(b.scale(3*s*s*t)).add(c.scale(3*s*t*t)).add(d.scale(t*t*t));
    }
    public static double smooth(double t) { t=Math.clamp(t,0,1); return t*t*(3-2*t); }
}
