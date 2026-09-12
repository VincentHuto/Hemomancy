package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Iron has physical faces; blood and nerve connections are joined ribbons with shader density. */
final class FerricDuctilisGeometry {
    static final VertexConsumer DISCARD=new VertexConsumer() {
        public VertexConsumer addVertex(float x,float y,float z){return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        public VertexConsumer setUv(float u,float v){return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}
    };
    static final int NERVE=0xFFE8A0, BLOOD=0x9C263B;
    private static int ribbons;
    private FerricDuctilisGeometry() {}
    static void begin(){ribbons=0;}
    static boolean handles(Form form) {
        return switch(form) {
            case IRON_HEART,IRON_CHARGE,CHOIR,RETORT,MENDING,MAGNET,MARK,CIRCUIT,WARD,LIGHTNING_CHARGE,THREAD,THREAD_CHARGE,
                    FERRIC_IMPACT,FERRIC_CONJURE,NERVE_PULSE,NERVE_HIT,PARALYSIS -> true;
            default -> false;
        };
    }

    static void draw(ManipulationVisualPacket packet,PoseStack p,VertexConsumer iron,VertexConsumer flow,
            Vec3 camera,Vec3 right,Vec3 up,double time,float age,float formation,float opacity,LivingEntity body) {
        if(opacity<.002)return;
        int seed=packet.entityId()>=0?packet.entityId():packet.from().hashCode();
        double height=body==null?1.8:body.getBbHeight(),width=body==null?.65:body.getBbWidth();
        Vec3 end=packet.to().subtract(packet.from());
        Vec3 aim=body==null?end.normalize():body.getViewVector((float)(time-Math.floor(time)));
        switch(packet.form()) {
            case IRON_HEART,IRON_CHARGE -> {
                boolean charge=packet.form()==Form.IRON_CHARGE;
                Vec3 at=charge?ChargeVisualGeometry.focus(packet.form(),aim):Vec3.ZERO;
                float growth=charge?Mth.clamp(packet.radius(),0,1):formation;
                p.pushPose();p.translate(at.x,at.y,at.z);
                for(int i=0;i<3;i++) {
                    double x=(i-1)*.11;
                    double y=(charge?.05:0)-.08+Math.abs(i-1)*.075;
                    plate(p,iron,new Vec3(x,y,0),.11,.23-Math.abs(i-1)*.04,.055,seed+i,growth*opacity);
                }
                ribbon(p,flow,new Vec3(-.18,.12,.034),new Vec3(.16,-.12,.034),.026,.025,time,seed,camera,BLOOD,opacity);
                if(growth<1)gather(p,flow,Vec3.ZERO,.4,1-growth,time,seed,camera,opacity);
                p.popPose();
            }
            case CHOIR -> {
                int count=Math.min(3,packet.count());
                for(int i=0;i<count;i++) {
                    double a=time*.018+i*Math.PI*2/Math.max(1,count);
                    p.pushPose();p.translate(Math.cos(a)*1.05,1.15,Math.sin(a)*1.05);p.mulPose(Axis.YP.rotation((float)(-a+Math.PI/2)));
                    for(int row=0;row<3;row++)plate(p,iron,new Vec3(0,(row-1)*.27,row*.02),.52,.31,.055,seed+i*3+row,formation*opacity);
                    p.popPose();
                }
                if(formation<1)gather(p,flow,new Vec3(0,.8,0),1.05,1-formation,time,seed,camera,opacity);
            }
            case RETORT -> {
                p.pushPose();p.translate(0,height*.62,0);
                if(body!=null)p.mulPose(Axis.YP.rotationDegrees(-body.yBodyRot));
                for(int i=0;i<3;i++)plate(p,iron,new Vec3((i-1)*.29,Math.abs(i-1)*-.08,.48),.32,.52,.065,seed+i,formation*opacity);
                p.popPose();
            }
            case FERRIC_IMPACT -> {
                double flight=Math.min(1,age/3);
                Vec3 at=end.scale(flight);
                p.pushPose();p.translate(at.x,at.y,at.z);
                if(age>3)p.mulPose(Axis.ZP.rotation((float)(age*.08)));
                plate(p,iron,Vec3.ZERO,.46,.55,.055,seed,opacity);
                p.popPose();
                for(int i=0;i<4;i++) {
                    double a=i*2.399;
                    Vec3 tip=at.add(Math.cos(a)*age*.026,.04+age*.015,Math.sin(a)*age*.026);
                    ribbon(p,flow,at,tip,.022,.02,time,seed+i,camera,BLOOD,opacity);
                }
            }
            case FERRIC_CONJURE,MENDING -> {
                Vec3 at=body==null?Vec3.ZERO:hand(body,(float)(time-Math.floor(time)));
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+time*.014;
                    Vec3 root=at.add(Math.cos(a)*.36,-.45,Math.sin(a)*.36);
                    ribbon(p,flow,root,at.add(0,.28,0),.038,.08,time,seed+i,camera,BLOOD,opacity);
                }
                card(p,flow,at,right,up,.21,.32,3,seed,NERVE,opacity*.6f);
            }
            case MAGNET -> field(p,flow,Vec3.ZERO,Math.min(8,packet.radius()),time,seed,camera,opacity);
            case MARK,PARALYSIS,CIRCUIT -> {
                boolean paralyzed=packet.form()==Form.PARALYSIS;
                Vec3 head=new Vec3(0,height*.85,0),spine=new Vec3(0,height*.25,0);
                int paths=paralyzed?7:5;
                for(int i=0;i<paths;i++) {
                    double a=i*2.399+seed*.1;
                    Vec3 root=spine.add(Math.cos(a)*width*.62,0,Math.sin(a)*width*.62);
                    Vec3 tip=head.add(Math.cos(a+.4)*width*.58,0,Math.sin(a+.4)*width*.58);
                    ribbon(p,flow,root,tip,paralyzed?.042:.032,.045,time*(paralyzed?2:1),seed+i,camera,NERVE,opacity);
                    Vec3 mid=root.lerp(tip,.55);
                    ribbon(p,flow,mid,mid.add(Math.cos(a)*width*.55,.22,Math.sin(a)*width*.55),.022,.025,time,seed+i+8,camera,NERVE,opacity*.8f);
                }
                if(paralyzed)card(p,flow,head,right,up,.22,.2,3,seed,NERVE,opacity*.7f);
            }
            case WARD -> {
                int count=Math.min(8,packet.count());
                for(int i=0;i<count;i++) {
                    double a=i*Math.PI*2/8+time*.009;
                    Vec3 root=new Vec3(Math.cos(a)*.74,.35,Math.sin(a)*.74);
                    Vec3 tip=new Vec3(Math.cos(a+.2)*.8,1.65,Math.sin(a+.2)*.8);
                    ribbon(p,flow,root,tip,.045,.1,time,seed+i,camera,NERVE,opacity);
                    if(i<count-1)ribbon(p,flow,tip,new Vec3(Math.cos(a+Math.PI/4)*.8,1.5,Math.sin(a+Math.PI/4)*.8),.028,.04,time,seed+i+8,camera,NERVE,opacity*.7f);
                }
            }
            case LIGHTNING_CHARGE -> {
                Vec3 focus=ChargeVisualGeometry.focus(packet.form(),aim);
                double radius=.1+packet.radius()*.24;
                for(int i=0;i<7;i++) {
                    double a=i*2.399+time*.014;
                    Vec3 at=focus.add(Math.cos(a)*radius,Math.sin(a*1.3)*radius,Math.sin(a)*radius);
                    ribbon(p,flow,at,focus,.025,.045,time*1.7,seed+i,camera,NERVE,opacity);
                }
                card(p,flow,focus,right,up,radius*.8,radius*.8,3,seed,NERVE,opacity);
                card(p,flow,focus,right,up,radius*1.8,radius*1.5,1,seed,NERVE,opacity*.55f);
            }
            case THREAD,THREAD_CHARGE -> {
                boolean charge=packet.form()==Form.THREAD_CHARGE;
                Vec3 focus=charge?ChargeVisualGeometry.focus(packet.form(),aim):Vec3.ZERO;
                Vec3 from=charge?focus.add(-.35,0,0):Vec3.ZERO,to=charge?focus.add(.35,0,0):end;
                Vec3 mid=from.lerp(to,.5);
                double gap=charge?0:Math.min(.4,age*.028);
                for(int i=0;i<3;i++) {
                    Vec3 offset=new Vec3(0,(i-1)*.065,0);
                    ribbon(p,flow,from.add(offset),mid.lerp(from,gap).add(offset),.026,.04,time,seed+i,camera,NERVE,opacity);
                    ribbon(p,flow,mid.lerp(to,gap).add(offset),to.add(offset),.026,.04,time,seed+i+3,camera,NERVE,opacity);
                }
                if(!charge && age<7)card(p,flow,mid,right,up,.25,.25,3,seed,NERVE,opacity);
            }
            case NERVE_PULSE -> {
                double radius=Math.min(packet.radius(),age*.85);
                for(int i=0;i<24;i++) {
                    double a=i*Math.PI/12,b=(i+1)*Math.PI/12;
                    ribbon(p,flow,new Vec3(Math.cos(a)*radius,.08,Math.sin(a)*radius),new Vec3(Math.cos(b)*radius,.08,Math.sin(b)*radius),.035,.01,time,seed+i,camera,NERVE,opacity*.65f);
                }
            }
            case NERVE_HIT -> {
                Vec3 center=body==null?Vec3.ZERO:new Vec3(0,height*.6,0);
                for(int i=0;i<6;i++) {
                    double a=i*2.399;
                    Vec3 tip=new Vec3(Math.cos(a)*.4,Math.sin(a*.8)*.45,Math.sin(a)*.4);
                    ribbon(p,flow,center,center.add(tip),.035,.06,time,seed+i,camera,NERVE,opacity);
                }
                card(p,flow,center,right,up,.28,.32,3,seed,NERVE,opacity);
                card(p,flow,center,right,up,.48,.4,1,seed,NERVE,opacity*.7f);
            }
            default -> { }
        }
    }

