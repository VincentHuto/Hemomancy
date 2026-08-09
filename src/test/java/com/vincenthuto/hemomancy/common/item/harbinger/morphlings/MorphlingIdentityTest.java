package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingIdentityTest {
	private MorphlingIdentityTest() {
	}

	public static void main(String[] args) {
		assignedIdentitySurvivesDivergentRuntimeComponents();
		independentMorphlingsOfTheSameStrainDoNotMatch();
		legacyMorphlingsFallBackToTheirStrain();
		differentStrainsNeverMatch();
	}

	private static void assignedIdentitySurvivesDivergentRuntimeComponents() {
		assertTrue("runtime bonding progress must not hide the equipped marker",
				MorphlingIdentity.matchesIdentity(true, "a1c9", "a1c9"));
	}

	private static void independentMorphlingsOfTheSameStrainDoNotMatch() {
		assertFalse("separately identified morphlings must remain distinguishable",
				MorphlingIdentity.matchesIdentity(true, "first", "second"));
	}

	private static void legacyMorphlingsFallBackToTheirStrain() {
		assertTrue("pre-identity saves must still recover their active marker",
				MorphlingIdentity.matchesIdentity(true, "", ""));
	}

	private static void differentStrainsNeverMatch() {
		assertFalse("different strains must never match",
				MorphlingIdentity.matchesIdentity(false, "shared", "shared"));
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(String label, boolean value) {
		if (value) throw new AssertionError(label);
	}
}
