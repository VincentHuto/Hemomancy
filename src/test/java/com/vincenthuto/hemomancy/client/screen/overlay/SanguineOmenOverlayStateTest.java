package com.vincenthuto.hemomancy.client.screen.overlay;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class SanguineOmenOverlayStateTest {
	private static final String STATE_CLASS =
			"com.vincenthuto.hemomancy.client.screen.overlay.SanguineOmenOverlayState";

	private SanguineOmenOverlayStateTest() {
	}

	public static void main(String[] args) throws Exception {
		Class<?> stateClass = Class.forName(STATE_CLASS);
		Constructor<?> ctor = stateClass.getDeclaredConstructor();
		ctor.setAccessible(true);
		Object state = ctor.newInstance();

		Method start = stateClass.getDeclaredMethod("start", int.class, float.class, int.class);
		Method tick = stateClass.getDeclaredMethod("tick");
		Method isActive = stateClass.getDeclaredMethod("isActive");
		Method alpha = stateClass.getDeclaredMethod("alpha", float.class);
		Method progress = stateClass.getDeclaredMethod("progress", float.class);
		Method seed = stateClass.getDeclaredMethod("seed");

		start.invoke(state, 120, 0.88f, 42);
		assertTrue("state becomes active after start", (boolean) isActive.invoke(state));
		assertEquals("seed is retained for shader variation", 42, (int) seed.invoke(state));
		assertBetween("alpha ramps in near the start", ((Number) alpha.invoke(state, 0.0f)).floatValue(), 0.0f, 0.88f);
		assertBetween("progress starts near zero", ((Number) progress.invoke(state, 0.0f)).floatValue(), 0.0f, 0.05f);

		for (int i = 0; i < 60; i++) {
			tick.invoke(state);
		}
		assertTrue("state remains active halfway through", (boolean) isActive.invoke(state));
		assertBetween("mid effect is strongly visible", ((Number) alpha.invoke(state, 0.5f)).floatValue(), 0.55f, 0.88f);
		assertBetween("progress advances halfway", ((Number) progress.invoke(state, 0.0f)).floatValue(), 0.45f, 0.55f);

		start.invoke(state, 40, 0.6f, 99);
		assertEquals("new trigger replaces seed", 99, (int) seed.invoke(state));
		assertBetween("new trigger restarts progress", ((Number) progress.invoke(state, 0.0f)).floatValue(), 0.0f, 0.05f);

		for (int i = 0; i < 40; i++) {
			tick.invoke(state);
		}
		assertFalse("state expires after duration", (boolean) isActive.invoke(state));
		assertEquals("alpha is zero after expiry", 0.0f, ((Number) alpha.invoke(state, 0.0f)).floatValue(), 0.0001f);
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}

	private static void assertBetween(String label, float actual, float minInclusive, float maxInclusive) {
		if (actual < minInclusive || actual > maxInclusive) {
			throw new AssertionError(label + ": expected " + actual + " between "
					+ minInclusive + " and " + maxInclusive);
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but found " + actual);
		}
	}

	private static void assertEquals(String label, float expected, float actual, float tolerance) {
		if (Math.abs(expected - actual) > tolerance) {
			throw new AssertionError(label + ": expected " + expected + " but found " + actual);
		}
	}
}
