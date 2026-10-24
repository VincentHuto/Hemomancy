package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertShowsEnglish;

class BloodMemoryItemAutoEquipSourceTest {
	@Test
	void bloodMemoryItemAutoEquipSourceContracts() {
		String source = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java");

		assertNotContains("blood memory cannot be learned by right click", source,
				"KnownManipulationGrantHelper.grantMemory");
		assertShowsEnglish("blood memory directs players to the brazier rite", source,
				"Burn this memory in a lit Iron Brazier");
	}
}
