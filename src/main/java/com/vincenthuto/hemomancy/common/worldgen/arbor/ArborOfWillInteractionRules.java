package com.vincenthuto.hemomancy.common.worldgen.arbor;

import java.util.UUID;

/** Server-side predicates shared by Arbor trunk and fruit interactions. */
public final class ArborOfWillInteractionRules {
	private ArborOfWillInteractionRules() {
	}

	public static boolean mayOpenTree(UUID owner, UUID actor, boolean inChamber) {
		return owner != null && owner.equals(actor) && inChamber;
	}

	public static boolean mayFocusFruit(UUID owner, UUID actor, boolean inChamber,
			boolean unlocked, double distance, double reach) {
		return mayOpenTree(owner, actor, inChamber) && unlocked && distance <= reach;
	}
}
