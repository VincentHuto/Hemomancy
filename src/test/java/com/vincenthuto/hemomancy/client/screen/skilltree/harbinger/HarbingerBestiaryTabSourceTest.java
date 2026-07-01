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
		assertContains("bestiary controller is owned by screen", screen,
				"private final BestiaryTabController bestiary = new BestiaryTabController();");
		assertContains("bestiary maps to controller", screen, "case BESTIARY");
		assertContains("bestiary maps to controller target", screen, "-> bestiary;");
		assertContains("bestiary participates in init", screen,
				"new IProgressTab[]{skills, manips, rites, crafting, scars, summons, materials, bestiary}");
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
