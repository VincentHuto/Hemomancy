package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;

/** Reskins vanilla End island terrain inside the Cortical Drift without touching its shape. */
public final class CorticalDriftSurfaceRuleData {
	private CorticalDriftSurfaceRuleData() {}

	public static SurfaceRules.RuleSource makeRules() {
		SurfaceRules.RuleSource myelin = state(BlockInit.myelin_sheath.get());
		SurfaceRules.RuleSource slate = state(BlockInit.axonal_slate.get());
		SurfaceRules.RuleSource ganglion = state(BlockInit.ganglion_matter.get());
		SurfaceRules.RuleSource tissue = state(BlockInit.neural_tissue.get());

		return SurfaceRules.ifTrue(SurfaceRules.isBiome(BiomeInit.CORTICAL_DRIFT), SurfaceRules.sequence(
				SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, myelin),
				SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(
						SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.2, 0.2), ganglion),
						slate)),
				SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, tissue)));
	}

	private static SurfaceRules.RuleSource state(net.minecraft.world.level.block.Block block) {
		return SurfaceRules.state(block.defaultBlockState());
	}
}
