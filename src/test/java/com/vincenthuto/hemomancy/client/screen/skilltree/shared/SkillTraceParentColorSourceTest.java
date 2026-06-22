package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SkillTraceParentColorSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private SkillTraceParentColorSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String traceCache = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillTraceLayerCache.java");

		assertContains("trace cache derives connector color from the parent skill", traceCache,
				"int branchColor = parentUnlocked ? branchTraceColor(parent) : dimTraceColor(branchTraceColor(parent));");
		assertNotContains("trace cache must not color connectors from the child skill", traceCache,
				"int branchColor = parentUnlocked ? branchTraceColor(sp) : dimTraceColor(branchTraceColor(sp));");
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
