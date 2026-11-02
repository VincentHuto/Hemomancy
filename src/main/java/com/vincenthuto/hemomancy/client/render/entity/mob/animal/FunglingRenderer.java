package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.FunglingModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;

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

public class FunglingRenderer extends MobRenderer<FunglingEntity, FunglingModel> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/fungling/model_fungling.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/fungling/model_fungling_glow.png"));

	public FunglingRenderer(Context context) {
		super(context, new FunglingModel(context.bakeLayer(FunglingModel.LAYER_LOCATION)), 0.4F);
		addLayer(new GlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(FunglingEntity entity) {
		return TEXTURE;
	}

	/** Its eye and amber spots brighten and fade together, each fungling at its own time. */
	private static final class GlowLayer extends RenderLayer<FunglingEntity, FunglingModel> {
		GlowLayer(RenderLayerParent<FunglingEntity, FunglingModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack pose, MultiBufferSource buffers, int light, FunglingEntity entity, float limbSwing,
				float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			int level = (int) (255 * (0.65F + 0.35F * Mth.sin((ageInTicks + entity.getId() * 11) * 0.12F)));
			getParentModel().renderToBuffer(pose, buffers.getBuffer(GLOW), light, OverlayTexture.NO_OVERLAY,
					FastColor.ARGB32.color(255, level, level, level));
		}
	}
}
