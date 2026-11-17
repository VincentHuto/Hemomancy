package com.vincenthuto.hemomancy.architecture;

import com.google.gson.JsonElement;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * G-2c / CQ-12: one boss definition. The entity type tag {@code hemomancy:bosses} decides what is a boss and
 * {@code HemoEntityPredicates.isBoss} is the only reader. No code tells a boss by its class name or package. Rite
 * sacrifices and the Scarlet Mummer's attention read the tag; the kill blood gain multiplier is the one named exception
 * and keeps the Ender Dragon and Wither pair (NQ-07-3).
 */
class BossDefinitionGuardrailTest {
	private static final String COMMON = "src/main/java/com/vincenthuto/hemomancy/common/";
	private static final String BOSS_PACKAGE = "com.vincenthuto.hemomancy.common.entity.boss.";
	private static final Pattern CLASS_NAME_CHECK = Pattern.compile(
			"getClass\\(\\)\\s*\\.\\s*get(?:Name|PackageName|SimpleName)\\(\\)[^;]*\\.boss");
	private static final Pattern BOSS_PREDICATE = Pattern.compile("\\bboolean isBoss\\w*\\(");
	private static final Pattern DRAGON_WITHER_PAIR = Pattern.compile(
			"instanceof\\s+(?:[\\w.]+\\.)?EnderDragon\\s*\\|\\|\\s*\\w+\\s+instanceof\\s+(?:[\\w.]+\\.)?WitherBoss");
	private static final Pattern REGISTRATION = Pattern.compile(
			"register\\(\\s*\"([a-z0-9_]+)\"\\s*,\\s*\\(\\)\\s*->\\s*EntityType\\.Builder\\.(?:<[^>]*>)?of\\(\\s*([\\w.]+)::new");
	private static final Pattern IMPORT = Pattern.compile("import\\s+([\\w.]+\\.(\\w+));");

	/** NQ-07-3: the one partial "boss" list kept on purpose, a balance number not widened by the definition. */
	private static final Map<String, String> DRAGON_WITHER_LISTS = Map.of(
			COMMON + "capability/player/harbinger/bloodvolume/BloodVolumeEvents.java",
			"Kill blood gain multiplier: Dragon and Wither only");

	/** Living types under common/entity/boss/ that are not bosses. Empty: every one counts today (NQ-07-2). */
	private static final Set<String> NOT_BOSSES = Set.of();

	@Test
	void hemoEntityPredicatesIsTheOneBossPredicate() throws IOException {
		Map<String, Integer> found = count(BOSS_PREDICATE);
		assertEquals(Map.of(COMMON + "entity/HemoEntityPredicates.java", 1), found,
				"read the boss definition through HemoEntityPredicates.isBoss");
		String helper = SourceAssertions.methodBody(SourceAssertions.readMainJava("common/entity/HemoEntityPredicates.java"),
				"public static boolean isBoss(");
		SourceAssertions.assertContains("isBoss", helper, "EntityInit.BOSSES");
		SourceAssertions.assertNotContains("isBoss", helper, "instanceof");
	}

	@Test
	void noCodeTellsABossByItsClassName() throws IOException {
		assertEquals(Map.of(), count(CLASS_NAME_CHECK), "add the type to hemomancy:bosses instead of a class-name check");
	}

	@Test
	void dragonAndWitherPairOnlyInTheNamedPartialLists() throws IOException {
		Map<String, Integer> expected = new TreeMap<>();
		DRAGON_WITHER_LISTS.keySet().forEach(file -> expected.put(file, 1));
		assertEquals(expected, count(DRAGON_WITHER_PAIR),
				"use HemoEntityPredicates.isBoss, or list a deliberate partial list here with its reason");
	}

	@Test
	void riteSacrificesAndMummerAttentionReadTheBossTag() {
		String sacrifices = SourceAssertions.methodBody(
				SourceAssertions.readMainJava("common/rite/harbinger/HarbingerCardinalRiteEvents.java"),
				"private static void processSacrifices(");
		SourceAssertions.assertContains("processSacrifices", sacrifices, "HemoEntityPredicates.isBoss(entity)");
		Set<String> attention = new TreeSet<>();
		for (JsonElement value : ResourceReader.data("tags/entity_type/puppet_attention_immune.json").getAsJsonArray("values")) {
			attention.add(value.getAsString());
		}
		assertEquals(Set.of("#hemomancy:bosses"), attention, "Scarlet Mummer attention immunity is the boss tag");
	}

	@Test
	void theRingmasterIsABoss() {
		Set<String> tagged = new TreeSet<>();
		for (JsonElement value : ResourceReader.data("tags/entity_type/bosses.json").getAsJsonArray("values")) {
			tagged.add(value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString());
		}
		assertTrue(tagged.contains("hemomancy:circus_ringmaster"), "D-33: the Circus Ringmaster is in hemomancy:bosses");
		SourceAssertions.assertNotContains("ThreadRipperManip",
				SourceAssertions.readMainJava("common/manipulation/ductilis/ThreadRipperManip.java"), "CircusRingmasterEntity");
	}

	@Test
	void everyLivingTypeFromTheBossPackageIsInTheTag() throws Exception {
		Set<String> tagged = new TreeSet<>();
		for (JsonElement value : ResourceReader.data("tags/entity_type/bosses.json").getAsJsonArray("values")) {
			String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
			if (id.startsWith("hemomancy:")) tagged.add(id.substring("hemomancy:".length()));
		}
		Set<String> bossPackageLiving = new TreeSet<>();
		for (Map.Entry<String, String> registration : registrations().entrySet()) {
			String className = registration.getValue();
			if (!className.startsWith(BOSS_PACKAGE)) continue;
			Class<?> type = Class.forName(className, false, getClass().getClassLoader());
			if (net.minecraft.world.entity.LivingEntity.class.isAssignableFrom(type)) bossPackageLiving.add(registration.getKey());
		}
		assertTrue(bossPackageLiving.size() >= 16, "the registration scan found too few boss bodies: " + bossPackageLiving);
		Set<String> expected = new TreeSet<>(bossPackageLiving);
		expected.removeAll(NOT_BOSSES);
		Set<String> taggedFromPackage = new TreeSet<>(tagged);
		taggedFromPackage.retainAll(bossPackageLiving);
		assertEquals(expected, taggedFromPackage, "a living type under common/entity/boss/ joins hemomancy:bosses or NOT_BOSSES");
	}

	private static Map<String, String> registrations() {
		String text = SourceAssertions.readMainJava("common/init/EntityInit.java");
		Map<String, String> imports = new HashMap<>();
		Matcher importMatcher = IMPORT.matcher(text);
		while (importMatcher.find()) imports.put(importMatcher.group(2), importMatcher.group(1));
		Map<String, String> found = new TreeMap<>();
		Matcher matcher = REGISTRATION.matcher(text);
		while (matcher.find()) {
			String name = matcher.group(2);
			found.put(matcher.group(1), name.contains(".") ? name : imports.getOrDefault(name, name));
		}
		return found;
	}

	private static Map<String, Integer> count(Pattern pattern) throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher matcher = pattern.matcher(SourceAssertions.read(file));
				int hits = 0;
				while (matcher.find()) hits++;
				if (hits > 0) found.put(RepoPaths.display(file), hits);
			}
		}
		return found;
	}
}
