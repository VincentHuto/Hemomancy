package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public final class CircusPavilionStructure extends Structure {
	public static final MapCodec<CircusPavilionStructure> CODEC = RecordCodecBuilder
			.<CircusPavilionStructure>mapCodec(instance -> instance.group(
						CircusPavilionStructure.settingsCodec(instance),
						StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
						ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
								.forGetter(structure -> structure.startJigsawName),
						Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
						HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
						Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
								.forGetter(structure -> structure.projectStartToHeightmap),
						Codec.intRange(1, 128).fieldOf("max_distance_from_center")
								.forGetter(structure -> structure.maxDistanceFromCenter))
				.apply(instance, CircusPavilionStructure::new));

	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int size;
	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;

	public CircusPavilionStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
			Optional<ResourceLocation> startJigsawName, int size, HeightProvider startHeight,
			Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter) {
		super(settings);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.size = size;
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!StructurePlacementChecks.isSuitableCircusPavilionSite(context)) {
			return Optional.empty();
		}

		int startY = startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
		ChunkPos chunk = context.chunkPos();
		BlockPos start = new BlockPos(chunk.getMinBlockX(), startY, chunk.getMinBlockZ());
		return JigsawPlacement.addPieces(context, startPool, startJigsawName, size, start, false,
				projectStartToHeightmap, maxDistanceFromCenter, PoolAliasLookup.EMPTY,
				JigsawStructure.DEFAULT_DIMENSION_PADDING, LiquidSettings.APPLY_WATERLOGGING);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.circus_pavilion.get();
	}
}
