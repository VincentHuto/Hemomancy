package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Coherent, bounded local-space waves. Trigonometry uses Minecraft's lookup table. */
final class ManipulationUndulation {
    private final float amplitude,phase,detail,seed,baseX,baseY,baseZ;
    private final boolean beam,sword;
    private final Vec3 axis;
    private final double length,radius;

    ManipulationUndulation(Form form,double time,float age,int objectSeed,double radius,Vec3 end) {
        var school=ManipulationMaterials.forForm(form);
        amplitude=switch(school) {
            case ANIMUS -> .075F;
            case MORTEM -> .045F;
            case DUCTILIS -> .028F;
            case FERRIC -> .022F;
            case FLAMMEUS -> .055F;
            case CONGEATIO -> .028F;
            case LUX -> .04F;
            case TENEBRIS -> .07F;
        };
        float speed=switch(school) {
            case FERRIC,CONGEATIO -> .017F;
            case FLAMMEUS,DUCTILIS -> .055F;
            default -> .032F;
        };
        double clock=form==Form.STILLNESS?Math.min(age,8):time;
        int mixed=Integer.rotateLeft(objectSeed*0x9E3779B9,13);
        seed=(mixed&65535)*(Mth.TWO_PI/65536F);
        phase=seed+(float)Math.IEEEremainder(clock*speed,Math.PI*2);
        detail=seed*1.73F+(float)Math.IEEEremainder(clock*speed*.63,Math.PI*2);
        baseX=wave(0,0,0,0);baseY=wave(0,0,0,1);baseZ=wave(0,0,0,2);
        beam=form==Form.VERDICT || form==Form.VERDICT_CHARGE;
        sword=form==Form.CROWN || form==Form.CROWN_CHARGE;
        axis=end.normalize();length=end.length();this.radius=radius;
    }

    float amplitude() {return amplitude;}

    private float wave(float x,float y,float z,int component) {
        float shift=component*2.17F;
        return .30F*Mth.sin(x*1.7F+y*1.1F+z*.8F+phase+shift)
                +.12F*Mth.sin(x*3.9F-y*2.3F+z*3.1F+detail-shift)
                +.14F*Mth.sin(x*2.2F+y*3.7F-z*1.4F+seed+shift);
    }

    void offset(float x,float y,float z,Vector3f out) {
        float dx=amplitude*(wave(x,y,z,0)-baseX);
        float dy=amplitude*(wave(z,x,y,1)-baseY);
        float dz=amplitude*(wave(y,z,x,2)-baseZ);
        if(sword) {
            float tip=1-smooth(.57,.83,y);
            dx*=tip;dy*=tip;dz*=tip;
        }
        if(beam) {
            double along=x*axis.x+y*axis.y+z*axis.z;
            double radial=Math.sqrt(Math.max(0,(double)x*x+(double)y*y+(double)z*z-along*along));
            float mask=smooth(0,.4,along)*smooth(0,.4,length-along)
                    *(1-smooth(radius*.65,radius*.9,radial))*(float)Math.min(1,radius*2);
            double axial=dx*axis.x+dy*axis.y+dz*axis.z;
            dx=(float)(dx-axial*axis.x)*mask;
            dy=(float)(dy-axial*axis.y)*mask;
            dz=(float)(dz-axial*axis.z)*mask;
        }
        out.set(dx,dy,dz);
    }

    Vec3 offset(double x,double y,double z) {
        Vector3f out=new Vector3f();
        offset((float)x,(float)y,(float)z,out);
        return new Vec3(out.x,out.y,out.z);
    }

    private static float smooth(double start,double end,double value) {
        double t=Mth.clamp((value-start)/Math.max(.00001,end-start),0,1);
        return (float)(t*t*(3-2*t));
    }
}
