package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScryingPodiumDiagnosticsResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private ScryingPodiumDiagnosticsResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String scryingPodium = readSource(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/ScryingPodiumBlock.java");
		String packetHandler = readSource("com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		String openPacket = readSource(
				"com/vincenthuto/hemomancy/common/network/capa/manips/PacketOpenScryingDiagnostics.java");
		String containerInit = readSource("com/vincenthuto/hemomancy/common/init/ContainerInit.java");
		String clientEvents = readSource("com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String diagnosticsMenu = readSource("com/vincenthuto/hemomancy/common/menu/ScryingDiagnosticsMenu.java");
		String diagnosticsProvider = readSource(
				"com/vincenthuto/hemomancy/common/menu/ScryingDiagnosticsMenuProvider.java");
		String diagnosticsScreen = readSource(
				"com/vincenthuto/hemomancy/client/screen/item/ScryingDiagnosticsScreen.java");
		String lang = readResource("assets/hemomancy/lang/en_us.json");
		String reference = read(Path.of("docs/HEMOMANCY_REFERENCE.md"));

		assertContains("scrying podium should open diagnostics on normal use", scryingPodium,
				"PacketHandler.sendToServer(new PacketOpenScryingDiagnostics(pos));");
		assertContains("scrying podium should keep its shift-use unstained conversion", scryingPodium,
				"BlockInit.unstained_podium");
		assertDoesNotContain("scrying podium should not reopen the equipment inventory", scryingPodium,
				"PacketOpenScarsInv");

		assertContains("diagnostic packet should validate the scrying podium block", openPacket,
				"player.level().getBlockState(pos).is(BlockInit.scrying_podium.get())");
		assertContains("diagnostic packet should open the diagnostics menu", openPacket,
				"player.openMenu(new ScryingDiagnosticsMenuProvider())");
		assertContains("packet handler should register scrying diagnostics", packetHandler,
				"PacketOpenScryingDiagnostics.TYPE");

		assertContains("container init should register the diagnostics menu", containerInit,
				"MenuType<ScryingDiagnosticsMenu>");
		assertContains("diagnostics menu should use its registered menu type", diagnosticsMenu,
				"ContainerInit.scrying_diagnostics.get()");
		assertContains("diagnostics provider should name the station", diagnosticsProvider,
				"container.hemomancy.scrying_diagnostics");
		assertContains("client should register the diagnostics screen", clientEvents,
				"ScryingDiagnosticsScreen::new");

		assertContains("diagnostics screen should expose blood volume", diagnosticsScreen, "Blood Volume");
		assertContains("diagnostics screen should expose dominant tendency", diagnosticsScreen, "Dominant Tendency");
		assertContains("diagnostics screen should expose vascular health", diagnosticsScreen, "Vascular Health");
		assertContains("diagnostics screen should expose known memories", diagnosticsScreen, "Known Memories");
		assertContains("diagnostics screen should expose rite readiness", diagnosticsScreen, "Rite Readiness");
		assertContains("diagnostics screen should use the shared procedural vein background pattern", diagnosticsScreen,
				"renderVeinBackground(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT);");
		assertContains("diagnostics screen should keep the procedural background under a tight vein budget",
				diagnosticsScreen, "private static final int VEIN_COUNT = 10;");
		assertContains("diagnostics screen should draw vein tendrils with a segment stride", diagnosticsScreen,
				"private static final int VEIN_STEP_STRIDE = 4;");
		assertContains("diagnostics screen should precompute static speckles instead of allocating each frame",
				diagnosticsScreen, "private int[][] speckleParams;");
		assertDoesNotContain("diagnostics screen should not rebuild speckle randoms every frame", diagnosticsScreen,
				"Random speckRand = new Random(12345L);");
		assertContains("diagnostics screen should draw animated vein tendrils", diagnosticsScreen,
				"drawVeinTendril(graphics, i, time, gx, gy, gw, gh);");

		assertContains("diagnostics menu should be localized", lang,
				"\"container.hemomancy.scrying_diagnostics\": \"Sanguine Diagnosis\"");
		assertContains("reference docs should describe the advanced diagnostic station", reference,
				"Advanced diagnostic station");
	}

	private static String readSource(String path) throws IOException {
		return read(SOURCE_ROOT.resolve(path));
	}

	private static String readResource(String path) throws IOException {
		return read(RESOURCE_ROOT.resolve(path));
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
