package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

/** Bounded textured anatomy. Adds no particle emitters, entities, hitboxes, or duplicate spell fields. */
public final class CastingAnatomyRenderer {
    private CastingAnatomyRenderer() {}
    private static int blood(EnumBloodTendency school) {
        return switch(school) { case MORTEM -> 0x591527; case TENEBRIS -> 0x390D24;
            case CONGEATIO -> 0x831C40; case FERRIC -> 0x701D28; default -> 0xB51B39; };
    }
    private static int accent(EnumBloodTendency school) {
        return switch(school) { case ANIMUS -> 0xEC3550; case FLAMMEUS -> 0xEF4A77; case DUCTILIS -> 0xF0C75B;
            case LUX -> 0xFFEAC7; case MORTEM -> 0x44643B; case CONGEATIO -> 0x8EBEDC;
            case FERRIC -> 0x42404A; case TENEBRIS -> 0x653774; };
    }
    private static VertexConsumer vertices(MultiBufferSource buffer,EnumBloodTendency school) {
        return buffer.getBuffer(ManipulationMaterials.valueOf(school.name()).renderType());
    }
    public static void hand(PoseStack p,MultiBufferSource buffer,CastingAnimationPacket packet,float time,float alpha,boolean first) {
        if(alpha<=0)return;
        var v=vertices(buffer,packet.school());
        int red=blood(packet.school()),edge=accent(packet.secondary()==null?packet.school():packet.secondary());
        double pulse=.9+.1*Math.sin(time*.6);
        double bead=first?.035:.045;
        // Veins wrap the active hand in its real model transform; there is no reconstructed world-space emitter.
        for(int i=0;i<(packet.rank().ordinal()==0?1:2);i++) {
            double x=(i==0?-1:1)*.065;
            VisceralMesh.strand(p,v,new Vec3(x,-.24,0),new Vec3(x*.5,.04,-.035),.012,.025,time*.06+i,red,alpha*.8F);
        }
        VisceralMesh.drop(p,v,new Vec3(0,-.015,-.08),bead*pulse,bead*1.5,red,alpha,time*.04);
        VisceralMesh.drop(p,v,new Vec3(.008,-.01,-.083),bead*.3,bead*.7,edge,alpha*.6F,0);
    }
    public static void body(PoseStack p,MultiBufferSource buffer,CastingAnimationPacket packet,float time,float alpha,double distance) {
        int rank=packet.rank().ordinal();
        if(rank==0 || alpha<=0)return;
        int setting=Minecraft.getInstance().options.particles().get().getId();
        // The pose and hand cue survive Minimal; large anatomy is decorative and obeys particle settings.
        if(setting==2 || distance>32*32)return;
        var v=vertices(buffer,packet.school());
        int red=blood(packet.school());
        if(rank==1 || setting==1 || distance>24*24) {
            for(int side:new int[]{-1,1}) VisceralMesh.strand(p,v,new Vec3(side*.24,1.42,.17),
                    new Vec3(0,1.02,.2),.018,.08,time*.035,red,alpha*.65F);
            return;
        }
        p.pushPose();
        if(packet.school()!=EnumBloodTendency.LUX) {
            // Vascular anatomy grows behind the caster; Lux wings keep their roots at the shoulders.
            double scale=rank==2?.52:rank==3?.82:1.22;
            p.translate(0,rank==2?.95:.25,rank==2?.28:.65);
            p.scale((float)scale,(float)scale,(float)scale);
        }
        switch(packet.school()) {
            case ANIMUS -> heart(p,v,time,alpha,red,rank);
            case FLAMMEUS -> furnace(p,v,time,alpha,red,rank);
            case DUCTILIS -> nerves(p,v,time,alpha,red,rank);
            case LUX -> LuxCastingWings.draw(p,buffer.getBuffer(LuxCastingWings.renderType()),packet.rank(),time,alpha);
            case MORTEM -> cadaver(p,v,time,alpha,red,rank);
            case CONGEATIO -> crystal(p,v,time,alpha,red,rank);
            case FERRIC -> foundry(p,v,time,alpha,red,rank);
            case TENEBRIS -> shadow(p,v,time,alpha,red,rank);
        }
        p.popPose();
    }
    private static void heart(PoseStack p,VertexConsumer v,double t,float a,int red,int rank) {
        double beat=1-.09*Math.pow(Math.max(0,Math.sin(t*.55)),4);
        Vec3 left=new Vec3(-.28,1.68,.05),right=new Vec3(.25,1.76,0),tip=new Vec3(.08,.95,0);
        VisceralMesh.drop(p,v,left,.43*beat,.48*beat,red,a*.8F,-.3);
        VisceralMesh.drop(p,v,right,.35*beat,.5*beat,red,a*.8F,.35);
        VisceralMesh.segment(p,v,new Vec3(0,1.58,0),tip,.36*beat,.045,red,a*.8F,0,1);
        for(int i=0;i<rank;i++) {
            double x=(i-(rank-1)/2.0)*.36;
            var root=new Vec3(x*.45,1.8,.08); var end=new Vec3(x,2.6+Math.abs(x)*.3,0);
            vessel(p,v,root,end,.052,red,a,t,i);
            vessel(p,v,end,end.add(x*.5,.5,.12),.025,red,a,t,i+2);
            vessel(p,v,end,end.add(-.18,.35,-.1),.018,red,a,t,i+4);
        }
    }
    private static void furnace(PoseStack p,VertexConsumer v,double t,float a,int red,int rank) {
        for(int side:new int[]{-1,1})for(int i=0;i<rank;i++) {
            Vec3 root=new Vec3(side*.2,.6+i*.2,0),top=new Vec3(side*(.55+i*.12),2.25+i*.16,.12);
            vessel(p,v,root,top,.065,red,a,t,i);
            // Combustion rides a blood conduit; the outer black tip remains bounded.
            double flicker=.12+.1*Math.sin(t*.9+i);
            VisceralMesh.strand(p,v,top.add(0,-.25,0),top.add(side*.1,.38+flicker,0),.04,.1,t*.1+i,0xF04C7B,a*.75F);
            VisceralMesh.tube(p,v,top.add(side*.1,.38+flicker,0),top.add(side*.15,.54+flicker,0),.035,0x260818,a*.8F);
        }
    }
    private static void nerves(PoseStack p,VertexConsumer v,double t,float a,int red,int rank) {
        Vec3 spine=new Vec3(0,1.8,0);
        vessel(p,v,new Vec3(0,.3,0),new Vec3(0,2.75,0),.045,red,a,t,0);
        for(int side:new int[]{-1,1})for(int i=0;i<rank;i++) {
            Vec3 origin=spine.add(0,-i*.36,0),node=origin.add(side*(.55+i*.16),.24,0);
            vessel(p,v,origin,node,.024,red,a,t,i);
            vessel(p,v,node,node.add(side*.28,.3,0),.012,red,a,t,i+1);
            double progress=((int)(t/3)+i)%6/5.0;
            VisceralMesh.drop(p,v,origin.lerp(node,progress),.035,.05,0xF0C75B,a,0);
        }
    }
    private static void cadaver(PoseStack p,VertexConsumer v,double t,float a,int red,int rank) {
        Vec3 neck=new Vec3(0,2.25,-.08),head=neck.add(0,.18,-.12);
        VisceralMesh.drop(p,v,head,.22,.3,red,a*.6F,.2);
        vessel(p,v,new Vec3(0,.55,.2),neck,.075,red,a,t,0);
        for(int side:new int[]{-1,1}) {
            Vec3 shoulder=neck.add(side*.52,-.2,.05),hand=shoulder.add(side*.28,-1.3,-.28);
            vessel(p,v,neck,shoulder,.085,red,a,t,side);
            vessel(p,v,shoulder,hand,.075,red,a,t,side);
            for(int i=0;i<rank;i++) {
                Vec3 rib=new Vec3(side*(.44-i*.045),1.85-i*.25,.17);
                vessel(p,v,rib,new Vec3(0,1.7-i*.25,0),.03,red,a,t,i);
            }
            Vec3 clot=hand.lerp(new Vec3(0,1.1,0),(t*.055)%1);
            VisceralMesh.drop(p,v,clot,.05,.075,0x496038,a*.7F,0);
        }
    }
    private static void crystal(PoseStack p,VertexConsumer v,double t,float a,int red,int rank) {
        for(int i=0;i<rank+2;i++) {
            double angle=i*Math.PI*2/(rank+2);
            Vec3 foot=new Vec3(Math.cos(angle)*.7,.15,Math.sin(angle)*.35);
            Vec3 point=foot.add(-foot.x*.3,2.4+(i%2)*.3,-foot.z*.2);
            Vec3 edge=foot.add(.22,1,.1);
            VisceralMesh.quad(p,v,foot,edge,point,foot.add(-.18,.9,-.1),red,a*.3F,0,0,1,1);
            VisceralMesh.tube(p,v,foot,point,.013,0x8EBEDC,a*.6F);
            vessel(p,v,foot.add(0,.3,0),point.add(0,-.3,0),.04,red,a,t,0);
            VisceralMesh.drop(p,v,foot.add(.1,1.1,-.02),.04,.075,red,a,.1);
        }
    }
    private static void foundry(PoseStack p,VertexConsumer v,double t,float a,int red,int rank) {
        for(int side:new int[]{-1,1}) {
            vessel(p,v,new Vec3(side*.8,.1,0),new Vec3(side*.8,2.7,0),.09,red,a,t,side);
            for(int i=0;i<rank;i++) {
                Vec3 at=new Vec3(side*.8,.45+i*.55,0);
                VisceralMesh.drop(p,v,at,.17,.19,0x42404A,a,0);
                VisceralMesh.tube(p,v,at.add(-.12,0,-.12),at.add(.12,0,-.12),.022,red,a);
                vessel(p,v,at,new Vec3(side*.22,at.y,.12),.04,red,a,t,i);
            }
        }
    }
    private static void shadow(PoseStack p,VertexConsumer v,double t,float a,int red,int rank) {
        // A detached vascular figure moves independently, never hides or duplicates the actual player's skin.
        double sway=Math.sin((t-6)*.13)*.35;
        Vec3 hip=new Vec3(-sway*.3,.75,.18),neck=new Vec3(sway,2.25,0);
        vessel(p,v,hip,neck,.18,red,a*.55F,t,0);
        VisceralMesh.drop(p,v,neck.add(.1,.23,0),.24,.31,red,a*.65F,sway);
        for(int side:new int[]{-1,1}) {
            Vec3 shoulder=neck.add(side*.46,-.2,0);
            Vec3 hand=shoulder.add(side*.5,Math.sin(t*.14+side)*.5-.45,-.25);
            vessel(p,v,neck,shoulder,.12,red,a*.7F,t,side);
            vessel(p,v,shoulder,hand,.1,red,a*.7F,t,side);
            vessel(p,v,hip,new Vec3(side*.36,.05,0),.11,red,a*.5F,t,side);
            for(int i=0;i<rank-1;i++)vessel(p,v,hand,hand.add(side*.12,-.3-i*.16,.12*i),.022,red,a*.5F,t,i);
        }
        VisceralMesh.drop(p,v,neck.add(.02,.25,-.2),.022,.028,0x653774,a,0);
        VisceralMesh.drop(p,v,neck.add(.17,.25,-.2),.022,.028,0x653774,a,0);
    }
    private static void vessel(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,double width,int color,float alpha,double t,int seed) {
        VisceralMesh.strand(p,v,a,b,width,width*1.8,seed,color,alpha*.8F);
        // A fine wet seam makes dark or metallized vessels remain visibly blood.
        VisceralMesh.strand(p,v,a.add(0,0,-width*.8),b.add(0,0,-width*.8),width*.15,width,seed,0xD33A50,alpha*.5F);
    }
}
