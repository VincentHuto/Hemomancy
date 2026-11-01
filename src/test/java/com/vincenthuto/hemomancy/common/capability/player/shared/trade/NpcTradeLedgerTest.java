package com.vincenthuto.hemomancy.common.capability.player.shared.trade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** docs/npc_trade/03: the ledger's NBT round trip, the lazy restock window and rapport clamping. */
class NpcTradeLedgerTest {
	private static final ResourceLocation OFFER = ResourceLocation.fromNamespaceAndPath("hemomancy", "alchemist/spores");

	@Test
	void roundTripsRapportAndWindowsUnderTheirKeys() {
		NpcTradeLedger ledger = new NpcTradeLedger();
		ledger.addRapport("alchemist", 12);
		ledger.addRapport("zealot", 3);
		ledger.recordUse(OFFER, 40L, 3);
		ledger.recordUse(OFFER, 41L, 3);

		CompoundTag tag = ledger.serializeNBT(null);
		assertTrue(tag.contains("Rapport"));
		assertTrue(tag.contains("Uses"));
		NpcTradeLedger copy = new NpcTradeLedger();
		copy.deserializeNBT(null, tag);

		assertEquals(12, copy.rapport("alchemist"));
		assertEquals(3, copy.rapport("zealot"));
		assertEquals(new NpcTradeLedger.Window(40L, 2), copy.usesView().get(OFFER));
		assertEquals(2, copy.used(OFFER, 42L, 3));
	}

	@Test
	void roundTripsRotationSightingsAndBankedActs() {
		NpcTradeLedger ledger = new NpcTradeLedger();
		assertTrue(ledger.markRotationSeen(OFFER, 9L));
		for (int i = 0; i < NpcTradeLedger.ACTS_MAX + 3; i++) ledger.recordAct("spectacle");

		CompoundTag tag = ledger.serializeNBT(null);
		assertTrue(tag.contains("RotationSeen"));
		assertTrue(tag.contains("Acts"));
		NpcTradeLedger copy = new NpcTradeLedger();
		copy.deserializeNBT(null, tag);
		assertTrue(copy.rotationSeen(OFFER, 9L));
		assertEquals(false, copy.rotationSeen(OFFER, 10L));
		assertEquals(NpcTradeLedger.ACTS_MAX, copy.acts("spectacle"), "acts bank up to the cap");
		assertTrue(copy.spendAct("spectacle"));
		assertEquals(NpcTradeLedger.ACTS_MAX - 1, copy.acts("spectacle"));
		assertEquals(false, copy.spendAct("juggling"));
	}

	@Test
	void aLapsedWindowReadsEmptyAndIsReplacedOnTheNextUse() {
		NpcTradeLedger ledger = new NpcTradeLedger();
		ledger.recordUse(OFFER, 10L, 2);
		ledger.recordUse(OFFER, 11L, 2);
		assertEquals(0, ledger.usesLeft(OFFER, 2, 11L, 2));
		assertEquals(1L, ledger.daysToRestock(OFFER, 11L, 2));

		assertEquals(2, ledger.usesLeft(OFFER, 2, 12L, 2), "day 12 is past a two-day window opened on day 10");
		assertEquals(0L, ledger.daysToRestock(OFFER, 12L, 2));
		assertEquals(new NpcTradeLedger.Window(10L, 2), ledger.usesView().get(OFFER), "reading changes nothing");

		ledger.recordUse(OFFER, 12L, 2);
		assertEquals(new NpcTradeLedger.Window(12L, 1), ledger.usesView().get(OFFER));
	}

	@Test
	void aClockSetBackwardsAlsoLapsesTheWindow() {
		NpcTradeLedger ledger = new NpcTradeLedger();
		ledger.recordUse(OFFER, 30L, 5);
		assertEquals(0, ledger.used(OFFER, 29L, 5));
	}

	@Test
	void rapportClampsBetweenZeroAndTheMaximum() {
		NpcTradeLedger ledger = new NpcTradeLedger();
		assertEquals(NpcTradeLedger.RAPPORT_MAX, ledger.addRapport("vicar", 250));
		assertEquals(0, ledger.addRapport("vicar", -400));
		assertEquals(0, ledger.rapport("vicar"));

		CompoundTag tag = new CompoundTag();
		CompoundTag rapport = new CompoundTag();
		rapport.putInt("hermit", 900);
		rapport.putInt("scout", -5);
		tag.put("Rapport", rapport);
		NpcTradeLedger loaded = new NpcTradeLedger();
		loaded.deserializeNBT(null, tag);
		assertEquals(NpcTradeLedger.RAPPORT_MAX, loaded.rapport("hermit"));
		assertEquals(0, loaded.rapport("scout"));
	}

	@Test
	void rapportWithOneSpeakerLeavesEveryOtherAlone() {
		NpcTradeLedger ledger = new NpcTradeLedger();
		ledger.addRapport("court_gardener", 4);
		ledger.addRapport("alchemist", 7);
		assertEquals(4, ledger.rapport("court_gardener"));
		assertEquals(0, ledger.rapport("court_bellwarden"));
	}
}
