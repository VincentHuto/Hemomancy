package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.shared.trade.NpcTradeLedger;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals.Grade;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService.Outcome;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService.Result;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** docs/archive/tracks/npc_trade/03: the trade transaction over a fake inventory. */
class NpcTradeServiceTest {
	private static final NpcTradeGive REDSTONE = new NpcTradeGive(rl("minecraft:redstone"), 4, Optional.empty());
	private static final NpcTradeGive MORE_REDSTONE = new NpcTradeGive(rl("minecraft:redstone"), 8, Optional.empty());
	private static final NpcTradeWant.Items TWO_BONES = items("minecraft:bone", 2, NpcTradeAppraisals.DURABILITY);
	private static final long DAY = 100L;

	@Test
	void everyRefusalLeavesTheInventoryTheBloodAndTheLedgerUntouched() {
		NpcTradeOffer bones = offer("alchemist/bones", TWO_BONES, List.of(REDSTONE), List.of(), new NpcTradeStock(1, 2));
		NpcTradeOffer blood = offer("alchemist/tithe", new NpcTradeWant.Blood(200, NpcTradeWant.Blood.TITHE),
				List.of(REDSTONE), List.of(), NpcTradeStock.UNLIMITED_STOCK);
		NpcTradeOffer custom = new NpcTradeOffer(rl("hemomancy:court_gardener/custom"), "court_gardener",
				NpcTradeSides.Side.COURT, "t", "l", 0, NpcTradeRequirements.NONE, new NpcTradeWant.Custom("custom_food"),
				List.of(), List.of(), 2, NpcTradeStock.UNLIMITED_STOCK, false, Optional.empty());

		record Case(String name, NpcTradeOffer offer, FakeParty party, int[] slots, String reason, boolean soldOut) {}
		List<Case> cases = List.of(
				new Case("side", bones, FakeParty.bones(Grade.FAIR).refusing(), slots(0), NpcTradeSides.refusalKey("alchemist"), false),
				new Case("locked", bones, FakeParty.bones(Grade.FAIR).locked(NpcTradeRequirements.LOCKED_DEGREE), slots(0),
						NpcTradeRequirements.LOCKED_DEGREE, false),
				new Case("sold out", bones, FakeParty.bones(Grade.FAIR), slots(0), NpcTradeService.REFUSED_SOLD_OUT, true),
				new Case("missing", bones, FakeParty.bones(Grade.FAIR), slots(1), NpcTradeService.REFUSED_MISSING, false),
				new Case("too few", bones, new FakeParty().slot("minecraft:bone", 1, Grade.FINE), slots(0),
						NpcTradeService.REFUSED_MISSING, false),
				new Case("poor", bones, FakeParty.bones(Grade.POOR), slots(0), NpcTradeService.REFUSED_POOR, false),
				new Case("no room", bones, FakeParty.bones(Grade.FAIR).full(), slots(0), NpcTradeService.REFUSED_NO_ROOM, false),
				new Case("dormant", blood, FakeParty.bones(Grade.FAIR).dormant(), slots(), NpcTradeRequirements.LOCKED_BLOOD, false),
				new Case("short", blood, FakeParty.bones(Grade.FAIR).bloodHeld(150), slots(), NpcTradeService.REFUSED_BLOOD, false),
				new Case("custom refused", custom, FakeParty.bones(Grade.FAIR).customRefused().offering(1), slots(1),
						"hemomancy.court.refused", false),
				new Case("no offering", custom, FakeParty.bones(Grade.FAIR), slots(0), NpcTradeService.REFUSED_MISSING, false));

		for (Case c : cases) {
			NpcTradeLedger ledger = new NpcTradeLedger();
			if (c.soldOut()) ledger.recordUse(c.offer().id(), DAY, 2);
			String before = ledger.serializeNBT(null).toString();
			List<Integer> counts = c.party().counts();
			Result result = NpcTradeService.run(c.offer(), c.party(), ledger, DAY, c.slots());
			assertEquals(Outcome.REFUSED, result.outcome(), c.name());
			assertEquals(c.reason(), result.reasonKey(), c.name());
			assertEquals(counts, c.party().counts(), c.name() + ": inventory changed");
			assertEquals(List.of(), c.party().changes, c.name() + ": something changed hands");
			assertEquals(before, ledger.serializeNBT(null).toString(), c.name() + ": ledger changed");
		}
	}

