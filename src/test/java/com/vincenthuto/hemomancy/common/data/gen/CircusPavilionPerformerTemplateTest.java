package com.vincenthuto.hemomancy.common.data.gen;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusPavilionPerformerTemplateTest {
	@Test
	void pavilionFloorUsesACircularFootprint() {
		var blocks = CircusPavilionTemplate.blocks();
		assertTrue(blocks.stream().anyMatch(block -> block.x() == 5 && block.y() == 0 && block.z() == 16));
		assertEquals(0, blocks.stream()
				.filter(block -> block.x() == 2 && block.y() == 0 && block.z() == 2)
				.count());
	}

	@Test
	void pavilionRoofHasLowCircularEavesAndASteepTaper() {
		var roof = CircusPavilionTemplate.blocks().stream()
				.filter(block -> block.name().endsWith("wool"))
				.toList();
		assertTrue(roof.stream().anyMatch(block -> block.x() == 1 && block.y() == 7 && block.z() == 16));
		assertTrue(roof.stream().anyMatch(block -> block.x() == 16 && block.y() == 7 && block.z() == 31));
		assertEquals(0, roof.stream().filter(block -> block.x() == 4 && block.z() == 4).count());
		assertTrue(roof.stream().anyMatch(block -> block.x() == 6 && block.y() == 8 && block.z() == 16));
		assertTrue(roof.stream().anyMatch(block -> block.x() == 9 && block.y() == 9 && block.z() == 16));
		assertTrue(roof.stream().anyMatch(block -> block.x() == 10 && block.y() == 10 && block.z() == 16));
		assertTrue(roof.stream().anyMatch(block -> block.x() == 16 && block.y() == 11 && block.z() == 16));
	}

	@Test
	void pavilionContainsOneOfEachReusablePerformer() {
		var performers = CircusPavilionTemplate.performers();
		assertEquals(4, performers.size());
		assertEquals(Set.of(
				"hemomancy:circus_fire_eater",
				"hemomancy:circus_stilt_walker",
				"hemomancy:circus_acrobat",
				"hemomancy:circus_knife_thrower"),
				performers.stream().map(CircusPavilionTemplate.PerformerPlacement::entityId)
						.collect(Collectors.toSet()));
		assertTrue(performers.stream().allMatch(performer -> performer.x() >= 0.0D
				&& performer.x() < CircusPavilionTemplate.WIDTH
				&& performer.z() >= 0.0D && performer.z() < CircusPavilionTemplate.DEPTH));
	}

	@Test
	void pavilionContainsOneCenteredCarousel() {
		var carousel = CircusPavilionTemplate.carousel();
		assertEquals("hemomancy:circus_carousel", carousel.entityId());
		assertEquals(16.5D, carousel.x());
		assertEquals(1.0D, carousel.y());
		assertEquals(16.5D, carousel.z());
	}

	@Test
	void pavilionContainsOneRingmasterOnTheRearRafter() {
		var ringmaster = CircusPavilionTemplate.ringmaster();
		assertEquals("hemomancy:circus_ringmaster", ringmaster.entityId());
		assertEquals(16.5D, ringmaster.x());
		assertEquals(8.0D, ringmaster.y());
		assertEquals(21.5D, ringmaster.z());
		assertEquals(15, CircusPavilionTemplate.blocks().stream()
				.filter(block -> block.y() == 7 && block.z() == 21 && block.x() >= 9 && block.x() <= 23)
				.count());
		assertEquals(0, CircusPavilionTemplate.blocks().stream()
				.filter(block -> block.x() == 16 && block.z() == 21 && block.y() >= 8 && block.y() <= 10)
				.count(), "Ringmaster hat needs three unobstructed blocks above the rafter");
	}

	@Test
	void pavilionHasRaftersOnAllFourSides() {
		var rafters = CircusPavilionTemplate.blocks().stream()
				.filter(block -> block.y() == 7 && "hemomancy:hematic_iron_pillar".equals(block.name()))
				.map(block -> block.x() + ":" + block.z())
				.collect(Collectors.toSet());

		for (int offset = 9; offset <= 23; offset++) {
			assertTrue(rafters.contains(offset + ":11"), "missing north rafter at " + offset + ":11");
			assertTrue(rafters.contains(offset + ":21"), "missing south rafter at " + offset + ":21");
			assertTrue(rafters.contains("11:" + offset), "missing west rafter at 11:" + offset);
			assertTrue(rafters.contains("21:" + offset), "missing east rafter at 21:" + offset);
		}
	}

	@Test
	void pavilionCurtainsSealTheOuterEavesExceptForTheFiveWideEntrance() {
		var blocks = CircusPavilionTemplate.blocks().stream()
				.collect(Collectors.toMap(block -> block.x() + ":" + block.y() + ":" + block.z(),
						CircusPavilionTemplate.BlockPlacement::name));

		for (int x = 1; x < CircusPavilionTemplate.WIDTH - 1; x++) {
			for (int z = 1; z < CircusPavilionTemplate.DEPTH - 1; z++) {
				int dx = x - CircusPavilionTemplate.CENTER;
				int dz = z - CircusPavilionTemplate.CENTER;
				boolean inside = dx * dx + dz * dz <= 225;
				boolean outerEdge = inside && (dx * dx + (dz - 1) * (dz - 1) > 225
						|| dx * dx + (dz + 1) * (dz + 1) > 225
						|| (dx - 1) * (dx - 1) + dz * dz > 225
						|| (dx + 1) * (dx + 1) + dz * dz > 225);
				if (!outerEdge) continue;

				boolean entrance = z < CircusPavilionTemplate.CENTER && x >= 14 && x <= 18;
				for (int y = 1; y <= 6; y++) {
					String block = blocks.get(x + ":" + y + ":" + z);
					if (entrance) {
						assertFalse("hemomancy:circus_curtain".equals(block),
								"entrance blocked at " + x + ":" + y + ":" + z);
					} else {
						assertEquals("hemomancy:circus_curtain", block,
								"curtain gap at " + x + ":" + y + ":" + z);
					}
				}
			}
		}
	}

	@Test
	void pavilionCurtainsFormOneConnectedWall() {
		Set<String> curtains = CircusPavilionTemplate.blocks().stream()
				.filter(block -> block.y() == 1 && "hemomancy:circus_curtain".equals(block.name()))
				.map(block -> block.x() + ":" + block.z())
				.collect(Collectors.toSet());
		Set<String> reached = new HashSet<>();
		var pending = new ArrayDeque<String>();
		pending.add(curtains.iterator().next());

		while (!pending.isEmpty()) {
			String position = pending.removeFirst();
			if (!reached.add(position)) continue;
			String[] coordinates = position.split(":");
			int x = Integer.parseInt(coordinates[0]);
			int z = Integer.parseInt(coordinates[1]);
			for (String neighbor : Set.of((x - 1) + ":" + z, (x + 1) + ":" + z,
					x + ":" + (z - 1), x + ":" + (z + 1))) {
				if (curtains.contains(neighbor) && !reached.contains(neighbor)) pending.add(neighbor);
			}
		}

		assertEquals(curtains, reached, "diagonal gaps stop adjacent curtain panes from connecting");
	}

	@Test
	void pavilionClearsTwoBlocksAboveItsFloor() {
		Set<String> blocks = CircusPavilionTemplate.blocks().stream()
				.map(block -> block.x() + ":" + block.y() + ":" + block.z())
				.collect(Collectors.toSet());
		for (String floor : blocks.stream().filter(position -> position.split(":")[1].equals("0")).toList()) {
			String[] coordinates = floor.split(":");
			assertTrue(blocks.contains(coordinates[0] + ":1:" + coordinates[2]), "uncleared floor cell above " + floor);
			assertTrue(blocks.contains(coordinates[0] + ":2:" + coordinates[2]), "uncleared tall-grass cell above " + floor);
		}
	}

	@Test
	void pavilionDisplaysTwoCapturedPrismCuttles() {
		var jars = CircusPavilionTemplate.blocks().stream()
				.filter(block -> "hemomancy:specimen_jar".equals(block.name()))
				.toList();
		assertEquals(2, jars.size());
		assertTrue(jars.stream().allMatch(block -> "hemomancy:prism_cuttle".equals(block.specimenId())));
	}
}
