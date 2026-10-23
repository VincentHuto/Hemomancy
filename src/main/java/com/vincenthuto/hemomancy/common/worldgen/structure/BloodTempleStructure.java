package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity;
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

public class BloodTempleStructure extends HemoJigsawStructure {

	public static final MapCodec<BloodTempleStructure> CODEC = codec(BloodTempleStructure::new);

	private static boolean extraSpawningChecks(Structure.GenerationContext context) {
		return StructurePlacementChecks.isSuitableLandChunk(context);
	}

	public BloodTempleStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return BloodTempleStructure.extraSpawningChecks(context);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.blood_temple.get(); // Helps the game know how to turn this structure back to json to
													// save to chunks
	}

	/**
	 * Called after the structure's pieces have been placed in each overlapping
	 * chunk. Spawns exactly one {@link EntityInit#harbinger_hermit} at the
	 * centre of the structure, mirroring how the Termite Mound guarantees a
	 * single queen.
	 */
	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager,
			ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
			ChunkPos chunkPos, PiecesContainer pieces) {

		// Compute the full bounding box of the structure from all pieces
		BoundingBox fullBox = pieces.calculateBoundingBox();
		int centerX = (fullBox.minX() + fullBox.maxX()) / 2;
		int centerY = (fullBox.minY() + fullBox.maxY()) / 2;
		int centerZ = (fullBox.minZ() + fullBox.maxZ()) / 2;

		// Only spawn in the chunk that contains the centre to avoid duplicates
		if (!chunkBox.isInside(centerX, centerY, centerZ)) {
			return;
		}

		AbocipherEmitterPlacement.placeBloodTempleEmitters(level, fullBox, random,
				new BlockPos(centerX, centerY + 3, centerZ));

		BlockPos spawnPos = new BlockPos(centerX, centerY + 1, centerZ);
		BlockPos displayPos = findMortalDisplay(level, fullBox);
		BlockPos focusPos = findCardinalFocus(level, fullBox);

		// Search for a suitable air block near the centre
		for (int attempt = 0; attempt < 16; attempt++) {
			BlockPos candidate = (attempt == 0) ? spawnPos
					: spawnPos.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
			if (level.getBlockState(candidate).isAir()
					&& level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(),
							net.minecraft.core.Direction.UP)) {
				spawnMob(level, EntityInit.harbinger_hermit.get(), candidate, displayPos, focusPos);
				return;
			}
		}

		// Last resort: force-spawn at centre
		spawnMob(level, EntityInit.harbinger_hermit.get(), spawnPos, displayPos, focusPos);
	}

	private BlockPos findMortalDisplay(WorldGenLevel level, BoundingBox box) {
		return findBlock(level, box, BlockInit.mortal_display.get());
	}

	private BlockPos findCardinalFocus(WorldGenLevel level, BoundingBox box) {
		return findBlock(level, box, BlockInit.cardinal_focus.get());
	}

	private BlockPos findBlock(WorldGenLevel level, BoundingBox box,
			net.minecraft.world.level.block.Block block) {
		for (int y = box.minY(); y <= box.maxY(); y++) {
			for (int x = box.minX(); x <= box.maxX(); x++) {
				for (int z = box.minZ(); z <= box.maxZ(); z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (level.getBlockState(pos).is(block)) return pos;
				}
			}
		}
		return null;
	}

	/**
	 * Manually creates and adds a mob entity during world generation.
	 * Uses {@code EntityType.create()} + {@code addFreshEntityWithPassengers()}
	 * because the standard {@code EntityType.spawn()} requires a {@code ServerLevel}
	 * which is not available during structure placement (only {@code WorldGenRegion}).
	 */
	private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos,
			BlockPos displayPos, BlockPos focusPos) {
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
		if (displayPos != null
				&& level.getBlockEntity(displayPos) instanceof MortalDisplayBlockEntity display) {
			display.linkHermit(entity.getUUID());
		}
		if (focusPos != null && displayPos != null
				&& level.getBlockEntity(focusPos) instanceof CardinalFocusBlockEntity focus) {
			focus.linkTempleDisplay(displayPos);
			focus.linkTempleHermit(entity.getUUID());
		}
	}
}
