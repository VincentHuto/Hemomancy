package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperWingedFlightPersistenceSourceTest {
	private static final Path ENTITY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");
	private static final Path ORDEAL = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/worldgen/VesperOrdealManager.java");

	@Test
	void entitySynchronizesAndPersistsEveryRecoverableFlightField() throws Exception {
		String source = Files.readString(ENTITY);
		for (String token : new String[] {
				"DATA_WINGS_GROWN", "DATA_FLIGHT_MODE", "DATA_FLIGHT_TICK",
				"DATA_LOCKED_AIM_X", "DATA_LOCKED_AIM_Y", "DATA_LOCKED_AIM_Z",
				"WingsGrown", "FlightMode", "FlightTick", "GroundedFlightTicks",
				"AirborneTicks", "SortieCount", "FlightHitMask", "LockedFlightAimX",
				"LockedFlightAimY", "LockedFlightAimZ", "SummonedFlightArenaBound",
				"SummonedFlightArenaCenterX", "SummonedFlightArenaFloorY",
				"SummonedFlightArenaCenterZ", "SummonedFlightArenaDimension" }) {
			assertTrue(source.contains(token), "missing synchronized/persisted flight contract " + token);
		}
	}

	@Test
	void flightAuthorityComesFromTheActiveOwnerBoundOrdeal() throws Exception {
		String source = Files.readString(ORDEAL);
		assertTrue(source.contains("flightArena(VesperTheCrownedRefusalEntity"));
		assertTrue(source.contains("owner.getPersistentData().getLong(ACTIVE_BLOOM_KEY) != vesper.getBloomOrigin()"));
		assertTrue(source.contains("owner.level() != vesper.level()"));
	}

	@Test
	void wingGrowthDoesNotRequireFlightArenaAuthority() throws Exception {
		String source = Files.readString(ENTITY);
		assertTrue(source.contains("VesperWingedFlightRules.shouldStartWingGrowth(getHealth(), getMaxHealth(), hasWingsGrown(),\n"
				+ "\t\t\t\tfalse, false)) {"),
				"the once-only 50% growth trigger must run even when sorties are forbidden outside the ordeal arena");
	}

	@Test
	void bossNeedlesUseCrownedAttributionAndExactDamage() throws Exception {
		String source = Files.readString(ENTITY);
		assertTrue(source.contains("new BloodNeedleEntity(server, this)"));
		assertTrue(source.contains("needle.setBaseDamage(4.0D)"));
	}

	@Test
	void ordinaryMeleeIsSuppressedThroughoutGrowthAndFlight() throws Exception {
		String source = Files.readString(ENTITY);
		assertTrue(source.contains("getFlightMode() != VesperWingedFlightRules.FlightMode.GROUNDED) return false"));
	}

	@Test
	void transformationReloadCancelsFlightAndRestoresInvulnerability() throws Exception {
		String source = Files.readString(ENTITY);
		assertTrue(source.contains("setInvulnerable(getTransitionTick() > 0)"));
		assertTrue(source.contains("if (getTransitionTick() > 0 && getFlightMode().airborne())"));
		assertTrue(source.contains("beginLanding(flightArena.orElse(null));"));
	}
}
