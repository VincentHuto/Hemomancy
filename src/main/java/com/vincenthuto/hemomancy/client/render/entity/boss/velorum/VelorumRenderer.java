package com.vincenthuto.hemomancy.client.render.entity.boss.velorum;

import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.vincenthuto.hemomancy.client.render.layer.mob.VelorumFrostGlowLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VelorumRenderer extends MobRenderer<VelorumEntity, VelorumModel<VelorumEntity>> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/velorum/velorum.png");

    public VelorumRenderer(Context context) {
        super(context, new VelorumModel<>(context.bakeLayer(VelorumModel.LAYER_LOCATION)), 0.7F);
        addLayer(new VelorumFrostGlowLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VelorumEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(VelorumEntity entity, PoseStack poseStack, float partialTick) {
        SaintRiseRender.sink(poseStack, entity, partialTick);
    }

    @Override
    protected float getFlipDegrees(VelorumEntity entity) { return 0; }

    @Override
    public void render(VelorumEntity entity,float yaw,float partial,PoseStack poses,MultiBufferSource buffers,int light) {
        super.render(entity,yaw,partial,poses,buffers,light);
        float bodyYaw=Mth.rotLerp(partial,entity.yBodyRotO,entity.yBodyRot);
        Vec3[] lanterns=java.util.Arrays.stream(model.lanterns()).map(point->offset(point,bodyYaw)).toArray(Vec3[]::new);
        VelorumAnimState.of(entity).attachments(offset(model.core(),bodyYaw),offset(model.claw(),bodyYaw),lanterns);
    }
    private static Vec3 offset(Vector3f point,float yaw) {
        point.set(-point.x,1.501F-point.y,point.z);
        return new Vec3(point.rotateY((180-yaw)*Mth.DEG_TO_RAD));
    }
}
