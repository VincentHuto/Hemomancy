package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;

import java.util.UUID;

/** The heart-node sanctum: who may plant one, how far it reaches, and whose death it keeps. */
public final class HeartNodeRules {
	public static final int FOOTPRINT_RADIUS = 8;
	public static final int FOOTPRINT_HEIGHT = 6;
	public static final int RIB_RADIUS = 6;
	public static final int RIBS = 8;
	public static final int RIB_HEIGHT = 3;
	public static final int GROW_TICKS = 100;
	public static final int BONUS_TICKS = 40;
	public static final float BONUS_SPORES = 4f;
	public static final int CACHE_SEARCH = 8;

	private HeartNodeRules() {}

	public static boolean mayPlant(boolean apotheos, boolean inDimension, boolean onNetwork) {
		return apotheos && inDimension && onNetwork;
	}

	public static boolean inFootprint(BlockPos node, BlockPos pos) {
		int dx = pos.getX() - node.getX();
		int dz = pos.getZ() - node.getZ();
		return dx * dx + dz * dz <= FOOTPRINT_RADIUS * FOOTPRINT_RADIUS
				&& Math.abs(pos.getY() - node.getY()) <= FOOTPRINT_HEIGHT;
	}

	public static boolean shouldCache(boolean apotheos, boolean keepInventory, boolean hasNode) {
		return apotheos && !keepInventory && hasNode;
	}

	public static boolean mayOpen(UUID owner, UUID actor) {
		return owner != null && owner.equals(actor);
	}

	/** The base of rib {@code rib}, evenly spaced on the ring around the node. */
	public static BlockPos ribBase(BlockPos node, int rib) {
		double angle = Math.PI * 2 * rib / RIBS;
		return node.offset((int) Math.round(Math.cos(angle) * RIB_RADIUS), 0,
				(int) Math.round(Math.sin(angle) * RIB_RADIUS));
	}
}
