package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VesperFightOuterRingClientConfigTest {
	@Test
	void lowPolyOuterRingRemainsEnabledByDefault() {
		HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());

		assertEquals(Boolean.TRUE, HemoClientConfig.USE_LOW_POLY_VESPER_FIGHT_OUTER_RING.getDefault());
	}
}
