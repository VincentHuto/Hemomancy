package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.NoticeKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.PendingNotice;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.PetitionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodlineSavedDataTest {
	private static final UUID LEADER = UUID.nameUUIDFromBytes(new byte[] {1});
	private static final UUID MEMBER = UUID.nameUUIDFromBytes(new byte[] {2});
	private static final UUID LINE = UUID.nameUUIDFromBytes(new byte[] {3});

	@Test
	void settlementsRemovalsAndPetitionsSurviveASave() {
		BloodlineSavedData data = new BloodlineSavedData();
		Bloodline line = new Bloodline("Ashwell", LEADER, LINE, new ArrayList<>());
		data.registerBloodline(line);
		data.addNotice(MEMBER, PendingNotice.of(NoticeKind.DISSOLVED, line, "", 1250.0F, true));
		data.addNotice(MEMBER, PendingNotice.of(NoticeKind.REFUSED, line, "Ashwell", 0.0F, false));
		data.recordRemoval(LINE, MEMBER, 4200L);
		data.addPetition(LINE, MEMBER, "Corvane");

		BloodlineSavedData loaded = BloodlineSavedData.load(data.save(new CompoundTag(), null), null);

		assertEquals(data.noticesFor(MEMBER), loaded.noticesFor(MEMBER));
		assertEquals(4200L, loaded.removedAt(LINE, MEMBER));
		assertEquals("Corvane", loaded.petitions(LINE).get(MEMBER));
		assertEquals(2, loaded.takeNotices(MEMBER).size());
		assertTrue(loaded.noticesFor(MEMBER).isEmpty(), "taken settlements are gone");
	}

	@Test
	void settlementsStayOutOfTheTagEveryLedgerCarries() {
		BloodlineSavedData data = new BloodlineSavedData();
		Bloodline line = new Bloodline("Ashwell", LEADER, LINE, new ArrayList<>());
		data.registerBloodline(line);
		data.addNotice(MEMBER, PendingNotice.of(NoticeKind.EXPELLED, line, "Ashwell", 0.0F, true));
		data.addPetition(LINE, MEMBER, "Corvane");
		CompoundTag ledgerTag = line.serialize();
		assertFalse(ledgerTag.toString().contains("Corvane"), "petitions are not written onto ledgers");
		assertFalse(ledgerTag.contains("pendingNotices"), "settlements are not written onto ledgers");
	}

	@Test
	void onlyALedgerWrittenAtOrBeforeTheRemovalCrumbles() {
		assertFalse(BloodlineSavedData.crumbles(null, 0L), "never removed: nothing crumbles");
		assertTrue(BloodlineSavedData.crumbles(500L, 0L), "an unstamped ledger reads as written at 0");
		assertTrue(BloodlineSavedData.crumbles(500L, 500L), "written in the tick of the removal");
		assertFalse(BloodlineSavedData.crumbles(500L, 501L), "written after the removal: a fresh invitation (NQ-11-1 B)");

		BloodlineSavedData data = new BloodlineSavedData();
		data.recordRemoval(LINE, MEMBER, 500L);
		assertTrue(data.ledgerCrumbles(LINE, MEMBER, 100L));
		assertFalse(data.ledgerCrumbles(LINE, LEADER, 100L), "another holder's copy stays");
	}

	@Test
	void aLineKeepsAtMostSixteenPetitionsAndOneEach() {
		BloodlineSavedData data = new BloodlineSavedData();
		assertEquals(PetitionResult.ADDED, data.addPetition(LINE, MEMBER, "Corvane"));
		assertEquals(PetitionResult.ALREADY_WAITING, data.addPetition(LINE, MEMBER, "Corvane"));
		for (int i = 1; i < BloodlineSavedData.MAX_PETITIONS; i++) {
			assertEquals(PetitionResult.ADDED, data.addPetition(LINE, UUID.randomUUID(), "applicant " + i));
		}
		assertEquals(16, data.petitions(LINE).size());
		assertEquals(PetitionResult.FULL, data.addPetition(LINE, UUID.randomUUID(), "one too many"));
		assertEquals(List.of(LINE), data.dropPetitionsOf(MEMBER));
		assertEquals(15, data.petitions(LINE).size());
	}

	@Test
	void disbandingDropsTheLinesRecordsButKeepsWhatIsOwed() {
		BloodlineSavedData data = new BloodlineSavedData();
		Bloodline line = new Bloodline("Ashwell", LEADER, LINE, new ArrayList<>());
		data.registerBloodline(line);
		data.recordRemoval(LINE, MEMBER, 10L);
		data.addPetition(LINE, UUID.randomUUID(), "Corvane");
		data.addNotice(MEMBER, PendingNotice.of(NoticeKind.DISSOLVED, line, "", 0.0F, true));
		data.disbandBloodline(LINE);
		assertEquals(null, data.removedAt(LINE, MEMBER));
		assertTrue(data.petitions(LINE).isEmpty());
		assertEquals(1, data.noticesFor(MEMBER).size(), "a settlement owed outlives the line");
	}

	@Test
	void theDefaultNameIsALangKeyWithTheFoundersName() {
		Bloodline line = new Bloodline("Ashwell", LEADER, LINE, new ArrayList<>());
		TranslatableContents name = (TranslatableContents) line.displayName().getContents();
		assertEquals("hemomancy.bloodline.default_name", name.getKey());
		assertEquals("Ashwell", name.getArgs()[0]);
		Bloodline copy = Bloodline.deserialize(line.serialize());
		assertEquals("Ashwell", copy.getFounderName());
		assertEquals("", copy.getCustomName());
		assertEquals("hemomancy.bloodline.none",
				((TranslatableContents) Bloodline.NOBLOODLINE.displayName().getContents()).getKey());
	}
}
