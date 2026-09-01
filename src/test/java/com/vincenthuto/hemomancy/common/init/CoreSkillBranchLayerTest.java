package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.client.screen.skilltree.shared.SkillTreeLayer;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.SkillTreeLayerRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.skills.CoreSkillBranch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreSkillBranchLayerTest {
	@Test
	void regularBaseSkillRemainsTheSurfaceRoot() {
		CoreSkillBranch.register(new ArrayList<SkillPoint>());

		assertEquals(SkillTreeLayer.SURFACE,
				SkillTreeLayerRules.layerForDegree(SkillPointInit.base_skill.getRequiredDegree()));
		assertEquals(SkillTreeLayer.DEEP,
				SkillTreeLayerRules.layerForDegree(SkillPointInit.deep_base_skill.getRequiredDegree()));
	}
}
