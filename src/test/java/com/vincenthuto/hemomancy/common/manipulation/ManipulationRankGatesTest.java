package com.vincenthuto.hemomancy.common.manipulation;

public final class ManipulationRankGatesTest {
	private ManipulationRankGatesTest() {
	}

	public static void main(String[] args) {
		assertEquals("humilis degree", 0, ManipulationRankGates.minDegreeForRank(EnumManipulationRank.HUMILIS));
		assertEquals("mediocritas degree", 1, ManipulationRankGates.minDegreeForRank(EnumManipulationRank.MEDIOCRITAS));
		assertEquals("summa degree", 3, ManipulationRankGates.minDegreeForRank(EnumManipulationRank.SUMMA));
		assertEquals("magister degree", 5, ManipulationRankGates.minDegreeForRank(EnumManipulationRank.MAGISTER));
		assertEquals("perfectus degree", 6, ManipulationRankGates.minDegreeForRank(EnumManipulationRank.PERFECTUS));

		assertTrue("degree 1 can learn mediocritas",
				ManipulationRankGates.playerMeetsRank(1, EnumManipulationRank.MEDIOCRITAS));
		assertFalse("degree 2 cannot learn summa",
				ManipulationRankGates.playerMeetsRank(2, EnumManipulationRank.SUMMA));
		assertTrue("degree 3 can learn summa",
				ManipulationRankGates.playerMeetsRank(3, EnumManipulationRank.SUMMA));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
