package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SkillTraceDeepParentsTest {
	private final SkillPoint deepBase = skill(38, 5, null);
	private final SkillPoint surfaceParent = skill(1, 4, null);
	private final SkillPoint otherSurfaceParent = skill(2, 3, null);

	@Test
	void deepRootDrawsOneHubTraceColouredByItsSurfacePrerequisite() {
		SkillPoint root = skill(10, 6, surfaceParent).addParents(deepBase);

		assertEquals(List.of(surfaceParent), SkillTraceLayerCache.deepTraceParents(root, deepBase));
	}

	@Test
	void deepBaseListedFirstStillYieldsASingleHubTrace() {
		SkillPoint root = skill(11, 5, deepBase).addParents(surfaceParent);

		assertEquals(List.of(surfaceParent), SkillTraceLayerCache.deepTraceParents(root, deepBase));
	}

	@Test
	void deepRootWithSeveralSurfacePrerequisitesNeverStacksHubTraces() {
		SkillPoint root = skill(12, 5, surfaceParent).addParents(otherSurfaceParent, deepBase);

		assertEquals(List.of(surfaceParent), SkillTraceLayerCache.deepTraceParents(root, deepBase));
	}

	@Test
	void nodeWithDeepParentsTracesOnlyToThem() {
		SkillPoint deepParent = skill(20, 5, surfaceParent).addParents(deepBase);
		SkillPoint secondDeepParent = skill(21, 6, deepParent).addParents(deepBase);
		SkillPoint child = skill(22, 7, deepParent).addParents(secondDeepParent, deepBase, surfaceParent);

		assertEquals(List.of(deepParent, secondDeepParent), SkillTraceLayerCache.deepTraceParents(child, deepBase));
	}

	@Test
	void onlyTheDeepBaseFallsBackToTheHub() {
		SkillPoint root = skill(13, 5, deepBase);

		assertEquals(List.of(deepBase), SkillTraceLayerCache.deepTraceParents(root, deepBase));
		assertEquals(List.of(), SkillTraceLayerCache.deepTraceParents(deepBase, deepBase));
	}

	@Test
	void deepBaseAndSurfaceSkillsAreHubParents() {
		assertFalse(SkillTraceLayerCache.isDeepParent(deepBase, deepBase));
		assertFalse(SkillTraceLayerCache.isDeepParent(surfaceParent, deepBase));
		assertTrue(SkillTraceLayerCache.isDeepParent(skill(30, 8, null), deepBase));
	}

	private static SkillPoint skill(int id, int degree, SkillPoint parent) {
		return new SkillPoint(id, "skill_" + id, 100, 3, EnumSkillStates.LOCKED, parent).setRequiredDegree(degree);
	}
}
