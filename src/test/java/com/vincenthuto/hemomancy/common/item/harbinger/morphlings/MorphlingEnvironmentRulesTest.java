package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class MorphlingEnvironmentRulesTest {
	@Test
	void hotheadedTradesHeatForPowerAndExhaustion() {
		assertEquals(MorphlingHeatRules.TEMPERATE, MorphlingHeatRules.environmentLevel(0.9F, false, false, false));
		assertEquals(MorphlingHeatRules.WARM, MorphlingHeatRules.environmentLevel(1.0F, false, false, false));
		assertEquals(MorphlingHeatRules.HOT, MorphlingHeatRules.environmentLevel(1.5F, false, false, false));
		assertEquals(MorphlingHeatRules.EXTREME, MorphlingHeatRules.environmentLevel(2.0F, false, false, false));
		assertEquals(MorphlingHeatRules.EXTREME, MorphlingHeatRules.environmentLevel(0.8F, true, false, false));

		assertEquals(0.0D, MorphlingHeatRules.benefit(MorphlingHeatRules.TEMPERATE));
		assertEquals(0.05D, MorphlingHeatRules.benefit(MorphlingHeatRules.WARM));
		assertEquals(0.10D, MorphlingHeatRules.benefit(MorphlingHeatRules.HOT));
		assertEquals(0.15D, MorphlingHeatRules.benefit(MorphlingHeatRules.EXTREME));
		assertEquals(1.10F, MorphlingHeatRules.exhaustionMultiplier(MorphlingHeatRules.WARM));
		assertEquals(1.25F, MorphlingHeatRules.exhaustionMultiplier(MorphlingHeatRules.HOT));
		assertEquals(1.50F, MorphlingHeatRules.exhaustionMultiplier(MorphlingHeatRules.EXTREME));
		assertEquals(1.0F, MorphlingHeatRules.incomingDamageMultiplier(MorphlingHeatRules.WARM));
		assertEquals(1.05F, MorphlingHeatRules.incomingDamageMultiplier(MorphlingHeatRules.HOT));
		assertEquals(1.10F, MorphlingHeatRules.incomingDamageMultiplier(MorphlingHeatRules.EXTREME));
	}
}
