package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

/**
 * Whether an empty Specimen Jar may take a creature. The tag {@code hemomancy:specimen_jar_capturable} names the
 * kinds; these rules hold for the one creature in front of the jar. A creature riding something is lifted off first
 * by the jar, so riding is not a refusal; one carrying riders is, because the riders would be lost with it.
 */
public final class SpecimenCaptureRules {
	/** Widest body a jar takes, in blocks; every tagged kind is at most 1.85 wide at its normal size. */
	public static final float MAX_WIDTH = 2.0F;
	/** Tallest body a jar takes, in blocks; every tagged kind is at most 1.8 tall at its normal size. */
	public static final float MAX_HEIGHT = 2.0F;
	/** Highest maximum health a jar takes. */
	public static final float MAX_HEALTH = 60.0F;

	private SpecimenCaptureRules() {
	}

	public enum Verdict {
		CAPTURE,
		NOT_A_SPECIMEN,
		DEAD,
		BOSS,
		OWNED,
		CARRYING_RIDERS,
		TOO_LARGE,
		TOO_STRONG;

		/** The lang key of the refusal shown to the player, or null when nothing is said. */
		public String messageKey() {
			return switch (this) {
				case CAPTURE, NOT_A_SPECIMEN, DEAD -> null;
				default -> "message.hemomancy.specimen_jar.refused." + name().toLowerCase(java.util.Locale.ROOT);
			};
		}
	}

	/** What the jar can see of a creature. {@code ownedByAnother} is a tamed or bound creature of someone else's. */
	public record Candidate(boolean listed, boolean alive, float width, float height, float maxHealth, boolean boss,
			boolean ownedByAnother, boolean carriesRiders) {
	}

	public static Verdict judge(Candidate candidate) {
		if (!candidate.listed()) return Verdict.NOT_A_SPECIMEN;
		if (!candidate.alive()) return Verdict.DEAD;
		if (candidate.boss()) return Verdict.BOSS;
		if (candidate.ownedByAnother()) return Verdict.OWNED;
		if (candidate.carriesRiders()) return Verdict.CARRYING_RIDERS;
		if (candidate.width() > MAX_WIDTH || candidate.height() > MAX_HEIGHT) return Verdict.TOO_LARGE;
		if (candidate.maxHealth() > MAX_HEALTH) return Verdict.TOO_STRONG;
		return Verdict.CAPTURE;
	}
}
