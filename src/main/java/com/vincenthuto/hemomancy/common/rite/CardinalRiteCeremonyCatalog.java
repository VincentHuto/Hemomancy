package com.vincenthuto.hemomancy.common.rite;

/**
 * Named layout generators available to explicitly authored ceremony JSON.
 */
public final class CardinalRiteCeremonyCatalog {
	public enum Layout {
		CARDINAL,
		DIAGONAL,
		CROOKED,
		SERPENTINE;

		public static Layout byName(String name) {
			if (name == null) return CARDINAL;
			try {
				return valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				return CARDINAL;
			}
		}
	}
	private CardinalRiteCeremonyCatalog() {}
}
