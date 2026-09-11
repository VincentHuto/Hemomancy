package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraBatch.Material.*;

/** Material-specific silhouettes: gas sheets, shallow pools, thin glass plates and heavy cruor. */
final class ThermalGeometry {
    private static final int WHITE=0xFFFFFF;
    private ThermalGeometry() {}

    static boolean handles(Form form) {
        return switch(form) {
            case FURNACE,IGNITION,GLASS,GLASS_CHARGE,UPDRAFT,FORGE,CAUTERIZE,PHOENIX,PHOENIX_READY,
                    FLAME_CONJURE,FROST_CONJURE,ICE,ICE_CHARGE,BONE,STILLNESS,HOUR,HOUR_BREAK,
                    CRYOGENIC_PULSE,CRUOR_FORM,CRUOR_BREAK,FROZEN_VEINS,RIMEBOUND,CRUOR_SURFACE,FROST_ADVANCE -> true;
            default -> false;
        };
    }

    static boolean cold(Form form) {
        return switch(form) {
            case ICE,ICE_CHARGE,BONE,STILLNESS,HOUR,HOUR_BREAK,CRYOGENIC_PULSE,CRUOR_FORM,CRUOR_BREAK,
                    FROZEN_VEINS,RIMEBOUND,FROST_CONJURE,CRUOR_SURFACE,FROST_ADVANCE -> true;
            default -> false;
        };
    }

