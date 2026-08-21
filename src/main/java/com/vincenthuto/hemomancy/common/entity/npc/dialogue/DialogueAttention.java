package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public enum DialogueAttention {
	NONE,
	NOTICE,
	URGENT;

	private static final DialogueAttention[] VALUES = values();

	public static DialogueAttention fromOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : NONE;
	}

	public static DialogueAttention strongest(DialogueAttention first, DialogueAttention second) {
		return first.ordinal() >= second.ordinal() ? first : second;
	}
}
