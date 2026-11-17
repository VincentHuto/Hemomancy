package com.vincenthuto.hemomancy.common.data;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** UN-70 and UN-18: every shipped sound file has one credits row, with a licence the owner allowed. */
class SoundCreditsTest {
	private static final Path SOUNDS = RepoPaths.ASSETS.resolve("sounds");
	private static final Pattern ROW = Pattern.compile("(?m)^\\| `([^`]+)` \\|([^|]*)\\|([^|]*)\\|([^|]*)\\|([^|]*)\\|\\s*$");
	private static final Set<String> LICENCES = Set.of("CC0 1.0", "Sonniss GDC");
	private static final String UNRECORDED = "source not recorded";

	@Test
	void everySoundFileHasExactlyOneRow() throws IOException {
		TreeSet<String> files = new TreeSet<>();
		try (Stream<Path> walk = Files.walk(SOUNDS)) {
			walk.filter(Files::isRegularFile).forEach(path -> files.add(SOUNDS.relativize(path).toString().replace('\\', '/')));
		}
		TreeMap<String, Integer> rows = new TreeMap<>();
		Matcher row = ROW.matcher(credits());
		while (row.find()) rows.merge(row.group(1), 1, Integer::sum);

		TreeSet<String> missing = new TreeSet<>(files);
		missing.removeAll(rows.keySet());
		assertEquals(new TreeSet<>(), missing, "add a row to docs/SOUND_CREDITS.md for each new sound file");
		TreeSet<String> stale = new TreeSet<>(rows.keySet());
		stale.removeAll(files);
		assertEquals(new TreeSet<>(), stale, "remove the row of a sound file that is gone");
		rows.values().removeIf(count -> count == 1);
		assertEquals(new TreeMap<>(), rows, "one row per file");
	}

	@Test
	void everyRecordedRowNamesItsAuthorLicenceAndLink() {
		TreeMap<String, String> incomplete = new TreeMap<>();
		Matcher row = ROW.matcher(credits());
		while (row.find()) {
			String source = row.group(2).trim();
			String author = row.group(3).trim();
			String licence = row.group(4).trim();
			String link = row.group(5).trim();
			if (source.equals(UNRECORDED)) continue;
			if (source.isEmpty() || !LICENCES.contains(licence)) {
				incomplete.put(row.group(1), "source and an allowed licence (" + LICENCES + ")");
			} else if (licence.equals("CC0 1.0") && (author.isEmpty() || !link.startsWith("https://freesound.org/"))) {
				incomplete.put(row.group(1), "a Freesound author and link");
			}
		}
		assertEquals(new TreeMap<>(), incomplete);
	}

	private static String credits() {
		return SourceAssertions.read(RepoPaths.DOCS.resolve("SOUND_CREDITS.md"));
	}
}
