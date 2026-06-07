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
		String foundingFaneGate = section(bloodCraftPacket, "private static boolean canStartFoundingFane",
				"private static boolean hasActiveFane");

		assertContains("ledger suppresses vanilla menu blur", ledgerScreen,
				"public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)");
		assertNotContains("ledger render avoids calling vanilla background", ledgerScreen,
				"this.renderBackground(graphics, mouseX, mouseY, partialTick);");
		assertContains("founding fane requires an existing bloodline", harbingerRites,
				"bloodline == null || !bloodline.isValid()");
		assertNotContains("founding fane cannot fall back to solo owner", harbingerRites,
				"? bloodline.getLeaderUUID() : caster.getUUID()");
		assertContains("founding fane validates bloodline before activation", bloodCraftPacket,
				"canStartFoundingFane(serverPlayer)");
		assertContains("founding fane activation rejects missing bloodline", bloodCraftPacket,
				"bloodline == null || !bloodline.isValid()");
		assertContains("founding fane activation requires progenitor", bloodCraftPacket,
				"bloodline.getLeaderUUID().equals(player.getUUID())");
		assertContains("founding fane activation checks existing active fane data", bloodCraftPacket,
				"FoundingFaneSavedData.get(level).hasFane(owner)");
		assertContains("founding fane activation checks every server level for existing fanes", bloodCraftPacket,
				"player.server.getAllLevels()");
		assertContains("founding fane validation rejects an already-active fane", foundingFaneGate,
				"hasActiveFane(player, bloodline.getLeaderUUID())");
		assertOrder("founding fane activation gate runs before catalyst consumption", bloodCraftPacket,
				"canStartFoundingFane(serverPlayer)",
				"consumeCatalystWithinMatch(sLevel, match, bp, ItemInit.sanguine_quintessence.get())");
		assertContains("bloodline pool screen defines screen padding", poolScreen, "SCREEN_PADDING");
		assertContains("bloodline pool screen calculates fit scale", poolScreen, "fitScale");
		assertContains("bloodline pool screen stores scaled panel width", poolScreen, "currentGuiWidth");
		assertContains("bloodline pool screen scales widget positions", poolScreen, "scaled(");
		assertContains("bloodline pool screen renders central shared pool sphere", poolScreen, "renderBloodSphere");
		assertContains("bloodline pool screen reuses machine blood volume widget for vessel bar", poolScreen,
				"BloodVolumeBarWidget.render");
		assertContains("bloodline pool screen reuses machine blood volume tooltip", poolScreen,
				"BloodVolumeBarWidget.renderTooltip");
		assertNotContains("bloodline pool screen removes the redundant seal settings button", poolScreen,
				"Seal Pool Settings");
		assertContains("bloodline pool screen uses themed ritual buttons", poolScreen, "RitualButton");
		assertContains("bloodline pool screen uses themed toggles instead of vanilla checkboxes", poolScreen,
				"ToggleRitualButton");
		assertContains("bloodline pool screen centers input field text", poolScreen, "renderCenteredEditBoxText");
		assertContains("bloodline pool screen suppresses vanilla edit box text", poolScreen, "CenteredEditBox");
		assertContains("bloodline pool screen suppresses vanilla edit box widget rendering", poolScreen,
				"public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)");
		assertContains("bloodline pool screen fits long button labels", poolScreen, "drawCenteredFittingString");
		assertContains("bloodline pool screen exposes fane sight cycle", poolScreen,
				"FaneBoundaryClientData.cycleViewMode()");
		assertNotContains("bloodline pool screen no longer uses vanilla checkbox widgets", poolScreen,
				"Checkbox.builder");
		assertNotContains("bloodline pool screen avoids normal edit box construction", poolScreen,
				"new EditBox(");
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

	private static String section(String text, String start, String end) {
		int startIndex = text.indexOf(start);
		int endIndex = text.indexOf(end, startIndex);
		if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex) {
			throw new AssertionError("unable to locate section from `" + start + "` to `" + end + "`");
		}
		return text.substring(startIndex, endIndex);
	}
}
