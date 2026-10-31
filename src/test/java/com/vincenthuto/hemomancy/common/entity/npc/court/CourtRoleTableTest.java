package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Brined Court Track 9: the role table (two roles a house, a lead at each Seat, stages, strains and resident marks). */
final class CourtRoleTableTest {
	/** Every resident mark Tracks 7 and 8 left in the Seat and outpost templates, by the site that carries it. */
	private static final Map<CourtSite, List<String>> MARKS = Map.of(
			CourtSite.LIGHTHOUSE_GARDEN, List.of("lamp_keeper", "gardener", "guest_host", "arbor_anchored", "arbor_withdrawing",
					"undercroft_tender", "undercroft_anchored", "undercroft_anchored_deep"),
			CourtSite.VENT_SANCTUM, List.of("kindled_elder", "kindled_preparer"),
			CourtSite.TIDE_REFUGE, List.of("refuge_keeper", "holdfast_braced"),
			CourtSite.WAKE_NURSERY, List.of("wake_tender"),
			CourtSite.LINE_CAMP, List.of("line_tender", "pod_keeper"),
			CourtSite.SCREENED_CLEFT, List.of("cleft_host", "cleft_veiled"),
			CourtSite.STILL_BELL, List.of("bell_keeper", "bell_sleeper"),
			CourtSite.WHALE_FALL, List.of("marrow_carer", "marrow_rooted"));

	@Test
	void everyHouseHasTwoRolesAndOnlyTheSeatsHaveLeads() {
		for (CourtHouse house : CourtHouse.values()) {
			List<CourtRole> roles = CourtRole.of(house);
			assertEquals(1, roles.stream().filter(r -> r.slot() == CourtRole.Slot.A).count(), house + " role A");
			assertEquals(1, roles.stream().filter(r -> r.slot() == CourtRole.Slot.B).count(), house + " role B");
			boolean seat = house == CourtHouse.GIVING_LIGHT || house == CourtHouse.KINDLING;
			assertEquals(seat ? 1 : 0, roles.stream().filter(r -> r.slot() == CourtRole.Slot.LEAD).count(), house + " lead");
		}
		assertEquals(18, CourtRole.values().length, "16 role types plus 2 leads");
	}

	@Test
	void eachRoleWearsItsOwnHouseStrainAtAStage() {
		for (CourtRole role : CourtRole.values()) {
			assertTrue(role.stage() >= 0 && role.stage() <= 5, role + " stage");
			assertEquals(role.house(), role.strain().house(), role + " wears another house's strain");
		}
		// The plan's stages: Mature A roles, Primal or Apex elders.
		assertEquals(3, CourtRole.GARDENER.stage());
		assertEquals(5, CourtRole.ARBOR.stage());
		assertEquals(4, CourtRole.EDGE_SWIMMER.stage());
		assertEquals(3, CourtRole.OFFERING_BEARER.stage());
	}

	@Test
	void everyResidentMarkBelongsToARoleOfItsSitesHouse() {
		Set<String> seen = new HashSet<>();
		for (var entry : MARKS.entrySet()) {
			CourtHouse house = CourtHouse.byId(entry.getKey().house());
			for (String mark : entry.getValue()) {
				CourtRole role = CourtRole.forMark(mark);
				assertNotNull(role, "no role stands on " + mark);
				assertEquals(house, role.house(), mark + " is claimed by another house's role");
				assertTrue(seen.add(mark), mark + " is claimed twice");
			}
		}
		for (CourtRole role : CourtRole.values()) {
			for (String mark : role.marks()) assertTrue(seen.contains(mark), role + " claims a mark no template has: " + mark);
		}
	}

	@Test
	void onlyTheGuardingRolesDefendAndLeadsTakeMembersIn() {
		assertEquals(EnumSet.of(CourtRole.TIDEWARDEN, CourtRole.EDGE_SWIMMER),
				EnumSet.copyOf(java.util.Arrays.stream(CourtRole.values()).filter(CourtRole::defends).toList()));
		assertTrue(CourtRole.LIGHTKEEPER.offers(CourtRole.Offer.LESSON));
		assertTrue(CourtRole.LIGHTKEEPER.offers(CourtRole.Offer.JOIN));
		assertTrue(CourtRole.HEAD_TENDER.offers(CourtRole.Offer.JOIN));
		for (CourtRole role : CourtRole.values()) {
			if (role != CourtRole.LIGHTKEEPER) assertFalse(role.offers(CourtRole.Offer.LESSON), role + " teaches the lesson");
		}
	}
}
