package com.vincenthuto.hemomancy.common.item.harbinger.tile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SpecimenJarCaptureSourceTest {
	private static final Path SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/SpecimenJarBlockItem.java");

	private SpecimenJarCaptureSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(SOURCE).replace("\r\n", "\n");
		assertContains("single jar capture replaces the hand before the source stack is emptied",
				source, "if (!player.getAbilities().instabuild && stack.getCount() <= 1) {\n\t\t\tplayer.setItemInHand(hand, filled);\n\t\t}");
		assertContains("venom rib centipedes are explicitly capturable as specimen jars",
				source, "target.getType() == EntityInit.venom_rib_centipede.get()");
		assertDoesNotContain("single jar capture must not shrink to empty before setting the filled hand stack",
				source, "stack.shrink(1);\n\t\t}\n\t\tif (stack.isEmpty())");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found forbidden " + forbidden);
		}
	}
}
