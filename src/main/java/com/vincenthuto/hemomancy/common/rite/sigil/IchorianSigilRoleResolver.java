package com.vincenthuto.hemomancy.common.rite.sigil;

public final class IchorianSigilRoleResolver {
	private IchorianSigilRoleResolver() {
	}

	public static IchorianSigilAnatomy.Role forSource(
			IchorianSigilDefinition definition, int source) {
		return definition.awakenedForm()
				.flatMap(anatomy -> anatomy.landmarks().stream()
						.filter(landmark -> landmark.source() == source)
						.map(IchorianSigilAnatomy.Landmark::role)
						.findFirst())
				.orElse(IchorianSigilAnatomy.Role.JOINT);
	}
}
