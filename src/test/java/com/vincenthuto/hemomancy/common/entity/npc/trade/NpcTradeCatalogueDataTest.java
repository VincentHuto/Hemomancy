package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides.Side;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * docs/archive/tracks/npc_trade/05: the shipped catalogues parse, keep the cross-file rules, and every speaker has the lines the
 * Trade box asks for. Item ids are checked against the frozen registry golden for Hemomancy and trusted for vanilla;
 * the live registry check (and the stack-size and component decoding) is {@code NpcTradeGameTests}' job, and the
 * no-want/give-loop rule is {@code NpcTradeGuardrailTest}'s.
 */
class NpcTradeCatalogueDataTest {
	private static final Set<String> GRADERS_THAT_REFUSE = Set.of(NpcTradeAppraisals.DURABILITY,
			NpcTradeAppraisals.TEMPER, NpcTradeAppraisals.SPECIMEN);

	private static final NpcTradeLoader.Environment ENV = new NpcTradeLoader.Environment() {
		private final Set<String> hemomancyItems = frozenItems();

		@Override public boolean itemExists(ResourceLocation item) {
			return item.getNamespace().equals("minecraft") || hemomancyItems.contains(item.toString());
		}
		@Override public boolean isMemoryItem(ResourceLocation item) { return false; }
		@Override public void checkGive(NpcTradeGive give) {}
	};

	@Test
	void everyShippedCardParsesAndKeepsTheCrossFileRules() throws IOException {
		Map<String, List<NpcTradeOffer>> catalogues = catalogues();
		assertFalse(catalogues.isEmpty(), "No offers shipped under data/hemomancy/npc_trade");
		List<NpcTradeOffer> all = catalogues.values().stream().flatMap(List::stream).toList();
		NpcTradeRegistry.Validation validation = NpcTradeRegistry.validate(all, tag -> Set.of(), item -> false);
		assertEquals(List.of(), validation.rejections());
		assertEquals(all.size(), validation.kept().size());
	}

	@Test
	void everyHubSpeakerOutsideTheCourtHasACatalogueAndCourtCardsAreCustomsOfRolesThatTakeThem() throws IOException {
		Map<String, List<NpcTradeOffer>> catalogues = catalogues();
		List<String> missing = new ArrayList<>();
		for (Map.Entry<String, Side> entry : NpcTradeSides.all().entrySet()) {
			String speaker = entry.getKey();
			boolean expected = entry.getValue() != Side.COURT || takesCustom(speaker);
			if (expected && !catalogues.containsKey(speaker)) missing.add(speaker);
			if (!expected) assertFalse(catalogues.containsKey(speaker), speaker + " takes no custom, so it has no card");
		}
		assertEquals(List.of(), missing, "Speakers with no catalogue");
		for (NpcTradeOffer offer : catalogues.values().stream().flatMap(List::stream).toList()) {
			if (offer.side() == Side.COURT) {
				assertTrue(offer.want() instanceof NpcTradeWant.Custom, offer.id() + ": Court cards are customs (Q-1)");
				assertTrue(offer.opens().isPresent(), offer.id() + ": a custom card shows what its standing opens");
			}
			if (offer.side() == Side.CIRCUS || offer.side() == Side.UNSTAINED) {
				assertFalse(offer.requirements().bloodActive(), offer.id() + ": no active-blood gate off the Harbinger side");
			}
		}
	}

	@Test
	void everySpeakerHasItsTradeLines() throws IOException {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		List<String> keys = new ArrayList<>();
		for (Map.Entry<String, List<NpcTradeOffer>> entry : catalogues().entrySet()) {
			String speaker = entry.getKey();
			String base = "hemomancy.trade." + speaker + ".";
			keys.add(base + "thanks");
			List<NpcTradeWant.Items> parts = entry.getValue().stream()
					.flatMap(offer -> offer.want().itemParts().stream()).toList();
			if (parts.stream().anyMatch(part -> NpcTradeAppraisals.spec(part.appraisal().grader())
					.map(NpcTradeAppraisals.Spec::canGradeFine).orElse(false))) keys.add(base + "thanks_fine");
			if (parts.stream().anyMatch(part -> GRADERS_THAT_REFUSE.contains(part.appraisal().grader()))) {
				keys.add(base + "appraise.poor");
			}
		}
		for (Map.Entry<String, Side> entry : NpcTradeSides.all().entrySet()) {
			if (entry.getValue() != Side.UNSTAINED) continue;
			keys.add(NpcTradeSides.refusalKey(entry.getKey()));
			keys.add("hemomancy.trade." + entry.getKey() + ".stained");
		}
		assertEquals(List.of(), keys.stream().filter(key -> !lang.has(key)).toList(), "Add these keys to en_us.json");
	}

	@Test
	void theTradeTagsShipAndTheNeverSoldTagCoversTheProofs() {
		for (String tag : List.of("stained", "curiosity", "never_sold")) {
			assertTrue(ResourceReader.exists("data/hemomancy/tags/item/trade/" + tag + ".json"), tag);
		}
		String neverSold = ResourceReader.text("data/hemomancy/tags/item/trade/never_sold.json");
		for (String item : List.of("hemomancy:scar_blank", "hemomancy:consecrated_copper_ingot",
				"hemomancy:ringmaster_topper", "hemomancy:mycophant_tendril")) {
			assertTrue(neverSold.contains("\"" + item + "\""), item);
		}
	}

	private static boolean takesCustom(String speaker) {
		for (CourtRole role : CourtRole.values()) {
			if (role.entityId().equals(speaker)) return role.offers(CourtRole.Offer.CUSTOM);
		}
		return false;
	}

	private static Map<String, List<NpcTradeOffer>> catalogues() throws IOException {
		Path folder = RepoPaths.DATA.resolve(NpcTradeLoader.FOLDER);
		Map<String, List<NpcTradeOffer>> bySpeaker = new TreeMap<>();
		if (!Files.isDirectory(folder)) return bySpeaker;
		try (Stream<Path> files = Files.walk(folder)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				String relative = folder.relativize(file).toString().replace('\\', '/');
				ResourceLocation id = ResourceLocation.fromNamespaceAndPath("hemomancy",
						relative.substring(0, relative.length() - ".json".length()));
				JsonObject json = ResourceReader.jsonObject(
						RepoPaths.MAIN_RESOURCES.relativize(file).toString().replace('\\', '/'));
				NpcTradeOffer offer = NpcTradeLoader.parse(id, json, ENV);
				bySpeaker.computeIfAbsent(offer.speaker(), speaker -> new ArrayList<>()).add(offer);
			}
		}
		return bySpeaker;
	}

	private static Set<String> frozenItems() {
		try {
			return Files.readAllLines(RepoPaths.resolve("src/gameTest/resources/frozen/registry-ids.txt")).stream()
					.filter(line -> line.startsWith("minecraft:item "))
					.map(line -> line.substring("minecraft:item ".length()).strip())
					.collect(Collectors.toSet());
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
