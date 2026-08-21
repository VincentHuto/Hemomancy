package com.vincenthuto.hemomancy.client.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;

class NpcProgressionMarkerClientStateTest {
	@AfterEach
	void clear() {
		NpcProgressionMarkerClientState.clear();
	}

	@Test
	void replacingSnapshotDropsStaleEntityIds() {
		NpcProgressionMarkerClientState.replace(Map.of(4, DialogueAttention.NOTICE, 8, DialogueAttention.URGENT));
		NpcProgressionMarkerClientState.replace(Map.of(8, DialogueAttention.NOTICE));

		assertEquals(DialogueAttention.NONE, NpcProgressionMarkerClientState.attention(4));
		assertEquals(DialogueAttention.NOTICE, NpcProgressionMarkerClientState.attention(8));
	}
}
