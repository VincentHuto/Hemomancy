package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.MnemonicWhaleModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleTuning;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MnemonicWhaleRenderer extends MobRenderer<MnemonicWhaleEntity, MnemonicWhaleModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/mnemonic_whale/model_mnemonic_whale.png");
	private static final RenderType NOETIC_GLOW = RenderType.entityTranslucentEmissive(Hemomancy.rloc(
			"textures/entity/mnemonic_whale/model_mnemonic_whale_glow.png"));

	public MnemonicWhaleRenderer(Context context) {
		super(context, new MnemonicWhaleModel(context.bakeLayer(MnemonicWhaleModel.LAYER_LOCATION)),
				MnemonicWhaleTuning.RENDER_SHADOW_RADIUS);
		addLayer(new RenderLayer<>(this) {
			@Override
			public void render(PoseStack pose, MultiBufferSource buffers, int light, MnemonicWhaleEntity whale,
					float swing, float amount, float partial, float age, float yaw, float pitch) {
				if (whale.isInvisible()) {
					return;
				}
				int alpha = Mth.floor(168.0F + 49.0F * Mth.sin(age * 0.065F));
				getParentModel().renderToBuffer(pose, buffers.getBuffer(NOETIC_GLOW), LightTexture.FULL_BRIGHT,
						OverlayTexture.NO_OVERLAY, (alpha << 24) | 0xFFFFFF);
			}
		});
	}

	@Override
	protected RenderType getRenderType(MnemonicWhaleEntity whale, boolean visible, boolean translucent, boolean glowing) {
		return visible ? RenderType.entityTranslucentCull(TEXTURE)
				: super.getRenderType(whale, false, translucent, glowing);
	}

	@Override
	public ResourceLocation getTextureLocation(MnemonicWhaleEntity entity) {
		return TEXTURE;
	}
}

