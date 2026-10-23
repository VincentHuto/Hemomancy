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

public class BogBodyOssuaryNicheStructure extends HemoJigsawStructure {
	private static final ResourceLocation PLACEHOLDER_ID = Hemomancy.rloc("random/ossuary_memo");
	private static final List<ResourceLocation> REPLACEMENTS = List.of(
			Hemomancy.rloc("bog_ossuary/memo_1"),
			Hemomancy.rloc("bog_ossuary/memo_2"),
			Hemomancy.rloc("bog_ossuary/memo_3"),
			Hemomancy.rloc("bog_ossuary/memo_4"),
			Hemomancy.rloc("bog_ossuary/memo_5"),
			Hemomancy.rloc("bog_ossuary/memo_6"));

	public static final MapCodec<BogBodyOssuaryNicheStructure> CODEC = codec(BogBodyOssuaryNicheStructure::new);

	private static boolean extraSpawningChecks(Structure.GenerationContext context) {
		return StructurePlacementChecks.isSuitableSwampChunk(context);
	}

	public BogBodyOssuaryNicheStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return extraSpawningChecks(context);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.bog_body_ossuary_niche.get();
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

