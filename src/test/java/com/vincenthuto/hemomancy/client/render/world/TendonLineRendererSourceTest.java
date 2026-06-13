package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TendonLineRendererSourceTest {
	private static final Path CLIENT_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/TendonLineRenderer.java");

	private TendonLineRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String events = Files.readString(CLIENT_EVENTS);
		assertContains("tendon line renderer is called during translucent world render", events,
				"TendonLineRenderer.render(event.getPoseStack(), partialTick);");

		if (!Files.exists(RENDERER)) {
			throw new AssertionError("missing " + RENDERER);
		}
		String renderer = Files.readString(RENDERER);
		assertContains("renderer reads tendon line anchor from held item", renderer, "TendonLineItem.readAnchor");
		assertContains("renderer draws a world line strip", renderer, "RenderType.lineStrip()");
		assertContains("renderer flushes line strip", renderer, "buffer.endBatch(RenderType.lineStrip())");
		assertContains("renderer moves tendon origin toward held item", renderer, "HAND_FORWARD_OFFSET");
		assertContains("renderer lowers tendon origin from shoulder height", renderer, "HAND_DOWN_OFFSET");
		assertContains("renderer lowers third-person tendon origin separately", renderer,
				"THIRD_PERSON_HAND_EXTRA_DROP");
		assertContains("renderer preserves first-person tendon origin", renderer,
				"mc.options.getCameraType().isFirstPerson()");
		assertContains("renderer offsets tendon origin to the active hand", renderer, "HAND_SIDE_OFFSET");
		assertNotContains("renderer should not anchor near the player's shoulder", renderer,
				"player.getEyeHeight() * 0.78D");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}

	private static void assertNotContains(String label, String source, String unexpected) {
		if (source.contains(unexpected)) {
			throw new AssertionError(label + " still contains: " + unexpected);
		}
	}
}
