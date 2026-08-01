package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProgressDetailKeyHandlerTest {
	@Test
	void eClosesAnOpenDetailWindowAndConsumesTheKey() {
		AtomicInteger closes = new AtomicInteger();

		boolean handled = ProgressDetailKeyHandler.handle(GLFW.GLFW_KEY_E, () -> {
			closes.incrementAndGet();
			return true;
		});

		assertTrue(handled);
		assertEquals(1, closes.get());
	}

	@Test
	void eFallsThroughWhenThereIsNoOpenDetailWindow() {
		assertFalse(ProgressDetailKeyHandler.handle(GLFW.GLFW_KEY_E, () -> false));
	}

	@Test
	void otherKeysNeverInvokeTheDetailCloser() {
		AtomicInteger closes = new AtomicInteger();

		boolean handled = ProgressDetailKeyHandler.handle(GLFW.GLFW_KEY_R, () -> {
			closes.incrementAndGet();
			return true;
		});

		assertFalse(handled);
		assertEquals(0, closes.get());
	}
}
