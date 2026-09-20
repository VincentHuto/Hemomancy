package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.antecedent.VigilArchiveBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;

public final class AntecedentVesselRenderer implements BlockEntityRenderer<VigilArchiveBlockEntity> {
    private static final ResourceLocation TEXTURE=Hemomancy.rloc("textures/block/antecedent_colloid.png");
    private static final ModelPart GRAIN=grain();
    private static ModelPart grain() {
        var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("grain",CubeListBuilder.create().texOffs(0,0).addBox(-1,-1,-1,2,2,2),PartPose.ZERO);
        return LayerDefinition.create(mesh,16,16).bakeRoot();
    }
    public AntecedentVesselRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(VigilArchiveBlockEntity vessel,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        double time=vessel.getLevel().getGameTime()+partial;
        float progress=vessel.response(partial);
        // A carried record produces a single faint inclusion; ordinary noise does nothing.
        var player=net.minecraft.client.Minecraft.getInstance().player;
        boolean proximity=false;
        if(player!=null && player.distanceToSqr(vessel.getBlockPos().getCenter())<64) {
            for(var stack:player.getInventory().items) if(com.vincenthuto.hemomancy.common.antecedent.AncientRecordings.get(stack)==com.vincenthuto.hemomancy.common.antecedent.AncientRecordings.SEVERED) proximity=true;
        }
        draw(pose,buffers,light,overlay,time,progress,true,proximity);
    }
    public static void draw(PoseStack pose,MultiBufferSource buffers,int light,int overlay,double time,float response,boolean ancient,boolean proximity) {
        var surface=buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        pose.pushPose();pose.translate(.5,.15,.5);pose.scale(3.4F,.4F,3.4F);
        GRAIN.render(pose,surface,light,overlay,0xFF101820);pose.popPose();
        if(ancient && response>.3F) for(int level=0;level<4;level++) for(int edge=0;edge<6;edge++) {
            double angle=(edge+.5)*Math.PI/3;
            double radius=.14*response;
            pose.pushPose();pose.translate(.5+Math.cos(angle)*radius,.17+level*.065*response,.5+Math.sin(angle)*radius);
            pose.mulPose(com.mojang.math.Axis.YP.rotation((float)-angle));pose.scale(.06F,.045F,(float)(radius*8));
            GRAIN.render(pose,surface,LightTexture.FULL_BRIGHT,overlay,0xFF315C69);pose.popPose();
        }
        for(int i=0;i<36;i++) {
            double angle=i*2.39996;
            double radius=.07+.2*((i*13%31)/31.0);
            double x=.5+Math.cos(angle)*radius,z=.5+Math.sin(angle)*radius;
            double y=.16+(i%3)*.018;
            if(ancient) {
                double targetX=.5+Math.cos(i*Math.PI/3)*(.1+(i/6%2)*.06);
                double targetZ=.5+Math.sin(i*Math.PI/3)*(.1+(i/6%2)*.06);
                x+=(targetX-x)*response;z+=(targetZ-z)*response;
                y+=response*(i%6)*.065;
            } else {
                x+=response*.13; y+=response*(.12+.12*Math.sin(time*.16+i));
            }
            boolean glow=i%5==0 && (response>0 || !ancient || proximity && i==0);
            pose.pushPose();pose.translate(x,y,z);pose.scale(.4F,.25F,.4F);
            GRAIN.render(pose,surface,glow?LightTexture.FULL_BRIGHT:light,overlay,glow?0xFF51ACA4:0xFF111B28);
            pose.popPose();
        }
    }
}
