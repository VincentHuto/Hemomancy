package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public enum DialogueScreenMode {
	TOPIC_HUB,
	FOCUSED;

	private static final DialogueScreenMode[] VALUES = values();

	public static DialogueScreenMode fromOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : FOCUSED;
	}
}
