package com.vincenthuto.hemomancy.common.capability.player.unstained;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ClarityBiologyWiringSourceTest {
	private ClarityBiologyWiringSourceTest() {
	}

	public static void main(String[] args) throws Exception {
		String mixins = Files.readString(Path.of("src/main/resources/hemomancy.mixins.json"));
		assertContains(mixins, "MixinHealOrHarmMobEffect");
		assertContains(mixins, "MixinRegenerationMobEffect");
		assertContains(mixins, "MixinPoisonMobEffect");
		assertContains(mixins, "MixinPlayer");

		String hooks = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/mixin/util/MixinHooks.java"));
		assertContains(hooks, "hasClarityUnlocked()");
		assertContains(hooks, "getClarity()");
		assertContains(hooks, "scalePotionResponse");
		assertContains(hooks, "scalePoisonDamage");
		assertContains(hooks, "scaleHungerExhaustion");
	}

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError("Expected source to contain: " + expected);
		}
	}
}
