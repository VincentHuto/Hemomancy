package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManipulationVeinStrainTest {
	@Test
	void strainPerCastScalesWithRank() {
		assertEquals(1.0F, EnumManipulationRank.HUMILIS.veinStrain());
		assertEquals(1.0F, EnumManipulationRank.MEDIOCRITAS.veinStrain());
		assertEquals(2.0F, EnumManipulationRank.SUMMA.veinStrain());
		assertEquals(2.0F, EnumManipulationRank.MAGISTER.veinStrain());
		assertEquals(3.0F, EnumManipulationRank.PERFECTUS.veinStrain());
	}
}
