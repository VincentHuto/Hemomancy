package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** docs/archive/tracks/npc_trade/02: the offer schema, its side rules, the give bans and the one-want-per-card rule. */
class NpcTradeLoaderTest {
	private static final Set<String> ITEMS = Set.of("hemomancy:ferric_enzyme", "hemomancy:recycled_enzyme",
			"hemomancy:enzyme_primer", "hemomancy:spore_sac", "hemomancy:memory_blood_shot", "hemomancy:hematic_memory",
			"hemomancy:crude_memory_blood_shot", "hemomancy:memory_thread", "hemomancy:sanguine_athanor_kit",
			"hemomancy:fungal_spine", "minecraft:redstone", "minecraft:iron_sword", "minecraft:copper_ingot",
			"minecraft:bone");

	private static final NpcTradeLoader.Environment ENV = new NpcTradeLoader.Environment() {
		@Override public boolean itemExists(ResourceLocation item) { return ITEMS.contains(item.toString()); }
		@Override public boolean isMemoryItem(ResourceLocation item) { return false; }
		@Override public void checkGive(NpcTradeGive give) {}
	};

	@Test
	void parsesAnItemsWantWithItsGradeAndFineGives() {
		NpcTradeOffer offer = parse("alchemist/worn_blade", """
				{"title": "hemomancy.trade.alchemist.worn_blade.title", "line": "hemomancy.trade.alchemist.worn_blade.line",
				 "order": 20, "requires": {"path": "harbinger", "degree_at_least": 2, "rapport_at_least": 5},
				 "want": {"kind": "items", "ingredient": [{"item": "minecraft:iron_sword"}, {"tag": "minecraft:swords"}],
				          "count": 2, "appraisal": "durability"},
				 "give": [{"item": "minecraft:redstone", "count": 4}],
				 "give_fine": [{"item": "minecraft:redstone", "count": 8}],
				 "stock": {"uses": 4, "restock_days": 3}, "show_locked": true}""");
		assertEquals(rl("hemomancy:alchemist/worn_blade"), offer.id());
		assertEquals("alchemist", offer.speaker());
		assertEquals(NpcTradeSides.Side.HARBINGER, offer.side());
		assertEquals(20, offer.order());
		assertEquals(NpcTradeRequirements.Path.HARBINGER, offer.requirements().path());
		assertEquals(2, offer.requirements().degreeAtLeast());
		assertEquals(5, offer.requirements().rapportAtLeast());
		NpcTradeWant.Items want = assertInstanceOf(NpcTradeWant.Items.class, offer.want());
		assertEquals(List.of(rl("minecraft:iron_sword")), want.match().items());
		assertEquals(List.of(rl("minecraft:swords")), want.match().tags());
		assertEquals(2, want.count());
		assertEquals(NpcTradeAppraisals.DURABILITY, want.appraisal().grader());
		assertEquals(4, offer.give().get(0).count());
		assertEquals(8, offer.giveFine().get(0).count());
		assertEquals(new NpcTradeStock(4, 3), offer.stock());
		assertEquals(1, offer.rapport());
		assertTrue(offer.showLocked());
	}

	@Test
	void parsesBloodBundleAndCustomWants() {
		NpcTradeOffer blood = parse("alchemist/fresh_blood", card("""
				{"kind": "blood", "amount": 250}"""));
		NpcTradeWant.Blood tithe = assertInstanceOf(NpcTradeWant.Blood.class, blood.want());
		assertEquals(250, tithe.amount());
		assertEquals(NpcTradeWant.Blood.TITHE, tithe.source());
		assertTrue(blood.requirements().bloodActive(), "A blood price implies active blood");

		NpcTradeOffer bundle = parse("artificer/kit", card("""
				{"kind": "bundle", "parts": [{"ingredient": {"item": "minecraft:bone"}, "count": 3},
				                             {"kind": "items", "ingredient": {"item": "minecraft:copper_ingot"}}]}"""));
		assertEquals(2, assertInstanceOf(NpcTradeWant.Bundle.class, bundle.want()).parts().size());

		NpcTradeOffer custom = parse("court_gardener/custom", """
				{"title": "hemomancy.trade.court_gardener.custom.title", "line": "hemomancy.trade.court_gardener.custom.line",
				 "opens": "hemomancy.trade.court_gardener.custom.opens",
				 "want": {"kind": "custom", "act": "custom_food"}}""");
		assertEquals("custom_food", assertInstanceOf(NpcTradeWant.Custom.class, custom.want()).act());
		assertEquals(2, custom.rapport(), "A custom earns two rapport by default");
		assertTrue(custom.give().isEmpty());
		assertEquals(Optional.of("hemomancy.trade.court_gardener.custom.opens"), custom.opens());
	}

