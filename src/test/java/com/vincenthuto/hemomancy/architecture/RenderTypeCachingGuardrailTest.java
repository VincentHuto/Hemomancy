package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 10 (roadmap Phase 8, V-2 / P-9): render types are built once. A render type is created in a static field, in a
 * {@code create*} factory that a {@code SeededRenderTypes} holder calls (it takes the uniforms {@code Supplier}), or in
 * one of the listed lazy singletons; never in a method that builds one per call. School shader clocks live in
 * {@code SchoolRenderTypes}, so no render-type class keeps its own static time field.
 */
class RenderTypeCachingGuardrailTest {
	/** A method returning a render type: its parameter list and the brace that opens its body. */
	private static final Pattern RENDER_TYPE_METHOD = Pattern.compile(
			"\\n[ \\t]*(?:(?:public|protected|private|static|final)\\s+)*(?:[\\w.]+\\.)?RenderType\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
	/** Any other method: its return type, name and parameter list. */
	private static final Pattern OTHER_METHOD = Pattern.compile(
			"\\n[ \\t]*(?:(?:public|protected|private|static|final|synchronized)\\s+)*([\\w.<>\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:throws [\\w., ]+)?\\{");
	private static final Pattern BUILDS = Pattern.compile("(?<!\\w)RenderType\\.create\\(|new RenderType\\(|[^.\\w]create\\(\"");
	private static final Pattern STATIC_CLOCK = Pattern.compile("private static float time;");

	/** Methods that build a type once and keep it: lazy singletons, not per-call factories. */
	private static final Map<String, String> LAZY = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringRenderTypes.java#entity",
			"Called only from veins(), which builds the two vein types once, after the body atlases upload",
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationMaterials.java#renderType",
			"Builds each material's type on first use and keeps it in the enum constant",
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/SomaticLoomSeamRenderer.java#tissue",
			"Builds the seam tissue type on first use and keeps it in a static field",
			"src/main/java/com/vincenthuto/hemomancy/client/render/SchoolRenderTypes.java#create",
			"Called only from the school holders' static field initializers");

	@Test
	void renderTypesAreNotBuiltPerCall() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : clientSources()) {
			String text = SourceAssertions.read(file);
			Matcher method = RENDER_TYPE_METHOD.matcher(text);
			while (method.find()) {
				String params = method.group(2).trim();
				if (params.contains("Supplier<")) continue;
				String body = body(text, method.end() - 1);
				if (!BUILDS.matcher(body).find()) continue;
				String key = RepoPaths.display(file) + "#" + method.group(1);
				if (!LAZY.containsKey(key)) found.add(key + "(" + params + ")");
			}
		}
		assertEquals(Set.of(), found,
				"hold parameterised render types in a SeededRenderTypes (or a static field), not one per call");
	}

	@Test
	void renderMethodsDoNotBuildRenderTypes() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : clientSources()) {
			String text = SourceAssertions.read(file);
			Matcher method = OTHER_METHOD.matcher(text);
			while (method.find()) {
				if (method.group(1).endsWith("RenderType") || method.group(3).contains("Supplier<")) continue;
				if (BUILDS.matcher(body(text, method.end() - 1)).find())
					found.add(RepoPaths.display(file) + "#" + method.group(2));
			}
		}
		assertEquals(Set.of(), found, "a draw asks a holder for its render type instead of building one");
	}

	@Test
	void lazySingletonsStillExist() throws IOException {
		Set<String> present = new TreeSet<>();
		for (Path file : clientSources()) {
			String text = SourceAssertions.read(file);
			Matcher method = RENDER_TYPE_METHOD.matcher(text);
			while (method.find()) present.add(RepoPaths.display(file) + "#" + method.group(1));
		}
		Set<String> stale = new TreeSet<>(LAZY.keySet());
		stale.removeAll(present);
		assertEquals(Set.of(), stale, "remove allow-list entries whose method is gone");
	}

	@Test
	void schoolClocksLiveInSchoolRenderTypes() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : clientSources()) {
			if (STATIC_CLOCK.matcher(SourceAssertions.read(file)).find()) found.add(RepoPaths.display(file));
		}
		assertEquals(Set.of(), found, "keep a SchoolRenderTypes.Clock instead of a static time field");
	}

	private static String body(String text, int open) {
		int depth = 0;
		for (int i = open; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '{') depth++;
			else if (c == '}' && --depth == 0) return text.substring(open, i + 1);
		}
		return text.substring(open);
	}

	private static List<Path> clientSources() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA.resolve("client"))) {
			return files.filter(path -> path.toString().endsWith(".java")).toList();
		}
	}
}
