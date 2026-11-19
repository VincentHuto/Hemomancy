package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.TrueScarletMummerModel;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * The Scarlet Mummer's true self in the Stilt Walker's Chamber (2026-11-17): its own rig, the eyes on its train and
 * the eye burning through its cracked mask lit.
 */
public class TrueScarletMummerRenderer extends MobRenderer<ScarletMummerEntity, TrueScarletMummerModel> {
	/** How many times over the true self is drawn: about three and a half blocks to the tips of its horns. */
	public static final float SCALE = 1.35F;
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/true_scarlet_mummer.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/true_scarlet_mummer_glow.png"));

	public TrueScarletMummerRenderer(EntityRendererProvider.Context context) {
		super(context, new TrueScarletMummerModel(context.bakeLayer(TrueScarletMummerModel.LAYER_LOCATION)), 0.5F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(ScarletMummerEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(ScarletMummerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) return;
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