	@Test
	void rejectsBannedGives() {
		for (String banned : List.of("hemomancy:memory_blood_shot", "hemomancy:hematic_memory",
				"hemomancy:crude_memory_blood_shot", "hemomancy:sanguine_athanor_kit")) {
			assertRejected("alchemist/banned", give(banned), "a trade may not give " + banned);
		}
		parse("alchemist/thread", give("hemomancy:memory_thread"));
	}

	@Test
	void rejectsBloodOffTheHarbingerSideAndCustomOffTheCourt() {
		assertRejected("zealot/blood", card("{\"kind\": \"blood\", \"amount\": 100}"), "blood is Harbinger only");
		assertRejected("circus_ringmaster/blood", card("{\"kind\": \"blood\", \"amount\": 100}"), "blood is Harbinger only");
		assertRejected("alchemist/custom", "{\"title\": \"a.b\", \"line\": \"a.c\", \"want\": {\"kind\": \"custom\", \"act\": \"custom_food\"}}", "custom is Court only");
		assertRejected("court_gardener/wrong_house", "{\"title\": \"a.b\", \"line\": \"a.c\", \"want\": {\"kind\": \"custom\", \"act\": \"custom_catch\"}}", "the custom must be the speaker's house's");
		assertRejected("court_gardener/priced", card(spentEnzyme()), "the Court sets no prices (Q-1)");
	}

	@Test
	void rejectsUnknownSpeakersAndHarbingerGatesOnUnstainedCards() {
		assertRejected("nobody/card", card(spentEnzyme()), "a speaker missing from NpcTradeSides fails");
		assertRejected("zealot/gated", card(spentEnzyme()).replace("\"want\"", "\"requires\": {\"path\": \"harbinger\"}, \"want\""), "Unstained refuse Harbingers");
		assertRejected("zealot/degree", card(spentEnzyme()).replace("\"want\"", "\"requires\": {\"degree_at_least\": 1}, \"want\""), "Unstained refuse Harbingers");
	}

	@Test
	void rejectsEnglishInPlaceOfKeysAndUnknownFields() {
		assertRejected("alchemist/english", card(spentEnzyme()).replace("hemomancy.trade.test.title", "Spent enzyme"), "titles are keys");
		assertRejected("alchemist/typo", card(spentEnzyme()).replace("\"want\"", "\"rapprot\": 3, \"want\""), "unknown fields fail");
		assertRejected("alchemist/grader", card("{\"kind\": \"items\", \"ingredient\": {\"item\": \"minecraft:bone\"}, \"appraisal\": \"vibes\"}"), "unknown graders fail");
		assertRejected("alchemist/fine", card(spentEnzyme()).replace("\"give\"", "\"give_fine\": [{\"item\": \"minecraft:bone\"}], \"give\""), "give_fine needs a grader that can grade fine");
	}

