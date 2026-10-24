package com.vincenthuto.hemomancy.common.entity.utility.arbor;

import java.util.UUID;

/** Predicates shared by Arbor trunk and fruit interactions on both sides. */
public final class ArborOfWillInteractionRules {
	/**
	 * Crosshair reach for Arbor fruit. The name label and the right-click use this same reach and target the
	 * same fruit, so any fruit whose name is showing opens the skill tree when clicked.
	 */
	public static final double FRUIT_TARGET_REACH = 8.0;
	/** Server check: the label reach plus slack for the player moving between the click and its packet. */
	public static final double FRUIT_SERVER_REACH = FRUIT_TARGET_REACH + 1.5;

	private ArborOfWillInteractionRules() {
	}

	public static boolean mayOpenTree(UUID owner, UUID actor, boolean inChamber) {
		return owner != null && owner.equals(actor) && inChamber;
	}

	/** Locked fruit focuses too: the label names every visible fruit, and focusing only opens the skill tree. */
	public static boolean mayFocusFruit(UUID owner, UUID actor, boolean inChamber, boolean visible, double distance) {
		return mayOpenTree(owner, actor, inChamber) && visible && distance <= FRUIT_SERVER_REACH;
	}
}
