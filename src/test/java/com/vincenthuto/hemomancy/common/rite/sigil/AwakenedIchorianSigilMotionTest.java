package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AwakenedIchorianSigilMotionTest {
	private static final double HOVER_X = 0.5D;
	private static final double HOVER_Y = 67.3D;
	private static final double HOVER_Z = 1.5D;

	@Test
	void beginsOnTheInscribedSurfaceAndFinishesPeelingAtTheHoverPoint() {
		var start = position(0);
		var peeled = position(AwakenedIchorianSigilMotion.PEEL_TICKS);

		assertEquals(4.5D, start.x(), 0.0001D);
		assertEquals(65.1D, start.y(), 0.0001D);
		assertEquals(0.5D, start.z(), 0.0001D);
		assertEquals(HOVER_X, peeled.x(), 0.0001D);
		assertEquals(HOVER_Z, peeled.z(), 0.0001D);
		assertTrue(Math.abs(peeled.y() - HOVER_Y) <= AwakenedIchorianSigilMotion.BOB_AMPLITUDE + 1.0E-6D);
		assertEquals(0.0F, AwakenedIchorianSigilMotion.peelProgress(0), 0.0001F);
		assertEquals(0.5F, AwakenedIchorianSigilMotion.peelProgress(15), 0.0001F);
		assertEquals(1.0F, AwakenedIchorianSigilMotion.peelProgress(30), 0.0001F);
	}

	@Test
	void holdsItsPointAndOnlyBobs() {
		for (int age = AwakenedIchorianSigilMotion.PEEL_TICKS; age <= 2000; age++) {
			var position = position(age);
			assertEquals(HOVER_X, position.x(), 0.0D, "drifted in x at age " + age);
			assertEquals(HOVER_Z, position.z(), 0.0D, "drifted in z at age " + age);
			assertTrue(Math.abs(position.y() - HOVER_Y) <= AwakenedIchorianSigilMotion.BOB_AMPLITUDE + 1.0E-6D,
					"bobbed too far at age " + age);
		}
	}

	@Test
	void theBobNeverSnaps() {
		var previous = position(AwakenedIchorianSigilMotion.PEEL_TICKS);
		for (int age = AwakenedIchorianSigilMotion.PEEL_TICKS + 1; age <= 600; age++) {
			var current = position(age);
			assertTrue(Math.abs(current.y() - previous.y()) <= 0.02D, "snapped at age " + age);
			previous = current;
		}
	}

	@Test
	void hoverPointSitsOverThePatternCentre() {
		IchorianSigilDefinition sigil = new IchorianSigilDefinition(
				ResourceLocation.parse("hemomancy:test_triangle"),
				IchorianSigilDefinition.Kind.SUPPORT, 1, 0, "Test", "Test", 0, 0,
				List.of(new IchorianSigilDefinition.Node(-2, 0),
						new IchorianSigilDefinition.Node(2, 0),
						new IchorianSigilDefinition.Node(0, 2)),
				List.of(), Optional.empty());

		var hover = AwakenedIchorianSigilMotion.hoverPoint(sigil, 10.0D, 64.0D, 20.0D);

		assertEquals(10.0D, hover.x(), 0.001D);
		assertEquals(66.2D, hover.y(), 0.001D);
		assertEquals(20.667D, hover.z(), 0.001D);
	}

	@Test
	void eachSigilTurnsAtItsOwnRate() {
		assertEquals(120.0F, AwakenedIchorianSigilMotion.turnYaw(id("shunt"), 0.0F, 100.0F), 0.001F);
		assertEquals(12.0F, AwakenedIchorianSigilMotion.turnYaw(id("seal"), 0.0F, 100.0F), 0.001F);
		assertEquals(40.0F, AwakenedIchorianSigilMotion.turnYaw(id("shunt"), 40.0F, 0.0F), 0.001F);
		assertEquals(50.0F, AwakenedIchorianSigilMotion.turnYaw(id("unknown_sigil"), 0.0F, 100.0F), 0.001F);
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

	private static AwakenedIchorianSigilMotion.Position position(int age) {
		return AwakenedIchorianSigilMotion.position(
				4.5D, 65.1D, 0.5D,
				HOVER_X, HOVER_Y, HOVER_Z, age);
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}

	private static void assertPosition(AwakenedIchorianSigilMotion.Position actual,
			double x, double y, double z) {
		assertEquals(x, actual.x(), 0.0001D);
		assertEquals(y, actual.y(), 0.0001D);
		assertEquals(z, actual.z(), 0.0001D);
	}
}
