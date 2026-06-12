package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;

public final class HemomancyTendrilEffectsTest {
	private HemomancyTendrilEffectsTest() {
	}

	public static void main(String[] args) {
		suturePresetUsesSoftLightColors();
		voidPresetKeepsCoreAndGlowSeparated();
		surfacePresetUsesSurfaceMode();
		seedsAreStableForTheSameInputs();
	}

	private static void suturePresetUsesSoftLightColors() {
		TendrilEffectConfig config = HemomancyTendrilEffects.sutureConfig(10.0D, 42L);
		assertEquals("suture core color", 0xF8FFF1C8, config.coreColor());
		assertEquals("suture glow color", 0xB8FF77C8, config.glowColor());
		assertEquals("suture color blending", true, config.blendColors());
		assertEquals("suture lifetime", 28, config.totalLifetime());
		assertEquals("suture seed", 42L, config.seed());
	}

	private static void voidPresetKeepsCoreAndGlowSeparated() {
		TendrilEffectConfig config = HemomancyTendrilEffects.voidConfig(18.0D, 99L);
		assertEquals("void core color", 0xF8000000, config.coreColor());
		assertEquals("void glow color", 0xB87518A8, config.glowColor());
		assertEquals("void color blending", false, config.blendColors());
		assertEquals("void branch depth", 2, config.branchDepth());
		assertEquals("void seed", 99L, config.seed());
	}

	private static void surfacePresetUsesSurfaceMode() {
		TendrilEffectConfig config = HemomancyTendrilEffects.voidSurfaceConfig(8.0D, 7L);
		assertEquals("surface mode", TendrilEffectConfig.Mode.SURFACE, config.mode());
		assertEquals("surface snap distance", 4.0F, config.surfaceSnapDistance());
		assertEquals("surface lift", 0.08F, config.surfaceLift());
	}

	private static void seedsAreStableForTheSameInputs() {
		long first = HemomancyTendrilEffects.seed(1234L, 25L, 9L);
		long second = HemomancyTendrilEffects.seed(1234L, 25L, 9L);
		long differentSalt = HemomancyTendrilEffects.seed(1234L, 25L, 10L);
		assertEquals("same seed inputs", first, second);
		if (first == differentSalt) {
			throw new AssertionError("different salts should produce different seeds");
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}

	private static void assertEquals(String label, long expected, long actual) {
		if (expected != actual) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}

	private static void assertEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}

	private static void assertEquals(String label, boolean expected, boolean actual) {
		if (expected != actual) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}
}
