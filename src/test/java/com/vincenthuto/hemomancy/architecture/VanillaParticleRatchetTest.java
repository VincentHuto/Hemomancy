package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Hemomancy's effects come from {@code HemoParticleData} and the authored visual toolkit; a vanilla particle is a
 * deliberate fallback for block debris, item crumbs and water, never the default. The files that still name one are
 * listed in {@code architecture/vanilla-particle-allow-list.txt} with an exact count and a reason (cleanup audit
 * AN-12 / A-33): lower a count when a vanilla particle leaves, and never add a file or raise a count.
 */
class VanillaParticleRatchetTest {
	private static final String ALLOW_LIST = "src/test/resources/architecture/vanilla-particle-allow-list.txt";
	private static final Pattern VANILLA = Pattern.compile(
			"(?<![A-Za-z0-9_])(?:ParticleTypes\\.|DustParticleOptions|DustColorTransitionOptions|BlockParticleOption|ItemParticleOption)");
	private static final List<String> DORMANT = List.<String>of();

	@Test
	void vanillaParticlesOnlyGoDown() throws Exception {
		Map<String, Integer> allowed = new TreeMap<>();
		for (String line : SourceAssertions.read(ALLOW_LIST).split("\n")) {
			line = line.strip();
			if (line.isEmpty() || line.startsWith("#")) continue;
			String[] parts = line.split("\t");
			allowed.put(parts[1], Integer.parseInt(parts[0]));
		}
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String relative = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
				if (DORMANT.stream().anyMatch(relative::startsWith)) continue;
				Matcher matcher = VANILLA.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) found.put(RepoPaths.display(file), count);
			}
		}
		List<String> drift = new ArrayList<>();
		for (var entry : found.entrySet()) {
			Integer limit = allowed.get(entry.getKey());
			if (limit == null) drift.add(entry.getKey() + ": " + entry.getValue() + " vanilla particles in a file not on the list");
			else if (!limit.equals(entry.getValue())) drift.add(entry.getKey() + ": listed " + limit + ", found " + entry.getValue());
		}
		for (String listed : allowed.keySet()) {
			if (!found.containsKey(listed)) drift.add(listed + ": listed but clean now; remove the row");
		}
		assertEquals(List.of(), drift, "vanilla particles: use HemoParticleData, or update the allow-list row with its count");
	}
}
