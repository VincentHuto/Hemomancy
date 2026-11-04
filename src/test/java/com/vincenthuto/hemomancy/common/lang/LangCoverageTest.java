package com.vincenthuto.hemomancy.common.lang;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What the game shows has a key, and every value is English a player can read (cleanup audit RS-2, RS-3, RS-4,
 * BK-10, BK-13, BK-23 / A-20): every damage type has both death messages, every biome and manipulation has a name
 * and every manipulation a description, no value carries a format other than {@code %s} (Minecraft shows anything
 * else raw), and no value is itself a translation key.
 */
class LangCoverageTest {
	/** A numeric conversion ({@code %d}, {@code %.0f}, ...); a lone {@code %} in prose is fine. */
	private static final Pattern BAD_FORMAT = Pattern.compile("%(?:\\d+\\$)?(?:\\.\\d+)?[dfiouxXeEgG]\\b");
	private static final Pattern LOOKS_LIKE_KEY = Pattern.compile("^[a-z0-9_]+(?:\\.[a-z0-9_]+){2,}$");
	private static final Pattern MANIPULATION_ID = Pattern.compile("MANIPS\\.register\\(\"([a-z_0-9]+)\"");

	@Test
	void everyDamageTypeAndBiomeHasAName() throws Exception {
		JsonObject lang = lang();
		TreeSet<String> missing = new TreeSet<>();
		for (String id : ids("damage_type")) {
			for (String suffix : List.of("", ".player")) {
				if (!lang.has("death.attack." + id + suffix)) missing.add("death.attack." + id + suffix);
			}
		}
		for (String id : ids("worldgen/biome")) {
			if (!lang.has("biome.hemomancy." + id)) missing.add("biome.hemomancy." + id);
		}
		assertEquals(new TreeSet<>(), missing, "missing lang keys");
	}

	@Test
	void everyManipulationHasANameAndADescription() {
		JsonObject lang = lang();
		TreeSet<String> missing = new TreeSet<>();
		Matcher matcher = MANIPULATION_ID.matcher(SourceAssertions.readMainJava("common/init/ManipulationInit.java"));
		while (matcher.find()) {
			String id = matcher.group(1);
			for (String key : List.of("hemomancy.manipulation." + id, "hemomancy.manipulation." + id + ".desc")) {
				if (!lang.has(key)) missing.add(key);
			}
		}
		assertEquals(new TreeSet<>(), missing, "missing manipulation keys");
	}

	@Test
	void keysTheGameShowsExist() {
		JsonObject lang = lang();
		TreeSet<String> missing = new TreeSet<>();
		for (String key : List.of("hemomancy.consecration.not_ready", "hemomancy.consecration.success",
				"hemomancy.jar.empty", "advancements.hemomancy.first_bloodcraft_reward_claimed.title",
				"advancements.hemomancy.first_bloodcraft_reward_claimed.description",
				"fluid_type.hemomancy.white_humor", "block.hemomancy.white_humor_block",
				"entity.hemomancy.gore_wound_harpoon", "block.minecraft.banner.hemomancy_heart.light_blue",
				"block.minecraft.banner.hemomancy_heart.light_gray", "block.minecraft.banner.hemomancy_veins.light_blue",
				"block.minecraft.banner.hemomancy_veins.light_gray", "item.hemomancy.chitinite_arm_banner.light_blue",
				"item.hemomancy.chitinite_arm_banner.light_gray")) {
			if (!lang.has(key)) missing.add(key);
		}
		assertEquals(new TreeSet<>(), missing, "missing lang keys");
	}

	@Test
	void valuesAreReadableEnglish() {
		JsonObject lang = lang();
		List<String> wrong = new ArrayList<>();
		for (Map.Entry<String, com.google.gson.JsonElement> entry : lang.entrySet()) {
			String value = entry.getValue().getAsString();
			if (BAD_FORMAT.matcher(value).find()) wrong.add(entry.getKey() + " uses a format other than %s");
			if (LOOKS_LIKE_KEY.matcher(value).matches()) wrong.add(entry.getKey() + " is a key, not English");
		}
		assertEquals(List.of(), wrong);
	}

	private static JsonObject lang() {
		return ResourceReader.asset("lang/en_us.json");
	}

	private static List<String> ids(String dataFolder) throws Exception {
		List<String> ids = new ArrayList<>();
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve(dataFolder))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				String name = file.getFileName().toString();
				ids.add(name.substring(0, name.length() - ".json".length()));
			}
		}
		return ids;
	}
}
