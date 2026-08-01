package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;

final class SkillTreeFilter {
	private SkillTreeFilter() {}

	static boolean includes(SkillPoint skill, SkillTreeLayer layer, Integer degree, String family) {
		return SkillTreeLayerRules.layerForDegree(skill.getRequiredDegree()) == layer
				&& (degree == null || SkillTreeLayerRules.layerForDegree(degree) != layer
						|| skill.getRequiredDegree() == degree)
				&& (family == null || family.equals(skill.getBranch()));
	}
}
