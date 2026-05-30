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
		assertContains("memory requires active blood", source, "volume == null || !volume.isActive()");
		assertContains("memory requires living staff bond", source, "!progress.hasLivingStaffBond()");
		assertContains("memory rejects duplicate awakening", source, "progress.isVesperMemoryAwakened()");
		assertContains("memory awakens player progress", source, "progress.awakenVesperMemory()");
		assertContains("memory consumes only after awakening", source, "stack.shrink(1)");
		assertContains("memory syncs player progress", source, "LivingStaffBondHelper.syncProgress");
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
