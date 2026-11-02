package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.shared.trade.NpcTradeLedger;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.succession.SuccessionResidents;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals.Grade;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Runs one trade the client asked for. Every check comes before anything changes hands: the side, the card's gates,
 * stock and day (a daily card only trades on its day), the price (items appraised, blood within the reserve, the Court
 * custom, or a banked Circus act) and room for the goods in a
 * simulated inventory. Only then is the price taken and the goods inserted, confirmed by count. The trade then counts
 * one use and adds rapport with this speaker only, and the reply carries a fresh Trade box. A trade never grants
 * progression.
 */
public final class NpcTradeService {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String REFUSED_UNKNOWN = "hemomancy.trade.refused.unknown";
	public static final String REFUSED_SOLD_OUT = "hemomancy.trade.refused.sold_out";
	public static final String REFUSED_MISSING = "hemomancy.trade.refused.missing";
	public static final String REFUSED_POOR = "hemomancy.trade.refused.poor";
	public static final String REFUSED_BLOOD = "hemomancy.trade.refused.blood";
	public static final String REFUSED_NO_ROOM = "hemomancy.trade.refused.no_room";
	/** An Unstained speaker won't take a stack tagged {@link NpcTradeSides#STAINED}. */
	public static final String REFUSED_STAINED = "hemomancy.trade.refused.stained";
	/** A daily card asked for on a day it isn't on the board. */
	public static final String REFUSED_NOT_TODAY = "hemomancy.trade.refused.not_today";
	public static final String DONE = "hemomancy.trade.done";
	public static final String DONE_FINE = "hemomancy.trade.done_fine";
	/** Every reason key the service itself answers with, for the lang resource test. */
	public static final List<String> REASON_KEYS = List.of(REFUSED_UNKNOWN, REFUSED_SOLD_OUT, REFUSED_MISSING,
			REFUSED_POOR, REFUSED_BLOOD, REFUSED_NO_ROOM, REFUSED_STAINED, REFUSED_NOT_TODAY, DONE,
			DONE_FINE);

	public enum Outcome { DONE, DONE_FINE, REFUSED }

	/** How a trade ended: {@code reasonKey} explains a refusal or thanks for a trade; {@code rapport} is the new value. */
	public record Result(Outcome outcome, String reasonKey, Grade grade, int rapport) {
		static Result refused(String reasonKey, int rapport) {
			return new Result(Outcome.REFUSED, reasonKey, Grade.POOR, rapport);
		}

		public boolean done() {
			return outcome != Outcome.REFUSED;
		}
	}

	/** {@code amount} items to take from main-inventory {@code slot}. */
	public record Take(int slot, int amount) {}

	/**
	 * What a trade needs from the player and the world. {@link #attempt} backs it with the real player; tests use a
	 * fake. Nothing before {@link #take}, {@link #pay} or {@link #acceptCustom} may change anything.
	 */
	public interface Party {
		/** The speaker turns this player away before any card (Unstained facing a Harbinger). */
		boolean refusedBySide(String speaker);

		Optional<String> requirementFailure(NpcTradeRequirements requirements, int rapport);

		int slotCount();

		boolean matches(int slot, NpcTradeMatch match);

		int count(int slot);

		Grade grade(int slot, NpcTradeAppraisal appraisal);

		/** Whether {@code speaker} refuses the stack in {@code slot} as stained ({@link NpcTradeSides#refusesStained}). */
		default boolean stained(String speaker, int slot) {
			return false;
		}

		boolean bloodActive();

		boolean canPayInFull(int amount, BloodAccounting.Source source);

		/** Pays the blood; true when it was paid in full. */
		boolean pay(int amount, BloodAccounting.Source source);

		/** Why the speaker won't take {@code act} as a custom, or empty. */
		Optional<String> customRefusal(String act);

		boolean isCustomOffering(int slot);

		/** Takes one offering from {@code slot} as the house's custom and records it. */
		void acceptCustom(int slot);

		/** Whether {@code gives} fit the main inventory once {@code takes} have come out of it. Changes nothing. */
		boolean fits(List<Take> takes, List<NpcTradeGive> gives);

		void take(int slot, int amount);

		/** Inserts {@code give} and returns how many of it actually arrived in the inventory, counted, not reported. */
		int insert(NpcTradeGive give);

