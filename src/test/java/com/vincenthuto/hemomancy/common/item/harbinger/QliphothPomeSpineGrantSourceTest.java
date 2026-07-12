package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QliphothPomeSpineGrantSourceTest {
	private QliphothPomeSpineGrantSourceTest() {}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/QliphothPomeItem.java"));
		assertContains(source, "QliphothPomeRules.shouldGrantFungalSpine(count, degree.hasFungalSpineGranted())");
		assertContains(source, "degree.setFungalSpineGranted(true)");
		assertContains(source, "new ItemStack(ItemInit.fungal_spine.get())");
	}

	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}
}
