package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import java.util.Arrays;
import net.minecraft.world.phys.Vec3;

/** Bounded client history; never retains a world or entity reference. */
public final class NaeglerophaeonPose {
    private static final int[] RING_DELAYS={1,4,8,13};
    private static final int[] TAIL_DELAYS={2,6,11,18,26,35};
    private static final double FOLLOW_RATE=.27;
    private final Vec3[] positions=new Vec3[64];
    private final double[] yaw=new double[64], pitch=new double[64];
    private final double[] followYaw=new double[64], followPitch=new double[64];
    private long lastTick=Long.MIN_VALUE;
    public record TurnLag(float[] ringYaw,float[] ringPitch,float[] tailYaw,float[] tailPitch) {
        public static final TurnLag ZERO=new TurnLag(new float[4],new float[4],new float[6],new float[6]);
    }

    public void update(long tick,Vec3 position) {
        update(tick,position,0,0);
    }
    public void update(long tick,Vec3 position,double heading,double tilt) {
        if(tick==lastTick) return;
        long elapsed=tick-lastTick;
        if(positions[0]==null || elapsed<=0 || elapsed>5 || positions[0].distanceToSqr(position)>64) {
            Arrays.fill(positions,position);
            Arrays.fill(yaw,heading);
            Arrays.fill(pitch,tilt);
            Arrays.fill(followYaw,heading);
            Arrays.fill(followPitch,tilt);
        } else {
            int steps=(int)elapsed;
            Vec3 previous=positions[0];
            double previousYaw=yaw[0], previousPitch=pitch[0];
            double nextYaw=previousYaw+Math.atan2(Math.sin(heading-previousYaw),Math.cos(heading-previousYaw));
            System.arraycopy(positions,0,positions,steps,positions.length-steps);
            System.arraycopy(yaw,0,yaw,steps,yaw.length-steps);
            System.arraycopy(pitch,0,pitch,steps,pitch.length-steps);
            System.arraycopy(followYaw,0,followYaw,steps,followYaw.length-steps);
            System.arraycopy(followPitch,0,followPitch,steps,followPitch.length-steps);
            for(int i=1;i<steps;i++) {
                double fraction=i/(double)steps;
                positions[i]=position.lerp(previous,fraction);
                yaw[i]=nextYaw+(previousYaw-nextYaw)*fraction;
                pitch[i]=tilt+(previousPitch-tilt)*fraction;
            }
            positions[0]=position;
            yaw[0]=nextYaw;
            pitch[0]=tilt;
            double smoothYaw=followYaw[steps], smoothPitch=followPitch[steps];
            for(int i=steps-1;i>=0;i--) {
                double fraction=(steps-i)/(double)steps;
                smoothYaw+=(previousYaw+(nextYaw-previousYaw)*fraction-smoothYaw)*FOLLOW_RATE;
                smoothPitch+=(previousPitch+(tilt-previousPitch)*fraction-smoothPitch)*FOLLOW_RATE;
                followYaw[i]=smoothYaw;
                followPitch[i]=smoothPitch;
            }
        }
        lastTick=tick;
    }

    public TurnLag turnLag(float partial) {
        return new TurnLag(lag(yaw,followYaw,RING_DELAYS,partial,true),
                lag(pitch,followPitch,RING_DELAYS,partial,false),
                lag(yaw,followYaw,TAIL_DELAYS,partial,true),
                lag(pitch,followPitch,TAIL_DELAYS,partial,false));
    }
    private static float[] lag(double[] current,double[] following,int[] delays,float partial,boolean heading) {
        float[] result=new float[delays.length];
        double newer=sample(current,1-partial);
        for(int i=0;i<delays.length;i++) {
            double older=smoothSample(following,delays[i]+1-partial);
            double difference=heading?newer-older:older-newer;
            double limit=Math.toRadians(heading?45:30);
            result[i]=(float)(limit*Math.tanh(difference/limit));
            newer=older;
        }
        return result;
    }
    private static double smoothSample(double[] history,double at) {
        double index=Math.clamp(at,0,history.length-1);
        int before=(int)index, after=Math.min(history.length-1,before+1);
        double t=index-before;
        double start=history[before], end=history[after];
        double startSlope=(history[after]-history[Math.max(0,before-1)])*.5;
        double endSlope=(history[Math.min(history.length-1,after+1)]-start)*.5;
        return (2*t*t*t-3*t*t+1)*start+(t*t*t-2*t*t+t)*startSlope
                +(-2*t*t*t+3*t*t)*end+(t*t*t-t*t)*endSlope;
    }
    private static double sample(double[] history,double at) {
        double index=Math.clamp(at,0,history.length-1);
        int before=(int)index, after=Math.min(history.length-1,before+1);
        return history[before]+(history[after]-history[before])*(index-before);
    }
    public Vec3[] offsets(Vec3 position) {
        Vec3[] result=new Vec3[positions.length];
        for(int i=0;i<result.length;i++) result[i]=positions[i]==null?Vec3.ZERO:positions[i].subtract(position);
        return result;
    }
}
