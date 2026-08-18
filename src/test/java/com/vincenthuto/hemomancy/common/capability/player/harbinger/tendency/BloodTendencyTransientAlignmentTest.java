package com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodTendencyTransientAlignmentTest {

	@Test
	void transientAlignmentAffectsReadsButNotSavedBaseValue() {
		BloodTendency tendency = new BloodTendency();
		tendency.setTendencyAlignment(EnumBloodTendency.LUX, 7F);

		tendency.addTransientAlignment(EnumBloodTendency.LUX, 2F);

		assertEquals(9F, tendency.getAlignmentByTendency(EnumBloodTendency.LUX));
		assertEquals(9F, tendency.getTendency().get(EnumBloodTendency.LUX));
		assertEquals(9F, tendency.getTotalAlignment());
		assertEquals(7F, tendency.serializeNBT(null).getFloat(EnumBloodTendency.LUX.toString()));

		tendency.addTransientAlignment(EnumBloodTendency.LUX, -2F);
		assertEquals(7F, tendency.getAlignmentByTendency(EnumBloodTendency.LUX));
	}

	@Test
	void multipleTransientSourcesStackWithoutChangingBaseAlignment() {
		BloodTendency tendency = new BloodTendency();
		tendency.setTendencyAlignment(EnumBloodTendency.FERRIC, 3F);

		tendency.addTransientAlignment(EnumBloodTendency.FERRIC, 1F);
		tendency.addTransientAlignment(EnumBloodTendency.FERRIC, 2F);

		assertEquals(6F, tendency.getAlignmentByTendency(EnumBloodTendency.FERRIC));
		assertEquals(3F, tendency.serializeNBT(null).getFloat(EnumBloodTendency.FERRIC.toString()));
	}

	@Test
	void earnedAlignmentWhileScarIsActiveDoesNotBakeInTransientAlignment() {
		BloodTendency tendency = new BloodTendency();
		tendency.setTendencyAlignment(EnumBloodTendency.ANIMUS, 4F);
		tendency.addTransientAlignment(EnumBloodTendency.ANIMUS, 2F);

		tendency.addTendencyAlignment(EnumBloodTendency.ANIMUS, 1F);

		assertEquals(7F, tendency.getAlignmentByTendency(EnumBloodTendency.ANIMUS));
		assertEquals(5F, tendency.serializeNBT(null).getFloat(EnumBloodTendency.ANIMUS.toString()));
	}
}
