package com.vincenthuto.hemomancy.common.capability.player.harbinger.organs;

/**
 * Enum of organs that can be extracted and modified through the Visceral Mirror ritual system.
 * Each organ grants unique benefits and risks when transplanted into the player's body.
 */
public enum EnumOrgan {

	/** Governs blood volume and filtration */
	SPLEEN("Spleen", 3, "Governs blood volume and filtration"),

	/** Metabolizes toxins and purifies the blood */
	LIVER("Liver", 3, "Metabolizes toxins and purifies the blood"),

	/** Oxygenates blood and sustains vital rhythm */
	LUNGS("Lungs", 3, "Oxygenates blood and sustains vital rhythm"),

	/** Filters impurities and maintains humoral balance */
	KIDNEYS("Kidneys", 3, "Filters impurities and maintains humoral balance"),

	/** The seat of circulation and will — the most risky organ */
	HEART("Heart", 4, "The seat of circulation and will");

	private final String name;
	private final int tier;
	private final String description;

	EnumOrgan(String name, int tier, String description) {
		this.name = name;
		this.tier = tier;
		this.description = description;
	}

	/**
	 * Returns the organ's display name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the tier (difficulty/risk level) of extracting this organ.
	 */
	public int getTier() {
		return tier;
	}

	/**
	 * Returns a brief description of the organ's function and effects.
	 */
	public String getDescription() {
		return description;
	}

	/** Returns the translation key for this organ (e.g., {@code hemomancy.organ.spleen}). */
	public String getLangKey() {
		return "hemomancy.organ." + name().toLowerCase();
	}

	/** Returns the organ with the given name (case-insensitive), or null if not found. */
	public static EnumOrgan byName(String name) {
		if (name == null) return null;
		for (EnumOrgan organ : values()) {
			if (organ.name.equalsIgnoreCase(name)) return organ;
		}
		return null;
	}
}
