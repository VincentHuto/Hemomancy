package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonistMemoryInquiryCoverageTest {
	private static final Pattern MEMORY_ITEM = Pattern.compile("item\\.hemomancy\\.(memory_[a-z_]+)");
	private static final Set<String> MATERIALS = Set.of("memory_thread");
	private static final String INQUIRIES = "data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/";

	@Test
	void everyMemoryHasItsOwnMnemonistInquiry() {
		JsonObject language = ResourceReader.asset("lang/en_us.json");
		Set<String> memories = new TreeSet<>();
		for (String key : language.keySet()) {
			Matcher matcher = MEMORY_ITEM.matcher(key);
			if (matcher.matches() && !MATERIALS.contains(matcher.group(1))) memories.add(matcher.group(1));
		}
		assertFalse(memories.isEmpty(), "no memory item names found");

		List<String> failures = new ArrayList<>();
		Map<String, String> speakers = new HashMap<>();
		for (String memory : memories) {
			String file = INQUIRIES + memory + ".json";
			if (!ResourceReader.exists(file)) {
				failures.add(memory + ": no Mnemonist inquiry file");
				continue;
			}
			JsonObject inquiry = ResourceReader.jsonObject(file);
			if (!inquiry.has("lines") || inquiry.getAsJsonArray("lines").isEmpty()) {
				failures.add(memory + ": no lines");
				continue;
			}
			String own = "hemomancy.mnemonist.item_inquiry." + memory + ".";
			for (JsonElement line : inquiry.getAsJsonArray("lines")) {
				String key = line.getAsString();
				if (!key.startsWith(own)) failures.add(memory + ": speaks another entry's line " + key);
				else if (!language.has(key)) failures.add(memory + ": missing localisation " + key);
				else {
					String text = language.get(key).getAsString();
					String earlier = speakers.putIfAbsent(text, memory);
					if (earlier != null) failures.add(memory + ": repeats a line of " + earlier);
				}
			}
		}
		assertTrue(failures.isEmpty(), () -> String.join("\n", failures));
	}
}
