package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HarbingerChromeGeometryTest {
	@Test
	void directEmissionMatchesTheExistingMaterializedGeometry() {
		List<HarbingerChromeGeometry.Segment> expected =
				HarbingerChromeGeometry.frameSegments(7, 11, 160, 80);
		List<HarbingerChromeGeometry.Segment> emitted = new ArrayList<>();

		HarbingerChromeGeometry.emitFrameSegments(7, 11, 160, 80,
				(left, top, right, bottom, tone) -> emitted.add(
						new HarbingerChromeGeometry.Segment(left, top, right, bottom, tone)));

		assertEquals(expected, emitted);
	}

	@Test
	void compactFramesKeepEveryOrnamentInsideTheirExistingHitbox() {
		List<HarbingerChromeGeometry.Segment> segments =
				HarbingerChromeGeometry.frameSegments(10, 20, 13, 13);

		assertTrue(segments.stream().allMatch(segment ->
				segment.left() >= 10 && segment.top() >= 20
						&& segment.right() <= 23 && segment.bottom() <= 33
						&& segment.right() > segment.left() && segment.bottom() > segment.top()));
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				11, 21, 14, 22, HarbingerChromeGeometry.Tone.HIGHLIGHT)));
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				19, 31, 22, 32, HarbingerChromeGeometry.Tone.SHADOW)));
	}

	@Test
	void largeFramesAddBalancedEdgeStitchesAtLiteralQuarterPoints() {
		List<HarbingerChromeGeometry.Segment> segments =
				HarbingerChromeGeometry.frameSegments(0, 0, 64, 40);

		assertEquals(4, segments.stream()
				.filter(segment -> segment.tone() == HarbingerChromeGeometry.Tone.STITCH)
				.count());
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				16, 2, 19, 3, HarbingerChromeGeometry.Tone.STITCH)));
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				45, 37, 48, 38, HarbingerChromeGeometry.Tone.STITCH)));
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				2, 10, 3, 13, HarbingerChromeGeometry.Tone.STITCH)));
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				61, 27, 62, 30, HarbingerChromeGeometry.Tone.STITCH)));
	}

	@Test
	void veryLongEdgesRepeatStitchesInsteadOfLeavingTheFrameBare() {
		List<HarbingerChromeGeometry.Segment> segments =
				HarbingerChromeGeometry.frameSegments(0, 0, 160, 80);

		assertTrue(segments.stream()
				.filter(segment -> segment.tone() == HarbingerChromeGeometry.Tone.STITCH)
				.count() >= 12);
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				24, 2, 27, 3, HarbingerChromeGeometry.Tone.STITCH)));
		assertTrue(segments.contains(new HarbingerChromeGeometry.Segment(
				133, 77, 136, 78, HarbingerChromeGeometry.Tone.STITCH)));
	}
}
