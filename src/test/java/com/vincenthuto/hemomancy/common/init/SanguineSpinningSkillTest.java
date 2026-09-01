package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.skills.SummonSkillBranch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SanguineSpinningSkillTest {
	@Test
	void registersAsTheDegreeFiveFarTetherTechnique() {
		List<SkillPoint> branch = new ArrayList<>();

		SummonSkillBranch.register(branch);

		SkillPoint skill = SkillPointInit.skill_sanguine_spinning;
		assertTrue(branch.contains(skill));
		assertEquals(72, skill.getId());
		assertEquals("skill_sanguine_spinning", skill.getName());
		assertEquals(1, skill.getMaxLevels());
		assertEquals(3, skill.getSkillPointCost());
		assertEquals(5, skill.getRequiredDegree());
		assertSame(SkillPointInit.skill_far_tether, skill.getParent());
		assertTrue(skill.isToggleable());
		assertEquals(548, skill.getTreeX());
		assertEquals(552, skill.getTreeY());
	}
}
