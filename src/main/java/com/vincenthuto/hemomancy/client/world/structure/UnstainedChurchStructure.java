package com.vincenthuto.hemomancy.client.world.structure;

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
import net.minecraft.world.level.StructureManager;

import java.util.Optional;

public class UnstainedChurchStructure extends Structure {

	public static final Codec<UnstainedChurchStructure> CODEC = RecordCodecBuilder
			.<UnstainedChurchStructure>mapCodec(instance -> instance.group(UnstainedChurchStructure.settingsCodec(instance),
					StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
					ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
							.forGetter(structure -> structure.startJigsawName),
					Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
					Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
							.forGetter(structure -> structure.projectStartToHeightmap),
					Codec.intRange(1, 128).fieldOf("max_distance_from_center")
							.forGetter(structure -> structure.maxDistanceFromCenter))
					.apply(instance, UnstainedChurchStructure::new))
			.codec();

	private static boolean extraSpawningChecks(GenerationContext context) {
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

	public UnstainedChurchStructure(StructureSettings config, Holder<StructureTemplatePool> startPool,
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
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {

		if (!UnstainedChurchStructure.extraSpawningChecks(context)) {
			return Optional.empty();
		}
		int startY = this.startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

		ChunkPos chunkPos = context.chunkPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

		Optional<GenerationStub> structurePiecesGenerator = JigsawPlacement.addPieces(context, this.startPool,
				this.startJigsawName, this.size, blockPos, false, this.projectStartToHeightmap,
				this.maxDistanceFromCenter);
		return structurePiecesGenerator;
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.unstained_church.get(); // Helps the game know how to turn this structure back to json to
													// save to chunks
	}

	/**
	 * Called after the structure's pieces have been placed in each overlapping
	 * chunk. Spawns exactly one {@link EntityInit#unstained_zealot}, two
	 * {@link EntityInit#unstained_guardian guardians} and three-to-five
	 * {@link EntityInit#unstained_acolyte acolytes} inside the structure,
	 * mirroring how the Termite Mound guarantees a single queen.
	 */
	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager,
			ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
			ChunkPos chunkPos, PiecesContainer pieces) {

		// Compute the full bounding box of the structure from all pieces
		BoundingBox fullBox = pieces.calculateBoundingBox();
		int centerX = (fullBox.minX() + fullBox.maxX()) / 2;
		int centerZ = (fullBox.minZ() + fullBox.maxZ()) / 2;

		// Use the bottom of the structure as the reference Y — the church
		// floor is near minY, not the vertical centre (which lands on the
		// roof for tall structures with steeples).
		int floorY = fullBox.minY();

		// Only spawn in the chunk that contains the centre to avoid duplicates
		if (!chunkBox.isInside(centerX, floorY, centerZ)) {
			return;
		}

		// Cap scan height to the lower half to stay inside the church interior
		int maxY = (fullBox.minY() + fullBox.maxY()) / 2;

		// --- 1 Zealot (the priest) near the centre ---
		spawnOnFloor(level, random, EntityInit.unstained_zealot.get(),
				centerX, centerZ, floorY, maxY, 3);

		// --- 2 Guardians (bouncers) near the edges of the structure ---
		int halfWidth = (fullBox.maxX() - fullBox.minX()) / 4;
		int halfDepth = (fullBox.maxZ() - fullBox.minZ()) / 4;
		spawnOnFloor(level, random, EntityInit.unstained_guardian.get(),
				centerX - halfWidth, centerZ - halfDepth, floorY, maxY, 2);
		spawnOnFloor(level, random, EntityInit.unstained_guardian.get(),
				centerX + halfWidth, centerZ + halfDepth, floorY, maxY, 2);

		// --- 3-5 Acolytes (parishioners) scattered in the interior ---
		int acolyteCount = 3 + random.nextInt(3);
		for (int i = 0; i < acolyteCount; i++) {
			int ax = centerX + random.nextInt(7) - 3;
			int az = centerZ + random.nextInt(7) - 3;
			spawnOnFloor(level, random, EntityInit.unstained_acolyte.get(),
					ax, az, floorY, maxY, 4);
		}
	}

	/**
	 * Attempts to spawn a single mob on a solid floor block inside the
	 * structure by scanning upward from {@code floorY}.
	 *
	 * @param originX  preferred X position
	 * @param originZ  preferred Z position
	 * @param floorY   lowest Y of the structure (start of scan)
	 * @param maxY     highest Y to scan (keeps mob below the roof)
	 * @param spread   random XZ offset range for retries
	 */
	private <T extends Entity> void spawnOnFloor(WorldGenLevel level, RandomSource random,
			EntityType<T> type, int originX, int originZ, int floorY, int maxY, int spread) {

		for (int attempt = 0; attempt < 32; attempt++) {
			int dx = (attempt < 4) ? 0 : random.nextInt(spread * 2 + 1) - spread;
			int dz = (attempt < 4) ? 0 : random.nextInt(spread * 2 + 1) - spread;
			int startY = floorY + (attempt % 4);

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

		// Last resort: force-spawn just above the floor
		spawnMob(level, type, new BlockPos(originX, floorY + 1, originZ));
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
					MobSpawnType.STRUCTURE, null, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}
}