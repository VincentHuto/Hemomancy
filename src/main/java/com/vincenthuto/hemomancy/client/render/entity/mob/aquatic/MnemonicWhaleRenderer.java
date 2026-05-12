package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.MnemonicWhaleModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleTuning;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MnemonicWhaleRenderer extends MobRenderer<MnemonicWhaleEntity, MnemonicWhaleModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/mnemonic_whale/model_mnemonic_whale.png");

	public MnemonicWhaleRenderer(Context context) {
		super(context, new MnemonicWhaleModel(context.bakeLayer(MnemonicWhaleModel.LAYER_LOCATION)),
				MnemonicWhaleTuning.RENDER_SHADOW_RADIUS);
	}

	@Override
	public ResourceLocation getTextureLocation(MnemonicWhaleEntity entity) {
		return TEXTURE;
	}
}

