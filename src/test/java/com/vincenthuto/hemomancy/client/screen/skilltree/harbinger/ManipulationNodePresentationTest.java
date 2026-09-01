package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.EnumNodeShape;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManipulationNodePresentationTest {
	@Test
	void ranksUseTheAgreedBorderPalette() {
		assertEquals(0, ManipulationNodePresentation.borderColor(EnumManipulationRank.HUMILIS));
		assertEquals(0xFFCD7F32, ManipulationNodePresentation.borderColor(EnumManipulationRank.MEDIOCRITAS));
		assertEquals(0xFFA7ADB2, ManipulationNodePresentation.borderColor(EnumManipulationRank.SUMMA));
		assertEquals(0xFFFFC43D, ManipulationNodePresentation.borderColor(EnumManipulationRank.MAGISTER));
		assertEquals(0xFFD94CFF, ManipulationNodePresentation.borderColor(EnumManipulationRank.PERFECTUS));
	}

	@Test
	void familyBaselinesUseOctagonsWithoutChangingOrdinaryNodes() {
		assertEquals(EnumNodeShape.OCTAGON,
				ManipulationNodePresentation.shape("blood_binding", EnumNodeShape.SQUARE));
		assertEquals(EnumNodeShape.DIAMOND,
				ManipulationNodePresentation.shape("blood_ritual", EnumNodeShape.DIAMOND));
	}

	@Test
	void familyBorderUsesHighestAvailableFormRank() {
		Map<String, EnumManipulationRank> ranks = Map.of(
				"blood_binding", EnumManipulationRank.HUMILIS,
				"lingering_blood_binding", EnumManipulationRank.MEDIOCRITAS,
				"chain_blood_binding", EnumManipulationRank.SUMMA,
				"blood_lattice", EnumManipulationRank.PERFECTUS);

		assertEquals(EnumManipulationRank.SUMMA, ManipulationNodePresentation.familyBorderRank(
				"blood_binding", Set.of("blood_binding", "chain_blood_binding"), ranks::get));
		assertEquals(EnumManipulationRank.HUMILIS, ManipulationNodePresentation.familyBorderRank(
				"blood_binding", Set.of("blood_binding"), ranks::get));
		assertEquals(EnumManipulationRank.HUMILIS, ManipulationNodePresentation.familyBorderRank(
				"blood_binding", Set.of(), ranks::get));
	}

	@Test
	void masteryPipsSitClockwiseOnTheCardinalNodeBorders() {
		int[][] expected = { { 0, -13 }, { 13, 0 }, { 0, 13 }, { -13, 0 } };
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i][0], ManipulationNodePresentation.masteryPipX(i, 13));
			assertEquals(expected[i][1], ManipulationNodePresentation.masteryPipY(i, 13));
		}
	}
}
