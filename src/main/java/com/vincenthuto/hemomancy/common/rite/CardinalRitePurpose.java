package com.vincenthuto.hemomancy.common.rite;

import java.util.Locale;

/**
 * What a Harbinger rite is for. The purpose decides the floor style the rite is
 * performed on and how it resolves when no culmination is authored.
 */
public enum CardinalRitePurpose {
	/** Degree up, or a power or body change in the caster. */
	SELF("self", "threshold", CardinalRiteCulmination.ASCENSION),
	/** Changes the world: conjures, alters weather, makes something appear. */
	WORLD("world", "dominion", CardinalRiteCulmination.DOMAIN),
	/** Consumes components to make something new. */
	MAKING("making", "working", CardinalRiteCulmination.FORGING),
	/** Acts on ties between people: bloodlines, covenants, shared pools. */
	BOND("bond", "communion", CardinalRiteCulmination.BINDING);

	private final String id;
	private final String floorStyle;
	private final CardinalRiteCulmination defaultCulmination;

	CardinalRitePurpose(String id, String floorStyle, CardinalRiteCulmination defaultCulmination) {
		this.id = id;
		this.floorStyle = floorStyle;
		this.defaultCulmination = defaultCulmination;
	}

	public String id() {
		return id;
	}

	public String floorStyle() {
		return floorStyle;
	}

	public CardinalRiteCulmination defaultCulmination() {
		return defaultCulmination;
	}

	public static CardinalRitePurpose byId(String id) {
		if (id == null || id.isBlank()) return null;
		String wanted = id.trim().toLowerCase(Locale.ROOT);
		for (CardinalRitePurpose purpose : values()) {
			if (purpose.id.equals(wanted)) return purpose;
		}
		return null;
	}

	/** Floor ids are {@code <style>_<tier>}; the style is everything before the last underscore. */
	public static String floorStyleOf(String floorPath) {
		int split = floorPath.lastIndexOf('_');
		return split <= 0 ? floorPath : floorPath.substring(0, split);
	}

	/** @return why the floor does not suit the purpose, or null when it does */
	public static String violation(CardinalRitePurpose purpose, boolean special, String floorPath) {
		if (purpose == null) return "declares no purpose (self, world, making or bond)";
		if (special) return null;
		return purpose.floorStyle.equals(floorStyleOf(floorPath)) ? null
				: "is a " + purpose.id + " rite and must use a " + purpose.floorStyle + " floor, not " + floorPath;
	}
}
