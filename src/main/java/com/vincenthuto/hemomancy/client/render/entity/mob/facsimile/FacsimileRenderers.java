package com.vincenthuto.hemomancy.client.render.entity.mob.facsimile;

import com.vincenthuto.hemomancy.client.model.entity.mob.facsimile.FacsimileModels;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.ChestedHorseRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.monster.Spider;

import java.util.EnumMap;
import java.util.Map;

/**
 * Vanilla renderers wearing a facsimile's skew: each swaps in its {@link FacsimileModels} model and adds the fungal
 * growth layer. Vanilla textures are read where they live; none is copied.
 */
public final class FacsimileRenderers {
	private FacsimileRenderers() {}

	public static class Bat extends BatRenderer {
		public Bat(EntityRendererProvider.Context context) {
			super(context);
			model = new FacsimileModels.FacsimileBatModel(context.bakeLayer(ModelLayers.BAT));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.BAT));
		}
	}

	public static class Enderman extends EndermanRenderer {
		public Enderman(EntityRendererProvider.Context context) {
			super(context);
			model = new FacsimileModels.FacsimileEndermanModel(context.bakeLayer(ModelLayers.ENDERMAN));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.ENDERMAN));
		}
	}

	public static class Blaze extends BlazeRenderer {
		public Blaze(EntityRendererProvider.Context context) {
			super(context);
			model = new FacsimileModels.FacsimileBlazeModel(context.bakeLayer(ModelLayers.BLAZE));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.BLAZE));
		}
	}

	public static class Golem extends IronGolemRenderer {
		public Golem(EntityRendererProvider.Context context) {
			super(context);
			model = new FacsimileModels.FacsimileGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.GOLEM));
		}
	}

	public static class SpiderFacsimile extends SpiderRenderer<Spider> {
		public SpiderFacsimile(EntityRendererProvider.Context context) {
			super(context);
			model = new FacsimileModels.FacsimileSpiderModel(context.bakeLayer(ModelLayers.SPIDER));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.SPIDER));
		}
	}

	public static class Wolf extends WolfRenderer {
		public Wolf(EntityRendererProvider.Context context) {
			super(context);
			model = new FacsimileModels.FacsimileWolfModel(context.bakeLayer(ModelLayers.WOLF));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.WOLF));
		}
	}

	/** Vanilla's horse renderer is final, so this one repeats its variant textures and markings. */
	public static class HorseFacsimile extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
		private static final Map<Variant, ResourceLocation> TEXTURES = new EnumMap<>(Map.of(
				Variant.WHITE, horse("white"), Variant.CREAMY, horse("creamy"), Variant.CHESTNUT, horse("chestnut"),
				Variant.BROWN, horse("brown"), Variant.BLACK, horse("black"), Variant.GRAY, horse("gray"),
				Variant.DARK_BROWN, horse("darkbrown")));

		public HorseFacsimile(EntityRendererProvider.Context context) {
			super(context, new FacsimileModels.FacsimileHorseModel(context.bakeLayer(ModelLayers.HORSE)), 1.1F);
			addLayer(new HorseMarkingLayer(this));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.HORSE));
		}

		private static ResourceLocation horse(String variant) {
			return ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_" + variant + ".png");
		}

		@Override
		public ResourceLocation getTextureLocation(Horse entity) {
			return TEXTURES.get(entity.getVariant());
		}
	}

	public static class DonkeyFacsimile extends ChestedHorseRenderer<Donkey> {
		private static final ResourceLocation TEXTURE =
				ResourceLocation.withDefaultNamespace("textures/entity/horse/donkey.png");

		public DonkeyFacsimile(EntityRendererProvider.Context context) {
			super(context, 0.87F, ModelLayers.DONKEY);
			model = new FacsimileModels.FacsimileDonkeyModel(context.bakeLayer(ModelLayers.DONKEY));
			addLayer(new FacsimileGrowthLayer<>(this, FacsimileRenderTypes.DONKEY));
		}

		@Override
		public ResourceLocation getTextureLocation(Donkey entity) {
			return TEXTURE;
		}
	}
}
