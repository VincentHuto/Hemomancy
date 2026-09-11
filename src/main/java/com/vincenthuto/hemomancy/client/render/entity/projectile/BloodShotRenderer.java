package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class BloodShotRenderer extends EntityRenderer<BloodShotEntity> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/block/end_portal.png");

	public BloodShotRenderer(Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(BloodShotEntity entity) {
		return TEXTURE;
	}

    @Override
    public void render(BloodShotEntity entity,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack poses,
            net.minecraft.client.renderer.MultiBufferSource buffers,int light) {
        // The shared blood pass draws this projectile together with its curved wake.
        super.render(entity,yaw,partial,poses,buffers,light);
    }

}