	@Test
	void twoCardsOnOneSpeakerMayNotWantTheSameThing() {
		NpcTradeOffer first = parse("alchemist/a_enzyme", card(spentEnzyme()));
		NpcTradeOffer twin = parse("alchemist/b_enzyme_again", card(spentEnzyme()));
		NpcTradeOffer elsewhere = parse("artificer/enzyme", card(spentEnzyme()));
		NpcTradeRegistry.Validation validation = NpcTradeRegistry.validate(List.of(twin, first, elsewhere), tag -> Set.of(), item -> false);
		assertEquals(Set.of(first.id(), elsewhere.id()), validation.kept().keySet(), "The lexically first card keeps its want; other speakers may want it too");
		assertEquals(1, validation.rejections().size());

		NpcTradeOffer byTag = parse("alchemist/a_tag", card("{\"kind\": \"items\", \"ingredient\": {\"tag\": \"hemomancy:enzymes\"}}"));
		NpcTradeOffer byItem = parse("alchemist/b_item", card("{\"kind\": \"items\", \"ingredient\": {\"item\": \"hemomancy:ferric_enzyme\"}}"));
		assertEquals(2, NpcTradeRegistry.validate(List.of(byTag, byItem), tag -> Set.of(), item -> false).kept().size(),
				"Before tags bind, a tag only clashes with the same tag");
		NpcTradeRegistry.Validation bound = NpcTradeRegistry.validate(List.of(byTag, byItem),
				tag -> tag.equals(rl("hemomancy:enzymes")) ? Set.of(rl("hemomancy:ferric_enzyme")) : Set.of(), item -> false);
		assertEquals(Set.of(byTag.id()), bound.kept().keySet(), "Once tags bind, a tag clashes with its items");

		NpcTradeOffer bloodA = parse("alchemist/blood_a", card("{\"kind\": \"blood\", \"amount\": 100}"));
		NpcTradeOffer bloodB = parse("alchemist/blood_b", card("{\"kind\": \"blood\", \"amount\": 200}"));
		assertEquals(Set.of(bloodA.id()), NpcTradeRegistry.validate(List.of(bloodA, bloodB), tag -> Set.of(), item -> false).kept().keySet());

		NpcTradeOffer overlapping = parse("artificer/overlap", card("""
				{"kind": "bundle", "parts": [{"ingredient": {"tag": "minecraft:bones"}}, {"ingredient": {"item": "minecraft:bone"}}]}"""));
		assertTrue(NpcTradeRegistry.validate(List.of(overlapping), tag -> Set.of(rl("minecraft:bone")), item -> false).kept().isEmpty(),
				"A bundle's parts want different things too");
	}

	@Test
	void neverSoldTagDropsACardOnceTagsBind() {
		NpcTradeOffer spine = parse("alchemist/spine", give("hemomancy:fungal_spine"));
		assertTrue(NpcTradeRegistry.validate(List.of(spine), tag -> Set.of(), rl("hemomancy:fungal_spine")::equals).kept().isEmpty());
	}

	@Test
	void loaderKeepsGoingPastABadFileAndCountsIt() {
		Map<ResourceLocation, Resource> files = new LinkedHashMap<>();
		files.put(rl("hemomancy:npc_trade/alchemist/good.json"), resource(card(spentEnzyme())));
		files.put(rl("hemomancy:npc_trade/alchemist/bad.json"), resource(give("hemomancy:hematic_memory")));
		files.put(rl("hemomancy:npc_trade/nobody/stray.json"), resource(card(spentEnzyme())));
		files.put(rl("hemomancy:npc_trade/zealot/fine.json"), resource(card(spentEnzyme())));
		AtomicReference<Map<ResourceLocation, NpcTradeOffer>> published = new AtomicReference<>();
		AtomicInteger errors = new AtomicInteger(-1);
		NpcTradeLoader loader = new NpcTradeLoader(ENV, new AtomicInteger(), (offers, count) -> {
			published.set(offers);
			errors.set(count);
		});
		PreparableReloadListener.PreparationBarrier now = new PreparableReloadListener.PreparationBarrier() {
			@Override public <T> CompletableFuture<T> wait(T prepared) { return CompletableFuture.completedFuture(prepared); }
		};
		loader.reload(now, new Resources(files), null, null, Runnable::run, Runnable::run).join();
		assertEquals(Set.of(rl("hemomancy:alchemist/good"), rl("hemomancy:zealot/fine")), published.get().keySet());
		assertEquals(2, errors.get());
	}

