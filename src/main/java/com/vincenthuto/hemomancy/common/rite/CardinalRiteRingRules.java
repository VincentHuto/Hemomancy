package com.vincenthuto.hemomancy.common.rite;

import java.util.List;
import java.util.TreeSet;

/**
 * How a Cardinal Rite's anchor rings are numbered, priced and woken. Ring 0 is fed in Consecration; each later ring
 * wakes when the sealed rite enters one of its openers, and every ring still asleep wakes at the last.
 */
public final class CardinalRiteRingRules {
	private CardinalRiteRingRules() {
	}

	/** Each anchor's rank: its authored ring value renumbered densely from 0, innermost first. */
	public static int[] ranks(List<Integer> authoredRings) {
		if (authoredRings == null || authoredRings.isEmpty()) return new int[0];
		List<Integer> distinct = List.copyOf(new TreeSet<>(authoredRings));
		int[] ranks = new int[authoredRings.size()];
		for (int i = 0; i < ranks.length; i++) ranks[i] = distinct.indexOf(authoredRings.get(i));
		return ranks;
	}

	public static int[] ranksOf(List<CardinalRiteCeremonyDefinition.Anchor> anchors) {
		return ranks(anchors == null ? List.of()
				: anchors.stream().map(CardinalRiteCeremonyDefinition.Anchor::ring).toList());
	}

	public static int ringCount(int[] ranks) {
		int highest = -1;
		for (int rank : ranks) highest = Math.max(highest, rank);
		return highest + 1;
	}

	/** Blood one anchor of this rank asks for: a quarter more than the ring inside it, rounded half up. */
	public static int bloodForRank(int rank) {
		return (CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML * (4 + Math.max(0, rank)) + 2) / 4;
	}

	/** Whether the rite's open anchors have all just filled: the moment the circle is consecrated. */
	public static boolean justConsecrated(int previousFull, int nowFull, int openAnchors) {
		return openAnchors > 0 && nowFull == openAnchors && previousFull < nowFull;
	}

	/** The phases a later ring can wake in. */
	public static boolean isOpener(CardinalRitePhase phase) {
		return phase == CardinalRitePhase.STATION_PROJECTION || phase == CardinalRitePhase.ORDEAL
				|| phase == CardinalRitePhase.STILL_INTERVAL || phase == CardinalRitePhase.OFFERING_PROCESSION;
	}

	/** How many openers a sealed rite of this shape enters before Culmination. */
	public static int openerCount(boolean station, int waves, boolean stillIntervals, boolean offerings) {
		int procession = offerings ? 1 : 0;
		if (station) return 1 + procession;
		if (waves > 0) return waves + (stillIntervals ? waves - 1 : 0) + procession;
		return (stillIntervals ? 1 : 0) + procession;
	}

	/** Rings to wake on entering opener {@code index}: one each, and every ring still asleep at the last. */
	public static int ringsToOpen(int index, int openers, int sleeping) {
		if (sleeping <= 0 || index < 0) return 0;
		return index >= openers - 1 ? sleeping : 1;
	}
}
