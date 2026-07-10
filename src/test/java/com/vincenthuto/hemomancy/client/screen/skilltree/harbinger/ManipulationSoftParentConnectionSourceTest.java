package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ManipulationSoftParentConnectionSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private ManipulationSoftParentConnectionSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String entry = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ManipulationTreeEntry.java");
		String controller = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ManipulationTraceLayerCache.java");

		assertContains("entry keeps soft parents as a separate dependency list",
				entry, "public List<String> getSoftParentNames()");
		assertContains("entry exposes visible connection parents",
				entry, "public List<String> getConnectionParentNames()");
		assertMethodDoesNotContain("visible connection parents must not include soft parents",
				entry, "getConnectionParentNames", "softParentNames");
		assertContains("manipulation tree renderer draws visible connection parents",
				controller, "entry.getConnectionParentNames()");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertMethodDoesNotContain(String label, String text, String methodName, String forbidden) {
		int methodStart = text.indexOf(methodName + "()");
		if (methodStart < 0) {
			throw new AssertionError(label + " (missing method " + methodName + ")");
		}
		int bodyStart = text.indexOf('{', methodStart);
		if (bodyStart < 0) {
			throw new AssertionError(label + " (missing method body for " + methodName + ")");
		}
		int depth = 0;
		for (int i = bodyStart; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					String methodBody = text.substring(bodyStart, i + 1);
					if (methodBody.contains(forbidden)) {
						throw new AssertionError(label + " (method still contains '" + forbidden + "')");
					}
					return;
				}
			}
		}
		throw new AssertionError(label + " (unterminated method body for " + methodName + ")");
	}
}
