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
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * S-1 / step 12: no new {@code getPersistentData()} call outside the documented allow-list. Durable player state goes
 * in an attachment; each file's count in {@code architecture/persistent-data-allow-list.txt} must match exactly, so a
 * migration that removes calls lowers it and nothing adds one silently.
 */
class PersistentDataAllowListTest {
	private static final String ALLOW_LIST = "src/test/resources/architecture/persistent-data-allow-list.txt";
	private static final Pattern CALL = Pattern.compile("\\bgetPersistentData\\(\\)");

	@Test
	void persistentDataCallsMatchTheAllowList() throws Exception {
		Map<String, Integer> allowed = new TreeMap<>();
		for (String line : SourceAssertions.read(ALLOW_LIST).split("\n")) {
			if (line.isBlank() || line.startsWith("#")) continue;
			String[] fields = line.split("\t");
			assertEquals(3, fields.length, "count, file and reason, tab-separated: " + line);
			assertFalse(fields[2].isBlank(), "every allowed file needs a reason: " + line);
			allowed.put(fields[1], Integer.parseInt(fields[0]));
		}

		Map<String, Integer> actual = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src/main/java"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher matcher = CALL.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) actual.put(RepoPaths.display(file), count);
			}
		}

		List<String> drift = new ArrayList<>();
		for (var entry : actual.entrySet()) {
			Integer limit = allowed.get(entry.getKey());
			if (limit == null) {
				drift.add("not allowed: " + entry.getKey() + " (" + entry.getValue() + ")");
			} else if (!limit.equals(entry.getValue())) {
				drift.add(entry.getKey() + ": allowed " + limit + ", found " + entry.getValue());
			}
		}
		for (String file : allowed.keySet()) {
			if (!actual.containsKey(file)) drift.add("stale entry: " + file);
		}
		assertEquals(List.of(), drift, "Keep durable player state in attachments; update " + ALLOW_LIST
				+ " only to lower a count or with an agreed reason");
	}
}
