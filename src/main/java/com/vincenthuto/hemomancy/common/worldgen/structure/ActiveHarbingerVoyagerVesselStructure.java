package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.Optional;

public class ActiveHarbingerVoyagerVesselStructure extends HemoJigsawStructure {
	private static final int WATERLINE_TEMPLATE_OFFSET = 3;

	public static final MapCodec<ActiveHarbingerVoyagerVesselStructure> CODEC = codec(ActiveHarbingerVoyagerVesselStructure::new);

	public ActiveHarbingerVoyagerVesselStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.isSuitableActiveVoyagerVesselChunk(context);
	}

	/** The single template sits at the waterline over the chunk centre's surface. */
	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!canGenerateAt(context)) {
			return Optional.empty();
		}

		ChunkPos chunkPos = context.chunkPos();
		int centerX = chunkPos.getMinBlockX() + 8;
		int centerZ = chunkPos.getMinBlockZ() + 8;
		int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(centerX, centerZ,
				Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
		int configuredOffset = sampleStartHeight(context);
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(),
				surfaceY - WATERLINE_TEMPLATE_OFFSET + configuredOffset, chunkPos.getMinBlockZ());

		return addPieces(context, blockPos, jigsaw.projectStartToHeightmap());
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
