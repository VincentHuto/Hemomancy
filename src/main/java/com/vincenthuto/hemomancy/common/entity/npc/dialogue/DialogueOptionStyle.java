package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public enum DialogueOptionStyle {
	NORMAL,
	EMPHASIZED,
	DANGER,
	/** A special action: filed in no box but in the hub's Important tab, and drawn glowing wherever it shows. */
	SPECIAL_ACTION;

	private static final DialogueOptionStyle[] VALUES = values();

	public static DialogueOptionStyle fromOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : NORMAL;
	}
}
