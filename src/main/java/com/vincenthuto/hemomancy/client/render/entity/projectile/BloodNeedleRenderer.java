package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class BloodNeedleRenderer<T extends BloodNeedleEntity> extends EntityRenderer<T> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blood_needle/model_blood_needle.png");

	public BloodNeedleRenderer(Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return TEXTURE;
	}

	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.isCoronationSword()) {
            com.vincenthuto.hemomancy.client.render.world.AnimusMortemRenderTypes.begin(entityIn.level().getGameTime()+partialTicks);
            var direction = entityIn.getDeltaMovement();
            if (direction.lengthSqr() < .0001) direction = entityIn.getLookAngle();
            com.vincenthuto.hemomancy.client.render.world.ManipulationVisualRenderer.projectileSword(
                    matrixStackIn, bufferIn, direction, entityIn.swordOpacity(), entityIn.tickCount + partialTicks,entityIn.getId());
        }
        // Ordinary needles are drawn with their wake in the shared blood pass.
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
}
