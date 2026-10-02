package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class CreativeTabTechnicalBlockGuardTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	public static void main(String[] args) throws IOException {
		hiddenBlocksWithNoItemFormAreSkipped();
	}

	private static void hiddenBlocksWithNoItemFormAreSkipped() throws IOException {
		String hemomancy = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/Hemomancy.java"));

		assertContains("creative tab population should skip blocks whose item form is air",
				hemomancy, "asItem() != Items.AIR");
		assertContains("creative tab population should use the shared block-item policy",
				hemomancy, "BlockInit.itemDecision(BuiltInRegistries.BLOCK.getKey(block))");
		assertContains("main creative tab should require the main route",
				hemomancy, "decision.creativeRoute() == BlockInit.CreativeRoute.MAIN");
		String blocks = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		int start = blocks.indexOf("ITEMLESS_BLOCKS = Set.of(");
		int end = blocks.indexOf("WIP_BLOCKS = Set.of(", start);
		if (start < 0 || end < start) throw new AssertionError("Missing itemless block catalog");
		assertContains("shared itemless catalog should retain the abocipher emitter",
				blocks.substring(start, end), "\"abocipher_emitter\"");
		assertContains("itemless policy should also hide the block from creative tabs",
				blocks, "new BlockItemDecision(ItemRoute.NONE, null, CreativeRoute.HIDDEN)");
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