    static void draw(ManipulationVisualPacket packet,PoseStack p,LuxUmbraBatch batch,VertexConsumer core,
            Vec3 from,Vec3 camera,Vec3 right,Vec3 up,double time,float age,float formation,float opacity,
            ThermalSurface surface,ThermalFragments fragments,float partial,boolean particles) {
        if(opacity<=.002)return;
        Form form=packet.form();
        int seed=packet.entityId()>=0?packet.entityId():packet.from().hashCode();
        double r=Math.min(8,Math.max(.2,packet.radius()));
        var flame=batch.vertices(FLAME);var frost=batch.vertices(CRUOR);
        switch(form) {
            case FURNACE,IGNITION -> {
                boolean burst=form==Form.IGNITION || packet.entityId()<0;
                float spread=burst?Mth.clamp(age/13,0,1):formation;
                pool(p,batch,from,surface,spread,opacity,seed);
                int count=burst?18:14;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996;
                    double distance=r*(.22+.7*random(seed,i))*spread;
                    Vec3 root=surface.onSurface(from,new Vec3(Math.cos(a)*distance,.06,Math.sin(a)*distance));
                    double cycle=burst?age*.07:(time*.075+i*.29)%1;
                    double lift=burst?Math.min(1.2,age*.08):cycle*.7;
                    fire(p,flame,root.add(0,lift,0),right,up,.25+random(seed,i+41)*.22,
                            (.55+random(seed,i+12)*.75)*(burst?1.25:1),seed+i,opacity);
                    if(particles && i%3==0)smoke(p,flame,root.add(.15,1.0+cycle*.6,0),right,up,.42,.72,seed+i,opacity*.45f);
                }
            }
            case GLASS,GLASS_CHARGE -> {
                boolean charge=form==Form.GLASS_CHARGE;
                Vec3 focus=charge?packet.to().subtract(packet.from()).normalize().scale(1.3):Vec3.ZERO;
                int count=charge?11:30;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996,y=1-2*(i+.5)/count,h=Math.sqrt(1-y*y);
                    Vec3 direction=new Vec3(Math.cos(a)*h,y,Math.sin(a)*h);
                    Vec3 at=charge?focus.add(direction.scale(.42+(.22+.06*Math.sin(time*.04+i))*(1-packet.radius()))):
                            fragments.offset(i,partial);
                    p.pushPose();p.translate(at.x,at.y,at.z);
                    p.mulPose(Axis.YP.rotation((float)(a+(charge?time*.018:age*(.04+random(seed,i)*.07)))));
                    p.mulPose(Axis.XP.rotation((float)(y*2+(charge?.2:age*.065))));
                    double size=(charge?.2:.25)+random(seed,i+7)*(charge?.16:.27);
                    shard(p,batch.vertices(GLASS),size,seed+i,opacity);
                    p.popPose();
                }
                if(charge || age<12) {
                    float flash=opacity*(charge?.65f:1-Mth.clamp(age/12,0,1));
                    fire(p,flame,focus,right,up,charge?.28:r*.5,charge?.45:r*.65,seed,flash);
                }
                if(particles && !charge)smoke(p,flame,focus.add(0,age*.025,0),right,up,r*.45,r*.55,seed+24,opacity*.45f);
            }
            case UPDRAFT -> {
                int mode=packet.count();
                boolean narrow=mode==2,hover=mode==3,blast=mode==4;
                for(int i=0;i<(blast?14:8);i++) {
                    double a=i*2.39996,spread=blast?Math.min(3.8,age*.18):narrow?.18:.4;
                    Vec3 at=new Vec3(Math.cos(a)*spread,-.25+((time*.1+i*.19)%1)*.55,Math.sin(a)*spread);
                    fire(p,flame,at,right,up,narrow?.24:blast?.45:.36,narrow?1.7:hover?1.25:1.0,seed+i,opacity);
                    if(particles && i%2==0)smoke(p,flame,at.add(0,.8,0),right,up,.4,.8,seed+i,opacity*.36f);
                    if(blast && age<34) {
                        Vec3 drop=fragments.offset(i,partial).add(0,.35,0);
                        p.pushPose();p.translate(drop.x,drop.y,drop.z);
                        p.scale(.7f,1.15f,.7f);
                        chunk(p,batch.vertices(MOLTEN),.085+random(seed,i)*.05,seed+i,opacity,1);
                        p.popPose();
                    }
                }
            }
            case FORGE -> {
                p.pushPose();p.translate(0,.95,0);
                p.pushPose();p.mulPose(Axis.YP.rotation((float)(time*.013)));
                chunk(p,batch.vertices(MOLTEN),.42,seed,opacity,2);
                p.popPose();
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI*.5+time*.04;
                    LuxUmbraGeometry.ground(p,batch.vertices(MOLTEN),new Vec3(Math.cos(a)*.14,i*.035,Math.sin(a)*.14),.48,0,seed+i,WHITE,opacity);
                    fire(p,flame,new Vec3(Math.cos(a)*.27,.04,Math.sin(a)*.27),right,up,.18,.46,seed+i,opacity);
                }
                p.popPose();
            }
            case CAUTERIZE -> {
                for(int i=0;i<5;i++) {
                    double a=i*2.39996;
                    Vec3 at=new Vec3(Math.cos(a)*.3,.4+i*.23,Math.sin(a)*.3);
                    fire(p,flame,at,right,up,.09,.22,seed+i,opacity*(1-Mth.clamp(age/24,0,1)));
                    if(particles)smoke(p,flame,at.add(0,age*.017,0),right,up,.14,.34,seed+i,opacity*.45f);
                }
            }
            case PHOENIX -> {
                double opening=Math.min(1,age/9.0);
                for(int side:new int[]{-1,1})for(int i=0;i<9;i++) {
                    double t=i/8.0;
                    Vec3 at=new Vec3(side*(.3+t*3.1)*opening,1.0+Math.sin(t*2.3)*1.3,Math.sin(t*3.0)*.4);
                    fire(p,flame,at,right,up,.3+(1-t)*.3,.5+(1-t)*1.05,seed+i,opacity);
                    if(particles && i%2==0)smoke(p,flame,at.add(0,.6+age*.016,0),right,up,.5,.85,seed+i,opacity*.4f);
                    if(particles && age>10) {
                        Vec3 cinder=at.add(side*age*.012,-Math.pow(Math.max(0,age-14)*.03,2),0);
                        LuxUmbraGeometry.card(p,flame,cinder,right,up,.035,.065,2,seed+i,WHITE,opacity);
                    }
                }
                fire(p,flame,new Vec3(0,.2,0),right,up,.8,1.3,seed+34,opacity);
            }
            case PHOENIX_READY,FLAME_CONJURE -> {
                int count=form==Form.PHOENIX_READY?3:7;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996+time*.016;
                    Vec3 at=new Vec3(Math.cos(a)*.34,.35+(i%3)*.32,Math.sin(a)*.34);
                    fire(p,flame,at,right,up,.1,.3,seed+i,opacity*.8f);
                    if(particles)smoke(p,flame,at.add(0,.4,0),right,up,.16,.36,seed+i,opacity*.25f);
                }
            }
            case STILLNESS,ICE,CRYOGENIC_PULSE -> {
                boolean still=form==Form.STILLNESS;
                double spread=still?formation:Math.min(1,age/12.0);
                cracks(p,frost,from,surface,(float)spread,opacity,seed);
                int count=still?11:16;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996,d=r*(.22+.68*random(seed,i));
                    double expansion=still?spread:1-Math.exp(-age*.1);
                    Vec3 at=new Vec3(Math.cos(a)*d*expansion,.22+(i%4)*.12,Math.sin(a)*d*expansion);
                    if(still)at=at.add(0,Math.sin(time*.013+i)*.025,0);
                    double size=(.18+random(seed,i+9)*.25)*formation;
                    clot(p,core,frost,at,size,seed+i,still?i*.7:age*.013+i,opacity);
                    if(particles)powder(p,frost,at.add(0,.02,0),right,up,.63+(still?.1:age*.009),seed+i,opacity*.9f);
                }
            }
            case ICE_CHARGE -> {
                Vec3 focus=packet.to().subtract(packet.from()).normalize().scale(1.25);
                for(int i=0;i<7;i++) {
                    double a=i*2.39996+time*.01,d=.55-packet.radius()*.24;
                    Vec3 at=focus.add(Math.cos(a)*d,Math.sin(i*1.7)*d,Math.sin(a)*d);
                    clot(p,core,frost,at,.11+packet.radius()*.1,seed+i,i+time*.012,opacity);
                    if(particles)powder(p,frost,at,right,up,.2,seed+i,opacity*.5f);
                }
            }
            case BONE,RIMEBOUND,FROZEN_VEINS,FROST_CONJURE -> {
                boolean bound=form==Form.RIMEBOUND,bloom=form==Form.BONE;
                int count=bound?9:bloom?8:4;
                double size=bound?.23:bloom?.19:.085;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996;
                    double growth=bloom?Math.min(1,age/10.0):formation;
                    double distance=(bound?.42:.3)+(bloom?Math.max(0,age-12)*.016:0);
                    Vec3 at=new Vec3(Math.cos(a)*distance,.25+(i%5)*.29,Math.sin(a)*distance);
                    if(form!=Form.FROZEN_VEINS)clot(p,core,frost,at,size*growth,seed+i,a+age*.009,opacity);
                    if(particles)powder(p,frost,at.add(0,-age*.001,0),right,up,.23,seed+i,opacity*.6f);
                }
            }
            case CRUOR_FORM,CRUOR_BREAK,FROST_ADVANCE -> {
                boolean breaking=form==Form.CRUOR_BREAK;
                double delay=form==Form.FROST_ADVANCE?Math.min(16,packet.from().distanceTo(packet.to())*1.4):0;
                if(age<delay)return;
                float growth=formation*Mth.clamp((float)(age-delay)/8,0,1);
                for(int i=0;i<7;i++) {
                    double a=i*2.39996;
                    Vec3 at=new Vec3(Math.cos(a)*.31,.2+(i%3)*.18,Math.sin(a)*.31);
                    if(breaking)at=at.add(fragments.offset(i,partial));
                    else at=at.scale(1.4-.4*growth);
                    clot(p,core,frost,at,(.16+random(seed,i)*.16)*(breaking?1:growth),seed+i,a+age*(breaking?.025:.006),opacity);
                    if(particles)powder(p,frost,at,right,up,.27,seed+i,opacity*.7f);
                }
            }
            case CRUOR_SURFACE -> blockFaces(p,frost,packet.count(),seed,opacity);
            case HOUR,HOUR_BREAK -> {
                boolean broken=form==Form.HOUR_BREAK;
                // HOUR.radius is debt, never a physical radius.
                double burden=Math.min(1,packet.radius()/30.0);
                for(int i=0;i<9;i++) {
                    double a=i*2.39996+time*.004;
                    Vec3 at=new Vec3(Math.cos(a)*.62,.25+(i%4)*.42,Math.sin(a)*.62);
                    if(broken)at=at.add(fragments.offset(i,partial));
                    clot(p,core,frost,at,.09+burden*.12,seed+i,a+(broken?age*.045:0),opacity);
                    if(particles)powder(p,frost,at,right,up,.18+burden*.12,seed+i,opacity*.35f);
                }
            }
            default -> {}
        }
    }

    private static void pool(PoseStack p,LuxUmbraBatch batch,Vec3 origin,ThermalSurface surface,float spread,float alpha,int seed) {
        int i=0;
        for(var patch:surface.patches()) {
            Vec3 at=patch.center().subtract(origin);
            if(at.horizontalDistance()>spread*8+.2){i++;continue;}
            LuxUmbraGeometry.ground(p,batch.vertices(MOLTEN),at,patch.halfWidth()*Math.max(.1,spread),0,seed+i++,WHITE,alpha);
        }
    }
    private static void cracks(PoseStack p,VertexConsumer v,Vec3 origin,ThermalSurface surface,float growth,float alpha,int seed) {
        int i=0;
        for(var patch:surface.patches())LuxUmbraGeometry.ground(p,v,patch.center().subtract(origin),patch.halfWidth(),2,seed+i++,WHITE,alpha*growth);
    }
    static void fire(PoseStack p,VertexConsumer v,Vec3 bottom,Vec3 right,Vec3 up,double width,double height,int seed,float alpha) {
        LuxUmbraGeometry.card(p,v,bottom.add(0,height*.75,0),right,up,width,height,0,seed,WHITE,alpha);
    }
    static void smoke(PoseStack p,VertexConsumer v,Vec3 at,Vec3 right,Vec3 up,double width,double height,int seed,float alpha) {
        LuxUmbraGeometry.card(p,v,at,right,up,width,height,1,seed,WHITE,alpha);
    }
    static void powder(PoseStack p,VertexConsumer v,Vec3 at,Vec3 right,Vec3 up,double size,int seed,float alpha) {
        LuxUmbraGeometry.card(p,v,at,right,up,size,size*.7,1,seed,WHITE,alpha);
    }

    static void shard(PoseStack p,VertexConsumer v,double size,int seed,float alpha) {
        Vec3[] outline={new Vec3(-.48,-.76,0),new Vec3(.24,-.52,0),new Vec3(.59,.34,0),
                new Vec3(.08,1,0),new Vec3(-.36,.22,0)};
        double thickness=size*.045;
        for(int i=0;i<outline.length;i++) {
            Vec3 a=outline[i].multiply(size*(.7+random(seed,0)*.6),size,1),b=outline[(i+1)%outline.length].multiply(size*(.7+random(seed,0)*.6),size,1);
            glassFace(p,v,new Vec3(0,0,thickness),a.add(0,0,thickness),b.add(0,0,thickness),size,seed,alpha);
            glassFace(p,v,new Vec3(0,0,-thickness),b.add(0,0,-thickness),a.add(0,0,-thickness),size,seed,alpha);
            quad(p,v,a.add(0,0,-thickness),b.add(0,0,-thickness),b.add(0,0,thickness),a.add(0,0,thickness),1,seed,alpha);
        }
    }
    private static void glassFace(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 c,double size,int seed,float alpha) {
        for(Vec3 at:new Vec3[]{a,b,c,c})vertex(p,v,at,(float)(.5+at.x/(size*2.1)),
                (float)(Math.floorMod(seed,127)*2+.45+at.y/(size*2.3)),alpha);
    }
    private static void clot(PoseStack p,VertexConsumer core,VertexConsumer shell,Vec3 at,double size,int seed,double rotation,float alpha) {
        if(size<=.005)return;
        p.pushPose();p.translate(at.x,at.y,at.z);p.mulPose(Axis.YP.rotation((float)rotation));
        p.mulPose(Axis.ZP.rotation((float)(random(seed,4)*.8-.4)));
        chunk(p,core,size,seed,alpha,0);
        chunk(p,shell,size*1.018,seed,alpha,4);
        p.popPose();
    }
    static void chunk(PoseStack p,VertexConsumer v,double size,int seed,float alpha,int shape) {
        for(int i=0;i<6;i++) {
            double a=i*Math.PI/3,b=(i+1)*Math.PI/3;
            double ra=size*(.8+random(seed,i)*.4),rb=size*(.8+random(seed,(i+1)%6)*.4);
            Vec3 low=new Vec3(Math.cos(a)*ra,-size*.55,Math.sin(a)*ra),next=new Vec3(Math.cos(b)*rb,-size*.55,Math.sin(b)*rb);
            Vec3 high=low.multiply(.76,-1.35,.76).add(size*.13,0,0),highNext=next.multiply(.76,-1.35,.76).add(size*.13,0,0);
            quad(p,v,low,next,highNext,high,shape,seed+i,alpha);
            Vec3 top=new Vec3(size*.08,size*.95,-size*.11),bottom=new Vec3(-size*.13,-size*.8,0);
            quad(p,v,high,highNext,top,top,shape,seed+i,alpha);
            quad(p,v,next,low,bottom,bottom,shape,seed+i,alpha);
        }
    }
    static void blockFaces(PoseStack p,VertexConsumer v,int mask,int seed,float alpha) {
        for(var side:net.minecraft.core.Direction.values()) {
            if((mask&(1<<side.ordinal()))==0)continue;
            Vec3 normal=Vec3.atLowerCornerOf(side.getNormal());
            Vec3 right=side.getAxis()==net.minecraft.core.Direction.Axis.Y?new Vec3(1,0,0):new Vec3(-normal.z,0,normal.x);
            Vec3 up=normal.cross(right);
            LuxUmbraGeometry.card(p,v,new Vec3(0,.5,0).add(normal.scale(.503)),right,up,.5,.5,3,seed+side.ordinal(),WHITE,alpha);
        }
    }
    private static double random(int seed,int slot){return ThermalMotion.random(seed,slot);}
    private static void quad(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int shape,int seed,float alpha) {
        vertex(p,v,a,shape*2,Math.floorMod(seed,127)*2,alpha);
        vertex(p,v,b,shape*2+1,Math.floorMod(seed,127)*2,alpha);
        vertex(p,v,c,shape*2+1,Math.floorMod(seed,127)*2+1,alpha);
        vertex(p,v,d,shape*2,Math.floorMod(seed,127)*2+1,alpha);
    }
    private static void vertex(PoseStack p,VertexConsumer v,Vec3 at,float u,float texV,float alpha) {
        v.addVertex(p.last().pose(),(float)at.x,(float)at.y,(float)at.z)
                .setColor(255,255,255,(int)(Mth.clamp(alpha,0,1)*255)).setUv(u,texV);
    }
}
