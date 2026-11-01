package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The loaded want cards, server side. {@link NpcTradeLoader} installs what parsed; the cross-file rules run here:
 * two cards on one speaker may not want the same thing (an item, a tag, blood, a custom), and no card may give an
 * item tagged {@link NpcTradeGiveRules#NEVER_SOLD}. Tags aren't bound while data loads, so the rules run once on
 * install with tags unexpanded and again from {@link NpcTradeTagEvents} once they are. Of two clashing cards the
 * lexically first id stays.
 */
public final class NpcTradeRegistry {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Comparator<NpcTradeOffer> CARD_ORDER = Comparator.comparingInt(NpcTradeOffer::order)
			.thenComparing(offer -> offer.id().toString());

	/** What survived the cross-file rules, and why the rest didn't. */
	public record Validation(Map<ResourceLocation, NpcTradeOffer> kept, List<String> rejections) {
		public Validation {
			kept = Map.copyOf(kept);
			rejections = List.copyOf(rejections);
		}
	}

	private record State(Map<ResourceLocation, NpcTradeOffer> parsed, int parseErrors,
			Map<ResourceLocation, NpcTradeOffer> active, Map<String, List<NpcTradeOffer>> bySpeaker, int errors) {
		static final State EMPTY = new State(Map.of(), 0, Map.of(), Map.of(), 0);
	}

	private static volatile State state = State.EMPTY;

	private NpcTradeRegistry() {}

	/** The speaker's cards, sorted by {@code order} then id. */
	public static List<NpcTradeOffer> offers(String speaker) {
		return state.bySpeaker().getOrDefault(speaker, List.of());
	}

	public static Optional<NpcTradeOffer> byId(ResourceLocation id) {
		return Optional.ofNullable(state.active().get(id));
	}

	public static Map<ResourceLocation, NpcTradeOffer> all() {
		return state.active();
	}

	/** Files that failed to parse plus cards the cross-file rules dropped, for the Track 7 GameTest. */
	public static int errors() {
		return state.errors();
	}

	/** Called by the loader with what parsed and how many files didn't. */
	static void install(Map<ResourceLocation, NpcTradeOffer> parsed, Integer parseErrors) {
		apply(Map.copyOf(parsed), parseErrors, tag -> Set.of(), item -> false);
	}

	/** Re-runs the cross-file rules with bound tags: {@code tagItems} lists a tag's items. */
	public static void bindTags(Function<ResourceLocation, Set<ResourceLocation>> tagItems) {
		State current = state;
		Set<ResourceLocation> neverSold = tagItems.apply(NpcTradeGiveRules.NEVER_SOLD);
		apply(current.parsed(), current.parseErrors(), tagItems, neverSold::contains);
	}

	private static void apply(Map<ResourceLocation, NpcTradeOffer> parsed, int parseErrors,
			Function<ResourceLocation, Set<ResourceLocation>> tagItems,
			Predicate<ResourceLocation> neverSold) {
		Validation validation = validate(parsed.values(), tagItems, neverSold);
		validation.rejections().forEach(reason -> LOGGER.error("Dropped NPC trade: {}", reason));
		Map<String, List<NpcTradeOffer>> bySpeaker = new TreeMap<>();
		validation.kept().values().forEach(offer -> bySpeaker.computeIfAbsent(offer.speaker(), s -> new ArrayList<>()).add(offer));
		Map<String, List<NpcTradeOffer>> sorted = new LinkedHashMap<>();
		bySpeaker.forEach((speaker, offers) -> sorted.put(speaker, offers.stream().sorted(CARD_ORDER).toList()));
		state = new State(parsed, parseErrors, validation.kept(), Map.copyOf(sorted),
				parseErrors + validation.rejections().size());
	}

	/**
	 * The cross-file rules over one snapshot. {@code tagItems} expands a tag to its items (return nothing before tags
	 * are bound: a tag then clashes only with the same tag).
	 */
	public static Validation validate(Iterable<NpcTradeOffer> offers,
			Function<ResourceLocation, Set<ResourceLocation>> tagItems,
			Predicate<ResourceLocation> neverSold) {
		Map<String, List<NpcTradeOffer>> bySpeaker = new TreeMap<>();
		offers.forEach(offer -> bySpeaker.computeIfAbsent(offer.speaker(), s -> new ArrayList<>()).add(offer));
		Map<ResourceLocation, NpcTradeOffer> kept = new HashMap<>();
		List<String> rejections = new ArrayList<>();
		for (List<NpcTradeOffer> speakerOffers : bySpeaker.values()) {
			speakerOffers.sort(Comparator.comparing(offer -> offer.id().toString()));
			Map<String, ResourceLocation> claimedBy = new HashMap<>();
			for (NpcTradeOffer offer : speakerOffers) {
				Optional<String> rejection = check(offer, claimedBy, tagItems, neverSold);
				if (rejection.isPresent()) {
					rejections.add(offer.id() + ": " + rejection.get());
					continue;
				}
				claims(offer, tagItems).forEach(claim -> claimedBy.put(claim, offer.id()));
				kept.put(offer.id(), offer);
			}
		}
		return new Validation(kept, rejections);
	}

	private static Optional<String> check(NpcTradeOffer offer, Map<String, ResourceLocation> claimedBy,
			Function<ResourceLocation, Set<ResourceLocation>> tagItems,
			Predicate<ResourceLocation> neverSold) {
		Optional<ResourceLocation> unsellable = Stream.concat(offer.give().stream(), offer.giveFine().stream())
				.map(NpcTradeGive::item).filter(neverSold).findFirst();
		if (unsellable.isPresent()) return Optional.of("gives " + unsellable.get() + ", tagged " + NpcTradeGiveRules.NEVER_SOLD);
		Set<String> seen = new HashSet<>();
		for (NpcTradeWant.Items part : offer.want().itemParts()) {
			for (String claim : claims(part.match(), tagItems)) {
				if (!seen.add(claim)) return Optional.of("two bundle parts both take " + claim);
			}
		}
		for (String claim : claims(offer, tagItems)) {
			ResourceLocation other = claimedBy.get(claim);
			if (other != null) return Optional.of("wants " + claim + ", which " + other + " already wants");
		}
		return Optional.empty();
	}

	/** Everything a card's want takes: item ids, {@code #tag} ids and their items, and its exclusive claim. */
	static Set<String> claims(NpcTradeOffer offer, Function<ResourceLocation, Set<ResourceLocation>> tagItems) {
		Set<String> claims = new HashSet<>();
		offer.want().itemParts().forEach(part -> claims.addAll(claims(part.match(), tagItems)));
		offer.want().exclusiveClaim().ifPresent(claims::add);
		return claims;
	}

	private static Set<String> claims(NpcTradeMatch match, Function<ResourceLocation, Set<ResourceLocation>> tagItems) {
		Set<String> claims = new HashSet<>();
		match.items().forEach(item -> claims.add(item.toString()));
		for (ResourceLocation tag : match.tags()) {
			claims.add("#" + tag);
			tagItems.apply(tag).forEach(item -> claims.add(item.toString()));
		}
		return claims;
	}
}
