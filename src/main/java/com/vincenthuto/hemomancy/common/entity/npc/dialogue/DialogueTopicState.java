package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public enum DialogueTopicState {
	AVAILABLE,
	ACTIVE,
	COMPLETE,
	TURN_IN,
	LOCKED,
	DISABLED;

	private static final DialogueTopicState[] VALUES = values();

	public static DialogueTopicState fromOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : DISABLED;
	}

	public boolean enabled() {
		return this != LOCKED && this != DISABLED;
	}
}
