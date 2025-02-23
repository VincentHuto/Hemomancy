package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class TerraBiomeInit {

	
	  public static final ResourceKey<Biome> HOT_RED = register("hot_red");
	    public static final ResourceKey<Biome> COLD_BLUE = register("cold_blue");

	    private static ResourceKey<Biome> register(String name)
	    {
	        return ResourceKey.create(Registries.BIOME, Hemomancy.rloc(name));
	    }
}