	@Test
	void theLowestGradeAmongTheStacksTakenDecidesTheTrade() {
		NpcTradeOffer offer = offer("alchemist/bones", TWO_BONES, List.of(REDSTONE), List.of(MORE_REDSTONE),
				NpcTradeStock.UNLIMITED_STOCK);
		FakeParty mixed = new FakeParty().slot("minecraft:bone", 1, Grade.FINE).slot("minecraft:bone", 1, Grade.FAIR);
		Result fair = NpcTradeService.run(offer, mixed, new NpcTradeLedger(), DAY, slots(0, 1));
		assertEquals(Outcome.DONE, fair.outcome());
		assertEquals(Grade.FAIR, fair.grade());
		assertEquals(List.of("insert minecraft:redstone x4"), mixed.inserted());

		FakeParty fine = new FakeParty().slot("minecraft:bone", 1, Grade.FINE).slot("minecraft:bone", 1, Grade.FINE);
		Result fineResult = NpcTradeService.run(offer, fine, new NpcTradeLedger(), DAY, slots(0, 1));
		assertEquals(Outcome.DONE_FINE, fineResult.outcome());
		assertEquals(List.of("insert minecraft:redstone x8"), fine.inserted());
	}

	@Test
	void aTradeTakesThePriceInsertsTheGoodsAndRecordsOnlyThisSpeaker() {
		NpcTradeOffer offer = offer("alchemist/bones", TWO_BONES, List.of(REDSTONE), List.of(), new NpcTradeStock(3, 2));
		FakeParty party = new FakeParty().slot("minecraft:bone", 5, Grade.FINE);
		NpcTradeLedger ledger = new NpcTradeLedger();
		ledger.addRapport("zealot", 9);

		Result result = NpcTradeService.run(offer, party, ledger, DAY, slots(0));

		assertEquals(Outcome.DONE_FINE, result.outcome());
		assertEquals(List.of(3), party.counts());
		assertEquals(List.of("take 0 x2", "insert minecraft:redstone x4"), party.changes);
		assertEquals(List.of(new NpcTradeService.Take(0, 2)), party.fitTakes, "room is checked with the price taken out");
		assertEquals(2, result.rapport(), "one for the card, one more for a fine trade with no fine gives");
		assertEquals(2, ledger.rapport("alchemist"));
		assertEquals(9, ledger.rapport("zealot"));
		assertEquals(2, ledger.usesLeft(offer.id(), 3, DAY, 2));
	}

	@Test
	void goodsTheInventoryReportsButDoesNotHoldAreDroppedAtTheFeet() {
		NpcTradeOffer offer = offer("alchemist/bones", TWO_BONES, List.of(REDSTONE), List.of(), NpcTradeStock.UNLIMITED_STOCK);
		FakeParty creative = new FakeParty().slot("minecraft:bone", 2, Grade.FAIR).discarding();

		Result result = NpcTradeService.run(offer, creative, new NpcTradeLedger(), DAY, slots(0));

		assertTrue(result.done());
		assertEquals(List.of("take 0 x2", "insert minecraft:redstone x4", "drop minecraft:redstone x4"), creative.changes);
	}

	@Test
	void bloodIsPaidThroughTheTitheAndACustomRecordsTheOffering() {
		NpcTradeOffer blood = offer("alchemist/tithe", new NpcTradeWant.Blood(200, NpcTradeWant.Blood.TITHE),
				List.of(REDSTONE), List.of(), NpcTradeStock.UNLIMITED_STOCK);
		FakeParty donor = new FakeParty().bloodHeld(500);
		assertTrue(NpcTradeService.run(blood, donor, new NpcTradeLedger(), DAY, slots()).done());
		assertEquals(List.of("pay 200 npc_tithe", "insert minecraft:redstone x4"), donor.changes);

		NpcTradeOffer custom = new NpcTradeOffer(rl("hemomancy:court_gardener/custom"), "court_gardener",
				NpcTradeSides.Side.COURT, "t", "l", 0, NpcTradeRequirements.NONE, new NpcTradeWant.Custom("custom_food"),
				List.of(), List.of(), 2, NpcTradeStock.UNLIMITED_STOCK, false, Optional.empty());
		FakeParty guest = new FakeParty().slot("minecraft:bone", 1, Grade.FAIR).slot("minecraft:glow_berries", 3, Grade.FAIR)
				.offering(1);
		NpcTradeLedger ledger = new NpcTradeLedger();
		assertTrue(NpcTradeService.run(custom, guest, ledger, DAY, slots(0, 1)).done());
		assertEquals(List.of("custom 1"), guest.changes);
		assertEquals(2, ledger.rapport("court_gardener"));
	}

