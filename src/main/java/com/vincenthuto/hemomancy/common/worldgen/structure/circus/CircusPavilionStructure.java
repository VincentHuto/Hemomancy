package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.AbocipherEmitterPlacement;
import com.vincenthuto.hemomancy.common.worldgen.structure.HemoJigsawStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.StructurePlacementChecks;
import net.minecraft.core.QuartPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.Optional;

public final class CircusPavilionStructure extends HemoJigsawStructure {
	public static final MapCodec<CircusPavilionStructure> CODEC = codec(CircusPavilionStructure::new);

	public CircusPavilionStructure(Structure.StructureSettings settings, Jigsaw jigsaw) {
		super(settings, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.canPlaceOverworldHemomancyStructure(context);
	}

	/** The usual start, then the biome and troupe-footprint checks on the assembled pieces. */
	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		var generated = super.findGenerationPoint(context);
        if (generated.isEmpty()) return Optional.empty();
        // Vanilla rejects this same stub after findGenerationPoint returns. Check before expensive terrain scans.
        var position = generated.get().position();
        if (!context.validBiome().test(context.chunkGenerator().getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(position.getX()), QuartPos.fromBlock(position.getY()),
                QuartPos.fromBlock(position.getZ()), context.randomState().sampler()))) return Optional.empty();
        var pieces = generated.get().getPiecesBuilder();
        if (!StructurePlacementChecks.isSuitableTroupeFootprint(context, pieces.getBoundingBox())) return Optional.empty();
        return Optional.of(new GenerationStub(generated.get().position(), com.mojang.datafixers.util.Either.right(pieces)));
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.circus_pavilion.get();
	}

	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager,
			ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
			ChunkPos chunkPos, PiecesContainer pieces) {
		BoundingBox fullBox = pieces.calculateBoundingBox();
		if (fullBox.getXSpan() < 65) return;
		int centerX = (fullBox.minX() + fullBox.maxX()) / 2;
		int centerZ = (fullBox.minZ() + fullBox.maxZ()) / 2;
		if (chunkBox.isInside(centerX, fullBox.minY(), centerZ)) {
			AbocipherEmitterPlacement.placeHarbingerOutpostEmitters(level, fullBox, random,
					centerX, centerZ, fullBox.minY(), fullBox.maxY());
		}
	}
}
