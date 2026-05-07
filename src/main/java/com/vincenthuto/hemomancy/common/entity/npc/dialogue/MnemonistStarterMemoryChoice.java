package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.Optional;

public enum MnemonistStarterMemoryChoice {
	BLOOD_SHOT("mnemonist_grant_crude_blood_shot", "blood_shot", "crude_memory_blood_shot"),
	BLOOD_RUSH("mnemonist_grant_crude_blood_rush", "blood_rush", "crude_memory_blood_rush"),
	DEADLY_GAZE("mnemonist_grant_crude_deadly_gaze", "deadly_gaze", "crude_memory_deadly_gaze");

	private final String eventId;
	private final String manipulationName;
	private final String itemName;

	public static final String CLAIM_KEY = "hemomancy.mnemonist_starter_memory_claimed";

	MnemonistStarterMemoryChoice(String eventId, String manipulationName, String itemName) {
		this.eventId = eventId;
		this.manipulationName = manipulationName;
		this.itemName = itemName;
	}

	public String eventId() {
		return eventId;
	}

	public String manipulationName() {
		return manipulationName;
	}

	public String itemName() {
		return itemName;
	}

	public static Optional<MnemonistStarterMemoryChoice> fromEventId(String eventId) {
		for (MnemonistStarterMemoryChoice choice : values()) {
			if (choice.eventId.equals(eventId)) {
				return Optional.of(choice);
			}
		}
		return Optional.empty();
	}

	public static boolean canClaim(int degree, boolean purifying, boolean clarity, boolean alreadyClaimed) {
		return degree >= 1 && !purifying && !clarity && !alreadyClaimed;
	}
}
