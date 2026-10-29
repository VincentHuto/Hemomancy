package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.Direction;

/** How the Gardens' ground answers a body: caps grow into bridges for an Apotheos, gills bite and shut on anyone else. */
public final class GardensTerrainRules {
	public static final int BRIDGE_REACH = 16;
	public static final int BRIDGE_GROW_TICKS = 2;
	public static final int BRIDGE_LIFETIME_TICKS = 600;
	public static final int BRIDGE_LINGER_TICKS = 100;
	public static final double BRIDGE_HOLD_RADIUS = 3.0;
	public static final int GILL_SPREAD = 24;
	public static final int GILL_REOPEN_TICKS = 100;
	public static final int GILL_REOPEN_WAIT = 20;
	public static final float GILL_BITE = 3f;

	private GardensTerrainRules() {}

	public static boolean mayBridge(boolean apotheos, boolean inDimension, boolean emptyHand) {
		return apotheos && inDimension && emptyHand;
	}

	/** The clicked side when it faces outward; from a cap's top or underside, the way the player faces. */
	public static Direction bridgeDirection(Direction face, Direction playerFacing) {
		return face.getAxis().isHorizontal() ? face : playerFacing;
	}

	public static boolean gillSnaps(boolean apotheos, boolean creativeOrSpectator, boolean alive) {
		return alive && !apotheos && !creativeOrSpectator;
	}
}