		/** Puts {@code count} of {@code give} at the player's feet. */
		void drop(NpcTradeGive give, int count);
	}

	private NpcTradeService() {}

	/**
	 * The server side of a trade request the payload has validated: the entity is a trade speaker within reach that
	 * deals with this player, and {@code slots} are distinct main-inventory slots. Rate-limited per player.
	 */
	public static void attempt(ServerPlayer player, Entity entity, ResourceLocation offerId, int[] slots) {
		if (!(entity instanceof NpcTradeSpeaker speaker)) return;
		if (!NpcTradeRequestLimiter.tryAcquire(player.getUUID(), player.server.getTickCount())) return;
		String speakerId = speaker.tradeSpeakerId();
		NpcTradeLedger ledger = HemoCapabilityAccess.npcTrades(player);
		Optional<NpcTradeOffer> offer = NpcTradeRegistry.byId(offerId)
				.filter(found -> found.speaker().equals(speakerId));
		long day = NpcTradeRules.day(player.level().getDayTime());
		Result result;
		if (offer.isEmpty() || !serves(player, entity)) {
			result = Result.refused(REFUSED_UNKNOWN, ledger.rapport(speakerId));
		} else if (!NpcTradeRotation.offeredToday(offer.get(), NpcTradeRegistry.offers(speakerId),
				player.serverLevel().getSeed(), day)) {
			result = Result.refused(REFUSED_NOT_TODAY, ledger.rapport(speakerId));
		} else {
			result = run(offer.get(), new PlayerTradeParty(player, entity), ledger, day, slots);
		}
		NpcTradeBoard board = NpcTradeCards.build(player, speakerId);
		PacketHandler.sendToPlayer(player, new NpcTradeResultPacket(entity.getId(), offerId, result.outcome(),
				result.reasonKey(), board));
	}

	/**
	 * A misbegotten Harbinger, or a successor who won't serve this player, keeps no trade either; nor does a Hermit
	 * in his farewell, whose tree no longer opens.
	 */
	private static boolean serves(ServerPlayer player, Entity entity) {
		if (entity instanceof HarbingerHermitEntity hermit) return !hermit.isFarewellDying();
		return !(entity instanceof ProfessionalHarbingerEntity npc)
				|| !npc.isMisbegotten() && (!npc.isSuccessor() || SuccessionResidents.mayServe(player, npc));
	}

