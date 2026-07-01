package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

public final class MaterialGateRulesTest {
	private MaterialGateRulesTest() {
	}

	public static void main(String[] args) {
		assertVisibility("always-visible entries are unlocked",
				MaterialGate.always().visibilityFor(MaterialAtlasPath.HARBINGER, 0, 0.0F, 0.0F),
				MaterialVisibility.UNLOCKED);

		assertVisibility("current Harbinger degree unlocks matching entries",
				MaterialGate.degree(3).visibilityFor(MaterialAtlasPath.HARBINGER, 3, 0.0F, 0.0F),
				MaterialVisibility.UNLOCKED);
		assertVisibility("one Harbinger degree ahead is a veiled preview",
				MaterialGate.degree(4).visibilityFor(MaterialAtlasPath.HARBINGER, 3, 0.0F, 0.0F),
				MaterialVisibility.NEXT_PREVIEW);
		assertVisibility("two Harbinger degrees ahead is hidden",
				MaterialGate.degree(5).visibilityFor(MaterialAtlasPath.HARBINGER, 3, 0.0F, 0.0F),
				MaterialVisibility.HIDDEN);

		assertVisibility("current purity unlocks matching Unstained entries",
				MaterialGate.purity(25.0F).visibilityFor(MaterialAtlasPath.UNSTAINED, 0, 25.0F, 0.0F),
				MaterialVisibility.UNLOCKED);
		assertVisibility("next configured purity tier is a veiled preview",
				MaterialGate.purity(25.0F).visibilityFor(MaterialAtlasPath.UNSTAINED, 0, 15.0F, 0.0F),
				MaterialVisibility.NEXT_PREVIEW);
		assertVisibility("later purity tiers stay hidden",
				MaterialGate.purity(35.0F).visibilityFor(MaterialAtlasPath.UNSTAINED, 0, 15.0F, 0.0F),
				MaterialVisibility.HIDDEN);

		assertVisibility("next configured clarity tier is a veiled preview",
				MaterialGate.clarity(25.0F).visibilityFor(MaterialAtlasPath.UNSTAINED, 0, 10.0F, 10.0F),
				MaterialVisibility.NEXT_PREVIEW);
		assertVisibility("later clarity tiers stay hidden",
				MaterialGate.clarity(35.0F).visibilityFor(MaterialAtlasPath.UNSTAINED, 0, 10.0F, 10.0F),
				MaterialVisibility.HIDDEN);

		assertEquals("degree requirement text", MaterialGate.degree(4).requirementText(), "Requires Degree 4");
		assertEquals("purity requirement text", MaterialGate.purity(25.0F).requirementText(), "Requires Purity 25");
		assertEquals("clarity requirement text", MaterialGate.clarity(10.0F).requirementText(), "Requires Clarity 10");
	}

	private static void assertVisibility(String label, MaterialVisibility actual, MaterialVisibility expected) {
		if (actual != expected) {
			throw new AssertionError(label + " expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, String actual, String expected) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + " expected '" + expected + "' but got '" + actual + "'");
		}
	}
}
