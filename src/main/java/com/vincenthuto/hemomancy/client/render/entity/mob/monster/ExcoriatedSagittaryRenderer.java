package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.ExcoriatedSagittaryModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedSagittaryEntity;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticRules;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class ExcoriatedSagittaryRenderer extends MobRenderer<ExcoriatedSagittaryEntity,ExcoriatedSagittaryModel> {
    public static final ResourceLocation TEXTURE=Hemomancy.rloc("textures/entity/excoriated_sagittary/excoriated_sagittary.png");
    public ExcoriatedSagittaryRenderer(EntityRendererProvider.Context context) {
        super(context,new ExcoriatedSagittaryModel(context.bakeLayer(ExcoriatedSagittaryModel.LAYER_LOCATION)),.75F);
        addLayer(new RenderLayer<>(this) {
            @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,ExcoriatedSagittaryEntity entity,
                                          float swing,float amount,float partial,float age,float yaw,float pitch) {
                float pulse=PhlegethonticRules.pulseStrength(entity.level().getGameTime(),partial);
                if(!entity.isInvisible() && !entity.clotted() && pulse>0)
                    getParentModel().renderVessels(pose,buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                            light,OverlayTexture.NO_OVERLAY,((int)(pulse*180)<<24)|0xDA3535);
            }
        });
    }
    @Override public ResourceLocation getTextureLocation(ExcoriatedSagittaryEntity entity) {return TEXTURE;}
    public net.minecraft.world.phys.Vec3 bowAnchor(ExcoriatedSagittaryEntity entity,float partial) {
        PoseStack pose=new PoseStack();float scale=entity.getScale();pose.scale(scale,scale,scale);
        float bodyYaw=net.minecraft.util.Mth.rotLerp(partial,entity.yBodyRotO,entity.yBodyRot);
        float headYaw=net.minecraft.util.Mth.rotLerp(partial,entity.yHeadRotO,entity.yHeadRot)-bodyYaw;
        float age=getBob(entity,partial);
        setupRotations(entity,pose,age,bodyYaw,partial,scale);pose.scale(-1,-1,1);this.scale(entity,pose,partial);
        pose.translate(0,-1.501,0);
        getModel().setupAnim(entity,entity.walkAnimation.position(partial),entity.walkAnimation.speed(partial),age,
                headYaw,net.minecraft.util.Mth.lerp(partial,entity.xRotO,entity.getXRot()));
        getModel().translateToBow(pose);
        var point=pose.last().pose().transformPosition(new org.joml.Vector3f());
        return entity.getPosition(partial).add(point.x,point.y,point.z);
    }
}
