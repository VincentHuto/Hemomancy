package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MorphlingBodyIdiomWiringSourceTest {
	private static final Path JAVA = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void bodyIdiomsUseTheExistingEquippedDamageAndExhaustionHooks() throws IOException {
		String events = read("common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java");
		String hooks = read("mixin/util/MixinHooks.java");
		String effects = read("common/init/EffectInit.java");

		assertTrue(events.contains("WinterShroudMorphlingItem.adjustIncomingColdDamage"));
		assertTrue(events.contains("EmberfangMorphlingItem.adjustIncomingDamage"));
		assertTrue(hooks.contains("EmberfangMorphlingItem.scaleExhaustion"));
		assertFalse(effects.contains("morphling_emberfang_movement_speed"));
		assertFalse(effects.contains("morphling_emberfang_attack_speed"));
	}

	@Test
	void powderSnowTraversalIsRegisteredInTheMainMixinConfig() throws IOException {
		String mixins = Files.readString(Path.of("src/main/resources/hemomancy.mixins.json"));
		String powderSnow = read("mixin/core/MixinPowderSnowBlock.java");

		assertTrue(mixins.contains("\"MixinPowderSnowBlock\""));
		assertTrue(powderSnow.contains("MixinHooks.canWalkOnPowderSnow"));
	}

	private static String read(String relative) throws IOException {
		return Files.readString(JAVA.resolve(relative));
	}
}
