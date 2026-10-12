package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnownManipulationFamilyAvailabilityTest {
	@Test
	void familyFormNeedsBothAbsorbedMemoryAndMastery() {
		BloodManipulation baseline = manipulation("blood_shot");
		BloodManipulation form = manipulation("hematic_mortar");
		KnownManipulations known = new KnownManipulations();
		LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
		map.put(baseline, new ManipLevel(2, 0));
		map.put(form, new ManipLevel(2, 0));
		known.setKnownManips(map);

		assertFalse(known.isManipulationAvailable(form));
		known.getManipLevel(baseline).setCurrentLevel(3);
		assertTrue(known.isManipulationAvailable(form));
	}

	@Test
	void legacySaveGrandfathersAlreadyKnownFamilyForms() {
		BloodManipulation form = manipulation("guided_blood_shot");
		KnownManipulations original = new KnownManipulations();
		LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
		map.put(form, new ManipLevel(0, 0));
		original.setKnownManips(map);
		ListTag legacy = original.serializeNBT(null);
		for (int i = legacy.size() - 1; i >= 0; i--) {
			CompoundTag tag = legacy.getCompound(i);
			if (tag.contains("familyMemoryVersion")) legacy.remove(i);
		}

		KnownManipulations restored = new KnownManipulations();
		restored.deserializeNBT(null, legacy);

		assertTrue(restored.getGrandfatheredFamilyForms().contains("guided_blood_shot"));
		BloodManipulation restoredForm = restored.getManipList().stream()
				.filter(manipulation -> "guided_blood_shot".equals(manipulation.getName())).findFirst().orElseThrow();
		assertTrue(restored.isManipulationAvailable(restoredForm));
	}

	@Test
	void versionOneMarionetteLeavesItsBindingMasteryBehindAndStaysUsable() {
		ListTag save = marionetteSave(1);

		KnownManipulations restored = new KnownManipulations();
		restored.deserializeNBT(null, save);

		assertEquals(3, restored.getManipLevel(named(restored, "blood_binding")).getCurrentLevel(),
				"Blood Binding mastery survived");
		assertEquals(0, restored.getManipLevel(named(restored, "hematic_rebuke")).getCurrentLevel(),
				"Old Binding mastery leaked into the Rebuke family");
		assertSame(restored.getManipLevel(named(restored, "hematic_rebuke")),
				restored.getManipLevel(named(restored, "sanguine_marionette")));
		assertTrue(restored.getGrandfatheredFamilyForms().contains("sanguine_marionette"));
		assertTrue(restored.isManipulationAvailable(named(restored, "sanguine_marionette")));
		assertTrue(restored.getEquippedManipNames().contains("sanguine_marionette"));
	}

	@Test
	void currentSaveKeepsMarionetteOnItsRebukeMastery() {
		KnownManipulations restored = new KnownManipulations();
		restored.deserializeNBT(null, marionetteSave(2));

		assertEquals(3, restored.getManipLevel(named(restored, "sanguine_marionette")).getCurrentLevel());
		assertFalse(restored.getGrandfatheredFamilyForms().contains("sanguine_marionette"));
	}

	/** Blood Binding and Marionette at L3, Hematic Rebuke at L0, Marionette equipped. */
	private static ListTag marionetteSave(int familyMemoryVersion) {
		KnownManipulations original = new KnownManipulations();
		LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
		map.put(manipulation("blood_binding"), new ManipLevel(3, 60));
		map.put(manipulation("hematic_rebuke"), new ManipLevel(0, 2));
		map.put(manipulation("sanguine_marionette"), new ManipLevel(3, 60));
		original.knownManips = map;
		original.setEquippedManipNames(java.util.List.of("sanguine_marionette"));
		ListTag save = original.serializeNBT(null);
		for (int i = 0; i < save.size(); i++) {
			CompoundTag tag = save.getCompound(i);
			if (tag.contains("familyMemoryVersion")) tag.putInt("familyMemoryVersion", familyMemoryVersion);
		}
		return save;
	}

	private static BloodManipulation named(KnownManipulations known, String name) {
		return known.getManipList().stream().filter(manipulation -> name.equals(manipulation.getName()))
				.findFirst().orElseThrow();
	}

	private static BloodManipulation manipulation(String name) {
		return new BloodManipulation(name, 10, 0, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);
	}
}
