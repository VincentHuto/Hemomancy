package com.vincenthuto.hemomancy.client.screen.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodlineScreenRegressionSourceTest {
	private static final Path LEDGER_SCREEN = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/LedgerScreen.java");
	private static final Path POOL_SCREEN = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/BloodlinePoolScreen.java");
	private static final Path HARBINGER_RITES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");
	private static final Path BLOOD_CRAFT_PACKET = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodCraftingKeyPressPacket.java");

	private BloodlineScreenRegressionSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String ledgerScreen = Files.readString(LEDGER_SCREEN).replace("\r\n", "\n");
		String poolScreen = Files.readString(POOL_SCREEN).replace("\r\n", "\n");
		String harbingerRites = Files.readString(HARBINGER_RITES).replace("\r\n", "\n");
		String bloodCraftPacket = Files.readString(BLOOD_CRAFT_PACKET).replace("\r\n", "\n");

		assertContains("ledger suppresses vanilla menu blur", ledgerScreen,
				"public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)");
		assertNotContains("ledger render avoids calling vanilla background", ledgerScreen,
				"this.renderBackground(graphics, mouseX, mouseY, partialTick);");
		assertContains("founding sanctum requires an existing bloodline", harbingerRites,
				"bloodline == null || !bloodline.isValid()");
		assertNotContains("founding sanctum cannot fall back to solo owner", harbingerRites,
				"? bloodline.getLeaderUUID() : caster.getUUID()");
		assertContains("founding sanctum validates bloodline before activation", bloodCraftPacket,
				"canStartFoundingSanctum(serverPlayer)");
		assertContains("founding sanctum activation rejects missing bloodline", bloodCraftPacket,
				"bloodline == null || !bloodline.isValid()");
		assertContains("founding sanctum activation requires progenitor", bloodCraftPacket,
				"bloodline.getLeaderUUID().equals(player.getUUID())");
		assertOrder("founding sanctum activation gate runs before catalyst consumption", bloodCraftPacket,
				"canStartFoundingSanctum(serverPlayer)",
				"consumeCatalystWithinMatch(sLevel, match, bp, ItemInit.sanguine_quintessence.get())");
		assertContains("bloodline pool screen defines screen padding", poolScreen, "SCREEN_PADDING");
		assertContains("bloodline pool screen calculates fit scale", poolScreen, "fitScale");
		assertContains("bloodline pool screen stores scaled panel width", poolScreen, "currentGuiWidth");
		assertContains("bloodline pool screen scales widget positions", poolScreen, "scaled(");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": unexpected " + unexpected);
		}
	}

	private static void assertOrder(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + ": expected `" + first + "` before `" + second + "`");
		}
	}
}
