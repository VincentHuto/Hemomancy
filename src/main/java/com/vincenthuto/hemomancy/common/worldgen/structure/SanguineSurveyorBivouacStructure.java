package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
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

import java.util.List;
import java.util.Optional;

public class SanguineSurveyorBivouacStructure extends Structure {
	private static final ResourceLocation PLACEHOLDER_ID = Hemomancy.rloc("random/surveyor_log");
	private static final List<ResourceLocation> REPLACEMENTS = List.of(
			Hemomancy.rloc("surveyor_bivouac/log_1"),
			Hemomancy.rloc("surveyor_bivouac/log_2"),
			Hemomancy.rloc("surveyor_bivouac/log_3"),
			Hemomancy.rloc("surveyor_bivouac/log_4"),
			Hemomancy.rloc("surveyor_bivouac/log_5"),
			Hemomancy.rloc("surveyor_bivouac/log_6"));

	public static final MapCodec<SanguineSurveyorBivouacStructure> CODEC = RecordCodecBuilder
			.<SanguineSurveyorBivouacStructure>mapCodec(instance -> instance.group(
							SanguineSurveyorBivouacStructure.settingsCodec(instance),
							StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
							ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
									.forGetter(structure -> structure.startJigsawName),
							Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
							HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
							Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
									.forGetter(structure -> structure.projectStartToHeightmap),
							Codec.intRange(1, 128).fieldOf("max_distance_from_center")
									.forGetter(structure -> structure.maxDistanceFromCenter))
					.apply(instance, SanguineSurveyorBivouacStructure::new));

	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int size;
	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;

	public SanguineSurveyorBivouacStructure(Structure.StructureSettings config, Holder<StructureTemplatePool> startPool,
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
		int startY = this.startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

		ChunkPos chunkPos = context.chunkPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

		return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.size, blockPos, false,
				this.projectStartToHeightmap, this.maxDistanceFromCenter, PoolAliasLookup.EMPTY,
				JigsawStructure.DEFAULT_DIMENSION_PADDING, LiquidSettings.APPLY_WATERLOGGING);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.sanguine_surveyor_bivouac.get();
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

		DiscoveryInscriptionRandomizer.replacePlaceholderIds(level, fullBox, random, PLACEHOLDER_ID, REPLACEMENTS);
	}
}