    private static Vec3 hand(LivingEntity body,float partial) {
        var mc=net.minecraft.client.Minecraft.getInstance();
        boolean first=body==mc.player && mc.options.getCameraType().isFirstPerson();
        double yaw=Math.toRadians(first?Mth.rotLerp(partial,body.yRotO,body.getYRot()):Mth.rotLerp(partial,body.yBodyRotO,body.yBodyRot));
        double side=body.getMainArm()==net.minecraft.world.entity.HumanoidArm.RIGHT?-.32:.32;
        Vec3 right=new Vec3(Math.cos(yaw),0,Math.sin(yaw));
        Vec3 forward=first?body.getViewVector(partial):new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
        return new Vec3(0,body.getEyeHeight()-(first?.38:.64),0).add(right.scale(side)).add(forward.scale(first?.55:.25));
    }
    static void construct(FerricConstructEntity entity,PoseStack p,VertexConsumer iron,VertexConsumer flow,
            Vec3 camera,Vec3 right,Vec3 up,double time) {
        double age=time-entity.bornAt();
        float formation=Mth.clamp((float)(age/8),0,1),opacity=Mth.clamp((float)((entity.expiresAt()-time)/9),0,1);
        int seed=entity.getId();
        p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-entity.constructFacing().toYRot()));
        switch(entity.constructKind()) {
            case WALL -> {
                for(int x=0;x<5;x++)for(int row=0;row<3;row++)
                    plate(p,iron,new Vec3(x-2,.5+row,((x+row)%2==0?.045:-.045)),1,1,.285,seed+x*3+row,formation*opacity);
            }
            case SPIKE -> spike(p,iron,1.5,.36,seed,formation*opacity);
            case PILLAR -> {
                for(int row=0;row<4;row++)plate(p,iron,new Vec3(0,.35+row*.7,0),.75,.7,.75,seed+row,formation*opacity);
                for(int i=0;i<3;i++){p.pushPose();p.translate((i-1)*.24,2.35,0);spike(p,iron,.45,.12,seed+i,formation*opacity);p.popPose();}
            }
            default -> { }
        }
        p.popPose();
        if(formation<1 || opacity<1)gather(p,flow,new Vec3(0,.15,0),entity.constructKind()==com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind.WALL?2.5:.5,
                1-Math.min(formation,opacity),time,seed,camera,Math.max(.3f,opacity));
        if(entity instanceof com.vincenthuto.hemomancy.common.entity.summon.EntityIronPillar)field(p,flow,Vec3.ZERO,8,time,seed,camera,opacity);
        if(entity.energizedUntil()>time) {
            double h=entity.getBoundingBox().getYsize();
            for(int i=0;i<4;i++){double a=i*Math.PI/2;Vec3 at=new Vec3(Math.cos(a)*.39,.08,Math.sin(a)*.39);
                ribbon(p,flow,at,at.add(0,h-.12,0),.048,.055,time,seed+i,camera,NERVE,opacity);}
        }
    }

    static void field(PoseStack p,VertexConsumer flow,Vec3 anchor,double radius,double time,int seed,Vec3 camera,float alpha) {
        for(int i=0;i<10;i++) {
            double angle=i*Math.PI/5,travel=1-(time*.02+i*.137)%1;
            Vec3 outer=anchor.add(Math.cos(angle)*radius*travel,.08,Math.sin(angle)*radius*travel);
            Vec3 inner=outer.lerp(anchor.add(0,.4,0),.2);
            ribbon(p,flow,outer,inner,.023,.025,time,seed+i,camera,0xBE8C73,alpha*.6f);
        }
    }

    static void gather(PoseStack p,VertexConsumer flow,Vec3 anchor,double radius,double amount,double time,int seed,Vec3 camera,float alpha) {
        for(int i=0;i<6;i++) {
            double a=i*2.399;
            Vec3 root=anchor.add(Math.cos(a)*radius*(.5+amount),-.12,Math.sin(a)*radius*(.5+amount));
            ribbon(p,flow,root,anchor.add(Math.cos(a)*radius*.3,.45+amount*.3,Math.sin(a)*radius*.3),.045,.12,time,seed+i,camera,BLOOD,alpha*(float)amount);
        }
    }

    static void plate(PoseStack p,VertexConsumer v,Vec3 center,double width,double height,double depth,int seed,float alpha) {
        if(v==DISCARD)return;
        double x=width*.5,y=height*.5,z=depth*.5;
        Vec3 a=center.add(-x,-y,-z),b=center.add(x,-y,-z),c=center.add(x,y,-z),d=center.add(-x,y,-z);
        Vec3 e=center.add(-x,-y,z),f=center.add(x,-y,z),g=center.add(x,y,z),h=center.add(-x,y,z);
        quad(p,v,a,b,c,d,0,seed,0xFFFFFF,alpha);quad(p,v,f,e,h,g,0,seed+1,0xFFFFFF,alpha);
        quad(p,v,e,a,d,h,0,seed+2,0xFFFFFF,alpha);quad(p,v,b,f,g,c,0,seed+3,0xFFFFFF,alpha);
        quad(p,v,d,c,g,h,0,seed+4,0xFFFFFF,alpha);quad(p,v,e,f,b,a,0,seed+5,0xFFFFFF,alpha);
    }

    static void spike(PoseStack p,VertexConsumer v,double height,double radius,int seed,float alpha) {
        if(v==DISCARD)return;
        Vec3 tip=new Vec3(.06,height,.025);
        Vec3[] base={new Vec3(-radius,0,-.13),new Vec3(radius,0,-.13),new Vec3(radius,0,.13),new Vec3(-radius,0,.13)};
        for(int i=0;i<4;i++)quad(p,v,base[i],base[(i+1)%4],tip,tip,0,seed+i,0xFFFFFF,alpha);
        quad(p,v,base[3],base[2],base[1],base[0],0,seed,0xFFFFFF,alpha);
    }

    static void card(PoseStack p,VertexConsumer v,Vec3 at,Vec3 right,Vec3 up,double width,double height,int shape,int seed,int color,float alpha) {
        if(v==DISCARD)return;
        Vec3 x=right.scale(width),y=up.scale(height);
        quad(p,v,at.subtract(x).subtract(y),at.add(x).subtract(y),at.add(x).add(y),at.subtract(x).add(y),shape,seed,color,alpha);
    }

    static void ribbon(PoseStack p,VertexConsumer v,Vec3 start,Vec3 end,double width,double bend,double time,int seed,Vec3 camera,int color,float alpha) {
        if(v==DISCARD || alpha<=.001 || start.distanceToSqr(end)<1e-8 || ribbons++>=512)return;
        int shape=color==BLOOD?4:0;
        Vec3 axis=end.subtract(start).normalize(),side=axis.cross(camera.subtract(start.lerp(end,.5))).normalize();
        if(side.lengthSqr()<.1)side=axis.cross(Math.abs(axis.y)>.8?new Vec3(1,0,0):new Vec3(0,1,0)).normalize();
        Vec3 sway=axis.cross(side).normalize();
        Vec3 previousLeft=null,previousRight=null;
        int segments=12;
        for(int i=0;i<=segments;i++) {
            double t=i/(double)segments;
            Vec3 center=start.lerp(end,t).add(sway.scale(Math.sin(t*Math.PI)*Math.sin(t*7+seed+time*.025)*Math.min(bend,start.distanceTo(end)*.1)));
            double half=width*(.3+.7*Math.sin(Math.PI*t));
            Vec3 left=center.subtract(side.scale(half)),right=center.add(side.scale(half));
            if(i>0) {
                vertex(p,v,previousLeft,shape,seed,(i-1)/(float)segments,0,color,alpha);
                vertex(p,v,previousRight,shape,seed,(i-1)/(float)segments,1,color,alpha);
                vertex(p,v,right,shape,seed,(float)t,1,color,alpha);
                vertex(p,v,left,shape,seed,(float)t,0,color,alpha);
            }
            previousLeft=left;previousRight=right;
        }
    }

    static void quad(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int shape,int seed,int color,float alpha) {
        vertex(p,v,a,shape,seed,0,0,color,alpha);vertex(p,v,b,shape,seed,1,0,color,alpha);
        vertex(p,v,c,shape,seed,1,1,color,alpha);vertex(p,v,d,shape,seed,0,1,color,alpha);
    }
    private static void vertex(PoseStack p,VertexConsumer v,Vec3 at,int shape,int seed,float u,float w,int color,float alpha) {
        v.addVertex(p.last(),(float)at.x,(float)at.y,(float)at.z).setColor((color>>16)&255,(color>>8)&255,color&255,(int)(255*Mth.clamp(alpha,0,1)))
                .setUv(shape*2+u,Math.floorMod(seed,128)*2+w);
    }
}
