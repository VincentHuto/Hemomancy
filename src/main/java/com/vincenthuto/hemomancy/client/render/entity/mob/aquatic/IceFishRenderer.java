package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.IceFishModel;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.SpecimenJarRenderer;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.IceFishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class IceFishRenderer extends MobRenderer<IceFishEntity, IceFishModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/ice_fish.png");

    public IceFishRenderer(EntityRendererProvider.Context context) {
        super(context, new IceFishModel(context.bakeLayer(IceFishModel.LAYER_LOCATION)), .15F);
    }

    @Override public ResourceLocation getTextureLocation(IceFishEntity entity) { return TEXTURE; }

    @Override protected void setupRotations(IceFishEntity entity, PoseStack pose, float age, float yaw,
                                            float partialTick, float scale) {
        super.setupRotations(entity, pose, age, yaw, partialTick, scale);
        pose.mulPose(Axis.YP.rotationDegrees(4 * Mth.sin(age * .35F)));
        if (!entity.isInWater() && !SpecimenJarRenderer.isDisplayEntity(entity)) {
            pose.translate(.1F, .1F, -.1F);
            pose.mulPose(Axis.ZP.rotationDegrees(90));
        }
    }
}
