package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteNpcStationRulesTest {
	private static final BlockPos STATION = new BlockPos(10, 64, -4);

	@Test
	void assignedNpcOnlyParticipatesNearItsSafeRoleStation() {
		assertTrue(CardinalRiteNpcStationRules.participates(
				new Vec3(12.75D, 64.0D, -3.5D), STATION, true));
		assertFalse(CardinalRiteNpcStationRules.participates(
				new Vec3(14.0D, 64.0D, -3.5D), STATION, true));
		assertFalse(CardinalRiteNpcStationRules.participates(
				new Vec3(10.5D, 64.0D, -3.5D), STATION, false));
	}

	@Test
	void nearbyDisplacementWalksBackWithoutTeleporting() {
		assertEquals(CardinalRiteNpcStationRules.Correction.APPROACH,
				CardinalRiteNpcStationRules.correction(
						new Vec3(14.5D, 64.0D, -3.5D), STATION, true));
	}

	@Test
	void fallingOrDistantNpcIsRecalledToTheStation() {
		assertEquals(CardinalRiteNpcStationRules.Correction.RECALL,
				CardinalRiteNpcStationRules.correction(
						new Vec3(10.5D, 61.9D, -3.5D), STATION, true));
		assertEquals(CardinalRiteNpcStationRules.Correction.RECALL,
				CardinalRiteNpcStationRules.correction(
						new Vec3(19.0D, 64.0D, -3.5D), STATION, true));
	}

	@Test
	void unsafeRoleStationCannotHoldOrRecallAnNpc() {
		assertEquals(CardinalRiteNpcStationRules.Correction.UNAVAILABLE,
				CardinalRiteNpcStationRules.correction(
						new Vec3(10.5D, 64.0D, -3.5D), STATION, false));
	}

	@Test
	void npcAtItsMarkerIsHeldThere() {
		assertEquals(CardinalRiteNpcStationRules.Correction.HOLD,
				CardinalRiteNpcStationRules.correction(
						new Vec3(10.5D, 64.0D, -3.5D), STATION, true));
	}

	@Test
	void stationRequiresLoadedGroundSupportAndClearNpcSpace() {
		assertTrue(CardinalRiteNpcStationRules.stationSafe(true, true, true));
		assertFalse(CardinalRiteNpcStationRules.stationSafe(false, true, true));
		assertFalse(CardinalRiteNpcStationRules.stationSafe(true, false, true));
		assertFalse(CardinalRiteNpcStationRules.stationSafe(true, true, false));
	}

	@Test
	void riteCleanupReturnsOnlyAssignedNpcAllies() {
		UUID npcOne = UUID.randomUUID();
		UUID playerAlly = UUID.randomUUID();
		UUID npcTwo = UUID.randomUUID();
		Map<UUID, CardinalRiteAllyRole> assignments = new LinkedHashMap<>();
		assignments.put(npcOne, CardinalRiteAllyRole.ANCHOR);
		assignments.put(playerAlly, CardinalRiteAllyRole.ATTENDANT);
		assignments.put(npcTwo, CardinalRiteAllyRole.WARDEN);

		assertEquals(List.of(npcOne, npcTwo),
				CardinalRiteNpcStationRules.assignedNpcAllies(
						assignments, id -> !id.equals(playerAlly)));
	}

	@Test
	void faneReturnArrivesCenteredOneBlockAboveRecallPoint() {
		assertEquals(new Vec3(20.5D, 73.0D, -8.5D),
				CardinalRiteNpcStationRules.faneReturnPosition(new BlockPos(20, 72, -9)));
	}
}
