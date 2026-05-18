package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

/**
 * The eight degrees of the Hematic Order.
 * Each degree is a rank within the secret society that the player
 * advances through as part of their initiatory journey.
 */
public enum EnumInitiatoryDegree {

	/** Degree 1 — awarded by the Rite of Sanguine Initiation */
	NEOPHYTE(1, "Neophyte of the Crimson Veil"),

	/** Degree 2 */
	VOTARY(2, "Votary of the Hematic Covenant"),

	/** Degree 3 */
	INITIATE(3, "Initiate of the Scarlet Sanctum"),

	/** Degree 4 */
	ADEPT(4, "Adept of the Sanguine Brotherhood"),

	/** Degree 5 */
	ILLUMINATUS(5, "Illuminatus of the Crimson Lodge"),

	/** Degree 6 */
	SANCTIFIED(6, "Sanctified of the Bloodline Covenant"),

	/** Degree 7 */
	ARCHON(7, "Archon of the Hematic Order"),

	/** Degree 8 — final rank */
	APOTHEOS(8, "Apotheos of the Hematic Order");

	private final int number;
	private final String title;

	EnumInitiatoryDegree(int number, String title) {
		this.number = number;
		this.title = title;
	}

	/** Returns the translation key for this degree (e.g., {@code hemomancy.degree.1}). */
	public String getLangKey() {
		return "hemomancy.degree." + number;
	}

	/**
	 * Returns the hardcoded English title for this degree.
	 * <p>
	 * <b>Note:</b> This is intended for internal/fallback use only.
	 * For localised display, use {@link #getLangKey()} with
	 * {@code Component.translatable()}.
	 * </p>
	 */
	public String getTitle() {
		return title;
	}

	public int getNumber() {
		return number;
	}

	/** Returns the degree that follows this one, or null if already at the highest. */
	public EnumInitiatoryDegree next() {
		return (number < 8) ? values()[number] : null;
	}

	/** Returns the degree with the given number (1–8), or null if not found. */
	public static EnumInitiatoryDegree byNumber(int number) {
		for (EnumInitiatoryDegree d : values()) {
			if (d.number == number) return d;
		}
		return null;
	}
}
