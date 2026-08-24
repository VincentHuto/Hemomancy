package com.vincenthuto.hemomancy.common.rite.unstained;

public final class UnstainedRitePreflight {
	private static final String LETHEAN_BAPTISM = "cardinal_rite/lethean_baptism";
	private static final String CLARITY_ASCENSION = "cardinal_rite/clarity_ascension";
	private static final String CLOSED_VEIN = "cardinal_rite/closed_vein";
	private static final String SEVERED_COVENANT = "cardinal_rite/severed_covenant";

	private UnstainedRitePreflight() {}

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
		if (SEVERED_COVENANT.equals(ritePath) && !state.severedCovenantEligible()) {
			return Result.failed(Failure.SEVERANCE_NOT_READY);
		}
		return Result.accepted();
	}

	public record State(boolean maySeekCure, boolean infectionSuppressed, boolean begunPurification,
			boolean purified, boolean baselineRestored, boolean novitiateVowsComplete,
			boolean clarityPrepared, boolean clarityUnlocked, boolean cleanBlood,
			boolean severedCovenantEligible) {}

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
		SEVERANCE_NOT_READY
	}
}
