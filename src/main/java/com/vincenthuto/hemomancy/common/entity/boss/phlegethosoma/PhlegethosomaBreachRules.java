package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import net.minecraft.world.phys.Vec3;

/** The submerged windup, committed strike and dive back into the pool. */
public final class PhlegethosomaBreachRules {
    public static final BossMoveTimeline TIMELINE = new BossMoveTimeline(30,20,24);
    public static final float DAMAGE = 8;
    private PhlegethosomaBreachRules() {}

    public static Vec3 arc(Vec3 start, Vec3 end, double clearance, double progress) {
        double t = Math.clamp(progress,0,1), remaining = 1-t;
        Vec3 control = new Vec3((start.x+end.x)/2,Math.max(start.y,end.y)+clearance,(start.z+end.z)/2);
        return start.scale(remaining*remaining).add(control.scale(2*remaining*t)).add(end.scale(t*t));
    }

    public static Vec3 step(Vec3 position, Vec3 destination) {
        Vec3 delta = destination.subtract(position);
        return delta.lengthSqr() > .81 ? delta.normalize().scale(.9) : delta;
    }
}
