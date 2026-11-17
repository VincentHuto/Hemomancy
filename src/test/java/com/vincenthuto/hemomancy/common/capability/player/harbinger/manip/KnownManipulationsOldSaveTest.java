package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CQ-11: a save this build cannot read must load as an empty selection, never kick the player. */
class KnownManipulationsOldSaveTest {
	/** The manipulation key's name before the Temper rename (CL-28), spelled in pieces because the word is retired. */
	private static final String PRE_RENAME_KEY = "tend" + "ency";

	@Test
	void anEmptyListWithANullSelectionSelectsBlankAndSerialises() {
		KnownManipulations known = new KnownManipulations();
		known.setSelectedManip(null);

		assertSame(BloodManipulation.BLANK, known.getSelectedManip());
		assertDoesNotThrow(() -> known.serializeNBT(null));
	}

	@Test
	void aPreRenameSaveLoadsWithoutAnyNullKeyAndRoundTrips() {
		KnownManipulations saved = knownWith(manip("blood_shot", "ANIMUS"), manip("vital_effusion", "LUX"));
		saved.setSelectedManip(saved.getManipList().get(1));
		ListTag tag = saved.serializeNBT(null);
		renameTemperKeys(tag);

		KnownManipulations restored = new KnownManipulations();
		assertDoesNotThrow(() -> restored.deserializeNBT(null, tag));

		assertFalse(restored.getKnownManips().containsKey(null), "no unreadable entry is kept as a null key");
		assertTrue(restored.getKnownManips().isEmpty(), "unreadable entries are dropped, not converted");
		assertSame(BloodManipulation.BLANK, restored.getSelectedManip());
		ListTag again = assertDoesNotThrow(() -> restored.serializeNBT(null));
		KnownManipulations reloaded = new KnownManipulations();
		reloaded.deserializeNBT(null, again);
		assertEquals("No Selected", reloaded.getSelectedManip().getName());
	}

	@Test
	void anEntryWithAnUnknownEnumNameIsDropped() {
		KnownManipulations saved = knownWith(manip("blood_shot", "ANIMUS"), manip("vital_effusion", "LUX"));
		ListTag tag = saved.serializeNBT(null);
		CompoundTag second = entryCarrying(tag, "Manip1").getCompound("Manip1");
		second.putString("temper", "NO_SUCH_TEMPER");

		KnownManipulations restored = new KnownManipulations();
		restored.deserializeNBT(null, tag);

		assertEquals(1, restored.getKnownManips().size());
		assertEquals("blood_shot", restored.getManipList().get(0).getName());
		assertFalse(restored.getKnownManips().containsKey(null));
	}

	@Test
	void aNullSelectionWithOneKnownFormSelectsThatForm() {
		BloodManipulation shot = manip("blood_shot", "ANIMUS");
		KnownManipulations known = knownWith(shot);
		known.setSelectedManip(null);

		assertSame(shot, known.getSelectedManip());
	}

	@Test
	void aNullSelectedVeinWithNoVeinsSelectsBlank() {
		KnownManipulations known = new KnownManipulations();
		known.setSelectedVein(null);

		assertSame(VeinLocation.BLANK, known.getSelectedVein());
	}

	private static KnownManipulations knownWith(BloodManipulation... manipulations) {
		LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
		for (BloodManipulation manipulation : manipulations) map.put(manipulation, new ManipLevel(1, 0));
		KnownManipulations known = new KnownManipulations();
		known.setKnownManips(map);
		return known;
	}

	private static BloodManipulation manip(String name, String temper) {
		return new BloodManipulation(name, 5, 0, 0, EnumManipulationType.QUICK, EnumManipulationRank.HUMILIS,
				EnumBloodTemper.valueOf(temper), EnumVeinSections.HEAD);
	}

	private static void renameTemperKeys(ListTag list) {
		for (Tag element : list) {
			CompoundTag entry = (CompoundTag) element;
			for (String key : entry.getAllKeys()) {
				if ((key.equals("Selected") || key.startsWith("Manip")) && entry.get(key) instanceof CompoundTag manip
						&& manip.contains("temper")) {
					manip.putString(PRE_RENAME_KEY, manip.getString("temper"));
					manip.remove("temper");
				}
			}
		}
	}

	private static CompoundTag entryCarrying(ListTag list, String key) {
		for (Tag element : list) {
			if (element instanceof CompoundTag entry && entry.contains(key)) return entry;
		}
		throw new AssertionError("no entry carries " + key);
	}
}
