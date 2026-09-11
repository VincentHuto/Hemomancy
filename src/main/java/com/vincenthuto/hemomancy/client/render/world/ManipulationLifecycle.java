package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.util.Mth;

/** Presentation clock; refreshing a paid/status cue never restarts its formation. */
final class ManipulationLifecycle {
    final long born;
    private final float entrance,exit;
    private final boolean sustained;
    private long until;
    private long retired=Long.MIN_VALUE;
    private float retiredFormation,retiredOpacity;

    ManipulationLifecycle(long born,int ticks,boolean sustained) {
        this(born,ticks,sustained,Math.min(8,Math.max(1,ticks*.25F)),Math.min(8,Math.max(1,ticks*.25F)));
    }

    private ManipulationLifecycle(long born,int ticks,boolean sustained,float entrance,float exit) {
        this.born=born;this.sustained=sustained;
        this.entrance=entrance;this.exit=exit;
        refresh(born,ticks);
    }

    static ManipulationLifecycle afterglow(long born,int ticks) {
        return new ManipulationLifecycle(born,ticks,false,1,Math.min(24,Math.max(1,ticks)));
    }

    void refresh(long now,int ticks) {until=now+Math.min(ticks,12000);}
    long remaining(long now) {return Math.max(0,until-now);}
    boolean expired(long now) {return now>=until;}
    boolean retiring() {return retired!=Long.MIN_VALUE;}
    boolean retire(long now) {
        if(retiring())return false;
        retiredFormation=formation(now);
        retiredOpacity=sustained?1:1-departure(now);
        retired=now;return true;
    }
    float retiredFormation() {return retiredFormation;}
    float retiredOpacity() {return retiredOpacity;}
    boolean finished(long now) {return retiring() && now>=retired+8;}
    float formation(double time) {return smooth((float)((time-born)/entrance));}
    float presence(double time) {
        return formation(time)*(sustained?1:smooth((float)((until-time)/exit)));
    }
    float residue(double time) {return retiring()?1-smooth((float)((time-retired)/8)):0;}
    float departure(double time) {return sustained?0:1-smooth((float)((until-time)/exit));}

    static float reveal(float progress,float x,float y,float z) {
        float vein=.5F+.24F*Mth.sin(x*4+y*2+z*3)+.12F*Mth.sin(y*7-x*2);
        return smooth((progress-vein*.65F)/.35F);
    }
    static float smooth(float value) {
        float t=Mth.clamp(value,0,1);return t*t*(3-2*t);
    }
}
