package com.vincenthuto.hemomancy.client.render.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConstrictorCordRendererSourceTest {
	private ConstrictorCordRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String clientEvents = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		assertContains("constrictor cord projectile renderer",
				clientEvents,
				"event.registerEntityRenderer(EntityInit.constrictor_cord_projectile.get(), ThrownItemRenderer::new)");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}
}
