package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MemoryOfVesperItemSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private MemoryOfVesperItemSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/MemoryOfVesperItem.java");
		String recipeInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/RecipeInit.java");

		assertContains("memory use override", source, "InteractionResultHolder<ItemStack> use");
		assertContains("memory directs the player to its graft rite", source,
				"hemomancy.memory_of_vesper.rite_guidance");
		assertNotContains("memory no longer awakens directly", source, "progress.awakenVesperMemory()");
		assertNotContains("memory is consumed by the rite instead of item use", source, "stack.shrink(1)");
		assertNotContains("old vesper staff serializer removed", recipeInit, "vesper_staff_upgrade");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