	@Test
	void anUnstainedSpeakerRefusesAStainedStackBeforeTakingAnything() {
		NpcTradeOffer arrows = new NpcTradeOffer(rl("hemomancy:guardian/arrows"), "guardian", NpcTradeSides.Side.UNSTAINED,
				"t", "l", 0, NpcTradeRequirements.NONE, items("minecraft:arrow", 4, NpcTradeAppraisals.NONE),
				List.of(REDSTONE), List.of(), 1, NpcTradeStock.UNLIMITED_STOCK, false, Optional.empty());
		FakeParty party = new FakeParty().slot("minecraft:arrow", 2, Grade.FAIR).slot("minecraft:arrow", 2, Grade.FAIR)
				.stainedSlot(1);
		NpcTradeLedger ledger = new NpcTradeLedger();
		Result result = NpcTradeService.run(arrows, party, ledger, DAY, slots(0, 1));
		assertEquals(NpcTradeService.REFUSED_STAINED, result.reasonKey());
		assertEquals(List.of(2, 2), party.counts());
		assertEquals(List.of(), party.changes);
		assertEquals(0, ledger.rapport("guardian"));

		FakeParty clean = new FakeParty().slot("minecraft:arrow", 2, Grade.FAIR).slot("minecraft:arrow", 2, Grade.FAIR)
				.slot("minecraft:arrow", 2, Grade.FAIR).stainedSlot(1);
		assertTrue(NpcTradeService.run(arrows, clean, new NpcTradeLedger(), DAY, slots(0, 2)).done(),
				"a stained stack the player didn't offer is not refused");
	}

	@Test
	void eachSlotPaysOnePartOfABundle() {
		NpcTradeWant.Bundle bundle = new NpcTradeWant.Bundle(List.of(items("minecraft:bone", 1, NpcTradeAppraisals.NONE),
				items("minecraft:copper_ingot", 2, NpcTradeAppraisals.NONE)));
		NpcTradeOffer offer = offer("alchemist/bundle", bundle, List.of(REDSTONE), List.of(), NpcTradeStock.UNLIMITED_STOCK);
		FakeParty party = new FakeParty().slot("minecraft:bone", 3, Grade.FAIR).slot("minecraft:copper_ingot", 1, Grade.FAIR)
				.slot("minecraft:copper_ingot", 1, Grade.FAIR);
		assertTrue(NpcTradeService.run(offer, party, new NpcTradeLedger(), DAY, slots(0, 1, 2)).done());
		assertEquals(List.of(2, 0, 0), party.counts());

		FakeParty short_ = new FakeParty().slot("minecraft:bone", 3, Grade.FAIR).slot("minecraft:copper_ingot", 1, Grade.FAIR);
		assertEquals(NpcTradeService.REFUSED_MISSING,
				NpcTradeService.run(offer, short_, new NpcTradeLedger(), DAY, slots(0, 1)).reasonKey());
	}

	@Test
	void rapportTiersFollowThePlan() {
		assertEquals(NpcTradeRules.STRANGER, NpcTradeRules.tier(9));
		assertEquals(NpcTradeRules.KNOWN, NpcTradeRules.tier(10));
		assertEquals(NpcTradeRules.KNOWN, NpcTradeRules.tier(29));
		assertEquals(NpcTradeRules.TRUSTED, NpcTradeRules.tier(30));
		assertEquals(NpcTradeRules.CONFIDANT, NpcTradeRules.tier(60));
		assertEquals(NpcTradeRules.CONFIDANT, NpcTradeRules.tier(100));
		assertEquals("hemomancy.trade.tier.trusted", NpcTradeRules.tierKey(NpcTradeRules.TRUSTED));
	}

	@Test
	void anActCardSpendsOneBankedActAndRefusesWithoutOne() {
		NpcTradeOffer spectacle = new NpcTradeOffer(rl("hemomancy:circus_ringmaster/spectacle"), "circus_ringmaster",
				NpcTradeSides.Side.CIRCUS, "t", "l", 0, NpcTradeRequirements.NONE,
				new NpcTradeWant.Act(NpcTradeWant.Act.SPECTACLE), List.of(new NpcTradeGive(rl("minecraft:bread"), 1,
						Optional.empty())), List.of(), 2, NpcTradeStock.UNLIMITED_STOCK, true, Optional.empty());
		NpcTradeLedger ledger = new NpcTradeLedger();
		FakeParty party = new FakeParty();
		NpcTradeService.Result refused = NpcTradeService.run(spectacle, party, ledger, 0L, slots());
		assertEquals(NpcTradeService.Outcome.REFUSED, refused.outcome());
		assertEquals("hemomancy.trade.locked.act.spectacle", refused.reasonKey());
		assertEquals(List.of(), party.changes);

		ledger.recordAct(NpcTradeWant.Act.SPECTACLE);
		NpcTradeService.Result done = NpcTradeService.run(spectacle, party, ledger, 0L, slots());
		assertEquals(NpcTradeService.Outcome.DONE, done.outcome());
		assertEquals(0, ledger.acts(NpcTradeWant.Act.SPECTACLE), "the act is spent");
		assertEquals(2, ledger.rapport("circus_ringmaster"));
		assertEquals(List.of("insert minecraft:bread x1"), party.changes);
	}

