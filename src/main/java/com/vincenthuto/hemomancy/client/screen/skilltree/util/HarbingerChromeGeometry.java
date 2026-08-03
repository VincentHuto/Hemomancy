package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.ArrayList;
import java.util.List;

/** Pixel geometry shared by Harbinger ornamental frames of every supported size. */
final class HarbingerChromeGeometry {
	private HarbingerChromeGeometry() {}

	static List<Segment> frameSegments(int x, int y, int width, int height) {
		if (width <= 0 || height <= 0) return List.of();
		int right = x + width;
		int bottom = y + height;
		List<Segment> segments = new ArrayList<>();

		add(segments, x, y, right, y + 1, Tone.OUTER);
		add(segments, x, bottom - 1, right, bottom, Tone.OUTER);
		add(segments, x, y, x + 1, bottom, Tone.OUTER);
		add(segments, right - 1, y, right, bottom, Tone.OUTER);

		if (width >= 4 && height >= 4) {
			add(segments, x + 1, y + 1, right - 1, y + 2, Tone.INNER);
			add(segments, x + 1, bottom - 2, right - 1, bottom - 1, Tone.INNER);
			add(segments, x + 1, y + 1, x + 2, bottom - 1, Tone.INNER);
			add(segments, right - 2, y + 1, right - 1, bottom - 1, Tone.INNER);
		}

		if (width >= 8 && height >= 8) {
			int cap = Math.min(3, Math.min(width - 2, height - 2));
			add(segments, x + 1, y + 1, x + 1 + cap, y + 2, Tone.HIGHLIGHT);
			add(segments, x + 1, y + 1, x + 2, y + 1 + cap, Tone.HIGHLIGHT);
			add(segments, right - 1 - cap, y + 1, right - 1, y + 2, Tone.HIGHLIGHT);
			add(segments, right - 2, y + 1, right - 1, y + 1 + cap, Tone.SHADOW);
			add(segments, x + 1, bottom - 2, x + 1 + cap, bottom - 1, Tone.SHADOW);
			add(segments, x + 1, bottom - 1 - cap, x + 2, bottom - 1, Tone.HIGHLIGHT);
			add(segments, right - 1 - cap, bottom - 2, right - 1, bottom - 1, Tone.SHADOW);
			add(segments, right - 2, bottom - 1 - cap, right - 1, bottom - 1, Tone.SHADOW);
		}

		if (width >= 24 && height >= 6) {
			int quarter = width / 4;
			int threeQuarter = width * 3 / 4;
			add(segments, x + quarter, y + 2, x + quarter + 3, y + 3, Tone.STITCH);
			add(segments, x + threeQuarter - 3, bottom - 3, x + threeQuarter, bottom - 2, Tone.STITCH);
		}
		if (height >= 24 && width >= 6) {
			int quarter = height / 4;
			int threeQuarter = height * 3 / 4;
			add(segments, x + 2, y + quarter, x + 3, y + quarter + 3, Tone.STITCH);
			add(segments, right - 3, y + threeQuarter - 3, right - 2, y + threeQuarter, Tone.STITCH);
		}
		if (width >= 96 && height >= 6) {
			for (int stitchX = x + 24; stitchX <= right - 27; stitchX += 24) {
				add(segments, stitchX, y + 2, stitchX + 3, y + 3, Tone.STITCH);
			}
			for (int stitchX = right - 27; stitchX >= x + 24; stitchX -= 24) {
				add(segments, stitchX, bottom - 3, stitchX + 3, bottom - 2, Tone.STITCH);
			}
		}
		if (height >= 64 && width >= 6) {
			for (int stitchY = y + 20; stitchY <= bottom - 23; stitchY += 20) {
				add(segments, x + 2, stitchY, x + 3, stitchY + 3, Tone.STITCH);
			}
			for (int stitchY = bottom - 23; stitchY >= y + 20; stitchY -= 20) {
				add(segments, right - 3, stitchY, right - 2, stitchY + 3, Tone.STITCH);
			}
		}
		return List.copyOf(segments);
	}

	private static void add(List<Segment> segments, int left, int top, int right, int bottom, Tone tone) {
		if (right > left && bottom > top) segments.add(new Segment(left, top, right, bottom, tone));
	}

	enum Tone { OUTER, INNER, HIGHLIGHT, SHADOW, STITCH }

	record Segment(int left, int top, int right, int bottom, Tone tone) {}
}
