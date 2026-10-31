package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * The Blood Temple (tools/worldgen/harbinger_sites/temple.py). Its anchor jigsaw lies under the Cardinal Focus, and the
 * temple starts with that jigsaw on the chunk's middle, so the Focus, the Mortal Display and the Hermit's marker (a
 * {@code minecraft:light} block) all land in that one chunk: its afterPlace finds them together, spawns the Hermit on
 * the marker and links the heart, the Focus and the Hermit.
 */
public class BloodTempleStructure extends HemoJigsawStructure {

	public static final MapCodec<BloodTempleStructure> CODEC = codec(BloodTempleStructure::new);

	public BloodTempleStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.isSuitableBloodTempleSite(context);
	}

	/** Starts on the chunk's middle, not its corner, so the anchor (and the heart beside it) stays in this chunk. */
	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!canGenerateAt(context)) return Optional.empty();
		ChunkPos chunk = context.chunkPos();
		return addPieces(context, new BlockPos(chunk.getMiddleBlockX(), sampleStartHeight(context),
				chunk.getMiddleBlockZ()), jigsaw.projectStartToHeightmap());
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.blood_temple.get();
	}

	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager,
			ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
			ChunkPos chunkPos, PiecesContainer pieces) {
		BoundingBox fullBox = pieces.calculateBoundingBox();
		StructureBraziers.rekindle(level, fullBox, chunkBox);
		BlockPos marker = find(level, fullBox, chunkBox, state -> state.is(Blocks.LIGHT));
		if (marker == null) return;
		level.setBlock(marker, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		BlockPos displayPos = find(level, fullBox, chunkBox, state -> state.is(BlockInit.mortal_display.get()));
		BlockPos focusPos = find(level, fullBox, chunkBox, state -> state.is(BlockInit.cardinal_focus.get()));
		AbocipherEmitterPlacement.placeBloodTempleEmitters(level, fullBox, random,
				(focusPos != null ? focusPos : marker).above(3));
		spawnHermit(level, marker, displayPos, focusPos);
	}

	/** The first block in the part of the temple this chunk placed that matches. */
	private static BlockPos find(WorldGenLevel level, BoundingBox fullBox, BoundingBox chunkBox,
			Predicate<BlockState> match) {
		var pos = new BlockPos.MutableBlockPos();
		for (int y = fullBox.minY(); y <= fullBox.maxY(); y++) {
			for (int x = Math.max(fullBox.minX(), chunkBox.minX()); x <= Math.min(fullBox.maxX(), chunkBox.maxX()); x++) {
				for (int z = Math.max(fullBox.minZ(), chunkBox.minZ()); z <= Math.min(fullBox.maxZ(), chunkBox.maxZ()); z++) {
					if (match.test(level.getBlockState(pos.set(x, y, z)))) return pos.immutable();
				}
			}
		}
		return null;
	}

	/**
	 * Creates the Hermit with {@code EntityType.create()} + {@code addFreshEntityWithPassengers()}: structure placement
	 * runs on a {@code WorldGenRegion}, not a {@code ServerLevel}, so {@code EntityType.spawn()} is unavailable.
	 */
	private static void spawnHermit(WorldGenLevel level, BlockPos pos, BlockPos displayPos, BlockPos focusPos) {
		EntityType<? extends Entity> type = EntityInit.harbinger_hermit.get();
		Entity entity = type.create(level.getLevel());
		if (entity == null) return;
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom()).toYRot(), 0.0f);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
		if (displayPos != null && level.getBlockEntity(displayPos) instanceof MortalDisplayBlockEntity display) {
			display.linkHermit(entity.getUUID());
		}
		if (focusPos != null && displayPos != null && level.getBlockEntity(focusPos) instanceof CardinalFocusBlockEntity focus) {
			focus.linkTempleDisplay(displayPos);
			focus.linkTempleHermit(entity.getUUID());
		}
	}
}
