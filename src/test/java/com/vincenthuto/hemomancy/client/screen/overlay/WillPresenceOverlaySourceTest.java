package com.vincenthuto.hemomancy.client.screen.overlay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillPresenceOverlaySourceTest {
	private static final Path OVERLAY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/WillPresenceOverlay.java");

	private WillPresenceOverlaySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(OVERLAY).replace("\r\n", "\n");

		assertContains("will presence keeps top vignette gradient", source,
				"graphics.fillGradient(0, 0, screenWidth, edge, edgeColor, clearColor)");
		assertContains("will presence keeps bottom vignette gradient", source,
				"graphics.fillGradient(0, screenHeight - edge, screenWidth, screenHeight, clearColor, edgeColor)");
		assertDoesNotContain("will presence does not draw a hard center line", source,
				"screenHeight / 2");
		assertDoesNotContain("will presence does not draw a center bar fill", source,
				"graphics.fill(0, midY - 1, screenWidth, midY + 1");
		assertDoesNotContain("will presence does not keep center bar alpha", source,
				"barAlpha");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": unexpected " + forbidden);
		}
	}
}
