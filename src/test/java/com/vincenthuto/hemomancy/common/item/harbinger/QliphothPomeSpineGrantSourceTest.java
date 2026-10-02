package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QliphothPomeSpineGrantSourceTest {
	private QliphothPomeSpineGrantSourceTest() {}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/QliphothPomeItem.java"));
		String delivery = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/QliphothBloomEvents.java"));
		String bloom = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/QliphothBloomBlock.java"));
		assertContains(bloom, "Spine: visit the Gardens. Lost it? Sneak-use empty-handed.");
		assertContains(source, "QliphothPomeRules.shouldGrantFungalSpine(count, degree.hasFungalSpineGranted())");
		assertContains(source, "QliphothBloomEvents.deliverPendingFungalSpine(player)");
		assertContains(delivery, "degree.isQliphothCommunionDone() || degree.hasFungalSpineGranted()");
		assertContains(delivery, "player.getInventory().add(new ItemStack(ItemInit.fungal_spine.get()))");
		assertContains(delivery, "degree.setFungalSpineGranted(true)");
		assertContains(delivery, "deliverPendingFungalSpine(player);");
		if (source.contains("player.drop(spine")) throw new AssertionError("unique Spine must not become a world drop");
	}

	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}
}
