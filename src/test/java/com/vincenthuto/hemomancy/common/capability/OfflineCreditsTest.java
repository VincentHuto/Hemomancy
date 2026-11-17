package com.vincenthuto.hemomancy.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** PT-17 and PT-20: credit earned offline is kept by UUID and handed over once. */
class OfflineCreditsTest {
	private static final UUID CRAFTER = UUID.fromString("00000000-0000-0000-0000-000000000017");
	private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000020");
	private static final ResourceLocation LIBER = ResourceLocation.parse("hemomancy:liber_sanguinum");
	private static final ResourceLocation SHOT = ResourceLocation.parse("hemomancy:memory_blood_shot");

	@Test
	void creditsAreTakenOnceInTheOrderEarned() {
		OfflineCredits credits = new OfflineCredits();
		credits.owe(CRAFTER, new OfflineCredits.Credit(OfflineCredits.Kind.STRUCTURE_CRAFT, LIBER));
		credits.owe(CRAFTER, new OfflineCredits.Credit(OfflineCredits.Kind.LOOM_WEAVE, SHOT));

		assertEquals(List.of(new OfflineCredits.Credit(OfflineCredits.Kind.STRUCTURE_CRAFT, LIBER),
				new OfflineCredits.Credit(OfflineCredits.Kind.LOOM_WEAVE, SHOT)), credits.take(CRAFTER));
		assertEquals(List.of(), credits.take(CRAFTER));
	}

	@Test
	void anotherPlayersLoginTakesNothing() {
		OfflineCredits credits = new OfflineCredits();
		credits.owe(CRAFTER, new OfflineCredits.Credit(OfflineCredits.Kind.STRUCTURE_CRAFT, LIBER));

		assertEquals(List.of(), credits.take(OTHER));
		assertEquals(1, credits.take(CRAFTER).size());
	}

	@Test
	void owingAndTakingMarkTheWorldDirty() {
		OfflineCredits credits = new OfflineCredits();
		assertFalse(credits.isDirty());
		credits.take(CRAFTER);
		assertFalse(credits.isDirty(), "taking nothing changes nothing");
		credits.owe(CRAFTER, new OfflineCredits.Credit(OfflineCredits.Kind.LOOM_WEAVE_GANGLIONIC, SHOT));
		assertTrue(credits.isDirty());
	}

	@Test
	void creditsSurviveASaveAndLoad() {
		OfflineCredits credits = new OfflineCredits();
		credits.owe(CRAFTER, new OfflineCredits.Credit(OfflineCredits.Kind.STRUCTURE_CRAFT, LIBER));
		credits.owe(OTHER, new OfflineCredits.Credit(OfflineCredits.Kind.LOOM_WEAVE_GANGLIONIC, SHOT));

		OfflineCredits loaded = OfflineCredits.load(credits.save(new CompoundTag(), null), null);

		assertEquals(List.of(new OfflineCredits.Credit(OfflineCredits.Kind.STRUCTURE_CRAFT, LIBER)), loaded.take(CRAFTER));
		assertEquals(List.of(new OfflineCredits.Credit(OfflineCredits.Kind.LOOM_WEAVE_GANGLIONIC, SHOT)), loaded.take(OTHER));
	}

	@Test
	void aCreditOfAnUnknownKindIsDroppedOnLoad() {
		CompoundTag entry = new CompoundTag();
		entry.putString("Kind", "no_such_kind");
		entry.putString("Item", LIBER.toString());
		CompoundTag players = new CompoundTag();
		ListTag list = new ListTag();
		list.add(entry);
		players.put(CRAFTER.toString(), list);
		CompoundTag tag = new CompoundTag();
		tag.put("Owed", players);

		assertEquals(List.of(), OfflineCredits.load(tag, null).take(CRAFTER));
	}
}
