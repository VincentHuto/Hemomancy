package com.vincenthuto.hemomancy.common.entity.summon;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ScarletMummerRulesTest {
	private ScarletMummerRulesTest() {
	}

	public static void main(String[] args) throws Exception {
		assertEquals(160, ScarletMummerRules.PERFORMANCE_INTERVAL_TICKS, "performance interval");
		assertEquals(80, ScarletMummerRules.PERFORMANCE_DURATION_TICKS, "performance duration");
		assertEquals(4, ScarletMummerRules.MAX_ATTENTION_TARGETS, "attention cap");

		assertTrue(ScarletMummerRules.mayRedirect(true, false, true),
				"active owner threat may be redirected");
		assertFalse(ScarletMummerRules.mayRedirect(false, false, true), "unrelated mob is ignored");
		assertFalse(ScarletMummerRules.mayRedirect(true, true, true), "immune threat is ignored");
		assertFalse(ScarletMummerRules.mayRedirect(true, false, false),
				"mob unable to attack mummer is ignored");

		assertTrue(ScarletMummerRules.mayEvade(true, true, true, true),
				"first safe melee hit is evaded");
		assertFalse(ScarletMummerRules.mayEvade(false, true, true, true),
				"mummer cannot evade outside performance");
		assertFalse(ScarletMummerRules.mayEvade(true, false, true, true), "spent evasion cannot repeat");
		assertFalse(ScarletMummerRules.mayEvade(true, true, false, true), "non-melee damage is not evaded");
		assertFalse(ScarletMummerRules.mayEvade(true, true, true, false),
				"unsafe reposition does not cancel damage");

		String immuneTag = Files.readString(Path.of(
				"src/main/resources/data/hemomancy/tags/entity_type/puppet_attention_immune.json"));
		assertTrue(immuneTag.contains("minecraft:ender_dragon"), "dragon is attention immune");
		assertTrue(immuneTag.contains("minecraft:wither"), "wither is attention immune");
		assertTrue(immuneTag.contains("hemomancy:vesper_crowned_refusal"), "Vesper is attention immune");
	}

	private static void assertTrue(boolean value, String message) {
		if (!value) throw new AssertionError(message);
	}

	private static void assertFalse(boolean value, String message) {
		if (value) throw new AssertionError(message);
	}

	private static void assertEquals(int expected, int actual, String message) {
		if (expected != actual) {
			throw new AssertionError(message + " expected " + expected + " but was " + actual);
		}
	}
}
