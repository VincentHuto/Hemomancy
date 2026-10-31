package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourtDiscoveryDialogueTest {
	private static CourtDiscoveryDialogue.View view(int degree, CourtProgress.Route knownBy, boolean letterLost,
			boolean carryingChart, int maturity) {
		return new CourtDiscoveryDialogue.View(degree, knownBy != null, knownBy, letterLost, carryingChart, maturity);
	}

	@Test
	void theVicarRefersAtTheFifthDegreeWithTheBearingFirst() {
		DialogueTree base = HarbingerVicarDialogueTrees.votary(42);
		assertSame(base, CourtDiscoveryDialogue.withVicar(base, view(4, null, false, false, -1), true));
		DialogueTree referral = CourtDiscoveryDialogue.withVicar(base, view(5, null, false, false, -1), true);
		assertTrue(hasOption(referral, CourtDiscoveryDialogue.NODE));
		assertTrue(hasEvent(referral.getNode(CourtDiscoveryDialogue.NODE), VoyagerIntroductionDialogue.BEARING));
		assertFalse(hasEvent(CourtDiscoveryDialogue.withVicar(base, view(5, null, false, false, -1), false)
				.getNode(CourtDiscoveryDialogue.NODE), VoyagerIntroductionDialogue.BEARING));
	}

	@Test
	void theSecondRouteAcknowledgesTheFirst() {
		for (CourtProgress.Route route : CourtProgress.Route.values()) {
			String suffix = ".court.known." + route.serializedName();
			assertTrue(CourtDiscoveryDialogue.withVicar(HarbingerVicarDialogueTrees.votary(42), view(5, route, false, false, -1),
					true).getNode(CourtDiscoveryDialogue.NODE).lines().contains("hemomancy.vicar" + suffix));
			assertTrue(CourtDiscoveryDialogue.withVoyager(HarbingerVoyagerDialogueTrees.forDegree(5, 42),
					view(5, route, false, false, -1)).getNode(CourtDiscoveryDialogue.NODE).lines()
					.contains("hemomancy.voyager" + suffix));
		}
	}

	@Test
	void theVoyagerIssuesAndReissuesTheChart() {
		DialogueTree base = HarbingerVoyagerDialogueTrees.forDegree(2, 42);
		assertSame(base, CourtDiscoveryDialogue.withVoyager(base, view(4, null, false, false, -1)));
		DialogueTree unknown = CourtDiscoveryDialogue.withVoyager(HarbingerVoyagerDialogueTrees.forDegree(5, 42),
				view(5, null, false, false, -1));
		assertTrue(hasEvent(unknown.getNode(CourtDiscoveryDialogue.NODE), CourtDiscoveryDialogue.VOYAGER_LEARN));
		DialogueTree known = CourtDiscoveryDialogue.withVoyager(base, view(2, CourtProgress.Route.FOUND, false, false, -1));
		assertTrue(hasEvent(known.getNode(CourtDiscoveryDialogue.NODE), CourtDiscoveryDialogue.VOYAGER_CHART));
		DialogueTree carrying = CourtDiscoveryDialogue.withVoyager(base, view(2, CourtProgress.Route.FOUND, false, true, -1));
		assertFalse(hasEvent(carrying.getNode(CourtDiscoveryDialogue.NODE), CourtDiscoveryDialogue.VOYAGER_CHART));
		DialogueTree lost = CourtDiscoveryDialogue.withVoyager(base, view(2, null, true, false, -1));
		assertTrue(hasEvent(lost.getNode(CourtDiscoveryDialogue.NODE), CourtDiscoveryDialogue.VOYAGER_LETTER));
	}

	@Test
	void theAlchemistNeedsAMatureStrain() {
		DialogueTree base = HarbingerAlchemistDialogueTrees.purifying(42);
		assertSame(base, CourtDiscoveryDialogue.withAlchemist(base, view(5, null, false, false, 2)));
		DialogueTree lead = CourtDiscoveryDialogue.withAlchemist(base, view(5, null, false, false, 3));
		assertTrue(hasEvent(lead.getNode(CourtDiscoveryDialogue.NODE), CourtDiscoveryDialogue.ALCHEMIST_LEAD));
		DialogueTree known = CourtDiscoveryDialogue.withAlchemist(base, view(5, CourtProgress.Route.LETTER, false, false, 3));
		assertEquals(List.of("hemomancy.alchemist.court.known.letter"),
				known.getNode(CourtDiscoveryDialogue.NODE).lines());
	}

	@Test
	void theLetterInquiryRecordsOnlyWhileTheCourtIsUnknown() {
		DialogueTree base = HarbingerVoyagerDialogueTrees.forDegree(2, 42);
		DialogueTree inquiry = DialogueItemInquiryNodes.addResolvedInventoryItemInquiries(base,
				Map.of(CourtDiscoveryDialogue.LETTER, List.of("hemomancy.voyager.item_inquiry.brine_sealed_letter.line1")));
		String id = DialogueItemInquiryNodes.inventoryNodeId(CourtDiscoveryDialogue.LETTER);
		DialogueTree unknown = CourtDiscoveryDialogue.withLetterInquiry(inquiry, "voyager",
				CourtDiscoveryDialogue.VOYAGER_LETTER, view(2, null, false, false, -1));
		assertTrue(hasEvent(unknown.getNode(id), CourtDiscoveryDialogue.VOYAGER_LETTER));
		DialogueTree known = CourtDiscoveryDialogue.withLetterInquiry(inquiry, "voyager",
				CourtDiscoveryDialogue.VOYAGER_LETTER, view(2, CourtProgress.Route.ALCHEMIST, false, false, -1));
		assertFalse(hasEvent(known.getNode(id), CourtDiscoveryDialogue.VOYAGER_LETTER));
		assertTrue(known.getNode(id).lines().contains("hemomancy.voyager.court.known.alchemist"));
	}

	private static boolean hasEvent(DialogueNode node, String eventId) {
		return node.options().stream().anyMatch(option -> eventId.equals(option.eventId()));
	}

	private static boolean hasOption(DialogueTree tree, String nodeId) {
		return tree.getStartNode().options().stream().anyMatch(option -> nodeId.equals(option.nextNodeId()));
	}
}
