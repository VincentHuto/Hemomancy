package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SkillTreeFilterTest {
	@Test
	void degreeAndFamilyFiltersApplyWithinTheActiveLayer() {
		SkillPoint core = skill(1, 3, "core");
		SkillPoint summon = skill(2, 5, "summons");

		assertTrue(SkillTreeFilter.includes(core, SkillTreeLayer.SURFACE, null, null));
		assertTrue(SkillTreeFilter.includes(core, SkillTreeLayer.SURFACE, 3, "core"));
		assertFalse(SkillTreeFilter.includes(core, SkillTreeLayer.SURFACE, 2, "core"));
		assertFalse(SkillTreeFilter.includes(core, SkillTreeLayer.SURFACE, 3, "summons"));
		assertFalse(SkillTreeFilter.includes(summon, SkillTreeLayer.SURFACE, null, null));
		assertTrue(SkillTreeFilter.includes(summon, SkillTreeLayer.DEEP, 5, "summons"));
	}

	private static SkillPoint skill(int id, int degree, String branch) {
		return new SkillPoint(id, "skill_" + id, 100, 3, EnumSkillStates.LOCKED, null)
				.setRequiredDegree(degree).setBranch(branch);
	}
}
