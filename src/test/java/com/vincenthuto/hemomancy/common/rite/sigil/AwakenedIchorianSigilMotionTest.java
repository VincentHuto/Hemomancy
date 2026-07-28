package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AwakenedIchorianSigilMotionTest {
	@Test
	void beginsOnTheInscribedSurfaceAndFinishesPeelingAtTheOrbit() {
		var start = AwakenedIchorianSigilMotion.position(
				4.5D, 65.1D, 0.5D,
				0.5D, 67.5D, 0.5D,
				2.0D, 0.0D, 0);
		var peeled = AwakenedIchorianSigilMotion.position(
				4.5D, 65.1D, 0.5D,
				0.5D, 67.5D, 0.5D,
				2.0D, 0.0D, 30);

		assertPosition(start, 4.5D, 65.1D, 0.5D);
		assertPosition(peeled, 2.5D, 67.5D, 0.5D);
		assertEquals(0.0F, AwakenedIchorianSigilMotion.peelProgress(0), 0.0001F);
		assertEquals(0.5F, AwakenedIchorianSigilMotion.peelProgress(15), 0.0001F);
		assertEquals(1.0F, AwakenedIchorianSigilMotion.peelProgress(30), 0.0001F);
	}

	@Test
	void orbitsTheRiteAfterPeelingFree() {
		var quarterOrbit = AwakenedIchorianSigilMotion.position(
				4.5D, 65.1D, 0.5D,
				0.5D, 67.5D, 0.5D,
				2.0D, 0.0D, 90);

		assertPosition(quarterOrbit, 0.5D, 67.5D, 2.5D);
	}

	@Test
	void everyKnownSigilHasADistinctFlightSignature() {
		List<String> sigils = List.of(
				"reservoir", "bastion", "hematic_lattice", "mnemonic",
				"suture", "shunt", "seal", "cage", "lens");
		Set<String> signatures = new HashSet<>();

		for (String sigil : sigils) {
			StringBuilder signature = new StringBuilder();
			for (int age : List.of(60, 90, 120)) {
				var position = personalityPosition(sigil, age);
				signature.append(Math.round(position.x() * 100.0D)).append(':')
						.append(Math.round(position.y() * 100.0D)).append(':')
						.append(Math.round(position.z() * 100.0D)).append('|');
			}
			signatures.add(signature.toString());
		}

		assertEquals(9, signatures.size(), "each sigil needs a recognizable trajectory");
	}

	@Test
	void ritualFunctionShapesTheMovementPersonality() {
		var reservoir = personalityPosition("reservoir", 90);
		var bastion = personalityPosition("bastion", 90);
		var seal = personalityPosition("seal", 90);
		var lens = personalityPosition("lens", 90);

		assertTrue(horizontalDistance(reservoir) < horizontalDistance(bastion),
				"the blood reservoir stays tucked safely inside the warden's patrol");
		assertTrue(reservoir.y() < 67.5D, "the weighty reservoir flies low");
		assertTrue(horizontalDistance(seal) < 0.75D, "the silencing seal holds near the ritual heart");
		assertTrue(lens.y() > 68.0D, "the revealing lens scans from above");
	}

	@Test
	void everyPersonalityRemainsInsideItsAssignedOrbitRadius() {
		for (String sigil : List.of(
				"reservoir", "bastion", "hematic_lattice", "mnemonic",
				"suture", "shunt", "seal", "cage", "lens")) {
			for (int age = 30; age <= 510; age += 7) {
				assertTrue(horizontalDistance(personalityPosition(sigil, age)) <= 2.0001D,
						sigil + " escaped the ritual space at age " + age);
			}
		}
	}

	@Test
	void livingFlightNeverSnapsBetweenAngularWaypoints() {
		for (String sigil : List.of("bastion", "cage")) {
			var previous = personalityPosition(sigil, 30);
			for (int age = 31; age <= 300; age++) {
				var current = personalityPosition(sigil, age);
				double movement = Math.sqrt(
						(current.x() - previous.x()) * (current.x() - previous.x())
								+ (current.y() - previous.y()) * (current.y() - previous.y())
								+ (current.z() - previous.z()) * (current.z() - previous.z()));
				assertTrue(movement <= 0.4D,
						sigil + " snapped " + movement + " blocks at age " + age);
				previous = current;
			}
		}
	}

	@Test
	void networkCorrectionsAdvanceSmoothlyAndFinishOnTheirTarget() {
		var target = new AwakenedIchorianSigilMotion.Position(6.0D, 3.0D, -3.0D);
		var first = AwakenedIchorianSigilMotion.smoothStep(
				new AwakenedIchorianSigilMotion.Position(0.0D, 0.0D, 0.0D), target, 3);
		var second = AwakenedIchorianSigilMotion.smoothStep(first, target, 2);
		var third = AwakenedIchorianSigilMotion.smoothStep(second, target, 1);

		assertPosition(first, 2.0D, 1.0D, -1.0D);
		assertPosition(second, 4.0D, 2.0D, -2.0D);
		assertPosition(third, 6.0D, 3.0D, -3.0D);
	}

	private static AwakenedIchorianSigilMotion.Position personalityPosition(String path, int age) {
		return AwakenedIchorianSigilMotion.position(
				ResourceLocation.fromNamespaceAndPath("hemomancy", path),
				4.5D, 65.1D, 0.5D,
				0.5D, 67.5D, 0.5D,
				2.0D, 0.0D, age);
	}

	private static double horizontalDistance(AwakenedIchorianSigilMotion.Position position) {
		return Math.hypot(position.x() - 0.5D, position.z() - 0.5D);
	}

	private static void assertPosition(AwakenedIchorianSigilMotion.Position actual,
			double x, double y, double z) {
		assertEquals(x, actual.x(), 0.0001D);
		assertEquals(y, actual.y(), 0.0001D);
		assertEquals(z, actual.z(), 0.0001D);
	}
}
