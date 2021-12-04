//package com.vincenthuto.hemomancy.world.structure;
//
//import java.util.List;
//import java.util.Random;
//
//import com.google.common.collect.ImmutableList;
//import com.mojang.serialization.Codec;
//import com.vincenthuto.hemomancy.Hemomancy;
//import com.vincenthuto.hemomancy.init.StructureInit;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.EmptyBlockGetter;
//import net.minecraft.world.level.LevelHeightAccessor;
//import net.minecraft.world.level.NoiseColumn;
//import net.minecraft.world.level.ServerLevelAccessor;
//import net.minecraft.world.level.StructureFeatureManager;
//import net.minecraft.world.level.WorldGenLevel;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.biome.BiomeSource;
//import net.minecraft.world.level.biome.MobSpawnSettings;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.Mirror;
//import net.minecraft.world.level.block.Rotation;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.chunk.ChunkGenerator;
//import net.minecraft.world.level.levelgen.GenerationStep;
//import net.minecraft.world.level.levelgen.Heightmap;
//import net.minecraft.world.level.levelgen.WorldgenRandom;
//import net.minecraft.world.level.levelgen.feature.StructureFeature;
//import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
//import net.minecraft.world.level.levelgen.structure.BoundingBox;
//import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
//import net.minecraft.world.level.levelgen.structure.StructurePiece;
//import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
//import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
//
//public class BloodTempleStructure extends StructureFeature<NoneFeatureConfiguration> {
//	public BloodTempleStructure(Codec<NoneFeatureConfiguration> codec) {
//		super(codec);
//	}
//
//	@Override
//	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
//		return BloodTempleStructure.Start::new;
//	}
//
//	@Override
//	public GenerationStep.Decoration step() {
//		return GenerationStep.Decoration.SURFACE_STRUCTURES;
//	}
//
//	private static final List<MobSpawnSettings.SpawnerData> STRUCTURE_MONSTERS = ImmutableList.of();
//
//	@Override
//	public List<MobSpawnSettings.SpawnerData> getDefaultSpawnList() {
//		return STRUCTURE_MONSTERS;
//	}
//
//	private static final List<MobSpawnSettings.SpawnerData> STRUCTURE_CREATURES = ImmutableList.of();
//
//	@Override
//	public List<MobSpawnSettings.SpawnerData> getDefaultCreatureSpawnList() {
//		return STRUCTURE_CREATURES;
//	}
//
//	@Override
//	protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed,
//			WorldgenRandom random, ChunkPos chunkPos1, Biome biome, ChunkPos chunkPos2,
//			NoneFeatureConfiguration featureConfig, LevelHeightAccessor heightLimitView) {
//		BlockPos blockPos = chunkPos1.getWorldPosition();
//
//		int landHeight = chunkGenerator.getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(),
//				Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);
//
//		NoiseColumn columnOfBlocks = chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ(), heightLimitView);
//		BlockState topBlock = columnOfBlocks.getBlockState(blockPos.above(landHeight));
//
//		return topBlock.getFluidState().isEmpty();
//	}
//
//	public static class Start extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
//		public Start(StructureFeature<NoneFeatureConfiguration> structureIn, ChunkPos chunkPos, int referenceIn,
//				long seedIn) {
//			super(structureIn, chunkPos, referenceIn, seedIn);
//		}
//
//		@Override
//		public void generatePieces(RegistryAccess dynamicRegistryAccess, ChunkGenerator chunkGenerator,
//				StructureManager structureManager, ChunkPos chunkPos, Biome biomeIn, NoneFeatureConfiguration config,
//				LevelHeightAccessor heightLimitView) {
//			int i = chunkPos.getMinBlockX() + this.random.nextInt(16);
//			int j = chunkPos.getMinBlockZ() + this.random.nextInt(16);
//			int k = chunkGenerator.getSeaLevel();
//			int l = k + this.random.nextInt(chunkGenerator.getGenDepth() / 2 - 2 - k);
//			NoiseColumn noisecolumn = chunkGenerator.getBaseColumn(i, j, heightLimitView);
//
//			for (BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(i, l,
//					j); l > k; --l) {
//				BlockState blockstate = noisecolumn.getBlockState(blockpos$mutableblockpos);
//				blockpos$mutableblockpos.move(Direction.DOWN);
//				BlockState blockstate1 = noisecolumn.getBlockState(blockpos$mutableblockpos);
//				if (blockstate.isAir() && (blockstate1.is(Blocks.SOUL_SAND) || blockstate1
//						.isFaceSturdy(EmptyBlockGetter.INSTANCE, blockpos$mutableblockpos, Direction.UP))) {
//					break;
//				}
//			}
//			Rotation rotation = Rotation.values()[this.random.nextInt(Rotation.values().length)];
//			if (l > k) {
//				BloodTemplePiece.start(structureManager, new BlockPos(i, l, j), rotation, this.pieces, random);
//				this.getBoundingBox();
//
//			}
//
//		}
//
//	}
//
//	public static class BloodTemplePiece {
//		private static final ResourceLocation LEFT_SIDE = new ResourceLocation(Hemomancy.MOD_ID, "blood_temple");
//
//		public static void start(StructureManager templateManager, BlockPos pos, Rotation rotation,
//				List<StructurePiece> pieceList, Random random) {
//			int x = pos.getX();
//			int z = pos.getZ();
//			BlockPos rotationOffSet = new BlockPos(0, 0, 0).rotate(rotation);
//			BlockPos blockpos = rotationOffSet.offset(x, pos.getY(), z);
//			pieceList.add(new BloodTemplePiece.Piece(templateManager, LEFT_SIDE, blockpos, rotation));
//			rotationOffSet = new BlockPos(-10, 0, 0).rotate(rotation);
//			blockpos = rotationOffSet.offset(x, pos.getY(), z);
//		}
//
//		public static class Piece extends TemplateStructurePiece {
//			public Piece(StructureManager p_72069_, ResourceLocation p_72070_, BlockPos p_72071_, Rotation p_72072_) {
//				super(StructureInit.BLOODTEMPLEPIECE, 0, p_72069_, p_72070_, p_72070_.toString(),
//						makeSettings(p_72072_), p_72071_);
//			}
//
//			public Piece(ServerLevel p_162971_, CompoundTag p_162972_) {
//				super(StructureInit.BLOODTEMPLEPIECE, p_162972_, p_162971_, (p_162980_) -> {
//					return makeSettings(Rotation.valueOf(p_162972_.getString("Rot")));
//				});
//			}
//
//			private static StructurePlaceSettings makeSettings(Rotation p_162977_) {
//				return (new StructurePlaceSettings()).setRotation(p_162977_).setMirror(Mirror.NONE)
//						.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
//			}
//
//			@Override
//			protected void addAdditionalSaveData(ServerLevel p_162974_, CompoundTag p_162975_) {
//				super.addAdditionalSaveData(p_162974_, p_162975_);
//				p_162975_.putString("Rot", this.placeSettings.getRotation().name());
//			}
//
//			@Override
//			protected void handleDataMarker(String p_72084_, BlockPos p_72085_, ServerLevelAccessor p_72086_,
//					Random p_72087_, BoundingBox p_72088_) {
//			}
//
//			@Override
//			public boolean postProcess(WorldGenLevel p_72074_, StructureFeatureManager p_72075_,
//					ChunkGenerator p_72076_, Random p_72077_, BoundingBox p_72078_, ChunkPos p_72079_,
//					BlockPos p_72080_) {
//				p_72078_.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
//				return super.postProcess(p_72074_, p_72075_, p_72076_, p_72077_, p_72078_, p_72079_, p_72080_);
//			}
//		}
//	}
//}