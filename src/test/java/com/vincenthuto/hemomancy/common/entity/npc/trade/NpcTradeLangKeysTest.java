package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Every key a shipped want card shows (title, line, opens), every lock reason, grade,
 * trade reply, rapport tier, act and the tithe's blood-source label is in en_us.json. */
class NpcTradeLangKeysTest {
	@Test
	void everyShippedTradeKeyIsInEnglish() throws IOException {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		List<String> keys = new ArrayList<>(NpcTradeRequirements.LOCK_KEYS);
		for (NpcTradeAppraisals.Grade grade : NpcTradeAppraisals.Grade.values()) keys.add(grade.key());
		keys.addAll(NpcTradeService.REASON_KEYS);
		for (int tier = NpcTradeRules.STRANGER; tier <= NpcTradeRules.CONFIDANT; tier++) keys.add(NpcTradeRules.tierKey(tier));
		keys.add(NpcTradeSources.TITHE.label());
		for (String act : NpcTradeWant.Act.ACTS) {
			keys.add(new NpcTradeWant.Act(act).lockKey());
			keys.add("hemomancy.trade.ui.act." + act);
			keys.add("hemomancy.trade.ui.act." + act + ".hint");
		}
		Path folder = RepoPaths.DATA.resolve(NpcTradeLoader.FOLDER);
		if (Files.isDirectory(folder)) {
			try (Stream<Path> files = Files.walk(folder)) {
				for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
					JsonObject offer = ResourceReader.jsonObject(RepoPaths.MAIN_RESOURCES.relativize(file).toString().replace('\\', '/'));
					for (String field : List.of("title", "line", "opens")) {
						if (offer.has(field)) keys.add(offer.get(field).getAsString());
					}
				}
			}
		}
		assertEquals(List.of(), keys.stream().filter(key -> !lang.has(key)).toList(), "Add these keys to en_us.json");
	}
}
