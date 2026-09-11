package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static com.vincenthuto.hemomancy.client.render.world.VisceralMesh.*;

/** Subjects and motion for the eight school exemplars, using the shared material surfaces. */
final class ManipulationSignatureEffects {
    private ManipulationSignatureEffects() {}

    static boolean draw(ManipulationVisuals.Form form, double r, int amount, Vec3 end,
            PoseStack p, VertexConsumer v, double time, float age, float fade) {
        switch(form) {
            case CROWN, CROWN_CHARGE -> {
                boolean charging=form==ManipulationVisuals.Form.CROWN_CHARGE;
                double growth=charging?Mth.clamp(r,0,1):Mth.clamp(age/8,0,1);
                if(charging)p.translate(0,-1.62,0);
                int count=charging?(int)Math.ceil(8*growth):Math.min(8,amount);
                for(int i=0;i<count;i++) {
                    double swordGrowth=charging?VisceralGeometry.crownSwordGrowth(growth,i):growth;
                    Vec3 at=ManipulationVisuals.swordOffset(time,i);
                    if(swordGrowth<1)strand(p,v,new Vec3(at.x*.5,.65,at.z*.5),at.add(0,-.9,0),
                            .06*(1-swordGrowth),.16,time*.05+i,0xA81630,fade*(float)swordGrowth);
                    p.pushPose();p.translate(at.x,at.y-.65*(1-swordGrowth),at.z);
                    p.mulPose(Axis.YP.rotation((float)(time*.025+i*Math.PI/4)));
                    p.scale((float)(.6+.4*swordGrowth),(float)Math.max(.02,swordGrowth),1);
                    sword(p,v,fade*(float)swordGrowth,time+i*7);
                    double drip=(time*.018+i*.31)%1;
                    drop(p,v,new Vec3(.025,-1-drip*.3,0),.022*(1-drip),.05,0xAC1732,fade*(float)((1-drip)*swordGrowth),0);
                    p.popPose();
                }
            }
            case BELL, BELL_CHARGE -> {
                boolean charge=form==ManipulationVisuals.Form.BELL_CHARGE;
                double formed=charge?Mth.clamp(r,0,1):Mth.clamp(age/6,0,1);
                p.pushPose();p.translate(0,charge?1:1.8,0);
                double swing=charge?0:Math.sin(age*.42)*.10*Math.exp(-age*.045);
                p.mulPose(Axis.ZP.rotation((float)swing));
                p.scale((float)(.7+formed*.3),(float)Math.max(.05,formed), (float)(.7+formed*.3));
                for(int row=0;row<12;row++) {
                    double a=row/12.0,b=(row+1)/12.0;
                    double wa=VisceralGeometry.bellRadius(a),wb=VisceralGeometry.bellRadius(b);
                    latheBand(p,v,a*1.12,b*1.12,wa,wb,24,row<2?0x834936:0x866B5A,fade,a,b);
                    latheBand(p,v,a*1.12,b*1.12,wa-.065,wb-.065,24,0x3D292B,fade,a,b);
                }
                latheBand(p,v,0,0,.775,.84,24,0xAF7960,fade,0,.1);
                latheBand(p,v,1.12,1.16,.22,0,24,0x625243,fade,0,1);
                for(int i=0;i<7;i++) {
                    double a=i*2.39996;
                    strand(p,v,new Vec3(Math.cos(a)*.3,.8,Math.sin(a)*.3),new Vec3(Math.cos(a)*.82,-.07,Math.sin(a)*.82),
                            .015,.045,i,0x687347,fade*.7F);
                }
                strand(p,v,new Vec3(0,1.1,0),new Vec3(-swing*3,-.09,0),.03,.018,0,0x8A473E,fade);
                drop(p,v,new Vec3(-swing*3,-.1,0),.11,.16,0xB37861,fade,0);
                p.popPose();
                if(!charge)for(int i=0;i<9;i++) {
                    double a=i*2.39996, expansion=r*Math.min(1,age/16.0);
                    Vec3 tip=new Vec3(Math.cos(a)*expansion,.09,Math.sin(a)*expansion);
                    strand(p,v,tip,tip.scale(.73).add(0,.1,0),.023,.12,i,0x725343,fade*.6F);
                }
            }

            case THREAD, THREAD_CHARGE -> {
                boolean charging=form==ManipulationVisuals.Form.THREAD_CHARGE;
                Vec3 target=charging?end.normalize().scale(1.3):end;
                Vec3 source=charging?target.add(-.45,-.1,0):Vec3.ZERO;
                if(charging)target=target.add(.45,.1,0);
                Vec3 middle=source.lerp(target,.5),side=VisceralGeometry.side(target.subtract(source));
                double gap=charging?0:Math.min(.65,age*.04),kick=charging?0:Math.sin(Math.min(Math.PI,age*.17))*.35;
                for(int i=0;i<4;i++) {
                    Vec3 offset=side.scale((i-1.5)*.028);
                    Vec3 left=middle.add(side.scale(kick)).lerp(source,gap);
                    Vec3 right=middle.subtract(side.scale(kick)).lerp(target,gap);
                    strand(p,v,source.add(offset),left.add(offset),.018,.06,i+time*.008,0xB79348,fade);
                    strand(p,v,target.add(offset),right.add(offset),.018,.06,i+time*.008,0xE6C968,fade);
                    if(charging || age<7) {
                        double t=(time*.07+i*.23)%1;
                        drop(p,v,source.lerp(charging?target:left,t).add(offset),.022,.032,0xFFF5CE,fade,0);
                    }
                }
            }
            case CHOIR, WARD -> {
                boolean iron=form==ManipulationVisuals.Form.CHOIR;
                int count=Math.min(iron?3:8,amount);
                double growth=Mth.clamp(age/8,0,1);
                for(int i=0;i<count;i++) {
                    double a=time*(iron?.018:.025)+i*Math.PI*2/Math.max(1,count);
                    p.pushPose();p.translate(Math.cos(a)*1.05,1.1+(1-growth)*.3,Math.sin(a)*1.05);
                    p.mulPose(Axis.YP.rotation((float)(-a+Math.PI/2)));
                    if(iron)for(int layer=0;layer<3;layer++) {
                        p.pushPose();p.translate(0,(layer-1)*.27,layer*.018);
                        plate(p,v,.28*growth,.24,.035,0xB2A5A0,fade,layer*.21);
                        strand(p,v,new Vec3(-.18,-.12,.08),new Vec3(.19,.15,.08),.012,.04,layer,0xA13E3D,fade);
                        p.popPose();
                    } else {
                        plate(p,v,.26*growth,.43,.035,0xA44039,fade*.7F,time*.006);
                        for(int nerve=0;nerve<3;nerve++)strand(p,v,new Vec3(0,-.36,.075),new Vec3((nerve-1)*.17,.3,.075),
                                .012,.07,nerve,0xF2D58A,fade);
                    }
                    p.popPose();
                }
            }

            case SWORD_IMPACT -> {
                Vec3 forward=end.lengthSqr()<1e-6?new Vec3(0,.3,1):end.normalize();
                for(int i=0;i<12;i++) {
                    double a=i*2.39996;
                    Vec3 motion=forward.scale(.3+(i%4)*.12).add(Math.cos(a)*.45,.12+Math.sin(i)*.2,Math.sin(a)*.3);
                    Vec3 at=motion.scale(age*.12).add(0,-age*age*.0015,0);
                    strand(p,v,at.subtract(motion.scale(.25)),at,.025*(1-age/20.0),.03,i,0xB9243E,fade);
                    drop(p,v,at,.035,.065,0xBC2940,fade,0);
                }
            }
            default -> { return false; }
        }
        return true;
    }
}
