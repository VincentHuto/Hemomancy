package com.vincenthuto.hemomancy.common.rite.unstained;

public final class UnstainedRitePreflight {
	private static final String LETHEAN_BAPTISM = "cardinal_rite/lethean_baptism";
	private static final String CLARITY_ASCENSION = "cardinal_rite/clarity_ascension";

	private UnstainedRitePreflight() {}

	public static Result check(String ritePath, State state) {
		if (LETHEAN_BAPTISM.equals(ritePath)) {
			if (!state.maySeekCure()) return Result.failed(Failure.FOUNDER_CANNOT_ENTER);
			if (!state.infectionSuppressed()) return Result.failed(Failure.INFECTION_NOT_SUPPRESSED);
			if (state.begunPurification()) return Result.failed(Failure.BAPTISM_ALREADY_COMPLETE);
		}
		if (CLARITY_ASCENSION.equals(ritePath)) {
			if (!state.purified()) return Result.failed(Failure.NOT_PURIFIED);
			if (state.clarityUnlocked()) return Result.failed(Failure.CLARITY_ALREADY_UNLOCKED);
			if (!state.clarityPrepared()) return Result.failed(Failure.CLARITY_NOT_PREPARED);
		}
		return Result.accepted();
	}

	public record State(boolean maySeekCure, boolean infectionSuppressed, boolean begunPurification,
			boolean purified, boolean clarityPrepared, boolean clarityUnlocked) {}

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
		NOT_PURIFIED,
		CLARITY_ALREADY_UNLOCKED,
		CLARITY_NOT_PREPARED
	}
}
