package com.vincenthuto.hemomancy.common.rite.sigil;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IchorianSigilLandmarkGeometryTest {
	@Test
	void everyAnatomicalRoleHasADistinctBoundedPrimitiveRecipe() {
		Set<String> signatures = new HashSet<>();
		for (IchorianSigilAnatomy.Role role : IchorianSigilAnatomy.Role.values()) {
			var recipe = IchorianSigilLandmarkGeometry.forRole(role, 73L);
			assertTrue(recipe.core().size() <= 4, role + " core primitive budget");
			assertTrue(recipe.glow().size() <= 2, role + " glow primitive budget");
			assertTrue(recipe.core().stream().allMatch(IchorianSigilLandmarkGeometryTest::bounded));
			assertTrue(recipe.glow().stream().allMatch(IchorianSigilLandmarkGeometryTest::bounded));
			signatures.add(recipe.signature());
		}
		assertEquals(IchorianSigilAnatomy.Role.values().length, signatures.size());
	}

	@Test
	void eyeAlwaysContainsCrimsonIrisBlackPupilAndWetHighlight() {
		var layers = IchorianSigilLandmarkGeometry.forRole(
				IchorianSigilAnatomy.Role.EYE, 11L).core().stream()
				.map(IchorianSigilLandmarkGeometry.Primitive::layer).toList();

		assertTrue(layers.contains(IchorianSigilLandmarkGeometry.Layer.IRIS));
		assertTrue(layers.contains(IchorianSigilLandmarkGeometry.Layer.PUPIL));
		assertTrue(layers.contains(IchorianSigilLandmarkGeometry.Layer.HIGHLIGHT));
	}

	@Test
	void boundaryAnchorUsesItsOwnIrregularClotRecipe() {
		assertTrue(IchorianSigilLandmarkGeometry.boundaryAnchor(19L).signature()
				.startsWith("BOUNDARY_ANCHOR:"));
	}

	private static boolean bounded(IchorianSigilLandmarkGeometry.Primitive primitive) {
		return Math.abs(primitive.x()) <= 1.5F
				&& Math.abs(primitive.y()) <= 1.5F
				&& Math.abs(primitive.z()) <= 1.5F
				&& primitive.scaleX() > 0.0F && primitive.scaleX() <= 1.5F
				&& primitive.scaleY() > 0.0F && primitive.scaleY() <= 1.5F
				&& primitive.scaleZ() > 0.0F && primitive.scaleZ() <= 1.5F;
	}
}
