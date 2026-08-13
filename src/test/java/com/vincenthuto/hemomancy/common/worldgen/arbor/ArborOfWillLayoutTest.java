package com.vincenthuto.hemomancy.common.worldgen.arbor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArborOfWillLayoutTest {
	@Test
	void placesEveryNonRootSkillOnceOnItsRequiredDegreeWhorl() {
		List<ArborOfWillLayout.SkillSpec> skills = List.of(
				new ArborOfWillLayout.SkillSpec(1, "core", 1, 1, 0),
				new ArborOfWillLayout.SkillSpec(2, "living_staff", 2, 2, 0),
				new ArborOfWillLayout.SkillSpec(3, "summons", 4, 2, 0),
				new ArborOfWillLayout.SkillSpec(4, "mycelial", 7, 3, 0));

		List<ArborOfWillLayout.FruitPlacement> result = ArborOfWillLayout.place(skills, 10.0);

		assertEquals(4, result.size());
		assertEquals(4, result.stream().map(ArborOfWillLayout.FruitPlacement::skillId).distinct().count());
		assertEquals(4, result.stream().filter(p -> p.whorl() == 7).findFirst().orElseThrow().skillId());
	}

	@Test
	void keepsAuthoredSlotsUniqueAndInsideWalkableCanopy() {
		List<ArborOfWillLayout.SkillSpec> skills = List.of(
				new ArborOfWillLayout.SkillSpec(10, "core", 5, 3, 0),
				new ArborOfWillLayout.SkillSpec(11, "core", 5, 4, 1),
				new ArborOfWillLayout.SkillSpec(12, "core", 5, 5, 2));

		List<ArborOfWillLayout.FruitPlacement> result = ArborOfWillLayout.place(skills, 8.0);

		assertEquals(3, result.stream().map(p -> p.x() + ":" + p.y() + ":" + p.z()).distinct().count());
		assertTrue(result.stream().allMatch(p -> Math.hypot(p.x(), p.z()) <= 8.0 * 0.67 + 0.0001));
	}

	@Test
	void preservesCurrentTwoDimensionalFamilyOrderAroundSpiral() {
		assertEquals(List.of("core", "living_staff", "mycelial", "scars", "covenant", "summons"),
				ArborOfWillLayout.orderedFamilies());
	}
}
