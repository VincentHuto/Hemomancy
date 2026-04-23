package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.common.init.PlacedFeatureInit;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class HemoPlacementUtils {
	  public static void bootstrap(BootstrapContext<PlacedFeature> context)
	    {
	        PlacedFeatureInit.bootstrap(context);
	    }
}
