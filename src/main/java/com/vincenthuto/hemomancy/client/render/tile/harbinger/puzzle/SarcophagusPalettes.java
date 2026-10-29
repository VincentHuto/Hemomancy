package com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.RetexturedBakedModel;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SarcophagusGeometry;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Each Saint's tomb in its own palette. tools/model_export/saint_sarcophagus.py paints a Saint's atlas in the default
 * atlas's exact layout and lists it in {@link SarcophagusGeometry#SAINT_PALETTES}; at bake time every quad of the tomb
 * is moved onto that atlas ({@link RetexturedBakedModel}), the baked body per facing and the renderer's moving parts
 * alike. The body picks its palette from the tomb's block entity as its section is meshed, so it stays chunk-baked and
 * the structure templates need no new state; the renderer picks its parts by the same Saint. A Saint without a palette,
 * or a tomb no relic has named, keeps the default.
 */
public final class SarcophagusPalettes {
	/** The Saint whose palette a tomb's section is meshed in; absent for the default palette. */
	private static final ModelProperty<EnumSaintType> SAINT = new ModelProperty<>();
	/** The moving parts in each Saint's palette, by part name; replaced on every model bake. */
	private static volatile Map<EnumSaintType, Map<String, BakedModel>> parts = Map.of();

	private SarcophagusPalettes() {}

	/** Wraps every tomb state model with its Saints' palettes and bakes the palette parts; run after its sections. */
	public static void bake(ModelEvent.ModifyBakingResult evt) {
		Map<ModelResourceLocation, BakedModel> models = evt.getModels();
		TextureAtlasSprite plain = sprite(evt, SarcophagusGeometry.atlas(null));
		Map<EnumSaintType, TextureAtlasSprite> sprites = new EnumMap<>(EnumSaintType.class);
		for (EnumSaintType saint : EnumSaintType.values())
			if (!SarcophagusGeometry.atlas(saint).equals(SarcophagusGeometry.atlas(null)))
				sprites.put(saint, sprite(evt, SarcophagusGeometry.atlas(saint)));

		Map<EnumSaintType, Map<String, BakedModel>> baked = new EnumMap<>(EnumSaintType.class);
		sprites.forEach((saint, sprite) -> {
			Map<String, BakedModel> byPart = new HashMap<>();
			for (String part : SaintSarcophagusRenderer.PART_MODELS) {
				BakedModel model = models.get(SaintSarcophagusRenderer.part(part));
				if (model != null) byPart.put(part, new RetexturedBakedModel(model, plain, sprite));
			}
			baked.put(saint, Map.copyOf(byPart));
		});
		parts = baked;
		if (sprites.isEmpty()) return;

		for (BlockState state : BlockInit.saint_sarcophagus.get().getStateDefinition().getPossibleStates()) {
			ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
			BakedModel tomb = models.get(location);
			if (tomb == null) continue;
			Map<EnumSaintType, BakedModel> palettes = new EnumMap<>(EnumSaintType.class);
			sprites.forEach((saint, sprite) -> palettes.put(saint, new RetexturedBakedModel(tomb, plain, sprite)));
			models.put(location, new PalettedTomb(tomb, palettes));
		}
	}

	/** A moving part in the palette of the Saint at rest in the tomb, or the default part. */
	static BakedModel part(String name, @Nullable EnumSaintType saint) {
		Map<String, BakedModel> palette = saint == null ? null : parts.get(saint);
		BakedModel model = palette == null ? null : palette.get(name);
		return model != null ? model
				: Minecraft.getInstance().getModelManager().getModel(SaintSarcophagusRenderer.part(name));
	}

	private static TextureAtlasSprite sprite(ModelEvent.ModifyBakingResult evt, String atlas) {
		return evt.getTextureGetter().apply(new Material(TextureAtlas.LOCATION_BLOCKS, Hemomancy.rloc("block/" + atlas)));
	}

	/** The tomb's baked body, meshed in the palette of the Saint its block entity names. */
	private static final class PalettedTomb extends BakedModelWrapper<BakedModel> {
		private final Map<EnumSaintType, BakedModel> palettes;

		PalettedTomb(BakedModel tomb, Map<EnumSaintType, BakedModel> palettes) {
			super(tomb);
			this.palettes = palettes;
		}

		@Override
		public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
			ModelData base = super.getModelData(level, pos, state, data);
			EnumSaintType saint = level.getBlockEntity(pos) instanceof SaintSarcophagusBlockEntity tomb
					? tomb.getSaintType() : null;
			return palettes.containsKey(saint) ? base.derive().with(SAINT, saint).build() : base;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
				ModelData data, @Nullable RenderType renderType) {
			EnumSaintType saint = data.get(SAINT);
			BakedModel palette = saint == null ? null : palettes.get(saint);
			return palette != null ? palette.getQuads(state, side, rand, data, renderType)
					: super.getQuads(state, side, rand, data, renderType);
		}
	}
}
