package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.LanternTickModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.LanternTickEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class LanternTickRenderer extends MobRenderer<LanternTickEntity, LanternTickModel> {
	public LanternTickRenderer(EntityRendererProvider.Context context) {
		super(context, new LanternTickModel(context.bakeLayer(LanternTickModel.LAYER_LOCATION)), 0.35F);
		addLayer(new RenderLayer<LanternTickEntity, LanternTickModel>(this) {
			@Override
			public void render(PoseStack pose, MultiBufferSource buffers, int packedLight, LanternTickEntity entity,
					float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
					float netHeadYaw, float headPitch) {
				if (!entity.isInvisible()) {
					getParentModel().renderLantern(pose, buffers);
				}
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(LanternTickEntity entity) {
		return LanternTickModel.TEXTURE;
	}
}
