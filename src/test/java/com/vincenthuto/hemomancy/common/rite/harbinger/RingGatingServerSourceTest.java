package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class RingGatingServerSourceTest {
	private static final String ROOT = "src/main/java/com/vincenthuto/hemomancy/";

	static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	// Which rites start with gated rings is behaviour: rite_kind_validation starts a ceremony and a station upgrade
	// gated, and a Working craft (no anchors) ungated, through CardinalRiteActivation.

	@Test
	void theEngineHoldsTheStationAndTheProcessionForTheRing() throws IOException {
		String engine = read(ROOT + "common/rite/harbinger/CardinalRiteOrdealEngine.java");
		assertTrue(engine.contains("case STATION_PROJECTION -> rite.tryFinishStationProjection();"));
		assertTrue(engine.contains("&& rite.finishOfferingProcession()) {"), "the swell message only on a real hand-off");
		assertTrue(engine.contains("rite.applyStillIntervalPressure(duration);"));
	}

	@Test
	void projectionFeedsTheWokenRingAndTheSealWaitsForIt() throws IOException {
		String handler = read(ROOT + "common/rite/harbinger/CardinalRiteInteractionHandler.java");
		assertTrue(handler.contains("if (rite.hungryLaterRingAnchors() <= 0) return CardinalRiteProjectionResult.handled(0.0D);"));
		SourceAssertions.assertShowsEnglish("the hungry-ring refusal", handler,
				"The woken ring hungers. Feed its sores before the altar will seal.");
		assertTrue(handler.contains("filter(rite::isAnchorOpen)"), "the nearest-anchor fallback skips sleeping anchors");
	}

	@Test
	void wokenRingsAreAnnouncedWithHemomancyParticles() throws IOException {
		String events = read(ROOT + "common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		int start = events.indexOf("private static boolean announceWokenRings(");
		int end = events.indexOf("\n\t}\n", start);
		assertTrue(start >= 0 && end > start);
		String announce = events.substring(start, end);
		assertTrue(announce.contains("CardinalRiteEventKind.RING_OPENED"));
		assertTrue(announce.contains("SoundInit.RITE_RING_OPEN"));
		assertTrue(announce.contains("HemoParticleData.bloodCell("));
		assertFalse(announce.contains("ParticleTypes."), "no vanilla particles");
		assertTrue(events.contains("announceWokenRings(sLevel, rite, recipe);"));
		assertFalse(events.contains("BLOOD_PER_ANCHOR_ML"), "every reader uses the anchor's own requirement");
	}

	@Test
	void theStingIsRegisteredWithASubtitle() throws IOException {
		String sounds = read("src/main/resources/assets/hemomancy/sounds.json");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String init = read(ROOT + "common/init/SoundInit.java");
		assertTrue(init.contains("RITE_RING_OPEN = registerSoundEvent(\"rite.ring_open\")"));
		assertTrue(sounds.contains("\"rite.ring_open\": {\"subtitle\": \"subtitle.hemomancy.rite.ring_open\""));
		assertTrue(lang.contains("\"subtitle.hemomancy.rite.ring_open\":"));
	}
	@Test
	void consecrationCountsOpenAnchorsAndWakingsSyncAtOnce() throws IOException {
		String events = read(ROOT + "common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		String engine = read(ROOT + "common/rite/harbinger/CardinalRiteOrdealEngine.java");
		assertTrue(events.contains("CardinalRiteRingRules.justConsecrated(previous.full().cardinality(), full.cardinality(),"),
				"the consecrated beat waits for the open anchors, not every anchor");
		assertTrue(events.contains("phaseChanged |= announceWokenRings(sLevel, rite, recipe);"),
				"a woken ring syncs the same tick, so its tear is not clipped");
		assertTrue(engine.contains("rite.applyStillIntervalPressure(duration);"));
		assertTrue(engine.contains("if (rite.getPhaseTicks() == Math.max(1, duration)) {"),
				"a hold with no still interval still warns once");
	}
}
