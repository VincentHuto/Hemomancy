package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public enum DialogueCategory {
	QUESTS,
	INQUIRIES,
	LORE,
	CONVERSATION;

	private static final DialogueCategory[] VALUES = values();

	public static DialogueCategory fromOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : CONVERSATION;
	}
}
