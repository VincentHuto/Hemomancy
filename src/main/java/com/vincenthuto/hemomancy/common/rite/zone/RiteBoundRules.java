package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import net.minecraft.network.chat.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Rite-bound things: blocks, items and creatures tagged {@code hemomancy:rite_bound/<purpose|any>} work only inside
 * a rite space whose purpose they name. This decides admission; {@link RiteBound} reads the tags.
 */
public final class RiteBoundRules {
	/** A rite-bound creature outside a matching space unravels after this long. */
	public static final int UNRAVEL_GRACE_TICKS = 100;

	private RiteBoundRules() {}

	/** The spaces a thing works in: one of {@code purposes}, or any space at all. Neither means it is not bound. */
	public record Requirement(Set<CardinalRitePurpose> purposes, boolean any) {
		public static final Requirement NONE = new Requirement(EnumSet.noneOf(CardinalRitePurpose.class), false);

		public Requirement {
			purposes = purposes.isEmpty() ? EnumSet.noneOf(CardinalRitePurpose.class) : EnumSet.copyOf(purposes);
		}

		public boolean bound() {
			return any || !purposes.isEmpty();
		}
	}

	/** Whether a thing with this requirement works where the space (null for none) has this purpose. */
	public static boolean admits(Requirement requirement, CardinalRitePurpose spacePurpose) {
		if (!requirement.bound()) return true;
		if (spacePurpose == null) return false;
		return requirement.any() || requirement.purposes().contains(spacePurpose);
	}

	/**
	 * Only a player is stopped from setting a rite-bound block down outside its space; anything placed by other means
	 * simply stays inert there.
	 */
	public static boolean refusesPlacement(Requirement requirement, boolean byPlayer, CardinalRitePurpose spacePurpose) {
		return byPlayer && !admits(requirement, spacePurpose);
	}

	/** "a Dominion rite space", named by the floor style each purpose is performed on. */
	public static Component describe(Requirement requirement) {
		if (requirement.any()) return Component.translatable("message.hemomancy.rite_bound_rules.any_rite_space");
		Component styles = null;
		for (CardinalRitePurpose purpose : CardinalRitePurpose.values()) {
			if (!requirement.purposes().contains(purpose)) continue;
			Component style = CardinalRiteFloorDefinition.styleName(purpose.floorStyle());
			styles = styles == null ? style
					: Component.translatable("message.hemomancy.rite_bound_rules.either", styles, style);
		}
		return Component.translatable("message.hemomancy.rite_bound_rules.a_rite_space",
				styles == null ? Component.empty() : styles);
	}

	public static boolean unravels(int ticksOutside) {
		return ticksOutside >= UNRAVEL_GRACE_TICKS;
	}
}
