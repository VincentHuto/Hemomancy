package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.TerrablenderOverworldBiomeBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

public class ErythrocoralReefRegion extends Region {
	static final String ERYTHROCORAL_REEF_PATH = BiomeInit.ERYTHROCORAL_REEF.location().toString();
	static final String DEFERRED_PLACEHOLDER_PATH = Region.DEFERRED_PLACEHOLDER.location().toString();

	private static final int TEMPERATURE_SLOTS = 5;
	private static final int HUMIDITY_SLOTS = 5;

	private final AccessibleOverworldBiomeBuilder overworldBiomeBuilder;

	public ErythrocoralReefRegion(ResourceLocation name, int weight) {
		super(name, RegionType.OVERWORLD, weight);
		this.overworldBiomeBuilder = new AccessibleOverworldBiomeBuilder(
				oceanBiomes(),
				middleBiomes(),
				middleBiomeVariants(),
				plateauBiomes(),
				plateauBiomeVariants(),
				shatteredBiomes(),
				beachBiomes(),
				peakBiomes(),
				peakBiomeVariants(),
				slopeBiomes(),
				slopeBiomeVariants());
	}

	@Override
	public void addBiomes(Registry<Biome> registry,
			Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
		this.overworldBiomeBuilder.addBiomesTo(mapper);
	}

	@SuppressWarnings("unchecked")
	static ResourceKey<Biome>[][] oceanBiomes() {
		ResourceKey<Biome> deferred = Region.DEFERRED_PLACEHOLDER;
		return new ResourceKey[][] {
				{ deferred, deferred, deferred, BiomeInit.ERYTHROCORAL_REEF, deferred },
				{ deferred, deferred, deferred, deferred, deferred }
		};
	}

	static String[][] oceanBiomePaths() {
		return toLocationPaths(oceanBiomes());
	}

	static ResourceKey<Biome>[][] middleBiomes() {
		return deferredLandTable();
	}

	static String[][] middleBiomePaths() {
		return toLocationPaths(middleBiomes());
	}

	static ResourceKey<Biome>[][] middleBiomeVariants() {
		return deferredLandTable();
	}

	static String[][] middleBiomeVariantPaths() {
		return toLocationPaths(middleBiomeVariants());
	}

	static ResourceKey<Biome>[][] plateauBiomes() {
		return deferredLandTable();
	}

	static String[][] plateauBiomePaths() {
		return toLocationPaths(plateauBiomes());
	}

	static ResourceKey<Biome>[][] plateauBiomeVariants() {
		return deferredLandTable();
	}

	static String[][] plateauBiomeVariantPaths() {
		return toLocationPaths(plateauBiomeVariants());
	}

	static ResourceKey<Biome>[][] shatteredBiomes() {
		return deferredLandTable();
	}

	static String[][] shatteredBiomePaths() {
		return toLocationPaths(shatteredBiomes());
	}

	static ResourceKey<Biome>[][] beachBiomes() {
		return deferredLandTable();
	}

	static String[][] beachBiomePaths() {
		return toLocationPaths(beachBiomes());
	}

	static ResourceKey<Biome>[][] peakBiomes() {
		return deferredLandTable();
	}

	static String[][] peakBiomePaths() {
		return toLocationPaths(peakBiomes());
	}

	static ResourceKey<Biome>[][] peakBiomeVariants() {
		return deferredLandTable();
	}

	static String[][] peakBiomeVariantPaths() {
		return toLocationPaths(peakBiomeVariants());
	}

	static ResourceKey<Biome>[][] slopeBiomes() {
		return deferredLandTable();
	}

	static String[][] slopeBiomePaths() {
		return toLocationPaths(slopeBiomes());
	}

	static ResourceKey<Biome>[][] slopeBiomeVariants() {
		return deferredLandTable();
	}

	static String[][] slopeBiomeVariantPaths() {
		return toLocationPaths(slopeBiomeVariants());
	}

	@SuppressWarnings("unchecked")
	private static ResourceKey<Biome>[][] deferredLandTable() {
		ResourceKey<Biome>[][] table = new ResourceKey[TEMPERATURE_SLOTS][HUMIDITY_SLOTS];
		for (ResourceKey<Biome>[] row : table) {
			Arrays.fill(row, Region.DEFERRED_PLACEHOLDER);
		}
		return table;
	}

	private static String[][] toLocationPaths(ResourceKey<Biome>[][] table) {
		String[][] paths = new String[table.length][];
		for (int row = 0; row < table.length; row++) {
			paths[row] = new String[table[row].length];
			for (int column = 0; column < table[row].length; column++) {
				paths[row][column] = table[row][column].location().toString();
			}
		}
		return paths;
	}

	private static final class AccessibleOverworldBiomeBuilder extends TerrablenderOverworldBiomeBuilder {
		AccessibleOverworldBiomeBuilder(ResourceKey<Biome>[][] oceans, ResourceKey<Biome>[][] middleBiomes,
				ResourceKey<Biome>[][] middleBiomesVariant, ResourceKey<Biome>[][] plateauBiomes,
				ResourceKey<Biome>[][] plateauBiomesVariant, ResourceKey<Biome>[][] shatteredBiomes,
				ResourceKey<Biome>[][] beachBiomes, ResourceKey<Biome>[][] peakBiomes,
				ResourceKey<Biome>[][] peakBiomesVariant, ResourceKey<Biome>[][] slopeBiomes,
				ResourceKey<Biome>[][] slopeBiomesVariant) {
			super(oceans, middleBiomes, middleBiomesVariant, plateauBiomes, plateauBiomesVariant, shatteredBiomes,
					beachBiomes, peakBiomes, peakBiomesVariant, slopeBiomes, slopeBiomesVariant);
		}

		void addBiomesTo(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
			super.addBiomes(mapper);
		}
	}
}
