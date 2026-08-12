package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class VesperFlammeusBreathIntegrationSourceTest {
	@Test
	void phaseTwoSelectsAndExecutesTheLockedFlammeusChannel() throws IOException {
		String actions = read("VesperWeaponAction.java");
		String rules = read("VesperWeaponCombatRules.java");
		String combat = read("VesperPhaseTwoCombat.java");
		assertContains(actions, "FLAMMEUS_CONCENTRATION");
		assertContains(rules, "case FLAMMEUS -> FLAMMEUS_CONCENTRATION");
		assertContains(combat, "case FLAMMEUS_CONCENTRATION -> flammeusConcentration");
		assertContains(combat, "LivingTorchBreathRules.isInsideCone");
		assertContains(combat, "VesperFlammeusBreathRules.isDamagePulse");
		assertContains(combat, "CrimsonFireHelper.igniteCrimson(candidate, 4)");
	}

	@Test
	void effectPathNamesOnlyCrimsonAndBlackAuthoredEffects() throws IOException {
		String combat = read("VesperPhaseTwoCombat.java");
		String method = between(combat, "private static void flammeusBreathEffect", "private static void");
		assertContains(method, "VesperVisualEffects.BLOOD");
		assertContains(method, "VesperVisualEffects.BLACK");
		assertTrue(!method.contains("EMBER") && !method.contains("ORANGE") && !method.contains("ParticleTypes.FLAME"));
	}

	private static String read(String file) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame", file));
	}

	private static String between(String text, String start, String nextMethod) {
		int from = text.indexOf(start);
		if (from < 0) return "";
		int to = text.indexOf(nextMethod, from + start.length());
		return to < 0 ? text.substring(from) : text.substring(from, to);
	}

	private static void assertContains(String text, String expected) {
		assertTrue(text.contains(expected), () -> "Expected source to contain: " + expected);
	}
}
