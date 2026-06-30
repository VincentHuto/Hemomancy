package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScarletVanityLayerToggleSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private ScarletVanityLayerToggleSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String screen = read("com/vincenthuto/hemomancy/client/screen/tile/functional/HarbingerEquipmentScreen.java");
		String packetHandler = read("com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		String togglePacket = read("com/vincenthuto/hemomancy/common/network/capa/harbinger/ToggleEquipmentLayerVisibilityPacket.java");
		String syncPacket = read("com/vincenthuto/hemomancy/common/network/capa/harbinger/SyncEquipmentLayerVisibilityPacket.java");
		String equipmentEvents = read("com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java");
		String gourdLayer = read("com/vincenthuto/hemomancy/client/render/layer/player/BloodGourdLayer.java");
		String jarLayer = read("com/vincenthuto/hemomancy/client/render/layer/player/MorphlingJarLayer.java");
		String charmLayer = read("com/vincenthuto/hemomancy/client/render/layer/player/VascCharmLayer.java");
		String tendrilLayer = read("com/vincenthuto/hemomancy/client/render/layer/player/MycophantTendrilFungalizationLayer.java");

		assertContains("screen imports toggle packet", screen, "ToggleEquipmentLayerVisibilityPacket");
		assertContains("screen sends toggle packet", screen,
				"PacketHandler.sendToServer(new ToggleEquipmentLayerVisibilityPacket(slotIndex));");
		assertContains("screen handles eye clicks before slot interaction", screen,
				"if (handleLayerToggleClick(mouseX, mouseY, button))");
		assertContains("screen uses jar slot constant for icon", screen, "HarbingerEquipmentMenu.JAR_SLOT_INDEX");
		assertContains("screen uses charm slot constant for icon", screen, "HarbingerEquipmentMenu.CHARM_SLOT_INDEX");
		assertContains("screen uses gourd slot constant for icon", screen, "HarbingerEquipmentMenu.GOURD_SLOT_INDEX");
		assertContains("screen draws open eye icon", screen, "drawLayerVisibilityIcon");
		assertContains("screen renders show tooltip", screen,
				"screen.hemomancy.scarlet_vanity.layer.show");
		assertContains("screen renders hide tooltip", screen,
				"screen.hemomancy.scarlet_vanity.layer.hide");

		assertPayload("toggle packet", togglePacket, "ToggleEquipmentLayerVisibilityPacket");
		assertPayload("sync packet", syncPacket, "SyncEquipmentLayerVisibilityPacket");
		assertContains("packet handler registers toggle packet", packetHandler,
				"net.playToServer(ToggleEquipmentLayerVisibilityPacket.TYPE");
		assertContains("packet handler registers sync packet", packetHandler,
				"net.playToClient(SyncEquipmentLayerVisibilityPacket.TYPE");
		assertContains("server fans visibility to tracking clients", equipmentEvents,
				"PacketDistributor.sendToPlayersTrackingEntityAndSelf(player");
		assertContains("server syncs visibility on login", equipmentEvents,
				"syncLayerVisibilityToClient((ServerPlayer) event.getEntity())");
		assertContains("server syncs visibility to new trackers", equipmentEvents,
				"syncLayerVisibilityToPlayer(trackedPlayer, tracker)");

		assertContains("gourd layer checks slot visibility", gourdLayer,
				"inv.isRenderLayerVisible(HarbingerEquipmentMenu.GOURD_SLOT_INDEX)");
		assertContains("jar layer checks slot visibility", jarLayer,
				"inv.isRenderLayerVisible(HarbingerEquipmentMenu.JAR_SLOT_INDEX)");
		assertContains("charm layer checks slot visibility", charmLayer,
				"inv.isRenderLayerVisible(HarbingerEquipmentMenu.CHARM_SLOT_INDEX)");
		assertContains("tendril layer checks charm slot visibility", tendrilLayer,
				"inv.isRenderLayerVisible(HarbingerEquipmentMenu.CHARM_SLOT_INDEX)");
	}

	private static void assertPayload(String label, String source, String className) {
		assertContains(label + " implements payload", source, "implements CustomPacketPayload");
		assertContains(label + " declares type", source, "Type<" + className + "> TYPE");
		assertContains(label + " declares codec", source, "StreamCodec<FriendlyByteBuf, " + className + "> STREAM_CODEC");
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
