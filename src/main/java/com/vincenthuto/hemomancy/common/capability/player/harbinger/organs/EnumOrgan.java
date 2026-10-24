package com.vincenthuto.hemomancy.common.capability.player.harbinger.organs;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Enum of organs that can be extracted and modified through the Visceral Mirror ritual system.
 * Each organ grants unique benefits and risks when transplanted into the player's body.
 */
public enum EnumOrgan {

	/** Governs blood volume and filtration */
	SPLEEN(3),

	/** Metabolizes toxins and purifies the blood */
	LIVER(3),

	/** Oxygenates blood and sustains vital rhythm */
	LUNGS(3),

	/** Filters impurities and maintains humoral balance */
	KIDNEYS(3),

	/** The seat of circulation and will — the most risky organ */
	HEART(4);

	private final int tier;

	EnumOrgan(int tier) {
		this.tier = tier;
	}

	/**
	 * Returns the organ's display name.
	 */
	public MutableComponent getDisplayName() {
		return Component.translatable(getLangKey());
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
	public MutableComponent getDescription() {
		return Component.translatable(getLangKey() + ".description");
	}

	/** Returns the translation key for this organ (e.g., {@code hemomancy.organ.spleen}). */
	public String getLangKey() {
		return "hemomancy.organ." + name().toLowerCase();
	}

	/** Returns the organ with the given name (case-insensitive), or null if not found. */
	public static EnumOrgan byName(String name) {
		if (name == null) return null;
		for (EnumOrgan organ : values()) {
			if (organ.name().equalsIgnoreCase(name)) return organ;
		}
		return null;
	}
}
