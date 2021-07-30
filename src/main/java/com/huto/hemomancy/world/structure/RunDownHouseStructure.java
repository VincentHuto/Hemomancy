package com.huto.hemomancy.world.structure;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableList;
import com.huto.hemomancy.Hemomancy;
import com.mojang.serialization.Codec;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import net.minecraft.world.level.levelgen.feature.StructureFeature.StructureStartFactory;

public class RunDownHouseStructure extends StructureFeature<NoneFeatureConfiguration> {
	public RunDownHouseStructure(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	/**
	 * This is how the worldgen code knows what to call when it is time to create
	 * the pieces of the structure for generation.
	 */
	@Override
	public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return RunDownHouseStructure.Start::new;
	}

	/**
	 * Generation stage for when to generate the structure. there are 10 stages you
	 * can pick from! This surface structure stage places the structure before
	 * plants and ores are generated.
	 */
	@Override
	public GenerationStep.Decoration getDecorationStage() {
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	/**
	 * || ONLY WORKS IN FORGE 34.1.12+ ||
	 *
	 * This method allows us to have mobs that spawn naturally over time in our
	 * structure. No other mobs will spawn in the structure of the same entity
	 * classification. The reason you want to match the classifications is so that
	 * your structure's mob will contribute to that classification's cap. Otherwise,
	 * it may cause a runaway spawning of the mob that will never stop.
	 *
	 * NOTE: getDefaultSpawnList is for monsters only and
	 * getDefaultCreatureSpawnList is for creatures only. If you want to add
	 * entities of another classification, use the StructureSpawnListGatherEvent to
	 * add water_creatures, water_ambient, ambient, or misc mobs. Use that event to
	 * add/remove mobs from structures that are not your own.
	 */
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

	/**
	 * This is where extra checks can be done to determine if the structure can
	 * spawn here. This only needs to be overridden if you're adding additional
	 * spawn conditions.
	 * 
	 * Notice how the biome is also passed in. Though, you are not going to do any
	 * biome checking here as you should've added this structure to the biomes you
	 * wanted already with the biome load event.
	 * 
	 * Basically, this method is used for determining if the land is at a suitable
	 * height, if certain other structures are too close or not, or some other
	 * restrictive condition.
	 *
	 * For example, Pillager Outposts added a check to make sure it cannot spawn
	 * within 10 chunk of a Village. (Bedrock Edition seems to not have the same
	 * check)
	 * 
	 * 
	 * Also, please for the love of god, do not do dimension checking here. If you
	 * do and another mod's dimension is trying to spawn your structure, the locate
	 * command will make minecraft hang forever and break the game.
	 *
	 * Instead, use the WorldEvent.Load event in StructureTutorialMain class. If you
	 * check for the dimension there and do not add your structure's spacing into
	 * the chunk generator, the structure will not spawn in that dimension!
	 */
//    @Override
//    protected boolean func_230363_a_(ChunkGenerator chunkGenerator, BiomeProvider biomeSource, long seed, SharedSeedRandom chunkRandom, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos, NoFeatureConfig featureConfig) {
//        int landHeight = chunkGenerator.getNoiseHeight(chunkX << 4, chunkZ << 4, Heightmap.Type.WORLD_SURFACE_WG);
//        return landHeight > 100;
//    }

	/**
	 * Handles calling up the structure's pieces class and height that structure
	 * will spawn at.
	 */
	public static class Start extends StructureStart<NoneFeatureConfiguration> {
		public Start(StructureFeature<NoneFeatureConfiguration> structureIn, int chunkX, int chunkZ,
				BoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}

		@Override
		public void func_230364_a_(RegistryAccess dynamicRegistryManager, ChunkGenerator generator,
				StructureManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoneFeatureConfiguration config) {
			// Check out vanilla's WoodlandMansionStructure for how they offset the x and z
			// so that they get the y value of the land at the mansion's entrance, no matter
			// which direction the mansion is rotated.
			//
			// However, for most purposes, getting the y value of land with the default x
			// and z is good enough.
			Rotation rotation = Rotation.values()[this.rand.nextInt(Rotation.values().length)];

			// Turns the chunk coordinates into actual coordinates we can use. (Gets center
			// of that chunk)
			int x = (chunkX << 4) + 7;
			int z = (chunkZ << 4) + 7;

			// Finds the y value of the terrain at location.
			int surfaceY = generator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
			BlockPos blockpos = new BlockPos(x, surfaceY, z);

			// Now adds the structure pieces to this.components with all details such as
			// where each part goes
			// so that the structure can be added to the world by worldgen.
			RunDownHousePieces.start(templateManagerIn, blockpos, rotation, this.components, this.rand);

			// Sets the bounds of the structure.
			this.recalculateStructureSize();

			// I use to debug and quickly find out if the structure is spawning or not and
			// where it is.
			Hemomancy.LOGGER.log(Level.DEBUG,
					"Rundown House at " + (blockpos.getX()) + " " + blockpos.getY() + " " + (blockpos.getZ()));
		}

	}
}