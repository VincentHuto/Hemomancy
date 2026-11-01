package com.vincenthuto.hemomancy.architecture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeGiveRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides.Side;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NPC trades (docs/npc_trade): offers are data under {@code data/hemomancy/npc_trade/<speaker>/}, run by
 * {@code NpcTradeService} from {@code NpcTradeRequestPacket}. A trade is never a {@code DialogueEvent}, never sells a
 * memory, a station kit or a progression proof, keeps each price kind on its side, and only the trade classes write
 * the ledger.
 */
class NpcTradeGuardrailTest {
	private static final Path OFFERS = RepoPaths.DATA.resolve("npc_trade");
	private static final Path TRADE = RepoPaths.mainJava("common/entity/npc/trade");
	private static final Pattern LEDGER_WRITE =
			Pattern.compile("\\.(addRapport|recordUse|spendAct|recordAct|markRotationSeen)\\(");
	/** Who may write each part of {@code NpcTradeLedger}: the transaction, act banking and the seen mark. */
	private static final Map<String, Set<String>> LEDGER_WRITERS = Map.of(
			"common/entity/npc/trade/NpcTradeService.java", Set.of("addRapport", "recordUse", "spendAct"),
			"common/entity/npc/trade/NpcTradeActs.java", Set.of("recordAct"),
			"common/entity/npc/trade/NpcTradeCards.java", Set.of("markRotationSeen"));
	/** The price kinds a side may ask for (Q-1, Q-3; the act is the Circus's spectacle). */
	private static final Map<String, Side> SIDE_ONLY_KINDS = Map.of(
			"blood", Side.HARBINGER, "custom", Side.COURT, "act", Side.CIRCUS);

	private record Card(String speaker, String name, JsonObject json) {
		String id() {
			return speaker + "/" + name;
		}
	}

	@Test
	void noCardSellsAMemoryAKitOrANeverSoldItem() throws IOException {
		Set<String> neverSold = new TreeSet<>();
		for (JsonElement value : ResourceReader.data("tags/item/trade/never_sold.json").getAsJsonArray("values")) {
			neverSold.add(value.getAsString());
		}
		List<String> banned = new ArrayList<>();
		for (Card card : cards()) {
			for (String item : gives(card.json())) {
				ResourceLocation id = ResourceLocation.parse(item);
				if (NpcTradeGiveRules.isBannedById(id) || neverSold.contains(item)) banned.add(card.id() + " gives " + item);
			}
		}
		assertEquals(List.of(), banned, "Trades never grant progression (docs/npc_trade/00)");
	}

	@Test
	void noSpeakerGivesWhatAnotherOfItsCardsWants() throws IOException {
		Map<String, Set<String>> wanted = new TreeMap<>();
		for (Card card : cards()) {
			wanted.computeIfAbsent(card.speaker(), speaker -> new TreeSet<>()).addAll(itemsIn(card.json().get("want")));
		}
		List<String> loops = new ArrayList<>();
		for (Card card : cards()) {
			for (String item : gives(card.json())) {
				if (wanted.get(card.speaker()).contains(item)) loops.add(card.id() + " gives " + item);
			}
		}
		assertEquals(List.of(), loops, "no item is both a want and a give at one NPC");
	}

	@Test
	void everySpeakerFolderHasASideAndPriceKindsStayOnTheirSide() throws IOException {
		List<String> problems = new ArrayList<>();
		try (Stream<Path> folders = Files.list(OFFERS)) {
			for (Path folder : folders.filter(Files::isDirectory).toList()) {
				String speaker = folder.getFileName().toString();
				if (NpcTradeSides.side(speaker).isEmpty()) problems.add(speaker + " has no NpcTradeSides row");
			}
		}
		for (Card card : cards()) {
			String kind = card.json().getAsJsonObject("want").get("kind").getAsString();
			Side only = SIDE_ONLY_KINDS.get(kind);
			if (only != null && !NpcTradeSides.is(card.speaker(), only)) {
				problems.add(card.id() + " asks a " + kind + " price off the " + only + " side");
			}
		}
		assertEquals(List.of(), problems);
	}

