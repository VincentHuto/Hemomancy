package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FungalScarLayerToggleSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private FungalScarLayerToggleSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String screen = read("com/vincenthuto/hemomancy/client/screen/tile/functional/SporeImplantScreen.java");
		String renderLayer = read("com/vincenthuto/hemomancy/client/render/layer/player/RenderScarsLayer.java");
		String togglePacket = read("com/vincenthuto/hemomancy/common/network/capa/harbinger/ToggleEquipmentLayerVisibilityPacket.java");
		String syncPacket = read("com/vincenthuto/hemomancy/common/network/capa/harbinger/SyncEquipmentLayerVisibilityPacket.java");
		String equipmentEvents = read("com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java");

		assertContains("spore screen imports toggle packet", screen, "ToggleEquipmentLayerVisibilityPacket");
		assertContains("spore screen sends fungal slot toggle", screen,
				"PacketHandler.sendToServer(new ToggleEquipmentLayerVisibilityPacket(IScars.FUNGAL_SLOT));");
		assertContains("spore screen checks toggle clicks before slots", screen,
				"if (handleLayerToggleClick(mouseX, mouseY, button))");
		assertContains("spore screen renders eye icon", screen, "drawLayerVisibilityIcon");
		assertContains("spore screen reads fungal visibility", screen,
				"this.menu.equipment.isRenderLayerVisible(IScars.FUNGAL_SLOT)");
		assertContains("spore screen uses hide tooltip", screen,
				"screen.hemomancy.scarlet_vanity.layer.hide");
		assertContains("spore screen uses show tooltip", screen,
				"screen.hemomancy.scarlet_vanity.layer.show");

		assertContains("toggle packet permits fungal slot", togglePacket,
				"slot == IScars.FUNGAL_SLOT");
		assertContains("sync packet carries fungal visibility", syncPacket,
				"private final boolean fungalVisible;");
		assertContains("sync packet writes fungal visibility", syncPacket,
				"buf.writeBoolean(msg.fungalVisible);");
		assertContains("sync packet applies fungal visibility", syncPacket,
				"equipment.setRenderLayerVisible(IScars.FUNGAL_SLOT, msg.fungalVisible);");
		assertContains("server sync includes fungal visibility", equipmentEvents,
				"equipment.isRenderLayerVisible(IScars.FUNGAL_SLOT)");

		assertContains("render layer imports scar slot constant", renderLayer,
				"import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;");
		assertContains("render layer checks fungal visibility", renderLayer,
				"equipment.isRenderLayerVisible(IScars.FUNGAL_SLOT)");
		assertContains("render layer skips hidden fungal scar", renderLayer,
				"if (!isFungalScarLayerVisible(player))");
		assertContains("render layer exits before rendering hidden fungal scar", renderLayer, "return;");
	}

	private static String read(String path) throws IOException {
		Path fullPath = SOURCE_ROOT.resolve(path);
		if (!Files.exists(fullPath)) {
			throw new AssertionError("missing " + fullPath);
		}
		return Files.readString(fullPath).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
