package com.vincenthuto.hemomancy.common.manipulation.ferric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IronheartedRulesTest {

	@Test
	void ironHeartsClampAndAbsorbBeforeHealth() {
		assertEquals(10.0F, IronheartedRules.addIronHeartHealth(9.0F, 10.0F));

		IronheartedRules.IronHeartAbsorption partial = IronheartedRules.absorbWithIronHearts(2.0F, 1.0F, 10.0F);
		assertEquals(1.0F, partial.ironHeartHealth(), 0.001F);
		assertEquals(0.0F, partial.remainingDamage(), 0.001F);

		IronheartedRules.IronHeartAbsorption depleted = IronheartedRules.absorbWithIronHearts(2.0F, 3.0F, 10.0F);
		assertEquals(0.0F, depleted.ironHeartHealth(), 0.001F);
		assertEquals(1.0F, depleted.remainingDamage(), 0.001F);
	}

	@Test
	void activeFerricScarBonusesExpandIronHeartCapacityByFullHearts() {
		assertEquals(10.0F, IronheartedRules.maxIronHeartHealth(0));
		assertEquals(14.0F, IronheartedRules.maxIronHeartHealth(2));
		assertEquals(18.0F, IronheartedRules.maxIronHeartHealth(4));
		assertEquals(22.0F, IronheartedRules.maxIronHeartHealth(6));
		assertEquals(26.0F, IronheartedRules.maxIronHeartHealth(8));
		assertEquals(34.0F, IronheartedRules.maxIronHeartHealth(12));
		assertEquals(17, IronheartedRules.ironHeartSlots(34.0F));
	}
}
