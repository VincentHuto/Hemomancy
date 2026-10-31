package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MorphlingEnvironmentWiringSourceTest {
	@Test
	void powderSnowTraversalIsRegisteredInTheMainMixinConfig() {
		String mixins = ResourceReader.text("hemomancy.mixins.json");
		String powderSnow = SourceAssertions.readMainJava("mixin/core/MixinPowderSnowBlock.java");

		assertTrue(mixins.contains("\"MixinPowderSnowBlock\""));
		assertTrue(powderSnow.contains("MixinHooks.canWalkOnPowderSnow"));
	}
}
