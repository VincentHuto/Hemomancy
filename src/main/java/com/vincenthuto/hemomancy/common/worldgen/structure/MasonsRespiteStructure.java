package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public class MasonsRespiteStructure extends HemoJigsawStructure {
	public static final MapCodec<MasonsRespiteStructure> CODEC = codec(MasonsRespiteStructure::new);

	private static final int MAX_PLACEMENT_ATTEMPTS = 20;

	public MasonsRespiteStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.canPlaceOverworldHemomancyStructure(context);
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

		int maxY = (fullBox.minY() + fullBox.maxY()) / 2 + 2;
		for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
			int dx = attempt == 0 ? 0 : random.nextInt(5) - 2;
			int dz = attempt == 0 ? 0 : random.nextInt(5) - 2;
			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(centerX + dx, floorY + 1, centerZ + dz);
			for (int y = floorY + 1; y <= maxY; y++) {
				mutable.setY(y);
				if (level.getBlockState(mutable).isAir()
						&& level.getBlockState(mutable.below()).isFaceSturdy(level, mutable.below(),
								net.minecraft.core.Direction.UP)) {
					spawnMob(level, EntityInit.harbinger_cicatrix_anchorite.get(), mutable.immutable());
					return;
				}
			}
		}

		spawnMob(level, EntityInit.harbinger_cicatrix_anchorite.get(),
				new BlockPos(centerX, floorY + 1, centerZ));
	}

	private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos) {
		T entity = type.create(level.getLevel());
		if (entity == null) return;
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				level.getRandom().nextFloat() * 360.0f, 0.0f);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
					MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.masons_respite.get();
	}
}
