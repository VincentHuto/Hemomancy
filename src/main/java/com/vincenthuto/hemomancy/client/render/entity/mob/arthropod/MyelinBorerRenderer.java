package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.MyelinBorerModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class MyelinBorerRenderer extends MobRenderer<MyelinBorerEntity, MyelinBorerModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/myelin_borer/model_myelin_borer.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/myelin_borer/model_myelin_borer_glow.png"));

	public MyelinBorerRenderer(Context renderManagerIn) {
		super(renderManagerIn, new MyelinBorerModel(renderManagerIn.bakeLayer(MyelinBorerModel.LAYER_LOCATION)), 0.3F);
		addLayer(new GlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(MyelinBorerEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(MyelinBorerEntity entity, float yaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		if (!entity.isBlinking()) {
			super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
		}
	}

	/** Its nodes and palps glow faintly, and burn while it is agitated. */
	private static final class GlowLayer extends RenderLayer<MyelinBorerEntity, MyelinBorerModel> {
		GlowLayer(RenderLayerParent<MyelinBorerEntity, MyelinBorerModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack pose, MultiBufferSource buffers, int light, MyelinBorerEntity entity, float limbSwing,
				float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			float level = entity.isAgitated() ? 0.85F + 0.15F * Mth.sin(ageInTicks * 0.5F)
					: 0.35F + 0.15F * Mth.sin((ageInTicks + entity.getId() * 7) * 0.1F);
			int channel = (int) (255 * level);
			getParentModel().renderToBuffer(pose, buffers.getBuffer(GLOW), light, OverlayTexture.NO_OVERLAY,
					FastColor.ARGB32.color(255, channel, channel, channel));
		}
	}
}
