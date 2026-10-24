package com.vincenthuto.hemomancy.common.entity.utility.arbor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborOfWillLayoutTest {
	// Shipped skill counts per family and required degree (D8 Mycelial clamps onto the seventh whorl).
	private static final Object[][] SHIPPED_BANDS = {
			{"core", 1, 4}, {"core", 2, 4}, {"core", 3, 4}, {"core", 4, 4}, {"core", 5, 6}, {"core", 6, 3},
			{"covenant", 5, 2}, {"covenant", 6, 2}, {"covenant", 7, 1},
			{"living_staff", 1, 1}, {"living_staff", 2, 2}, {"living_staff", 3, 8}, {"living_staff", 4, 4},
			{"living_staff", 5, 1}, {"living_staff", 6, 2}, {"living_staff", 7, 1},
			{"mycelial", 4, 2}, {"mycelial", 5, 2}, {"mycelial", 7, 1}, {"mycelial", 8, 1},
			{"scars", 3, 1}, {"scars", 4, 1}, {"scars", 6, 2}, {"scars", 7, 2},
			{"summons", 2, 1}, {"summons", 3, 2}, {"summons", 4, 2}, {"summons", 5, 3}, {"summons", 6, 2}};
	// The built floor reaches the full chamber radius; fruit must hang over it with half a block to spare.
	private static final double FLOOR_MARGIN = 0.5;

	@Test
	void placesEveryNonRootSkillOnceOnItsRequiredDegreeWhorl() {
		List<ArborOfWillLayout.SkillSpec> skills = List.of(
				new ArborOfWillLayout.SkillSpec(1, "core", 1, 1, 0),
				new ArborOfWillLayout.SkillSpec(2, "living_staff", 2, 2, 0),
				new ArborOfWillLayout.SkillSpec(3, "summons", 4, 2, 0),
				new ArborOfWillLayout.SkillSpec(4, "mycelial", 7, 3, 0));

		List<ArborOfWillLayout.FruitPlacement> result = ArborOfWillLayout.place(skills, 10.0, 7);

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

		List<ArborOfWillLayout.FruitPlacement> result = ArborOfWillLayout.place(skills, 8.0, 6);

		assertEquals(3, result.stream().map(p -> p.x() + ":" + p.y() + ":" + p.z()).distinct().count());
		assertTrue(result.stream().allMatch(p -> Math.hypot(p.x(), p.z()) <= 8.0 - FLOOR_MARGIN + 0.0001));
	}

	@Test
	void preservesCurrentTwoDimensionalFamilyOrderAroundSpiral() {
		assertEquals(List.of("core", "living_staff", "mycelial", "scars", "covenant", "summons"),
				ArborOfWillLayout.orderedFamilies());
	}

	@Test
	void shippedFruitNeverSinksIntoTheFloor() {
		for (int degree = 1; degree <= 8; degree++) {
			for (ArborOfWillLayout.FruitPlacement fruit : visible(degree)) {
				assertTrue(fruit.y() >= 0.8, "D" + degree + " skill " + fruit.skillId() + " hangs into the floor at " + fruit.y());
			}
		}
	}

	@Test
	void shippedFruitClustersDoNotCrowdEachOtherAtAnyDegree() {
		for (int degree = 1; degree <= 8; degree++) {
			List<ArborOfWillLayout.FruitPlacement> fruits = visible(degree);
			for (int i = 0; i < fruits.size(); i++) {
				for (int j = i + 1; j < fruits.size(); j++) {
					ArborOfWillLayout.FruitPlacement a = fruits.get(i), b = fruits.get(j);
					double distance = Math.sqrt(Math.pow(a.x() - b.x(), 2) + Math.pow(a.y() - b.y(), 2)
							+ Math.pow(a.z() - b.z(), 2));
					assertTrue(distance >= 0.45, "D" + degree + " skills " + a.skillId() + " and " + b.skillId()
							+ " are only " + distance + " apart");
				}
			}
		}
	}

	@Test
	void visibleFruitStaysInsideTheChamberAndIgnoresChamberSize() {
		for (int degree = 1; degree <= 8; degree++) {
			int chamberRadius = chamberRadius(degree);
			List<ArborOfWillLayout.FruitPlacement> current = ArborOfWillLayout.place(shippedSkills(), chamberRadius, degree);
			List<ArborOfWillLayout.FruitPlacement> roomy = ArborOfWillLayout.place(shippedSkills(), 10.0, degree);
			for (int i = 0; i < current.size(); i++) {
				ArborOfWillLayout.FruitPlacement fruit = current.get(i);
				if (fruit.whorl() > Math.min(7, degree)) continue;
				assertTrue(Math.hypot(fruit.x(), fruit.z()) <= chamberRadius - FLOOR_MARGIN + 1.0e-4,
						"D" + degree + " skill " + fruit.skillId() + " leaves the chamber");
				assertEquals(roomy.get(i).x(), fruit.x(), 1.0e-9);
				assertEquals(roomy.get(i).y(), fruit.y(), 1.0e-9);
				assertEquals(roomy.get(i).z(), fruit.z(), 1.0e-9);
			}
		}
	}

	@Test
	void higherDegreesWeepFurtherOutAndLower() {
		List<ArborOfWillLayout.FruitPlacement> fruits = ArborOfWillLayout.place(shippedSkills(), 10.0, 7);
		double previousRadius = 0, previousHeight = Double.MAX_VALUE;
		for (int whorl = 1; whorl <= 7; whorl++) {
			int w = whorl;
			double radius = fruits.stream().filter(p -> p.whorl() == w)
					.mapToDouble(p -> Math.hypot(p.x(), p.z())).average().orElseThrow();
			double height = fruits.stream().filter(p -> p.whorl() == w)
					.mapToDouble(ArborOfWillLayout.FruitPlacement::y).average().orElseThrow();
			assertTrue(radius > previousRadius, "whorl " + whorl + " is not further out");
			assertTrue(height < previousHeight, "whorl " + whorl + " does not hang lower");
			previousRadius = radius;
			previousHeight = height;
		}
	}

	private static List<ArborOfWillLayout.FruitPlacement> visible(int degree) {
		return ArborOfWillLayout.place(shippedSkills(), chamberRadius(degree), degree).stream()
				.filter(fruit -> fruit.whorl() <= Math.min(7, degree)).toList();
	}

	private static int chamberRadius(int degree) {
		return Math.max(3, Math.min(10, degree + 2));
	}

	static List<ArborOfWillLayout.SkillSpec> shippedSkills() {
		List<ArborOfWillLayout.SkillSpec> specs = new ArrayList<>();
		int id = 1;
		for (Object[] band : SHIPPED_BANDS) {
			for (int slot = 0; slot < (int) band[2]; slot++) {
				specs.add(new ArborOfWillLayout.SkillSpec(id++, (String) band[0], (int) band[1], slot % 4, slot));
			}
		}
		return specs;
	}
}
