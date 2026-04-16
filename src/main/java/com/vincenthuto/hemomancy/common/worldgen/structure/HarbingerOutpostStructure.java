package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.Optional;

import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.StructureManager;
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
import net.minecraft.world.level.chunk.ChunkGenerator;

public class HarbingerOutpostStructure extends Structure {
	private static final int MAX_PLACEMENT_ATTEMPTS = 32;
	private static final int INITIAL_CENTERED_ATTEMPTS = 4;
	private static final int VICAR_SPAWN_SPREAD = 3;
	private static final int ALCHEMIST_SPAWN_SPREAD = 2;

	public static final Codec<HarbingerOutpostStructure> CODEC = RecordCodecBuilder
			.<HarbingerOutpostStructure>mapCodec(instance -> instance.group(HarbingerOutpostStructure.settingsCodec(instance),
					StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
					ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
							.forGetter(structure -> structure.startJigsawName),
					Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
					Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
							.forGetter(structure -> structure.projectStartToHeightmap),
					Codec.intRange(1, 128).fieldOf("max_distance_from_center")
							.forGetter(structure -> structure.maxDistanceFromCenter))
					.apply(instance, HarbingerOutpostStructure::new))
			.codec();

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

	public HarbingerOutpostStructure(Structure.StructureSettings config, Holder<StructureTemplatePool> startPool,
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

		if (!HarbingerOutpostStructure.extraSpawningChecks(context)) {
			return Optional.empty();
		}
		int startY = this.startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

		ChunkPos chunkPos = context.chunkPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

		Optional<Structure.GenerationStub> structurePiecesGenerator = JigsawPlacement.addPieces(context, this.startPool,
				this.startJigsawName, this.size, blockPos, false, this.projectStartToHeightmap,
				this.maxDistanceFromCenter);
		return structurePiecesGenerator;
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.harbinger_outpost.get();
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

		int maxY = (fullBox.minY() + fullBox.maxY()) / 2;

		spawnOnFloor(level, random, EntityInit.harbinger_vicar.get(),
				centerX, centerZ, floorY, maxY, VICAR_SPAWN_SPREAD);

		int halfWidth = (fullBox.maxX() - fullBox.minX()) / 4;
		int halfDepth = (fullBox.maxZ() - fullBox.minZ()) / 4;
		spawnOnFloor(level, random, EntityInit.harbinger_alchemist.get(),
				centerX - halfWidth, centerZ - halfDepth, floorY, maxY, ALCHEMIST_SPAWN_SPREAD);
		spawnOnFloor(level, random, EntityInit.harbinger_alchemist.get(),
				centerX + halfWidth, centerZ + halfDepth, floorY, maxY, ALCHEMIST_SPAWN_SPREAD);
	}

	private <T extends Entity> void spawnOnFloor(WorldGenLevel level, RandomSource random,
			EntityType<T> type, int originX, int originZ, int floorY, int maxY, int spread) {

		for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
			int dx = (attempt < INITIAL_CENTERED_ATTEMPTS) ? 0 : random.nextInt(spread * 2 + 1) - spread;
			int dz = (attempt < INITIAL_CENTERED_ATTEMPTS) ? 0 : random.nextInt(spread * 2 + 1) - spread;
			int startY = floorY + (attempt % INITIAL_CENTERED_ATTEMPTS);

			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(
					originX + dx, startY, originZ + dz);

			for (int y = startY; y <= maxY; y++) {
				mutable.setY(y);
				if (level.getBlockState(mutable).isAir()
						&& level.getBlockState(mutable.below()).isFaceSturdy(level, mutable.below(),
								net.minecraft.core.Direction.UP)) {
					spawnMob(level, type, mutable.immutable());
					return;
				}
			}
		}

		spawnMob(level, type, new BlockPos(originX, floorY + 1, originZ));
	}

	private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos) {
		T entity = type.create(level.getLevel());
		if (entity == null) return;
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				level.getRandom().nextFloat() * 360.0f, 0.0f);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
					MobSpawnType.STRUCTURE, null, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}
}
