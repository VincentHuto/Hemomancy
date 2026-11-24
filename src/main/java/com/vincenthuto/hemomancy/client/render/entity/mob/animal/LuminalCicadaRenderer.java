package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.LuminalCicadaModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public final class LuminalCicadaRenderer extends MobRenderer<LuminalCicadaEntity, LuminalCicadaModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/luminal_cicada/luminal_cicada.png");

	private static final RenderType LANTERN_GLOW = RenderType.eyes(Hemomancy.rloc(
			"textures/entity/luminal_cicada/luminal_cicada_glow.png"));

	public LuminalCicadaRenderer(EntityRendererProvider.Context context) {
		super(context, new LuminalCicadaModel(context.bakeLayer(LuminalCicadaModel.LAYER_LOCATION)), 0.24F);
		addLayer(new LanternLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(LuminalCicadaEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void setupRotations(LuminalCicadaEntity entity, PoseStack poseStack, float bob,
			float bodyYaw, float partialTick, float scale) {
		Direction face = entity.getClingFace();
		super.setupRotations(entity, poseStack, bob,
				face == null ? bodyYaw : LuminalCicadaRules.clingBodyYaw(face) + 180.0F, partialTick, scale);
		if (face != null) {
			// Local +Z points away from the trunk. Bring the feet to the surface before tilting upright.
			poseStack.translate(0.0D, 0.0D, -entity.getBbWidth() * 0.5D);
			poseStack.mulPose(Axis.XP.rotationDegrees(LuminalCicadaRules.clingTiltDegrees(face)));
		}
	}

	private static final class LanternLayer extends RenderLayer<LuminalCicadaEntity, LuminalCicadaModel> {
		LanternLayer(LuminalCicadaRenderer parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffers, int light, LuminalCicadaEntity entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
				float headPitch) {
			if (entity.isInvisible()) return;
			float pulse = 0.22F + 0.10F * Mth.sin(ageInTicks * 0.23F + entity.getId());
			int level = (int) (255.0F * Mth.lerp(entity.getFlashStrength(partialTick), pulse, 1.0F));
			getParentModel().renderToBuffer(poseStack, buffers.getBuffer(LANTERN_GLOW), light,
					OverlayTexture.NO_OVERLAY, FastColor.ARGB32.color(255, level, level, level));
		}
	}
}
