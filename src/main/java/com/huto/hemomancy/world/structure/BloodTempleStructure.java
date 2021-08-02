package com.huto.hemomancy.world.structure;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.StructureInit;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class BloodTempleStructure extends StructureFeature<NoneFeatureConfiguration> {
	public BloodTempleStructure(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return BloodTempleStructure.Start::new;
	}

	@Override
	public GenerationStep.Decoration step() {
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	private static final List<MobSpawnSettings.SpawnerData> STRUCTURE_MONSTERS = ImmutableList.of(
			new MobSpawnSettings.SpawnerData(EntityType.ILLUSIONER, 100, 4, 9),
			new MobSpawnSettings.SpawnerData(EntityType.VINDICATOR, 100, 4, 9));

	@Override
	public List<MobSpawnSettings.SpawnerData> getDefaultSpawnList() {
		return STRUCTURE_MONSTERS;
	}

	private static final List<MobSpawnSettings.SpawnerData> STRUCTURE_CREATURES = ImmutableList.of(
			new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 30, 10, 15),
			new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 100, 1, 2));

	@Override
	public List<MobSpawnSettings.SpawnerData> getDefaultCreatureSpawnList() {
		return STRUCTURE_CREATURES;
	}

	public static class Start extends StructureStart<NoneFeatureConfiguration> {
		public Start(StructureFeature<NoneFeatureConfiguration> structureIn, int chunkX, int chunkZ,
				BoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}

		@Override
		public void generatePieces(RegistryAccess dynamicRegistryManager, ChunkGenerator generator,
				StructureManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn,
				NoneFeatureConfiguration config) {
			ChunkPos chunkpos = new ChunkPos(chunkX, chunkZ);
			int i = chunkpos.getMinBlockX() + this.random.nextInt(16);
			int j = chunkpos.getMinBlockZ() + this.random.nextInt(16);
			int k = generator.getSeaLevel();
			int l = k + this.random.nextInt(generator.getGenDepth() / 2 - 2 - k);
			BlockGetter BlockGetter = generator.getBaseColumn(i, j);

			for (BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(i, l, j); l > k; --l) {
				BlockState blockstate = BlockGetter.getBlockState(blockpos$mutable);
				blockpos$mutable.move(Direction.DOWN);
				BlockState blockstate1 = BlockGetter.getBlockState(blockpos$mutable);
				if (blockstate.isAir() && (blockstate1.is(Blocks.SOUL_SAND)
						|| blockstate1.isFaceSturdy(BlockGetter, blockpos$mutable, Direction.UP))) {
					break;
				}
			}
			int x = (chunkX << 4) + 7;
			int z = (chunkZ << 4) + 7;

			// Finds the y value of the terrain at location.
			BlockPos blockpos = new BlockPos(x, l, z);
			Rotation rotation = Rotation.values()[this.random.nextInt(Rotation.values().length)];
			BloodTemplePiece.start(templateManagerIn, blockpos, rotation, this.pieces, this.random);
			this.calculateBoundingBox();
			/*
			 * Hemomancy.LOGGER.log(Level.DEBUG, "Blood Temple at " + (blockpos.getX()) +
			 * " " + blockpos.getY() + " " + (blockpos.getZ()));
			 */
		}

	}

	public static class BloodTemplePiece {
		private static final ResourceLocation LEFT_SIDE = new ResourceLocation(Hemomancy.MOD_ID, "blood_temple");
		private static final Map<ResourceLocation, BlockPos> OFFSET = ImmutableMap.of(LEFT_SIDE, new BlockPos(0, 1, 0));

		public static void start(StructureManager templateManager, BlockPos pos, Rotation rotation,
				List<StructurePiece> pieceList, Random random) {
			int x = pos.getX();
			int z = pos.getZ();
			BlockPos rotationOffSet = new BlockPos(0, 0, 0).rotate(rotation);
			BlockPos blockpos = rotationOffSet.offset(x, pos.getY(), z);
			pieceList.add(new BloodTemplePiece.Piece(templateManager, LEFT_SIDE, blockpos, rotation));

			rotationOffSet = new BlockPos(-10, 0, 0).rotate(rotation);
			blockpos = rotationOffSet.offset(x, pos.getY(), z);
		}

		public static class Piece extends TemplateStructurePiece {
			private ResourceLocation resourceLocation;
			private Rotation rotation;

			public Piece(StructureManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos,
					Rotation rotationIn) {
				super(StructureInit.RDHP, 0);
				this.resourceLocation = resourceLocationIn;
				BlockPos blockpos = BloodTemplePiece.OFFSET.get(resourceLocation);
				this.templatePosition = pos.offset(blockpos.getX(), blockpos.getY(), blockpos.getZ());
				this.rotation = rotationIn;
				this.setupPiece(templateManagerIn);
			}

			public Piece(StructureManager templateManagerIn, CompoundTag tagCompound) {
				super(StructureInit.RDHP, tagCompound);
				this.resourceLocation = new ResourceLocation(tagCompound.getString("Template"));
				this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
				this.setupPiece(templateManagerIn);
			}

			private void setupPiece(StructureManager templateManager) {
				StructureTemplate template = templateManager.getOrCreate(this.resourceLocation);
				StructurePlaceSettings placementsettings = (new StructurePlaceSettings()).setRotation(this.rotation)
						.setMirror(Mirror.NONE);
				this.setup(template, this.templatePosition, placementsettings);
			}

			@Override
			protected void addAdditionalSaveData(CompoundTag tagCompound) {
				super.addAdditionalSaveData(tagCompound);
				tagCompound.putString("Template", this.resourceLocation.toString());
				tagCompound.putString("Rot", this.rotation.name());
			}

			@Override
			protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, Random rand,
					BoundingBox sbb) {
				if ("chest".equals(function)) {
					worldIn.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
					BlockEntity tileentity = worldIn.getBlockEntity(pos);

					// Just another check to make sure everything is going well before we try to set
					// the chest.
					if (tileentity instanceof ChestBlockEntity) {
						// ((ChestBlockEntity) tileentity).setLootTable(<resource_location_to_loottable>,
						// rand.nextLong());
					}
				}
			}
		}
	}
}
