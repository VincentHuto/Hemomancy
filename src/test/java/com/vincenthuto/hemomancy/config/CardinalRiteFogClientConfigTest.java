package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteFogClientConfigTest {
	@Test
	void cardinalRiteFogIsEnabledByDefault() {
		HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());

		assertEquals(Boolean.TRUE, HemoClientConfig.RENDER_CARDINAL_RITE_FOG.getDefault());
	}

	@Test
	void diffuseFogVerticalOffsetRemainsTunable() {
		HemoClientConfig.registerClientConfig(new ModConfigSpec.Builder());

		assertEquals(0.0D,
				HemoClientConfig.CARDINAL_RITE_FOG_VERTICAL_OFFSET.getDefault());
	}
}
