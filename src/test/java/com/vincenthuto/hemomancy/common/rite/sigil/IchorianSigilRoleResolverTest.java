package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class IchorianSigilRoleResolverTest {
	@Test
	void resolvesTheLandmarkWhoseSourceMatchesTheGroundNode() {
		assertEquals(IchorianSigilAnatomy.Role.HOOK,
				IchorianSigilRoleResolver.forSource(definition(true), 1));
	}

	@Test
	void missingAndLegacyAnatomyFallsBackToJoint() {
		assertEquals(IchorianSigilAnatomy.Role.JOINT,
				IchorianSigilRoleResolver.forSource(definition(false), 1));
		assertEquals(IchorianSigilAnatomy.Role.JOINT,
				IchorianSigilRoleResolver.forSource(definition(true), 7));
	}

	private static IchorianSigilDefinition definition(boolean withAnatomy) {
		Optional<IchorianSigilAnatomy> anatomy = withAnatomy
				? Optional.of(new IchorianSigilAnatomy(new Vec3(0, 0, -1),
						new IchorianSigilAnatomy.Animation(
								IchorianSigilAnatomy.Style.NEEDLE_THREAD, 1, 1, 0),
						List.of(new IchorianSigilAnatomy.Landmark(
								1, Vec3.ZERO, IchorianSigilAnatomy.Role.HOOK, 0.1F)),
						List.of(), List.of()))
				: Optional.empty();
		return new IchorianSigilDefinition(ResourceLocation.parse("hemomancy:role_test"),
				IchorianSigilDefinition.Kind.SUPPORT, 1, 0, "Test", "Test", 0, 0,
				List.of(new IchorianSigilDefinition.Node(0, 0),
						new IchorianSigilDefinition.Node(1, 0)),
				List.of(), anatomy);
	}
}
