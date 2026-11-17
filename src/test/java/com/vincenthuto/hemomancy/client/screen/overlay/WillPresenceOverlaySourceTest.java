package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;

public final class WillPresenceOverlaySourceTest {
	private static final Path OVERLAY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/WillPresenceOverlay.java");

	private WillPresenceOverlaySourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String source = Files.readString(OVERLAY).replace("\r\n", "\n");

		assertContains("will presence keeps top vignette gradient", source,
				"graphics.fillGradient(0, 0, screenWidth, edge, edgeColor, clearColor)");
		assertContains("will presence keeps bottom vignette gradient", source,
				"graphics.fillGradient(0, screenHeight - edge, screenWidth, screenHeight, clearColor, edgeColor)");
		assertNotContains("will presence does not draw a hard center line", source,
				"screenHeight / 2");
		assertNotContains("will presence does not draw a center bar fill", source,
				"graphics.fill(0, midY - 1, screenWidth, midY + 1");
		assertNotContains("will presence does not keep center bar alpha", source,
				"barAlpha");
	}
}
