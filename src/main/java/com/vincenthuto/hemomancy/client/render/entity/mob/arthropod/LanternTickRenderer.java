package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.LanternTickModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.LanternTickEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class LanternTickRenderer extends MobRenderer<LanternTickEntity, LanternTickModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/lantern_tick/model_lantern_tick.png");

	public LanternTickRenderer(EntityRendererProvider.Context context) {
		super(context, new LanternTickModel(context.bakeLayer(LanternTickModel.LAYER_LOCATION)), 0.35F);
	}

	@Override
	public ResourceLocation getTextureLocation(LanternTickEntity entity) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLightLevel(LanternTickEntity entity, BlockPos pos) {
		return entity.shouldGlowAsLure() ? 15 : super.getBlockLightLevel(entity, pos);
	}
}
