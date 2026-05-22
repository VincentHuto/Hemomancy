package com.vincenthuto.hemomancy.client.screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MnemonicReliquaryScreenScalingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private MnemonicReliquaryScreenScalingResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String screen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/tile/functional/MnemonicReliquaryScreen.java"));

		assertContains("mnemonic reliquary screen should compute a viewport fit scale", screen,
				"private static float fitScale(int screenWidth, int screenHeight)");
		assertContains("mnemonic reliquary screen should leave a small edge margin", screen,
				"SCREEN_PADDING");
		assertContains("mnemonic reliquary screen should scale width and height from the viewport", screen,
				"Math.min(availableWidth / (float) GUI_WIDTH, availableHeight / (float) GUI_HEIGHT)");
		assertContains("mnemonic reliquary screen should update the container image width", screen,
				"this.imageWidth = currentGuiWidth;");
		assertContains("mnemonic reliquary screen should update the container image height", screen,
				"this.imageHeight = currentGuiHeight;");
		assertContains("mnemonic reliquary screen should scale item icons with the panel", screen,
				"renderScaledItem");
		assertDoesNotContain("mnemonic reliquary screen should not center against the fixed width", screen,
				"guiLeft = (this.width - GUI_WIDTH) / 2;");
		assertDoesNotContain("mnemonic reliquary screen should not center against the fixed height", screen,
				"guiTop = (this.height - GUI_HEIGHT) / 2;");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
