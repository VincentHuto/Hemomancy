package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ResonantForgeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.WarpChairBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.WarpChairFillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
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
import net.minecraft.world.phys.AABB;

import java.util.Optional;

/** Courtyard, paired wall segments, gate, and four corner homes assembled by jigsaw pools. */
public class HarbingerOutpostStructure extends Structure {
	public static final MapCodec<HarbingerOutpostStructure> CODEC = RecordCodecBuilder
			.<HarbingerOutpostStructure>mapCodec(instance -> instance.group(settingsCodec(instance),
					StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
					ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
					Codec.intRange(0, 30).fieldOf("size").forGetter(s -> s.size),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
					Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
							.forGetter(s -> s.projectStartToHeightmap),
					Codec.intRange(1, 128).fieldOf("max_distance_from_center")
							.forGetter(s -> s.maxDistanceFromCenter))
				.apply(instance, HarbingerOutpostStructure::new));

	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int size;
	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;

	public HarbingerOutpostStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
			Optional<ResourceLocation> startJigsawName, int size, HeightProvider startHeight,
			Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter) {
		super(settings);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.size = size;
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		var surface = StructurePlacementChecks.harbingerOutpostSurface(context);
		if (surface.isEmpty()) return Optional.empty();
		int offset = startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
		ChunkPos chunk = context.chunkPos();
		// The anchor is under the plaza at local (15, 0, 15); the floor is at local y = 2.
		BlockPos anchor = new BlockPos(chunk.getMiddleBlockX(), surface.getAsInt() - 2 + offset,
				chunk.getMiddleBlockZ());
		return JigsawPlacement.addPieces(context, startPool, startJigsawName, size, anchor, false,
				Optional.empty(), maxDistanceFromCenter, PoolAliasLookup.EMPTY,
				JigsawStructure.DEFAULT_DIMENSION_PADDING, LiquidSettings.APPLY_WATERLOGGING);
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
		int minX = Math.max(fullBox.minX(), chunkBox.minX());
		int maxX = Math.min(fullBox.maxX(), chunkBox.maxX());
		int minZ = Math.max(fullBox.minZ(), chunkBox.minZ());
		int maxZ = Math.min(fullBox.maxZ(), chunkBox.maxZ());
		String outpostKey = HarbingerRecruitmentRules.createOutpostKey(
				level.getLevel().dimension().location(), fullBox);

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				BlockPos base = new BlockPos(x, fullBox.minY(), z);
				if (!level.getBlockState(base).is(Blocks.BLACKSTONE)) continue;
				for (int y = base.getY() - 1; y >= base.getY() - 8; y--) {
					BlockPos support = new BlockPos(x, y, z);
					if (!level.getBlockState(support).canBeReplaced()) break;
					level.setBlock(support, Blocks.BLACKSTONE.defaultBlockState(), 2);
				}
			}
		}

		// Visit controllers one block beyond this chunk to link authored fillers.
		// Only place a missing filler inside the current chunk: a later template
		// pass across the border could otherwise remove it and destroy the controller.
		for (int x = minX - 1; x <= maxX + 1; x++) {
			for (int z = minZ - 1; z <= maxZ + 1; z++) {
				for (int y = fullBox.minY() + 3; y <= Math.min(fullBox.maxY(), fullBox.minY() + 7); y++) {
					BlockPos pos = new BlockPos(x, y, z);
					var state = level.getBlockState(pos);
					boolean ownsController = chunkBox.isInside(x, y, z);
					if (ownsController && state.is(Blocks.LIGHT)) {
						EntityType<?> resident = switch (state.getValue(LightBlock.LEVEL)) {
							case 11 -> EntityInit.harbinger_vicar.get();
							case 12 -> EntityInit.harbinger_artificer.get();
							case 13 -> EntityInit.harbinger_alchemist.get();
							case 14 -> EntityInit.harbinger_mnemonist.get();
							default -> null;
						};
						if (resident != null) {
							level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
							spawnMob(level, resident, pos, outpostKey);
						}
					} else if (state.is(BlockInit.warp_chair.get())) {
						BlockPos above = pos.above();
						if (level.getBlockState(above).is(BlockInit.warp_chair_filler.get())) {
							bindFiller(level, above, pos);
						} else if (chunkBox.isInside(above)) {
							level.setBlock(above, BlockInit.warp_chair_filler.get().defaultBlockState()
									.setValue(WarpChairFillerBlock.FACING, state.getValue(WarpChairBlock.FACING))
									.setValue(WarpChairFillerBlock.WATERLOGGED, level.getFluidState(above).is(FluidTags.WATER)), 2);
							bindFiller(level, above, pos);
						}
					} else if (state.is(BlockInit.resonant_forge.get())) {
						Direction right = state.getValue(ResonantForgeBlock.FACING).getClockWise();
						for (Direction side : new Direction[] {right, right.getOpposite()}) {
							placeFiller(level, chunkBox, pos.relative(side), pos);
							placeFiller(level, chunkBox, pos.relative(side).above(), pos);
						}
					} else if (state.is(BlockInit.somatic_loom.get())) {
						Direction facing = state.getValue(SomaticLoomBlock.FACING);
						placeFiller(level, chunkBox, pos.above(), pos);
						placeFiller(level, chunkBox, pos.relative(facing), pos);
						placeFiller(level, chunkBox, pos.relative(facing.getOpposite()), pos);
					}
				}
			}
		}
	}

	private static void placeFiller(WorldGenLevel level, BoundingBox chunkBox, BlockPos fillerPos, BlockPos mainPos) {
		if (level.getBlockState(fillerPos).is(BlockInit.filler_block.get())) {
			bindFiller(level, fillerPos, mainPos);
			return;
		}
		if (!chunkBox.isInside(fillerPos)) return;
		level.setBlock(fillerPos, BlockInit.filler_block.get().defaultBlockState()
				.setValue(FillerBlock.WATERLOGGED, level.getFluidState(fillerPos).is(FluidTags.WATER)), 2);
		bindFiller(level, fillerPos, mainPos);
	}

	private static void bindFiller(WorldGenLevel level, BlockPos fillerPos, BlockPos mainPos) {
		if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) filler.setMainBlockPos(mainPos);
	}

	private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos,
			String outpostKey) {
		if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
				|| !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return;
		if (!level.getLevel().getEntitiesOfClass(Entity.class, new AABB(pos).inflate(7),
				entity -> entity.getType() == type).isEmpty()) return;
		T entity = type.create(level.getLevel());
		if (entity == null) return;
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				level.getRandom().nextFloat() * 360.0f, 0.0f);
		HarbingerRecruitmentRules.markOutpostOrigin(entity, outpostKey);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		if (entity instanceof HarbingerVicarEntity vicar) vicar.setOutpostHome(pos);
		level.addFreshEntityWithPassengers(entity);
	}
}
