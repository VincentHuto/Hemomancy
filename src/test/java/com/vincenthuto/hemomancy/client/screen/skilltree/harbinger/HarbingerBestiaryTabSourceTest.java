package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerBestiaryTabSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private HarbingerBestiaryTabSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String screen = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java"));
		String packetHandler = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java"));

		assertContains("bestiary tab is degree 2 gated", screen,
				"BESTIARY(\"Bestiary\", 0xFF77AA66, 2, true)");
		assertContains("bestiary controllers are cached by the tab registry", screen,
				"private final ProgressTabRegistry<Tab> tabs = new ProgressTabRegistry<>(Tab.class, this::createController);");
		assertContains("bestiary creates its controller through the tab factory", screen,
				"case BESTIARY -> new BestiaryTabController();");
		assertContains("bottom-right tab is drawn separately", screen, "drawBottomRightTabs");
		assertContains("bottom-right hit test is checked", screen, "bottomRightTabUnder");

		assertContains("bestiary request packet is registered", packetHandler,
				"PacketRequestSpecimenBestiary.TYPE");
		assertContains("bestiary sync packet is registered", packetHandler,
				"PacketSyncSpecimenBestiary.TYPE");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
