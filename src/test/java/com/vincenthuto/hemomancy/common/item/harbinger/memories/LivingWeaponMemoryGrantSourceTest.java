package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertShowsEnglish;

class LivingWeaponMemoryGrantSourceTest {
	@Test
	void livingWeaponMemoryGrantSourceContracts() {
		String helper = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulationGrantHelper.java");
		String memoryItem = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java");
		String unlocks = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponMemoryUnlocks.java");

		assertContains("helper exposes memory grant result", helper, "record MemoryGrantResult");
		assertContains("helper exposes status enum", helper, "enum MemoryGrantStatus");
		assertContains("helper validates active blood", helper, "NO_ACTIVE_BLOOD");
		assertContains("helper validates rank gate", helper, "RANK_TOO_LOW");
		assertContains("helper validates already known", helper, "ALREADY_KNOWN");
		assertContains("helper grants and syncs player memory", helper, "grantMemory(ServerPlayer player");
		assertContains("helper syncs known manipulations", helper, "KnownManipulationEvents.syncPlayerEvent(player)");
		assertContains("helper preserves guarded open-slot auto equip", helper, "known.equipManip");

		assertShowsEnglish("memory item directs learning through brazier absorption", memoryItem, "lit Iron Brazier");
		assertContains("memory item keeps retired memory guard", memoryItem, "isRetiredMemoryItem()");

		assertContains("living weapon unlock helper exists", unlocks, "final class LivingWeaponMemoryUnlocks");
		assertContains("unlock helper delegates blade", unlocks, "ManipulationInit.conjure_blade");
		assertContains("unlock helper delegates axe", unlocks, "ManipulationInit.conjure_axe");
		assertContains("unlock helper delegates spear", unlocks, "ManipulationInit.conjure_spear");
		assertContains("unlock helper delegates claws", unlocks, "ManipulationInit.conjure_claws");
		assertContains("unlock helper delegates crossbow", unlocks, "ManipulationInit.conjure_crossbow");
		assertContains("unlock helper delegates torch", unlocks, "ManipulationInit.conjure_torch");
		assertContains("unlock helper delegates flail", unlocks, "ManipulationInit.conjure_flail");
		assertContains("unlock helper uses shared grant", unlocks, "KnownManipulationGrantHelper.grantMemory");
	}
}
