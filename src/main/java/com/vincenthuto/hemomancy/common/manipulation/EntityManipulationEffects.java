package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;

/**
 * The one way an entity casts a manipulation: the caster must be able to act, the cast counts as school damage from
 * its caster, and the manipulation's own {@link EntityCast} (attached at registration, see {@link EntityCasts})
 * does the work. A manipulation without one does nothing.
 */
public final class EntityManipulationEffects {
	private EntityManipulationEffects() {
	}

	/** Whether a Will may choose this manipulation: it has an entity cast and is not retired. */
	public static boolean isSupported(BloodManipulation manipulation) {
		return manipulation != null && manipulation.getEntityCast().isPresent()
				&& !ManipulationRetirementRules.isRetiredManipulation(manipulation);
	}

	public static boolean cast(BloodManipulation manipulation, ManipulationCastContext context) {
		if (manipulation == null || context == null || context.level().isClientSide
				|| Paralysis.blocksActions(context.caster())) return false;
		try (var schoolCast = SchoolDamage.cast(manipulation, context.caster(), 1)) {
			return manipulation.getEntityCast().map(cast -> cast.cast(context)).orElse(false);
		}
	}
}
