package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import net.minecraft.network.chat.Component;

/**
 * The eight degrees of the Hematic Order.
 * Each degree is a rank within the secret society that the player
 * advances through as part of their initiatory journey.
 */
public enum EnumInitiatoryDegree {

	/** Degree 1 — awarded by formal Vicar initiation */
	NEOPHYTE(1),

	/** Degree 2 */
	VOTARY(2),

	/** Degree 3 */
	INITIATE(3),

	/** Degree 4 */
	ADEPT(4),

	/** Degree 5 */
	ILLUMINATUS(5),

	/** Degree 6 */
	SANCTIFIED(6),

	/** Degree 7 */
	ARCHON(7),

	/** Degree 8 — final rank */
	APOTHEOS(8);

	private final int number;

	EnumInitiatoryDegree(int number) {
		this.number = number;
	}

	/** Returns the translation key for this degree (e.g., {@code hemomancy.degree.1}). */
	public String getLangKey() {
		return "hemomancy.degree." + number;
	}

	/** The degree's title, from {@link #getLangKey()}. */
	public Component displayName() {
		return Component.translatable(getLangKey());
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
