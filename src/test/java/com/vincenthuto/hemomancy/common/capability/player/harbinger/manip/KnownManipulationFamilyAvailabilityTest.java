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

import static org.junit.jupiter.api.Assertions.assertFalse;
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

	private static BloodManipulation manipulation(String name) {
		return new BloodManipulation(name, 10, 0, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);
	}
}
