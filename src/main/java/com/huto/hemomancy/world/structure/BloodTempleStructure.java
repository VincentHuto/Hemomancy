package com.huto.hemomancy.world.structure;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.StructureInit;
import com.mojang.serialization.Codec;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
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

public class BloodTempleStructure extends StructureFeature<NoneFeatureConfiguration> {
	public BloodTempleStructure(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return BloodTempleStructure.Start::new;
	}

	@Override
	public GenerationStep.Decoration getDecorationStage() {
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
		public void func_230364_a_(RegistryAccess dynamicRegistryManager, ChunkGenerator generator,
				StructureManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoneFeatureConfiguration config) {
			ChunkPos chunkpos = new ChunkPos(chunkX, chunkZ);
			int i = chunkpos.getXStart() + this.rand.nextInt(16);
			int j = chunkpos.getZStart() + this.rand.nextInt(16);
			int k = generator.getSeaLevel();
			int l = k + this.rand.nextInt(generator.getMaxBuildHeight() / 2 - 2 - k);
			IBlockReader iblockreader = generator.func_230348_a_(i, j);

			for (BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(i, l, j); l > k; --l) {
				BlockState blockstate = iblockreader.getBlockState(blockpos$mutable);
				blockpos$mutable.move(Direction.DOWN);
				BlockState blockstate1 = iblockreader.getBlockState(blockpos$mutable);
				if (blockstate.isAir() && (blockstate1.matchesBlock(Blocks.SOUL_SAND)
						|| blockstate1.isSolidSide(iblockreader, blockpos$mutable, Direction.UP))) {
					break;
				}
			}
			int x = (chunkX << 4) + 7;
			int z = (chunkZ << 4) + 7;

			// Finds the y value of the terrain at location.
			BlockPos blockpos = new BlockPos(x, l, z);
			Rotation rotation = Rotation.values()[this.rand.nextInt(Rotation.values().length)];
			BloodTemplePiece.start(templateManagerIn, blockpos, rotation, this.components, this.rand);
			this.recalculateStructureSize();
			/*
			 * Hemomancy.LOGGER.log(Level.DEBUG, "Blood Temple at " + (blockpos.getX()) +
			 * " " + blockpos.getY() + " " + (blockpos.getZ()));
			 */
		}

	}

	public static class BloodTemplePiece {
		private static final ResourceLocation LEFT_SIDE = new ResourceLocation(Hemomancy.MOD_ID, "blood_temple");
		private static final Map<ResourceLocation, BlockPos> OFFSET = ImmutableMap.of(LEFT_SIDE, new BlockPos(0, 1, 0));

		public static void start(TemplateManager templateManager, BlockPos pos, Rotation rotation,
				List<StructurePiece> pieceList, Random random) {
			int x = pos.getX();
			int z = pos.getZ();
			BlockPos rotationOffSet = new BlockPos(0, 0, 0).rotate(rotation);
			BlockPos blockpos = rotationOffSet.add(x, pos.getY(), z);
			pieceList.add(new BloodTemplePiece.Piece(templateManager, LEFT_SIDE, blockpos, rotation));

			rotationOffSet = new BlockPos(-10, 0, 0).rotate(rotation);
			blockpos = rotationOffSet.add(x, pos.getY(), z);
		}

		public static class Piece extends TemplateStructurePiece {
			private ResourceLocation resourceLocation;
			private Rotation rotation;

			public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos,
					Rotation rotationIn) {
				super(StructureInit.RDHP, 0);
				this.resourceLocation = resourceLocationIn;
				BlockPos blockpos = BloodTemplePiece.OFFSET.get(resourceLocation);
				this.templatePosition = pos.add(blockpos.getX(), blockpos.getY(), blockpos.getZ());
				this.rotation = rotationIn;
				this.setupPiece(templateManagerIn);
			}

			public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
				super(StructureInit.RDHP, tagCompound);
				this.resourceLocation = new ResourceLocation(tagCompound.getString("Template"));
				this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
				this.setupPiece(templateManagerIn);
			}

			private void setupPiece(TemplateManager templateManager) {
				Template template = templateManager.getTemplateDefaulted(this.resourceLocation);
				PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation)
						.setMirror(Mirror.NONE);
				this.setup(template, this.templatePosition, placementsettings);
			}

			@Override
			protected void readAdditional(CompoundNBT tagCompound) {
				super.readAdditional(tagCompound);
				tagCompound.putString("Template", this.resourceLocation.toString());
				tagCompound.putString("Rot", this.rotation.name());
			}

			@Override
			protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand,
					MutableBoundingBox sbb) {
				if ("chest".equals(function)) {
					worldIn.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
					TileEntity tileentity = worldIn.getTileEntity(pos);

					// Just another check to make sure everything is going well before we try to set
					// the chest.
					if (tileentity instanceof ChestTileEntity) {
						// ((ChestTileEntity) tileentity).setLootTable(<resource_location_to_loottable>,
						// rand.nextLong());
					}
				}
			}
		}
	}
}
