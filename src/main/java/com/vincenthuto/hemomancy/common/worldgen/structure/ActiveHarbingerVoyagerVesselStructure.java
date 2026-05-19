package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class ActiveHarbingerVoyagerVesselStructure extends Structure {
	private static final int WATERLINE_TEMPLATE_OFFSET = 3;

	public static final MapCodec<ActiveHarbingerVoyagerVesselStructure> CODEC = RecordCodecBuilder
			.<ActiveHarbingerVoyagerVesselStructure>mapCodec(instance -> instance.group(
							ActiveHarbingerVoyagerVesselStructure.settingsCodec(instance),
							StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
							ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
									.forGetter(structure -> structure.startJigsawName),
							Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
							HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
							Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
									.forGetter(structure -> structure.projectStartToHeightmap),
							Codec.intRange(1, 128).fieldOf("max_distance_from_center")
									.forGetter(structure -> structure.maxDistanceFromCenter))
					.apply(instance, ActiveHarbingerVoyagerVesselStructure::new));

	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int size;
	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;

	public ActiveHarbingerVoyagerVesselStructure(Structure.StructureSettings config,
			Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int size,
			HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap,
			int maxDistanceFromCenter) {
		super(config);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.size = size;
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!StructurePlacementChecks.isSuitableActiveVoyagerVesselChunk(context)) {
			return Optional.empty();
		}

		ChunkPos chunkPos = context.chunkPos();
		int centerX = chunkPos.getMinBlockX() + 8;
		int centerZ = chunkPos.getMinBlockZ() + 8;
		int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(centerX, centerZ,
				Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
		int configuredOffset = this.startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(),
				surfaceY - WATERLINE_TEMPLATE_OFFSET + configuredOffset, chunkPos.getMinBlockZ());

		return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.size, blockPos,
				false, this.projectStartToHeightmap, this.maxDistanceFromCenter, PoolAliasLookup.EMPTY,
				JigsawStructure.DEFAULT_DIMENSION_PADDING, LiquidSettings.APPLY_WATERLOGGING);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.harbinger_voyager_vessel.get();
	}

	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, PiecesContainer pieces) {
		BoundingBox fullBox = pieces.calculateBoundingBox();
		int centerX = (fullBox.minX() + fullBox.maxX()) / 2;
		int centerY = (fullBox.minY() + fullBox.maxY()) / 2;
		int centerZ = (fullBox.minZ() + fullBox.maxZ()) / 2;

		if (!chunkBox.isInside(centerX, centerY, centerZ)) {
			return;
		}

		ActiveHarbingerVoyagerNpcSpawner.spawnForActiveVessel(level, fullBox,
				new BlockPos(centerX, centerY, centerZ), random);
	}
}
