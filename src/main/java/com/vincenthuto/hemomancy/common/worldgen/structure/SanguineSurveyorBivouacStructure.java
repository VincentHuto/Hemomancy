package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class SanguineSurveyorBivouacStructure extends HemoJigsawStructure {
	/** The camp's template height: a floor needs this much open air above it. */
	private static final int HEADROOM = 8;
	private static final ResourceLocation PLACEHOLDER_ID = Hemomancy.rloc("random/surveyor_log");
	private static final List<ResourceLocation> REPLACEMENTS = List.of(
			Hemomancy.rloc("surveyor_bivouac/log_1"),
			Hemomancy.rloc("surveyor_bivouac/log_2"),
			Hemomancy.rloc("surveyor_bivouac/log_3"),
			Hemomancy.rloc("surveyor_bivouac/log_4"),
			Hemomancy.rloc("surveyor_bivouac/log_5"),
			Hemomancy.rloc("surveyor_bivouac/log_6"));

	public static final MapCodec<SanguineSurveyorBivouacStructure> CODEC = codec(SanguineSurveyorBivouacStructure::new);

	public SanguineSurveyorBivouacStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.canPlaceOverworldHemomancyStructure(context);
	}

	/**
	 * Under a ceiling (the Nether) the surface heightmap finds the roof, so the camp starts on the highest floor
	 * under it with room above, clear of the lava sea; with open sky it projects to the heightmap as configured.
	 */
	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		NoiseColumn column = context.chunkGenerator().getBaseColumn(chunkPos.getMiddleBlockX(),
				chunkPos.getMiddleBlockZ(), context.heightAccessor(), context.randomState());
		int top = context.chunkGenerator().getMinY() + context.chunkGenerator().getGenDepth() - 1;
		if (column.getBlock(top).isAir()) {
			return super.findGenerationPoint(context);
		}
		if (!canGenerateAt(context)) {
			return Optional.empty();
		}
		OptionalInt floor = coveredFloor(column, top, context.chunkGenerator().getSeaLevel() + 2);
		if (floor.isEmpty()) {
			return Optional.empty();
		}
		return addPieces(context, new BlockPos(chunkPos.getMinBlockX(), floor.getAsInt(), chunkPos.getMinBlockZ()),
				Optional.empty());
	}

	/** The highest open cell over solid, dry ground with {@link #HEADROOM} of air above it, scanning down. */
	private static OptionalInt coveredFloor(NoiseColumn column, int top, int lowest) {
		int air = 0;
		for (int y = top; y > lowest; y--) {
			BlockState state = column.getBlock(y);
			if (state.isAir()) {
				air++;
				continue;
			}
			if (air >= HEADROOM && state.getFluidState().isEmpty()) {
				return OptionalInt.of(y + 1);
			}
			air = 0;
		}
		return OptionalInt.empty();
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.sanguine_surveyor_bivouac.get();
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

		DiscoveryInscriptionRandomizer.replacePlaceholderIds(level, fullBox, random, PLACEHOLDER_ID, REPLACEMENTS);
	}
}

