package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.ArrayList;
import java.util.List;

/** Pixel geometry shared by Harbinger ornamental frames of every supported size. */
final class HarbingerChromeGeometry {
	private HarbingerChromeGeometry() {}

	static List<Segment> frameSegments(int x, int y, int width, int height) {
		if (width <= 0 || height <= 0) return List.of();
		List<Segment> segments = new ArrayList<>();
		emitFrameSegments(x, y, width, height,
				(left, top, right, bottom, tone) -> segments.add(new Segment(left, top, right, bottom, tone)));
		return List.copyOf(segments);
	}

	static void emitFrameSegments(int x, int y, int width, int height, SegmentSink sink) {
		if (width <= 0 || height <= 0) return;
		int right = x + width;
		int bottom = y + height;

		add(sink, x, y, right, y + 1, Tone.OUTER);
		add(sink, x, bottom - 1, right, bottom, Tone.OUTER);
		add(sink, x, y, x + 1, bottom, Tone.OUTER);
		add(sink, right - 1, y, right, bottom, Tone.OUTER);

		if (width >= 4 && height >= 4) {
			add(sink, x + 1, y + 1, right - 1, y + 2, Tone.INNER);
			add(sink, x + 1, bottom - 2, right - 1, bottom - 1, Tone.INNER);
			add(sink, x + 1, y + 1, x + 2, bottom - 1, Tone.INNER);
			add(sink, right - 2, y + 1, right - 1, bottom - 1, Tone.INNER);
		}

		if (width >= 8 && height >= 8) {
			int cap = Math.min(3, Math.min(width - 2, height - 2));
			add(sink, x + 1, y + 1, x + 1 + cap, y + 2, Tone.HIGHLIGHT);
			add(sink, x + 1, y + 1, x + 2, y + 1 + cap, Tone.HIGHLIGHT);
			add(sink, right - 1 - cap, y + 1, right - 1, y + 2, Tone.HIGHLIGHT);
			add(sink, right - 2, y + 1, right - 1, y + 1 + cap, Tone.SHADOW);
			add(sink, x + 1, bottom - 2, x + 1 + cap, bottom - 1, Tone.SHADOW);
			add(sink, x + 1, bottom - 1 - cap, x + 2, bottom - 1, Tone.HIGHLIGHT);
			add(sink, right - 1 - cap, bottom - 2, right - 1, bottom - 1, Tone.SHADOW);
			add(sink, right - 2, bottom - 1 - cap, right - 1, bottom - 1, Tone.SHADOW);
		}

		if (width >= 24 && height >= 6) {
			int quarter = width / 4;
			int threeQuarter = width * 3 / 4;
			add(sink, x + quarter, y + 2, x + quarter + 3, y + 3, Tone.STITCH);
			add(sink, x + threeQuarter - 3, bottom - 3, x + threeQuarter, bottom - 2, Tone.STITCH);
		}
		if (height >= 24 && width >= 6) {
			int quarter = height / 4;
			int threeQuarter = height * 3 / 4;
			add(sink, x + 2, y + quarter, x + 3, y + quarter + 3, Tone.STITCH);
			add(sink, right - 3, y + threeQuarter - 3, right - 2, y + threeQuarter, Tone.STITCH);
		}
		if (width >= 96 && height >= 6) {
			for (int stitchX = x + 24; stitchX <= right - 27; stitchX += 24) {
				add(sink, stitchX, y + 2, stitchX + 3, y + 3, Tone.STITCH);
			}
			for (int stitchX = right - 27; stitchX >= x + 24; stitchX -= 24) {
				add(sink, stitchX, bottom - 3, stitchX + 3, bottom - 2, Tone.STITCH);
			}
		}
		if (height >= 64 && width >= 6) {
			for (int stitchY = y + 20; stitchY <= bottom - 23; stitchY += 20) {
				add(sink, x + 2, stitchY, x + 3, stitchY + 3, Tone.STITCH);
			}
			for (int stitchY = bottom - 23; stitchY >= y + 20; stitchY -= 20) {
				add(sink, right - 3, stitchY, right - 2, stitchY + 3, Tone.STITCH);
			}
		}
	}

	private static void add(SegmentSink sink, int left, int top, int right, int bottom, Tone tone) {
		if (right > left && bottom > top) sink.accept(left, top, right, bottom, tone);
	}

	@FunctionalInterface
	interface SegmentSink {
		void accept(int left, int top, int right, int bottom, Tone tone);
	}

	enum Tone { OUTER, INNER, HIGHLIGHT, SHADOW, STITCH }

	record Segment(int left, int top, int right, int bottom, Tone tone) {}
}
