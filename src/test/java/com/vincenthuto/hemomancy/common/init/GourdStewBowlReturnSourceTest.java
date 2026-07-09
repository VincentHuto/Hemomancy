package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GourdStewBowlReturnSourceTest {
	private static final Path ITEM_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");

	private GourdStewBowlReturnSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read(ITEM_INIT);
		String gourdStewRegistration = itemInit.substring(itemInit.indexOf("gourd_stew = BASEITEMS.register"),
				itemInit.indexOf("sanguine_formation = BASEITEMS.register"));

		assertContains("gourd stew should keep bowl as crafting remainder", gourdStewRegistration,
				"craftRemainder(Items.BOWL)");
		assertContains("gourd stew should return a bowl after being eaten", gourdStewRegistration,
				"usingConvertsTo(Items.BOWL)");
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
}
