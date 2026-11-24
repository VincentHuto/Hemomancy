package com.vincenthuto.hemomancy.client.screen;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class MnemonicReliquaryScreenScalingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private MnemonicReliquaryScreenScalingResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
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
		assertContains("casket parts should be laid out in scaled panel pixels", screen,
				"private int px(int value)");
		assertContains("a crowded side wall should shrink its drawer icons rather than run past its foot", screen,
				"if (needed <= bottom - top || size <= minSize) break;");
		assertContains("the memorized counter should be a translated plaque on the lid", screen,
				"Component.translatable(\"screen.hemomancy.mnemonic_reliquary.memorized\", used, maxSlots)");
		assertNotContains("mnemonic reliquary screen should not center against the fixed width", screen,
				"guiLeft = (this.width - GUI_WIDTH) / 2;");
		assertNotContains("mnemonic reliquary screen should not center against the fixed height", screen,
				"guiTop = (this.height - GUI_HEIGHT) / 2;");
	}
}
