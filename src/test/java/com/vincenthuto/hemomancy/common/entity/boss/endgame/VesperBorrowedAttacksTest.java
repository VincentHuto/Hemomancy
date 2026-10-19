package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class VesperBorrowedAttacksTest {
	@Test
	void everyRosterShapeMapsToItsOwnAttack() {
		Set<VesperWeaponAction> seen = new HashSet<>();
		for (String shape : VesperPuppetRoster.SHAPES) {
			VesperWeaponAction attack = VesperBorrowedAttacks.forShape(shape).orElseThrow(() -> new AssertionError(shape));
			assertTrue(seen.add(attack), shape + " shares an attack");
			assertNull(attack.tendency(), "borrowed attacks belong to no school");
			assertTrue(attack.impactTick() >= 10, attack.name());
			int recovery = attack.durationTicks() - attack.lastImpactTick();
			assertTrue(recovery >= 8 && recovery <= 14, attack.name());
		}
		assertTrue(VesperBorrowedAttacks.forShape(PuppeteerSummonDefinitions.RINGMASTER_PATTERN).isEmpty());
	}

	@Test
	void noInheritanceMeansNoBorrowedAttacks() {
		assertTrue(VesperBorrowedAttacks.available(List.of()).isEmpty());
	}

	@Test
	void availableFollowsTheInheritedShapes() {
		List<VesperWeaponAction> attacks = VesperBorrowedAttacks.available(List.of(
				PuppeteerSummonDefinitions.GOREBOUND_HULK, PuppeteerSummonDefinitions.MNEMONIST_PUPPET));
		assertEquals(List.of(VesperWeaponAction.BORROWED_HULK_YANK, VesperWeaponAction.BORROWED_MNEMONIC_LASH), attacks);
	}

	@Test
	void borrowedAttacksAreRarerInTheSickleAct() {
		assertTrue(VesperBorrowedAttacks.weight(VesperEveningStarAct.SICKLES)
				< VesperBorrowedAttacks.weight(VesperEveningStarAct.OPEN));
		assertEquals(300, VesperBorrowedAttacks.COOLDOWN_TICKS);
	}

	@Test
	void mnemonicLashReplaysMostOfTheBiggestHitButIsCapped() {
		assertEquals(6.0F, VesperBorrowedAttacks.mnemonicDamage(10.0F), 1.0E-5F);
		assertEquals(14.0F, VesperBorrowedAttacks.mnemonicDamage(80.0F), 1.0E-5F);
		assertEquals(4.0F, VesperBorrowedAttacks.mnemonicDamage(0.0F), 1.0E-5F);
	}
}
