package com.vincenthuto.hemomancy.common.manipulation.verb;

import java.util.Locale;

/**
 * The two mechanical powers of a Harbinger: Projection pushes blood into the world, Absorption pulls it back. The
 * active verb is player state ({@code IKnownManipulations#getActiveVerb}); a selected manipulation can sculpt what it
 * does through a {@link VerbSculpting}.
 */
public enum Verb {
	PROJECT,
	ABSORB;

	public Verb other() {
		return this == PROJECT ? ABSORB : PROJECT;
	}

	/** {@code verb.hemomancy.project} or {@code verb.hemomancy.absorb}. */
	public String langKey() {
		return "verb.hemomancy." + name().toLowerCase(Locale.ROOT);
	}

	public static Verb byId(int id) {
		return id == ABSORB.ordinal() ? ABSORB : PROJECT;
	}

	/** A saved name; anything unknown reads as Projection, the default verb. */
	public static Verb byName(String name) {
		return ABSORB.name().equals(name) ? ABSORB : PROJECT;
	}
}
