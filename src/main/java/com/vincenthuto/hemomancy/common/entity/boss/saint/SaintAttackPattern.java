package com.vincenthuto.hemomancy.common.entity.boss.saint;

/** Fixed ground footprints shared by server hit checks and their client warnings. */
public enum SaintAttackPattern {
    IRON_RAKE(1, 28, 12, 20, 14, 6),
    MARROW_RUPTURE(2, 32, 24, 24, 10, 4),
    NERVE_LANCE(3, 24, 12, 20, 14, 4),
    REGROWTH_KNOT(4, 36, 60, 20, 2.75, 3),
    SPORE_GARDEN(5, 32, 80, 20, 2, 2),
    CENSER_SWEEP(6, 26, 20, 20, 8, 5),
    FROZEN_ADVANCE(7, 34, 36, 24, 10, 4),
    SHADOW_CROSS(8, 24, 16, 22, 5, 4);

    private final int id, windup, active, recovery;
    private final double radius;
    private final float damage;
    private static final SaintAttackPattern[] BY_CUE=values();
    SaintAttackPattern(int id,int windup,int active,int recovery,double radius,float damage) {
        this.id=id;this.windup=windup;this.active=active;this.recovery=recovery;this.radius=radius;this.damage=damage;
    }
    public int id(){return id;}
    public int cue(){return 100+id;}
    public int windup(){return windup;}
    public int active(){return active;}
    public int duration(){return windup+active+recovery;}
    public double radius(){return radius;}
    public float damage(){return damage;}
    public record Motion(float gather,float strike) {}
    public Motion motion(float remaining) {
        float age=Math.max(0,Math.min(duration(),duration()-remaining));
        float recover=1-com.vincenthuto.hemomancy.common.util.HemoEasing.smoothstep((age-windup-active)/(float)recovery);
        return new Motion(com.vincenthuto.hemomancy.common.util.HemoEasing.smoothstep(age/windup)*recover,
                com.vincenthuto.hemomancy.common.util.HemoEasing.pulse(age-windup,3,Math.min(active,16))*recover);
    }
    public boolean pulsed(){return this==REGROWTH_KNOT || this==SPORE_GARDEN;}
    public boolean damageTick(int age) {
        return age>=windup && age<windup+active && (!pulsed() || (age-windup)%20==0);
    }
    public static SaintAttackPattern fromCue(int cue) {
        int index=cue-101;
        return index>=0 && index<BY_CUE.length?BY_CUE[index]:null;
    }
    public static SaintAttackPattern byId(int id) {return fromCue(id+100);}
    public double progress(double age){return Math.max(0,Math.min(1,(age-windup)/active));}
    public double sweepAngle(double age){return -Math.PI/3+progress(age)*Math.PI*2/3;}
    public boolean hits(double x,double z,double targetX,double targetZ,double age) {
        double length=Math.hypot(targetX,targetZ);
        double dx=length>.001?targetX/length:0,dz=length>.001?targetZ/length:1;
        double forward=x*dx+z*dz,side=x*dz-z*dx;
        double tx=x-targetX,tz=z-targetZ;
        return switch(this) {
            case IRON_RAKE -> forward>=0 && forward<=radius && lane(side,.5);
            case MARROW_RUPTURE -> Math.hypot(x,z)<=radius && Math.abs(Math.hypot(x,z)-radius*progress(age))<=.8;
            case NERVE_LANCE -> forward>=0 && forward<=radius && Math.abs(side)<=.65;
            case REGROWTH_KNOT -> tx*tx+tz*tz<=radius*radius;
            case SPORE_GARDEN -> tx*tx+tz*tz<=4 || (tx-3)*(tx-3)+(tz-2)*(tz-2)<=4
                    || (tx+3)*(tx+3)+(tz-2)*(tz-2)<=4;
            case CENSER_SWEEP -> {
                double angle=Math.atan2(side,forward);
                yield Math.hypot(x,z)<=radius && Math.hypot(x,z)>=1 && Math.abs(angle)<=Math.PI/3
                        && Math.abs(angle-sweepAngle(age))<=.18;
            }
            case FROZEN_ADVANCE -> forward>=0 && forward<=radius && lane(side,.45) && Math.abs(forward-radius*progress(age))<=.8;
            case SHADOW_CROSS -> Math.abs(tx)<=radius && Math.abs(tz)<=radius && (Math.abs(tx)<=.65 || Math.abs(tz)<=.65);
        };
    }
    private static boolean lane(double side,double width) {return Math.abs(side)<=width || Math.abs(side-2)<=width || Math.abs(side+2)<=width;}
}
