package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.TrueMnemonistPuppetModel;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** The Mnemonist Puppet's true self in the Retired Understudy's Chamber (2026-11-17): its eye and spool core lit. */
public class TrueMnemonistPuppetRenderer extends MobRenderer<MnemonistPuppetEntity, TrueMnemonistPuppetModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/true_mnemonist_puppet.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/true_mnemonist_puppet_glow.png"));

	public TrueMnemonistPuppetRenderer(EntityRendererProvider.Context context) {
		super(context, new TrueMnemonistPuppetModel(context.bakeLayer(TrueMnemonistPuppetModel.LAYER_LOCATION)), 0.45F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(MnemonistPuppetEntity entity) {
		return TEXTURE;
	}
}
