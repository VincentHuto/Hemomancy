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
		desertContextIncludesSerpentLayer();
		openLandContextFallsBackToPests();
		selectedMaskIsCappedAndRoundTrips();
		emptyContextStillSelectsOneLayer();
	}

	private static void oceanContextIncludesAquaticLayers() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().inWater().ocean());
		assertContains("ocean context includes cuttlefish", candidates, MorphlingPolypLayer.CUTTLEFISH);
		assertContains("ocean context includes urchin", candidates, MorphlingPolypLayer.URCHIN);
	}

	private static void undergroundContextIncludesCaveLayers() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().underground().lowLight().stoneNearby().dirtNearby());
		assertContains("underground context includes bat", candidates, MorphlingPolypLayer.BAT);
		assertContains("underground context includes mole", candidates, MorphlingPolypLayer.MOLE);
		assertContains("underground context includes centipede", candidates, MorphlingPolypLayer.CENTIPEDE);
	}

	private static void desertContextIncludesSerpentLayer() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().desertOrBadlands());
		assertContains("desert context includes serpent", candidates, MorphlingPolypLayer.SERPENT);
	}

	private static void openLandContextFallsBackToPests() {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				MorphlingPolypLayerRules.context().openLand());
		assertEquals("open land candidate count", 1, candidates.size());
		assertContains("open land fallback includes pests", candidates, MorphlingPolypLayer.PESTS);
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
