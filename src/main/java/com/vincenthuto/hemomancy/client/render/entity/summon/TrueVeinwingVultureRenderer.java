package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.TrueVeinwingVultureModel;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** The Veinwing Vulture's true self in the Acrobat's Chamber (2026-11-17): its own rig, its wing veins lit. */
public class TrueVeinwingVultureRenderer extends MobRenderer<VeinwingVultureEntity, TrueVeinwingVultureModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/true_veinwing_vulture.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/true_veinwing_vulture_glow.png"));

	public TrueVeinwingVultureRenderer(EntityRendererProvider.Context context) {
		super(context, new TrueVeinwingVultureModel(context.bakeLayer(TrueVeinwingVultureModel.LAYER_LOCATION)), 0.35F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(VeinwingVultureEntity entity) {
		return TEXTURE;
	}
}
