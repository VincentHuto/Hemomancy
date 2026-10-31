package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Brined Court balance rules 1, 2 and 4 against every strain's ladder, so a tuning pass cannot leave the cost and
 * cooldown bands unnoticed (spec, "Balance rules"; track 02, "Balance rules every strain keeps").
 */
final class MorphlingBalanceBandsTest {
	@Test
	void everyStrainHasAPassiveAndOnePowerPerStage() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			List<MorphlingPower> powers = MorphlingPowers.of(strain);
			assertEquals(MorphlingPower.Kind.PASSIVE, powers.getFirst().kind(), strain.id() + " starts with its passive");
			Set<Integer> stages = powers.stream().filter(power -> power.kind() != MorphlingPower.Kind.PASSIVE)
					.map(MorphlingPower::stage).collect(Collectors.toSet());
			assertEquals(Set.of(2, 3, 4, 5), stages, strain.id() + " needs one power at each of Developing to Primal");
		}
	}

	@Test
	void reactiveCooldownsStayInTheirStageBand() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			for (MorphlingPower power : MorphlingPowers.of(strain)) {
				if (power.kind() != MorphlingPower.Kind.REACTIVE) continue;
				int min = MorphlingBalanceBands.reactiveCooldownMin(power.stage());
				int max = MorphlingBalanceBands.reactiveCooldownMax(power.stage());
				assertTrue(power.cooldownTicks() >= min && power.cooldownTicks() <= max,
						strain.id() + " " + power.id() + " cooldown " + power.cooldownTicks() + " outside " + min + "-" + max);
				assertTrue(power.homeCooldownTicks() >= min && power.homeCooldownTicks() <= max,
						strain.id() + " " + power.id() + " home cooldown outside its band");
				assertEquals(0.0D, power.costMl(), strain.id() + " " + power.id() + " is free below Primal");
			}
		}
	}

	@Test
	void primalsCostAndRecoverInsideTheirBands() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			for (MorphlingPower power : MorphlingPowers.of(strain)) {
				switch (power.kind()) {
					case PRIMAL -> {
						assertTrue(power.costMl() >= MorphlingBalanceBands.PRIMAL_COST_MIN
								&& power.costMl() <= MorphlingBalanceBands.PRIMAL_COST_MAX, strain.id() + " " + power.id() + " cost");
						assertCooldown(strain, power);
					}
					case TRIGGERED_PRIMAL -> {
						assertEquals(0.0D, power.costMl(), power.id() + " triggers on a kill and costs nothing");
						assertCooldown(strain, power);
					}
					case DEATH_SAVE -> {
						assertTrue(power.costMl() >= MorphlingBalanceBands.DEATH_SAVE_COST_MIN
								&& power.costMl() <= MorphlingBalanceBands.DEATH_SAVE_COST_MAX, strain.id() + " " + power.id() + " cost");
						assertEquals(MorphlingBalanceBands.LAST_RITE_COOLDOWN_TICKS, power.cooldownTicks(),
								power.id() + " shares the Last Rite cooldown");
					}
					default -> {
					}
				}
			}
		}
	}

	@Test
	void onlyTheTwoSettledDeathSavesExist() {
		List<String> saves = MorphlingStrains.ALL.stream().flatMap(strain -> MorphlingPowers.of(strain).stream())
				.filter(power -> power.kind() == MorphlingPower.Kind.DEATH_SAVE).map(MorphlingPower::id).toList();
		assertEquals(List.of("ink_reprieve", "cryptobiosis"), saves);
		assertTrue(MorphlingPowers.of(MorphlingStrains.SUNBURST).stream()
				.noneMatch(power -> power.kind() == MorphlingPower.Kind.DEATH_SAVE), "Sunburst has no self death-save");
	}

	@Test
	void giversNeverDropBelowTheirFloors() {
		assertEquals(0.5F, MorphlingBalanceBands.SHARED_TABLE_GIVER_FLOOR);
		assertEquals(0.5F, MorphlingBalanceBands.COLONY_GIVER_FLOOR);
		assertEquals(0.3F, MorphlingBalanceBands.BREAKWATER_GIVER_FLOOR);
		assertEquals(2.0F, MorphlingBalanceBands.givable(20.0F, 20.0F, 0.5F, 2.0F));
		assertEquals(1.0F, MorphlingBalanceBands.givable(11.0F, 20.0F, 0.5F, 2.0F), 1.0E-6F);
		assertEquals(0.0F, MorphlingBalanceBands.givable(9.0F, 20.0F, 0.5F, 2.0F));
	}

	private static void assertCooldown(MorphlingStrain strain, MorphlingPower power) {
		assertTrue(power.cooldownTicks() >= MorphlingBalanceBands.PRIMAL_COOLDOWN_MIN
				&& power.cooldownTicks() <= MorphlingBalanceBands.PRIMAL_COOLDOWN_MAX,
				strain.id() + " " + power.id() + " cooldown " + power.cooldownTicks());
	}
}
