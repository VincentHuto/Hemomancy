package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One want card as the client sees it: the offer's keys, what it wants and gives, and where the player stands with it.
 * {@code lockKey} is set only on a {@link State#LOCKED} card, {@code daysToRestock} only on a {@link State#SOLD_OUT}
 * one, and {@code usesLeft} is {@link #UNLIMITED} for an unlimited card. {@code daily} marks today's pick from a
 * rotation pool (the "today" ribbon), and {@code fresh} one the player hasn't seen yet today. Nothing here reads the
 * player's inventory: the client matches its own and the server checks again.
 */
public record NpcTradeCard(ResourceLocation offerId, String titleKey, String lineKey, int order, NpcTradeWant want,
		List<NpcTradeGive> give, List<NpcTradeGive> giveFine, State state, Optional<String> lockKey,
		int daysToRestock, int usesLeft, Optional<String> opens, boolean daily, boolean fresh) {
	public static final int UNLIMITED = -1;

	private static final int WANT_ITEMS = 0;
	private static final int WANT_BLOOD = 1;
	private static final int WANT_BUNDLE = 2;
	private static final int WANT_CUSTOM = 3;
	private static final int WANT_ACT = 4;

	public enum State {
		OPEN, SOLD_OUT, LOCKED;

		static State fromOrdinal(int ordinal) {
			State[] values = values();
			return ordinal >= 0 && ordinal < values.length ? values[ordinal] : LOCKED;
		}
	}

	public NpcTradeCard {
		give = List.copyOf(give);
		giveFine = List.copyOf(giveFine);
	}

	/** An ordinary (non-rotating) card. */
	public NpcTradeCard(ResourceLocation offerId, String titleKey, String lineKey, int order, NpcTradeWant want,
			List<NpcTradeGive> give, List<NpcTradeGive> giveFine, State state, Optional<String> lockKey,
			int daysToRestock, int usesLeft, Optional<String> opens) {
		this(offerId, titleKey, lineKey, order, want, give, giveFine, state, lockKey, daysToRestock, usesLeft, opens,
				false, false);
	}

	public static NpcTradeCard of(NpcTradeOffer offer, Optional<String> lockKey, int usesLeft, long daysToRestock) {
		return of(offer, lockKey, usesLeft, daysToRestock, false);
	}

	/**
	 * The card for {@code offer}, given the player's first failed requirement and what is left of its stock;
	 * {@code fresh} when it is a daily card the player hasn't seen today.
	 */
	public static NpcTradeCard of(NpcTradeOffer offer, Optional<String> lockKey, int usesLeft, long daysToRestock,
			boolean fresh) {
		State state = lockKey.isPresent() ? State.LOCKED
				: offer.stock().limited() && usesLeft <= 0 ? State.SOLD_OUT : State.OPEN;
		return new NpcTradeCard(offer.id(), offer.title(), offer.line(), offer.order(), offer.want(), offer.give(),
				offer.giveFine(), state, state == State.LOCKED ? lockKey : Optional.empty(),
				state == State.SOLD_OUT ? (int) Math.max(1L, daysToRestock) : 0,
				offer.stock().limited() ? Math.max(0, usesLeft) : UNLIMITED, offer.opens(),
				offer.rotation().isPresent(), fresh && offer.rotation().isPresent());
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeResourceLocation(offerId);
		buf.writeUtf(titleKey);
		buf.writeUtf(lineKey);
		buf.writeVarInt(order);
		writeWant(buf, want);
		writeGives(buf, give);
		writeGives(buf, giveFine);
		buf.writeVarInt(state.ordinal());
		writeOptional(buf, lockKey);
		buf.writeVarInt(daysToRestock);
		buf.writeVarInt(usesLeft);
		writeOptional(buf, opens);
		buf.writeBoolean(daily);
		buf.writeBoolean(fresh);
	}

	public static NpcTradeCard fromNetwork(FriendlyByteBuf buf) {
		ResourceLocation offerId = buf.readResourceLocation();
		String title = buf.readUtf();
		String line = buf.readUtf();
		int order = buf.readVarInt();
		NpcTradeWant want = readWant(buf);
		List<NpcTradeGive> give = readGives(buf);
		List<NpcTradeGive> giveFine = readGives(buf);
		State state = State.fromOrdinal(buf.readVarInt());
		Optional<String> lockKey = readOptional(buf);
		int days = buf.readVarInt();
		int usesLeft = buf.readVarInt();
		Optional<String> opens = readOptional(buf);
		boolean daily = buf.readBoolean();
		boolean fresh = buf.readBoolean();
		return new NpcTradeCard(offerId, title, line, order, want, give, giveFine, state, lockKey, days, usesLeft, opens,
				daily, fresh);
	}

	private static void writeWant(FriendlyByteBuf buf, NpcTradeWant want) {
		switch (want) {
			case NpcTradeWant.Items items -> {
				buf.writeVarInt(WANT_ITEMS);
				writeItems(buf, items);
			}
			case NpcTradeWant.Blood blood -> {
				buf.writeVarInt(WANT_BLOOD);
				buf.writeVarInt(blood.amount());
				buf.writeUtf(blood.source());
			}
			case NpcTradeWant.Bundle bundle -> {
				buf.writeVarInt(WANT_BUNDLE);
				buf.writeVarInt(bundle.parts().size());
				bundle.parts().forEach(part -> writeItems(buf, part));
			}
			case NpcTradeWant.Custom custom -> {
				buf.writeVarInt(WANT_CUSTOM);
				buf.writeUtf(custom.act());
			}
			case NpcTradeWant.Act act -> {
				buf.writeVarInt(WANT_ACT);
				buf.writeUtf(act.act());
			}
		}
	}

	private static NpcTradeWant readWant(FriendlyByteBuf buf) {
		int kind = buf.readVarInt();
		return switch (kind) {
			case WANT_ITEMS -> readItems(buf);
			case WANT_BLOOD -> new NpcTradeWant.Blood(buf.readVarInt(), buf.readUtf());
			case WANT_BUNDLE -> {
				int size = Math.min(buf.readVarInt(), NpcTradeWant.Bundle.MAX_PARTS);
				List<NpcTradeWant.Items> parts = new ArrayList<>(size);
				for (int i = 0; i < size; i++) parts.add(readItems(buf));
				yield new NpcTradeWant.Bundle(parts);
			}
			case WANT_CUSTOM -> new NpcTradeWant.Custom(buf.readUtf());
			case WANT_ACT -> new NpcTradeWant.Act(buf.readUtf());
			default -> throw new IllegalArgumentException("Unknown trade want kind " + kind);
		};
	}

	private static void writeItems(FriendlyByteBuf buf, NpcTradeWant.Items items) {
		writeIds(buf, items.match().items());
		writeIds(buf, items.match().tags());
		buf.writeVarInt(items.count());
		buf.writeUtf(items.appraisal().grader());
		buf.writeVarInt(items.appraisal().params().size());
		items.appraisal().params().forEach((name, value) -> {
			buf.writeUtf(name);
			buf.writeUtf(value);
		});
	}

	private static NpcTradeWant.Items readItems(FriendlyByteBuf buf) {
		NpcTradeMatch match = new NpcTradeMatch(readIds(buf), readIds(buf));
		int count = buf.readVarInt();
		String grader = buf.readUtf();
		int params = buf.readVarInt();
		Map<String, String> values = new LinkedHashMap<>();
		for (int i = 0; i < params; i++) values.put(buf.readUtf(), buf.readUtf());
		return new NpcTradeWant.Items(match, count, new NpcTradeAppraisal(grader, values));
	}

	private static void writeGives(FriendlyByteBuf buf, List<NpcTradeGive> gives) {
		buf.writeVarInt(gives.size());
		for (NpcTradeGive give : gives) {
			buf.writeResourceLocation(give.item());
			buf.writeVarInt(give.count());
			writeOptional(buf, give.components());
		}
	}

	private static List<NpcTradeGive> readGives(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<NpcTradeGive> gives = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			gives.add(new NpcTradeGive(buf.readResourceLocation(), buf.readVarInt(), readOptional(buf)));
		}
		return gives;
	}

	private static void writeIds(FriendlyByteBuf buf, List<ResourceLocation> ids) {
		buf.writeVarInt(ids.size());
		ids.forEach(buf::writeResourceLocation);
	}

	private static List<ResourceLocation> readIds(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<ResourceLocation> ids = new ArrayList<>(size);
		for (int i = 0; i < size; i++) ids.add(buf.readResourceLocation());
		return ids;
	}

	static void writeOptional(FriendlyByteBuf buf, Optional<String> value) {
		buf.writeBoolean(value.isPresent());
		value.ifPresent(buf::writeUtf);
	}

	static Optional<String> readOptional(FriendlyByteBuf buf) {
		return buf.readBoolean() ? Optional.of(buf.readUtf()) : Optional.empty();
	}
}
