package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.StructureManager;

public class BloodTempleStructure extends Structure {

	public static final MapCodec<BloodTempleStructure> CODEC = RecordCodecBuilder
			.<BloodTempleStructure>mapCodec(instance -> instance.group(BloodTempleStructure.settingsCodec(instance),
					StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
					ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
							.forGetter(structure -> structure.startJigsawName),
					Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
					Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
							.forGetter(structure -> structure.projectStartToHeightmap),
					Codec.intRange(1, 128).fieldOf("max_distance_from_center")
							.forGetter(structure -> structure.maxDistanceFromCenter))
				.apply(instance, BloodTempleStructure::new));

	private static boolean extraSpawningChecks(Structure.GenerationContext context) {
		ChunkPos chunkpos = context.chunkPos();
		return context.chunkGenerator().getFirstOccupiedHeight(chunkpos.getMinBlockX(), chunkpos.getMinBlockZ(),
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState()) < 150;
	}
	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int size;
	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;

	private final int maxDistanceFromCenter;

	public BloodTempleStructure(Structure.StructureSettings config, Holder<StructureTemplatePool> startPool,
			Optional<ResourceLocation> startJigsawName, int size, HeightProvider startHeight,
			Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter) {
		super(config);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.size = size;
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {

		if (!BloodTempleStructure.extraSpawningChecks(context)) {
			return Optional.empty();
		}
		int startY = this.startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

		ChunkPos chunkPos = context.chunkPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

		Optional<Structure.GenerationStub> structurePiecesGenerator = JigsawPlacement.addPieces(context, this.startPool,
				this.startJigsawName, this.size, blockPos, false, this.projectStartToHeightmap,
			this.maxDistanceFromCenter, PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING,
			LiquidSettings.APPLY_WATERLOGGING);
		return structurePiecesGenerator;
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

		BlockPos spawnPos = new BlockPos(centerX, centerY + 1, centerZ);

		// Search for a suitable air block near the centre
		for (int attempt = 0; attempt < 16; attempt++) {
			BlockPos candidate = (attempt == 0) ? spawnPos
					: spawnPos.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
			if (level.getBlockState(candidate).isAir()
					&& level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(),
							net.minecraft.core.Direction.UP)) {
				spawnMob(level, EntityInit.harbinger_hermit.get(), candidate);
				return;
			}
		}

		// Last resort: force-spawn at centre
		spawnMob(level, EntityInit.harbinger_hermit.get(), spawnPos);
	}

	/**
	 * Manually creates and adds a mob entity during world generation.
	 * Uses {@code EntityType.create()} + {@code addFreshEntityWithPassengers()}
	 * because the standard {@code EntityType.spawn()} requires a {@code ServerLevel}
	 * which is not available during structure placement (only {@code WorldGenRegion}).
	 */
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
}