package com.vincenthuto.hemomancy.common.creative;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FerventChitiniteCreativeTabSourceTest {

	private static final Path HEMOMANCY = Path.of("src/main/java/com/vincenthuto/hemomancy/Hemomancy.java");

	@Test
	void ferventChitiniteItemsAreInTheMainTabBesideRegularChitiniteItems() throws Exception {
		String source = Files.readString(HEMOMANCY).replace("\r\n", "\n");
		String wipContents = source.substring(source.indexOf("private static void acceptWipCreativeTabContents"),
				source.indexOf("private static boolean shouldShowItemInCreativeTab"));

		assertFalse(wipContents.contains("ItemInit.fervent_husk.get()"));
		int regularHusk = source.indexOf("if (item.get() == ItemInit.chitinous_husk.get())");
		int ferventHusk = source.indexOf("populator.accept(ItemInit.fervent_husk.get())", regularHusk);
		assertTrue(regularHusk >= 0 && ferventHusk > regularHusk);
		assertFalse(source.substring(source.indexOf("private static boolean isWipItem"))
				.contains("item == ItemInit.fervent_husk.get()"));
	}
}
