package com.vincenthuto.hemomancy.common.entity.projectile;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingFlailTerrainRulesTest {
	@Test
	void waterConversionRequiresSourceWaterNoBlockEntityAndProtectionApproval() throws Exception {
		Method mayFreeze = Class.forName("com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailTerrainRules")
				.getMethod("mayFreezeWater", boolean.class, boolean.class, boolean.class, boolean.class);
		assertTrue((boolean) mayFreeze.invoke(null, true, true, false, true));
		assertFalse((boolean) mayFreeze.invoke(null, false, true, false, true));
		assertFalse((boolean) mayFreeze.invoke(null, true, false, false, true));
		assertFalse((boolean) mayFreeze.invoke(null, true, true, true, true));
		assertFalse((boolean) mayFreeze.invoke(null, true, true, false, false));
	}

	@Test
	void snowRequiresSupportReplaceableTargetAndProtectionApproval() throws Exception {
		Method maySnow = Class.forName("com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailTerrainRules")
				.getMethod("mayPlaceSnow", boolean.class, boolean.class, boolean.class, int.class, boolean.class, boolean.class);
		assertTrue((boolean) maySnow.invoke(null, true, true, false, 0, false, true));
		assertTrue((boolean) maySnow.invoke(null, true, false, true, 5, false, true));
		assertFalse((boolean) maySnow.invoke(null, false, true, false, 0, false, true));
		assertFalse((boolean) maySnow.invoke(null, true, false, true, 8, false, true));
		assertFalse((boolean) maySnow.invoke(null, true, true, false, 0, true, true));
		assertFalse((boolean) maySnow.invoke(null, true, true, false, 0, false, false));
	}

	@Test
	void snowLayersIncrementWithoutExceedingEight() throws Exception {
		Method next = Class.forName("com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailTerrainRules")
				.getMethod("nextSnowLayers", int.class);
		assertEquals(1, next.invoke(null, 0));
		assertEquals(6, next.invoke(null, 5));
		assertEquals(8, next.invoke(null, 8));
	}
}
