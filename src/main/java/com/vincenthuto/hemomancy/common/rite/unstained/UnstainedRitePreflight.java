package com.vincenthuto.hemomancy.common.rite.unstained;

public final class UnstainedRitePreflight {
	private static final String LETHEAN_BAPTISM = "cardinal_rite/lethean_baptism";
	private static final String CLARITY_ASCENSION = "cardinal_rite/clarity_ascension";
	private static final String CLOSED_VEIN = "cardinal_rite/closed_vein";
	private static final String SEVERED_COVENANT = "cardinal_rite/severed_covenant";
	private static final String SILVER_VEIL = "cardinal_rite/silver_veil";
	private static final String LETHEAN_TIDE = "cardinal_rite/lethean_tide";
	private static final String SILVER_DAWN = "cardinal_rite/silver_dawn";
	static final float SILVER_VEIL_MIN_PURITY = 25.0f;
	static final float LETHEAN_TIDE_MIN_PURITY = 50.0f;

	private UnstainedRitePreflight() {}

	/**
	 * Every refusal an Unstained rite can make. It runs at activation and again
	 * just before completion consumes anything, so a refused rite never starts
	 * or never spends its offerings.
	 */
	public static Result check(String ritePath, State state) {
		if (LETHEAN_BAPTISM.equals(ritePath)) {
			if (!state.maySeekCure()) return Result.failed(Failure.FOUNDER_CANNOT_ENTER);
			if (!state.infectionSuppressed()) return Result.failed(Failure.INFECTION_NOT_SUPPRESSED);
			if (state.begunPurification()) return Result.failed(Failure.BAPTISM_ALREADY_COMPLETE);
		}
		if (CLARITY_ASCENSION.equals(ritePath)) {
			if (!state.baselineRestored() && !state.novitiateVowsComplete()) {
				return Result.failed(Failure.NOT_READY_TO_PLEDGE);
			}
			if (!state.cleanBlood()) return Result.failed(Failure.BLOOD_STILL_ACTIVE);
			if (state.clarityUnlocked()) return Result.failed(Failure.CLARITY_ALREADY_UNLOCKED);
			if (!state.clarityPrepared()) return Result.failed(Failure.CLARITY_NOT_PREPARED);
		}
		if (CLOSED_VEIN.equals(ritePath)) {
			if (state.clarityUnlocked()) return Result.accepted();
			if (state.baselineRestored()) return Result.failed(Failure.CLOSED_VEIN_MEMBER_ONLY);
			if (!state.begunPurification() || !state.purified()) return Result.failed(Failure.CURE_NOT_READY);
		}
		if (SEVERED_COVENANT.equals(ritePath)) {
			if (!state.severedCovenantEligible()) return Result.failed(Failure.SEVERANCE_NOT_READY);
			if (!state.leadsFoundersCovenant()) return Result.failed(Failure.NO_FOUNDERS_COVENANT);
		}
		if (SILVER_VEIL.equals(ritePath) && state.purity() < SILVER_VEIL_MIN_PURITY) {
			return Result.failed(Failure.VEIL_PURITY_TOO_LOW);
		}
		if (LETHEAN_TIDE.equals(ritePath)) {
			if (!state.begunPurification() || state.purity() < LETHEAN_TIDE_MIN_PURITY) {
				return Result.failed(Failure.TIDE_PURITY_TOO_LOW);
			}
			if (!state.bloodMoonActive()) return Result.failed(Failure.NO_BLOOD_MOON);
		}
		if (SILVER_DAWN.equals(ritePath) && !state.clarityUnlocked()) {
			return Result.failed(Failure.CLARITY_REQUIRED);
		}
		return Result.accepted();
	}

	public record State(boolean maySeekCure, boolean infectionSuppressed, boolean begunPurification,
			boolean purified, boolean baselineRestored, boolean novitiateVowsComplete,
			boolean clarityPrepared, boolean clarityUnlocked, boolean cleanBlood,
			boolean severedCovenantEligible, boolean leadsFoundersCovenant, float purity,
			boolean bloodMoonActive) {}

	public record Result(boolean success, Failure failure) {
		private static Result accepted() {
			return new Result(true, Failure.NONE);
		}

		private static Result failed(Failure failure) {
			return new Result(false, failure);
		}
	}

	public enum Failure {
		NONE,
		FOUNDER_CANNOT_ENTER,
		INFECTION_NOT_SUPPRESSED,
		BAPTISM_ALREADY_COMPLETE,
		NOT_READY_TO_PLEDGE,
		BLOOD_STILL_ACTIVE,
		CLARITY_ALREADY_UNLOCKED,
		CLARITY_NOT_PREPARED,
		CURE_NOT_READY,
		CLOSED_VEIN_MEMBER_ONLY,
		SEVERANCE_NOT_READY,
		NO_FOUNDERS_COVENANT,
		VEIL_PURITY_TOO_LOW,
		TIDE_PURITY_TOO_LOW,
		NO_BLOOD_MOON,
		CLARITY_REQUIRED
	}
}
