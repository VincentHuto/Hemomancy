package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Faceted ice and open shadow seams; neither replaces the server's actual status or hit test. */
final class VelorumRimeGeometry {
    private static final int TINT=0xFFFFFF, CORE=0xFFD2C4;
    record Frame(VelorumVisualRules.Phase phase,float age,int seed,Vec3 core,Vec3 claw,Vec3[] lanterns,Vec3 floor) {}
    private VelorumRimeGeometry() {}
    static boolean handles(Form form){return form==Form.VELORUM_RIME;}
    static void collect(ClientLevel level,PoseStack p,LuxUmbraBatch batch,Vec3 camera,Vec3 right,Vec3 up,float partial) {
        for(VelorumEntity boss:level.getEntitiesOfClass(VelorumEntity.class,new AABB(camera,camera).inflate(96))) {
            if(!boss.isAlive() || boss.isInvisible())continue;
            float rise=boss.riseTime(partial);if(rise>=0 && rise<SaintRise.TICKS)continue;
            var motion=VelorumAnimState.of(boss);motion.update(boss,partial);
            Vec3 at=boss.getPosition(partial);
            p.pushPose();p.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
            body(p,batch.vertices(LuxUmbraBatch.Material.RIME),batch.vertices(LuxUmbraBatch.Material.UMBRA),camera.subtract(at),right,up,
                    new Frame(motion.phase(),AnimationTime.age(boss,partial),boss.getId(),motion.core(),motion.claw(),motion.lanterns(),Vec3.atBottomCenterOf(boss.blockPosition()).subtract(at)));
            p.popPose();
        }
    }
    static void body(PoseStack p,VertexConsumer ice,VertexConsumer shade,Vec3 camera,Vec3 right,Vec3 up,Frame f) {
        var action=VelorumVisualRules.pose(f.phase());
        float pressure=Math.max(action.gather(),Math.max(action.frost(),action.silence()));
        for(int i=0;i<f.lanterns().length;i++) {
            Vec3 lantern=f.lanterns()[i];
            card(p,ice,lantern,right,up,.065+pressure*.025,.1+pressure*.04,WISP,f.seed()+i,TINT,.7F);
            for(int n=0;n<2;n++) {
                double a=f.age()*.017+i*2+n*Math.PI;
                Vec3 frost=lantern.add(Math.cos(a)*.14,-.1-n*.16,Math.sin(a)*.14);
                spire(p,ice,frost,.018,.1,new Vec3(.015,0,0),f.seed()+n,.6F);
            }
        }
        if(pressure>.01) {
            ribbonPath(p,shade,List.of(f.core(),f.core().lerp(f.claw(),.5).add(0,.1,0),f.claw()),camera,.12,.03,RIBBON,f.seed(),TINT,pressure*.45F);
            ribbonPath(p,ice,List.of(f.core(),f.claw()),camera,.025,.008,RIBBON,f.seed(),TINT,pressure*.8F);
        }
        if(f.phase().frostUntil()>0 && f.phase().frostUntil()<=VelorumVisualRules.FROST_WARNING) {
            square(p,ice,f.floor(),10.5,camera,.045,f.seed(),.7F);
            for(int i=0;i<12;i++) {
                Vec3 root=perimeter(f.floor(),10.5,i/12.0);
                Vec3 inner=root.lerp(f.floor(),.65);
                ribbonPath(p,ice,List.of(root,inner.add(0,.04,0),f.floor()),camera,.035,.006,RIBBON,f.seed()+i,TINT,action.gather()*.8F);
                spire(p,ice,root,.055,.15+action.gather()*.35,new Vec3(0,0,.08),f.seed()+i,.7F);
            }
        }
        if(f.phase().veilUntil()>0 && f.phase().veilUntil()<=VelorumVisualRules.VEIL_WARNING)
            square(p,shade,f.floor(),12.5,camera,.1,f.seed(),.6F);
        if(action.martyr()>.01) {
            for(int i=0;i<7;i++) {
                double a=i*2.39996;
                Vec3 at=f.core().add(Math.cos(a)*.3,(i-3)*.13,Math.sin(a)*.3);
                spire(p,ice,at,.025,.12*action.martyr(),new Vec3(Math.cos(a)*.07,0,Math.sin(a)*.07),f.seed()+i,action.martyr()*.8F);
            }
        }
    }
    static Vec3 perimeter(Vec3 center,double extent,double t) {
        double along=(t-Math.floor(t))*4,v=(along-Math.floor(along))*2-1;
        return center.add(switch((int)along){case 0->new Vec3(v*extent,.07,-extent);case 1->new Vec3(extent,.07,v*extent);
            case 2->new Vec3(-v*extent,.07,extent);default->new Vec3(-extent,.07,-v*extent);});
    }
    private static void square(PoseStack p,VertexConsumer v,Vec3 center,double extent,Vec3 camera,double width,int seed,float alpha) {
        for(int i=0;i<32;i++)ribbonPath(p,v,List.of(perimeter(center,extent,i/32.0),perimeter(center,extent,(i+.72)/32.0)),camera,width,RIBBON,seed+i,TINT,alpha);
    }
    /** Four real tapered faces, with a leaning tip and a dark translucent frost material. */
    static void spire(PoseStack p,VertexConsumer v,Vec3 at,double width,double height,Vec3 lean,int seed,float alpha) {
        if(height<=0 || width<=0 || alpha<=0)return;
        Vec3 tip=at.add(lean).add(0,height,0);
        for(int i=0;i<4;i++) {
            double a=i*Math.PI/2,b=a+Math.PI/2;
            Vec3 x=at.add(Math.cos(a)*width,0,Math.sin(a)*width),y=at.add(Math.cos(b)*width,0,Math.sin(b)*width);
            MaterialQuads.quad(v,p.last().pose(),x.x,x.y,x.z,y.x,y.y,y.z,tip.x,tip.y,tip.z,tip.x,tip.y,tip.z,
                    RIBBON,MaterialQuads.seedTile(seed+i),TINT,alpha,0,1,false);
        }
    }
    static void draw(ManipulationVisualPacket packet,PoseStack p,VertexConsumer ice,VertexConsumer shade,Vec3 camera,Vec3 right,Vec3 up,
                     double time,float age,float alpha) {
        float fade=alpha*VelorumVisualRules.fade(age,packet.ticks());if(fade<=0)return;
        int kind=packet.count(),seed=Math.floorMod(packet.from().hashCode(),113);
        double radius=packet.radius();
        if(kind==VelorumVisualRules.FROST) {
            square(p,ice,Vec3.ZERO,radius+.5,camera,.08*fade,seed,fade);
            for(int i=0;i<16;i++) {
                Vec3 rim=perimeter(Vec3.ZERO,radius+.5,i/16.0);
                double travel=HemoEasing.easeOutCubic(age/7.0);
                Vec3 foot=rim.scale(.2+travel*.8);
                ribbonPath(p,ice,List.of(Vec3.ZERO.add(0,.08,0),foot),camera,.05,.012,RIBBON,seed+i,TINT,fade);
                spire(p,ice,foot,.13*fade,(.55+(i%3)*.16)*fade,new Vec3(foot.x*.025,0,foot.z*.025),seed+i,fade);
            }
            return;
        }
        if(kind==VelorumVisualRules.VEIL) {
            square(p,shade,Vec3.ZERO,radius+.5,camera,.16,seed,fade*.6F);
            for(int i=0;i<10;i++) {
                double a=i*Math.PI/5+seed,spread=HemoEasing.easeOutCubic(age/12.0);
                Vec3 root=new Vec3(Math.cos(a)*radius*spread,.1,Math.sin(a)*radius*spread);
                Vec3 top=root.scale(.9).add(0,1.7*fade,0);
                ribbonPath(p,shade,List.of(root,root.lerp(top,.5).add(.2*Math.sin(a),0,.2*Math.cos(a)),top),camera,
                        .24*fade,.02,WISP,seed+i,TINT,fade*.45F);
                ribbonPath(p,ice,List.of(root,top),camera,.012,.003,RIBBON,seed+i,TINT,fade*.35F);
            }
            return;
        }
        if(kind==VelorumVisualRules.DRAIN) {
            Vec3 end=packet.to().subtract(packet.from());
            double t=HemoEasing.smoothstep(age/16.0);
            Vec3 head=end.scale(t),tail=end.scale(Math.max(0,t-.35));
            ribbonPath(p,shade,List.of(Vec3.ZERO,end.scale(.5).add(0,-.25,0),end),camera,.16,.055,BEAM,seed,TINT,fade*.4F);
            ribbonPath(p,ice,List.of(tail,head),camera,.04,.012,RIBBON,seed,TINT,fade);
            card(p,ice,head,right,up,.07,.1,WISP,seed,CORE,fade);
            return;
        }
        if(kind==VelorumVisualRules.MELEE) {
            for(double offset:slashOffsets())ribbonPath(p,ice,List.of(new Vec3(offset-.2,.4,0),new Vec3(offset,0,0),new Vec3(offset+.2,-.35,0)),camera,.055,.01,RIBBON,seed,TINT,fade);
            return;
        }
        boolean rest=kind==VelorumVisualRules.REST;
        for(int i=0;i<10;i++) {
            double a=i*2.39996+seed,travel=HemoEasing.easeOutCubic(age/(rest?22.0:8.0));
            Vec3 at=new Vec3(Math.cos(a)*radius*travel,(rest?-1.4:.3)*travel,Math.sin(a)*radius*travel);
            spire(p,ice,at,.055*fade,.25*fade,new Vec3(Math.cos(a)*.1,0,Math.sin(a)*.1),seed+i,fade);
            ribbonPath(p,shade,List.of(Vec3.ZERO,at.scale(.5).add(0,.15,0),at),camera,.12,.015,WISP,seed+i,TINT,fade*.4F);
        }
    }
}
