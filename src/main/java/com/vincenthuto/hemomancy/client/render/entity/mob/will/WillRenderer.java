package com.vincenthuto.hemomancy.client.render.entity.mob.will;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.will.WillModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillPhase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WillRenderer extends MobRenderer<WillEntity, WillModel> {
	private static final ResourceLocation HERMIT_TEXTURE =
			Hemomancy.rloc("textures/entity/will/will.png");
	private static final float MONOLITH_SHELL_SCALE = 1.035F;
	private static final int MONOLITH_SHELL_LIGHT = 0x00F000F0;

	public WillRenderer(EntityRendererProvider.Context context) {
		super(context, new WillModel(context.bakeLayer(WillModel.LAYER_LOCATION)), 0.25F);
		this.addLayer(new WillMonolithShellLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(WillEntity entity) {
		return HERMIT_TEXTURE;
	}

	@Override
	protected RenderType getRenderType(WillEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
		return RenderType.entityTranslucent(this.getTextureLocation(entity));
	}

	@Override
	public void render(WillEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		int hurtTime = entity.hurtTime;
		try {
			entity.hurtTime = 0;
			super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		} finally {
			entity.hurtTime = hurtTime;
		}
	}

	@Override
	protected boolean shouldShowName(WillEntity entity) {
		return !entity.isInvisible();
	}

	@Override
	protected void renderNameTag(WillEntity entity, Component displayName, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, float partialTick) {
		super.renderNameTag(entity, stateLabel(entity.getPhase()), poseStack, buffer, packedLight, partialTick);
	}

	private static Component stateLabel(WillPhase phase) {
		return Component.literal(switch (phase) {
			case DRIFTING -> "Drifting";
			case MATERIALIZED -> "Materialized";
			case FALTERING -> "Faltering";
			case ABSORBING -> "Absorbing";
			case DISSOLVING -> "Dissolving";
		});
	}

	private static float[] shellColor(WillPhase phase) {
		return switch (phase) {
			case DRIFTING -> new float[] { 0.35F, 0.55F, 1.0F };
			case MATERIALIZED -> new float[] { 0.95F, 0.95F, 1.0F };
			case FALTERING -> new float[] { 1.0F, 0.82F, 0.29F };
			case ABSORBING -> new float[] { 0.24F, 0.05F, 0.08F };
			case DISSOLVING -> new float[] { 1.0F, 0.24F, 0.24F };
		};
	}

	private static class WillMonolithShellLayer extends RenderLayer<WillEntity, WillModel> {
		private WillMonolithShellLayer(RenderLayerParent<WillEntity, WillModel> renderer) {
			super(renderer);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, WillEntity entity,
				float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
				float netHeadYaw, float headPitch) {
			if (entity.isInvisible()) {
				return;
			}
			float seed = Math.floorMod(entity.getUUID().hashCode(), 10007) / 10007.0F;
			float[] color = shellColor(entity.getPhase());
			float dissolveProgress = entity.getDissolveProgress(partialTicks);
			float absorptionProgress = entity.getAbsorptionProgress(partialTicks);
			float flickerAlpha = entity.getPhase() == WillPhase.ABSORBING
					? absorptionFlickerAlpha(ageInTicks, absorptionProgress, seed)
					: 1.0F;
			VertexConsumer consumer = buffer.getBuffer(HemoRenderTypes.willStateMonolithShell(
					ageInTicks / 20.0F, seed, color[0], color[1], color[2], dissolveProgress, flickerAlpha));
			poseStack.pushPose();
			poseStack.scale(MONOLITH_SHELL_SCALE, MONOLITH_SHELL_SCALE, MONOLITH_SHELL_SCALE);
			this.getParentModel().renderWithColor(poseStack, consumer, MONOLITH_SHELL_LIGHT,
					OverlayTexture.NO_OVERLAY, 0x33FFFFFF);
			poseStack.popPose();
		}

		private static float absorptionFlickerSpeed(float absorptionProgress) {
			return 8.0F + absorptionProgress * absorptionProgress * 54.0F;
		}

		private static float absorptionFlickerAlpha(float ageInTicks, float absorptionProgress, float seed) {
			float wave = Mth.sin(ageInTicks * absorptionFlickerSpeed(absorptionProgress) + seed * 31.0F);
			float amplitude = 0.12F + absorptionProgress * 0.38F;
			return Mth.clamp(0.72F + wave * amplitude, 0.10F, 1.0F);
		}
	}
}
