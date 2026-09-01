package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.WretchedWillModel;
import com.vincenthuto.hemomancy.common.entity.summon.EntityWretchedWill;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class WretchedWillRenderer extends MobRenderer<EntityWretchedWill, WretchedWillModel<EntityWretchedWill>> {

	public static final ResourceLocation texture = Hemomancy.rloc("textures/entity/wretched_will/modelwretchedwill.png");

	public WretchedWillRenderer(EntityRendererProvider.Context p_174447_) {
		super(p_174447_, new WretchedWillModel<EntityWretchedWill>(p_174447_.bakeLayer(WretchedWillModel.wretched_will)),
				0F);

	}

	@Nonnull
	@Override
	public ResourceLocation getTextureLocation(@Nonnull EntityWretchedWill entity) {
		return texture;
	}

}
