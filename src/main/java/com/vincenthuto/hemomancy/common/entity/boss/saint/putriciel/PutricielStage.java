package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

/**
 * Where Putriciel's fire cycle stands. ROT is every rot phase; IGNITING runs from the interval's start through the
 * Ignition; BURNING is the cleansing interval proper (fire moves, the two Offerings, the burn-down); EMBERING is the
 * slump after the fire dies, before the rot comes back. The stage drives the Kindling's drift, the presence, the
 * chamber's lights and the char and ember layers on the body.
 */
public enum PutricielStage {
	ROT, IGNITING, BURNING, EMBERING;

	/** The fire burns on the body: the ember layer may draw and fire moves may run. */
	public boolean fire() {
		return this == IGNITING || this == BURNING;
	}

	/** Part of the cleansing interval (the bar is the Purifier's). */
	public boolean interval() {
		return this != ROT;
	}

	public static PutricielStage byId(int id) {
		PutricielStage[] all = values();
		return id >= 0 && id < all.length ? all[id] : ROT;
	}
}
