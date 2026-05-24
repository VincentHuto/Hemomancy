package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodMemoryItemAutoEquipSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private BloodMemoryItemAutoEquipSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java");

		assertContains("blood memory imports manipulation slot helper", source,
				"com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper");
		assertContains("blood memory imports manipulation equip helper", source,
				"com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper");
		assertContains("blood memory computes current player slot count", source,
				"ManipSlotHelper.getMaxSlots(playerIn)");
		assertContains("blood memory counts normal slots before auto-equip", source,
				"ManipulationEquipHelper.countNormalEquippedNames(");
		assertContains("blood memory counts current equipped names", source,
				"known.getEquippedManipNames())");
		assertContains("blood memory treats fixed mechanics separately from normal slots", source,
				"ManipulationEquipHelper.isFixedMechanicalManip(manipulation.getName())");
		assertContains("blood memory attempts to equip newly learned memory", source,
				"known.equipManip(manipulation.getName(), maxSlots)");
		assertContains("blood memory syncs learned and equipped state", source,
				"PacketHandler.sendToPlayer((ServerPlayer) playerIn, new KnownManipulationServerPacket(known));");
		assertContains("blood memory tells player when auto-equipped", source,
				"Memorized and equipped: ");
		assertContains("blood memory tells player when reliquary is needed", source,
				"Use a Mnemonic Reliquary to change equipped memories.");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
