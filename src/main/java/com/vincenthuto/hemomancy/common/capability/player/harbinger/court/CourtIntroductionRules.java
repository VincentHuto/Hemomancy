package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

/**
 * The pure rules behind the three ways a player learns of the Brined Court (Track 5). Callers pass the player's
 * degree and state; runtime degree checks still go through {@code ProgressionGates} with {@link #MINIMUM_DEGREE}.
 */
public final class CourtIntroductionRules {
	/** The Vicar's referral, the Voyager's explanation and the Alchemist's lead start at the fifth Degree. */
	public static final int MINIMUM_DEGREE = 5;
	/** {@code MorphlingItem.MATURITY_NAMES} index of Mature. */
	public static final int MATURE = 3;

	private CourtIntroductionRules() {
	}

	/** What a route does when it arrives: tell the player, or acknowledge the route that already did. */
	public enum Arrival {
		FIRST, ACKNOWLEDGED
	}

	public static Arrival arrive(boolean known) {
		return known ? Arrival.ACKNOWLEDGED : Arrival.FIRST;
	}

	/** The Vicar refers a fifth-Degree player to the Voyagers. */
	public static boolean vicarRefers(int degree) {
		return degree >= MINIMUM_DEGREE;
	}

	/** A Voyager speaks of the Court once it is known, after a letter dropped, or from the fifth Degree. */
	public static boolean voyagerSpeaks(int degree, boolean known, boolean letterDropped) {
		return known || letterDropped || degree >= MINIMUM_DEGREE;
	}

	/** The Alchemist's lead needs the fifth Degree and a strain at Mature or higher. */
	public static boolean alchemistPoints(int degree, int highestStrainMaturity) {
		return degree >= MINIMUM_DEGREE && highestStrainMaturity >= MATURE;
	}

	/** A Votary carries one letter per player, and only for a player who doesn't know the Court yet. */
	public static boolean letterDrops(boolean known, boolean letterDropped) {
		return !known && !letterDropped;
	}

	/** A dropped letter that never reached an NPC can still be explained, by word, until the Court is known. */
	public static boolean letterLost(boolean known, boolean letterDropped, boolean carryingLetter) {
		return !known && letterDropped && !carryingLetter;
	}

	/** Voyagers reissue the Relay Chart to anyone who knows the Court and isn't carrying one. */
	public static boolean canIssueChart(boolean known, boolean carryingChart) {
		return known && !carryingChart;
	}
}
