package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeLoader;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * docs/archive/tracks/npc_trade/06: every rapport lore line is written, listed in the lore golden, and belongs to a speaker the
 * player can actually earn rapport with (a known side and shipped want cards).
 */
class DialogueRapportLoreTest {
	@Test
	void everyLineHasItsQuestionTeaserAndAnswerInEnglish() throws IOException {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		List<String> missing = new ArrayList<>();
		for (List<DialogueRapportLore.Line> lines : DialogueRapportLore.all().values()) {
			for (DialogueRapportLore.Line line : lines) {
				List<String> keys = new ArrayList<>(List.of(line.textKey(), line.teaserKey()));
				keys.addAll(line.lineKeys());
				keys.stream().filter(key -> !lang.has(key)).forEach(missing::add);
				assertTrue(!lang.has(line.textKey() + ".line" + (line.lines() + 1)),
						line.textKey() + " has more answer lines in en_us.json than the table reads");
			}
		}
		assertEquals(List.of(), missing, "Add these keys to en_us.json");
	}

	@Test
	void everyLineIsInTheLoreGolden() throws IOException {
		Set<String> golden = new TreeSet<>();
		for (String line : Files.readAllLines(RepoPaths.resolve("src/test/resources/dialogue/lore-options.txt"))) {
			if (!line.isBlank() && !line.startsWith("#")) golden.add(line.strip());
		}
		Set<String> missing = new TreeSet<>();
		DialogueRapportLore.all().forEach((speaker, lines) -> lines.forEach(line -> {
			String entry = speaker + " " + line.textKey();
			if (!golden.contains(entry)) missing.add(entry);
		}));
		assertEquals(Set.of(), missing, "add these to lore-options.txt");
	}

	@Test
	void eachSpeakerHasAKnownThenATrustedLineAndCardsToEarnThemWith() {
		DialogueRapportLore.all().forEach((speaker, lines) -> {
			assertTrue(NpcTradeSides.side(speaker).isPresent(), speaker + " is not in NpcTradeSides");
			assertTrue(Files.isDirectory(RepoPaths.DATA.resolve(NpcTradeLoader.FOLDER).resolve(speaker)),
					speaker + " has no want cards, so its rapport can't rise");
			assertEquals(List.of(NpcTradeRules.KNOWN, NpcTradeRules.TRUSTED),
					lines.stream().map(DialogueRapportLore.Line::tier).toList(), speaker);
			assertEquals(lines.size(), lines.stream().map(DialogueRapportLore.Line::nodeId).distinct().count());
		});
	}

	@Test
	void rapportLinesAreLoreOptionsGatedOnTheirTier() {
		DialogueTree base = DialogueTree.builder("speaker", null, 1)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of()))
				.build();
		DialogueTree tree = DialogueRapportLore.with(base, "voyager", NpcTradeRules.KNOWN);
		List<DialogueOption> options = tree.getStartNode().options();
		assertEquals(2, options.size());
		for (int i = 0; i < options.size(); i++) {
			DialogueRapportLore.Line line = DialogueRapportLore.lines("voyager").get(i);
			assertTrue(options.get(i).presentation().lore());
			assertEquals(line.tier(), options.get(i).presentation().rapportTier());
		}
		assertTrue(tree.getNode(DialogueRapportLore.lines("voyager").get(0).nodeId()) != null);
		assertEquals(null, tree.getNode(DialogueRapportLore.lines("voyager").get(1).nodeId()));
		assertEquals(base, DialogueRapportLore.with(base, "court_arbor", NpcTradeRules.CONFIDANT), "no lines, no change");
	}
}
