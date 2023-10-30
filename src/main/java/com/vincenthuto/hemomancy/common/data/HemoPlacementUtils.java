package com.vincenthuto.hemomancy.common.data;

import com.vincenthuto.hemomancy.common.worldgen.PlacedFeatureInit;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class HemoPlacementUtils {
	  public static void bootstrap(BootstapContext<PlacedFeature> context)
	    {
	        PlacedFeatureInit.bootstrap(context);
	    }
}
