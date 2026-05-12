package com.vincenthuto.hemomancy.common.worldgen.terrablender;

final class FungalSurfaceBiomeRulesTest {
	public static void main(String[] args) {
		usesFungalSurfaceForFungalGardens();
		usesFungalSurfaceForErythrocoralReef();
		rejectsOrdinaryVanillaBiomes();
		ordersDeepestFungalSurfaceRulesFirst();
	}

	private static void usesFungalSurfaceForFungalGardens() {
		assertTrue(FungalSurfaceBiomeRules.usesFungalSurfaceName("fungal_gardens"),
				"fungal gardens should use the fungal surface palette");
	}

	private static void usesFungalSurfaceForErythrocoralReef() {
		assertTrue(FungalSurfaceBiomeRules.usesFungalSurfaceName("erythrocoral_reef"),
				"erythrocoral reef exposed land should not remain grass/dirt/stone");
	}

	private static void rejectsOrdinaryVanillaBiomes() {
		assertFalse(FungalSurfaceBiomeRules.usesFungalSurfaceName("plains"),
				"ordinary plains should keep vanilla surface rules");
	}

	private static void ordersDeepestFungalSurfaceRulesFirst() {
		String[] order = FungalSurfaceBiomeRules.surfaceRuleOrderNames();
		assertEquals("very_deep_under_floor", order[0],
				"hemorrhagic crust must be checked before shallower floor rules");
		assertEquals("deep_under_floor", order[1],
				"venous stone must be checked before ordinary dirt");
		assertEquals("under_floor", order[2],
				"erythrocytic dirt should sit above deep stone");
		assertEquals("on_floor", order[3],
				"surface cap should be the final fungal surface layer");
	}

	private static void assertEquals(String expected, String actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		if (condition) {
			throw new AssertionError(message);
		}
	}
}
