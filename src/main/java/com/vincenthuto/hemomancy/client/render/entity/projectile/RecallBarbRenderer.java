package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.ExcoriatedRenderer;
import com.vincenthuto.hemomancy.common.entity.projectile.RecallBarbEntity;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class RecallBarbRenderer extends EntityRenderer<RecallBarbEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(Hemomancy.rloc("recall_barb"),"main");
    private final ModelPart barb;
    public RecallBarbRenderer(EntityRendererProvider.Context context) {super(context);barb=context.bakeLayer(LAYER);}
    public static LayerDefinition layer() {
        var mesh=new MeshDefinition();var cubes=CubeListBuilder.create().texOffs(128,0).addBox(-8,-.4F,-.4F,16,.8F,.8F)
                .texOffs(0,128).addBox(-5,-.65F,-.65F,7,1.3F,1.3F);
        for(int i=0;i<3;i++)cubes.texOffs(128,0).addBox(2+i*1.5F,-1.8F,-.35F,.7F,3.6F,.7F);
        mesh.getRoot().addOrReplaceChild("barb",cubes,PartPose.ZERO);
        return LayerDefinition.create(mesh,256,256);
    }
    @Override public void render(RecallBarbEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial,entity.yRotO,entity.getYRot())-90));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial,entity.xRotO,entity.getXRot())));
        barb.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))),light,OverlayTexture.NO_OVERLAY);
        pose.popPose();
        if(entity.victimId()>=0 && entity.level().getEntity(entity.archerId()) instanceof com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedEntity owner
                && net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(owner) instanceof ExcoriatedRenderer renderer) {
            Vec3 from=renderer.bowAnchor(owner,partial).subtract(entity.getPosition(partial));
            VertexConsumer vertices=buffers.getBuffer(RenderType.leash());
            for(int plane=0;plane<2;plane++) for(int i=0;i<=24;i++) {
                float t=i/24F;
                double sag=Math.sin(t*Math.PI)*Math.min(.4,from.length()*.025);
                float x=(float)(from.x*(1-t)),y=(float)(from.y*(1-t)-sag),z=(float)(from.z*(1-t));
                float width=.014F+(float)Math.sin(entity.tickCount*.25F+partial+i*.6F)*.002F;
                for(int side=-1;side<=1;side+=2)vertices.addVertex(pose.last().pose(),x+(plane==0?side*width:0),
                        y+(plane==1?side*width:0),z).setColor(135,13,21,255).setLight(light);
            }
        }
        super.render(entity,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(RecallBarbEntity entity) {return ExcoriatedRenderer.TEXTURE;}
}
