package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * S-7: cooldowns follow two tiers. A persisted cooldown is an entry in the {@code PlayerCooldowns} attachment (or a
 * field of its system's own attachment), never a raw persistent-data long. A session cooldown is a static map keyed
 * by player UUID, so {@code SessionStateRegistryTest} sees it and the hub clears it.
 */
class CooldownTierGuardrailTest {
	private static final Pattern RAW_COOLDOWN = Pattern.compile(
			"getPersistentData\\(\\)\\s*\\.\\s*(?:get|put)Long\\([^;]*(?i:cooldown)");
	private static final Pattern COOLDOWN_MAP = Pattern.compile(
			"static\\s+final\\s+Map<\\s*([\\w.]+)\\s*,[^=;]*>\\s+(\\w*(?i:cooldown)\\w*)\\s*=");

	@Test
	void persistedCooldownsAreNotRawPersistentDataLongs() throws Exception {
		List<String> raw = new ArrayList<>();
		forEachMainSource((shown, text) -> {
			if (RAW_COOLDOWN.matcher(text).find()) raw.add(shown);
		});
		assertEquals(List.of(), raw, "Keep a persisted cooldown in PlayerCooldowns");
	}

	@Test
	void sessionCooldownMapsAreKeyedByPlayerUuid() throws Exception {
		List<String> wrongKey = new ArrayList<>();
		forEachMainSource((shown, text) -> {
			Matcher matcher = COOLDOWN_MAP.matcher(text);
			while (matcher.find()) {
				if (!matcher.group(1).equals("UUID")) wrongKey.add(shown + " " + matcher.group(2) + " keyed by " + matcher.group(1));
			}
		});
		assertEquals(List.of(), wrongKey, "Key a session cooldown map by player UUID");
	}

	private interface SourceVisitor {
		void visit(String shown, String text);
	}

	private static void forEachMainSource(SourceVisitor visitor) throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src/main/java"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				visitor.visit(RepoPaths.display(file), SourceAssertions.read(file));
			}
		}
	}
}
