package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AwakenedIchorianSigilPoseCalculatorTest {

	@Test
	void unfoldingUsesAuthoredStagesAndContinuousTierSizedEndpoints() {
		IchorianSigilDefinition definition = definition(IchorianSigilAnatomy.Style.ARTERIAL_FORK, 3);
		var start = AwakenedIchorianSigilPoseCalculator.calculate(definition, 0);
		var migrationStart = AwakenedIchorianSigilPoseCalculator.calculate(definition, 6);
		var middle = AwakenedIchorianSigilPoseCalculator.calculate(definition, 19);
		var migrated = AwakenedIchorianSigilPoseCalculator.calculate(definition, 32);
		var quickened = AwakenedIchorianSigilPoseCalculator.calculate(definition, 40);

		assertEquals(AwakenedIchorianSigilPoseCalculator.normalizedGroundPosition(definition, 0),
				start.landmarks().getFirst().position());
		assertEquals(0.0F, migrationStart.migration(), 0.0001F);
		assertTrue(middle.migration() > 0 && middle.migration() < 1);
		assertEquals(1.0F, migrated.migration(), 0.0001F);
		assertEquals(1.0F, quickened.quickening(), 0.0001F);
		assertEquals(2, quickened.primaryVessels().size());
		assertEquals(1, quickened.secondaryVessels().size());
		assertEquals(1, quickened.membranes().size());
		assertFinite(middle);
	}

	@Test
	void tierTargetsRangeFromPointEightToOnePointSixBlocks() {
		assertEquals(List.of(0.8F, 1.0F, 1.2F, 1.4F, 1.6F),
				java.util.stream.IntStream.rangeClosed(1, 5)
						.mapToObj(AwakenedIchorianSigilPoseCalculator::tierScale).toList());
	}

	@Test
	void landmarkRadiiScaleMoreGentlyThanPositionalExtent() {
		assertEquals(List.of(0.8F, 0.9F, 1.0F, 1.1F, 1.2F),
				java.util.stream.IntStream.rangeClosed(1, 5)
						.mapToObj(AwakenedIchorianSigilPoseCalculator::radiusScale).toList());
		var tierFive = AwakenedIchorianSigilPoseCalculator.calculate(
				definition(IchorianSigilAnatomy.Style.ARTERIAL_FORK, 5), 0);
		assertEquals(0.13F * 1.2F, tierFive.landmarks().getFirst().radius(), 0.0001F);
	}

	@Test
	void everyCasteHasADistinctLivingDeformationSignature() {
		Set<String> signatures = new HashSet<>();
		for (IchorianSigilAnatomy.Style style : IchorianSigilAnatomy.Style.values()) {
			StringBuilder signature = new StringBuilder();
			for (int age : List.of(41, 58, 75)) {
				var pose = AwakenedIchorianSigilPoseCalculator.calculate(definition(style, 3), age);
				for (var node : pose.landmarks()) {
					signature.append(Math.round(node.position().x * 1000)).append(':')
							.append(Math.round(node.position().y * 1000)).append(':')
							.append(Math.round(node.radius() * 1000)).append('|');
				}
			}
			signatures.add(signature.toString());
		}
		assertEquals(IchorianSigilAnatomy.Style.values().length, signatures.size());
	}

	@Test
	void livingMotionIsBoundedBetweenAdjacentTicks() {
		IchorianSigilDefinition definition = definition(
				IchorianSigilAnatomy.Style.OPTIC_STALK_VEIL, 5);
		var previous = AwakenedIchorianSigilPoseCalculator.calculate(definition, 0);
		for (int age = 1; age <= 100; age++) {
			var current = AwakenedIchorianSigilPoseCalculator.calculate(definition, age);
			for (int index = 0; index < current.landmarks().size(); index++) {
				assertTrue(current.landmarks().get(index).position()
						.distanceTo(previous.landmarks().get(index).position()) < 0.3D);
			}
			previous = current;
		}
	}

	@Test
	void movementAnimatesIndividualNodesInsteadOfDraggingTheRigAsOnePiece() {
		IchorianSigilDefinition definition = definition(
				IchorianSigilAnatomy.Style.ARTERIAL_FORK, 4);
		var idle = AwakenedIchorianSigilPoseCalculator.calculate(definition, 72.0F, 0.0F);
		var moving = AwakenedIchorianSigilPoseCalculator.calculate(definition, 72.0F, 0.08F);

		Vec3 firstShift = moving.landmarks().get(0).position()
				.subtract(idle.landmarks().get(0).position());
		Vec3 secondShift = moving.landmarks().get(1).position()
				.subtract(idle.landmarks().get(1).position());

		assertTrue(firstShift.lengthSqr() > 0.000001D);
		assertTrue(secondShift.lengthSqr() > 0.000001D);
		assertNotEquals(firstShift, secondShift,
				"joint phases should bend vessels rather than translate the entire rig");
	}

	@Test
	void nodeArticulationRemainsSubtleAtFlightSpeed() {
		IchorianSigilDefinition definition = definition(
				IchorianSigilAnatomy.Style.OPTIC_STALK_VEIL, 5);
		var idle = AwakenedIchorianSigilPoseCalculator.calculate(definition, 90.0F, 0.0F);
		var moving = AwakenedIchorianSigilPoseCalculator.calculate(definition, 90.0F, 0.12F);

		for (int index = 0; index < moving.landmarks().size(); index++) {
			assertTrue(moving.landmarks().get(index).position()
					.distanceTo(idle.landmarks().get(index).position()) <= 0.12D);
		}
	}

	@Test
	void flightJointsSweepThroughBroadSmoothArcsInsteadOfVibratingInPlace() {
		IchorianSigilDefinition definition = definition(
				IchorianSigilAnatomy.Style.ARTERIAL_FORK, 3);
		double minShiftX = Double.POSITIVE_INFINITY;
		double maxShiftX = Double.NEGATIVE_INFINITY;
		Vec3 previousShift = null;
		for (int age = 40; age <= 160; age++) {
			var idle = AwakenedIchorianSigilPoseCalculator.calculate(definition, age, 0.0F);
			var moving = AwakenedIchorianSigilPoseCalculator.calculate(definition, age, 0.08F);
			Vec3 shift = moving.landmarks().get(2).position()
					.subtract(idle.landmarks().get(2).position());
			minShiftX = Math.min(minShiftX, shift.x);
			maxShiftX = Math.max(maxShiftX, shift.x);
			if (previousShift != null) {
				assertTrue(shift.distanceTo(previousShift) < 0.008D,
						"adjacent joint poses must remain smooth");
			}
			previousShift = shift;
		}

		assertTrue(maxShiftX - minShiftX > 0.075D,
				"joint should visibly shift through space instead of trembling in place");
	}

	private static IchorianSigilDefinition definition(
			IchorianSigilAnatomy.Style style, int tier) {
		var nodes = List.of(
				new IchorianSigilDefinition.Node(-1, 0),
				new IchorianSigilDefinition.Node(0, -1),
				new IchorianSigilDefinition.Node(1, 0));
		var anatomy = new IchorianSigilAnatomy(new Vec3(0, 0, -1),
				new IchorianSigilAnatomy.Animation(style, 1, 1, 0.5F),
				List.of(
						new IchorianSigilAnatomy.Landmark(0, new Vec3(0, 0, -0.6),
								IchorianSigilAnatomy.Role.EYE, 0.13F),
						new IchorianSigilAnatomy.Landmark(1, Vec3.ZERO,
								IchorianSigilAnatomy.Role.ORGAN, 0.18F),
						new IchorianSigilAnatomy.Landmark(2, new Vec3(0.5, 0.2, 0.5),
								IchorianSigilAnatomy.Role.VALVE, 0.11F)),
				List.of(new IchorianSigilAnatomy.Vessel(1, 2, 0.06F)),
				List.of(new IchorianSigilAnatomy.Membrane(0, 1, 2)));
		return new IchorianSigilDefinition(ResourceLocation.parse("hemomancy:test_" + style.name().toLowerCase()),
				IchorianSigilDefinition.Kind.SUPPORT, tier, 0, "Test", "Test", 0, 0,
				nodes, List.of(), Optional.of(anatomy));
	}

	private static void assertFinite(AwakenedIchorianSigilPose pose) {
		for (var landmark : pose.landmarks()) {
			assertTrue(Double.isFinite(landmark.position().x));
			assertTrue(Double.isFinite(landmark.position().y));
			assertTrue(Double.isFinite(landmark.position().z));
			assertTrue(Float.isFinite(landmark.radius()));
		}
	}
}
