package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verdigris ({@code HemoPalette.VERDIGRIS}, VX-8) is the Unstained copper-green. Blood-side work never takes a copper
 * tone, so only the Unstained files listed here may name it.
 */
class VerdigrisPaletteGuardrailTest {
	private static final Pattern VERDIGRIS = Pattern.compile(
			"HemoPalette\\.VERDIGRIS\\b|import\\s+static\\s+[\\w.]*HemoPalette\\.(?:VERDIGRIS|\\*)\\s*;");

	/** Unstained files that draw in verdigris. */
	private static final Set<String> UNSTAINED = Set.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/UnstainedPodiumBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/unstained/crafting/VerdigrisLatticeBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/unstained/functional/AltarOfCleansingBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/effect/VerdigrisAuraEffect.java",
			"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/animal/VerdigrisMothEntity.java",
			"src/main/java/com/vincenthuto/hemomancy/common/event/ConsecrationHandler.java",
			"src/main/java/com/vincenthuto/hemomancy/common/event/ViridSalisTrailHandler.java",
			"src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/UnstainedRiteParticles.java");

	@Test
	void onlyUnstainedFilesNameVerdigris() throws IOException {
		Set<String> found = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String path = RepoPaths.display(file);
				if (path.endsWith("/common/particle/HemoPalette.java")) continue;
				if (VERDIGRIS.matcher(SourceAssertions.read(file)).find()) found.add(path);
			}
		}
		assertEquals(new TreeSet<>(UNSTAINED), found,
				"verdigris is Unstained only: list an Unstained file here, and keep it off blood-side effects");
	}
}
