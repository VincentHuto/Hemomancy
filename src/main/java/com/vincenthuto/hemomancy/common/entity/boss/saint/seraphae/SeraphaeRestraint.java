package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

/**
 * The irons that bind Seraphae, in body order across the HUD: each binding anchor holds one of the first four, a woken
 * reserve anchor chains to her waist. {@code hudX} is the shackle's place along the meter as a fraction of its width.
 */
public enum SeraphaeRestraint {
	LEFT_WING(0.22F, true),
	LEFT_WRIST(0.40F, false),
	RIGHT_WRIST(0.60F, false),
	RIGHT_WING(0.78F, true),
	WAIST(0.50F, false);

	/** The four a binding anchor can hold. */
	public static final SeraphaeRestraint[] BINDING = {LEFT_WING, LEFT_WRIST, RIGHT_WRIST, RIGHT_WING};

	private final float hudX;
	private final boolean wing;

	SeraphaeRestraint(float hudX, boolean wing) {
		this.hudX = hudX;
		this.wing = wing;
	}

	public float hudX() {
		return hudX;
	}

	public boolean wing() {
		return wing;
	}

	public boolean left() {
		return this == LEFT_WING || this == LEFT_WRIST;
	}

	public static SeraphaeRestraint byId(int id) {
		SeraphaeRestraint[] all = values();
		return all[Math.floorMod(id, all.length)];
	}

	/**
	 * Which restraint a binding anchor holds, from where it stands in her chamber's frame: the two nearer the floor hold
	 * the wrists, the two behind her the wing roots; her right hand is the low-z side as she faces the door.
	 */
	public static SeraphaeRestraint forAnchor(boolean behind, boolean lowZ) {
		if (behind) return lowZ ? RIGHT_WING : LEFT_WING;
		return lowZ ? RIGHT_WRIST : LEFT_WRIST;
	}
}
