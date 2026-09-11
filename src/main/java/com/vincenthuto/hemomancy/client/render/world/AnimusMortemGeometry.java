package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

/** Blood pressure, circulation and decay expressed with a small set of shader surfaces. */
final class AnimusMortemGeometry {
    private static final int TINT=0xFFFFFF;
    private AnimusMortemGeometry() {}
    static boolean handles(Form form) {
        return switch(form) {
            case CROWN,CROWN_CHARGE,CLOUD,GROWTH,COMMAND,RUSH,RUPTURE,NEEDLE_CHARGE,FAN_CHARGE,
                    LANCE_CHARGE,MORTAR_CHARGE,ANEURYSM_CHARGE,BELL,BELL_CHARGE,DRAIN,WOUND,HUNGER,
                    GRAVE,BLACK_HEART,BLOOM,DEBT,ANIMUS_IMPACT,ANIMUS_CONJURE,MORTEM_CONJURE,
                    MORTEM_BURST,GRAVE_REFUND,HUNGER_COLLAPSE,BLACKHEART_RUPTURE,TITHE_RETURN,TITHE_COLLECT,
                    ROT_INFECTION,COMMUNION -> true;
            default -> false;
        };
    }
    static boolean mortem(Form form){return ManipulationMaterials.forForm(form)==ManipulationMaterials.MORTEM;}

