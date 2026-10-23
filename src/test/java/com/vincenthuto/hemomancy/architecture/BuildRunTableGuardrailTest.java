package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * T-6: build.gradle declares only the base runs. Review, observer and profile clients are rows in
 * {@code gradle/review-clients.gradle}, and GameTest servers rows in {@code gradle/game-tests.gradle}; both tables
 * feed one factory each.
 */
class BuildRunTableGuardrailTest {
	private static final Pattern RUN_BLOCK = Pattern.compile("(?m)^        (\\w+) \\{");

	@Test
	void buildGradleDeclaresOnlyTheBaseRuns() {
		String build = SourceAssertions.read("build.gradle");
		String runs = SourceAssertions.section(build, "    runs {", "    mods {");
		List<String> declared = RUN_BLOCK.matcher(runs).results().map(match -> match.group(1)).toList();
		assertEquals(List.of("client", "alphaJourneyClient", "server", "data", "configureEach"), declared);
		assertFalse(build.contains("runs.create(") || build.contains("runs.maybeCreate("),
				"Add a run as a table row in gradle/review-clients.gradle or gradle/game-tests.gradle");
	}

	@Test
	void bothRunTablesAreApplied() {
		String build = SourceAssertions.read("build.gradle");
		for (String table : List.of("gradle/game-tests.gradle", "gradle/review-clients.gradle")) {
			assertEquals(1, build.split(Pattern.quote("apply from: '" + table + "'"), -1).length - 1, table);
		}
	}
}