	@Test
	void durabilityAndRarityGrades() {
		assertEquals(NpcTradeAppraisals.Grade.POOR, NpcTradeAppraisals.durabilityGrade(90, 100));
		assertEquals(NpcTradeAppraisals.Grade.FAIR, NpcTradeAppraisals.durabilityGrade(50, 100));
		assertEquals(NpcTradeAppraisals.Grade.FINE, NpcTradeAppraisals.durabilityGrade(10, 100));
		assertEquals(NpcTradeAppraisals.Grade.FAIR, NpcTradeAppraisals.rarityGrade(net.minecraft.world.item.Rarity.COMMON));
		assertEquals(NpcTradeAppraisals.Grade.FINE, NpcTradeAppraisals.rarityGrade(net.minecraft.world.item.Rarity.RARE));
		assertEquals(NpcTradeAppraisals.Grade.POOR, NpcTradeAppraisals.Grade.FINE.min(NpcTradeAppraisals.Grade.POOR));
		assertFalse(NpcTradeAppraisals.spec(NpcTradeAppraisals.NONE).orElseThrow().canGradeFine());
	}

	@Test
	void tendencyAndSpecimenGrades() {
		EnumBloodTendency ferric = EnumBloodTendency.FERRIC;
		EnumBloodTendency lux = EnumBloodTendency.LUX;
		assertEquals(NpcTradeAppraisals.Grade.POOR, NpcTradeAppraisals.tendencyGrade(List.of(lux), true, ferric));
		assertEquals(NpcTradeAppraisals.Grade.FAIR, NpcTradeAppraisals.tendencyGrade(List.of(lux, ferric), false, ferric));
		assertEquals(NpcTradeAppraisals.Grade.FINE, NpcTradeAppraisals.tendencyGrade(List.of(ferric), true, ferric));
		assertEquals(Optional.of(ferric), NpcTradeAppraisals.tendencyNamed("Ferric"));
		assertEquals(NpcTradeAppraisals.Grade.POOR, NpcTradeAppraisals.specimenGrade(""));
		assertEquals(NpcTradeAppraisals.Grade.FAIR, NpcTradeAppraisals.specimenGrade("minecraft:cow"));
		assertEquals(NpcTradeAppraisals.Grade.FINE, NpcTradeAppraisals.specimenGrade("hemomancy:leech"));
	}

	@Test
	void aTendencyAppraisalNeedsAKnownTendency() {
		String base = "{\"kind\": \"items\", \"ingredient\": {\"item\": \"hemomancy:ferric_enzyme\"}, \"count\": 1, "
				+ "\"appraisal\": \"tendency\"";
		NpcTradeWant.Items want = assertInstanceOf(NpcTradeWant.Items.class,
				parse("alchemist/draw", card(base + ", \"appraisal_tendency\": \"ferric\"}")).want());
		assertEquals(Optional.of("ferric"), want.appraisal().param(NpcTradeAppraisals.TENDENCY_PARAM));
		assertThrows(JsonSyntaxException.class, () -> parse("alchemist/draw", card(base + "}")));
		assertThrows(JsonSyntaxException.class,
				() -> parse("alchemist/draw", card(base + ", \"appraisal_tendency\": \"sanguine\"}")));
	}

	// helpers

	@Test
	void parsesARotationAndACircusAct() {
		NpcTradeOffer daily = parse("alchemist/daily", "{\"title\": \"hemomancy.trade.test.title\", "
				+ "\"line\": \"hemomancy.trade.test.line\", \"want\": " + spentEnzyme()
				+ ", \"give\": [{\"item\": \"hemomancy:enzyme_primer\"}], "
				+ "\"rotation\": {\"pool\": \"alchemist_daily\", \"weight\": 3}}");
		assertEquals(Optional.of(new NpcTradeRotation.Entry("alchemist_daily", 3)), daily.rotation());

		NpcTradeOffer act = parse("circus_ringmaster/spectacle", card("{\"kind\": \"act\", \"act\": \"spectacle\"}"));
		assertEquals(NpcTradeWant.Act.SPECTACLE, assertInstanceOf(NpcTradeWant.Act.class, act.want()).act());
		assertEquals(2, act.rapport(), "an act pays rapport like a custom");
		assertTrue(act.showLocked(), "an act card shows while the player has none banked");
		assertEquals(Optional.of("act:spectacle"), act.want().exclusiveClaim());
	}

