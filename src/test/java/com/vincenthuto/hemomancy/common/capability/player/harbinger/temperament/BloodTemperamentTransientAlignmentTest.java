package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodTemperamentTransientAlignmentTest {

	@Test
	void transientAlignmentAffectsReadsButNotSavedBaseValue() {
		BloodTemperament temper = new BloodTemperament();
		temper.setTemperAlignment(EnumBloodTemper.LUX, 7F);

		temper.addTransientAlignment(EnumBloodTemper.LUX, 2F);

		assertEquals(9F, temper.getAlignmentByTemper(EnumBloodTemper.LUX));
		assertEquals(9F, temper.getTemper().get(EnumBloodTemper.LUX));
		assertEquals(9F, temper.getTotalAlignment());
		assertEquals(7F, temper.serializeNBT(null).getFloat(EnumBloodTemper.LUX.toString()));

		temper.addTransientAlignment(EnumBloodTemper.LUX, -2F);
		assertEquals(7F, temper.getAlignmentByTemper(EnumBloodTemper.LUX));
	}

	@Test
	void multipleTransientSourcesStackWithoutChangingBaseAlignment() {
		BloodTemperament temper = new BloodTemperament();
		temper.setTemperAlignment(EnumBloodTemper.FERRIC, 3F);

		temper.addTransientAlignment(EnumBloodTemper.FERRIC, 1F);
		temper.addTransientAlignment(EnumBloodTemper.FERRIC, 2F);

		assertEquals(6F, temper.getAlignmentByTemper(EnumBloodTemper.FERRIC));
		assertEquals(3F, temper.serializeNBT(null).getFloat(EnumBloodTemper.FERRIC.toString()));
	}

	@Test
	void earnedAlignmentWhileScarIsActiveDoesNotBakeInTransientAlignment() {
		BloodTemperament temper = new BloodTemperament();
		temper.setTemperAlignment(EnumBloodTemper.ANIMUS, 4F);
		temper.addTransientAlignment(EnumBloodTemper.ANIMUS, 2F);

		temper.addTemperAlignment(EnumBloodTemper.ANIMUS, 1F);

		assertEquals(7F, temper.getAlignmentByTemper(EnumBloodTemper.ANIMUS));
		assertEquals(5F, temper.serializeNBT(null).getFloat(EnumBloodTemper.ANIMUS.toString()));
	}
}
