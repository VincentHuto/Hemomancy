package com.vincenthuto.hemomancy.common.entity.npc.trade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Daily wants. An offer with a {@code rotation} joins its speaker's pool of that name, and each pool shows one card per
 * in-game day. The pick is a weighted draw from {@code hash(levelSeed, speaker, pool, day)}, so every player in a world
 * sees the same daily want on the same day, whatever they can trade.
 */
public final class NpcTradeRotation {
	public static final int MAX_WEIGHT = 100;
	private static final Comparator<NpcTradeOffer> BY_ID = Comparator.comparing(offer -> offer.id().toString());

	/** An offer's place in a rotation: the pool it joins and its weight in the draw. */
	public record Entry(String pool, int weight) {
		public Entry {
			if (pool == null || pool.isBlank()) throw new IllegalArgumentException("A rotation needs a pool");
			if (weight < 1 || weight > MAX_WEIGHT) throw new IllegalArgumentException("Rotation weight must be 1-" + MAX_WEIGHT);
		}
	}

	private NpcTradeRotation() {}

	/** {@code offers} as they stand on {@code day}: every ordinary card, and from each pool only that day's pick. */
	public static List<NpcTradeOffer> today(List<NpcTradeOffer> offers, long seed, long day) {
		Map<String, NpcTradeOffer> picks = picks(offers, seed, day);
		List<NpcTradeOffer> shown = new ArrayList<>(offers.size());
		for (NpcTradeOffer offer : offers) {
			if (offer.rotation().isEmpty() || offer.equals(picks.get(poolKey(offer)))) shown.add(offer);
		}
		return shown;
	}

	/** Whether {@code offer} is on its speaker's board on {@code day}: always for an ordinary card. */
	public static boolean offeredToday(NpcTradeOffer offer, List<NpcTradeOffer> speakerOffers, long seed, long day) {
		if (offer.rotation().isEmpty()) return true;
		return offer.equals(picks(speakerOffers, seed, day).get(poolKey(offer)));
	}

	/** Each pool's pick on {@code day}, keyed by speaker and pool. */
	static Map<String, NpcTradeOffer> picks(List<NpcTradeOffer> offers, long seed, long day) {
		Map<String, List<NpcTradeOffer>> pools = new LinkedHashMap<>();
		for (NpcTradeOffer offer : offers) {
			if (offer.rotation().isPresent()) pools.computeIfAbsent(poolKey(offer), key -> new ArrayList<>()).add(offer);
		}
		Map<String, NpcTradeOffer> picks = new LinkedHashMap<>();
		pools.forEach((key, members) -> {
			NpcTradeOffer first = members.getFirst();
			pick(members, seed, first.speaker(), first.rotation().orElseThrow().pool(), day)
					.ifPresent(offer -> picks.put(key, offer));
		});
		return picks;
	}

	/** The weighted draw over one pool's members, the same for every caller with the same arguments. */
	public static Optional<NpcTradeOffer> pick(List<NpcTradeOffer> members, long seed, String speaker, String pool,
			long day) {
		if (members.isEmpty()) return Optional.empty();
		List<NpcTradeOffer> sorted = members.stream().sorted(BY_ID).toList();
		long total = 0;
		for (NpcTradeOffer offer : sorted) total += weight(offer);
		long roll = Long.remainderUnsigned(hash(seed, speaker, pool, day), total);
		for (NpcTradeOffer offer : sorted) {
			roll -= weight(offer);
			if (roll < 0) return Optional.of(offer);
		}
		return Optional.of(sorted.getLast());
	}

	/** {@code hash(levelSeed, speaker, pool, day)}: String hashes are fixed by the language, so this is stable. */
	static long hash(long seed, String speaker, String pool, long day) {
		long h = mix(seed);
		h = mix(h ^ speaker.hashCode());
		h = mix(h ^ pool.hashCode());
		return mix(h ^ day);
	}

	/** The SplitMix64 finaliser. */
	private static long mix(long value) {
		long z = value + 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	private static int weight(NpcTradeOffer offer) {
		return offer.rotation().map(Entry::weight).orElse(1);
	}

	private static String poolKey(NpcTradeOffer offer) {
		return offer.speaker() + "/" + offer.rotation().orElseThrow().pool();
	}
}
