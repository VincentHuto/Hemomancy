package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.Optional;
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

/**
 * W-1: a Hemomancy structure assembled from a template pool. Every one reads the same six codec fields
 * ({@code start_pool}, {@code start_jigsaw_name}, {@code size}, {@code start_height},
 * {@code project_start_to_heightmap}, {@code max_distance_from_center}); structure JSONs and the registered
 * structure types are unchanged. {@code use_expansion_hack} is not read and stays off, as before. A structure
 * supplies its placement check and its {@code afterPlace} dressing; one with its own start position overrides
 * {@link #findGenerationPoint} and still places through {@link #addPieces}.
 */
public abstract class HemoJigsawStructure extends Structure {
	public record Jigsaw(Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int size,
			HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter) {
	}

	protected final Jigsaw jigsaw;

	protected HemoJigsawStructure(StructureSettings settings, Jigsaw jigsaw) {
		super(settings);
		this.jigsaw = jigsaw;
	}

	public static <S extends HemoJigsawStructure> MapCodec<S> codec(BiFunction<StructureSettings, Jigsaw, S> factory) {
		return RecordCodecBuilder.<S>mapCodec(instance -> instance.group(
						settingsCodec(instance),
						StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.jigsaw.startPool()),
						ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
								.forGetter(structure -> structure.jigsaw.startJigsawName()),
						Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.jigsaw.size()),
						HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.jigsaw.startHeight()),
						Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
								.forGetter(structure -> structure.jigsaw.projectStartToHeightmap()),
						Codec.intRange(1, 128).fieldOf("max_distance_from_center")
								.forGetter(structure -> structure.jigsaw.maxDistanceFromCenter()))
				.apply(instance, (settings, startPool, startJigsawName, size, startHeight, heightmap, maxDistance) ->
						factory.apply(settings, new Jigsaw(startPool, startJigsawName, size, startHeight, heightmap, maxDistance))));
	}

	/** Whether this chunk suits the structure: biome, terrain and spacing checks beyond the structure set. */
	protected abstract boolean canGenerateAt(GenerationContext context);

	/** The checked chunk's north-west corner at the configured start height. */
	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!canGenerateAt(context)) {
			return Optional.empty();
		}
		int startY = sampleStartHeight(context);
		ChunkPos chunkPos = context.chunkPos();
		return addPieces(context, new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ()),
				jigsaw.projectStartToHeightmap());
	}

	/**
	 * The pieces natural generation would lay out if the ground's first free block were {@code surface}: the same start
	 * height and rotation, without the site check or the heightmap lookup. Review clients and dev commands place a
	 * structure exactly where it would sit on that ground.
	 */
	public Optional<GenerationStub> assembleOnSurface(GenerationContext context, BlockPos surface) {
		return addPieces(context, surface.above(sampleStartHeight(context)), Optional.empty());
	}

	protected int sampleStartHeight(GenerationContext context) {
		return jigsaw.startHeight().sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
	}

	protected Optional<GenerationStub> addPieces(GenerationContext context, BlockPos start,
			Optional<Heightmap.Types> projectStartToHeightmap) {
		return JigsawPlacement.addPieces(context, jigsaw.startPool(), jigsaw.startJigsawName(), jigsaw.size(), start,
				false, projectStartToHeightmap, jigsaw.maxDistanceFromCenter(), PoolAliasLookup.EMPTY,
				JigsawStructure.DEFAULT_DIMENSION_PADDING, LiquidSettings.APPLY_WATERLOGGING);
	}
}