	@Test
	void rejectsActsOffTheCircusAndBadRotations() {
		assertRejected("alchemist/act", card("{\"kind\": \"act\", \"act\": \"spectacle\"}"), "acts are Circus only");
		assertRejected("circus_ringmaster/act", card("{\"kind\": \"act\", \"act\": \"juggling\"}"), "unknown act");
		String base = "{\"title\": \"hemomancy.trade.test.title\", \"line\": \"hemomancy.trade.test.line\", "
				+ "\"want\": " + spentEnzyme() + ", \"give\": [{\"item\": \"hemomancy:enzyme_primer\"}], ";
		assertRejected("alchemist/r1", base + "\"rotation\": {\"pool\": \"Daily Pool\"}}", "pool must be an id");
		assertRejected("alchemist/r2", base + "\"rotation\": {\"pool\": \"daily\", \"weight\": 0}}", "weight 1-100");
		assertRejected("alchemist/r3", base + "\"rotation\": {\"pool\": \"daily\", \"odds\": 2}}", "unknown field");
		assertRejected("court_gardener/r4", "{\"title\": \"hemomancy.trade.test.title\", \"line\": "
				+ "\"hemomancy.trade.test.line\", \"want\": {\"kind\": \"custom\", \"act\": \"custom_food\"}, "
				+ "\"rotation\": {\"pool\": \"daily\"}}", "a custom never rotates");
	}

	private static String spentEnzyme() {
		return "{\"kind\": \"items\", \"ingredient\": {\"item\": \"hemomancy:recycled_enzyme\"}, \"count\": 4}";
	}

	private static String card(String want) {
		return "{\"title\": \"hemomancy.trade.test.title\", \"line\": \"hemomancy.trade.test.line\", \"want\": " + want
				+ ", \"give\": [{\"item\": \"hemomancy:enzyme_primer\"}]}";
	}

	private static String give(String item) {
		return "{\"title\": \"hemomancy.trade.test.title\", \"line\": \"hemomancy.trade.test.line\", \"want\": "
				+ spentEnzyme() + ", \"give\": [{\"item\": \"" + item + "\"}]}";
	}

	private static NpcTradeOffer parse(String path, String json) {
		return NpcTradeLoader.parse(rl("hemomancy:" + path), object(json), ENV);
	}

	private static void assertRejected(String path, String json, String why) {
		assertThrows(JsonSyntaxException.class, () -> parse(path, json), why);
	}

	private static JsonObject object(String json) {
		return JsonParser.parseString(json).getAsJsonObject();
	}

	private static ResourceLocation rl(String value) {
		return ResourceLocation.parse(value);
	}

	private static Resource resource(String json) {
		return new Resource(null, () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
	}

	private record Resources(Map<ResourceLocation, Resource> files) implements ResourceManager {
		@Override public Set<String> getNamespaces() { return Set.of("hemomancy"); }
		@Override public Optional<Resource> getResource(ResourceLocation id) { return Optional.ofNullable(files.get(id)); }
		@Override public List<Resource> getResourceStack(ResourceLocation id) { return getResource(id).stream().toList(); }
		@Override public Stream<PackResources> listPacks() { return Stream.empty(); }
		@Override public Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter) {
			return files.entrySet().stream()
					.filter(entry -> entry.getKey().getPath().startsWith(path + "/") && filter.test(entry.getKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		@Override public Map<ResourceLocation, List<Resource>> listResourceStacks(String path, Predicate<ResourceLocation> filter) {
			throw new AssertionError("The loader scans resolved resources");
		}
	}
}
