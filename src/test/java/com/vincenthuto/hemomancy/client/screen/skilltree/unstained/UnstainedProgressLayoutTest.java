package com.vincenthuto.hemomancy.client.screen.skilltree.unstained;

import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Kind;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Node;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Pip;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Progress;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Rect;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Status;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.View;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedPhase;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances.Observance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnstainedProgressLayoutTest {

	private static final List<String> FULL_PATH = List.of(
			"lethean_baptism",
			"purity_corrupted", "purity_tainted", "purity_cleansing", "purity_absolved", "purity_purified",
			"closed_vein", "consecrated_copper", "clarity_ascension",
			"clarity_awakened", "clarity_discerning", "clarity_vigilant", "clarity_resolute", "clarity_enlightened");

	/** Purification-route progress: every phase past the novitiate began with Lethean Baptism. */
	private static UnstainedProgressLayout layout(UnstainedPhase phase, EnumPurityStage purity,
			boolean prepared, boolean clarityUnlocked, EnumClarityStage clarity) {
		boolean begun = phase != UnstainedPhase.OUTSIDER && phase != UnstainedPhase.NOVITIATE;
		return UnstainedProgressLayout.build(new Progress(phase, purity, prepared, clarityUnlocked, clarity, begun));
	}

	private static UnstainedProgressLayout pledged(EnumClarityStage clarity) {
		return layout(UnstainedPhase.PLEDGED, EnumPurityStage.PURIFIED, false, true, clarity);
	}

	/** Pledged through the five service vows: Clarity Ascension without Baptism or purification. */
	private static UnstainedProgressLayout vowPledged(EnumClarityStage clarity) {
		return UnstainedProgressLayout.build(new Progress(UnstainedPhase.PLEDGED, EnumPurityStage.CORRUPTED,
				true, true, clarity, false));
	}

	@Test
	void vowRoutePledgeBypassesThePuritySegmentAndClosedVein() {
		UnstainedProgressLayout layout = vowPledged(EnumClarityStage.AWAKENED);

		assertEquals(FULL_PATH, ids(layout));
		assertEquals(Status.REACHED, status(layout, "lethean_baptism"));
		for (String id : FULL_PATH.subList(1, 7)) {
			assertEquals(Status.BYPASSED, status(layout, id), id);
		}
		assertEquals(Status.REACHED, status(layout, "consecrated_copper"));
		assertEquals(Status.REACHED, status(layout, "clarity_ascension"));
		assertEquals(Status.CURRENT, status(layout, "clarity_awakened"));
		assertEquals(Status.NEXT, status(layout, "clarity_discerning"));
		for (UnstainedProgressLayout.Link link : layout.links()) {
			if (link.to().status() == Status.BYPASSED) assertFalse(link.lit(), link.to().id());
		}
	}

	@Test
	void purificationRoutePledgeStillReachesEveryPurityStage() {
		UnstainedProgressLayout layout = pledged(EnumClarityStage.AWAKENED);

		for (String id : FULL_PATH.subList(0, 9)) {
			assertTrue(status(layout, id).done(), id);
			assertTrue(status(layout, id) != Status.BYPASSED, id);
		}
	}

	private static List<String> ids(UnstainedProgressLayout layout) {
		return layout.nodes().stream().map(Node::id).toList();
	}

	private static Status status(UnstainedProgressLayout layout, String id) {
		return layout.node(id).orElseThrow(() -> new AssertionError("missing node " + id)).status();
	}

	@Test
	void pledgedPathRunsBaptismPurityBridgeRitesThenClarity() {
		UnstainedProgressLayout layout = pledged(EnumClarityStage.AWAKENED);

		assertEquals(FULL_PATH, ids(layout));
		List<Kind> kinds = layout.nodes().stream().map(Node::kind).toList();
		assertEquals(Kind.RITE, kinds.get(0));
		for (int i = 1; i <= 5; i++) assertEquals(Kind.PURITY_STAGE, kinds.get(i));
		for (int i = 6; i <= 8; i++) assertEquals(Kind.RITE, kinds.get(i));
		for (int i = 9; i <= 13; i++) assertEquals(Kind.CLARITY_STAGE, kinds.get(i));
	}

	@Test
	void nodesSitOnOneBaselineWithStrictlyIncreasingX() {
		List<Node> nodes = pledged(EnumClarityStage.VIGILANT).nodes();
		for (int i = 1; i < nodes.size(); i++) {
			assertEquals(nodes.get(0).y(), nodes.get(i).y(), "baseline for " + nodes.get(i).id());
			int pitch = nodes.get(i).x() - nodes.get(i - 1).x();
			assertTrue(pitch >= 60, "pitch before " + nodes.get(i).id() + " was " + pitch);
		}
	}

	@Test
	void stageNodesKeepThirtyPixelDiamondsAndRitesAreSmaller() {
		for (Node node : pledged(EnumClarityStage.AWAKENED).nodes()) {
			if (node.kind() == Kind.RITE) {
				assertTrue(node.halfSize() * 2 <= 22 && node.halfSize() * 2 >= 18, node.id());
			} else {
				assertEquals(30, node.halfSize() * 2, node.id());
			}
		}
	}

	@Test
	void clarityStagesAreOmittedUntilClarityIsUnlocked() {
		for (UnstainedPhase phase : EnumSet.complementOf(EnumSet.of(UnstainedPhase.PLEDGED))) {
			UnstainedProgressLayout layout = layout(phase, EnumPurityStage.PURIFIED, false, false,
					EnumClarityStage.AWAKENED);
			assertEquals(FULL_PATH.subList(0, 9), ids(layout), phase.name());
			assertTrue(layout.clarityHeader().isEmpty(), phase.name());
		}
	}

	@Test
	void hiddenClarityStagesDoNotShiftVisibleNodes() {
		UnstainedProgressLayout locked = layout(UnstainedPhase.CURE_READY, EnumPurityStage.PURIFIED, false, false,
				EnumClarityStage.AWAKENED);
		UnstainedProgressLayout unlocked = pledged(EnumClarityStage.AWAKENED);
		for (Node node : locked.nodes()) {
			Node twin = unlocked.node(node.id()).orElseThrow();
			assertEquals(node.x(), twin.x(), node.id());
			assertEquals(node.y(), twin.y(), node.id());
		}
		for (Pip pip : locked.pips()) {
			Pip twin = unlocked.pip(pip.observance()).orElseThrow();
			assertEquals(pip.x(), twin.x(), pip.observance().name());
			assertEquals(pip.y(), twin.y(), pip.observance().name());
		}
	}

	@Test
	void bridgeRitesStayVeiledUntilPurified() {
		UnstainedProgressLayout absolved = layout(UnstainedPhase.PURIFYING, EnumPurityStage.ABSOLVED, false, false,
				EnumClarityStage.AWAKENED);
		assertEquals(Status.VEILED, status(absolved, "closed_vein"));
		assertEquals(Status.VEILED, status(absolved, "consecrated_copper"));
		assertEquals(Status.VEILED, status(absolved, "clarity_ascension"));
		assertEquals(List.of("Veiled"), absolved.node("closed_vein").orElseThrow().labelLines());

		UnstainedProgressLayout cureReady = layout(UnstainedPhase.CURE_READY, EnumPurityStage.PURIFIED, false, false,
				EnumClarityStage.AWAKENED);
		assertEquals(Status.CURRENT, status(cureReady, "purity_purified"));
		assertEquals(Status.NEXT, status(cureReady, "closed_vein"));
		assertEquals(Status.LOCKED, status(cureReady, "consecrated_copper"));
		assertEquals(Status.LOCKED, status(cureReady, "clarity_ascension"));
		assertEquals(List.of("Closed", "Vein"), cureReady.node("closed_vein").orElseThrow().labelLines());
	}

	@Test
	void everyPhaseGetsASensiblePath() {
		for (UnstainedPhase phase : List.of(UnstainedPhase.OUTSIDER, UnstainedPhase.NOVITIATE)) {
			UnstainedProgressLayout layout = layout(phase, EnumPurityStage.CORRUPTED, false, false,
					EnumClarityStage.AWAKENED);
			assertEquals(Status.NEXT, status(layout, "lethean_baptism"), phase.name());
			assertEquals(Status.LOCKED, status(layout, "purity_corrupted"), phase.name());
			assertEquals(Status.VEILED, status(layout, "clarity_ascension"), phase.name());
			assertEquals("lethean_baptism", layout.focusNode().id(), phase.name());
		}

		UnstainedProgressLayout tainted = layout(UnstainedPhase.PURIFYING, EnumPurityStage.TAINTED, false, false,
				EnumClarityStage.AWAKENED);
		assertEquals(Status.REACHED, status(tainted, "lethean_baptism"));
		assertEquals(Status.REACHED, status(tainted, "purity_corrupted"));
		assertEquals(Status.CURRENT, status(tainted, "purity_tainted"));
		assertEquals(Status.NEXT, status(tainted, "purity_cleansing"));
		assertEquals(Status.LOCKED, status(tainted, "purity_absolved"));

		UnstainedProgressLayout cleansed = layout(UnstainedPhase.CLEANSED_UNPLEDGED, EnumPurityStage.PURIFIED, false,
				false, EnumClarityStage.AWAKENED);
		assertEquals(Status.REACHED, status(cleansed, "closed_vein"));
		assertEquals(Status.NEXT, status(cleansed, "consecrated_copper"));
		assertEquals(Status.LOCKED, status(cleansed, "clarity_ascension"));

		UnstainedProgressLayout prepared = layout(UnstainedPhase.CLEANSED_UNPLEDGED, EnumPurityStage.PURIFIED, true,
				false, EnumClarityStage.AWAKENED);
		assertEquals(Status.REACHED, status(prepared, "consecrated_copper"));
		assertEquals(Status.NEXT, status(prepared, "clarity_ascension"));

		UnstainedProgressLayout vigilant = pledged(EnumClarityStage.VIGILANT);
		assertEquals(Status.REACHED, status(vigilant, "purity_purified"));
		assertEquals(Status.REACHED, status(vigilant, "clarity_ascension"));
		assertEquals(Status.CURRENT, status(vigilant, "clarity_vigilant"));
		assertEquals(Status.NEXT, status(vigilant, "clarity_resolute"));
		assertEquals(1, vigilant.nodes().stream().filter(n -> n.status() == Status.CURRENT).count());

		UnstainedProgressLayout enlightened = pledged(EnumClarityStage.ENLIGHTENED);
		assertEquals(Status.CURRENT, status(enlightened, "clarity_enlightened"));
		assertTrue(enlightened.nodes().stream().noneMatch(n -> n.status() == Status.NEXT));
		assertEquals("clarity_enlightened", enlightened.focusNode().id());
	}

	@Test
	void linksFollowThePathAndLightOnlyIntoReachedNodes() {
		UnstainedProgressLayout layout = layout(UnstainedPhase.PURIFYING, EnumPurityStage.CLEANSING, false, false,
				EnumClarityStage.AWAKENED);
		List<UnstainedProgressLayout.Link> links = layout.links();
		assertEquals(layout.nodes().size() - 1, links.size());
		for (int i = 0; i < links.size(); i++) {
			assertEquals(layout.nodes().get(i), links.get(i).from());
			assertEquals(layout.nodes().get(i + 1), links.get(i).to());
		}
		assertTrue(links.get(2).lit(), "tainted -> cleansing is reached");
		assertFalse(links.get(3).lit(), "cleansing -> absolved is not reached");
	}

	@Test
	void observancesAreListedInEarnOrder() {
		assertEquals(List.of(
				Observance.NOVITIATE_GATHER_REMEDIES, Observance.NOVITIATE_GENTLE_SEPARATION,
				Observance.NOVITIATE_STILLWATER_LABOR, Observance.NOVITIATE_CLEAN_LABOR,
				Observance.NOVITIATE_SHELTER_AFFLICTED,
				Observance.GATHER_GHOST_PIPE,
				Observance.WEAVE_WREATH, Observance.PREPARE_HEMOLYTIC,
				Observance.CONDENSE_STILL_WATERS, Observance.PLATE_THE_WARD,
				Observance.BEAR_PALLID_ICON,
				Observance.CONSECRATE_COPPER,
				Observance.OFFER_CHALICE,
				Observance.RING_THE_PALE_WATCH), UnstainedProgressLayout.earnOrder());
		assertEquals(Observance.values().length, UnstainedProgressLayout.earnOrder().size());
	}

	@Test
	void observancePipsHangUnderTheStageThatOpensThem() {
		Map<Observance, String> expected = Map.ofEntries(
				Map.entry(Observance.NOVITIATE_GATHER_REMEDIES, "lethean_baptism"),
				Map.entry(Observance.NOVITIATE_SHELTER_AFFLICTED, "lethean_baptism"),
				Map.entry(Observance.GATHER_GHOST_PIPE, "purity_corrupted"),
				Map.entry(Observance.WEAVE_WREATH, "purity_tainted"),
				Map.entry(Observance.PREPARE_HEMOLYTIC, "purity_tainted"),
				Map.entry(Observance.CONDENSE_STILL_WATERS, "purity_cleansing"),
				Map.entry(Observance.PLATE_THE_WARD, "purity_cleansing"),
				Map.entry(Observance.BEAR_PALLID_ICON, "purity_absolved"),
				Map.entry(Observance.CONSECRATE_COPPER, "purity_purified"),
				Map.entry(Observance.OFFER_CHALICE, "clarity_awakened"),
				Map.entry(Observance.RING_THE_PALE_WATCH, "clarity_vigilant"));
		UnstainedProgressLayout layout = pledged(EnumClarityStage.AWAKENED);
		assertEquals(Observance.values().length, layout.pips().size());
		expected.forEach((observance, anchor) -> {
			Pip pip = layout.pip(observance).orElseThrow();
			assertEquals(anchor, pip.anchor().id(), observance.name());
			assertTrue(Math.abs(pip.x() - pip.anchor().x()) <= 20, observance.name());
			assertTrue(pip.y() > pip.anchor().labelRect().bottom(), observance.name() + " sits below the label");
		});

		Pip wreath = layout.pip(Observance.WEAVE_WREATH).orElseThrow();
		Pip hemolytic = layout.pip(Observance.PREPARE_HEMOLYTIC).orElseThrow();
		assertTrue(wreath.x() < hemolytic.x(), "pips under one stage follow earn order");
		List<Pip> vows = layout.pips().stream().filter(p -> p.anchor().id().equals("lethean_baptism")).toList();
		assertEquals(5, vows.size());
		for (int i = 1; i < vows.size(); i++) assertTrue(vows.get(i - 1).x() < vows.get(i).x());

		UnstainedProgressLayout locked = layout(UnstainedPhase.PURIFYING, EnumPurityStage.TAINTED, false, false,
				EnumClarityStage.AWAKENED);
		assertTrue(locked.pip(Observance.OFFER_CHALICE).isEmpty(), "clarity observances wait for clarity stages");
		assertTrue(locked.pip(Observance.RING_THE_PALE_WATCH).isEmpty());
	}

	@Test
	void nodesLabelsPipsAndHeadersNeverOverlap() {
		UnstainedProgressLayout layout = pledged(EnumClarityStage.RESOLUTE);
		List<Node> nodes = layout.nodes();
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = i + 1; j < nodes.size(); j++) {
				double d = Math.hypot(nodes.get(i).x() - nodes.get(j).x(), nodes.get(i).y() - nodes.get(j).y());
				assertTrue(d >= 40, nodes.get(i).id() + " / " + nodes.get(j).id());
			}
		}

		List<Rect> labels = nodes.stream().map(Node::labelRect).toList();
		for (int i = 0; i < labels.size(); i++) {
			assertTrue(labels.get(i).w() <= 64, nodes.get(i).id() + " label is " + labels.get(i).w() + " px");
			for (int j = i + 1; j < labels.size(); j++) {
				Rect a = labels.get(i);
				Rect b = labels.get(j);
				assertFalse(a.grow(2).intersects(b.grow(2)),
						nodes.get(i).id() + " label crowds " + nodes.get(j).id());
			}
		}

		List<Rect> obstacles = new ArrayList<>(labels);
		obstacles.add(layout.purityHeader());
		layout.clarityHeader().ifPresent(obstacles::add);
		obstacles.add(layout.penaltyRect());
		for (Node node : nodes) {
			for (Rect rect : obstacles) {
				assertFalse(rect.intersects(node.bounds()), node.id() + " is covered by " + rect);
			}
		}
		for (Pip pip : layout.pips()) {
			for (Rect rect : obstacles) {
				assertFalse(rect.intersects(pip.rect()), pip.observance().name() + " is covered by " + rect);
			}
			for (Node node : nodes) {
				assertFalse(pip.rect().intersects(node.bounds()), pip.observance().name() + " on " + node.id());
			}
		}
		assertFalse(layout.purityHeader().intersects(layout.clarityHeader().orElseThrow()));
	}

	@Test
	void contentBoundsContainEveryLabelPipAndHeader() {
		for (UnstainedProgressLayout layout : List.of(pledged(EnumClarityStage.AWAKENED),
				layout(UnstainedPhase.OUTSIDER, EnumPurityStage.CORRUPTED, false, false, EnumClarityStage.AWAKENED))) {
			Rect bounds = layout.contentBounds();
			assertEquals(0, bounds.x());
			assertEquals(0, bounds.y());
			List<Rect> parts = new ArrayList<>();
			layout.nodes().forEach(n -> {
				parts.add(n.bounds());
				parts.add(n.labelRect());
			});
			layout.pips().forEach(p -> parts.add(p.rect()));
			parts.add(layout.purityHeader());
			layout.clarityHeader().ifPresent(parts::add);
			parts.add(layout.penaltyRect());
			for (Rect part : parts) {
				assertTrue(part.x() >= bounds.x() + 4 && part.y() >= bounds.y() + 4
						&& part.right() <= bounds.right() - 4 && part.bottom() <= bounds.bottom() - 4,
						part + " outside " + bounds);
			}
		}
	}

	@Test
	void openingViewFitsThePathIntoLandscapeViewports() {
		UnstainedProgressLayout layout = pledged(EnumClarityStage.AWAKENED);
		Rect bounds = layout.contentBounds();

		View small = layout.fit(0, 22, 448, 202);
		assertTrue(small.zoom() >= 0.5F && small.zoom() <= 1.0F);
		assertTrue(bounds.w() * small.zoom() <= 448.5F, "whole path fits a 448 px viewport");
		assertEquals(224.0, small.panX() + bounds.w() * small.zoom() / 2.0, 1.0);
		assertEquals(22 + 101.0, small.panY() + bounds.h() * small.zoom() / 2.0, 1.0);

		View large = layout.fit(0, 22, 928, 472);
		assertEquals(1.0F, large.zoom(), 1.0E-4F);

		UnstainedProgressLayout locked = layout(UnstainedPhase.PURIFYING, EnumPurityStage.CORRUPTED, false, false,
				EnumClarityStage.AWAKENED);
		assertTrue(locked.fit(0, 22, 448, 202).zoom() > small.zoom(), "the shorter pre-clarity path zooms closer");
	}

	@Test
	void openingViewRightOfTheSidebarKeepsTheFocusNodeVisible() {
		UnstainedProgressLayout layout = pledged(EnumClarityStage.RESOLUTE);
		int areaX = 192;
		int areaW = 448 - areaX;
		View view = layout.fit(areaX, 22, areaW, 202);
		assertEquals(0.5F, view.zoom(), 1.0E-4F);
		Node focus = layout.focusNode();
		assertEquals("clarity_enlightened", focus.id());
		double focusLeft = view.panX() + focus.labelRect().x() * view.zoom();
		double focusRight = view.panX() + focus.labelRect().right() * view.zoom();
		assertTrue(focusLeft >= areaX && focusRight <= areaX + areaW,
				"focus label spans " + focusLeft + ".." + focusRight);
	}

	@Test
	void fitZoomIsClampedToTheIconRange() {
		assertEquals(0.5F, UnstainedProgressLayout.fitZoom(2000, 100, 448, 202), 1.0E-4F);
		assertEquals(1.0F, UnstainedProgressLayout.fitZoom(100, 50, 928, 472), 1.0E-4F);
		assertEquals(0.75F, UnstainedProgressLayout.fitZoom(800, 100, 600, 400), 1.0E-4F);
	}

	@Test
	void labelWidthEstimateMatchesTheVanillaFont() {
		assertEquals(52, UnstainedProgressLayout.estimateWidth("Corrupted"));
		assertEquals(57, UnstainedProgressLayout.estimateWidth("Enlightened"));
		assertEquals(64, UnstainedProgressLayout.estimateWidth("Consecrated"));
		assertEquals(36, UnstainedProgressLayout.estimateWidth("Tainted"));
		assertEquals(50, UnstainedProgressLayout.estimateWidth("Ascension"));
	}
}
