package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 11 (roadmap Phase 10, L-1): what a manipulation does when an entity casts it is attached at registration
 * with {@code setEntityCast(EntityCasts::x)}, like its Drudge action; {@code EntityManipulationEffects} only applies
 * the shared rules and runs the attached cast. No name switch decides it again.
 */
class EntityCastGuardrailTest {
	private static final Pattern CAST_METHOD = Pattern.compile("public static boolean (\\w+)\\(ManipulationCastContext context\\)");
	private static final Pattern ATTACHED = Pattern.compile("\\.setEntityCast\\(EntityCasts::(\\w+)\\)");

	@Test
	void theEntryPointHasNoNameSwitch() {
		String entry = SourceAssertions.readMainJava("common/manipulation/EntityManipulationEffects.java");
		SourceAssertions.assertNotContains("no dispatch on the manipulation's name", entry, "getName()");
		SourceAssertions.assertNotContains("no switch", entry, "switch");
		assertEquals(false, Pattern.compile("\"[a-z_]+\"").matcher(entry).find(),
				"EntityManipulationEffects names no manipulation; attach a cast in ManipulationInit instead");
	}

	@Test
	void everyEntityCastIsAttachedExactlyOnce() {
		Set<String> casts = new TreeSet<>();
		Matcher method = CAST_METHOD.matcher(SourceAssertions.readMainJava("common/manipulation/EntityCasts.java"));
		while (method.find()) casts.add(method.group(1));
		TreeMap<String, Integer> attached = new TreeMap<>();
		Matcher hook = ATTACHED.matcher(SourceAssertions.readMainJava("common/init/ManipulationInit.java"));
		while (hook.find()) attached.merge(hook.group(1), 1, Integer::sum);
		TreeMap<String, Integer> expected = new TreeMap<>();
		for (String cast : casts) expected.put(cast, 1);
		assertEquals(expected, attached, "attach each EntityCasts method to exactly one manipulation in ManipulationInit");
	}

	/** Each cast is named for the manipulation it is attached to, so a swapped hook cannot pass unnoticed. */
	@Test
	void eachCastIsAttachedToTheManipulationItIsNamedFor() {
		String init = SourceAssertions.readMainJava("common/init/ManipulationInit.java");
		Matcher entry = Pattern.compile("MANIPS\\.register\\(\"(\\w+)\"(.*?)(?=MANIPS\\.register\\(|$)", Pattern.DOTALL)
				.matcher(init);
		TreeMap<String, String> mismatched = new TreeMap<>();
		while (entry.find()) {
			Matcher hook = ATTACHED.matcher(entry.group(2));
			if (hook.find() && !hook.group(1).equals(camel(entry.group(1)))) mismatched.put(entry.group(1), hook.group(1));
		}
		assertEquals(new TreeMap<>(), mismatched, "attach EntityCasts::<camelCaseName> to the manipulation of that name");
	}

	/** Only the entry point runs a cast, so paralysis and school damage always apply first. */
	@Test
	void onlyTheEntryPointRunsACast() throws java.io.IOException {
		TreeSet<String> readers = new TreeSet<>();
		try (var files = java.nio.file.Files.walk(com.vincenthuto.hemomancy.support.RepoPaths.MAIN_JAVA)) {
			for (var file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (SourceAssertions.read(file).contains(".getEntityCast()")) {
					readers.add(com.vincenthuto.hemomancy.support.RepoPaths.display(file));
				}
			}
		}
		assertEquals(Set.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityManipulationEffects.java"),
				readers, "cast through EntityManipulationEffects.cast");
	}

	private static String camel(String snake) {
		StringBuilder out = new StringBuilder();
		boolean upper = false;
		for (char c : snake.toCharArray()) {
			if (c == '_') {
				upper = true;
			} else {
				out.append(upper ? Character.toUpperCase(c) : c);
				upper = false;
			}
		}
		return out.toString();
	}
}
