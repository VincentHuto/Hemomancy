package com.vincenthuto.hemomancy.common.world;

import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** "In the Gardens": the Fungal Gardens dimension, or the Overworld biome of the same name. */
public final class FungalGardensPlaces {
	private FungalGardensPlaces() {}

	public static boolean inDimension(Level level) {
		return level.dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS);
	}

	public static boolean inOverworldBiome(Level level, BlockPos pos) {
		return level.getBiome(pos).is(BiomeInit.FUNGAL_GARDENS);
	}

	public static boolean isInGardens(Level level, BlockPos pos) {
		return inDimension(level) || inOverworldBiome(level, pos);
	}
}
