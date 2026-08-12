package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperCrownedWeaponLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperMountAbsorptionLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperTransitionCocoonRenderer;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperThroneAnchorLayer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VesperTheCrownedRefusalRenderer
        extends MobRenderer<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/vesper_crowned_refusal.png");

    public VesperTheCrownedRefusalRenderer(Context context) {
        super(context, new VesperTheCrownedRefusalModel(context.bakeLayer(VesperTheCrownedRefusalModel.LAYER_LOCATION)), 4.5F);
        addLayer(new VesperCrownedWeaponLayer(this));
        addLayer(new VesperMountAbsorptionLayer(this));
        addLayer(new VesperThroneAnchorLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VesperTheCrownedRefusalEntity entity) {
        return TEXTURE;
    }

	@Override
	public boolean shouldRender(VesperTheCrownedRefusalEntity entity, Frustum frustum,
			double cameraX, double cameraY, double cameraZ) {
		if (entity.getTransitionTick() > 0 && entity.shouldRender(cameraX, cameraY, cameraZ)
				&& frustum.isVisible(entity.getBoundingBoxForCulling().inflate(6.0D))) {
			return true;
		}
		return super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
	}

	@Override
	public void render(VesperTheCrownedRefusalEntity entity, float entityYaw, float partialTick,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
		VesperTransitionCocoonRenderer.render(entity, partialTick, poseStack, buffer);
	}

    @Nullable
    @Override
    protected RenderType getRenderType(VesperTheCrownedRefusalEntity entity, boolean bodyVisible,
            boolean translucent, boolean glowing) {
        if (bodyVisible && VesperPhaseTransitionRules.isAbsorbing(entity.getTransitionTick())) {
            return RenderType.entityTranslucent(TEXTURE);
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }
}
