package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BloodlineRiteReserveTest {
	@Test
	void npcReservePersistsAndBloodspentLastsOneMinecraftDay() {
		UUID leader = UUID.randomUUID();
		UUID npc = UUID.randomUUID();
		Bloodline line = new Bloodline("Test", leader, UUID.randomUUID(), new ArrayList<>());
		assertTrue(line.addNpcMember(npc));
		assertEquals(1000, line.getNpcRiteReserve(npc, 0));

		assertEquals(1000, line.drawNpcRiteReserve(npc, 1200, 500));
		assertTrue(line.isNpcBloodspent(npc, 500));
		assertTrue(line.isNpcBloodspent(npc, 24499));
		assertFalse(line.isNpcBloodspent(npc, 24500));

		Bloodline restored = Bloodline.deserialize(line.serialize());
		assertTrue(restored.isNpcBloodspent(npc, 24499));
		assertEquals(1000, restored.getNpcRiteReserve(npc, 24500));
	}
}
