package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.CrimsonDoeModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.CrimsonDoeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public final class CrimsonDoeRenderer extends MobRenderer<CrimsonDoeEntity, CrimsonDoeModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/crimson_doe/model_crimson_doe_redesign.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/crimson_doe/model_crimson_doe_redesign_glow.png"));

	public CrimsonDoeRenderer(EntityRendererProvider.Context context) {
		super(context, new CrimsonDoeModel(), 0.5F);
		addLayer(new GlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(CrimsonDoeEntity entity) {
		return TEXTURE;
	}

	/** Its veins, eyes and antlers brighten on a slow double heartbeat. */
	private static final class GlowLayer extends RenderLayer<CrimsonDoeEntity, CrimsonDoeModel> {
		GlowLayer(RenderLayerParent<CrimsonDoeEntity, CrimsonDoeModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack pose, MultiBufferSource buffers, int light, CrimsonDoeEntity entity, float limbSwing,
				float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			int level = (int) (255 * (0.35F + 0.65F * CrimsonDoeModel.heartbeat(ageInTicks, entity.getId())));
			getParentModel().renderToBuffer(pose, buffers.getBuffer(GLOW), light, OverlayTexture.NO_OVERLAY,
					FastColor.ARGB32.color(255, level, level, level));
		}
	}
}