	private static NpcTradeOffer offer(String path, NpcTradeWant want, List<NpcTradeGive> give, List<NpcTradeGive> fine,
			NpcTradeStock stock) {
		return new NpcTradeOffer(rl("hemomancy:" + path), "alchemist", NpcTradeSides.Side.HARBINGER, "t", "l", 0,
				NpcTradeRequirements.NONE, want, give, fine, 1, stock, false, Optional.empty());
	}

	private static NpcTradeWant.Items items(String item, int count, String grader) {
		return new NpcTradeWant.Items(new NpcTradeMatch(List.of(rl(item)), List.of()), count,
				new NpcTradeAppraisal(grader, Map.of()));
	}

	private static int[] slots(int... slots) {
		return slots;
	}

	private static ResourceLocation rl(String id) {
		return ResourceLocation.parse(id);
	}

	/** An inventory of item ids with counts and grades; every change that would reach the world is logged. */
	private static final class FakeParty implements NpcTradeService.Party {
		private final List<String> ids = new ArrayList<>();
		private final List<Integer> counts = new ArrayList<>();
		private final List<Grade> grades = new ArrayList<>();
		final List<String> changes = new ArrayList<>();
		List<NpcTradeService.Take> fitTakes = List.of();
		private boolean refusing;
		private Optional<String> locked = Optional.empty();
		private boolean full;
		private boolean active = true;
		private int blood = 1000;
		private boolean customRefused;
		private int offeringSlot = -1;
		private boolean discarding;
		private int stainedSlot = -1;

		static FakeParty bones(Grade grade) {
			return new FakeParty().slot("minecraft:bone", 4, grade).slot("minecraft:dirt", 8, Grade.FAIR);
		}

		FakeParty slot(String id, int count, Grade grade) {
			ids.add(id);
			counts.add(count);
			grades.add(grade);
			return this;
		}

		FakeParty refusing() { refusing = true; return this; }
		FakeParty locked(String key) { locked = Optional.of(key); return this; }
		FakeParty full() { full = true; return this; }
		FakeParty dormant() { active = false; return this; }
		FakeParty bloodHeld(int amount) { blood = amount; return this; }
		FakeParty customRefused() { customRefused = true; return this; }
		FakeParty offering(int slot) { offeringSlot = slot; return this; }
		FakeParty discarding() { discarding = true; return this; }
		FakeParty stainedSlot(int slot) { stainedSlot = slot; return this; }

		List<Integer> counts() {
			return List.copyOf(counts);
		}

		List<String> inserted() {
			return changes.stream().filter(change -> change.startsWith("insert")).toList();
		}

		@Override public boolean refusedBySide(String speaker) { return refusing; }
		@Override public Optional<String> requirementFailure(NpcTradeRequirements requirements, int rapport) { return locked; }
		@Override public int slotCount() { return 36; }
		@Override public boolean matches(int slot, NpcTradeMatch match) {
			return slot < ids.size() && match.items().contains(rl(ids.get(slot)));
		}
		@Override public int count(int slot) { return slot < counts.size() ? counts.get(slot) : 0; }
		@Override public Grade grade(int slot, NpcTradeAppraisal appraisal) {
			return NpcTradeAppraisals.NONE.equals(appraisal.grader()) ? Grade.FAIR : grades.get(slot);
		}
		@Override public boolean stained(String speaker, int slot) {
			return slot == stainedSlot && NpcTradeSides.is(speaker, NpcTradeSides.Side.UNSTAINED);
		}
		@Override public boolean bloodActive() { return active; }
		@Override public boolean canPayInFull(int amount, BloodAccounting.Source source) { return blood >= amount; }
		@Override public boolean pay(int amount, BloodAccounting.Source source) {
			changes.add("pay " + amount + " " + source.id());
			blood -= amount;
			return true;
		}
		@Override public Optional<String> customRefusal(String act) {
			return customRefused ? Optional.of("hemomancy.court.refused") : Optional.empty();
		}
		@Override public boolean isCustomOffering(int slot) { return slot == offeringSlot; }
		@Override public void acceptCustom(int slot) {
			changes.add("custom " + slot);
			counts.set(slot, counts.get(slot) - 1);
		}
		@Override public boolean fits(List<NpcTradeService.Take> takes, List<NpcTradeGive> gives) {
			fitTakes = List.copyOf(takes);
			return !full;
		}
		@Override public void take(int slot, int amount) {
			changes.add("take " + slot + " x" + amount);
			counts.set(slot, counts.get(slot) - amount);
		}
		@Override public int insert(NpcTradeGive give) {
			changes.add("insert " + give.item() + " x" + give.count());
			return discarding ? 0 : give.count();
		}
		@Override public void drop(NpcTradeGive give, int count) {
			changes.add("drop " + give.item() + " x" + count);
		}
	}
}
