package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.vincenthuto.hemomancy.common.init.BiomeInit;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class FungalSurfaceBiomeRules
{
	private static final String[] SURFACE_RULE_ORDER_NAMES = {
			"very_deep_under_floor",
			"deep_under_floor",
			"under_floor",
			"on_floor"
	};

	private FungalSurfaceBiomeRules()
	{
	}

	public static boolean usesFungalSurfaceName(String biomePath)
	{
		return "fungal_gardens".equals(biomePath) || "erythrocoral_reef".equals(biomePath);
	}

	@SuppressWarnings("unchecked")
	static ResourceKey<Biome>[] fungalSurfaceBiomes()
	{
		return new ResourceKey[] { BiomeInit.FUNGAL_GARDENS, BiomeInit.ERYTHROCORAL_REEF };
	}

	@SuppressWarnings("unchecked")
	static ResourceKey<Biome>[] fungalGardensBiome()
	{
		return new ResourceKey[] { BiomeInit.FUNGAL_GARDENS };
	}

	@SuppressWarnings("unchecked")
	static ResourceKey<Biome>[] erythrocoralReefBiome()
	{
		return new ResourceKey[] { BiomeInit.ERYTHROCORAL_REEF };
	}

	public static String[] surfaceRuleOrderNames()
	{
		return SURFACE_RULE_ORDER_NAMES.clone();
	}
}
