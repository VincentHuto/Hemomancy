package com.vincenthuto.hemomancy.common.entity.summon;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public final class MorphlingPolypLayerRulesTest {
	private MorphlingPolypLayerRulesTest() {
	}

	public static void main(String[] args) {
		oceanContextIncludesAquaticLayers();
		undergroundContextIncludesCaveLayers();
		desertContextIncludesVentLayer();
		openLandContextYieldsSunAlgaOnly();
		selectedMaskIsCappedAndRoundTrips();
		emptyContextStillSelectsOneLayer();
	}

	private static void oceanContextIncludesAquaticLayers() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().inWater().ocean());
		assertContains("ocean context includes sun alga", candidates, MorphlingPolypLayer.SUN_ALGA);
		assertContains("ocean context includes gut bacteria", candidates, MorphlingPolypLayer.GUT_BACTERIA);
		assertContains("ocean context includes light bacteria", candidates, MorphlingPolypLayer.LIGHT_BACTERIA);
	}

	private static void undergroundContextIncludesCaveLayers() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().underground().lowLight().stoneNearby().dirtNearby());
		assertContains("underground context includes black yeast", candidates, MorphlingPolypLayer.BLACK_YEAST);
		assertContains("underground context includes bone bacteria", candidates, MorphlingPolypLayer.BONE_BACTERIA);
		assertContains("underground context includes vent bacteria", candidates, MorphlingPolypLayer.VENT_BACTERIA);
	}

	private static void desertContextIncludesVentLayer() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().desertOrBadlands());
		assertContains("desert context includes vent bacteria", candidates, MorphlingPolypLayer.VENT_BACTERIA);
	}

	private static void openLandContextYieldsSunAlgaOnly() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().openLand());
		assertEquals("open land candidate count", 1, candidates.size());
		assertContains("open land yields sun alga", candidates, MorphlingPolypLayer.SUN_ALGA);
	}

	private static void selectedMaskIsCappedAndRoundTrips() {
		EnumSet<MorphlingPolypLayer> allLayers = EnumSet.allOf(MorphlingPolypLayer.class);
		int mask = MorphlingPolypLayerRules.selectLayerMask(allLayers, new Random(42L), 3);
		List<MorphlingPolypLayer> layers = MorphlingPolypLayerRules.layersFromMask(mask);
		assertEquals("selected layer count is capped", 3, layers.size());
		assertEquals("round-tripped mask is sanitized", mask, MorphlingPolypLayerRules.maskFor(layers));
	}

	private static void emptyContextStillSelectsOneLayer() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context());
		int mask = MorphlingPolypLayerRules.selectLayerMask(candidates, new Random(7L), 3);
		assertEquals("empty context still has one layer", 1, MorphlingPolypLayerRules.layersFromMask(mask).size());
	}

	private static void assertContains(String label, EnumSet<MorphlingPolypLayer> candidates,
			MorphlingPolypLayer expected) {
		if (!candidates.contains(expected)) {
			throw new AssertionError(label + ": candidates were " + candidates);
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
	}
}
