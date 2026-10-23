package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public class MausoleumStructure extends HemoJigsawStructure {

	public static final MapCodec<MausoleumStructure> CODEC = codec(MausoleumStructure::new);

	public MausoleumStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.isSuitableBuriedMausoleumSite(context);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.mausoleum.get();
	}

	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager,
			ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
			ChunkPos chunkPos, PiecesContainer pieces) {
		BoundingBox fullBox = pieces.calculateBoundingBox();
		int centerX = (fullBox.minX() + fullBox.maxX()) / 2;
		int centerZ = (fullBox.minZ() + fullBox.maxZ()) / 2;
		int floorY = fullBox.minY();
		if (!chunkBox.isInside(centerX, floorY, centerZ)) {
			return;
		}

		BlockPos inscriptionOrigin = new BlockPos(centerX, floorY + 1, centerZ);
		DiscoveryInscriptionPlacement.placeOnInteriorWall(level, fullBox, inscriptionOrigin.offset(-3, 1, 3),
				BlockInit.blood_echo_inscription.get(), Hemomancy.rloc("mausoleum/saints_echo"));
		DiscoveryInscriptionPlacement.placeOnInteriorFloor(level, fullBox, inscriptionOrigin.offset(3, 0, -3),
				BlockInit.rite_fragment_inscription.get(), Hemomancy.rloc("mausoleum/sanguine_brotherhood_fragment"));
	}
}