	/** The transaction over one offer. Nothing changes unless every check passes. */
	public static Result run(NpcTradeOffer offer, Party party, NpcTradeLedger ledger, long day, int[] slots) {
		String speaker = offer.speaker();
		int rapport = ledger.rapport(speaker);
		if (party.refusedBySide(speaker)) return Result.refused(NpcTradeSides.refusalKey(speaker), rapport);
		Optional<String> locked = party.requirementFailure(offer.requirements(), rapport);
		if (locked.isPresent()) return Result.refused(locked.get(), rapport);
		NpcTradeStock stock = offer.stock();
		if (stock.limited() && ledger.usesLeft(offer.id(), stock.uses(), day, stock.restockDays()) <= 0) {
			return Result.refused(REFUSED_SOLD_OUT, rapport);
		}

		List<Take> takes = new ArrayList<>();
		Grade grade = Grade.FAIR;
		int customSlot = -1;
		switch (offer.want()) {
			case NpcTradeWant.Blood blood -> {
				Optional<BloodAccounting.Source> source = NpcTradeSources.byId(blood.source());
				if (source.isEmpty()) return Result.refused(REFUSED_UNKNOWN, rapport);
				if (!party.bloodActive()) return Result.refused(NpcTradeRequirements.LOCKED_BLOOD, rapport);
				if (!party.canPayInFull(blood.amount(), source.get())) return Result.refused(REFUSED_BLOOD, rapport);
			}
			case NpcTradeWant.Custom custom -> {
				Optional<String> refusal = party.customRefusal(custom.act());
				if (refusal.isPresent()) return Result.refused(refusal.get(), rapport);
				for (int slot : slots) {
					if (party.isCustomOffering(slot)) {
						customSlot = slot;
						break;
					}
				}
				if (customSlot < 0) return Result.refused(REFUSED_MISSING, rapport);
			}
			case NpcTradeWant.Act act -> {
				if (ledger.acts(act.act()) <= 0) return Result.refused(act.lockKey(), rapport);
			}
			case NpcTradeWant.Items items -> {
				if (offersStained(offer, party, slots)) return Result.refused(REFUSED_STAINED, rapport);
				Optional<Grade> planned = plan(offer.want().itemParts(), party, slots, takes);
				if (planned.isEmpty()) return Result.refused(REFUSED_MISSING, rapport);
				if (planned.get() == Grade.POOR) return Result.refused(REFUSED_POOR, rapport);
				grade = planned.get();
			}
			case NpcTradeWant.Bundle bundle -> {
				if (offersStained(offer, party, slots)) return Result.refused(REFUSED_STAINED, rapport);
				Optional<Grade> planned = plan(bundle.parts(), party, slots, takes);
				if (planned.isEmpty()) return Result.refused(REFUSED_MISSING, rapport);
				if (planned.get() == Grade.POOR) return Result.refused(REFUSED_POOR, rapport);
				grade = planned.get();
			}
		}

		boolean fine = grade == Grade.FINE;
		List<NpcTradeGive> gives = fine && !offer.giveFine().isEmpty() ? offer.giveFine() : offer.give();
		if (!gives.isEmpty() && !party.fits(takes, gives)) return Result.refused(REFUSED_NO_ROOM, rapport);

		if (offer.want() instanceof NpcTradeWant.Blood blood) {
			if (!party.pay(blood.amount(), NpcTradeSources.byId(blood.source()).orElseThrow())) {
				LOGGER.warn("NPC trade {}: blood was affordable but not paid in full", offer.id());
				return Result.refused(REFUSED_BLOOD, rapport);
			}
		}
		for (Take take : takes) party.take(take.slot(), take.amount());
		if (customSlot >= 0) party.acceptCustom(customSlot);
		if (offer.want() instanceof NpcTradeWant.Act act) ledger.spendAct(act.act());

		for (NpcTradeGive give : gives) {
			int arrived = Math.max(0, party.insert(give));
			if (arrived < give.count()) {
				LOGGER.warn("NPC trade {}: {} of {} {} did not fit after the check; dropped at the player's feet",
						offer.id(), give.count() - arrived, give.count(), give.item());
				party.drop(give, give.count() - arrived);
			}
		}

		if (stock.limited()) ledger.recordUse(offer.id(), day, stock.restockDays());
		int bonus = fine && offer.giveFine().isEmpty() ? 1 : 0;
		int after = ledger.addRapport(speaker, offer.rapport() + bonus);
		return new Result(fine ? Outcome.DONE_FINE : Outcome.DONE, fine ? DONE_FINE : DONE, grade, after);
	}

	/** Whether a chosen slot that would pay this card holds a stack the speaker refuses as stained. */
	static boolean offersStained(NpcTradeOffer offer, Party party, int[] slots) {
		for (int slot : slots) {
			if (slot < 0 || slot >= party.slotCount() || party.count(slot) <= 0) continue;
			boolean pays = offer.want().itemParts().stream().anyMatch(part -> party.matches(slot, part.match()));
			if (pays && party.stained(offer.speaker(), slot)) return true;
		}
		return false;
	}

	/**
	 * Picks the stacks that pay {@code parts} from the chosen slots, in the order sent, each slot serving one part.
	 * Returns the trade's grade, the lowest among the stacks taken ({@code POOR} when any is poor), or empty when the
	 * slots don't hold enough.
	 */
	static Optional<Grade> plan(List<NpcTradeWant.Items> parts, Party party, int[] slots, List<Take> takes) {
		boolean[] used = new boolean[party.slotCount()];
		Grade grade = Grade.FINE;
		for (NpcTradeWant.Items part : parts) {
			int remaining = part.count();
			for (int slot : slots) {
				if (remaining <= 0) break;
				if (slot < 0 || slot >= used.length || used[slot]) continue;
				int count = party.count(slot);
				if (count <= 0 || !party.matches(slot, part.match())) continue;
				grade = grade.min(party.grade(slot, part.appraisal()));
				if (grade == Grade.POOR) return Optional.of(Grade.POOR);
				int taken = Math.min(remaining, count);
				takes.add(new Take(slot, taken));
				used[slot] = true;
				remaining -= taken;
			}
			if (remaining > 0) return Optional.empty();
		}
		return Optional.of(grade);
	}
}
