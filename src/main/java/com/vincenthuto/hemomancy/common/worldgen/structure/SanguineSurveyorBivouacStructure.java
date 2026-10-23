package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.List;

public class SanguineSurveyorBivouacStructure extends HemoJigsawStructure {
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

