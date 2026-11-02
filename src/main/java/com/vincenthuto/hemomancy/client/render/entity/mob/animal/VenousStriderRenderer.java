package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.VenousStriderModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VenousStriderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public final class VenousStriderRenderer extends MobRenderer<VenousStriderEntity, VenousStriderModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/venous_strider/model_venous_strider.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/venous_strider/model_venous_strider_glow.png"));

	public VenousStriderRenderer(EntityRendererProvider.Context context) {
		super(context, new VenousStriderModel(context.bakeLayer(VenousStriderModel.LAYER_LOCATION)), 0.4F);
		addLayer(new GlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(VenousStriderEntity entity) {
		return TEXTURE;
	}

	/** Blood showing through its skin, swelling and fading with a long slow breath. */
	private static final class GlowLayer extends RenderLayer<VenousStriderEntity, VenousStriderModel> {
		GlowLayer(RenderLayerParent<VenousStriderEntity, VenousStriderModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack pose, MultiBufferSource buffers, int light, VenousStriderEntity entity, float limbSwing,
				float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			int level = (int) (255 * (0.6F + 0.4F * Mth.sin((ageInTicks + entity.getId() * 7) * 0.06F)));
			getParentModel().renderToBuffer(pose, buffers.getBuffer(GLOW), light, OverlayTexture.NO_OVERLAY,
					FastColor.ARGB32.color(255, level, level, level));
		}
	}
}
