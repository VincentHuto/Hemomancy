package com.vincenthuto.hemomancy.common.data.gen;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	void pavilionDisplaysTwoCapturedPrismCuttles() {
		var jars = CircusPavilionTemplate.blocks().stream()
				.filter(block -> "hemomancy:specimen_jar".equals(block.name()))
				.toList();
		assertEquals(2, jars.size());
		assertTrue(jars.stream().allMatch(block -> "hemomancy:prism_cuttle".equals(block.specimenId())));
	}
}
