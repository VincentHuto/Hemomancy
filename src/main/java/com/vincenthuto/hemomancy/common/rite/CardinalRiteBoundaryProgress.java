package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Converts completed adjacent anchor pairs into the visible arcs connecting
 * them. Angles are retained so authored rotations and non-cardinal layouts
 * still grow from their actual node positions.
 */
public final class CardinalRiteBoundaryProgress {
	private static final double FULL_CIRCLE = Math.PI * 2.0D;

	private CardinalRiteBoundaryProgress() {
	}

	public static List<Segment> completedSegments(
			List<CardinalRiteCeremonyDefinition.Anchor> anchors, int[] anchorBloodMl) {
		if (anchors == null || anchors.isEmpty() || anchorBloodMl == null) return List.of();
		List<Segment> result = new ArrayList<>();
		int maxRing = anchors.stream().mapToInt(CardinalRiteCeremonyDefinition.Anchor::ring).max().orElse(-1);
		for (int ring = 0; ring <= maxRing; ring++) {
			final int targetRing = ring;
			List<IndexedAnchor> ringAnchors = new ArrayList<>();
			for (int index = 0; index < anchors.size() && index < anchorBloodMl.length; index++) {
				var anchor = anchors.get(index);
				if (anchor.ring() == targetRing) {
					ringAnchors.add(new IndexedAnchor(index, anchor));
				}
			}
			ringAnchors.sort(Comparator.comparingInt(entry -> entry.anchor().order()));
			if (ringAnchors.size() < 2) continue;
			for (int index = 0; index < ringAnchors.size(); index++) {
				IndexedAnchor start = ringAnchors.get(index);
				IndexedAnchor end = ringAnchors.get((index + 1) % ringAnchors.size());
				if (!complete(anchorBloodMl, start.index()) || !complete(anchorBloodMl, end.index())) continue;
				double startAngle = Math.atan2(start.anchor().z(), start.anchor().x());
				double endAngle = Math.atan2(end.anchor().z(), end.anchor().x());
				double sweep = normalizeAngle(endAngle - startAngle);
				result.add(new Segment(ring, startAngle, sweep, start.index(), 1.0F));
			}
		}
		return List.copyOf(result);
	}

	private static boolean complete(int[] blood, int index) {
		return index >= 0 && index < blood.length
				&& blood[index] >= CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
	}

	private record IndexedAnchor(int index, CardinalRiteCeremonyDefinition.Anchor anchor) {
	}

	public record Segment(int ring, double startAngle, double sweepAngle,
			int startAnchorIndex, float integrity) {
		public Segment(int ring, double startAngle, double sweepAngle) {
			this(ring, startAngle, sweepAngle, -1, 1.0F);
		}

		public Segment withIntegrity(float newIntegrity) {
			return new Segment(ring, startAngle, sweepAngle, startAnchorIndex,
					Math.max(0.0F, Math.min(1.0F, newIntegrity)));
		}
	}

	private static double normalizeAngle(double angle) {
		double normalized = angle % FULL_CIRCLE;
		return normalized < 0.0D ? normalized + FULL_CIRCLE : normalized;
	}
}
