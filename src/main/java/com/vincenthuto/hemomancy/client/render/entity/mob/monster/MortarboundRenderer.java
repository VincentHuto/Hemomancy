package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.MortarboundModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundEntity;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MortarboundRenderer extends MobRenderer<MortarboundEntity, MortarboundModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/mortarbound.png");
    public MortarboundRenderer(EntityRendererProvider.Context context) {
        super(context, new MortarboundModel(context.bakeLayer(MortarboundModel.LAYER_LOCATION)), .2F);
    }
    @Override protected void setupRotations(MortarboundEntity entity, PoseStack pose, float bob, float bodyYaw, float partialTick, float scale) {
        super.setupRotations(entity, pose, bob, bodyYaw, partialTick, scale);
        pose.mulPose(Axis.YP.rotationDegrees(bodyYaw - entity.wallFace().toYRot()));
    }
    @Override public ResourceLocation getTextureLocation(MortarboundEntity entity) { return TEXTURE; }
}
