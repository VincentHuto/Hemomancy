package com.vincenthuto.hemomancy.common.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingRules;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** LP-4, NQ-01-7: a sutured buffer clots like any tank, and its suture asks to revive the clot. */
class SuturedBufferReviveRulesTest {
	private static BlockBloodVolume clottedBuffer() {
		BlockBloodVolume buffer = new BlockBloodVolume();
		buffer.setMaxBloodVolume(600.0D);
		buffer.settle(0L, false);
		buffer.fill(600.0D);
		buffer.settle(CoagulationRules.FRESH_WINDOW_TICKS + CoagulationRules.MINUTE_TICKS, false);
		return buffer;
	}

	@Test
	void aBufferWhoseClotFillsItsRoomStillAsks() {
		BlockBloodVolume buffer = clottedBuffer();
		assertEquals(540.0D, buffer.getBloodVolume(), 1e-6);
		assertEquals(60.0D, BloodRoutingRules.calculateReserveRequest(buffer, 600.0D, 1_000.0D), 1e-6);
	}

	@Test
	void anIdleBufferCostsAQuarterOfWhatClots() {
		BlockBloodVolume buffer = clottedBuffer();
		double request = BloodRoutingRules.calculateReserveRequest(buffer, 600.0D, 1_000.0D);
		CoagulationRules.Pour pour = buffer.revive(
				buffer.pourOf(request, 0.5D, CoagulationRules.DEFAULT_SUTURED_REVIVE_YIELD).taken(), 0.5D,
				CoagulationRules.DEFAULT_SUTURED_REVIVE_YIELD);
		assertEquals(15.0D, pour.taken(), 1e-6);
		assertEquals(600.0D, buffer.getBloodVolume(), 1e-6);
		assertEquals(0.0D, buffer.clotted(), 1e-9);
	}
}
