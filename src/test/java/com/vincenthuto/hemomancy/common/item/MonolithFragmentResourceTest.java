package com.vincenthuto.hemomancy.common.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MonolithFragmentResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private MonolithFragmentResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String monolithBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineMonolithBlock.java"));
		String fragmentItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/MonolithFragmentItem.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/MonolithFragmentItemRenderer.java"));
		String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/monolith_fragment.json"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String tag = read(RESOURCE_ROOT.resolve("data/hemomancy/tags/item/progression/monolith_fragments.json"));

		assertContains("item registry includes monolith fragments", itemInit,
				"monolith_fragment = BASEITEMS.register(\"monolith_fragment\"");
		assertContains("monolith fragments use their custom item class", itemInit,
				"new MonolithFragmentItem(new Item.Properties().rarity(Rarity.RARE).fireResistant())");
		assertContains("shattering drops monolith fragments", monolithBlock,
				"new ItemStack(ItemInit.monolith_fragment.get(), MonolithFragmentDropRules.rollFragmentCount(worldIn.random))");
		assertContains("fragment drop happens alongside qliphoth seed", monolithBlock,
				"popResource(worldIn, pos.above(), new ItemStack(ItemInit.qliphoth_seed.get()));");
		assertContains("fragment item is stackable through default item properties", fragmentItem,
				"extends Item implements HemoClientItemExtensionsProvider");
		assertContains("fragment item supplies custom renderer", fragmentItem,
				"return new MonolithFragmentItemRenderer(");
		assertContains("fragment model delegates to custom renderer", model,
				"\"parent\": \"builtin/entity\"");
		assertContains("fragment model has a valid particle texture", model,
				"\"particle\": \"hemomancy:item/qliphoth_pome\"");
		assertContains("fragment renderer emits triangular planes", renderer,
				"drawTriangle(");
		assertContains("fragment renderer keeps the item pitch black", renderer,
				"private static final int BLACK = 0xFF000000;");
		assertContains("fragment renderer animates unstable shape changes", renderer,
				"morphPhase");
		assertContains("fragment display name is localized", lang,
				"\"item.hemomancy.monolith_fragment\": \"Monolith Fragment\"");
		assertContains("fragment tag exposes late-game recipe hook", tag,
				"\"hemomancy:monolith_fragment\"");
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
