package com.vincenthuto.hemomancy.common.item.harbinger;

public final class RecycledEnzymeIdentityTest {
	private RecycledEnzymeIdentityTest() {
	}

	public static void main(String[] args) {
		var first = RecycledEnzymeIdentity.fromSeed(918273645L);
		var again = RecycledEnzymeIdentity.fromSeed(918273645L);
		assertEquals("same seed keeps tendency", first.tendencyIndex(), again.tendencyIndex());
		assertEquals("same seed keeps potency", first.potency(), again.potency());
		if (first.potency() < 3F || first.potency() > 7F) {
			throw new AssertionError("recycled potency must stay in the low-grade 3-7 range: " + first.potency());
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
