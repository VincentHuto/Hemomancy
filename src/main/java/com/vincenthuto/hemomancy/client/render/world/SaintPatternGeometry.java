package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintAttackPattern;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Committed attack footprints: warnings use the server's actual lanes, circles and sweep angles. */
final class SaintPatternGeometry {
    private static final int WHITE=0xFFFFFF, CORE=0xFFD2C4;
    private SaintPatternGeometry() {}
    static boolean handles(ManipulationVisualPacket packet) {
        var move=SaintAttackPattern.fromCue(packet.count());
        if(move==null)return false;
        return switch(packet.form()) {
            case HEMORATH_RELIQUARY -> move.id()<=2;
            case SERAPHAE_RELIQUARY -> move.id()>=3 && move.id()<=4;
            case PUTRICIEL_OFFERING -> move.id()>=5 && move.id()<=6;
            case VELORUM_RIME -> move.id()>=7;
            default -> false;
        };
    }
    static LuxUmbraBatch.Material material(ManipulationVisualPacket packet) {
        return switch(packet.form()) {
            case HEMORATH_RELIQUARY -> LuxUmbraBatch.Material.ANIMUS;
            case SERAPHAE_RELIQUARY -> LuxUmbraBatch.Material.LUX;
            case PUTRICIEL_OFFERING -> LuxUmbraBatch.Material.PUTRICIEL_ROT;
            default -> LuxUmbraBatch.Material.RIME;
        };
    }
    static LuxUmbraBatch.Material accent(ManipulationVisualPacket packet) {
        return switch(packet.form()) {
            case SERAPHAE_RELIQUARY -> LuxUmbraBatch.Material.DUCTILIS;
            case VELORUM_RIME -> LuxUmbraBatch.Material.UMBRA;
            default -> material(packet);
        };
    }
    static void draw(ManipulationVisualPacket packet,PoseStack p,VertexConsumer iron,VertexConsumer v,VertexConsumer accent,
                     boolean solids,Vec3 camera,Vec3 right,Vec3 up,float age) {
        var move=SaintAttackPattern.fromCue(packet.count());
        if(move==null || age<0 || age>=move.duration())return;
        boolean warning=age<move.windup(),active=age>=move.windup() && age<move.windup()+move.active();
        float fade=1-HemoEasing.smoothstep((age-move.windup()-move.active())/(move.duration()-move.windup()-move.active()));
        float grow=HemoEasing.smoothstep(age/8);
        float alpha=fade*(warning?.3F+.4F*HemoEasing.smoothstep(age/move.windup()):.9F);
        Vec3 target=packet.to().subtract(packet.from());
        Vec3 flat=new Vec3(target.x,0,target.z);
        Vec3 forward=flat.lengthSqr()>.001?flat.normalize():new Vec3(0,0,1);
        Vec3 side=new Vec3(forward.z,0,-forward.x);
        int seed=packet.entityId();
        if(solids) {
            if(move==SaintAttackPattern.IRON_RAKE && !warning) {
                for(int lane=-1;lane<=1;lane++)for(int n=1;n<=7;n++) {
                    Vec3 foot=forward.scale(n*2).add(side.scale(lane*2)).add(0,.04,0);
                    spike(p,iron,foot,.12,.7*fade,side,forward,seed+n);
                }
            }
            return;
        }
        switch(move) {
            case IRON_RAKE, FROZEN_ADVANCE -> {
                double width=move==SaintAttackPattern.IRON_RAKE?.5:.45;
                for(int lane=-1;lane<=1;lane++) {
                    Vec3 root=side.scale(lane*2).add(0,.045,0),end=root.add(forward.scale(move.radius()));
                    strip(p,v,root,end,side,width,seed+lane,alpha*(warning?1:.3F));
                    if(!warning) {
                        double front=move.radius()*move.progress(age);
                        Vec3 head=root.add(forward.scale(front));
                        if(move==SaintAttackPattern.FROZEN_ADVANCE)
                            for(int n=-1;n<=1;n++)spike(p,v,head.add(side.scale(n*.25)),.13,.75*fade,side,forward,seed+n);
                        else ribbonPath(p,v,List.of(root,root.add(forward.scale(6)).add(0,.3,0),end),camera,.06,.012,RIBBON,seed+lane,WHITE,alpha);
                    }
                }
            }
            case MARROW_RUPTURE -> {
                if(warning)circle(p,v,Vec3.ZERO,move.radius(),camera,seed,alpha);
                else {
                    double radius=Math.max(.05,move.radius()*move.progress(age));
                    circle(p,v,Vec3.ZERO,radius,camera,seed,alpha);
                    for(int i=0;i<12;i++) {
                        double a=i*Math.PI/6;Vec3 at=new Vec3(Math.cos(a)*radius,.06,Math.sin(a)*radius);
                        ribbonPath(p,v,List.of(at,at.add(0,.65*fade,0),at.add(forward.scale(.12))),camera,.05,.008,SLASH,seed+i,CORE,alpha);
                    }
                }
            }
            case NERVE_LANCE -> {
                Vec3 root=new Vec3(0,.05,0),end=forward.scale(move.radius()).add(0,.05,0);
                strip(p,v,root,end,side,.65,seed,alpha*(warning?1:.3F));
                if(!warning) {
                    Vec3 high=new Vec3(0,1.4,0),tip=end.add(0,1.35,0);
                    ribbonPath(p,v,List.of(high,high.lerp(tip,.5).add(0,.08,0),tip),camera,.055,.008,RIBBON,seed,WHITE,alpha);
                    for(int i=1;i<8;i++) {
                        Vec3 knot=high.lerp(tip,i/8.0);
                        ribbonPath(p,accent,List.of(knot,knot.add(side.scale(.35)).add(0,.15,0),knot.add(side.scale(.5))),camera,.015,.003,RIBBON,seed+i,WHITE,alpha);
                    }
                }
            }
            case REGROWTH_KNOT, SPORE_GARDEN -> {
                int count=move==SaintAttackPattern.SPORE_GARDEN?3:1;
                for(int i=0;i<count;i++) {
                    Vec3 center=target.add(i==1?3:i==2?-3:0,.04,i==0?0:2);
                    circle(p,v,center,move.radius(),camera,seed+i,alpha);
                    if(!warning)for(int j=0;j<6;j++) {
                        double a=j*Math.PI/3;Vec3 root=center.add(Math.cos(a)*move.radius()*.65,0,Math.sin(a)*move.radius()*.65);
                        Vec3 head=root.add(Math.sin(age*.1+j)*.08,.4*grow*fade,0);
                        ribbonPath(p,accent,List.of(root,root.lerp(head,.5).add(side.scale(.1)),head),camera,.035,.006,RIBBON,seed+j,WHITE,alpha);
                        card(p,v,head,right,up,.24*fade,.32*fade,move==SaintAttackPattern.SPORE_GARDEN?SLASH:FOCUS,seed+j,WHITE,alpha*.7F);
                    }
                }
            }
            case CENSER_SWEEP -> {
                if(warning)for(int i=0;i<=8;i++) {
                    double a=-Math.PI/3+i*Math.PI/12;Vec3 end=forward.scale(Math.cos(a)*8).add(side.scale(Math.sin(a)*8)).add(0,.05,0);
                    ribbonPath(p,v,List.of(new Vec3(0,.05,0),end),camera,.025,RIBBON,seed+i,WHITE,alpha);
                }
                else {
                    double a=move.sweepAngle(age);
                    Vec3 end=forward.scale(Math.cos(a)*8).add(side.scale(Math.sin(a)*8));
                    double lowAngle=Math.max(-Math.PI/3,a-.18),highAngle=Math.min(Math.PI/3,a+.18);
                    Vec3 low=forward.scale(Math.cos(lowAngle)).add(side.scale(Math.sin(lowAngle)));
                    Vec3 high=forward.scale(Math.cos(highAngle)).add(side.scale(Math.sin(highAngle)));
                    surface(p,v,low.add(0,.05,0),low.scale(8).add(0,.05,0),high.scale(8).add(0,.05,0),high.add(0,.05,0),RIBBON,seed,WHITE,alpha*.55F);
                    ribbonPath(p,v,List.of(new Vec3(0,1.4,0),end.scale(.5).add(0,.75,0),end.add(0,.15,0)),camera,.12,.025,RIBBON,seed,WHITE,alpha);
                }
            }
            case SHADOW_CROSS -> {
                Vec3 center=target.add(0,.05,0);
                for(Vec3 axis:new Vec3[]{new Vec3(1,0,0),new Vec3(0,0,1)}) {
                    Vec3 normal=new Vec3(axis.z,0,-axis.x);
                    Vec3 start=center.subtract(axis.scale(5)),end=center.add(axis.scale(5));
                    strip(p,accent,start,end,normal,.65,seed,alpha);
                    if(!warning)ribbonPath(p,v,List.of(start,start.lerp(end,.5).add(0,.8*fade,0),end),camera,.035,.008,SLASH,seed,WHITE,alpha);
                }
            }
        }
    }
    private static void strip(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 normal,double half,int seed,float alpha) {
        Vec3 w=normal.scale(half);
        surface(p,v,a.subtract(w),b.subtract(w),b.add(w),a.add(w),RIBBON,seed,WHITE,alpha*.45F);
        for(int sign:new int[]{-1,1}) {
            Vec3 edge=normal.scale(sign*half),thin=normal.scale(.02);
            surface(p,v,a.add(edge).subtract(thin),b.add(edge).subtract(thin),b.add(edge).add(thin),a.add(edge).add(thin),RIBBON,seed,WHITE,alpha);
        }
    }
    private static void circle(PoseStack p,VertexConsumer v,Vec3 center,double r,Vec3 camera,int seed,float alpha) {
        for(int i=0;i<24;i++) {
            double a=i*Math.PI/12,b=(i+1)*Math.PI/12;
            ribbonPath(p,v,List.of(center.add(Math.cos(a)*r,.04,Math.sin(a)*r),center.add(Math.cos(b)*r,.04,Math.sin(b)*r)),
                    camera,.035,RIBBON,seed+i,WHITE,alpha);
        }
    }
    private static void spike(PoseStack p,VertexConsumer v,Vec3 at,double width,double height,Vec3 side,Vec3 forward,int seed) {
        Vec3 top=at.add(0,height,0),a=at.add(side.scale(width)),b=at.add(forward.scale(width)),c=at.subtract(side.scale(width)),d=at.subtract(forward.scale(width));
        surface(p,v,a,b,top,top,SLASH,seed,WHITE,1);
        surface(p,v,b,c,top,top,SLASH,seed,WHITE,1);
        surface(p,v,c,d,top,top,SLASH,seed,WHITE,1);
        surface(p,v,d,a,top,top,SLASH,seed,WHITE,1);
    }
}
