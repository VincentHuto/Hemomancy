package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import java.util.Arrays;

/** Ejected fragment translations are evaluated at 20 Hz, then interpolated by the renderer. */
final class ThermalFragments {
    private final Vec3[] previous=new Vec3[30],current=new Vec3[30];
    ThermalFragments(){Arrays.fill(previous,Vec3.ZERO);Arrays.fill(current,Vec3.ZERO);}
    void update(ManipulationVisualPacket packet,double age) {
        Form form=packet.form();
        int count=form==Form.GLASS?30:form==Form.CRUOR_BREAK?7:form==Form.HOUR_BREAK?9:
                form==Form.UPDRAFT && packet.count()==4?14:0;
        for(int i=0;i<count;i++) {
            double angle=i*2.39996;
            Vec3 velocity;
            double drag,gravity;
            if(form==Form.GLASS) {
                double y=1-2*(i+.5)/count,h=Math.sqrt(1-y*y),r=Math.min(8,Math.max(.2,packet.radius()));
                velocity=new Vec3(Math.cos(angle)*h,y,Math.sin(angle)*h).scale(r*.1).add(0,.12,0);drag=.035;gravity=.0022;
            } else if(form==Form.UPDRAFT) {
                velocity=new Vec3(Math.cos(angle)*.16,.19,Math.sin(angle)*.16);
                drag=.035;gravity=.011;
            } else {
                double speed=form==Form.CRUOR_BREAK?.045:.075;
                velocity=new Vec3(Math.cos(angle)*speed,form==Form.CRUOR_BREAK?.065:.02,Math.sin(angle)*speed);
                drag=form==Form.CRUOR_BREAK?.065:.03;gravity=.002;
            }
            previous[i]=ThermalMotion.drift(velocity,Math.max(0,age-1),drag,gravity);
            current[i]=ThermalMotion.drift(velocity,age,drag,gravity);
        }
    }
    Vec3 offset(int index,float partial){return previous[index].lerp(current[index],partial);}
}
