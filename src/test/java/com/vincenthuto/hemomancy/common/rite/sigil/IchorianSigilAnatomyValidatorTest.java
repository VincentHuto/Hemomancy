package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class IchorianSigilAnatomyValidatorTest {

	@Test
	void acceptsACompleteFiniteAnatomicalRig() {
		var result = IchorianSigilAnatomyValidator.validate(3, validRig());

		assertTrue(result.form().isPresent());
		assertEquals(List.of(), result.errors());
	}

	@Test
	void rejectsIncompleteOrDuplicatedSourceMappings() {
		IchorianSigilAnatomy rig = validRig();
		var duplicated = new IchorianSigilAnatomy(rig.forward(), rig.animation(), List.of(
				rig.landmarks().get(0),
				new IchorianSigilAnatomy.Landmark(0, new Vec3(0, 0, 0),
						IchorianSigilAnatomy.Role.ORGAN, 0.18F),
				rig.landmarks().get(2)), rig.vessels(), rig.membranes());

		assertInvalid(IchorianSigilAnatomyValidator.validate(3, duplicated), "source");
		assertInvalid(IchorianSigilAnatomyValidator.validate(4, rig), "source");
	}

	@Test
	void rejectsInvalidRolesAndTopology() {
		IchorianSigilAnatomy rig = validRig();
		var twoEyes = replaceLandmark(rig, 1,
				new IchorianSigilAnatomy.Landmark(1, Vec3.ZERO,
						IchorianSigilAnatomy.Role.EYE, 0.18F));
		var badVessel = new IchorianSigilAnatomy(rig.forward(), rig.animation(),
				rig.landmarks(), List.of(new IchorianSigilAnatomy.Vessel(0, 7, 0.1F)),
				rig.membranes());
		var badMembrane = new IchorianSigilAnatomy(rig.forward(), rig.animation(),
				rig.landmarks(), rig.vessels(),
				List.of(new IchorianSigilAnatomy.Membrane(0, 1, 7)));

		assertInvalid(IchorianSigilAnatomyValidator.validate(3, twoEyes), "eye");
		assertInvalid(IchorianSigilAnatomyValidator.validate(3, badVessel), "vessel");
		assertInvalid(IchorianSigilAnatomyValidator.validate(3, badMembrane), "membrane");
	}

	@Test
	void rejectsNonFiniteGeometryAndUnsafeTuning() {
		IchorianSigilAnatomy rig = validRig();
		var zeroForward = new IchorianSigilAnatomy(Vec3.ZERO, rig.animation(),
				rig.landmarks(), rig.vessels(), rig.membranes());
		var badPosition = replaceLandmark(rig, 2,
				new IchorianSigilAnatomy.Landmark(2,
						new Vec3(Double.NaN, 0, 0), IchorianSigilAnatomy.Role.VALVE, 0.12F));
		var badRadius = replaceLandmark(rig, 2,
				new IchorianSigilAnatomy.Landmark(2,
						new Vec3(0.5, 0.1, 0.5), IchorianSigilAnatomy.Role.VALVE, 0.0F));
		var badThickness = new IchorianSigilAnatomy(rig.forward(), rig.animation(),
				rig.landmarks(), List.of(new IchorianSigilAnatomy.Vessel(1, 2, Float.NaN)),
				rig.membranes());
		var badAnimation = new IchorianSigilAnatomy(rig.forward(),
				new IchorianSigilAnatomy.Animation(
						IchorianSigilAnatomy.Style.ARTERIAL_FORK, 2.1F, 0.7F, 0.25F),
				rig.landmarks(), rig.vessels(), rig.membranes());

		assertInvalid(IchorianSigilAnatomyValidator.validate(3, zeroForward), "forward");
		assertInvalid(IchorianSigilAnatomyValidator.validate(3, badPosition), "finite");
		assertInvalid(IchorianSigilAnatomyValidator.validate(3, badRadius), "radius");
		assertInvalid(IchorianSigilAnatomyValidator.validate(3, badThickness), "thickness");
		assertInvalid(IchorianSigilAnatomyValidator.validate(3, badAnimation), "pulse");
	}

	@Test
	void legacyDefinitionConstructorDefaultsNewDataToFallback() {
		IchorianSigilDefinition definition = new IchorianSigilDefinition(
				ResourceLocation.parse("hemomancy:legacy"),
				IchorianSigilDefinition.Kind.SUPPORT, 1, 0xAA0000,
				"Legacy", "Compatibility", 0, 0,
				List.of(new IchorianSigilDefinition.Node(0, 0)));

		assertTrue(definition.connections().isEmpty());
		assertTrue(definition.awakenedForm().isEmpty());
	}

	private static IchorianSigilAnatomy validRig() {
		return new IchorianSigilAnatomy(
				new Vec3(0.0D, 0.0D, -1.0D),
				new IchorianSigilAnatomy.Animation(
						IchorianSigilAnatomy.Style.ARTERIAL_FORK, 1.0F, 0.7F, 0.25F),
				List.of(
						new IchorianSigilAnatomy.Landmark(0, new Vec3(0, 0, -0.6),
								IchorianSigilAnatomy.Role.EYE, 0.14F),
						new IchorianSigilAnatomy.Landmark(1, Vec3.ZERO,
								IchorianSigilAnatomy.Role.ORGAN, 0.18F),
						new IchorianSigilAnatomy.Landmark(2, new Vec3(0.5, 0.1, 0.5),
								IchorianSigilAnatomy.Role.VALVE, 0.12F)),
				List.of(new IchorianSigilAnatomy.Vessel(1, 2, 0.07F)),
				List.of(new IchorianSigilAnatomy.Membrane(0, 1, 2)));
	}

	private static IchorianSigilAnatomy replaceLandmark(IchorianSigilAnatomy rig,
			int index, IchorianSigilAnatomy.Landmark replacement) {
		var landmarks = new java.util.ArrayList<>(rig.landmarks());
		landmarks.set(index, replacement);
		return new IchorianSigilAnatomy(rig.forward(), rig.animation(),
				landmarks, rig.vessels(), rig.membranes());
	}

	private static void assertInvalid(IchorianSigilAnatomyValidator.Result result,
			String expectedMessagePart) {
		assertFalse(result.form().isPresent());
		assertTrue(result.errors().stream()
						.anyMatch(error -> error.toLowerCase().contains(expectedMessagePart)),
				() -> "Expected error containing " + expectedMessagePart + " but got " + result.errors());
	}
}
