package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public enum DialogueOptionStyle {
	NORMAL,
	EMPHASIZED,
	DANGER;

	private static final DialogueOptionStyle[] VALUES = values();

	public static DialogueOptionStyle fromOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : NORMAL;
	}
}
