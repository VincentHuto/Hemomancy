package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;

import java.util.Map;

public final class NpcProgressionMarkerClientState {
	private static Map<Integer, DialogueAttention> markers = Map.of();

	private NpcProgressionMarkerClientState() {
	}

	public static void replace(Map<Integer, DialogueAttention> snapshot) {
		markers = Map.copyOf(snapshot);
	}

	public static DialogueAttention attention(int entityId) {
		return markers.getOrDefault(entityId, DialogueAttention.NONE);
	}

	public static void clear() {
		markers = Map.of();
	}
}
