package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;

/**
 * The eight houses of the Brined Court, shallow to deep. Each keeps one hereditary Morphling strain
 * ({@link MorphlingStrains}). Track 5 may move this beside the Court player state once that exists.
 */
public enum CourtHouse implements StringRepresentable {
	GIVING_LIGHT("giving_light"),
	HOLDING_STONE("holding_stone"),
	SHARED_WAKE("shared_wake"),
	WOVEN_CURRENT("woven_current"),
	VEILED_FOLD("veiled_fold"),
	STILL_BELL("still_bell"),
	LAST_MARROW("last_marrow"),
	KINDLING("kindling");

	private final String id;

	CourtHouse(String id) {
		this.id = id;
	}

	public String id() {
		return this.id;
	}

	/** The house with this id, or null. */
	@Nullable
	public static CourtHouse byId(String id) {
		for (CourtHouse house : values()) if (house.id.equals(id)) return house;
		return null;
	}

	/** The house's name as players read it ("the Giving Light"). */
	public Component displayName() {
		return Component.translatable("court_house.hemomancy." + this.id);
	}

	/** The id, so a house can be a block-state property (the Court's house markers). */
	@Override
	public String getSerializedName() {
		return this.id;
	}
}
