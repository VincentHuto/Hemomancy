package com.vincenthuto.hemomancy.common.manipulation.ferric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CellHandFormSwapSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private CellHandFormSwapSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/CellHandFormHelper.java");
		String changePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/ChangeSelectedManipPacket.java");
		String updatePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UpdateCurrentManipPacket.java");
		String conjuration = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/ConjurationManip.java");

		assertContains("cell hand helper recognizes absorption", helper, "ManipulationEquipHelper.BLOOD_ABSORPTION");
		assertContains("cell hand helper recognizes projection", helper, "ManipulationEquipHelper.BLOOD_PROJECTION");
		assertContains("cell hand helper only swaps existing cell hands", helper, "isCellHandForm(held)");
		assertContains("cell hand helper swaps main hand item", helper, "player.setItemInHand(InteractionHand.MAIN_HAND");
		assertContains("cell hand helper maps absorption item", helper, "ItemInit.blood_absorption.get()");
		assertContains("cell hand helper maps projection item", helper, "ItemInit.blood_projection.get()");

		assertContains("cycling selection applies cell hand form swaps", changePacket,
				"CellHandFormHelper.applySelection");
		assertContains("radial selection applies cell hand form swaps", updatePacket,
				"CellHandFormHelper.applySelection");
		assertContains("conjuration selection can hot-swap cell hands before empty-hand summon", conjuration,
				"CellHandFormHelper.applySelection");
		assertContains("conjuration returns after a successful cell hand hot-swap", conjuration,
				"return;");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