    static void draw(ManipulationVisualPacket packet,PoseStack p,VertexConsumer v,Vec3 camera,Vec3 right,Vec3 up,
            double time,float age,float formation,float alpha) {
        if(alpha<=0)return;
        Form form=packet.form();double r=packet.radius();int count=packet.count();
        int seed=Math.floorMod(packet.entityId()>=0?packet.entityId():packet.from().hashCode(),113);
        Vec3 end=packet.to().subtract(packet.from());
        switch(form) {
            case CROWN,CROWN_CHARGE -> {
                boolean charge=form==Form.CROWN_CHARGE;
                double progress=charge?Mth.clamp(r,.02,1):formation;
                if(charge)p.translate(0,-1.62,0);
                int swords=charge?(int)Math.ceil(8*progress):Math.min(8,count);
                for(int i=0;i<swords;i++) {
                    Vec3 at=ManipulationVisuals.swordOffset(time,i);
                    stream(p,v,new Vec3(at.x*.3,.9,at.z*.3),at.add(0,-.8,0),camera,.09,.16,time*.08+i,RIBBON,seed+i,TINT,alpha*(float)(1-progress*.7));
                    p.pushPose();p.translate(at.x,at.y,at.z);p.scale(1,(float)Math.max(.02,progress),1);
                    VisceralMesh.sword(p,new BloodSurfaceVertices(v),alpha,time+i*7);p.popPose();
                }
            }
            case NEEDLE_CHARGE,FAN_CHARGE,LANCE_CHARGE,MORTAR_CHARGE,ANEURYSM_CHARGE -> {
                Vec3 aim=end.lengthSqr()<1e-8?new Vec3(0,0,1):end.normalize();
                Vec3 focus=aim.scale(1.2).add(0,-.12,0);
                boolean mortar=form==Form.MORTAR_CHARGE,aneurysm=form==Form.ANEURYSM_CHARGE;
                int strands=form==Form.FAN_CHARGE?7:form==Form.LANCE_CHARGE?3:4;
                double size=mortar?.13+r*.23:aneurysm?.08+r*.12:.08;
                card(p,v,focus,right,up,size,size*(mortar?1.1:.7),FOCUS,seed,TINT,alpha);
                for(int i=0;i<strands;i++) {
                    double a=i*Math.PI*2/strands+time*.045;
                    Vec3 from=focus.add(right.scale(Math.cos(a)*(.45-r*.12))).add(up.scale(Math.sin(a)*.32));
                    Vec3 tip=focus.add(form==Form.FAN_CHARGE?right.scale((i-3)*.10):aim.scale(r*.35));
                    stream(p,v,from,tip,camera,.055,.1,i+time*.08,RIBBON,seed+i,TINT,alpha*.8f);
                }
            }
            case CLOUD -> {
                for(int i=0;i<8;i++) {
                    double a=i*2.39996+time*(count==3?.032:.009),distance=r*(i%3)*.22;
                    Vec3 at=new Vec3(Math.cos(a)*distance,Math.sin(a*1.7)*.18,Math.sin(a)*distance);
                    card(p,v,at,right,up,r*.65,.35+r*.14,WISP,seed+i,TINT,alpha*.65f);
                    if(i<4)stream(p,v,at,at.add(Math.sin(a)*.15,-.65,Math.cos(a)*.15),camera,.045,.06,time*.06+i,RIBBON,seed+i,TINT,alpha*.65f);
                }
            }
            case COMMAND -> {
                Vec3 crown=new Vec3(0,Math.min(2.6,r+.35),0);
                spiral(p,v,crown,camera,.34,.16,time*(count==1?.1:.025),count==3?3:2,seed,alpha);
                if(count<3 && end.lengthSqr()>.05)stream(p,v,crown,end,camera,.065,.18,time*.04,RIBBON,seed,TINT,alpha*.65f);
            }
            case RUSH -> spiral(p,v,new Vec3(0,.35,0),camera,.38,1.2,-age*.35,3,seed,alpha);
            case GROWTH -> {
                for(int i=0;i<3;i++) {
                    double a=i*2.39996+time*.025;
                    stream(p,v,new Vec3(Math.cos(a)*.25,.03,Math.sin(a)*.25),new Vec3(Math.cos(a+.8)*.07,.6,Math.sin(a+.8)*.07),camera,.06,.10,time*.04+i,RIBBON,seed+i,TINT,alpha);
                }
            }
            case BELL,BELL_CHARGE -> {
                boolean charge=form==Form.BELL_CHARGE;double progress=charge?Mth.clamp(r,.05,1):Math.max(.1,1-age/48.0);
                Vec3 center=new Vec3(0,charge?1.0:2.2,0);
                for(int i=0;i<3;i++) {
                    double a=i*Math.PI/3+Math.sin(age*.25)*.04;
                    card(p,v,center,new Vec3(Math.cos(a),0,Math.sin(a)),new Vec3(0,1,0),.95,progress*.85,SLASH,seed+i,TINT,alpha*.8f);
                }
                if(!charge)ground(p,v,new Vec3(0,.055,0),Math.max(.12,r*Math.min(1,age/12.0)),RIPPLE,seed,TINT,alpha*.8f);
                for(int i=0;i<2;i++)card(p,v,center.add((i-.5)*.8,.2,0),right,up,.5,.85,WISP,seed+i,TINT,alpha*.35f);
            }
            case DRAIN,GRAVE_REFUND -> {
                stream(p,v,end,Vec3.ZERO,camera,.10,.18,time*.025,RIBBON,seed,TINT,alpha);
                for(int i=0;i<4;i++) {
                    double travel=(age*.065+i*.25)%1;
                    Vec3 at=end.lerp(Vec3.ZERO,travel);
                    card(p,v,at,right,up,.075,.12,FOCUS,seed+i,TINT,alpha*(float)Math.sin(travel*Math.PI));
                }
            }
            case WOUND,HUNGER,GRAVE,ROT_INFECTION -> {
                // The detailed fissures are on the posed model. Wisps describe their different motion.
                for(int i=0;i<3;i++) {
                    double a=i*2.39996+seed;
                    double cycle=(time*.018+i*.33)%1;
                    double height=form==Form.HUNGER?1.4-cycle*.6:.4+cycle*.9;
                    Vec3 at=new Vec3(Math.cos(a)*.34,height,Math.sin(a)*.34);
                    card(p,v,at,right,up,.15,.28,form==Form.ROT_INFECTION?10:WISP,seed+i,TINT,alpha*.42f);
                }
            }
            case BLACK_HEART -> {
                Vec3 heart=new Vec3(0,0,0);double saturation=Mth.clamp(r/12,0,1);
                double beat=(.7+saturation*.4)*(1+Math.sin(time*(.14+saturation*.12))*.07);
                card(p,v,heart,right,up,.18*beat,.24*beat,FOCUS,seed,TINT,alpha);
                spiral(p,v,heart.add(0,-.15,0),camera,.16+saturation*.16,.22+saturation*.25,time*.014,2,seed,alpha*.65f);
            }
            case DEBT -> {
                Vec3 clot=new Vec3(0,2.25,0);
                card(p,v,clot,right,up,.18,.25,FOCUS,seed,TINT,alpha);
                spiral(p,v,clot,camera,.38,.12,-time*.015,2,seed,alpha*.7f);
            }
            case COMMUNION -> {
                for(int i=0;i<3;i++) {
                    double a=i*2.39996+seed;
                    Vec3 foot=new Vec3(Math.cos(a)*.3,.05,Math.sin(a)*.3);
                    stream(p,v,foot,foot.add(0,.65,0),camera,.07,.05,time*.018+i,RIBBON,seed+i,TINT,alpha*.6f);
                }
            }
            case BLOOM -> {
                double spread=Math.max(.1,r*Math.min(1,(age+1)/12.0));

                for(int i=0;i<7;i++) {
                    double a=i*2.39996+seed;
                    Vec3 at=new Vec3(Math.cos(a)*spread*.65,.25+Math.sin(age*.07+i)*.12,Math.sin(a)*spread*.65);
                    card(p,v,at,right,up,.40,.50,WISP,seed+i,TINT,alpha*.55f);
                }
            }
            case RUPTURE,ANIMUS_IMPACT,MORTEM_BURST,BLACKHEART_RUPTURE -> {
                double spread=Math.max(.05,Math.min(r,4)*Math.min(1,(age+1)/8.0));
                for(int i=0;i<8;i++) {
                    double a=i*2.39996+seed;
                    Vec3 at=new Vec3(Math.cos(a)*spread,.18+Math.sin(i*2.1)*.2+age*.012,Math.sin(a)*spread);
                    stream(p,v,at.scale(.65),at,camera,.09,.035,time*.025+i,RIBBON,seed+i,TINT,alpha);
                    card(p,v,at,right,up,.065,.10,FOCUS,seed+i,TINT,alpha);
                }
            }
            case ANIMUS_CONJURE,MORTEM_CONJURE -> {
                Vec3 focus=end.lengthSqr()>.001?end.normalize().scale(.8):new Vec3(0,1,0);
                spiral(p,v,focus,camera,.25,.45,time*.08,3,seed,alpha);
            }
            case HUNGER_COLLAPSE,TITHE_RETURN,TITHE_COLLECT -> {
                double shrink=Math.max(.02,1-age/20.0);Vec3 center=new Vec3(0,1,0);
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+time*.015;
                    Vec3 from=center.add(Math.cos(a)*shrink*.7,(i%2-.5)*.4,Math.sin(a)*shrink*.7);
                    stream(p,v,from,center,camera,.10,.1,time*.015+i,RIBBON,seed+i,TINT,alpha);
                }
            }
            default -> throw new IllegalArgumentException("Not a blood form: "+form);
        }
    }
    private static void spiral(PoseStack p,VertexConsumer v,Vec3 center,Vec3 camera,double radius,double height,double phase,int arms,int seed,float alpha) {
        for(int arm=0;arm<arms;arm++) {
            var points=new ArrayList<Vec3>(13);
            for(int j=0;j<=12;j++) {double t=j/12.0,a=phase+arm*Math.PI*2/arms+t*3.0;
                points.add(center.add(Math.cos(a)*radius,t*height,Math.sin(a)*radius));}
            ribbonPath(p,v,points,camera,.075,RIBBON,seed+arm,TINT,alpha);
        }
    }
}
