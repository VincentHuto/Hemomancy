package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
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
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;

public class HarbingerVoyagerWreckStructure extends Structure {
	private static final int MAX_PLACEMENT_ATTEMPTS = 36;
	private static final ResourceKey<LootTable> COMMON_LOOT = loot("chests/harbinger_voyager_wreck_common");
	private static final ResourceKey<LootTable> RESEARCH_LOOT = loot("chests/harbinger_voyager_wreck_research");
	private static final ResourceKey<LootTable> LORE_LOOT = loot("chests/harbinger_voyager_wreck_lore");

	public static final MapCodec<HarbingerVoyagerWreckStructure> CODEC = RecordCodecBuilder
			.<HarbingerVoyagerWreckStructure>mapCodec(instance -> instance.group(
							HarbingerVoyagerWreckStructure.settingsCodec(instance),
							StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
							ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
									.forGetter(structure -> structure.startJigsawName),
							Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
							HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
							Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
									.forGetter(structure -> structure.projectStartToHeightmap),
							Codec.intRange(1, 128).fieldOf("max_distance_from_center")
									.forGetter(structure -> structure.maxDistanceFromCenter))
					.apply(instance, HarbingerVoyagerWreckStructure::new));

	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int size;
	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;

	public HarbingerVoyagerWreckStructure(Structure.StructureSettings config, Holder<StructureTemplatePool> startPool,
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
		if (!StructurePlacementChecks.isSuitableOceanWreckChunk(context)) {
			return Optional.empty();
		}
		int startY = this.startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
		ChunkPos chunkPos = context.chunkPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

		return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.size, blockPos,
				false, this.projectStartToHeightmap, this.maxDistanceFromCenter, PoolAliasLookup.EMPTY,
				JigsawStructure.DEFAULT_DIMENSION_PADDING, LiquidSettings.APPLY_WATERLOGGING);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.harbinger_voyager_wreck.get();
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

		BlockPos center = new BlockPos(centerX, centerY, centerZ);
		DiscoveryInscriptionPlacement.placeOnInteriorWall(level, fullBox, center.offset(-3, 1, 2),
				BlockInit.blood_echo_inscription.get(), Hemomancy.rloc("harbinger_voyager_wreck/red_current_chart"));
		DiscoveryInscriptionPlacement.placeOnInteriorFloor(level, fullBox, center.offset(2, -1, -2),
				BlockInit.rite_fragment_inscription.get(), Hemomancy.rloc("harbinger_voyager_wreck/last_covenant_watch"));
		DiscoveryInscriptionPlacement.placeOnInteriorWall(level, fullBox, center.offset(4, 1, -1),
				BlockInit.blood_echo_inscription.get(), Hemomancy.rloc("harbinger_voyager_wreck/vent_survey_fragment"));

		placeLootBarrel(level, fullBox, center.offset(-3, 0, -2), COMMON_LOOT, random);
		placeLootBarrel(level, fullBox, center.offset(2, 0, 2), RESEARCH_LOOT, random);
		placeLootBarrel(level, fullBox, center.offset(0, 1, -4), LORE_LOOT, random);

		int votaries = 1 + random.nextInt(3);
		for (int i = 0; i < votaries; i++) {
			spawnAtSubmergedFloor(level, fullBox, random, EntityInit.brined_votary.get(), center,
					3 + i * 2);
		}
	}

	private static void placeLootBarrel(WorldGenLevel level, BoundingBox fullBox, BlockPos origin,
			ResourceKey<LootTable> lootTable, RandomSource random) {
		BlockPos pos = findSubmergedFloor(level, fullBox, origin, 6);
		if (pos == null) {
			return;
		}
		BlockState state = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
		if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
			container.setLootTable(lootTable);
			container.setLootTableSeed(random.nextLong());
		}
	}

	private static <T extends Entity> void spawnAtSubmergedFloor(WorldGenLevel level, BoundingBox fullBox,
			RandomSource random, EntityType<T> type, BlockPos origin, int spread) {
		for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
			BlockPos probe = origin.offset(random.nextInt(spread * 2 + 1) - spread, 0,
					random.nextInt(spread * 2 + 1) - spread);
			BlockPos pos = findSubmergedFloor(level, fullBox, probe, 5);
			if (pos != null) {
				spawnMob(level, type, pos);
				return;
			}
		}
	}

	private static BlockPos findSubmergedFloor(WorldGenLevel level, BoundingBox fullBox, BlockPos origin, int radius) {
		for (int dy = radius; dy >= -radius; dy--) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					BlockPos pos = origin.offset(dx, dy, dz);
					if (!fullBox.isInside(pos.getX(), pos.getY(), pos.getZ())) {
						continue;
					}
					if (!level.getFluidState(pos).is(FluidTags.WATER)) {
						continue;
					}
					BlockPos below = pos.below();
					if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
						return pos;
					}
				}
			}
		}
		return null;
	}

	private static <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos) {
		T entity = type.create(level.getLevel());
		if (entity == null) {
			return;
		}
		entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
				level.getRandom().nextFloat() * 360.0F, 0.0F);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}

	private static ResourceKey<LootTable> loot(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, Hemomancy.rloc(path));
	}
}