	@Test
	void theUnstainedRefusalIsCheckedWhereTheBoardIsBuiltAndWhereATradeRuns() {
		String cards = SourceAssertions.read(TRADE.resolve("NpcTradeCards.java"));
		SourceAssertions.assertContains("the board refuses before any card",
				SourceAssertions.methodBody(cards, "public static NpcTradeBoard build(ServerPlayer player"),
				"NpcTradeSides.refuses(speaker, player)");
		String service = SourceAssertions.read(TRADE.resolve("NpcTradeService.java"));
		SourceAssertions.assertContains("attempt runs the transaction with the real player",
				SourceAssertions.methodBody(service, "public static void attempt("), "new PlayerTradeParty(player, entity)");
		SourceAssertions.assertContains("the transaction refuses first",
				SourceAssertions.methodBody(service, "public static Result run("), "if (party.refusedBySide(speaker))");
		SourceAssertions.assertContains("the real party asks the side table",
				SourceAssertions.read(TRADE.resolve("PlayerTradeParty.java")), "return NpcTradeSides.refuses(speakerId, player);");
	}

	@Test
	void onlyTheTradeClassesWriteTheLedger() throws IOException {
		Map<String, Set<String>> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String source = SourceAssertions.read(file);
				if (file.getFileName().toString().equals("NpcTradeLedger.java")) continue;
				// recordUse and recordAct are also station-practice and Court-standing methods; only files that
				// reach the trade ledger count.
				if (!source.contains("NpcTradeLedger") && !source.contains("npcTrades(")) continue;
				Matcher matcher = LEDGER_WRITE.matcher(source);
				while (matcher.find()) {
					String relative = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
					found.computeIfAbsent(relative, key -> new TreeSet<>()).add(matcher.group(1));
				}
			}
		}
		assertEquals(LEDGER_WRITERS, found, "change rapport, stock, acts or the seen mark through the trade classes");
	}

	@Test
	void tradesAreNeverDialogueEvents() throws IOException {
		List<String> offenders = new ArrayList<>();
		for (Path root : List.of(TRADE, RepoPaths.mainJava("common/network/trade"))) {
			try (Stream<Path> files = Files.walk(root)) {
				for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
					if (SourceAssertions.read(file).contains("DialogueEvent")) offenders.add(RepoPaths.display(file));
				}
			}
		}
		String routes = SourceAssertions.readDialogueRoutes();
		if (routes.contains("NpcTrade")) offenders.add("a dialogue route names a trade class");
		assertEquals(List.of(), offenders, "a trade runs from NpcTradeRequestPacket, never a dialogue event");
	}

	private static List<Card> cards() throws IOException {
		List<Card> cards = new ArrayList<>();
		try (Stream<Path> files = Files.walk(OFFERS)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				Path relative = OFFERS.relativize(file);
				String name = relative.getFileName().toString().replace(".json", "");
				cards.add(new Card(relative.getName(0).toString(), name,
						JsonParser.parseString(SourceAssertions.read(file)).getAsJsonObject()));
			}
		}
		return cards;
	}

	private static Set<String> gives(JsonObject card) {
		Set<String> items = new TreeSet<>();
		for (String field : List.of("give", "give_fine")) {
			if (card.has(field)) items.addAll(itemsIn(card.get(field)));
		}
		return items;
	}

	/** Every {@code "item"} value inside {@code element}, at any depth. */
	private static Set<String> itemsIn(JsonElement element) {
		Set<String> items = new TreeSet<>();
		if (element == null) return items;
		if (element.isJsonArray()) {
			for (JsonElement child : element.getAsJsonArray()) items.addAll(itemsIn(child));
		} else if (element.isJsonObject()) {
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
				if (entry.getKey().equals("item") && entry.getValue().isJsonPrimitive()) {
					items.add(entry.getValue().getAsString());
				} else {
					items.addAll(itemsIn(entry.getValue()));
				}
			}
		}
		return items;
	}
}
